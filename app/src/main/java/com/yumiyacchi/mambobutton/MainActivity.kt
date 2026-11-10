package com.yumiyacchi.mambobutton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yumiyacchi.mambobutton.audio.SoundManager
import com.yumiyacchi.mambobutton.ui.SoundpadScreen
import com.yumiyacchi.mambobutton.ui.theme.MamboButtonTheme

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        soundManager = SoundManager(applicationContext)

        setContent {
            MamboButtonTheme {
                SoundpadScreen(soundManager = soundManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
