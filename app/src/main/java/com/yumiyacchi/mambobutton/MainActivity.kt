package com.yumiyacchi.mambobutton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.yumiyacchi.mambobutton.audio.SoundManager
import com.yumiyacchi.mambobutton.model.CustomSoundRepository
import com.yumiyacchi.mambobutton.model.SoundCatalog
import com.yumiyacchi.mambobutton.ui.CreateSoundScreen
import com.yumiyacchi.mambobutton.ui.SoundpadScreen
import com.yumiyacchi.mambobutton.ui.theme.MamboButtonTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager
    private lateinit var customSoundRepository: CustomSoundRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        soundManager = SoundManager(applicationContext)
        customSoundRepository = CustomSoundRepository(applicationContext)

        setContent {
            MamboButtonTheme {
                var isCreatingSound by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                val customSounds by customSoundRepository.customSounds.collectAsState()
                val existingCategories = remember(customSounds) {
                    (SoundCatalog.defaultSounds + customSounds).map { it.category }.distinct()
                }

                if (isCreatingSound) {
                    CreateSoundScreen(
                        existingCategories = existingCategories,
                        onSave = { name, category, color, audioUri, imageUri, emoji ->
                            scope.launch {
                                customSoundRepository.addCustomSound(
                                    title = name,
                                    category = category,
                                    color = color,
                                    audioUri = audioUri,
                                    imageUri = imageUri,
                                    emoji = emoji
                                )
                            }
                            isCreatingSound = false
                        },
                        onCancel = {
                            isCreatingSound = false
                        }
                    )
                } else {
                    SoundpadScreen(
                        soundManager = soundManager,
                        customSoundRepository = customSoundRepository,
                        onCreateNewClick = {
                            isCreatingSound = true
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
