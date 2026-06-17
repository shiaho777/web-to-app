// Reading Mode — official WebToApp sample.
//
// Demonstrates the full module surface in one place:
//  - Reads multiple typed configItems via getConfig()
//  - Heuristic article extraction (no external libs)
//  - Heavy DOM restructuring with safe rollback
//  - Registers a panel button via __WTA_MODULE_UI__ so users can summon it
//  - Persists state across SPA navigations using sessionStorage
//
// The implementation is intentionally simple — under 200 lines — so it
// reads like a tutorial. For production use, wire in a real extraction
// library such as Mozilla's @mozilla/readability.

(function () {
  var STATE_KEY = 'wta-reading-mode-active';
  var ROOT_CLASS = 'wta-reading-mode-root';
  var ARTICLE_ID = 'wta-reading-mode-article';

  function getOpts() {
    return {
      theme: getConfig('theme', 'light'),
      fontSize: getConfig('fontSize', 'medium'),
      fontFamily: getConfig('fontFamily', 'serif'),
      maxWidth: parseInt(getConfig('maxWidth', '720'), 10) || 720,
      autoEnter: String(getConfig('autoEnter', 'false')) === 'true',
    };
  }

  // ── content extraction ────────────────────────────────────────────
  // Score every block element by text density × tag weight, pick the best.
  // Not as accurate as Readability.js but good enough for the common case
  // and trivial to read.
  function findArticle() {
    var candidates = document.querySelectorAll('article, main, [role=main], section, div');
    var best = null;
    var bestScore = 0;

    var AD_PATTERNS = /ad|advert|promo|sidebar|footer|header|nav|menu|cookie|consent|popup|modal|social|share|comment|related|recommend/i;

    for (var i = 0; i < candidates.length; i++) {
      var el = candidates[i];
      if (el === document.body || el === document.documentElement) continue;

      var text = (el.textContent || '').trim();
      var len = text.length;
      if (len < 200) continue;

      var paragraphs = el.querySelectorAll('p').length;
      if (paragraphs < 2) continue;

      var tagBoost = 1;
      var name = el.tagName.toLowerCase();
      if (name === 'article') tagBoost = 4;
      else if (name === 'main') tagBoost = 3;
      else if (el.getAttribute('role') === 'main') tagBoost = 3;

      var links = el.querySelectorAll('a').length;
      var linkDensity = links / Math.max(1, paragraphs);
      var penalty = linkDensity > 3 ? 0.3 : 1;

      var cls = (el.className || '') + ' ' + (el.id || '');
      if (AD_PATTERNS.test(cls)) penalty *= 0.2;

      var commLinks = el.querySelectorAll('a[href*="comment"], a[href*="share"], a[href*="social"]').length;
      if (commLinks > 3) penalty *= 0.5;

      var score = len * tagBoost * penalty;
      if (score > bestScore) {
        bestScore = score;
        best = el;
      }
    }

    return best;
  }

  function wordCount(el) {
    return (el.textContent || '').split(/\s+/).filter(Boolean).length;
  }

  // ── enter / exit ──────────────────────────────────────────────────
  function enter() {
    if (document.documentElement.classList.contains(ROOT_CLASS)) return;

    var article = findArticle();
    if (!article) {
      console.warn('[Reading Mode] no readable article detected');
      return;
    }

    var opts = getOpts();
    var html = article.innerHTML;
    var title = (document.title || '').trim();
    var words = (article.textContent || '').trim().split(/\s+/).filter(Boolean).length;
    var readMin = Math.max(1, Math.round(words / 200));
    var readLabel = readMin + ' min read';

    var shell = document.createElement('div');
    shell.id = ARTICLE_ID;
    shell.dataset.theme = opts.theme;
    shell.dataset.fontSize = String(opts.fontSize);
    shell.dataset.fontFamily = opts.fontFamily;
    shell.style.maxWidth = opts.maxWidth + 'px';
    shell.innerHTML =
      '<header class="wta-rm-header"><h1>' +
      escapeHtml(title) +
      '</h1><span class="wta-rm-meta">' + escapeHtml(readLabel) + ' \u00b7 ' + words + ' words</span></header>' +
      '<div class="wta-rm-body">' +
      html +
      '</div>';

    document.documentElement.classList.add(ROOT_CLASS);
    // Stash the original <body> instead of replacing it — leaving the
    // node tree intact lets the page's own SPA router keep working.
    document.body.dataset.wtaRmCovered = '1';
    document.body.appendChild(shell);

    sessionStorage.setItem(STATE_KEY, '1');
  }

  function exit() {
    var shell = document.getElementById(ARTICLE_ID);
    if (shell && shell.parentNode) shell.parentNode.removeChild(shell);
    document.documentElement.classList.remove(ROOT_CLASS);
    delete document.body.dataset.wtaRmCovered;
    sessionStorage.removeItem(STATE_KEY);
  }

  function toggle() {
    if (sessionStorage.getItem(STATE_KEY) === '1') exit();
    else enter();
  }

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  // ── panel button registration ─────────────────────────────────────
  // Lets users summon Reading Mode from WebToApp's floating panel.
  // The runtime handles the visual button — we just hand it a callback.
  if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
    __WTA_MODULE_UI__.register({
      id: __MODULE_INFO__.id,
      name: __MODULE_INFO__.name,
      icon: __MODULE_INFO__.icon,
      uiConfig: __MODULE_UI_CONFIG__,
      runMode: __MODULE_RUN_MODE__,
      onClick: toggle,
    });
  }

  // ── auto-enter ────────────────────────────────────────────────────
  // Restore reader if user navigated within the same SPA session.
  if (sessionStorage.getItem(STATE_KEY) === '1') {
    enter();
    return;
  }

  // Auto-enter on long-form pages when the option is on.
  if (getOpts().autoEnter) {
    var article = findArticle();
    if (article && wordCount(article) >= 800) enter();
  }
})();
