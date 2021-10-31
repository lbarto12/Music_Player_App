package com.example.musicapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import com.example.musicapp.Util.ColorUtil
import kotlinx.android.synthetic.main.music_controls.*

class MusicControl: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_controls)

        initVolumeChanger()
        initDurationBar()
        linkButtons()
    }

    // STATS FOR VIEWS / SEEKBARS

    private var stop = false

    private fun initVolumeChanger(){
        val am: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val volume = am.getStreamVolume(AudioManager.STREAM_MUSIC)


        volumeBar.max = 10
        volumeBar.progress = volume

        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, p1, 0)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }


    private fun initDurationBar(){
        durationBar.max = MainActivity.songService.currentMediaPlayer.duration
        endDuration.text = createTimeString(durationBar.max)


        durationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) MainActivity.songService.currentMediaPlayer.seekTo(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        Thread {
            while (!stop){
                // Progress Bar
                durationBar.progress = MainActivity.songService.currentMediaPlayer.currentPosition

                // Time Stamp
                timeStamp.post{
                    kotlin.run {
                        timeStamp.text = createTimeString(
                            MainActivity.songService.currentMediaPlayer.currentPosition
                        )
                    }
                }

                if (MainActivity.songService.shouldUpdate) {
                    updateDisplay()
                    MainActivity.songService.shouldUpdate = false
                }
                Thread.sleep(1000)
            }
        }.start()




    }

    // !STATS FOR VIEWS / SEEKBARS


    // MAIN -> LINK BUTTONS

    private fun updatePlayState(){
        ppButton.setBackgroundResource(
            if (MainActivity.songService.paused) R.drawable.not_play
            else R.drawable.not_pause
        )
    }


    private fun linkButtons() {
        returnToSongs.setOnClickListener{ finish() }


        // Play button stuff
        ppButton.setOnClickListener {
            MainActivity.songService.togglePlay()
            updatePlayState()
        }

        prevSong.setOnClickListener { MainActivity.songService.prevSong(); updateDisplay(); }
        nextSong.setOnClickListener { MainActivity.songService.nextSong(); updateDisplay(); }

        updateDisplay()
    }
    // !MAIN


    // DISPLAY
    private fun updateDisplay() {

        val song = MainActivity.songService.currentSongObj
        songTitle.post{songTitle.text = song.name}
        artistName.post{ artistName.text = song.artist }
        albumName.post { albumName.text = song.album }
        durationBar.post{ durationBar.max = MainActivity.songService.currentMediaPlayer.duration }
        endDuration.post{ endDuration.text = createTimeString(durationBar.max) }

        ppButton.post{
            ppButton.setBackgroundResource(
                if (MainActivity.songService.paused) R.drawable.not_play
                else R.drawable.not_pause
            )
        }

        val bitmap = MainActivity.songService.currentSongObj.albumArt

        if (bitmap != null){
            albumCover.setImageBitmap(bitmap)

            val average = ColorUtil.avgBitMapCol(bitmap)

            val gradient = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(
                    Color.parseColor("#3b3b3b"),
                    average
                )
            )

            backDrop.background = gradient
        }
        else {
            albumCover.setImageResource(R.drawable.image)
            backDrop.setBackgroundColor(Color.parseColor("#3b3b3b"))
        }



    }
    // !DISPLAY


    // UTIL
    private fun createTimeString(t: Int): String {
        val minutes = t / 1000 / 60
        val seconds = t / 1000 % 60

        val leadingZero: String = if (seconds < 10) "0" else ""

        return "$minutes:$leadingZero$seconds"
    }
    // !UTIL


    override fun onDestroy() {
        super.onDestroy()
        stop = true     // Kill Thread
        MainActivity.self.updatePlayPause()
    }
}