package com.webtoapp.core.adblock

/**
 * Page script that applies [AdBlocker.getCosmeticPayloadJson].
 * WebViewManager installs it at document start and again after load.
 * The home-card thumbnail runs the same function so a recapture shows
 * the cosmetic colors and hidden elements the real page shows.
 */
object CosmeticFilterScript {
    const val APPLY_JS = """
        (function(p) {
            'use strict';
            if (!p) return;
            if (window.__wta_cosmetic_observer__) return;
            window.__wta_cosmetic_observer__ = true;
            window.__wta_cosmetic_filters__ = true;

            var css = p.css || '';
            var batches = p.batches || [];
            var procRules = p.proc || [];

            if (css && !document.querySelector('style[data-wta="cosmetic"]')) {
                try {
                    var style = document.createElement('style');
                    style.setAttribute('type', 'text/css');
                    style.setAttribute('data-wta', 'cosmetic');
                    style.textContent = css;
                    (document.head || document.documentElement).appendChild(style);
                } catch(e) {}
            }

            var hideMatches = function() {
                for (var b = 0; b < batches.length; b++) {
                    try {
                        var els = document.querySelectorAll(batches[b]);
                        for (var i = 0; i < els.length; i++) {
                            if (els[i].style.display !== 'none') {
                                els[i].style.setProperty('display', 'none', 'important');
                                els[i].style.setProperty('visibility', 'hidden', 'important');
                            }
                        }
                    } catch(e) { /* invalid selector list — skip this batch */ }
                }
            };

            // Procedural rules: base selector via qSA, then the pseudo chain
            // (t = text match, u = upward walk, r = remove) evaluated here in page JS.
            var applyProc = function() {
                for (var r = 0; r < procRules.length; r++) {
                    var rule = procRules[r];
                    try {
                        var els = document.querySelectorAll(rule.b);
                        for (var i = 0; i < els.length; i++) {
                            var el = els[i];
                            var ok = true;
                            for (var o = 0; o < rule.o.length && ok; o++) {
                                var op = rule.o[o];
                                var kind = op.charAt(0);
                                var arg = op.substring(2);
                                if (kind === 't') {
                                    var txt = el.textContent || '';
                                    if (arg.length > 2 && arg.charAt(0) === '/' && arg.charAt(arg.length - 1) === '/') {
                                        ok = new RegExp(arg.substring(1, arg.length - 1), 'i').test(txt);
                                    } else {
                                        ok = txt.toLowerCase().indexOf(arg.toLowerCase()) >= 0;
                                    }
                                } else if (kind === 'u') {
                                    var steps = parseInt(arg, 10);
                                    if (!isNaN(steps)) {
                                        while (steps-- > 0 && el) el = el.parentElement;
                                    } else if (el.closest) {
                                        el = el.closest(arg);
                                    } else {
                                        el = null;
                                    }
                                    ok = !!el;
                                }
                            }
                            if (!ok || !el) continue;
                            if (rule.a === 1) {
                                el.remove();
                            } else if (el.style && el.style.display !== 'none') {
                                el.style.setProperty('display', 'none', 'important');
                                el.style.setProperty('visibility', 'hidden', 'important');
                            }
                        }
                    } catch(e) { /* invalid base selector — skip rule */ }
                }
            };

            if (batches.length > 0 || procRules.length > 0) {
                var pending = false;
                var observer = new MutationObserver(function() {
                    if (pending) return;
                    pending = true;
                    (window.requestIdleCallback || setTimeout)(function() {
                        pending = false;
                        hideMatches();
                        applyProc();
                    }, { timeout: 100 });
                });

                // At document_start <html> may not exist yet; observing the
                // document node still catches every element the parser adds.
                var observerTarget = document.documentElement || document;
                if (observerTarget instanceof Node) {
                    observer.observe(observerTarget, {
                        childList: true, subtree: true
                    });
                }

                setTimeout(function() { observer.disconnect(); }, 30000);
                applyProc();
            }
        })
"""
}
