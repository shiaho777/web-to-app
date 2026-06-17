// Find in Page — Ctrl+F style search with highlighting.
// Supports case-sensitive, regex, match navigation.

(function () {

    var highlightColor = getConfig('highlightColor', '#ffeb3b');
    var otherColor = getConfig('otherMatchColor', '#fff59d');

    var bar = null;
    var input = null;
    var countEl = null;
    var marks = [];
    var currentIdx = -1;
    var caseSensitive = false;
    var useRegex = false;

    function buildBar() {
        if (bar) return;
        bar = document.createElement('div');
        bar.id = 'wta-tf-bar';
        bar.innerHTML =
            '<input type="text" placeholder="Find…" />' +
            '<span class="wta-tf-count"></span>' +
            '<button data-action="prev" type="button">\u25B2</button>' +
            '<button data-action="next" type="button">\u25BC</button>' +
            '<button data-action="close" type="button">\u2715</button>' +
            '<div class="wta-tf-opts">' +
            '<button data-action="case" type="button" title="Case sensitive">Aa</button>' +
            '<button data-action="regex" type="button" title="Regex">.*</button>' +
            '</div>';
        input = bar.querySelector('input');
        countEl = bar.querySelector('.wta-tf-count');

        bar.addEventListener('click', function (e) {
            var btn = e.target.closest('button[data-action]');
            if (!btn) return;
            var act = btn.dataset.action;
            if (act === 'prev') navMatch(-1);
            else if (act === 'next') navMatch(1);
            else if (act === 'close') closeBar();
            else if (act === 'case') {
                caseSensitive = !caseSensitive;
                btn.classList.toggle('active', caseSensitive);
                performSearch();
            } else if (act === 'regex') {
                useRegex = !useRegex;
                btn.classList.toggle('active', useRegex);
                performSearch();
            }
        });

        input.addEventListener('input', performSearch);
        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); navMatch(e.shiftKey ? -1 : 1); }
            if (e.key === 'Escape') { e.preventDefault(); closeBar(); }
        });

        document.body.appendChild(bar);
    }

    function openBar() {
        buildBar();
        bar.classList.add('wta-tf-open');
        input.focus();
        input.select();
    }

    function closeBar() {
        if (bar) bar.classList.remove('wta-tf-open');
        clearMarks();
    }

    function clearMarks() {
        marks.forEach(function (m) {
            var parent = m.parentNode;
            if (parent) {
                parent.replaceChild(document.createTextNode(m.textContent), m);
                parent.normalize();
            }
        });
        marks = [];
        currentIdx = -1;
    }

    function performSearch() {
        clearMarks();
        var query = input.value;
        if (!query) { countEl.textContent = ''; return; }

        var flags = caseSensitive ? 'g' : 'gi';
        var pattern;
        if (useRegex) {
            try { pattern = new RegExp(query, flags); }
            catch (e) { countEl.textContent = 'Invalid'; return; }
        } else {
            pattern = new RegExp(query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), flags);
        }

        var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
            acceptNode: function (node) {
                var parent = node.parentNode;
                if (!parent) return NodeFilter.FILTER_REJECT;
                var tag = parent.tagName;
                if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'NOSCRIPT') return NodeFilter.FILTER_REJECT;
                if (parent.classList && parent.classList.contains('wta-tf-mark')) return NodeFilter.FILTER_REJECT;
                if (!node.textContent.trim()) return NodeFilter.FILTER_REJECT;
                return NodeFilter.FILTER_ACCEPT;
            }
        });

        var nodes = [];
        var n;
        while ((n = walker.nextNode())) nodes.push(n);

        for (var i = 0; i < nodes.length; i++) {
            var textNode = nodes[i];
            var text = textNode.textContent;
            var match;
            pattern.lastIndex = 0;
            var lastIndex = 0;
            var fragments = [];

            while ((match = pattern.exec(text)) !== null) {
                if (match[0].length === 0) { pattern.lastIndex++; continue; }
                if (match.index > lastIndex) {
                    fragments.push(document.createTextNode(text.slice(lastIndex, match.index)));
                }
                var mark = document.createElement('span');
                mark.className = 'wta-tf-mark';
                mark.style.background = otherColor;
                mark.textContent = match[0];
                fragments.push(mark);
                marks.push(mark);
                lastIndex = match.index + match[0].length;
            }

            if (fragments.length > 0) {
                if (lastIndex < text.length) {
                    fragments.push(document.createTextNode(text.slice(lastIndex)));
                }
                var parent = textNode.parentNode;
                fragments.forEach(function (f) { parent.insertBefore(f, textNode); });
                parent.removeChild(textNode);
            }
        }

        if (marks.length > 0) {
            currentIdx = 0;
            highlightCurrent();
        } else {
            currentIdx = -1;
        }
        updateCount();
    }

    function highlightCurrent() {
        marks.forEach(function (m, i) {
            if (i === currentIdx) {
                m.style.background = highlightColor;
                m.classList.add('current');
            } else {
                m.style.background = otherColor;
                m.classList.remove('current');
            }
        });
    }

    function navMatch(dir) {
        if (marks.length === 0) return;
        currentIdx = (currentIdx + dir + marks.length) % marks.length;
        highlightCurrent();
        updateCount();
        marks[currentIdx].scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function updateCount() {
        if (marks.length === 0) {
            countEl.textContent = input.value ? '0 / 0' : '';
        } else {
            countEl.textContent = (currentIdx + 1) + ' / ' + marks.length;
        }
    }

    if (typeof __WTA_MODULE_UI__ !== 'undefined' && __WTA_MODULE_UI__.register) {
        __WTA_MODULE_UI__.register({
            id: __MODULE_INFO__.id,
            name: __MODULE_INFO__.name,
            icon: __MODULE_INFO__.icon,
            uiConfig: __MODULE_UI_CONFIG__,
            runMode: __MODULE_RUN_MODE__,
            onClick: function () {
                if (bar && bar.classList.contains('wta-tf-open')) closeBar();
                else openBar();
            }
        });
    }
})();
