package com.killedbythegalaxy.radiokotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.killedbythegalaxy.radiokotlin.ui.screens.ChatScreen
import com.killedbythegalaxy.radiokotlin.ui.screens.RadioScreen
import com.killedbythegalaxy.radiokotlin.ui.theme.RadioKotlinTheme
import com.killedbythegalaxy.radiokotlin.ui.theme.VoidBlack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            RadioKotlinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VoidBlack
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "radio"
                    ) {
                        composable("radio") {
                            RadioScreen(
                                onChatClick = { navController.navigate("chat") }
                            )
                        }
                        composable("chat") {
                            ChatScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
