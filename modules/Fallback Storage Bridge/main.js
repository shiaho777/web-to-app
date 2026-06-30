(function () {
  'use strict';

  // 1. Safely mock the persistence API to prevent read-only TypeErrors
  try {
    if (navigator.storage) {
      Object.defineProperty(navigator.storage, 'persist', {
        value: async () => true,
        configurable: true
      });
      Object.defineProperty(navigator.storage, 'persisted', {
        value: async () => true,
        configurable: true
      });
      console.log("💾 navigator.storage successfully mocked.");
    }
  } catch (e) {
    console.error("❌ Failed to mock navigator.storage:", e);
  }

  // 2. Intercept and bridge localStorage
  try {
    const mockStorage = {};
    
    Object.defineProperty(window, 'localStorage', {
      value: {
        setItem: function (key, value) {
          mockStorage[key] = String(value);
          // Route natively to the framework storage if available
          if (typeof setConfig === 'function') {
            setConfig(key, String(value));
          }
        },
        getItem: function (key) {
          if (typeof getConfig === 'function') {
            const saved = getConfig(key, null);
            if (saved !== null) return saved;
          }
          return mockStorage.hasOwnProperty(key) ? mockStorage[key] : null;
        },
        removeItem: function (key) {
          delete mockStorage[key];
        },
        clear: function () {
          for (let key in mockStorage) delete mockStorage[key];
        },
        key: function (i) {
          const keys = Object.keys(mockStorage);
          return keys[i] || null;
        },
        get length() {
          return Object.keys(mockStorage).length;
        }
      },
      configurable: true
    });
    
    console.log("💾 localStorage interceptor successfully injected!");
  } catch (e) {
    console.error("❌ localStorage interceptor failed to load:", e);
  }
})();