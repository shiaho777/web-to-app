// Force Dark — intelligent dark mode for any page.
// Uses CSS custom properties for tunable brightness, preserves media.

(function () {

    var brightness = Math.max(0, Math.min(100, parseInt(getConfig('brightness', '10'), 10) || 10));
    var textBrightness = Math.max(50, Math.min(100, parseInt(getConfig('textBrightness', '90'), 10) || 90));
    var preserveImages = String(getConfig('preserveImages', 'true')) === 'true';

    // Convert brightness % to RGB value (0% = 0, 100% = 255).
    var bgVal = Math.round(brightness / 100 * 255);
    // Clamp to at least 18 to avoid pure black (hard on eyes).
    if (bgVal < 18) bgVal = 18;

    function apply() {
        var html = document.documentElement;
        html.classList.add('wta-force-dark');
        if (preserveImages) html.classList.add('preserve-media');
        html.style.setProperty('--wta-dark-bg', bgVal + ', ' + bgVal + ', ' + Math.round(bgVal * 1.1));
        html.style.setProperty('--wta-dark-text', String(textBrightness));
    }

    if (document.documentElement) {
        apply();
    } else {
        document.addEventListener('DOMContentLoaded', apply, { once: true });
    }

    if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
        __WTA_MODULE_UI__.register({
            id: __MODULE_INFO__.id,
            name: __MODULE_INFO__.name,
            icon: __MODULE_INFO__.icon,
            uiConfig: __MODULE_UI_CONFIG__,
            runMode: __MODULE_RUN_MODE__,
            onClick: function () {
                document.documentElement.classList.toggle('wta-force-dark');
            }
        });
    }
})();
