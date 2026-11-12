package com.yumiyacchi.mambobutton.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yumiyacchi.mambobutton.audio.SoundManager
import com.yumiyacchi.mambobutton.model.CustomSoundRepository
import com.yumiyacchi.mambobutton.model.SoundCatalog
import com.yumiyacchi.mambobutton.model.SoundItem
import com.yumiyacchi.mambobutton.model.SoundType
import com.yumiyacchi.mambobutton.ui.theme.MamboButtonTheme
import java.io.File
import kotlin.math.roundToInt

@Composable
fun SoundpadScreen(
    soundManager: SoundManager,
    customSoundRepository: CustomSoundRepository,
    onCreateNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentlyPlayingId by soundManager.currentlyPlayingId.collectAsState()
    val customSounds by customSoundRepository.customSounds.collectAsState()

    val allSounds = remember(customSounds) {
        SoundCatalog.defaultSounds + customSounds
    }

    SoundpadContent(
        sounds = allSounds,
        currentlyPlayingId = currentlyPlayingId,
        onPlaySound = { sound, pitch ->
            soundManager.play(sound, pitch)
        },
        onStopAll = {
            soundManager.stopAll()
        },
        onDeleteSound = { sound ->
            customSoundRepository.deleteCustomSound(sound.id)
            soundManager.unloadCustomSound(sound.id)
        },
        onCreateNewClick = onCreateNewClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundpadContent(
    sounds: List<SoundItem>,
    currentlyPlayingId: String?,
    onPlaySound: (SoundItem, Float) -> Unit,
    onStopAll: () -> Unit,
    onDeleteSound: (SoundItem) -> Unit = {},
    onCreateNewClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var pitchModifier by remember { mutableFloatStateOf(1.0f) }
    var soundToDelete by remember { mutableStateOf<SoundItem?>(null) }
    val view = LocalView.current

    val categories = remember(sounds) {
        listOf("All") + sounds.map { it.category }.distinct()
    }

    val filteredSounds = remember(selectedCategory, sounds) {
        if (selectedCategory == "All") {
            sounds
        } else {
            sounds.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎵 MamboButton",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onStopAll()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop All",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onCreateNewClick()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Button", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            selectedCategory = category
                        },
                        label = { Text(category) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Pitch & Speed modifier panel
            PitchControlCard(
                pitch = pitchModifier,
                onPitchChange = { pitchModifier = it },
                onReset = { pitchModifier = 1.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Sound Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredSounds, key = { it.id }) { sound ->
                    SoundButtonCard(
                        sound = sound,
                        isPlaying = currentlyPlayingId == sound.id || currentlyPlayingId == sound.type.name,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPlaySound(sound, pitchModifier)
                        },
                        onLongClick = {
                            if (sound.isCustom) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                soundToDelete = sound
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    soundToDelete?.let { sound ->
        AlertDialog(
            onDismissRequest = { soundToDelete = null },
            title = { Text("Delete Sound Button?") },
            text = { Text("Are you sure you want to delete \"${sound.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSound(sound)
                        soundToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { soundToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PitchControlCard(
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pitch / Speed: ${((pitch * 100).roundToInt()) / 100.0}x",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (pitch != 1.0f) {
                    IconButton(onClick = onReset, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset pitch",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Slider(
                value = pitch,
                onValueChange = onPitchChange,
                valueRange = 0.5f..2.0f,
                steps = 15,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SoundButtonCard(
    sound: SoundItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    val imageFile = remember(sound.imageFilePath) {
        sound.imageFilePath?.let { File(it) }?.takeIf { it.exists() }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.08f)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(22.dp)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            sound.color.copy(alpha = 0.88f),
                            sound.color
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (imageFile != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = sound.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else if (sound.imageFilePath != null && sound.imageFilePath.startsWith("content://")) {
                    AsyncImage(
                        model = sound.imageFilePath,
                        contentDescription = sound.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = sound.emoji,
                        fontSize = 44.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = sound.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = sound.category,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (sound.isCustom) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SoundpadPreview() {
    MamboButtonTheme {
        SoundpadContent(
            sounds = SoundCatalog.defaultSounds,
            currentlyPlayingId = "airhorn",
            onPlaySound = { _, _ -> },
            onStopAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SoundButtonPreview() {
    MamboButtonTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SoundButtonCard(
                sound = SoundCatalog.defaultSounds.first(),
                isPlaying = false,
                onClick = {}
            )
        }
    }
}
