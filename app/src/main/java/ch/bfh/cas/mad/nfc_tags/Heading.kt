package ch.bfh.cas.mad.nfc_tags

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun Heading(text: String) {
    Text(text = text, fontSize = 30.sp)
}