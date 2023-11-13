package ch.bfh.cas.mad.nfc_tags

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.bfh.cas.mad.nfc_tags.ui.theme.NfctagsTheme


class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>

    private val viewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val nfcIntent = Intent(this, javaClass)
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, FLAG_MUTABLE)
        intentFiltersArray = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))

        setContent {
            NfctagsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val messagePayload = viewModel.messagePayload.collectAsStateWithLifecycle(
                        initialValue = ""
                    )
                    val writeResult = viewModel.writeResult.collectAsStateWithLifecycle()
                    val hasTag = viewModel.hasTag.collectAsStateWithLifecycle(initialValue = false)
                    Column {
                        if (hasTag.value) {
                            Heading("Tag detected")
                            TextField(
                                value = messagePayload.value,
                                onValueChange = viewModel::messageChanged,
                                label = { Text("Message") }
                            )
                            Button(onClick = {
                                viewModel.writeCurrentText()
                            }) {
                                Text("Write")
                            }
                            WriteResult(writeResult.value)
                        } else {
                            Heading("No tag detected yet")
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun WriteResult(writeResult: WriteResult?) {
        if (writeResult == null) {
            return
        }
        when (writeResult) {
            is WriteResult.Success -> Text("Success")
            is WriteResult.Error -> Text("Error: ${writeResult.reason}")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.setCurrentIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(
            this,
            nfcPendingIntent,
            intentFiltersArray,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }
}