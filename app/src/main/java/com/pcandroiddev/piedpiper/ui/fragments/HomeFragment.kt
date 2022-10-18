package com.pcandroiddev.piedpiper.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pcandroiddev.piedpiper.R
import com.pcandroiddev.piedpiper.adapters.SongAdapter
import com.pcandroiddev.piedpiper.databinding.FragmentHomeBinding
import com.pcandroiddev.piedpiper.other.Status
import com.pcandroiddev.piedpiper.ui.PiedPiperActivity
import com.pcandroiddev.piedpiper.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding = FragmentHomeBinding.bind(view)

        songAdapter = SongAdapter((activity as PiedPiperActivity).glide)

        setupRecyclerView()
        subscribeToObservers()

        // When recyclerview's adapter's any of the song items is clicked, play or pause that song
        songAdapter.setOnItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }

    }

    private fun subscribeToObservers() {

        // Get list of all songs and send them to recyclerview adapter
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.LOADING -> binding.allSongsProgressBar.visibility = View.VISIBLE
                Status.ERROR -> Unit
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.visibility = View.GONE
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                        binding.tvTotalSongs.text = "${songs.size} Songs"
                    }
                }
            }
        }
    }

    // A function to set up our recyclerview
    private fun setupRecyclerView() = binding.rvSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

}