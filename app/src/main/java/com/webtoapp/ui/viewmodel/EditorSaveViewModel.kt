package com.webtoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class EditorSaveViewModel : ViewModel() {
    private val mutableState = MutableStateFlow<EditorSaveState>(EditorSaveState.Idle)
    val state = mutableState.asStateFlow()
    val isSaving: Boolean get() = state.value is EditorSaveState.Saving

    fun submit(
        showProgress: Boolean = true,
        onFinished: (Boolean) -> Unit = {},
        save: suspend () -> Unit
    ): Boolean {
        if (state.value != EditorSaveState.Idle) return false
        mutableState.value = EditorSaveState.Saving(showProgress)
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            var saved = false
            try {
                withContext(NonCancellable) {
                    save()
                    saved = true
                }
                mutableState.value = EditorSaveState.Saved
            } catch (error: CancellationException) {
                mutableState.value = EditorSaveState.Idle
                throw error
            } catch (error: Exception) {
                AppLogger.e("EditorSave", "Unable to save project", error)
                val message = (error as? EditorSaveException)?.message ?: Strings.saveFailed
                mutableState.value = EditorSaveState.Failed(message)
            } finally {
                onFinished(saved)
            }
        }
        return true
    }

    fun dismissError() {
        if (state.value is EditorSaveState.Failed) mutableState.value = EditorSaveState.Idle
    }
}

internal sealed interface EditorSaveState {
    data object Idle : EditorSaveState
    data class Saving(val showProgress: Boolean) : EditorSaveState
    data object Saved : EditorSaveState
    data class Failed(val message: String) : EditorSaveState
}
