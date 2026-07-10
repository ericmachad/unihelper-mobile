package br.edu.utfpr.unihelper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.edu.utfpr.unihelper.auth.ui.ChangePasswordScreen
import br.edu.utfpr.unihelper.auth.ui.EditProfileScreen
import br.edu.utfpr.unihelper.auth.ui.LoginScreen
import br.edu.utfpr.unihelper.auth.ui.RegisterScreen
import br.edu.utfpr.unihelper.auth.ui.SplashScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaDetalheScreen
import br.edu.utfpr.unihelper.disciplina.ui.DisciplinaFormScreen
import br.edu.utfpr.unihelper.home.ui.HomeScreen
import br.edu.utfpr.unihelper.notificacao.ui.NotificacaoListScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DISCIPLINA_CRIAR = "disciplina/criar"
    const val DISCIPLINA_DETALHE = "disciplina/{id}"
    const val DISCIPLINA_EDITAR = "disciplina/editar/{id}"
    const val EDITAR_PERFIL = "perfil/editar"
    const val ALTERAR_SENHA = "perfil/alterar-senha"
    const val NOTIFICACOES = "notificacoes"
}

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
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
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
    }
}
