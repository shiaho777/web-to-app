package com.webtoapp.core.webview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

/**
 * Bridges the web [Media Session API](https://developer.mozilla.org/en-US/docs/Web/API/Media_Session_API)
 * (`navigator.mediaSession`) to a native Android [MediaSessionCompat].
 *
 * Audio is NOT transferred — it keeps playing inside the WebView. This bridge only
 * carries metadata and control commands between the page and the system media
 * controls (notification, lock screen, Bluetooth, Android Auto).
 */
class MediaSessionBridge(
    private val context: Context,
    private val scope: CoroutineScope,
    private val webViewProvider: () -> WebView? = { null },
    private val smallIconRes: Int = context.applicationInfo.icon
) {

    companion object {
        const val JS_INTERFACE_NAME = "AndroidMediaSession"
        private const val TAG = "MediaSessionBridge"
        private const val CHANNEL_ID = "wta_media_session"
        private const val NOTIFICATION_ID = 3100

        /** The document-start JS polyfill that hooks navigator.mediaSession. */
        fun getInjectionScript(): String = INJECTION_SCRIPT

        private val INJECTION_SCRIPT = """
(function(){
    'use strict';
    if(window._wtaMediaSessionHooked)return;
    window._wtaMediaSessionHooked=true;
    var Bridge=window.AndroidMediaSession;
    if(!Bridge)return;
    var handlers={};
    var lastReported='';
    function reportActions(){
        var a=Object.keys(handlers).join(',');
        if(a!==lastReported){lastReported=a;try{Bridge.reportActionSupported(a);}catch(e){}}
    }
    var hasNative='mediaSession'in navigator;
    var ms;
    if(hasNative){
        ms=navigator.mediaSession;
        ms._wtaHandlers=handlers;
        var orig=ms.setActionHandler?ms.setActionHandler.bind(ms):null;
        ms.setActionHandler=function(action,handler){
            if(handler&&typeof handler==='function'){handlers[action]=handler;}else{delete handlers[action];}
            reportActions();
            if(orig){try{orig(action,handler);}catch(e){}}
        };
    }else{
        ms={metadata:null,playbackState:'none',setActionHandler:function(a,h){if(h){handlers[a]=h;}else{delete handlers[a];}reportActions();},setPositionState:function(){},_wtaHandlers:handlers};
        navigator.mediaSession=ms;
    }
    var lastMeta=null,lastState=null,lastPos=0,lastDur=0;
    function poll(){
        try{
            if(ms.metadata){
                var m={title:ms.metadata.title||'',artist:ms.metadata.artist||'',album:ms.metadata.album||'',artwork:[]};
                try{var art=ms.metadata.artwork;if(art&&art.length){for(var i=0;i<art.length;i++){m.artwork.push({src:art[i].src||'',sizes:art[i].sizes||'',type:art[i].type||''});}}}catch(e2){}
                var s=JSON.stringify(m);
                if(s!==lastMeta){lastMeta=s;try{Bridge.updateMetadata(s);}catch(e3){}}
            }else if(!hasNative){
                var f=JSON.stringify({title:document.title||'',artist:'',album:'',artwork:[]});
                if(f!==lastMeta){lastMeta=f;try{Bridge.updateMetadata(f);}catch(e4){}}
            }
            var els=document.querySelectorAll('audio,video');
            var pos=0,dur=0,rate=1,st='none';
            for(var i=0;i<els.length;i++){
                if(!els[i].paused&&!els[i].ended){pos=els[i].currentTime||0;dur=els[i].duration||0;rate=els[i].playbackRate||1;st='playing';break;}
            }
            if(st==='none'){for(var j=0;j<els.length;j++){if(els[j].currentTime>0){st='paused';pos=els[j].currentTime;dur=els[j].duration||0;break;}}}
            if(st!==lastState||Math.abs(pos-lastPos)>0.5||dur!==lastDur){lastState=st;lastPos=pos;lastDur=dur;try{Bridge.updatePlaybackState(st,pos,dur,rate);}catch(e5){}}
        }catch(e){}
    }
    setInterval(poll,800);
    poll();
    document.addEventListener('play',function(e){if(e.target&&(e.target.tagName==='AUDIO'||e.target.tagName==='VIDEO')){poll();}},true);
    document.addEventListener('pause',function(e){if(e.target&&(e.target.tagName==='AUDIO'||e.target.tagName==='VIDEO')){poll();}},true);
    document.addEventListener('loadedmetadata',function(e){poll();},true);
})();
        """.trimIndent()
    }

    private var mediaSession: MediaSessionCompat? = null
    private val currentMeta = AtomicReference<Meta?>(null)

    @Volatile private var isPlaying = false
    @Volatile private var positionMs: Long = 0L
    @Volatile private var durationMs: Long = 0L
    @Volatile private var rate: Float = 1f
    @Volatile private var supportedActions: Set<String> = emptySet()
    @Volatile private var lastUpdateElapsed: Long = 0L

    // ---- Lifecycle ----

    fun ensureSession() {
        if (mediaSession != null) return
        try {
            ensureNotificationChannel()
            val session = MediaSessionCompat(context, "WebToAppMediaSession")
            session.setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = dispatchAction("play")
                override fun onPause() = dispatchAction("pause")
                override fun onStop() = dispatchAction("stop")
                override fun onSkipToNext() = dispatchAction("nexttrack")
                override fun onSkipToPrevious() = dispatchAction("previoustrack")
                override fun onSeekTo(pos: Long) = dispatchSeek(pos)
                override fun onFastForward() = dispatchAction("seekforward")
                override fun onRewind() = dispatchAction("seekbackward")
            })
            session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            session.isActive = true
            mediaSession = session
            AppLogger.d(TAG, "MediaSession created")
        } catch (e: Exception) {
            AppLogger.e(TAG, "create MediaSession failed: ${e.message}", e)
        }
    }

    fun release() {
        mediaSession?.runCatching { isActive = false; release() }
        mediaSession = null
        cancelNotification()
    }

    // ---- JS → Android ----

    @JavascriptInterface
    fun updateMetadata(json: String) {
        scope.launch(Dispatchers.Main) { applyMetadata(json) }
    }

    @JavascriptInterface
    fun updatePlaybackState(stateStr: String, positionSec: Double, durationSec: Double, rate: Float) {
        isPlaying = stateStr == "playing"
        positionMs = (positionSec * 1000).toLong().coerceAtLeast(0)
        durationMs = if (durationSec > 0) (durationSec * 1000).toLong() else 0
        this.rate = if (rate > 0) rate else 1f
        lastUpdateElapsed = SystemClock.elapsedRealtime()
        scope.launch(Dispatchers.Main) { applyPlaybackState() }
    }

    @JavascriptInterface
    fun reportActionSupported(actions: String) {
        supportedActions = if (actions.isBlank()) emptySet()
                           else actions.split(",").map { it.trim() }.toSet()
    }

    // ---- Android → JS ----

    fun dispatchAction(action: String) {
        val js = "(function(){try{var ms=navigator.mediaSession;if(ms&&ms._wtaHandlers&&ms._wtaHandlers['$action']){ms._wtaHandlers['$action']({});}}catch(e){}})();"
        scope.launch(Dispatchers.Main) { webViewProvider()?.evaluateJavascript(js, null) }
    }

    fun dispatchSeek(posMs: Long) {
        val sec = posMs / 1000.0
        val js = "(function(){try{var ms=navigator.mediaSession;if(ms&&ms._wtaHandlers&&ms._wtaHandlers['seekto']){ms._wtaHandlers['seekto']({seekTime:$sec});}else{var els=document.querySelectorAll('audio,video');for(var i=0;i<els.length;i++){if(!els[i].paused){els[i].currentTime=$sec;break;}}}}catch(e){}})();"
        scope.launch(Dispatchers.Main) { webViewProvider()?.evaluateJavascript(js, null) }
    }

    // ---- Apply state ----

    private fun applyMetadata(json: String) {
        ensureSession()
        val session = mediaSession ?: return
        try {
            val obj = JsonParser.parseString(json).takeIf { it.isJsonObject }?.asJsonObject ?: return
            val title = obj.get("title")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val artist = obj.get("artist")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val album = obj.get("album")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            var artworkUrl: String? = null
            obj.getAsJsonArray("artwork")?.let { arr ->
                var best = -1
                for (i in 0 until arr.size()) {
                    val art = arr[i]?.takeIf { it.isJsonObject }?.asJsonObject ?: continue
                    val src = art.get("src")?.takeIf { !it.isJsonNull }?.asString ?: continue
                    val sz = art.get("sizes")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                        .split("x").firstOrNull()?.toIntOrNull() ?: 0
                    if (sz > best) { best = sz; artworkUrl = src }
                }
            }
            val mb = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
            currentMeta.set(Meta(title, artist, album, artworkUrl))
            if (artworkUrl != null) {
                loadBitmap(artworkUrl) { bmp ->
                    if (bmp != null) mb.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bmp)
                    session.setMetadata(mb.build())
                    updateNotification()
                }
            } else {
                session.setMetadata(mb.build())
                updateNotification()
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "applyMetadata failed: ${e.message}")
        }
    }

    private fun applyPlaybackState() {
        ensureSession()
        val session = mediaSession ?: return
        try {
            var actions = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_STOP)
            if ("previoustrack" in supportedActions) actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            if ("nexttrack" in supportedActions) actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            if ("seekto" in supportedActions) actions = actions or PlaybackStateCompat.ACTION_SEEK_TO
            if ("seekforward" in supportedActions) actions = actions or PlaybackStateCompat.ACTION_FAST_FORWARD
            if ("seekbackward" in supportedActions) actions = actions or PlaybackStateCompat.ACTION_REWIND

            val stateCode = when {
                isPlaying -> PlaybackStateCompat.STATE_PLAYING
                positionMs > 0 -> PlaybackStateCompat.STATE_PAUSED
                else -> PlaybackStateCompat.STATE_NONE
            }
            val pos = if (isPlaying) {
                positionMs + ((SystemClock.elapsedRealtime() - lastUpdateElapsed) * rate).toLong()
            } else positionMs

            val pb = PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(stateCode, pos, if (isPlaying) rate else 0f)
            session.setPlaybackState(pb.build())
            updateNotification()
        } catch (e: Exception) {
            AppLogger.w(TAG, "applyPlaybackState failed: ${e.message}")
        }
    }

    // ---- Notification ----

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Media Session", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Media playback controls"; setShowBadge(false)
                }
            )
        }
    }

    private fun mediaButtonIntent(keyCode: Int): PendingIntent {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            setPackage(context.packageName)
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, keyCode, intent, flags)
    }

    private fun updateNotification() {
        val session = mediaSession ?: return
        val meta = currentMeta.get() ?: return
        try {
            val token = session.sessionToken
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(smallIconRes)
                .setContentTitle(meta.title)
                .setContentText(listOfNotNull(
                    meta.artist.takeIf { it.isNotBlank() },
                    meta.album.takeIf { it.isNotBlank() }
                ).joinToString(" - "))
                .setStyle(MediaStyle().setMediaSession(token))
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if ("previoustrack" in supportedActions) {
                builder.addAction(android.R.drawable.ic_media_previous, "Prev",
                    mediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            }
            builder.addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                if (isPlaying) mediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PAUSE)
                else mediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PLAY)
            )
            if ("nexttrack" in supportedActions) {
                builder.addAction(android.R.drawable.ic_media_next, "Next",
                    mediaButtonIntent(KeyEvent.KEYCODE_MEDIA_NEXT))
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            AppLogger.w(TAG, "updateNotification failed: ${e.message}")
        }
    }

    private fun cancelNotification() {
        try {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        } catch (e: Exception) {}
    }

    private fun loadBitmap(url: String, cb: (android.graphics.Bitmap?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val bmp = try {
                URL(url).openConnection().apply { connectTimeout = 5000; readTimeout = 5000 }
                    .getInputStream().use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) { null }
            cb(bmp)
        }
    }

    private data class Meta(val title: String, val artist: String, val album: String, val artworkUrl: String?)
}
