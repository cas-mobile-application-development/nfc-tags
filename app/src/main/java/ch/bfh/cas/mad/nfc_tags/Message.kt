package ch.bfh.cas.mad.nfc_tags

data class Message(
    val mimeType: String,
    val payload: String
) {
    companion object {
        fun empty() = Message(mimeType = "", payload = "")
    }
}