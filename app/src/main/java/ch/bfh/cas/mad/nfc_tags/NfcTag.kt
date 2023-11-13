package ch.bfh.cas.mad.nfc_tags

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.TagTechnology

fun Tag.writeMessage(message: Message): WriteResult {
    val ndefMessage = message.toNDefMessage()
    try {
        val nDefTag = Ndef.get(this)
        if (nDefTag != null) {
            return nDefTag.write(ndefMessage)
        }

        val nDefFormatableTag = NdefFormatable.get(this)
        if (nDefFormatableTag != null) {
            return nDefFormatableTag.formatWrite(ndefMessage)
        }

        return WriteResult.Error("NDEF is not supported")
    } catch (e: Exception) {
        return WriteResult.Error("Exception: $e")
    }
}

private fun Ndef.write(message: NdefMessage): WriteResult =
    withConnection {
        if (maxSize < message.toByteArray().size) {
            return@withConnection WriteResult.Error("Message to large to write to NFC tag")
        }
        if (!isWritable) {
            return@withConnection WriteResult.Error("NFC tag is read-only")
        }
        writeNdefMessage(message)
        return@withConnection WriteResult.Success
    }

private fun NdefFormatable.formatWrite(message: NdefMessage): WriteResult =
    withConnection {
        format(message)
        WriteResult.Success
    }

private fun Message.toNDefMessage(): NdefMessage {
    val nfcRecord = NdefRecord(
        NdefRecord.TNF_MIME_MEDIA,
        mimeType.toByteArray(),
        null,
        payload.toByteArray()
    )
    return NdefMessage(arrayOf(nfcRecord))
}

private fun <NdefType : TagTechnology, Result> NdefType.withConnection(fn: (tag: NdefType) -> Result): Result {
    this.connect()
    this.use {
        return fn(this)
    }
}