package com.yumiyacchi.mambobutton.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yumiyacchi.mambobutton.model.SoundCatalog
import com.yumiyacchi.mambobutton.model.SoundItem
import com.yumiyacchi.mambobutton.ui.theme.SoundCoral

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSoundScreen(
    existingCategories: List<String>,
    onSave: (name: String, category: String, color: Color, audioUri: Uri, imageUri: Uri?, emoji: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(SoundCatalog.presetColors.first()) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var emoji by remember { mutableStateOf("🎵") }

    var isPreviewPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            audioUri = uri
            audioFileName = getFileNameFromUri(context, uri) ?: "Selected Audio"
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "✨ Create Sound Button",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card Section
            Text(
                text = "Live Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(180.dp)) {
                    SoundButtonCard(
                        sound = SoundItem(
                            id = "preview",
                            title = if (name.isBlank()) "My Button" else name,
                            emoji = emoji.ifBlank { "🎵" },
                            category = if (category.isBlank()) "Custom" else category,
                            color = selectedColor,
                            imageFilePath = imageUri?.toString(),
                            isCustom = true
                        ),
                        isPlaying = isPreviewPlaying,
                        onClick = {}
                    )
                }
            }

            // 1. Button Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Button Name *") },
                placeholder = { Text("e.g. Bruh 2.0, Victory Siren") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Sound Upload (Required)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sound File *",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Button(
                            onClick = { audioPicker.launch("audio/*") },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (audioUri == null) "Select Audio" else "Change")
                        }
                    }

                    if (audioUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = audioFileName ?: "Audio file selected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            // Test preview button
                            IconButton(
                                onClick = {
                                    if (isPreviewPlaying) {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        isPreviewPlaying = false
                                    } else {
                                        try {
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(context, audioUri!!)
                                                prepare()
                                                setOnCompletionListener {
                                                    isPreviewPlaying = false
                                                }
                                                start()
                                            }
                                            isPreviewPlaying = true
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            isPreviewPlaying = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPreviewPlaying) "Stop Preview" else "Play Preview",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Button Background Color
            Column {
                Text(
                    text = "Button Background Color",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SoundCatalog.presetColors.take(5).forEach { color ->
                        ColorSwatchCircle(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SoundCatalog.presetColors.drop(5).take(5).forEach { color ->
                        ColorSwatchCircle(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }

            // 4. Image Upload (Optional)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Image Upload (Optional)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(if (imageUri == null) "Upload" else "Change")
                        }
                    }

                    if (imageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Image uploaded",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { imageUri = null }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Image",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji (Icon fallback)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 5. Category (Optional)
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Optional)") },
                    placeholder = { Text("e.g. Memes, Reactions, Custom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (existingCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        existingCategories.filter { it != "All" }.distinct().forEach { cat ->
                            SuggestionChip(
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val validAudioUri = audioUri ?: return@Button
                        val validName = name.trim()
                        if (validName.isBlank()) return@Button
                        onSave(
                            validName,
                            category.trim(),
                            selectedColor,
                            validAudioUri,
                            imageUri,
                            emoji.trim()
                        )
                    },
                    enabled = name.isNotBlank() && audioUri != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Create Button", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ColorSwatchCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val index = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = c.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
