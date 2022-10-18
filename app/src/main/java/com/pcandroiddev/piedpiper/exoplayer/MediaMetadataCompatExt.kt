package com.pcandroiddev.piedpiper.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.pcandroiddev.piedpiper.data.entities.Song

// Function to convert MediaMetadataCompat object to our Song.kt class object
fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(it.mediaId?:"",it.title.toString(),it.subtitle.toString(),it.mediaUri.toString(),it.iconUri.toString())
    }
}