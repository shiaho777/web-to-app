(function() {
    'use strict';

    /* --------------- CONTROLS --------------- *
     * Arrow Keys / D-Pad  = Move cursor
     * Enter (short)      = Click
     * Enter (hold ~0.8s) = Teleport to gear
     * Esc / Back         = Back / hide cursor / close menu
     * M / Menu           = Toggle settings menu
     * ---------------------------------------- */

    const LS_KEY = 'wta-mouse-settings-v4';

    const defaults = {
        cursorSpeed: 10,
        cursorFast: 24,
        accelAfter: 18,
        scrollSpeed: 14,
        scrollZone: 80,
        highlight: false,
        zoom: 100,
    };

    // ═══════════════════════════════════════════════════════════════
    // STORAGE: GM_* (Tampermonkey) → localStorage fallback
    // ═══════════════════════════════════════════════════════════════
    function storeGet(key, defaultValue) {
        if (typeof GM_getValue !== 'undefined') {
            try {
                const val = GM_getValue(key);
                return val !== undefined ? val : defaultValue;
            } catch(e) {}
        }
        try {
            const val = localStorage.getItem(key);
            return val !== null ? JSON.parse(val) : defaultValue;
        } catch(e) {
            return defaultValue;
        }
    }

    function storeSet(key, value) {
        if (typeof GM_setValue !== 'undefined') {
            try { GM_setValue(key, value); return; } catch(e) {}
        }
        try { localStorage.setItem(key, JSON.stringify(value)); } catch(e) {}
    }

    function loadSettings() {
        const saved = storeGet(LS_KEY, null);
        if (saved && typeof saved === 'object') {
            return { ...defaults, ...saved };
        }
        return { ...defaults };
    }

    function saveSettings() {
        storeSet(LS_KEY, S);
    }

    let S = loadSettings();

    const CFG = {
        CURSOR_SIZE: 24,
        IDLE_HIDE: 5000,
        LONG_PRESS_MS: 800,
    };

    // ─── STATE ───
    let x = window.innerWidth / 2;
    let y = window.innerHeight / 2;
    let heldKeys = new Set();
    let frameCount = {};
    let idleTimer;
    let cursorEl, lastTarget;
    let menuOpen = false;
    let menuIndex = 0;
    let menuItems = [];
    let zoomStyleEl = null;
    let longPressTimer = null;
    let didLongPress = false;
    let rafId = null;
    let scrollFrame = 0;

    // Gamepad
    let gamepadIndex = null;

    // ─── CSS ───
    const style = document.createElement('style');
    style.textContent = `
        #wta-firetv-cursor {
            position: fixed;
            top: 0; left: 0;
            width: ${CFG.CURSOR_SIZE}px;
            height: ${CFG.CURSOR_SIZE}px;
            margin-left: -${CFG.CURSOR_SIZE/2}px;
            margin-top: -${CFG.CURSOR_SIZE/2}px;
            background: radial-gradient(circle, rgba(0,200,255,0.95) 30%, rgba(0,200,255,0.3) 70%);
            border: 2.5px solid #fff;
            border-radius: 50%;
            box-shadow: 0 0 12px rgba(0,200,255,0.9), 0 0 30px rgba(0,200,255,0.3);
            z-index: 2147483647;
            pointer-events: none;
            will-change: transform;
            transition: transform 0.05s linear, opacity 0.3s ease;
            transform: translate(0,0);
        }
        #wta-firetv-cursor.teleporting { transition: transform 0.25s cubic-bezier(0.2,0.8,0.2,1); }
        #wta-firetv-cursor.clicking { transform: translate(0,0) scale(0.75); background: #4cff4c; border-color: #fff; box-shadow: 0 0 15px #4cff4c; }
        #wta-firetv-cursor.idle { opacity: 0.25; }
        #wta-firetv-cursor.hidden { opacity: 0; pointer-events: none; }

        .wta-hover-target { outline: 3px solid rgba(0,200,255,0.6) !important; outline-offset: 2px !important; transition: outline 0.15s; }

        #wta-settings-btn {
            position: fixed;
            bottom: 20px;
            right: 20px;
            width: 48px;
            height: 48px;
            background: rgba(20,20,30,0.85);
            border: 2px solid rgba(255,255,255,0.3);
            border-radius: 12px;
            color: #fff;
            font-size: 22px;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2147483646;
            pointer-events: none !important;
            user-select: none;
            backdrop-filter: blur(6px);
            transition: transform 0.15s, border-color 0.15s, box-shadow 0.15s;
        }
        #wta-settings-btn.focused {
            border-color: #00c8ff;
            transform: scale(1.1);
            box-shadow: 0 0 15px rgba(0,200,255,0.4);
        }
        #wta-settings-btn.active { background: rgba(0,200,255,0.2); }

        #wta-settings-menu {
            position: fixed;
            top: 50%; left: 50%;
            transform: translate(-50%, -50%);
            width: 480px;
            max-width: 80vw;
            background: rgba(20,20,30,0.95);
            border: 2px solid rgba(255,255,255,0.15);
            border-radius: 16px;
            padding: 20px 24px;
            z-index: 2147483647;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            color: #fff;
            display: none;
            backdrop-filter: blur(12px);
            box-shadow: 0 20px 60px rgba(0,0,0,0.6);
        }
        #wta-settings-menu.open { display: block; }

        #wta-settings-menu h2 {
            margin: 0 0 20px 0;
            font-size: 20px;
            font-weight: 600;
            color: #00c8ff;
            border-bottom: 1px solid rgba(255,255,255,0.1);
            padding-bottom: 12px;
        }

        .wta-menu-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 14px 16px;
            margin: 6px -8px;
            border-radius: 10px;
            font-size: 16px;
            transition: background 0.15s, box-shadow 0.15s;
            border: 2px solid transparent;
        }
        .wta-menu-row .label { display: flex; align-items: center; gap: 10px; }
        .wta-menu-row .value { font-weight: 600; color: #00c8ff; font-variant-numeric: tabular-nums; }
        .wta-menu-row .hint { font-size: 12px; color: rgba(255,255,255,0.4); margin-left: auto; margin-right: 12px; }
        .wta-menu-row.active {
            background: rgba(0,200,255,0.15);
            border-color: #00c8ff;
            box-shadow: 0 0 12px rgba(0,200,255,0.25);
        }
        .wta-menu-row.close-btn { justify-content: center; color: #ff5555; font-weight: 600; margin-top: 10px; border-top: 1px solid rgba(255,255,255,0.1); }
        .wta-menu-row.close-btn.active { background: rgba(255,85,85,0.15); border-color: #ff5555; box-shadow: 0 0 12px rgba(255,85,85,0.25); }
        .wta-arrows { opacity: 0; transition: opacity 0.15s; font-size: 12px; color: rgba(255,255,255,0.5); }
        .wta-menu-row.active .wta-arrows { opacity: 1; }
    `;
    document.head.appendChild(style);

    // ─── ZOOM ───
    function applyZoom() {
        if (!zoomStyleEl) {
            zoomStyleEl = document.createElement('style');
            zoomStyleEl.id = 'wta-zoom-style';
            document.head.appendChild(zoomStyleEl);
        }
        const z = S.zoom / 100;
        zoomStyleEl.textContent = `html, body { zoom: ${z}; }`;
    }

    // ─── CURSOR ───
    function initCursor() {
        if (document.getElementById('wta-firetv-cursor')) return;
        cursorEl = document.createElement('div');
        cursorEl.id = 'wta-firetv-cursor';
        document.body.appendChild(cursorEl);
        updateTransform();
    }

    function updateTransform() {
        // Clamp
        x = Math.max(CFG.CURSOR_SIZE/2, Math.min(window.innerWidth - CFG.CURSOR_SIZE/2, x));
        y = Math.max(CFG.CURSOR_SIZE/2, Math.min(window.innerHeight - CFG.CURSOR_SIZE/2, y));

        cursorEl.style.transform = `translate(${x}px, ${y}px)`;

        cursorEl.classList.remove('idle');
        clearTimeout(idleTimer);
        idleTimer = setTimeout(() => cursorEl.classList.add('idle'), CFG.IDLE_HIDE);

        if (!menuOpen && S.highlight) checkHover();
        else clearHighlights();

        checkGearHover();
    }

    function clearHighlights() {
        if (lastTarget) {
            lastTarget.classList.remove('wta-hover-target');
            lastTarget = null;
        }
    }

    // ─── MOUSE EVENTS ───
    function fireMouse(type, opts = {}) {
        const el = document.elementFromPoint(x, y) || document.documentElement;
        const evt = new MouseEvent(type, {
            bubbles: true, cancelable: true, view: window, detail: 1,
            screenX: x + window.screenX, screenY: y + window.screenY,
            clientX: x, clientY: y,
            ctrlKey: false, altKey: false, shiftKey: false, metaKey: false,
            button: opts.button ?? 0, relatedTarget: opts.relatedTarget || null,
            ...opts
        });
        el.dispatchEvent(evt);
        return el;
    }

    function click(button = 0) {
        if (!cursorEl) return;

        // EXPLICIT GEAR BUTTON CHECK (no DOM event dispatch)
        const gearBtn = document.getElementById('wta-settings-btn');
        if (gearBtn && !menuOpen) {
            const r = gearBtn.getBoundingClientRect();
            const isOverGear = x >= r.left && x <= r.right && y >= r.top && y <= r.bottom;
            if (isOverGear) {
                cursorEl.classList.add('clicking');
                if (window.event) {
                    window.event.stopPropagation();
                    window.event.stopImmediatePropagation();
                    window.event.preventDefault();
                }
                toggleMenu(!menuOpen);
                setTimeout(() => cursorEl.classList.remove('clicking'), 180);
                return;
            }
        }

        cursorEl.classList.add('clicking');
        const el = fireMouse('mousedown', { button });
        fireMouse('mouseup', { button });
        fireMouse('click', { button });
        if (button === 0 && el?.click) { try { el.click(); } catch(e){} }
        if (button === 2) fireMouse('contextmenu');
        setTimeout(() => cursorEl.classList.remove('clicking'), 180);
    }

    function isClickable(el) {
        if (!el) return false;
        const s = window.getComputedStyle(el);
        if (s.display === 'none' || s.visibility === 'hidden' || parseFloat(s.opacity) === 0) return false;
        const tag = el.tagName;
        return tag === 'A' || tag === 'BUTTON' || tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA' ||
               tag === 'VIDEO' || tag === 'LABEL' || el.onclick || el.closest('a') || el.closest('button') ||
               el.closest('[role="button"]') || el.closest('label') || el.closest('video');
    }

    function checkHover() {
        const el = document.elementFromPoint(x, y);
        if (el === lastTarget) return;
        if (lastTarget) {
            lastTarget.dispatchEvent(new MouseEvent('mouseout', { bubbles: true, relatedTarget: el }));
            lastTarget.classList.remove('wta-hover-target');
        }
        lastTarget = el;
        if (el) {
            el.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
            if (isClickable(el)) el.classList.add('wta-hover-target');
        }
    }

    function checkGearHover() {
        const btn = document.getElementById('wta-settings-btn');
        if (!btn) return;
        const r = btn.getBoundingClientRect();
        const isOverGear = x >= r.left && x <= r.right && y >= r.top && y <= r.bottom;
        btn.classList.toggle('focused', isOverGear);
    }

    // ─── TELEPORT TO GEAR ───
    function teleportToGear() {
        const btn = document.getElementById('wta-settings-btn');
        if (!btn) return;
        const r = btn.getBoundingClientRect();
        x = r.left + r.width/2;
        y = r.top + r.height/2;
        cursorEl.classList.add('teleporting');
        updateTransform();
        setTimeout(() => cursorEl.classList.remove('teleporting'), 300);
    }

    // ═══════════════════════════════════════════════════════════════
    // DETERMINISTIC MOVEMENT LOOP
    // ═══════════════════════════════════════════════════════════════
    function cursorLoop() {
        if (!cursorEl) { rafId = requestAnimationFrame(cursorLoop); return; }

        let dx = 0, dy = 0, fast = false;

        for (const key of heldKeys) {
            frameCount[key] = (frameCount[key] || 0) + 1;
            if (frameCount[key] > S.accelAfter) fast = true;
        }

        const spd = fast ? S.cursorFast : S.cursorSpeed;

        if (heldKeys.has('ArrowUp'))    dy -= spd;
        if (heldKeys.has('ArrowDown'))  dy += spd;
        if (heldKeys.has('ArrowLeft'))  dx -= spd;
        if (heldKeys.has('ArrowRight')) dx += spd;

        if (dx !== 0 || dy !== 0) {
            // ─── Apply movement ───
            x += dx;
            y += dy;

            // ─── SCROLLING (decide BEFORE clamping) ───
            scrollFrame++;
            if (scrollFrame % 3 === 0) {
                const M = S.scrollZone;
                const s = S.scrollSpeed;

                // Use unclamped coordinates for scroll logic
                const futureY = y;
                const futureX = x;

                // Vertical scroll
                if (dy > 0 && futureY > window.innerHeight - M) {
                    if (window.scrollY + window.innerHeight < document.documentElement.scrollHeight) {
                        window.scrollBy({ top: s, behavior: 'auto' });
                    }
                }
                if (dy < 0 && futureY < M) {
                    if (window.scrollY > 0) {
                        window.scrollBy({ top: -s, behavior: 'auto' });
                    }
                }

                // Horizontal scroll
                if (dx > 0 && futureX > window.innerWidth - M) {
                    if (window.scrollX + window.innerWidth < document.documentElement.scrollWidth) {
                        window.scrollBy({ left: s, behavior: 'auto' });
                    }
                }
                if (dx < 0 && futureX < M) {
                    if (window.scrollX > 0) {
                        window.scrollBy({ left: -s, behavior: 'auto' });
                    }
                }
            }

            // ─── NOW clamp to viewport ───
            x = Math.max(CFG.CURSOR_SIZE/2, Math.min(window.innerWidth - CFG.CURSOR_SIZE/2, x));
            y = Math.max(CFG.CURSOR_SIZE/2, Math.min(window.innerHeight - CFG.CURSOR_SIZE/2, y));

            updateTransform();
        }

        rafId = requestAnimationFrame(cursorLoop);
    }

    rafId = requestAnimationFrame(cursorLoop);

    // ─── GAMEPAD CONNECTION ───
    window.addEventListener('gamepadconnected', (e) => {
        gamepadIndex = e.gamepad.index;
        console.log('[Android TV D-Pad] Gamepad connected:', e.gamepad.id);
    });
    window.addEventListener('gamepaddisconnected', (e) => {
        if (gamepadIndex === e.gamepad.index) gamepadIndex = null;
    });

    // ─── SETTINGS UI ───
    function buildMenu() {
        const existing = document.getElementById('wta-settings-menu');
        if (existing) existing.remove();

        const menu = document.createElement('div');
        menu.id = 'wta-settings-menu';

        const items = [
            { id: 'cursorSpeed', label: '⚡ Cursor Speed', type: 'range', min: 1, max: 40, step: 1, hint: '← → adjust' },
            { id: 'cursorFast',  label: '🔥 Fast Speed',    type: 'range', min: 10, max: 60, step: 1, hint: '← → adjust' },
            { id: 'accelAfter',  label: '⏱️ Accel Delay',   type: 'range', min: 0, max: 60, step: 1, hint: 'frames ← →' },
            { id: 'scrollSpeed', label: '📜 Scroll Speed',  type: 'range', min: 0, max: 40, step: 1, hint: '← → adjust' },
            { id: 'highlight',   label: '🔦 Highlight',      type: 'toggle', hint: '⏎ toggle' },
            { id: 'zoom',        label: '🔍 Page Zoom',       type: 'range', min: 50, max: 200, step: 10, suffix: '%', hint: '← → adjust' },
            { id: 'close',       label: '✖ Close Menu',       type: 'action', hint: '⏎ or Back' },
        ];

        let html = '<h2>Mouse Settings</h2><div id="wta-menu-list">';
        items.forEach((it, i) => {
            const val = it.type === 'toggle' ? (S[it.id] ? 'ON' : 'OFF') : (S[it.id] + (it.suffix || ''));
            html += `
                <div class="wta-menu-row ${i===0?'active':''}" data-idx="${i}" data-type="${it.type}" data-id="${it.id}">
                    <span class="label">${it.label} <span class="wta-arrows">◀ ▶</span></span>
                    <span class="hint">${it.hint}</span>
                    <span class="value" id="wta-val-${it.id}">${val}</span>
                </div>
            `;
        });
        html += '</div>';
        menu.innerHTML = html;
        document.body.appendChild(menu);
        menuItems = Array.from(menu.querySelectorAll('.wta-menu-row'));
    }

    function updateMenuHighlight() {
        menuItems.forEach((row, i) => row.classList.toggle('active', i === menuIndex));
    }

    function toggleMenu(show) {
        if (show) {
            buildMenu();
            menuOpen = true;
            menuIndex = 0;
            document.getElementById('wta-settings-menu').classList.add('open');
            const btn = document.getElementById('wta-settings-btn');
            if (btn) btn.classList.add('active');
            if (cursorEl) cursorEl.classList.add('hidden');
            clearHighlights();
        } else {
            menuOpen = false;
            const m = document.getElementById('wta-settings-menu');
            if (m) m.classList.remove('open');
            const btn = document.getElementById('wta-settings-btn');
            if (btn) btn.classList.remove('active', 'focused');
            if (cursorEl) cursorEl.classList.remove('hidden');
        }
    }

    function adjustMenuValue(dir) {
        const row = menuItems[menuIndex];
        if (!row) return;
        const type = row.dataset.type;
        const id = row.dataset.id;
        const item = { cursorSpeed:{min:1,max:40,step:1}, cursorFast:{min:10,max:60,step:1}, accelAfter:{min:0,max:60,step:1}, scrollSpeed:{min:0,max:40,step:1}, zoom:{min:50,max:200,step:10} }[id];
        if (!item || type !== 'range') return;

        S[id] = Math.max(item.min, Math.min(item.max, S[id] + dir * item.step));
        document.getElementById('wta-val-' + id).textContent = S[id] + (id === 'zoom' ? '%' : '');
        saveSettings();
        if (id === 'zoom') applyZoom();
    }

    function activateMenuItem() {
        const row = menuItems[menuIndex];
        if (!row) return;
        const id = row.dataset.id;
        if (id === 'close') { toggleMenu(false); return; }
        if (row.dataset.type === 'toggle') {
            S[id] = !S[id];
            document.getElementById('wta-val-' + id).textContent = S[id] ? 'ON' : 'OFF';
            saveSettings();
        }
    }

    function buildGearButton() {
        if (document.getElementById('wta-settings-btn')) return;
        const btn = document.createElement('div');
        btn.id = 'wta-settings-btn';
        btn.innerHTML = '⚙';
        document.body.appendChild(btn);
    }

    // ─── KEYBOARD ROUTING ───
    window.addEventListener('keydown', (e) => {
        const k = e.key;

        if (k === 'Menu' || k === 'ContextMenu' || k === 'm' || k === 'M') {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            toggleMenu(!menuOpen);
            return;
        }

        if (!menuOpen) {
  