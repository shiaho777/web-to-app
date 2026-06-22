(function () {
  'use strict';

  const COUNTDOWN    = 5; // seconds before auto-open
  const VIDEO_EXTS   = /\.(mp4|mkv|avi|mov|wmv|flv|webm|m4v|mpg|mpeg|3gp|ts|m3u8)(\?.*)?$/i;

  // ── Exclusion list ─────────────────────────────────────────────
  const EXCLUDE = [
    'test-videos.co.uk',
    'bigbuckbunny',
    'sample-videos.com',
    'w3schools.com',
  ];

  function isExcluded(url) { return EXCLUDE.some(e => url.includes(e)); }
  function isVideoUrl(url) { return typeof url === 'string' && VIDEO_EXTS.test(url) && !isExcluded(url); }

  // ── URL registry ───────────────────────────────────────────────
  const detected = new Map();
  let currentUrl  = null;
  let countdownStarted = false;

  function pickBest() {
    for (const [url] of detected) if (/\.mp4/i.test(url) && !url.includes('download')) return url;
    for (const [url] of detected) if (/\.mp4/i.test(url)) return url;
    for (const [url] of detected) if (/master\.m3u8/i.test(url)) return url;
    return detected.keys().next().value || null;
  }

  function register(url, label) {
    if (!url || !isVideoUrl(url) || detected.has(url)) return;
    detected.set(url, label);
    const best = pickBest();
    if (best && best !== currentUrl) {
      currentUrl = best;
      if (!countdownStarted) startCountdown();
    }
  }

  // ── Open in player ─────────────────────────────────────────────
  function openInPlayer(url) {
    window.location.href =
      `intent:${url}#Intent;` +
      `action=android.intent.action.VIEW;` +
      `type=video/*;` +
      `end`;
  }

  // ── XHR intercept ─────────────────────────────────────────────
  const OrigXHR = window.XMLHttpRequest;
  window.XMLHttpRequest = function () {
    const xhr = new OrigXHR();
    const origOpen = xhr.open.bind(xhr);
    xhr.open = function (method, url) {
      try { if (isVideoUrl(url)) register(url, 'XHR'); } catch {}
      return origOpen.apply(xhr, arguments);
    };
    return xhr;
  };
  Object.defineProperties(window.XMLHttpRequest, Object.getOwnPropertyDescriptors(OrigXHR));

  // ── fetch intercept ───────────────────────────────────────────
  const origFetch = window.fetch;
  window.fetch = function (input) {
    try {
      const url = typeof input === 'string' ? input : (input && input.url);
      if (url && isVideoUrl(url)) register(url, 'fetch');
    } catch {}
    return origFetch.apply(this, arguments);
  };

  // ── JWPlayer hook ─────────────────────────────────────────────
  let jwHooked = false;
  function hookJWPlayer() {
    if (jwHooked || !window.jwplayer) return;
    jwHooked = true;
    const origJW = window.jwplayer;
    window.jwplayer = function () {
      const player = origJW.apply(this, arguments);
      if (player && player.setup) {
        const origSetup = player.setup.bind(player);
        player.setup = function (config) {
          try { extractFromJWConfig(config); } catch {}
          return origSetup(config);
        };
      }
      return player;
    };
    Object.assign(window.jwplayer, origJW);
  }

  function extractFromJWConfig(config) {
    if (!config) return;
    if (config.file) register(config.file, 'JWPlayer');
    if (Array.isArray(config.sources))  config.sources.forEach(s => s && s.file && register(s.file, 'JWPlayer'));
    if (Array.isArray(config.playlist)) config.playlist.forEach(item => {
      if (item && item.file) register(item.file, 'JWPlayer');
      if (Array.isArray(item.sources)) item.sources.forEach(s => s && s.file && register(s.file, 'JWPlayer'));
    });
  }

  const jwPoll = setInterval(() => { if (window.jwplayer) { hookJWPlayer(); clearInterval(jwPoll); } }, 200);
  setTimeout(() => clearInterval(jwPoll), 15000);

  // ── video.src property trap ───────────────────────────────────
  const origSrc = Object.getOwnPropertyDescriptor(HTMLMediaElement.prototype, 'src');
  if (origSrc) {
    Object.defineProperty(HTMLMediaElement.prototype, 'src', {
      set(url) {
        try { if (isVideoUrl(url)) register(url, 'video.src'); } catch {}
        return origSrc.set.call(this, url);
      },
      get() { return origSrc.get.call(this); },
      configurable: true,
    });
  }

  // ── Shadow DOM UI ──────────────────────────────────────────────
  const SHADOW_CSS = `
    :host {
      all: initial;
      position: fixed;
      z-index: 2147483647;
      pointer-events: none;
    }

    /* Countdown banner — top centre */
    #countdown-bar {
      position: fixed;
      top: 14px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(10,10,10,0.96);
      color: #fff;
      font-family: sans-serif;
      font-size: 14px;
      padding: 10px 18px;
      border-radius: 24px;
      border: 1px solid #333;
      box-shadow: 0 4px 16px rgba(0,0,0,0.8);
      display: flex;
      align-items: center;
      gap: 12px;
      pointer-events: all;
      opacity: 0;
      transition: opacity 0.3s;
      white-space: nowrap;
    }
    #countdown-bar.visible { opacity: 1; }

    #countdown-num {
      background: #e53935;
      color: #fff;
      font-weight: bold;
      font-size: 15px;
      border-radius: 50%;
      width: 28px;
      height: 28px;
      line-height: 28px;
      text-align: center;
      flex-shrink: 0;
    }

    #cancel-btn {
      background: rgba(255,255,255,0.12);
      color: #fff;
      border: 1px solid #555;
      border-radius: 12px;
      padding: 3px 12px;
      font-size: 12px;
      cursor: pointer;
      font-family: sans-serif;
    }
    #cancel-btn:active { background: rgba(255,255,255,0.25); }

    /* Fallback button — bottom right — shown after cancel */
    #fallback-btn {
      position: fixed;
      bottom: 160px;
      right: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      background: rgba(15,15,15,0.95);
      color: #fff;
      font-family: sans-serif;
      font-size: 14px;
      font-weight: bold;
      padding: 10px 16px;
      border-radius: 24px;
      border: 1px solid #444;
      box-shadow: 0 4px 16px rgba(0,0,0,0.8);
      cursor: pointer;
      pointer-events: all;
      user-select: none;
      touch-action: none;
      opacity: 0;
      transform: scale(0.85);
      transition: opacity 0.3s, transform 0.3s;
    }
    #fallback-btn.visible { opacity: 1; transform: scale(1); }
    #fallback-btn:active  { background: rgba(40,40,40,0.98); }
    .fb-icon { font-size: 20px; }
    .fb-sub  { font-size: 10px; font-weight: normal; color: #aaa; display: block; }
  `;

  let host, shadow, countdownBar, countdownNum, cancelBtn, fallbackBtn;
  let countdownTimer = null, countdownVal = COUNTDOWN;
  let cancelled = false;

  // Drag state for fallback button
  let isDragging = false, dragMoved = false, startX, startY;

  function buildUI() {
    host   = document.createElement('div');
    shadow = host.attachShadow({ mode: 'open' });

    const style = document.createElement('style');
    style.textContent = SHADOW_CSS;
    shadow.appendChild(style);

    // Countdown banner
    countdownBar = document.createElement('div');
    countdownBar.id = 'countdown-bar';
    countdownNum = document.createElement('div');
    countdownNum.id = 'countdown-num';
    countdownNum.textContent = COUNTDOWN;

    const label = document.createElement('span');
    label.textContent = '📺 Opening in player…';

    cancelBtn = document.createElement('div');
    cancelBtn.id = 'cancel-btn';
    cancelBtn.textContent = 'Cancel';
    cancelBtn.addEventListener('pointerup', e => { e.stopPropagation(); cancel(); });

    countdownBar.appendChild(countdownNum);
    countdownBar.appendChild(label);
    countdownBar.appendChild(cancelBtn);
    shadow.appendChild(countdownBar);

    // Fallback button (shown after cancel)
    fallbackBtn = document.createElement('div');
    fallbackBtn.id = 'fallback-btn';
    fallbackBtn.innerHTML = `
      <span class="fb-icon">📺</span>
      <span>Open with<span class="fb-sub">Choose Player</span></span>`;

    fallbackBtn.addEventListener('pointerdown', e => {
      isDragging = true; dragMoved = false;
      fallbackBtn.setPointerCapture(e.pointerId);
      const rect = fallbackBtn.getBoundingClientRect();
      fallbackBtn.style.bottom = 'auto'; fallbackBtn.style.right = 'auto';
      fallbackBtn.style.left = rect.left + 'px'; fallbackBtn.style.top = rect.top + 'px';
      startX = e.clientX - rect.left; startY = e.clientY - rect.top;
      e.preventDefault();
    });
    fallbackBtn.addEventListener('pointermove', e => {
      if (!isDragging) return;
      dragMoved = true;
      const maxX = window.innerWidth  - fallbackBtn.offsetWidth;
      const maxY = window.innerHeight - fallbackBtn.offsetHeight;
      fallbackBtn.style.left = Math.max(0, Math.min(e.clientX - startX, maxX)) + 'px';
      fallbackBtn.style.top  = Math.max(0, Math.min(e.clientY - startY, maxY)) + 'px';
      e.preventDefault();
    });
    fallbackBtn.addEventListener('pointerup', () => {
      isDragging = false;
      if (!dragMoved && currentUrl) openInPlayer(currentUrl);
    });

    shadow.appendChild(fallbackBtn);
    document.body.appendChild(host);
  }

  function startCountdown() {
    if (countdownStarted || cancelled) return;
    countdownStarted = true;
    countdownVal = COUNTDOWN;

    if (!countdownBar) return; // UI not ready yet — will be called again after buildUI
    countdownBar.classList.add('visible');
    countdownNum.textContent = countdownVal;

    countdownTimer = setInterval(() => {
      countdownVal--;
      countdownNum.textContent = countdownVal;
      if (countdownVal <= 0) {
        clearInterval(countdownTimer);
        countdownBar.classList.remove('visible');
        openInPlayer(currentUrl);
      }
    }, 1000);
  }

  function cancel() {
    cancelled = true;
    countdownStarted = false;
    clearInterval(countdownTimer);
    countdownBar.classList.remove('visible');
    // Show fallback button so user can still open manually
    fallbackBtn.classList.add('visible');
  }

  // ── Also scan page URL and inline scripts ─────────────────────
  function checkPageUrl() {
    if (isVideoUrl(location.href)) register(location.href, 'Page URL');
  }

  function scanInlineScripts() {
    const re = /['"`](https?:\/\/[^'"`\s]{10,}\.(?:mp4|mkv|m3u8|webm)[^'"`\s]*?)['"`]/gi;
    document.querySelectorAll('script:not([src])').forEach(s => {
      for (const m of s.textContent.matchAll(re)) register(m[1], 'Script');
    });
  }

  function checkVideoLinks() {
    document.querySelectorAll('a[href]').forEach(a => {
      if (isVideoUrl(a.href) && !a._owbAttached) {
        a._owbAttached = true;
        a.addEventListener('click', e => { e.preventDefault(); register(a.href, 'Link'); });
      }
    });
  }

  // ── Init ───────────────────────────────────────────────────────
  checkPageUrl();

  function init() {
    buildUI();

    // If countdown was already triggered before UI was ready, start it now
    if (countdownStarted) {
      countdownStarted = false;
      startCountdown();
    } else if (currentUrl) {
      startCountdown();
    }

    checkVideoLinks();
    scanInlineScripts();
    setTimeout(() => { checkVideoLinks(); scanInlineScripts(); }, 2000);

    new MutationObserver(() => checkVideoLinks())
      .observe(document.documentElement, { childList: true, subtree: true });
  }

  if (document.body) {
    init();
  } else {
    document.addEventListener('DOMContentLoaded', init);
  }

})();
