// Auto Scroll — official WebToApp sample.
//
// Demonstrates:
//  - requestAnimationFrame-based smooth scrolling (not setInterval +
//    scrollBy, which produces visible stuttering on devices)
//  - Pause-on-touch and pause-on-tab-hidden
//  - Speed control through a config item
//  - Floating control panel with start/stop + speed slider
//  - State persistence with sessionStorage so navigating to a new page on
//    the same site continues scrolling

(function () {
  var STATE_KEY = 'wta-auto-scroll-active';
  var SPEED_KEY = 'wta-auto-scroll-speed';

  // Speed: pixels-per-second (independent of frame rate). We map the
  // 1..10 user-visible level to 30..300 px/s; level 5 is a leisurely
  // article-reading pace.
  function levelToPxPerSec(level) {
    var n = Math.max(1, Math.min(10, level | 0));
    return 30 * n;
  }

  // Read a possibly-overridden speed from sessionStorage first, else the
  // user's saved config. Lets the floating panel +/- control do live
  // tuning without polluting the persisted value.
  function getSpeedLevel() {
    var override = sessionStorage.getItem(SPEED_KEY);
    if (override) return parseInt(override, 10) || 5;
    return parseInt(getConfig('speedLevel', '5'), 10) || 5;
  }

  // ── core scroll engine ────────────────────────────────────────────
  var rafId = null;
  var lastTs = 0;
  var paused = false;
  var stopOnUser = String(getConfig('pauseOnInteract', 'true')) === 'true';

  function step(ts) {
    if (paused) {
      rafId = requestAnimationFrame(step);
      lastTs = ts;
      return;
    }
    if (!lastTs) lastTs = ts;
    var dt = (ts - lastTs) / 1000; // seconds
    lastTs = ts;

    var px = levelToPxPerSec(getSpeedLevel()) * dt;
    var before = window.scrollY;
    window.scrollBy(0, px);
    // If we didn't move (page bottom or scroll-locked), stop — it's
    // pointless to keep burning frames.
    if (Math.abs(window.scrollY - before) < 0.1) {
      stop();
      return;
    }
    rafId = requestAnimationFrame(step);
  }

  function start() {
    if (rafId !== null) return;
    sessionStorage.setItem(STATE_KEY, '1');
    paused = false;
    lastTs = 0;
    rafId = requestAnimationFrame(step);
    refreshPanel();
  }

  function stop() {
    if (rafId !== null) cancelAnimationFrame(rafId);
    rafId = null;
    sessionStorage.removeItem(STATE_KEY);
    refreshPanel();
  }

  function toggle() {
    if (rafId === null) start();
    else stop();
  }

  // ── pause-on-interaction ──────────────────────────────────────────
  // We pause briefly when the user touches the page (so manual scroll
  // works), then resume after a short idle period.
  var resumeTimer = null;
  function onUserScroll() {
    if (rafId === null || !stopOnUser) return;
    paused = true;
    if (resumeTimer) clearTimeout(resumeTimer);
    resumeTimer = setTimeout(function () {
      paused = false;
      lastTs = 0;
    }, 1500);
  }
  window.addEventListener('wheel', onUserScroll, { passive: true });
  window.addEventListener('touchstart', onUserScroll, { passive: true });
  window.addEventListener(
    'keydown',
    function (e) {
      // Space / arrows / page down — assume user wants control back.
      if (
        e.key === ' ' ||
        e.key === 'ArrowDown' ||
        e.key === 'ArrowUp' ||
        e.key === 'PageDown' ||
        e.key === 'PageUp'
      ) {
        onUserScroll();
      }
    },
    { passive: true }
  );

  // Pause when the tab is backgrounded — saves battery and stops the
  // scroll position drifting while the user does something else.
  document.addEventListener('visibilitychange', function () {
    if (document.hidden) paused = true;
    else if (rafId !== null) {
      paused = false;
      lastTs = 0;
    }
  });

  // ── floating panel ────────────────────────────────────────────────
  // A tiny remote control: play/pause toggle and speed +/-. Built with
  // plain DOM so the code reads end-to-end with no framework knowledge.
  var panel = null;
  function ensurePanel() {
    if (panel) return panel;
    panel = document.createElement('div');
    panel.id = 'wta-as-panel';
    panel.innerHTML =
      '<button data-action="toggle" type="button" class="wta-as-main"></button>' +
      '<button data-action="slower" type="button">−</button>' +
      '<span class="wta-as-speed"></span>' +
      '<button data-action="faster" type="button">+</button>';
    panel.addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-action]');
      if (!btn) return;
      if (btn.dataset.action === 'toggle') toggle();
      if (btn.dataset.action === 'slower') {
        sessionStorage.setItem(SPEED_KEY, String(Math.max(1, getSpeedLevel() - 1)));
        refreshPanel();
      }
      if (btn.dataset.action === 'faster') {
        sessionStorage.setItem(SPEED_KEY, String(Math.min(10, getSpeedLevel() + 1)));
        refreshPanel();
      }
    });
    document.body.appendChild(panel);
    return panel;
  }

  function refreshPanel() {
    if (!panel) return;
    var main = panel.querySelector('.wta-as-main');
    var speed = panel.querySelector('.wta-as-speed');
    if (rafId === null) {
      main.textContent = '▶';
      panel.classList.remove('wta-as-running');
    } else {
      main.textContent = '⏸';
      panel.classList.add('wta-as-running');
    }
    speed.textContent = getSpeedLevel() + '×';
  }

  // ── panel button registration ─────────────────────────────────────
  if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
    __WTA_MODULE_UI__.register({
      id: __MODULE_INFO__.id,
      name: __MODULE_INFO__.name,
      icon: __MODULE_INFO__.icon,
      uiConfig: __MODULE_UI_CONFIG__,
      runMode: __MODULE_RUN_MODE__,
      onClick: function () {
        ensurePanel();
        refreshPanel();
        toggle();
      },
    });
  }

  // ── resume after navigation ───────────────────────────────────────
  if (sessionStorage.getItem(STATE_KEY) === '1') {
    ensurePanel();
    refreshPanel();
    start();
  }
})();
