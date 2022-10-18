package com.pcandroiddev.piedpiper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.pcandroiddev.piedpiper.R
import com.pcandroiddev.piedpiper.adapters.SwipeSongAdapter
import com.pcandroiddev.piedpiper.data.entities.Song
import com.pcandroiddev.piedpiper.databinding.ActivityPiedpiperBinding
import com.pcandroiddev.piedpiper.exoplayer.isPlaying
import com.pcandroiddev.piedpiper.exoplayer.toSong
import com.pcandroiddev.piedpiper.other.Status
import com.pcandroiddev.piedpiper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PiedPiperActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject // For Image Loading
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private lateinit var binding: ActivityPiedpiperBinding

    private var playbackState: PlaybackStateCompat ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPiedpiperBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        swipeSongAdapter = SwipeSongAdapter(glide)

        subscribeToObservers()

        binding.vpSong.adapter = swipeSongAdapter

        binding.ivPlayPause.setOnClickListener {
            //Toast.makeText(this, "isPlaying: ${playbackState?.isPlaying} | isPlayEnabled: ${playbackState?.isPlayEnabled} | isPrepared: ${playbackState?.isPrepared}", Toast.LENGTH_SHORT).show()
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }

        }

        binding.vpSong.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying==true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        val navController = findNavController(R.id.navHostFragment)

        swipeSongAdapter.setOnItemClickListener {
            navController.navigate(R.id.globalActionToSongFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }


    }

    // It will set viewpager to current playing song
    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex!=-1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {

        // List of all songs
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songList ->
                            swipeSongAdapter.songs = songList
                            if(songList.isNotEmpty()) {
                                glide.load((curPlayingSong?:songList[0]).imageUrl).into(binding.ivCurSongImage)
                                switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
                            }
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        // Information about currently playing song in MediaMetadataCompat format
        mainViewModel.curPlayingSong.observe(this) {
            if(it==null) return@observe
            curPlayingSong = it.toSong() // MediaMetadataCompat converted to our Song.kt class format using the extension function we created in MediaMetadataCompat.kt
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
        }

        // Play / Pause state of our song
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                    if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        // Check whether media browser is connected or not
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result->
                when(result.status) {
                    Status.ERROR -> Toast.makeText(this,"An unknown error occurred",Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        }

        // Check whether internet is connected or not
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result->
                when(result.status) {
                    Status.ERROR -> Toast.makeText(this,"Error: Please check your internet connection",Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        }

    }

    private fun hideBottomBar() {
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }



}