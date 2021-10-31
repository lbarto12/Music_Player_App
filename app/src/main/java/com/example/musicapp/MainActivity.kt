package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.Util.Song
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Field
import android.os.IBinder
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.example.musicapp.Util.ColorUtil
import com.example.musicapp.Util.SongService
import kotlinx.android.synthetic.main.music_controls.*

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainActivity.self = this
        val intent = Intent(this, SongService::class.java)
        applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        if (isBound) main()
    }


    // VARS

    companion object {
        public lateinit var self: MainActivity
//        lateinit var songAdapter: SongAdapter

        lateinit var songService: SongService
        var isBound: Boolean = false
        var serviceConnection = SConnect()

        fun songNameBuilder(name: String): String {
            return name.replace("_", " ")
        }
    }

    // !VARS


    // MAIN -> LINK BUTTONS; SERVICE; FIELDS; ETC

    @RequiresApi(Build.VERSION_CODES.P)
    private fun main() {
        //startService(Intent(this, SongService::class.java))

        if (isBound) {
            currentSong.text = songService.currentSongObj.name
            if (!songService.paused) playPause.setBackgroundResource(R.drawable.not_pause)
        }


        mainRecycler.adapter = SongService.songAdapter
        mainRecycler.layoutManager = LinearLayoutManager(this)

        val allNames = songService.getAllSongNames()

        val fields: Array<Field> = R.raw::class.java.fields
        fields.forEach start@{
            if (songNameBuilder(it.name) in allNames) return@start
            val filename = "android.resource://" + this.packageName + "/raw/" + it.name

            val mp = MediaPlayer.create(
                this,
                resources.getIdentifier(it.name, "raw", packageName)
            );

            mp.setOnCompletionListener {
                songService.nextSong()
                songService.shouldUpdate = true
            }


            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(this, filename.toUri())


            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val cover = mmr.embeddedPicture

            var bitmap: Bitmap? = null

            if (cover != null){
                bitmap = cover.let { it1 ->
                    BitmapFactory.decodeByteArray(it1, 0,
                        it1.lastIndex)
                }
            }

            updateGradient(bitmap)


            SongService.songAdapter.addSong(
                Song(
                    songNameBuilder(it.name?: "No Name"),
                    artist ?: "No Artist",
                    album ?: "No Album",
                    mp,
                    bitmap
                )
            )
        }

        if (songService.currentSongObj.albumArt != null){
            hotbarCover.setImageBitmap(songService.currentSongObj.albumArt)
        }

        currentSong.setOnClickListener {
            val intent = Intent(this, MusicControl::class.java)
            startActivity(intent)
        }

        playPause.setOnClickListener {
            songService.togglePlay()
            updatePlayPause()
        }


        timeStampBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){
                    songService.currentMediaPlayer.seekTo(p1)
                    timeStampBar.progress = p1

                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                SongService.songAdapter.filter.filter(p0)
                songService.resetCurrentColor()
                return true
            }

        })
        searchBar.queryHint = "Search Library"
        searchBar.clearFocus()

        Thread {
            while (true){
                timeStampBar.max = songService.currentMediaPlayer.duration
                timeStampBar.progress = songService.currentMediaPlayer.currentPosition
                if (songService.currentSongObj.name == "N/a") {
                    lowerPanel.post{ lowerPanel.isVisible = false }
                    timeStampBar.post{timeStampBar.isVisible = false}
                }
                else if (!lowerPanel.isVisible) {
                    lowerPanel.post{ lowerPanel.isVisible = true }
                    timeStampBar.post{timeStampBar.isVisible = true}
                }
                Thread.sleep(500)
            }
        }.start()

    }


    fun updateGradient(bitmap: Bitmap?){
        if (bitmap != null){
            val avg = ColorUtil.avgBitMapCol(bitmap)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                intArrayOf(
                    Color.parseColor("#242424"),
                    avg
                )
            )
            lowerPanel.background = gradient
        }
        else{
            lowerPanel.setBackgroundColor(Color.parseColor("#242424"))
        }
    }


    override fun onResume() {
        super.onResume()
        searchBar.setQuery("", false);
        searchBar.clearFocus();
        if (isBound) updateGradient(songService.currentSongObj.albumArt)
    }

    // !MAIN


    // DISPLAY

    public fun changeCurrentSongText(name: String){
        currentSong.text = name
    }

    fun updatePlayPause(){
        playPause.setBackgroundResource(
            if (songService.paused) R.drawable.not_play else R.drawable.not_pause)
    }

    // !DISPLAY




    // SERVICE CONNECTION OBJECT

    class SConnect : ServiceConnection {

        @RequiresApi(Build.VERSION_CODES.P)
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val b = p1 as SongService.MainBinder
            Log.println(Log.DEBUG, "CONN", "CONNECTED")
            songService = b.getSongService()
            isBound = true
            MainActivity.self.main()
        }

        override fun onServiceDisconnected(p0: ComponentName?) { isBound = false }

    }

    // !SERVICE CONNECTION OBJECT
}
