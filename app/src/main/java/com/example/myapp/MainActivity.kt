package com.example.myapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapp.core.NotificationExtras
import com.example.myapp.ui.navigation.AppNavHost
import com.example.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {

    /** true — приложение открыто по нажатию на пуш-уведомление о паре. */
    private var openProfile by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openProfile = intent.wantsProfile()

        setContent {
            MyAppTheme {
                AppNavHost(
                    openProfile = openProfile,
                    onProfileOpened = { openProfile = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.wantsProfile()) openProfile = true
    }

    private fun Intent.wantsProfile(): Boolean =
        getBooleanExtra(NotificationExtras.EXTRA_OPEN_PROFILE, false)
}
