// TV D-Pad Cursor — virtual mouse cursor for Android TV / Fire TV / TV boxes.
//
// Maps D-pad / arrow-key input to a movable on-screen cursor and simulates
// mouse + pointer events at the cursor location. Designed for DOCUMENT_START
// injection: all DOM access is deferred until document.body is available.
//
// Controls:
//   Arrow keys / D-Pad    = Move cursor (hold to accelerate)
//   Enter (short)         = Left click
//   Enter (long ~0.8s)    = Teleport cursor to the settings gear
//   Back / Esc             = Close menu / hide cursor
//   M / Menu               = Toggle settings menu
//
// In the settings menu:
//   Up / Down              = Select row
//   Left / Right           = Adjust value
//   Enter                  = Toggle / activate

(function () {

    // ── Configuration from configItems ────────────────────────────
    var S = {
        cursorSpeed: parseInt(getConfig('cursorSpeed', '10'), 10) || 10,
        fastSpeed:   parseInt(getConfig('fastSpeed', '24'), 10)   || 24,
        accelFrames: parseInt(getConfig('accelFrames', '18'), 10) || 18,
        scrollSpeed: parseInt(getConfig('scrollSpeed', '14'), 10) || 14,
        scrollZone:  parseInt(getConfig('scrollZone', '80'), 10)  || 80,
        idleHideMs:  parseInt(getConfig('idleHideMs', '5000'), 10),
        highlight:   String(getConfig('highlight', 'false')) === 'true',
        pageZoom:    parseInt(getConfig('pageZoom', '100'), 10)   || 100
    };

    // ── Constants ─────────────────────────────────────────────────
    var CURSOR_SIZE   = 24;
    var LONG_PRESS_MS = 800;
    var LS_KEY        = 'wta-tv-cursor-settings';
    var ZOOM_ID       = 'wta-tv-zoom-style';

    // ── Runtime state ─────────────────────────────────────────────
    var x = window.innerWidth / 2;
    var y = window.innerHeight / 2;
    var heldKeys = {};
    var frameCount = {};
    var idleTimer = null;
    var cursorEl = null;
    var gearEl = null;
    var lastTarget = null;
    var menuOpen = false;
    var menuIndex = 0;
    var menuRows = [];
    var longPressTimer = null;
    var didLongPress = false;
    var rafId = null;
    var scrollTick = 0;
    var ready = false;

    // ── Persistence (localStorage only — custom modules get no GM_*) ──
    function loadOverrides() {
        try {
            var raw = localStorage.getItem(LS_KEY);
            if (raw) {
                var saved = JSON.parse(raw);
                if (saved && typeof saved === 'object') {
                    for (var k in saved) {
                        if (saved.hasOwnProperty(k) && S.hasOwnProperty(k)) {
                            S[k] = saved[k];
                        }
                    }
                }
            }
        } catch (e) {}
    }

    function saveOverrides() {
        try {
            localStorage.setItem(LS_KEY, JSON.stringify(S));
        } catch (e) {}
    }

    loadOverrides();

    // ── Page zoom via CSS transform (GeckoView-compatible) ────────
    function applyZoom() {
        var existing = document.getElementById(ZOOM_ID);
        if (!existing) {
            existing = document.createElement('style');
            existing.id = ZOOM_ID;
            (document.head || document.documentElement).appendChild(existing);
        }
        var z = S.pageZoom / 100;
        existing.textContent =
            'html { transform: scale(' + z + '); transform-origin: 0 0; ' +
            'width: ' + (100 / z) + '%; height: ' + (100 / z) + '%; }';
    }

    // ── DOM readiness: defer all body-dependent init ──────────────
    function onReady(fn) {
        if (document.body) {
            fn();
        } else {
            var observer = new MutationObserver(function () {
                if (document.body) {
                    observer.disconnect();
                    fn();
                }
            });
            observer.observe(document.documentElement, { childList: true });
        }
    }

    // ── Cursor element ────────────────────────────────────────────
    function buildCursor() {
        if (cursorEl) return;
        cursorEl = document.createElement('div');
        cursorEl.id = 'wta-tv-cursor';
        document.body.appendChild(cursorEl);
        updateTransform();
    }

    function buildGear() {
        if (gearEl) return;
        gearEl = document.createElement('div');
        gearEl.id = 'wta-tv-gear';
        gearEl.textContent = '\u2699';
        document.body.appendChild(gearEl);
    }

    function clampPos() {
        var half = CURSOR_SIZE / 2;
        x = Math.max(half, Math.min(window.innerWidth - half, x));
        y = Math.max(half, Math.min(window.innerHeight - half, y));
    }

    function updateTransform() {
        if (!cursorEl) return;
        clampPos();
        cursorEl.style.transform = 'translate(' + x + 'px, ' + y + 'px)';

        cursorEl.classList.remove('idle');
        if (idleTimer) clearTimeout(idleTimer);
        if (S.idleHideMs > 0) {
            idleTimer = setTimeout(function () {
                if (cursorEl) cursorEl.classList.add('idle');
            }, S.idleHideMs);
        }

        if (!menuOpen && S.highlight) {
            checkHover();
        } else {
            clearHighlights();
        }

        checkGearHover();
    }

    function clearHighlights() {
        if (lastTarget) {
            lastTarget.classList.remove('wta-tv-hover-target');
            lastTarget = null;
        }
    }

    // ── Clickable detection ───────────────────────────────────────
    function isClickable(el) {
        if (!el || el === document) return false;
        var s = window.getComputedStyle(el);
        if (s.display === 'none' || s.visibility === 'hidden' || parseFloat(s.opacity) === 0) {
            return false;
        }
        var tag = el.tagName;
        if (tag === 'A' || tag === 'BUTTON' || tag === 'INPUT' || tag === 'SELECT' ||
            tag === 'TEXTAREA' || tag === 'VIDEO' || tag === 'LABEL') {
            return true;
        }
        if (el.closest('a') || el.closest('button') || el.closest('[role="button"]') ||
            el.closest('label') || el.closest('video') || el.closest('[onclick]')) {
            return true;
        }
        return false;
    }

    function checkHover() {
        var el = document.elementFromPoint(x, y);
        if (el === lastTarget) return;
        if (lastTarget) {
            lastTarget.dispatchEvent(new MouseEvent('mouseout', { bubbles: true, relatedTarget: el }));
            lastTarget.classList.remove('wta-tv-hover-target');
        }
        lastTarget = el;
        if (el) {
            el.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
            if (isClickable(el)) el.classList.add('wta-tv-hover-target');
        }
    }

    function checkGearHover() {
        if (!gearEl) return;
        var r = gearEl.getBoundingClientRect();
        var over = x >= r.left && x <= r.right && y >= r.top && y <= r.bottom;
        gearEl.classList.toggle('focused', over);
    }

    // ── Event simulation: Mouse + Pointer Events for framework compat ──
    function fireEvents(type, button) {
        var el = document.elementFromPoint(x, y) || document.documentElement;
        var opts = {
            bubbles: true,
            cancelable: true,
            view: window,
            detail: 1,
            screenX: x + window.screenX,
            screenY: y + window.screenY,
            clientX: x,
            clientY: y,
            ctrlKey: false,
            altKey: false,
            shiftKey: false,
            metaKey: false,
            button: button,
            buttons: (type === 'mousedown') ? (1 << button) : 0,
            relatedTarget: null
        };

        // Pointer Events first — many modern frameworks (React 17+,
        // Vue 3) rely on pointerdown rather than mousedown.
        if (typeof PointerEvent !== 'undefined') {
            try {
                el.dispatchEvent(new PointerEvent(
                    type === 'click' ? 'click' : 'pointer' + type,
                    opts
                ));
            } catch (e) {}
        }

        // Mouse Events as the universal fallback.
        el.dispatchEvent(new MouseEvent(type, opts));
        return el;
    }

    function click(button) {
        if (!cursorEl) return;
        button = button || 0;

        // Check if cursor is over the gear button — toggle menu instead
        // of dispatching a DOM click.
        if (gearEl && !menuOpen) {
            var r = gearEl.getBoundingClientRect();
            if (x >= r.left && x <= r.right && y >= r.top && y <= r.bottom) {
                cursorEl.classList.add('clicking');
                toggleMenu(true);
                setTimeout(function () { cursorEl.classList.remove('clicking'); }, 180);
                return;
            }
        }

        cursorEl.classList.add('clicking');
        var el = fireEvents('mousedown', button);
        fireEvents('mouseup', button);
        fireEvents('click', button);
        if (button === 0 && el && el.click) {
            try { el.click(); } catch (e) {}
        }
        if (button === 2) {
            fireEvents('contextmenu', 2);
        }
        setTimeout(function () { cursorEl.classList.remove('clicking'); }, 180);
    }

    // ── Teleport cursor to gear button ────────────────────────────
    function teleportToGear() {
        if (!gearEl || !cursorEl) return;
        var r = gearEl.getBoundingClientRect();
        x = r.left + r.width / 2;
        y = r.top + r.height / 2;
        cursorEl.classList.add('teleporting');
        updateTransform();
        setTimeout(function () {
            if (cursorEl) cursorEl.classList.remove('teleporting');
        }, 300);
    }

    // ── Movement loop (requestAnimationFrame, deterministic) ──────
    function cursorLoop() {
        if (!ready || !cursorEl) {
            rafId = requestAnimationFrame(cursorLoop);
            return;
        }

        var dx = 0, dy = 0, fast = false;

        for (var key in heldKeys) {
            if (!heldKeys.hasOwnProperty(key) || !heldKeys[key]) continue;
            frameCount[key] = (frameCount[key] || 0) + 1;
            if (frameCount[key] > S.accelFrames) fast = true;
        }

        var spd = fast ? S.fastSpeed : S.cursorSpeed;

        if (heldKeys['ArrowUp'])    dy -= spd;
        if (heldKeys['ArrowDown'])  dy += spd;
        if (heldKeys['ArrowLeft'])  dx -= spd;
        if (heldKeys['ArrowRight']) dx += spd;

        if (dx !== 0 || dy !== 0) {
            x += dx;
            y += dy;

            // Edge scrolling — check BEFORE clamping so the cursor can
            // push into the scroll zone to trigger scrolling.
            scrollTick++;
            if (scrollTick % 3 === 0) {
                var M = S.scrollZone;
                var ss = S.scrollSpeed;

                if (dy > 0 && y > window.innerHeight - M) {
                    if (window.scrollY + window.innerHeight < document.documentElement.scrollHeight) {
                        window.scrollBy({ top: ss, behavior: 'auto' });
                    }
                }
                if (dy < 0 && y < M) {
                    if (window.scrollY > 0) {
                        window.scrollBy({ top: -ss, behavior: 'auto' });
                    }
                }
                if (dx > 0 && x > window.innerWidth - M) {
                    if (window.scrollX + window.innerWidth < document.documentElement.scrollWidth) {
                        window.scrollBy({ left: ss, behavior: 'auto' });
                    }
                }
                if (dx < 0 && x < M) {
                    if (window.scrollX > 0) {
                        window.scrollBy({ left: -ss, behavior: 'auto' });
                    }
                }
            }

            updateTransform();
        }

        rafId = requestAnimationFrame(cursorLoop);
    }

    // ── Settings menu ─────────────────────────────────────────────
    function buildMenu() {
        var existing = document.getElementById('wta-tv-menu');
        if (existing) existing.remove();

        var menu = document.createElement('div');
        menu.id = 'wta-tv-menu';

        var items = [
            { id: 'cursorSpeed', label: 'Cursor Speed',    type: 'range',  min: 1,  max: 40,  step: 1 },
            { id: 'fastSpeed',   label: 'Fast Speed',       type: 'range',  min: 10, max: 60,  step: 1 },
            { id: 'accelFrames', label: 'Accel Delay',      type: 'range',  min: 0,  max: 60,  step: 1 },
            { id: 'scrollSpeed', label: 'Scroll Speed',     type: 'range',  min: 0,  max: 40,  step: 1 },
            { id: 'highlight',   label: 'Highlight',         type: 'toggle' },
            { id: 'pageZoom',    label: 'Page Zoom',         type: 'range',  min: 50, max: 200, step: 10, suffix: '%' },
            { id: 'close',       label: 'Close Menu',        type: 'action' }
        ];

        var html = '<h2>TV Cursor Settings</h2>';
        for (var i = 0; i < items.length; i++) {
            var it = items[i];
            var val;
            if (it.type === 'toggle') {
                val = S[it.id] ? 'ON' : 'OFF';
            } else if (it.type === 'action') {
                val = '';
            } else {
                val = S[it.id] + (it.suffix || '');
            }
            var hint = it.type === 'toggle' ? '\u23ce toggle' :
                       it.type === 'action'  ? '\u23ce or Back' :
                       '\u25c0 \u25b6 adjust';
            html += '<div class="wta-tv-row' + (i === 0 ? ' active' : '') +
                    '" data-idx="' + i + '" data-type="' + it.type + '" data-id="' + it.id + '">' +
                    '<span class="label">' + it.label +
                    ' <span class="wta-tv-arrows">\u25c0 \u25b6</span></span>' +
                    '<span class="hint">' + hint + '</span>' +
                    '<span class="value" id="wta-tv-val-' + it.id + '">' + val + '</span>' +
                    '</div>';
        }
        menu.innerHTML = html;
        document.body.appendChild(menu);
        menuRows = Array.prototype.slice.call(menu.querySelectorAll('.wta-tv-row'));
    }

    function updateMenuHighlight() {
        for (var i = 0; i < menuRows.length; i++) {
            menuRows[i].classList.toggle('active', i === menuIndex);
        }
    }

    function toggleMenu(show) {
        if (show) {
            buildMenu();
            menuOpen = true;
            menuIndex = 0;
            var m = document.getElementById('wta-tv-menu');
            if (m) m.classList.add('open');
            if (gearEl) gearEl.classList.add('active');
            if (cursorEl) cursorEl.classList.add('hidden');
            clearHighlights();
        } else {
            menuOpen = false;
            var m2 = document.getElementById('wta-tv-menu');
            if (m2) m2.classList.remove('open');
            if (gearEl) gearEl.classList.remove('active', 'focused');
            if (cursorEl) cursorEl.classList.remove('hidden');
        }
    }

    function adjustMenuValue(dir) {
        var row = menuRows[menuIndex];
        if (!row) return;
        var type = row.dataset.type;
        var id = row.dataset.id;
        if (type !== 'range') return;

        var ranges = {
            cursorSpeed: { min: 1,  max: 40,  step: 1 },
            fastSpeed:   { min: 10, max: 60,  step: 1 },
            accelFrames: { min: 0,  max: 60,  step: 1 },
            scrollSpeed: { min: 0,  max: 40,  step: 1 },
            pageZoom:    { min: 50, max: 200, step: 10 }
        };
        var r = ranges[id];
        if (!r) return;

        S[id] = Math.max(r.min, Math.min(r.max, S[id] + dir * r.step));
        var valEl = document.getElementById('wta-tv-val-' + id);
        if (valEl) valEl.textContent = S[id] + (id === 'pageZoom' ? '%' : '');
        saveOverrides();
        if (id === 'pageZoom') applyZoom();
    }

    function activateMenuItem() {
        var row = menuRows[menuIndex];
        if (!row) return;
        var id = row.dataset.id;
        if (id === 'close') {
            toggleMenu(false);
            return;
        }
        if (row.dataset.type === 'toggle') {
            S[id] = !S[id];
            var valEl = document.getElementById('wta-tv-val-' + id);
            if (valEl) valEl.textContent = S[id] ? 'ON' : 'OFF';
            saveOverrides();
        }
    }

    // ── Keyboard routing ──────────────────────────────────────────
    function onKeyDown(e) {
        var k = e.key;

        // Menu / M toggles settings.
        if (k === 'Menu' || k === 'ContextMenu' || k === 'm' || k === 'M') {
            e.preventDefault();
            e.stopPropagation();
            toggleMenu(!menuOpen);
            return;
        }

        if (menuOpen) {
            // ── Menu navigation ──
            if (k === 'ArrowUp') {
                e.preventDefault();
                e.stopPropagation();
                menuIndex = (menuIndex - 1 + menuRows.length) % menuRows.length;
                updateMenuHighlight();
                return;
            }
            if (k === 'ArrowDown') {
                e.preventDefault();
                e.stopPropagation();
                menuIndex = (menuIndex + 1) % menuRows.length;
                updateMenuHighlight();
                return;
            }
            if (k === 'ArrowLeft') {
                e.preventDefault();
                e.stopPropagation();
                adjustMenuValue(-1);
                return;
            }
            if (k === 'ArrowRight') {
                e.preventDefault();
                e.stopPropagation();
                adjustMenuValue(1);
                return;
            }
            if (k === 'Enter') {
                e.preventDefault();
                e.stopPropagation();
                activateMenuItem();
                return;
            }
            if (k === 'Back' || k === 'Escape') {
                e.preventDefault();
                e.stopPropagation();
                toggleMenu(false);
                return;
            }
            return;
        }

        // ── Cursor mode ──
        if (k === 'ArrowUp' || k === 'ArrowDown' || k === 'ArrowLeft' || k === 'ArrowRight') {
            e.preventDefault();
            e.stopPropagation();
            if (!heldKeys[k]) {
                heldKeys[k] = true;
                frameCount[k] = 0;
            }
            return;
        }

        if (k === 'Enter') {
            e.preventDefault();
            e.stopPropagation();
            if (longPressTimer) clearTimeout(longPressTimer);
            didLongPress = false;
            longPressTimer = setTimeout(function () {
                didLongPress = true;
                teleportToGear();
            }, LONG_PRESS_MS);
            return;
        }

        if (k === 'Back' || k === 'Escape') {
            e.preventDefault();
            e.stopPropagation();
            if (cursorEl) {
                if (cursorEl.classList.contains('hidden')) {
                    cursorEl.classList.remove('hidden');
                } else {
                    cursorEl.classList.add('hidden');
                }
            }
            return;
        }
    }

    function onKeyUp(e) {
        var k = e.key;

        if (k === 'Enter') {
            if (longPressTimer) {
                clearTimeout(longPressTimer);
                longPressTimer = null;
            }
            if (!didLongPress) {
                click(0);
            }
            didLongPress = false;
            e.preventDefault();
            e.stopPropagation();
            return;
        }

        if (k === 'ArrowUp' || k === 'ArrowDown' || k === 'ArrowLeft' || k === 'ArrowRight') {
            heldKeys[k] = false;
            frameCount[k] = 0;
            e.preventDefault();
            e.stopPropagation();
            return;
        }
    }

    // ── Window resize: keep cursor in bounds ──────────────────────
    function onResize() {
        clampPos();
        if (cursorEl) cursorEl.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
    }

    // ── Initialization ────────────────────────────────────────────
    onReady(function () {
        buildCursor();
        buildGear();
        applyZoom();
        ready = true;
    });

    // Keyboard listeners can be attached immediately — they don't
    // depend on document.body.
    window.addEventListener('keydown', onKeyDown, true);
    window.addEventListener('keyup', onKeyUp, true);
    window.addEventListener('resize', onResize);

    // Start the movement loop right away; it waits for `ready` before
    // doing anything visible.
    rafId = requestAnimationFrame(cursorLoop);

})();
