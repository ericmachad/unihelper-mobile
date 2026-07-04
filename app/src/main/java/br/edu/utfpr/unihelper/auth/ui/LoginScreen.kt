package br.edu.utfpr.unihelper.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.ui.theme.Accent
import br.edu.utfpr.unihelper.ui.theme.Border
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = org.koin.androidx.compose.koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val fieldErrors = remember { mutableStateMapOf<String, String?>() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    fun validate(): Boolean {
        fieldErrors.clear()
        var valid = true

        if (email.isBlank()) {
            fieldErrors["email"] = "Campo obrigatório"
            valid = false
        } else if (!EMAIL_REGEX.matches(email.trim())) {
            fieldErrors["email"] = "Email inválido"
            valid = false
        }

        if (senha.isBlank()) {
            fieldErrors["senha"] = "Campo obrigatório"
            valid = false
        }

        return valid
    }

    fun submit() {
        focusManager.clearFocus()
        if (validate()) {
            viewModel.login(email.trim(), senha)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        // Formas decorativas desfocadas
        Box(
            modifier = Modifier
                .size(200.dp)
                .blur(60.dp)
                .background(Accent.copy(alpha = 0.08f), CircleShape)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp)
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .blur(50.dp)
                .background(Accent.copy(alpha = 0.06f), CircleShape)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 50.dp)
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .blur(60.dp)
                .background(Accent.copy(alpha = 0.05f), CircleShape)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 60.dp)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .blur(50.dp)
                .background(Primary.copy(alpha = 0.04f), CircleShape)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-30).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Surface, RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, Border), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "uni",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome do app
            Text(
                text = "UniHelper",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(48.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; fieldErrors.remove("email") },
                label = { Text("E-mail") },
                placeholder = { Text("seu@email.com") },
                isError = fieldErrors["email"] != null,
                supportingText = fieldErrors["email"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = { /* TODO: recuperação de senha */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Esqueceu sua senha?",
                    fontSize = 13.sp,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Campo Palavra-passe
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it; fieldErrors.remove("senha") },
                label = { Text("Senha") },
                placeholder = { Text("••••••••") },
                isError = fieldErrors["senha"] != null,
                supportingText = fieldErrors["senha"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submit() }
                ),
                singleLine = true
            )

            // Erro da API
            uiState.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Entrar
            Button(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Criar Conta
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary,
                ),
                border = BorderStroke(1.dp, Primary)
            ) {
                Text(
                    text = "Criar Conta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFFE5E7EB),
    focusedBorderColor = Primary,
    unfocusedContainerColor = Color(0xFFF9FAFB),
    focusedContainerColor = Color(0xFFF9FAFB),
)
