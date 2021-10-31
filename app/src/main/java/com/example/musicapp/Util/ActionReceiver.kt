package com.example.musicapp.Util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.musicapp.MainActivity

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SongService.ACTION_PREV -> SongService.self.prevSong()
            SongService.ACTION_NEXT ->  SongService.self.nextSong()
            SongService.ACTION_PAUSE -> SongService.self.togglePlay()
        }
    }

}