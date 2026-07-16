package com.webtoapp.core.linux

object PerformanceOptimizer {

    data class OptimizeConfig(
        val compressImages: Boolean = true,
        val imageQuality: Int = 80,
        val convertToWebP: Boolean = true,
        val minifyCode: Boolean = true,
        val minifySvg: Boolean = true,
        val removeUnusedResources: Boolean = true,
        val parallelProcessing: Boolean = true,
        val enableCache: Boolean = true,
        val injectPreloadHints: Boolean = true,
        val injectLazyLoading: Boolean = true,
        val optimizeScripts: Boolean = true,
        val injectDnsPrefetch: Boolean = true,
        val injectPerformanceScript: Boolean = true
    )

    fun generatePerformanceScript(): String {
        return """
(function() {
    'use strict';
    if (window.__wta_perf_injected) return;
    window.__wta_perf_injected = true;

    // 1. 图片懒加载 (Intersection Observer)
    if ('IntersectionObserver' in window) {
        var lazyObserver = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                    }
                    if (img.dataset.srcset) {
                        img.srcset = img.dataset.srcset;
                        img.removeAttribute('data-srcset');
                    }
                    img.classList.remove('wta-lazy');
                    lazyObserver.unobserve(img);
                }
            });
        }, { rootMargin: '200px 0px' });

        document.querySelectorAll('img[data-src], img[loading="lazy"]').forEach(function(img) {
            lazyObserver.observe(img);
        });
    }

    // 2. 被动事件监听器（滚动性能优化）
    var origAdd = EventTarget.prototype.addEventListener;
    var passiveEvents = { touchstart: 1, touchmove: 1, wheel: 1, mousewheel: 1, scroll: 1 };
    EventTarget.prototype.addEventListener = function(type, fn, opts) {
        if (passiveEvents[type] && opts !== false) {
            var newOpts = typeof opts === 'object' ? opts : {};
            if (!('passive' in newOpts)) newOpts.passive = true;
            return origAdd.call(this, type, fn, newOpts);
        }
        return origAdd.call(this, type, fn, opts);
    };

    // 3. 预连接常见 CDN
    var cdnDomains = [];
    document.querySelectorAll('script[src], link[href]').forEach(function(el) {
        try {
            var url = new URL(el.src || el.href, location.href);
            if (url.origin !== location.origin && cdnDomains.indexOf(url.origin) === -1) {
                cdnDomains.push(url.origin);
            }
        } catch(e) { /* URL parse failed */ }
    });
    cdnDomains.slice(0, 5).forEach(function(origin) {
        if (!document.querySelector('link[rel="preconnect"][href="' + origin + '"]')) {
            var link = document.createElement('link');
            link.rel = 'preconnect';
            link.href = origin;
            link.crossOrigin = '';
            document.head.appendChild(link);
        }
    });

    // 4. 长任务监控 + 自动让步
    if ('PerformanceObserver' in window) {
        try {
            var longTaskObserver = new PerformanceObserver(function(list) {
                list.getEntries().forEach(function(entry) {
                    if (entry.duration > 100) {
                        console.warn('[WTA Perf] Long task detected:', entry.duration.toFixed(0) + 'ms');
                    }
                });
            });
            longTaskObserver.observe({ entryTypes: ['longtask'] });
        } catch(e) { /* PerformanceObserver not supported */ }
    }

    // 5. 资源加载优先级优化
    document.querySelectorAll('img').forEach(function(img) {
        if (img.getBoundingClientRect().top > window.innerHeight * 1.5) {
            img.loading = 'lazy';
            img.decoding = 'async';
        } else {
            img.decoding = 'async';
        }
    });

    // 6. CSS 动画性能优化（will-change 提示）
    var style = document.createElement('style');
    style.textContent = '.wta-animate{will-change:transform,opacity}.wta-gpu{transform:translateZ(0)}';
    document.head.appendChild(style);

    // 7. 内存优化：页面隐藏时释放非关键资源
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            // 暂停视频
            document.querySelectorAll('video').forEach(function(v) {
                if (!v.paused) { v.dataset.wtaWasPlaying = '1'; v.pause(); }
            });
        } else {
            // 恢复视频
            document.querySelectorAll('video[data-wta-was-playing]').forEach(function(v) {
                v.play().catch(function(){ /* autoplay blocked */ });
                delete v.dataset.wtaWasPlaying;
            });
        }
    });

    // 8. 首次内容绘制加速：关键 CSS 阻塞检测
    if (window.performance && performance.getEntriesByType) {
        var cssEntries = performance.getEntriesByType('resource').filter(function(r) {
            return r.initiatorType === 'link' && r.name.endsWith('.css');
        });
        if (cssEntries.length > 3) {
            console.info('[WTA Perf] ' + cssEntries.length + ' CSS files detected. Consider inlining critical CSS.');
        }
    }
})();
""".trimIndent()
    }
}
