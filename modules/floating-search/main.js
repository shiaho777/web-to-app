(function () {
  var ENGINES = {
    google: 'https:
    duckduckgo: 'https:
    bing: 'https:
    baidu: 'https:
    wikipedia: 'https:
  };

  function getOpts() {
    return {
      engine: getConfig('engine', 'google'),
      minLength: parseInt(getConfig('minLength', '2'), 10) || 2,
      openInNewTab: String(getConfig('openInNewTab', 'true')) === 'true',
    };
  }

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
    if (/^https?:\/\
    return /^[a-z0-9-]+(\.[a-z0-9-]+)+(\/.*)?$/i.test(s);
  }

  function normaliseUrl(s) {
    return /^https?:\/\
  }

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

  document.addEventListener(
    'pointerdown',
    function (e) {
      if (bar && !bar.contains(e.target)) hide();
    },
    { capture: true }
  );

  if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
    __WTA_MODULE_UI__.register({
      id: __MODULE_INFO__.id,
      name: __MODULE_INFO__.name,
      icon: __MODULE_INFO__.icon,
      uiConfig: __MODULE_UI_CONFIG__,
      runMode: __MODULE_RUN_MODE__,
      onClick: function () {

      },
    });
  }
})();
