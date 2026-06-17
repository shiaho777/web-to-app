// Web Tint — visual filter overlay for any page.
// Modes: night shift (amber), grayscale, high contrast, invert, custom tint.
// Replaces the former Night Shift module with a multi-mode superset.

(function () {

    var mode = getConfig('mode', 'night');
    var intensity = Math.max(0, Math.min(100, parseInt(getConfig('intensity', '20'), 10) || 20)) / 100;
    var temperature = getConfig('temperature', '2700');
    var tintColor = getConfig('tintColor', '#ff9b40');
    var contrastAmount = Math.max(100, Math.min(300, parseInt(getConfig('contrastAmount', '140'), 10) || 140));

    var TEMP_COLORS = {
        '2000': '#ff8c00',
        '2700': '#ff9b40',
        '3500': '#ffb968',
        '5000': '#ffe4b5'
    };

    var overlayId = 'wta-tint-overlay';
    var filterClasses = ['wta-tint-grayscale', 'wta-tint-invert'];

    function injectOverlay() {
        var existing = document.getElementById(overlayId);
        if (existing) existing.remove();

        filterClasses.forEach(function (cls) {
            document.documentElement.classList.remove(cls);
        });

        if (mode === 'grayscale') {
            document.documentElement.classList.add('wta-tint-grayscale');
            return;
        }

        if (mode === 'invert') {
            document.documentElement.classList.add('wta-tint-invert');
            return;
        }

        if (mode === 'contrast') {
            var c = contrastAmount / 100;
            document.documentElement.style.setProperty('filter', 'contrast(' + c + ')', 'important');
            return;
        }

        var overlay = document.createElement('div');
        overlay.id = overlayId;
        overlay.dataset.mode = mode;

        if (mode === 'night') {
            overlay.style.background = TEMP_COLORS[temperature] || TEMP_COLORS['2700'];
            overlay.style.opacity = String(intensity);
        } else if (mode === 'custom') {
            overlay.style.background = tintColor;
            overlay.style.opacity = String(intensity);
        }

        (document.documentElement || document.body).appendChild(overlay);
    }

    if (document.documentElement) {
        injectOverlay();
    } else {
        document.addEventListener('DOMContentLoaded', injectOverlay, { once: true });
    }

    if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
        __WTA_MODULE_UI__.register({
            id: __MODULE_INFO__.id,
            name: __MODULE_INFO__.name,
            icon: __MODULE_INFO__.icon,
            uiConfig: __MODULE_UI_CONFIG__,
            runMode: __MODULE_RUN_MODE__,
            onClick: function () {
                var o = document.getElementById(overlayId);
                if (o) {
                    o.style.opacity = o.style.opacity === '0' ? String(intensity) : '0';
                }
            }
        });
    }
})();
