// Selection Actions — floating action bar for text selections.
// Search, copy, open as URL, translate, share.
// Replaces the former Floating Search module with configurable actions.

(function () {

    var SEARCH_ENGINES = {
        google: 'https://www.google.com/search?q=',
        duckduckgo: 'https://duckduckgo.com/?q=',
        bing: 'https://www.bing.com/search?q=',
        baidu: 'https://www.baidu.com/s?wd=',
        wikipedia: 'https://en.wikipedia.org/wiki/Special:Search?search='
    };

    var TRANSLATE_ENGINES = {
        google: 'https://translate.google.com/?sl=auto&tl=auto&text=',
        deepl: 'https://www.deepl.com/translator#auto/en/',
        mymemory: 'https://mymemory.translated.net/en/TranslationAPI/ajax?langpair=auto|en&q='
    };

    function getOpts() {
        return {
            engine: getConfig('engine', 'google'),
            translateEngine: getConfig('translateEngine', 'google'),
            showSearch: String(getConfig('showSearch', 'true')) === 'true',
            showCopy: String(getConfig('showCopy', 'true')) === 'true',
            showOpen: String(getConfig('showOpen', 'true')) === 'true',
            showTranslate: String(getConfig('showTranslate', 'true')) === 'true',
            showShare: String(getConfig('showShare', 'false')) === 'true',
            minLength: parseInt(getConfig('minLength', '2'), 10) || 2
        };
    }

    var bar = null;

    function buildButtons() {
        var opts = getOpts();
        var parts = [];

        if (opts.showSearch) {
            parts.push(makeBtn('search', '\uD83D\uDD0D', 'Search'));
        }
        if (opts.showCopy) {
            parts.push(makeDivider());
            parts.push(makeBtn('copy', '\uD83D\uDCCB', 'Copy'));
        }
        if (opts.showOpen) {
            parts.push(makeDivider());
            parts.push(makeBtn('open', '\uD83D\uDD17', 'Open'));
        }
        if (opts.showTranslate) {
            parts.push(makeDivider());
            parts.push(makeBtn('translate', '\uD835\uDC02', 'Translate'));
        }
        if (opts.showShare) {
            parts.push(makeDivider());
            parts.push(makeBtn('share', '\uD83D\uDD17', 'Share'));
        }
        return parts;
    }

    function makeBtn(action, icon, label) {
        return '<button data-action="' + action + '" type="button">' +
            '<span class="wta-sa-icon">' + icon + '</span><span>' + label + '</span></button>';
    }

    function makeDivider() {
        return '<div class="wta-sa-divider"></div>';
    }

    function ensureBar() {
        if (bar) return bar;
        bar = document.createElement('div');
        bar.id = 'wta-sa-bar';
        bar.innerHTML = buildButtons().join('');
        bar.addEventListener('mousedown', function (e) { e.preventDefault(); });
        bar.addEventListener('click', onBarClick);
        document.body.appendChild(bar);
        return bar;
    }

    function show(text, px, py) {
        var b = ensureBar();
        b.dataset.selection = text;
        b.style.left = px + 'px';
        b.style.top = py + 'px';
        b.classList.add('wta-sa-visible');
    }

    function hide() {
        if (bar) bar.classList.remove('wta-sa-visible');
    }

    function onBarClick(e) {
        var btn = e.target.closest('button[data-action]');
        if (!btn || !bar) return;
        var text = bar.dataset.selection || '';
        if (!text) return;

        var opts = getOpts();
        var action = btn.dataset.action;

        if (action === 'search') {
            var base = SEARCH_ENGINES[opts.engine] || SEARCH_ENGINES.google;
            navigate(base + encodeURIComponent(text));
        } else if (action === 'copy') {
            copyToClipboard(text);
        } else if (action === 'open') {
            if (looksLikeUrl(text)) navigate(normaliseUrl(text));
        } else if (action === 'translate') {
            var tBase = TRANSLATE_ENGINES[opts.translateEngine] || TRANSLATE_ENGINES.google;
            navigate(tBase + encodeURIComponent(text));
        } else if (action === 'share') {
            if (navigator.share) {
                navigator.share({ text: text }).catch(function () {});
            } else {
                copyToClipboard(text);
            }
        }
        hide();
    }

    function navigate(url) {
        var w = window.open(url, '_blank');
        if (!w) location.href = url;
    }

    function copyToClipboard(text) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).catch(function () { legacyCopy(text); });
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
        try { document.execCommand('copy'); } catch (_) {}
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

    var debounceTimer = null;
    function onSelectionChange() {
        if (debounceTimer) clearTimeout(debounceTimer);
        debounceTimer = setTimeout(handleSelection, 120);
    }

    function handleSelection() {
        var sel = window.getSelection && window.getSelection();
        if (!sel || sel.isCollapsed) { hide(); return; }
        var text = sel.toString().trim();
        if (text.length < getOpts().minLength) { hide(); return; }
        var rect = sel.getRangeAt(0).getBoundingClientRect();
        if (!rect.width && !rect.height) { hide(); return; }
        show(text, rect.left + rect.width / 2 + window.scrollX, rect.top + window.scrollY);
    }

    document.addEventListener('selectionchange', onSelectionChange);
    document.addEventListener('scroll', hide, { passive: true });
    window.addEventListener('resize', hide);
    document.addEventListener('pointerdown', function (e) {
        if (bar && !bar.contains(e.target)) hide();
    }, { capture: true });

    if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
        __WTA_MODULE_UI__.register({
            id: __MODULE_INFO__.id,
            name: __MODULE_INFO__.name,
            icon: __MODULE_INFO__.icon,
            uiConfig: __MODULE_UI_CONFIG__,
            runMode: __MODULE_RUN_MODE__,
            onClick: function () {}
        });
    }
})();
