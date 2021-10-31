package com.example.musicapp.Util

import android.graphics.Bitmap
import android.media.MediaPlayer

data class Song(
    val name: String,
    val artist: String,
    val album: String,
    val mediaPlayer: MediaPlayer,
    val albumArt: Bitmap? = null
)