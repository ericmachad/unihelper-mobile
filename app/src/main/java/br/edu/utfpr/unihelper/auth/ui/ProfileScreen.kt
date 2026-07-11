package br.edu.utfpr.unihelper.auth.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.utfpr.unihelper.agenda.ui.ConfigMediaMinimaDialog
import br.edu.utfpr.unihelper.core.local.MediaConfig
import br.edu.utfpr.unihelper.core.ui.ErrorDialogHandler
import br.edu.utfpr.unihelper.core.ui.SuccessDialogHandler
import br.edu.utfpr.unihelper.ui.theme.Alert
import br.edu.utfpr.unihelper.ui.theme.Primary
import br.edu.utfpr.unihelper.ui.theme.Surface
import br.edu.utfpr.unihelper.ui.theme.TextGray
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToNotificacoes: () -> Unit,
    onLogout: () -> Unit
) {
    val activity = LocalActivity.current as ComponentActivity
    val authViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = activity)
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val userName = user?.apelido ?: user?.nomeCompleto
    val userCurso = user?.curso

    val mediaConfig: MediaConfig = koinInject()
    val mediaMinima by mediaConfig.mediaMinima.collectAsState(initial = MediaConfig.DEFAULT_MEDIA_MINIMA)
    val scope = rememberCoroutineScope()
    var showMediaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (user == null) {
            authViewModel.carregarPerfil()
        }
    }

    SuccessDialogHandler(uiEvent = authViewModel.uiEvent)
    ErrorDialogHandler(uiEvent = authViewModel.uiEvent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Primary)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Olá, ${userName ?: "Usuário"}",
                        fontSize = 14.sp,
                        color = Surface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userCurso ?: "Sem curso definido",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Surface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp),
                    tint = Surface.copy(alpha = 0.08f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                // Email option
                MenuItem(
                    icon = Icons.Default.Email,
                    title = "Informações pessoais",
                    subtitle = user?.email ?: "carregando...",
                    onClick = onNavigateToEditProfile
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFF3F4F6),
                    thickness = 1.dp
                )

                // Seguranca e Senha
                MenuItem(
                    icon = Icons.Default.Shield,
                    title = "Segurança e Senha",
                    subtitle = null,
                    onClick = onNavigateToChangePassword
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFF3F4F6),
                    thickness = 1.dp
                )

                // Media Minima
                MenuItem(
                    icon = Icons.Filled.Star,
                    title = "Média Mínima",
                    subtitle = "%.1f".format(mediaMinima),
                    onClick = { showMediaDialog = true }
                )

            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout button
        TextButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sair",
                tint = Alert,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sair da Conta",
                color = Alert,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showMediaDialog) {
        ConfigMediaMinimaDialog(
            valorAtual = mediaMinima,
            onSalvar = { valor ->
                scope.launch { mediaConfig.setMediaMinima(valor) }
                showMediaDialog = false
            },
            onDismiss = { showMediaDialog = false }
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Abrir",
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
