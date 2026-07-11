package br.edu.utfpr.unihelper.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import br.edu.utfpr.unihelper.auth.ui.ChangePasswordScreen
import br.edu.utfpr.unihelper.auth.ui.ConfirmEmailScreen
import br.edu.utfpr.unihelper.auth.ui.EditProfileScreen
import br.edu.utfpr.unihelper.auth.ui.ForgotPasswordScreen
import br.edu.utfpr.unihelper.auth.ui.LoginScreen
import br.edu.utfpr.unihelper.auth.ui.RegisterScreen
import br.edu.utfpr.unihelper.auth.ui.ResetPasswordScreen
import br.edu.utfpr.unihelper.auth.ui.SplashScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaDetalheScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaFormScreen
import br.edu.utfpr.unihelper.home.ui.HomeScreen
import br.edu.utfpr.unihelper.notificacao.ui.NotificacaoListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToConfirmEmail = { email ->
                    navController.navigate("confirmar-email/${Uri.encode(email)}") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onNavigateToConfirmEmail = { email ->
                    navController.navigate("confirmar-email/${Uri.encode(email)}") {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { email ->
                    navController.navigate("confirmar-email/${Uri.encode(email)}") {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(Routes.EDITAR_PERFIL) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ALTERAR_SENHA) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DISCIPLINA_CRIAR) {
            DisciplinaFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DISCIPLINA_DETALHE,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            DisciplinaDetalheScreen(
                disciplinaId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DISCIPLINA_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            DisciplinaFormScreen(
                disciplinaId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.NOTIFICACOES) {
            NotificacaoListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "confirmar-email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ConfirmEmailScreen(
                email = email,
                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onConfirmSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(
            "redefinir-senha/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "unihelper://redefinir-senha/{token}" })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(
                token = token,
                onSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
