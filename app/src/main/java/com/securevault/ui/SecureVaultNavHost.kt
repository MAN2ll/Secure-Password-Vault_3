package com.securevault.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.securevault.ui.screens.LockScreen
import com.securevault.ui.screens.MainScreen

@Composable
fun SecureVaultNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = "lock") {
        
        // 🔐 Экран блокировки (вход в приложение)
        composable("lock") {
            LockScreen(
                onUnlock = { 
                    // После успешного ввода мастер-пароля → главный экран
                    navController.navigate("main") { 
                        popUpTo("lock") { inclusive = true } 
                    } 
                },
                onBiometricClick = { 
                    // TODO: здесь будет логика биометрии
                }
            )
        }
        
        // 🏠 Главный экран (список паролей)
        composable("main") {
            MainScreen(
                onLock = { 
                    // Кнопка "Заблокировать" → возврат на экран входа
                    navController.navigate("lock") { 
                        popUpTo("main") { inclusive = true } 
                    } 
                },
                onAddEntry = { /* TODO: экран добавления */ },
                onEditEntry = { /* TODO: экран редактирования */ }
            )
        }
    }
}
