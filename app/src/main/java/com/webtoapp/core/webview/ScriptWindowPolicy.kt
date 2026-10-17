package com.webtoapp.core.webview

import com.webtoapp.data.model.NewWindowBehavior

/**
 * Decides which `window.open` / `target=_blank` requests must stay a real
 * child window. Google's popup sign-in posts the credential to
 * `window.opener` and then calls `window.close()`. Folding that popup into
 * the opener page leaves the "this window will close automatically" screen
 * up forever and the site never receives the result (#1316).
 */
internal object ScriptWindowPolicy {

    fun keepRealPopup(hitTestUrl: String?, behavior: NewWindowBehavior): Boolean {
        if (behavior == NewWindowBehavior.BLOCK) return false
        if (behavior == NewWindowBehavior.POPUP_WINDOW) return true
        // A link click carries the anchor URL. A scripted window.open does not.
        if (hitTestUrl.isNullOrBlank()) return true
        return OAuthProviderHosts.isOAuthHost(hitTestUrl)
    }
}
