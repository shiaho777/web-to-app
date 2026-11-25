package com.webtoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.scraper.WebsiteScraper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

internal data class OfflinePackDraft(
    val name: String,
    val url: String,
    val result: WebsiteScraper.ScrapeResult.Success
)

internal sealed interface OfflinePackDraftState {
    data object Idle : OfflinePackDraftState
    data class Scraping(val progress: WebsiteScraper.ScrapeProgress? = null) : OfflinePackDraftState
    data class Ready(val draft: OfflinePackDraft) : OfflinePackDraftState
    data class Failed(val message: String) : OfflinePackDraftState
}

internal class OfflinePackDraftViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableState = MutableStateFlow<OfflinePackDraftState>(OfflinePackDraftState.Idle)
    val state = mutableState.asStateFlow()
    private var saving = false
    private var cleared = false

    fun prepare(name: String, config: WebsiteScraper.ScrapeConfig) {
        if (state.value != OfflinePackDraftState.Idle) return
        mutableState.value = OfflinePackDraftState.Scraping()
        viewModelScope.launch {
            val context = getApplication<Application>()
            val scraper = WebsiteScraper(context, File(context.cacheDir, "offline_pack_previews"))
            try {
                when (val result = scraper.scrape(config) { mutableState.value = OfflinePackDraftState.Scraping(it) }) {
                    is WebsiteScraper.ScrapeResult.Success -> {
                        mutableState.value = OfflinePackDraftState.Ready(OfflinePackDraft(name, config.url, result))
                    }
                    is WebsiteScraper.ScrapeResult.Error -> {
                        mutableState.value = OfflinePackDraftState.Failed(result.message)
                    }
                }
            } catch (error: CancellationException) {
                withContext(NonCancellable + Dispatchers.IO) { scraper.discardCapture() }
                throw error
            } catch (error: Exception) {
                mutableState.value = OfflinePackDraftState.Failed(Strings.scrapeFailed.format(error.message.orEmpty()))
            }
        }
    }

    fun beginSaving(): Boolean {
        if (saving || state.value !is OfflinePackDraftState.Ready) return false
        saving = true
        return true
    }

    fun finishSaving(committed: Boolean) {
        saving = false
        if (committed || cleared) discard()
    }

    fun discard() {
        if (saving) return
        val directory = (state.value as? OfflinePackDraftState.Ready)?.draft?.result?.projectDir
        mutableState.value = OfflinePackDraftState.Idle
        if (directory != null) {
            Dispatchers.IO.dispatch(EmptyCoroutineContext, Runnable { directory.deleteRecursively() })
        }
    }

    override fun onCleared() {
        cleared = true
        discard()
    }
}
