// Element Blocker — hide page elements via CSS selectors.
// Includes built-in defaults + custom rules + interactive picker.

(function () {

    var LS_KEY = 'wta-eb-rules';

    var DEFAULT_RULES = [
        '[class*="ad-"]',
        '[class*="ads-"]',
        '[class*="advert"]',
        '[id*="ad-"]',
        '[id*="ads-"]',
        '[id*="advert"]',
        '[class*="cookie-banner"]',
        '[class*="cookie-consent"]',
        '[id*="cookie"]',
        '[class*="newsletter"]',
        '[class*="popup"]',
        '[class*="modal-overlay"]',
        '[class*="subscribe"]',
        '[class*="paywall"]',
        'ins.adsbygoogle',
        '[id*="google_ads"]',
        '[data-ad]'
    ];

    var enableDefaults = String(getConfig('enableDefaults', 'true')) === 'true';
    var customRulesRaw = getConfig('customRules', '');
    var enablePicker = String(getConfig('enablePicker', 'true')) === 'true';

    var customRules = [];
    if (customRulesRaw) {
        customRulesRaw.split('\n').forEach(function (line) {
            var s = line.trim();
            if (s && s.indexOf('//') !== 0) customRules.push(s);
        });
    }

    function getSiteRules() {
        try {
            var raw = localStorage.getItem(LS_KEY);
            if (raw) {
                var data = JSON.parse(raw);
                if (data && typeof data === 'object') return data;
            }
        } catch (e) {}
        return {};
    }

    function addSiteRule(selector) {
        var rules = getSiteRules();
        var host = location.hostname;
        if (!rules[host]) rules[host] = [];
        if (rules[host].indexOf(selector) === -1) {
            rules[host].push(selector);
            try { localStorage.setItem(LS_KEY, JSON.stringify(rules)); } catch (e) {}
        }
    }

    function injectBlockStyles() {
        var allRules = [];
        if (enableDefaults) allRules = allRules.concat(DEFAULT_RULES);
        allRules = allRules.concat(customRules);

        var siteRules = getSiteRules();
        var host = location.hostname;
        if (siteRules[host]) allRules = allRules.concat(siteRules[host]);

        // De-duplicate.
        var seen = {};
        allRules = allRules.filter(function (r) {
            if (seen[r]) return false;
            seen[r] = true;
            return true;
        });

        var css = allRules.map(function (sel) {
            return sel + ' { display: none !important; }';
        }).join('\n');

        var style = document.createElement('style');
        style.id = 'wta-eb-blocks';
        style.textContent = css;
        (document.head || document.documentElement).appendChild(style);
    }

    // ── Picker mode ──────────────────────────────────────────────
    var pickerActive = false;

    function startPicker() {
        if (pickerActive) return;
        pickerActive = true;

        var banner = document.createElement('div');
        banner.id = 'wta-eb-banner';
        banner.innerHTML = '<span>Tap any element to block it. Esc to cancel.</span><button type="button">Cancel</button>';
        banner.querySelector('button').addEventListener('click', stopPicker);
        document.body.appendChild(banner);

        document.addEventListener('pointerover', onPickerHover, true);
        document.addEventListener('click', onPickerClick, true);
        document.addEventListener('keydown', onPickerKey, true);
    }

    function stopPicker() {
        pickerActive = false;
        var banner = document.getElementById('wta-eb-banner');
        if (banner) banner.remove();
        document.querySelectorAll('.wta-eb-pick-hover').forEach(function (el) {
            el.classList.remove('wta-eb-pick-hover');
        });
        document.removeEventListener('pointerover', onPickerHover, true);
        document.removeEventListener('click', onPickerClick, true);
        document.removeEventListener('keydown', onPickerKey, true);
    }

    var lastHover = null;
    function onPickerHover(e) {
        if (lastHover) lastHover.classList.remove('wta-eb-pick-hover');
        lastHover = e.target;
        if (lastHover && lastHover !== document.body && lastHover.id !== 'wta-eb-banner') {
            lastHover.classList.add('wta-eb-pick-hover');
        }
    }

    function onPickerClick(e) {
        e.preventDefault();
        e.stopPropagation();
        e.stopImmediatePropagation();

        if (!lastHover || lastHover === document.body) return;

        var selector = generateSelector(lastHover);
        if (selector) {
            addSiteRule(selector);
            lastHover.style.display = 'none';
            injectBlockStyles();
        }

        stopPicker();
    }

    function onPickerKey(e) {
        if (e.key === 'Escape') {
            e.preventDefault();
            stopPicker();
        }
    }

    function generateSelector(el) {
        if (el.id) return '#' + el.id;

        var classes = Array.prototype.slice.call(el.classList);
        if (classes.length) return '.' + classes.join('.');

        var tag = el.tagName.toLowerCase();
        var parent = el.parentElement;
        if (parent && parent !== document.body) {
            var siblings = Array.prototype.filter.call(parent.children, function (c) {
                return c.tagName === el.tagName;
            });
            if (siblings.length === 1) return tag;
            var idx = Array.prototype.indexOf.call(parent.children, el) + 1;
            return tag + ':nth-child(' + idx + ')';
        }
        return tag;
    }

    // ── Init ─────────────────────────────────────────────────────
    injectBlockStyles();

    if (enablePicker && typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
        __WTA_MODULE_UI__.register({
            id: __MODULE_INFO__.id,
            name: __MODULE_INFO__.name,
            icon: __MODULE_INFO__.icon,
            uiConfig: __MODULE_UI_CONFIG__,
            runMode: __MODULE_RUN_MODE__,
            onClick: function () {
                if (pickerActive) stopPicker();
                else startPicker();
            }
        });
    }
})();
