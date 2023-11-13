package ch.bfh.cas.mad.nfc_tags

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class MainViewModel : ViewModel(), DefaultLifecycleObserver {
    private var currentIntent: Intent? = null
    private val _currentMessage = MutableStateFlow<Message?>(null)
    val messagePayload: Flow<String> = _currentMessage.mapNotNull { it?.payload }

    private var _currentTag = MutableStateFlow<Tag?>(null)
    val hasTag: Flow<Boolean> = _currentTag.map { it != null }

    private var _writeResult = MutableStateFlow<WriteResult?>(null)
    val writeResult: StateFlow<WriteResult?> = _writeResult

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        @Suppress("DEPRECATION")
        this._currentTag.value = currentIntent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        this._writeResult.value = null
        this._currentMessage.value = currentIntent?.tagMessage() ?: Message.empty()
    }

    fun messageChanged(newMessage: String) {
        this._currentMessage.value =
            Message(payload = newMessage, mimeType = "application/vnd.ch.bfh.cas.mad.nfc")
    }

    fun writeCurrentText() {
        _writeResult.value = null
        this._currentMessage.value?.let {
            _writeResult.value = _currentTag.value?.writeMessage(it)
        }
    }

    fun setCurrentIntent(intent: Intent?) {
        intent?.let { this.currentIntent = intent }
    }

    private fun Intent.tagMessage(): Message? {
        @Suppress("DEPRECATION")
        return getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.let { rawMsgs ->
            val message = rawMsgs[0] as NdefMessage
            Message(
                mimeType = String(message.records[0].type),
                payload = String(message.records[0].payload)
            )
        }
    }
}