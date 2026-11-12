package com.yumiyacchi.mambobutton.model

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CustomSoundRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val jsonFile = File(context.filesDir, "custom_sounds.json")
    private val audioDir = File(context.filesDir, "custom_audio").apply { mkdirs() }
    private val imageDir = File(context.filesDir, "custom_images").apply { mkdirs() }

    private val _customSounds = MutableStateFlow<List<SoundItem>>(emptyList())
    val customSounds: StateFlow<List<SoundItem>> = _customSounds.asStateFlow()

    init {
        scope.launch {
            loadSounds()
        }
    }

    private fun loadSounds() {
        if (!jsonFile.exists()) {
            _customSounds.value = emptyList()
            return
        }

        try {
            val jsonStr = jsonFile.readText()
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<SoundItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val emoji = obj.optString("emoji", "🎵")
                val category = obj.optString("category", "Custom")
                val colorArgb = obj.getInt("colorArgb")
                val soundFilePath = obj.optString("soundFilePath").takeIf { it.isNotBlank() }
                val imageFilePath = obj.optString("imageFilePath").takeIf { it.isNotBlank() }

                list.add(
                    SoundItem(
                        id = id,
                        title = title,
                        emoji = emoji,
                        category = category,
                        type = SoundType.CUSTOM,
                        color = Color(colorArgb),
                        soundFilePath = soundFilePath,
                        imageFilePath = imageFilePath,
                        isCustom = true
                    )
                )
            }
            _customSounds.value = list
        } catch (e: Exception) {
            e.printStackTrace()
            _customSounds.value = emptyList()
        }
    }

    private fun saveSounds(list: List<SoundItem>) {
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("emoji", item.emoji)
                    put("category", item.category)
                    put("colorArgb", item.color.toArgb())
                    put("soundFilePath", item.soundFilePath ?: "")
                    put("imageFilePath", item.imageFilePath ?: "")
                }
                jsonArray.put(obj)
            }
            jsonFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addCustomSound(
        title: String,
        category: String,
        color: Color,
        audioUri: Uri,
        imageUri: Uri?,
        emoji: String = "🎵"
    ): SoundItem? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()

        // Copy audio file to internal app storage
        val audioFile = File(audioDir, "sound_$id.bin")
        val audioSuccess = copyUriToFile(audioUri, audioFile)
        if (!audioSuccess) return@withContext null

        // Copy image file if provided
        var imageFilePath: String? = null
        if (imageUri != null) {
            val imageFile = File(imageDir, "img_$id.bin")
            if (copyUriToFile(imageUri, imageFile)) {
                imageFilePath = imageFile.absolutePath
            }
        }

        val newItem = SoundItem(
            id = id,
            title = title,
            emoji = if (emoji.isBlank()) "🎵" else emoji,
            category = if (category.isBlank()) "Custom" else category.trim(),
            type = SoundType.CUSTOM,
            color = color,
            soundFilePath = audioFile.absolutePath,
            imageFilePath = imageFilePath,
            isCustom = true
        )

        val updatedList = _customSounds.value + newItem
        _customSounds.value = updatedList
        saveSounds(updatedList)

        newItem
    }

    fun deleteCustomSound(soundId: String) {
        scope.launch {
            val current = _customSounds.value
            val target = current.find { it.id == soundId } ?: return@launch

            target.soundFilePath?.let { File(it).delete() }
            target.imageFilePath?.let { File(it).delete() }

            val updatedList = current.filterNot { it.id == soundId }
            _customSounds.value = updatedList
            saveSounds(updatedList)
        }
    }

    private fun copyUriToFile(uri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
