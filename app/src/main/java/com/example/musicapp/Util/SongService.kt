package com.example.musicapp.Util

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import kotlinx.android.synthetic.main.activity_main.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity;
import kotlinx.android.synthetic.main.song_cell.view.*

class SongService : Service() {

    override fun onCreate() {
        super.onCreate()
        binder = MainBinder()
        SongService.songAdapter = SongAdapter(mutableListOf())
        SongService.self = this
    }


    // BINDER
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBind(p0: Intent?): IBinder {
        createNotificationChannel()
        updateNotification()
        return binder
    }
    inner class MainBinder : Binder() {fun getSongService(): SongService { return this@SongService }}
    // !BINDER


    fun getAllSongNames(): ArrayList<String>{
        val temp = ArrayList<String>()
        for (i in songAdapter.songs) temp.add(i.name)
        return temp;
    }






    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        updateNotification()
        Log.println(Log.DEBUG, "SERV", "START COMMAND CALLED")
        return START_STICKY
    }


    // NOTIFICATION

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val nc = NotificationChannel(
            "SongChannel1",
            "Foreground Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val m = getSystemService(NotificationManager::class.java)
        m.createNotificationChannel(nc)
    }

    private fun updateNotification(){

        val inten = Intent(this, MainActivity::class.java)

        val pending = PendingIntent.getActivity(this, 0, inten, 0)

        // BUTTON INTENTS
        val prevIntent = Intent(this, ActionReceiver::class.java).apply { action = ACTION_PREV }
        val nextIntent = Intent(this, ActionReceiver::class.java).apply { action = ACTION_NEXT }
        val pauseIntent = Intent(this, ActionReceiver::class.java).apply { action = ACTION_PAUSE }

        val pendingPrevIntent = PendingIntent.getBroadcast(this,
            0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingNextIntent = PendingIntent.getBroadcast(this,
            0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingPauseIntent = PendingIntent.getBroadcast(this,
            0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Sexy notification building ;)
        notification =  androidx.core.app.NotificationCompat.Builder(this, "SongChannel1")
            .setContentTitle(currentSongObj.name)
            .setContentText(currentSongObj.artist)
            .setSmallIcon(R.drawable.music_note)
            .setLargeIcon(currentSongObj.albumArt)
            .setSound(null)             // GET RID OF THAT DAMN SOUND EFFECT
            .setContentIntent(pending)
            .setShowWhen(false)         // DO NOT SHOW TIME RECEIVED
            .setWhen(0)                 // FORCES TO THE TOP
            .setColor(Color.DKGRAY)
            .setColorized(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .addAction(R.drawable.not_prev, "", pendingPrevIntent)
            .addAction(if (paused) R.drawable.not_play else R.drawable.not_pause,"", pendingPauseIntent)
            .addAction(R.drawable.not_next, "", pendingNextIntent)
            .build()

        startForeground(1, notification)
    }

    // !NOTIFICATION



    // VARS
    private lateinit var binder: MainBinder
    private var notification: Notification? = null

    var shouldUpdate: Boolean = false       // Used for updating UI from threads
    var currentMediaPlayer: MediaPlayer = MediaPlayer()
    var currentSongObj: Song = Song("N/a", "N/a", "N/a", MediaPlayer())
    var currentSongIndex: Int = 0
    var paused: Boolean = true;

    companion object {
        lateinit var songAdapter: SongAdapter
        lateinit var self: SongService


        // FOR BROADCAST RECEIVER USE
        const val ACTION_PREV = "prev"
        const val ACTION_NEXT = "next"
        const val ACTION_PAUSE = "pause"
    }
    // !VARS



    // MUSIC CONTROLS
    fun changeSongTo(position: Int){
        val current = songAdapter.songs[position]
        currentMediaPlayer.pause()
        currentMediaPlayer.seekTo(0)
        currentMediaPlayer = current.mediaPlayer
        currentMediaPlayer.start()
        paused = false
        currentSongObj = current



        MainActivity.self.changeCurrentSongText(current.name)
        MainActivity.self.updatePlayPause()
        if (current.albumArt != null)
            MainActivity.self.hotbarCover.setImageBitmap(current.albumArt)
        else
            MainActivity.self.hotbarCover.setImageResource(R.drawable.image)
        updateNotification()
        refreshOnResume()
    }

    fun togglePlay() {
        resetCurrentColor()
        if (paused && !currentMediaPlayer.isPlaying) currentMediaPlayer.start()
        else currentMediaPlayer.pause()
        paused = !paused
        updateNotification()
    }

    fun nextSong(){
        resetCurrentColor()
        if (++currentSongIndex > songAdapter.songs.size - 1) currentSongIndex = 0
        changeSongTo(currentSongIndex)
        updateNotification()
    }

    fun prevSong(){
        resetCurrentColor()
        if (--currentSongIndex < 0) currentSongIndex = songAdapter.songs.size - 1
        changeSongTo(currentSongIndex)
        updateNotification()
    }

    fun resetCurrentColor(){
        songAdapter.bounds.forEach { view ->
            view.boundingBox.setTextColor(Color.WHITE)
        }
    }

    fun refreshOnResume(){
        for (i in songAdapter.bounds) {
            if (i.boundingBox.text == currentSongObj.name && i.artistText.text == currentSongObj.artist){
                i.boundingBox.setTextColor(Color.parseColor("#1DB954"))
            }
        }
    }

    // !MUSIC CONTROLS




    // DESTRUCTOR
    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}