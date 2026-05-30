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

  function findArticle() {
    var candidates = document.querySelectorAll('article, main, [role=main], section, div');
    var best = null;
    var bestScore = 0;

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

    var shell = document.createElement('div');
    shell.id = ARTICLE_ID;
    shell.dataset.theme = opts.theme;
    shell.dataset.fontSize = opts.fontSize;
    shell.dataset.fontFamily = opts.fontFamily;
    shell.style.maxWidth = opts.maxWidth + 'px';
    shell.innerHTML =
      '<header class="wta-rm-header"><h1>' +
      escapeHtml(title) +
      '</h1></header>' +
      '<div class="wta-rm-body">' +
      html +
      '</div>';

    document.documentElement.classList.add(ROOT_CLASS);

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

  if (sessionStorage.getItem(STATE_KEY) === '1') {
    enter();
    return;
  }

  if (getOpts().autoEnter) {
    var article = findArticle();
    if (article && wordCount(article) >= 800) enter();
  }
})();
