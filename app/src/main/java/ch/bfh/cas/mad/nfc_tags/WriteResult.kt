package ch.bfh.cas.mad.nfc_tags

sealed class WriteResult {
    data object Success: WriteResult()

    data class Error(val reason: String): WriteResult()
}