package com.webtoapp.core.webview

internal val WEB_SPEECH_SYNTHESIS_SCRIPT = """
    (() => {
        const channel = window.WtaSpeechSynthesis;
        if (window !== window.top || !channel ||
            (typeof window.SpeechSynthesisUtterance === 'function' &&
                window.speechSynthesis && typeof window.speechSynthesis.speak === 'function')) return;

        function handler(target, type, onSubscribe) {
            let listener = null;
            const wrapper = event => listener.call(target, event);
            Object.defineProperty(target, 'on' + type, {
                configurable: true,
                enumerable: true,
                get: () => listener,
                set(value) {
                    const previous = listener;
                    listener = typeof value === 'function' ? value : null;
                    if (previous && !listener) target.removeEventListener(type, wrapper);
                    if (!previous && listener) {
                        EventTarget.prototype.addEventListener.call(target, type, wrapper);
                        if (onSubscribe) onSubscribe();
                    }
                }
            });
        }

        class SpeechSynthesisUtterance extends EventTarget {
            constructor(text = '') {
                super();
                this.text = String(text);
                this.lang = '';
                this.voice = null;
                this.volume = 1;
                this.rate = 1;
                this.pitch = 1;
                for (const type of ['start', 'end', 'error', 'pause', 'resume', 'boundary', 'mark']) {
                    handler(this, type);
                }
            }
        }

        class SpeechSynthesisEvent extends Event {
            constructor(type, init = {}) {
                super(type, init);
                this.utterance = init.utterance;
                this.charIndex = init.charIndex || 0;
                this.charLength = init.charLength || 0;
                this.elapsedTime = init.elapsedTime || 0;
                this.name = init.name || '';
            }
        }

        class SpeechSynthesisErrorEvent extends SpeechSynthesisEvent {
            constructor(type, init = {}) {
                super(type, init);
                this.error = init.error;
            }
        }

        const post = (action, fields) => channel.postMessage(JSON.stringify({action, ...fields}));
        let voices = [];
        let voicesRequested = false;
        let paused = false;
        let nextId = 0;
        const queue = [];

        function requestVoices() {
            if (voicesRequested) return;
            voicesRequested = true;
            post('voices');
        }

        function emit(item, type, fields = {}) {
            const EventType = type === 'error' ? SpeechSynthesisErrorEvent : SpeechSynthesisEvent;
            item.utterance.dispatchEvent(new EventType(type, {
                utterance: item.utterance,
                charIndex: item.position,
                elapsedTime: item.startedAt === null ? 0 : (performance.now() - item.startedAt) / 1000,
                ...fields
            }));
        }

        function startNext() {
            const item = queue[0];
            if (!item || item.id !== null || paused) return;
            if (item.text.length > 32767) {
                queue.shift();
                queueMicrotask(() => {
                    emit(item, 'error', {error: 'text-too-long'});
                    startNext();
                });
                return;
            }
            const utterance = item.utterance;
            item.id = String(++nextId);
            item.offset = item.position;
            post('speak', {
                id: item.id,
                text: item.text.slice(item.offset),
                lang: String(utterance.lang || document.documentElement?.lang || '').slice(0, 128),
                voice: utterance.voice ? String(utterance.voice.voiceURI || '').slice(0, 512) : '',
                rate: Number(utterance.rate),
                pitch: Number(utterance.pitch),
                volume: Number(utterance.volume)
            });
        }

        function stopItem(item) {
            if (!item || item.id === null) return;
            post('stop', {id: item.id});
            item.id = null;
        }

        class SpeechSynthesis extends EventTarget {
            get speaking() { return queue.length > 0 && queue[0].startedAt !== null; }
            get pending() { return queue.length > 1 || (queue.length === 1 && !this.speaking); }
            get paused() { return paused; }

            getVoices() {
                requestVoices();
                return voices.slice();
            }

            addEventListener(type, listener, options) {
                super.addEventListener(type, listener, options);
                if (type === 'voiceschanged' && listener) requestVoices();
            }

            speak(utterance) {
                if (!(utterance instanceof SpeechSynthesisUtterance)) {
                    throw new TypeError('Expected a SpeechSynthesisUtterance');
                }
                queue.push({
                    utterance,
                    text: String(utterance.text),
                    id: null,
                    offset: 0,
                    position: 0,
                    startedAt: null
                });
                startNext();
            }

            cancel() {
                stopItem(queue[0]);
                for (const item of queue.splice(0)) {
                    const error = item.startedAt === null ? 'canceled' : 'interrupted';
                    queueMicrotask(() => emit(item, 'error', {error}));
                }
            }

            pause() {
                if (paused) return;
                paused = true;
                const item = queue[0];
                stopItem(item);
                if (item && item.startedAt !== null) emit(item, 'pause');
            }

            resume() {
                if (!paused) return;
                paused = false;
                startNext();
            }
        }

        const synthesis = new SpeechSynthesis();
        handler(synthesis, 'voiceschanged', requestVoices);
        channel.onmessage = ({data}) => {
            const message = JSON.parse(data);
            if (message.event === 'voices') {
                voices = message.voices.map(voice => Object.freeze(voice));
                if (voices.length > 0) synthesis.dispatchEvent(new Event('voiceschanged'));
                return;
            }
            const item = queue[0];
            if (!item || item.id !== message.id) return;
            switch (message.event) {
                case 'start':
                    if (item.startedAt === null) {
                        item.startedAt = performance.now();
                        emit(item, 'start');
                    } else {
                        emit(item, 'resume');
                    }
                    break;
                case 'progress':
                case 'boundary':
                    item.position = item.offset + message.charIndex;
                    if (message.event === 'boundary') {
                        emit(item, 'boundary', {charLength: message.charLength, name: 'word'});
                    }
                    break;
                case 'end':
                case 'error':
                    queue.shift();
                    if (message.event === 'end') item.position = item.text.length;
                    emit(item, message.event, {error: message.error});
                    startNext();
                    break;
            }
        };

        window.addEventListener('pagehide', () => {
            stopItem(queue[0]);
            queue.length = 0;
            paused = false;
        });
        Object.defineProperties(window, {
            SpeechSynthesis: {configurable: true, writable: true, value: SpeechSynthesis},
            SpeechSynthesisUtterance: {configurable: true, writable: true, value: SpeechSynthesisUtterance},
            SpeechSynthesisEvent: {configurable: true, writable: true, value: SpeechSynthesisEvent},
            SpeechSynthesisErrorEvent: {configurable: true, writable: true, value: SpeechSynthesisErrorEvent},
            speechSynthesis: {configurable: true, get: () => synthesis}
        });
    })();
""".trimIndent()
