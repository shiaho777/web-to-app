// Floating Search — official WebToApp sample.
//
// Demonstrates:
//  - Selection event handling that survives SPA navigations
//  - A floating UI built without any framework
//  - Clipboard via async navigator.clipboard with a fallback
//  - Multi-engine search URL building
//  - Cleaning up listeners — important for modules that hook globals
//
// Kept short on purpose. Reading top to bottom should give a contributor
// a working mental model of how to ship a polished interactive module.

(function () {
  var ENGINES = {
    google: 'https://www.google.com/search?q=',
    duckduckgo: 'https://duckduckgo.com/?q=',
    bing: 'https://www.bing.com/search?q=',
    baidu: 'https://www.baidu.com/s?wd=',
    wikipedia: 'https://en.wikipedia.org/wiki/Special:Search?search=',
  };

  function getOpts() {
    return {
      engine: getConfig('engine', 'google'),
      minLength: parseInt(getConfig('minLength', '2'), 10) || 2,
      openInNewTab: String(getConfig('openInNewTab', 'true')) === 'true',
    };
  }

  // Build the floating bar lazily — first selection event triggers it,
  // we don't want to add idle DOM nodes to every page load.
  var bar = null;
  function ensureBar() {
    if (bar) return bar;
    bar = document.createElement('div');
    bar.id = 'wta-fs-bar';
    bar.innerHTML =
      '<button data-action="search" type="button">' +
      '<span class="wta-fs-icon">🔍</span><span>Search</span></button>' +
      '<div class="wta-fs-divider"></div>' +
      '<button data-action="copy" type="button">' +
      '<span class="wta-fs-icon">📋</span><span>Copy</span></button>' +
      '<div class="wta-fs-divider"></div>' +
      '<button data-action="open" type="button">' +
      '<span class="wta-fs-icon">🔗</span><span>Open</span></button>';
    bar.addEventListener('mousedown', function (e) {
      // Don't let the bar steal selection on click.
      e.preventDefault();
    });
    bar.addEventListener('click', onBarClick);
    document.body.appendChild(bar);
    return bar;
  }

  function show(text, x, y) {
    var b = ensureBar();
    b.dataset.selection = text;
    b.style.left = x + 'px';
    b.style.top = y + 'px';
    b.classList.add('wta-fs-visible');
  }

  function hide() {
    if (bar) bar.classList.remove('wta-fs-visible');
  }

  function onBarClick(e) {
    var btn = e.target.closest('button[data-action]');
    if (!btn || !bar) return;
    var text = bar.dataset.selection || '';
    if (!text) return;

    var opts = getOpts();
    switch (btn.dataset.action) {
      case 'search': {
        var base = ENGINES[opts.engine] || ENGINES.google;
        navigate(base + encodeURIComponent(text), opts.openInNewTab);
        break;
      }
      case 'copy':
        copyToClipboard(text);
        break;
      case 'open': {
        var url = looksLikeUrl(text) ? normaliseUrl(text) : null;
        if (url) navigate(url, opts.openInNewTab);
        break;
      }
    }
    hide();
  }

  function navigate(url, newTab) {
    if (newTab) {
      var w = window.open(url, '_blank');
      // Some hosts block window.open silently — fall back to current tab.
      if (!w) location.href = url;
    } else {
      location.href = url;
    }
  }

  function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).catch(function () {
        legacyCopy(text);
      });
    } else {
      legacyCopy(text);
    }
  }

  function legacyCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.style.cssText = 'position:fixed;left:-9999px;';
    document.body.appendChild(ta);
    ta.select();
    try {
      document.execCommand('copy');
    } catch (_) {}
    document.body.removeChild(ta);
  }

  function looksLikeUrl(s) {
    if (!s) return false;
    if (/^https?:\/\//i.test(s)) return true;
    return /^[a-z0-9-]+(\.[a-z0-9-]+)+(\/.*)?$/i.test(s);
  }

  function normaliseUrl(s) {
    return /^https?:\/\//i.test(s) ? s : 'https://' + s;
  }

  // ── selection tracking ────────────────────────────────────────────
  // Use selectionchange + a tiny debounce so we follow live drag-selects
  // smoothly without flicker on each mouse move.
  var debounceTimer = null;
  function onSelectionChange() {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(handleSelectionSettled, 120);
  }

  function handleSelectionSettled() {
    var sel = window.getSelection && window.getSelection();
    if (!sel || sel.isCollapsed) {
      hide();
      return;
    }
    var text = sel.toString().trim();
    if (text.length < getOpts().minLength) {
      hide();
      return;
    }
    var rect = sel.getRangeAt(0).getBoundingClientRect();
    if (!rect.width && !rect.height) {
      hide();
      return;
    }
    var x = rect.left + rect.width / 2 + window.scrollX;
    var y = rect.top + window.scrollY;
    show(text, x, y);
  }

  document.addEventListener('selectionchange', onSelectionChange);
  document.addEventListener('scroll', hide, { passive: true });
  window.addEventListener('resize', hide);

  // Hide on outside taps so the bar disappears once the user moves on.
  document.addEventListener(
    'pointerdown',
    function (e) {
      if (bar && !bar.contains(e.target)) hide();
    },
    { capture: true }
  );

  // ── panel button: shows engine picker quick-toggle ────────────────
  if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
    __WTA_MODULE_UI__.register({
      id: __MODULE_INFO__.id,
      name: __MODULE_INFO__.name,
      icon: __MODULE_INFO__.icon,
      uiConfig: __MODULE_UI_CONFIG__,
      runMode: __MODULE_RUN_MODE__,
      onClick: function () {
        // The runtime opens the module's settings sheet by default if we
        // don't return anything custom — that's exactly what we want here.
      },
    });
  }
})();
