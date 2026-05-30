(function () {
  var STATE_KEY = 'wta-auto-scroll-active';
  var SPEED_KEY = 'wta-auto-scroll-speed';

  function levelToPxPerSec(level) {
    var n = Math.max(1, Math.min(10, level | 0));
    return 30 * n;
  }

  function getSpeedLevel() {
    var override = sessionStorage.getItem(SPEED_KEY);
    if (override) return parseInt(override, 10) || 5;
    return parseInt(getConfig('speedLevel', '5'), 10) || 5;
  }

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
    var dt = (ts - lastTs) / 1000;
    lastTs = ts;

    var px = levelToPxPerSec(getSpeedLevel()) * dt;
    var before = window.scrollY;
    window.scrollBy(0, px);

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

  document.addEventListener('visibilitychange', function () {
    if (document.hidden) paused = true;
    else if (rafId !== null) {
      paused = false;
      lastTs = 0;
    }
  });

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

  if (sessionStorage.getItem(STATE_KEY) === '1') {
    ensurePanel();
    refreshPanel();
    start();
  }
})();
