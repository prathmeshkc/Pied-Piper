package com.pcandroiddev.piedpiper.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.pcandroiddev.piedpiper.R
import com.pcandroiddev.piedpiper.data.entities.Song
import com.pcandroiddev.piedpiper.databinding.FragmentSongBinding
import com.pcandroiddev.piedpiper.exoplayer.isPlaying
import com.pcandroiddev.piedpiper.exoplayer.toSong
import com.pcandroiddev.piedpiper.other.Status
import com.pcandroiddev.piedpiper.ui.viewmodels.MainViewModel
import com.pcandroiddev.piedpiper.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null

    private lateinit var binding: FragmentSongBinding

    private var playbackState: PlaybackStateCompat ?= null
    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding = FragmentSongBinding.bind(view)

        subscribeToObservers()

        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Do not update seekbar through song position
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })


    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
//        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        // I am subtracting (30*60000) from position (it) because the output was 30 minutes + current position so I deducted 30 minutes in form of milliseconds
//        binding.tvCurTime.text = dateFormat.format(ms-(30*60000))


        val mm: Long = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val ss: Long = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        val timeInMMSS = String.format("%02d:%02d", mm, ss)
        binding.tvCurTime.text = timeInMMSS

//        Log.d("setCurPlayerTimeToTextView", "${ms/1000/60}:${ms/1000%60}")
        Log.d("setCurPlayerTimeToTextView", timeInMMSS)
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        binding.tvSongName.text = title
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservers() {

        // List of all songs
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if(curPlayingSong==null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        // Current Song
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if(it==null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }

        // Play / Pause state of our song
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.ivPlayPauseDetail.setImageResource(if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play)
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }


        // Position of current song
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()

                setCurPlayerTimeToTextView(it)
            }
        }

        // Duration of current song
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            /*val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            // I am subtracting (30*60000) from duration (it) because the output was 30 minutes + current duration so I deducted 30 minutes in form of milliseconds

            binding.tvSongDuration.text = dateFormat.format(it-(30*60000))
*/

            val mm: Long = TimeUnit.MILLISECONDS.toMinutes(it) % 60
            val ss: Long = TimeUnit.MILLISECONDS.toSeconds(it) % 60
            val timeInMMSS = String.format("%02d:%02d", mm, ss)
            binding.tvSongDuration.text = timeInMMSS






        }

    }

}
