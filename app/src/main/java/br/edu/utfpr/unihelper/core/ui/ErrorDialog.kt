package br.edu.utfpr.unihelper.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.utfpr.unihelper.ui.theme.Alert
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ErrorDialogHandler(
    uiEvent: SharedFlow<UiEvent>,
    onNavigateToLogin: () -> Unit = {}
) {
    var event by remember { mutableStateOf<UiEvent.ErrorDialog?>(null) }

    LaunchedEffect(Unit) {
        uiEvent.collectLatest { e ->
            if (e is UiEvent.ErrorDialog) event = e
        }
    }

    event?.let { error ->
        AlertDialog(
            onDismissRequest = { event = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = error.title,
                    fontWeight = FontWeight.Bold,
                    color = Alert,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = error.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { event = null },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            },
            dismissButton = if (error.isAuthError) {
                {
                    TextButton(
                        onClick = {
                            event = null
                            onNavigateToLogin()
                        }
                    ) {
                        Text("Ir para login")
                    }
                }
            } else null
        )
    }
}
