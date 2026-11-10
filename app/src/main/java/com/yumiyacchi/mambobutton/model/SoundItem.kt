package com.yumiyacchi.mambobutton.model

import androidx.compose.ui.graphics.Color
import com.yumiyacchi.mambobutton.ui.theme.SoundCoral
import com.yumiyacchi.mambobutton.ui.theme.SoundIndigo
import com.yumiyacchi.mambobutton.ui.theme.SoundLime
import com.yumiyacchi.mambobutton.ui.theme.SoundMint
import com.yumiyacchi.mambobutton.ui.theme.SoundOrange
import com.yumiyacchi.mambobutton.ui.theme.SoundPurple
import com.yumiyacchi.mambobutton.ui.theme.SoundRose
import com.yumiyacchi.mambobutton.ui.theme.SoundSky
import com.yumiyacchi.mambobutton.ui.theme.SoundSun
import com.yumiyacchi.mambobutton.ui.theme.SoundTeal

enum class SoundType {
    AIRHORN,
    BOING,
    BRUH_WOMP,
    COIN,
    LASER,
    EXPLOSION,
    WRONG_BUZZER,
    VICTORY,
    DRUM_ROLL,
    POLICE_SIREN
}

data class SoundItem(
    val id: String,
    val title: String,
    val emoji: String,
    val category: String,
    val type: SoundType,
    val color: Color
)

object SoundCatalog {
    val defaultSounds = listOf(
        SoundItem("airhorn", "Airhorn", "🎺", "Memes", SoundType.AIRHORN, SoundCoral),
        SoundItem("boing", "Boing!", "🦘", "Cartoons", SoundType.BOING, SoundMint),
        SoundItem("bruh", "Sad Womp", "🗿", "Memes", SoundType.BRUH_WOMP, SoundPurple),
        SoundItem("coin", "8-Bit Coin", "🪙", "Gaming", SoundType.COIN, SoundSun),
        SoundItem("laser", "Laser Pew", "🔫", "Gaming", SoundType.LASER, SoundSky),
        SoundItem("buzzer", "Wrong!", "❌", "Reactions", SoundType.WRONG_BUZZER, SoundRose),
        SoundItem("explosion", "Vine Boom", "💥", "Memes", SoundType.EXPLOSION, SoundOrange),
        SoundItem("victory", "Ta-Dah!", "🎉", "Reactions", SoundType.VICTORY, SoundTeal),
        SoundItem("drum", "Drum Roll", "🥁", "Reactions", SoundType.DRUM_ROLL, SoundIndigo),
        SoundItem("siren", "Wee-Woo", "🚨", "Alerts", SoundType.POLICE_SIREN, SoundLime)
    )
}
