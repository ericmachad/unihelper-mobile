package br.edu.utfpr.unihelper.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray

@Composable
fun ConfirmEmailScreen(
    email: String,
    onBackToLogin: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    val viewModel: ConfirmEmailViewModel = org.koin.androidx.compose.koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(email) {
        viewModel.init(email)
    }

    LaunchedEffect(uiState.loggedIn) {
        if (uiState.loggedIn) {
            onConfirmSuccess()
        }
    }

    ErrorDialogHandler(uiEvent = viewModel.uiEvent)

    val focusRequesters = remember { List(6) { FocusRequester() } }
    var otpValues by remember { mutableStateOf(List(6) { "" }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Digite o código de",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Text(
                text = "confirmação",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enviamos um código de 6 dígitos para",
                fontSize = 16.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until 6) {
                    OutlinedTextField(
                        value = otpValues[i],
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                val updated = otpValues.toMutableList()
                                updated[i] = newValue
                                otpValues = updated

                                val codigo = updated.joinToString("")
                                viewModel.onCodigoChanged(codigo)

                                if (newValue.length == 1 && i < 5) {
                                    focusRequesters[i + 1].requestFocus()
                                }

                                if (codigo.length == 6) {
                                    viewModel.submit()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(52.dp, 64.dp)
                            .focusRequester(focusRequesters[i])
                            .onFocusChanged { state ->
                                if (state.isFocused && otpValues[i].isNotEmpty()) {
                                    val updated = otpValues.toMutableList()
                                    updated[i] = ""
                                    otpValues = updated
                                    viewModel.onCodigoChanged(updated.joinToString(""))
                                }
                            },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = otpFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Primary
                        )
                    )
                }
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp
                )
            }

            if (uiState.reenviado) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Código reenviado com sucesso!",
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                enabled = !uiState.isLoading && uiState.codigo.length == 6
            ) {
                Text(
                    text = "Confirmar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.resendConfirmation(email) },
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Reenviar código",
                    fontSize = 14.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Voltar para login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun otpFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFFE5E7EB),
    focusedBorderColor = Primary,
    unfocusedContainerColor = Color(0xFFF9FAFB),
    focusedContainerColor = Color(0xFFF9FAFB),
    cursorColor = Primary
)
