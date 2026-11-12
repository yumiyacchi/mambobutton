package com.yumiyacchi.mambobutton.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.yumiyacchi.mambobutton.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val soundPool: SoundPool
    private val soundIdMap = mutableMapOf<SoundType, Int>()
    private val customSoundIdMap = mutableMapOf<String, Int>()
    private val activeStreams = mutableSetOf<Int>()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _currentlyPlaying = MutableStateFlow<SoundType?>(null)
    val currentlyPlaying: StateFlow<SoundType?> = _currentlyPlaying.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                if (soundIdMap.size >= SoundType.values().size - 1) { // exclude CUSTOM
                    _isReady.value = true
                }
            }
        }

        scope.launch {
            loadAllSounds()
        }
    }

    private fun loadAllSounds() {
        val soundDir = File(context.cacheDir, "sounds").apply { mkdirs() }

        SoundType.values().forEach { type ->
            if (type != SoundType.CUSTOM) {
                val soundFile = File(soundDir, "${type.name.lowercase()}.wav")
                if (!soundFile.exists() || soundFile.length() == 0L) {
                    generateWavFile(type, soundFile)
                }
                val soundId = soundPool.load(soundFile.absolutePath, 1)
                soundIdMap[type] = soundId
            }
        }
        _isReady.value = true
    }

    fun loadCustomSound(id: String, filePath: String) {
        if (customSoundIdMap.containsKey(id)) return
        val file = File(filePath)
        if (file.exists()) {
            val soundId = soundPool.load(file.absolutePath, 1)
            if (soundId != 0) {
                customSoundIdMap[id] = soundId
            }
        }
    }

    fun unloadCustomSound(id: String) {
        customSoundIdMap.remove(id)?.let { soundId ->
            soundPool.unload(soundId)
        }
    }

    fun play(sound: com.yumiyacchi.mambobutton.model.SoundItem, pitch: Float = 1.0f) {
        val soundId = if (sound.isCustom) {
            val path = sound.soundFilePath ?: return
            if (!customSoundIdMap.containsKey(sound.id)) {
                loadCustomSound(sound.id, path)
            }
            customSoundIdMap[sound.id]
        } else {
            soundIdMap[sound.type]
        } ?: return

        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val streamId = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, clampedPitch)
        if (streamId != 0) {
            activeStreams.add(streamId)
            _currentlyPlaying.value = sound.type
            _currentlyPlayingId.value = sound.id
        }
    }

    fun play(type: SoundType, pitch: Float = 1.0f) {
        val soundId = soundIdMap[type] ?: return
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        val streamId = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, clampedPitch)
        if (streamId != 0) {
            activeStreams.add(streamId)
            _currentlyPlaying.value = type
            _currentlyPlayingId.value = type.name
        }
    }

    fun stopAll() {
        activeStreams.forEach { streamId ->
            soundPool.stop(streamId)
        }
        activeStreams.clear()
        _currentlyPlaying.value = null
        _currentlyPlayingId.value = null
    }

    fun release() {
        stopAll()
        soundPool.release()
    }

    private fun generateWavFile(type: SoundType, file: File) {
        val sampleRate = 44100
        val samples = when (type) {
            SoundType.AIRHORN -> generateAirhorn(sampleRate)
            SoundType.BOING -> generateBoing(sampleRate)
            SoundType.BRUH_WOMP -> generateBruhWomp(sampleRate)
            SoundType.COIN -> generateCoin(sampleRate)
            SoundType.LASER -> generateLaser(sampleRate)
            SoundType.EXPLOSION -> generateExplosion(sampleRate)
            SoundType.WRONG_BUZZER -> generateWrongBuzzer(sampleRate)
            SoundType.VICTORY -> generateVictory(sampleRate)
            SoundType.DRUM_ROLL -> generateDrumRoll(sampleRate)
            SoundType.POLICE_SIREN -> generatePoliceSiren(sampleRate)
            SoundType.CUSTOM -> ShortArray(0)
        }
        writeWav(file, samples, sampleRate)
    }

    // --- Audio Synthesis Algorithms ---

    private fun generateAirhorn(sampleRate: Int): ShortArray {
        val durationSec = 0.55
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val baseFreq = 466.16 // Bb4
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val vibrato = 1.0 + 0.03 * sin(2.0 * PI * 25.0 * t)
            val freq = baseFreq * vibrato
            // Layering harmonics to simulate brass airhorn
            val s1 = sin(2.0 * PI * freq * t)
            val s2 = 0.6 * sin(2.0 * PI * freq * 2.0 * t)
            val s3 = 0.4 * sin(2.0 * PI * freq * 3.0 * t)
            val s4 = 0.25 * sin(2.0 * PI * freq * 4.0 * t)
            val env = (1.0 - t / durationSec).coerceIn(0.0, 1.0)
            val combined = (s1 + s2 + s3 + s4) / 2.25 * env
            buffer[i] = (combined * 32000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBoing(sampleRate: Int): ShortArray {
        val durationSec = 0.45
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 200.0 + 800.0 * (t / durationSec) * (t / durationSec)
            val wobble = 1.0 + 0.15 * sin(2.0 * PI * 35.0 * t)
            phase += 2.0 * PI * (freq * wobble) / sampleRate
            val env = exp(-3.0 * t)
            val sample = sin(phase) * env
            buffer[i] = (sample * 30000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBruhWomp(sampleRate: Int): ShortArray {
        val durationSec = 0.6
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Downward pitch drop
            val freq = 280.0 * exp(-3.5 * t) + 60.0
            phase += 2.0 * PI * freq / sampleRate
            // Rich sawtooth tone
            val saw = (phase % (2.0 * PI)) / PI - 1.0
            val env = (1.0 - t / durationSec).coerceIn(0.0, 1.0)
            buffer[i] = (saw * env * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateCoin(sampleRate: Int): ShortArray {
        val durationSec = 0.35
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = if (t < 0.09) 987.77 else 1318.51 // B5 to E6
            val env = (1.0 - t / durationSec).coerceIn(0.0, 1.0)
            val sample = sin(2.0 * PI * freq * t) * env
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLaser(sampleRate: Int): ShortArray {
        val durationSec = 0.22
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 1800.0 * exp(-14.0 * t) + 120.0
            phase += 2.0 * PI * freq / sampleRate
            val env = (1.0 - t / durationSec).coerceIn(0.0, 1.0)
            val sample = sin(phase) * env
            buffer[i] = (sample * 30000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateExplosion(sampleRate: Int): ShortArray {
        val durationSec = 0.7
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var lowPass = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val noise = Random.nextDouble(-1.0, 1.0)
            lowPass += (noise - lowPass) * 0.12 // Low-pass filter
            val subBass = sin(2.0 * PI * (80.0 * exp(-4.0 * t)) * t) * 0.6
            val env = exp(-4.0 * t)
            val sample = (lowPass + subBass) * env
            buffer[i] = (sample * 31000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateWrongBuzzer(sampleRate: Int): ShortArray {
        val durationSec = 0.4
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Harsh dissonant square waves
            val f1 = sin(2.0 * PI * 130.0 * t)
            val f2 = sin(2.0 * PI * 138.5 * t)
            val square = (if (f1 + f2 > 0) 1.0 else -1.0) * (1.0 - t / durationSec)
            buffer[i] = (square * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateVictory(sampleRate: Int): ShortArray {
        val durationSec = 0.5
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val step = durationSec / notes.size
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val noteIndex = (t / step).toInt().coerceIn(0, notes.size - 1)
            val noteFreq = notes[noteIndex]
            val localT = t - (noteIndex * step)
            val env = (1.0 - localT / step).coerceIn(0.0, 1.0)
            val sample = (sin(2.0 * PI * noteFreq * t) + 0.3 * sin(2.0 * PI * noteFreq * 2.0 * t)) * env
            buffer[i] = (sample * 25000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateDrumRoll(sampleRate: Int): ShortArray {
        val durationSec = 0.5
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val period = 0.04 * (1.0 - 0.5 * (t / durationSec)) // Accelerating hits
            val hitPhase = (t % period) / period
            val hitEnv = exp(-30.0 * hitPhase)
            val noise = Random.nextDouble(-1.0, 1.0) * hitEnv * (0.3 + 0.7 * (t / durationSec))
            buffer[i] = (noise * 30000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generatePoliceSiren(sampleRate: Int): ShortArray {
        val durationSec = 0.7
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 650.0 + 350.0 * sin(2.0 * PI * 3.5 * t)
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * (1.0 - t / (durationSec * 1.2))
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun writeWav(file: File, samples: ShortArray, sampleRate: Int) {
        val byteData = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { byteData.putShort(it) }
        val rawData = byteData.array()

        val totalDataLen = rawData.size + 36
        val byteRate = sampleRate * 1 * 2 // 1 channel, 16-bit

        val header = ByteArray(44)
        val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        bb.put("RIFF".toByteArray())
        bb.putInt(totalDataLen)
        bb.put("WAVE".toByteArray())
        bb.put("fmt ".toByteArray())
        bb.putInt(16) // Subchunk1Size for PCM
        bb.putShort(1) // AudioFormat (1 = PCM)
        bb.putShort(1) // NumChannels (Mono)
        bb.putInt(sampleRate)
        bb.putInt(byteRate)
        bb.putShort(2) // BlockAlign
        bb.putShort(16) // BitsPerSample
        bb.put("data".toByteArray())
        bb.putInt(rawData.size)

        FileOutputStream(file).use { fos ->
            fos.write(header)
            fos.write(rawData)
        }
    }
}
