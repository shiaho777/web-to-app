package com.webtoapp.core.webview

internal object LocalDirectoryScript {
    const val SOURCE: String = """
(function(){
  if (window.WtaDirectory) return;
  var CHUNK = 192 * 1024;
  function parse(raw) {
    try { return JSON.parse(raw); } catch (e) {
      return { ok: false, error: 'BAD_RESPONSE', message: String(raw) };
    }
  }
  function bytesToBase64(bytes) {
    var bin = '';
    for (var i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin);
  }
  function base64ToBytes(data) {
    var bin = atob(data || '');
    var out = new Uint8Array(bin.length);
    for (var i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
    return out;
  }
  function fileUrl(raw) {
    var file = parse(raw);
    if (!file.ok) throw Object.assign(new Error(file.message || file.error), file);
    return file.url;
  }
  window.WtaDirectory = {
    info: function() { return parse(NativeBridge.directoryInfo()); },
    pick: function() {
      return new Promise(function(resolve) {
        var id = 'dir-' + Date.now() + '-' + Math.random();
        var previous = window.NativeBridgeDirectoryResult;
        window.NativeBridgeDirectoryResult = function(requestId, payload) {
          if (typeof previous === 'function' && previous !== window.NativeBridgeDirectoryResult) {
            try { previous(requestId, payload); } catch (e) {}
          }
          if (requestId !== id) return;
          resolve(payload);
        };
        var immediate = parse(NativeBridge.pickDirectory(id));
        if (!immediate.ok || !immediate.pending) resolve(immediate);
      });
    },
    release: function() { return parse(NativeBridge.releaseDirectory()); },
    list: function(path) { return parse(NativeBridge.listDirectory(path || '')); },
    openUrl: function(path) { return fileUrl(NativeBridge.openDirectoryFileUrl(path)); },
    closeUrl: function(url) { NativeBridge.releaseStreamUrl(url); },
    read: async function(path) {
      if (typeof NativeBridge.openDirectoryReadUrl === 'function') {
        var url = fileUrl(NativeBridge.openDirectoryReadUrl(path));
        try {
          var response = await fetch(url, { cache: 'no-store' });
          if (!response.ok) throw new Error('Could not read the file (HTTP ' + response.status + ')');
          return new Uint8Array(await response.arrayBuffer());
        } finally {
          NativeBridge.releaseStreamUrl(url);
        }
      }
      var parts = [];
      var offset = 0;
      var size = 0;
      for (;;) {
        var page = parse(NativeBridge.readDirectoryFile(path, offset, CHUNK));
        if (!page.ok) throw Object.assign(new Error(page.message || page.error), page);
        size = page.size || 0;
        if (page.data) parts.push(base64ToBytes(page.data));
        var got = page.length || 0;
        offset += got;
        if (page.eof || got === 0) break;
      }
      var out = new Uint8Array(size);
      var cursor = 0;
      for (var i = 0; i < parts.length; i++) {
        out.set(parts[i], cursor);
        cursor += parts[i].length;
      }
      return out;
    },
    write: async function(path, data) {
      var bytes = data instanceof Uint8Array ? data : new Uint8Array(data || []);
      if (bytes.length === 0) {
        var empty = parse(NativeBridge.writeDirectoryFile(path, '', false));
        if (!empty.ok) throw Object.assign(new Error(empty.message || empty.error), empty);
        return empty;
      }
      var offset = 0;
      while (offset < bytes.length) {
        var end = Math.min(bytes.length, offset + CHUNK);
        var slice = bytes.subarray(offset, end);
        var res = parse(NativeBridge.writeDirectoryFile(path, bytesToBase64(slice), offset !== 0));
        if (!res.ok) throw Object.assign(new Error(res.message || res.error), res);
        offset = end;
      }
      return { ok: true, size: bytes.length };
    },
    remove: function(path) { return parse(NativeBridge.deleteDirectoryEntry(path)); }
  };
})();
"""
}
