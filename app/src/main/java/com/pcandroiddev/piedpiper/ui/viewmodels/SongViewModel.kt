package com.pcandroiddev.piedpiper.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pcandroiddev.piedpiper.exoplayer.MusicService
import com.pcandroiddev.piedpiper.exoplayer.MusicServiceConnection
import com.pcandroiddev.piedpiper.exoplayer.currentPlaybackPosition
import com.pcandroiddev.piedpiper.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(val musicServiceConnection: MusicServiceConnection): ViewModel() {
    private val playbackState = musicServiceConnection.playbackState

    private var _curSongDuration = MutableLiveData<Long>()
    val curSongDuration = _curSongDuration

    private var _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    // Function to get current position of song
    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }


}
