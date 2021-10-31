package com.example.musicapp.Util

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Debug
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.song_cell.view.*
import org.intellij.lang.annotations.JdkConstants


class SongAdapter(
    var songs: MutableList<Song>
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Filterable {

    class SongViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.song_cell,
                parent,
                false
            )
        )
    }



    fun addSong(song: Song){
        songs.add(song)
        notifyItemInserted(songs.size - 1)
    }

    val bounds = ArrayList<View>()

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val current = songs[position]
        bounds.add(holder.itemView)

        holder.itemView.apply {


            boundingBox.text = current.name
            if (MainActivity.songService.currentSongObj.name ==
                boundingBox.text){
                boundingBox.setTextColor(Color.parseColor("#1DB954"))
            }


            boundingBox.setOnClickListener {

                MainActivity.songService.resetCurrentColor()
                MainActivity.songService.changeSongTo(position)
                MainActivity.songService.currentSongIndex = position

                val b = it as Button
                b.setTextColor(Color.parseColor("#1DB954"))

                MainActivity.self.updateGradient(MainActivity.songService.currentSongObj.albumArt)
            }

            if (current.albumArt != null){
                cellCover.setImageBitmap(current.albumArt)
            }

            artistText.text = current.artist

            optionsButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, "${current.name} ${current.artist}")
                MainActivity.self.startActivity(intent)
            }


        }
    }


    override fun getItemCount(): Int {
        return songs.size
    }


    var filteredSongs = this.songs

    override fun getFilter(): Filter {

        return object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filterResults =  FilterResults();
                if (p0 == null || p0.isEmpty()) {
                    filterResults.count = filteredSongs.size
                    filterResults.values = filteredSongs
                }
                else{
                    val searchResult = p0.toString().lowercase()
                    val songItems = ArrayList<Song>()
                    for (song in filteredSongs){
                        if (searchResult in song.name.lowercase() ||
                                searchResult in song.artist.lowercase() ||
                                searchResult in song.album.lowercase()){
                            songItems.add(song)
                        }
                    }
                    filterResults.count = songItems.size
                    filterResults.values = songItems
                }

                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                songs = p1!!.values as MutableList<Song>
                notifyDataSetChanged()
            }

        }
    }
}