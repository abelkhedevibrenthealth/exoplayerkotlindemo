package com.example.exoplayerkotlindemo.ui.home

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.exoplayerkotlindemo.databinding.FragmentHomeBinding
import com.example.exoplayerkotlindemo.ui.AppState
import com.example.exoplayerkotlindemo.ui.FragmentStateForVideo
import com.example.exoplayerkotlindemo.ui.ShowVideoFragmentArgs
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class HomeFragment : Fragment() {

    private val args : HomeFragmentArgs by navArgs()

    private var _binding: FragmentHomeBinding? = null

    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var trackSelectionParameters: TrackSelectionParameters? = null

    private var startAutoPlay = false
    private var startItemIndex = 0
    private var startPosition: Long = 0

    private val compositeDisposable = CompositeDisposable()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        startAutoPlay= args.startAutoPlay
        startItemIndex = args.startItemIndex
        startPosition = args.startPosition

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        trackSelectionParameters = TrackSelectionParameters.Builder(requireContext()).build();


        playerView = binding.playerView
        playerView?.setErrorMessageProvider(PlayerErrorMessageProvider())
        playerView?.requestFocus()



        playerView?.setFullscreenButtonClickListener {
            updateStartPosition()

            val action = HomeFragmentDirections.actionNavigationHomeToShowVideoFragment(
                startAutoPlay,
                startItemIndex,
                startPosition
            )
            findNavController().navigate(action)
        }
//            if (isFullScreen) {
//                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//            } else {
//                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//
//            }
//        }

        binding.requestFocusToTop.setOnClickListener {
            updateStartPosition()

            val action = HomeFragmentDirections.actionNavigationHomeToShowVideoFragment(startAutoPlay, startItemIndex, startPosition)
            findNavController().navigate(action)
        }

        compositeDisposable.add(FragmentStateForVideo.appStateObserver
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { appState ->
                when(appState) {
                    AppState.resume -> {
                        println("resume")
                    }
                    AppState.pause -> {
                        println("pause")
                    }
                    AppState.destroy -> {
                        println("destroy")
                    }
                }

            }
        )


        return root
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        if (Build.VERSION.SDK_INT > 23) {
            initializePlayer()
            if (playerView != null) {
                playerView!!.onResume()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23 || player == null) {
            initializePlayer()
            if (playerView != null) {
                playerView!!.onResume()
            }
        }

        FragmentStateForVideo.appStateObserver.onNext(AppState.resume)
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
        if (playerView != null) {
            playerView!!.onPause()
        }
        releasePlayer()
        FragmentStateForVideo.appStateObserver.onNext(AppState.pause)
    }

    protected fun initializePlayer(): Boolean {
        val mediaItems = MediaItem.Builder()
            .setUri("https://html5demos.com/assets/dizzy.mp4")
            .setMediaMetadata( MediaMetadata.Builder().setTitle("Clear DASH: Tears").build())
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        if (player == null) {


            val playerBuilder = ExoPlayer.Builder( requireContext())
                .setMediaSourceFactory(DefaultMediaSourceFactory(requireContext()))
            player = playerBuilder.build()
            player?.trackSelectionParameters = trackSelectionParameters!!
            player?.addAnalyticsListener(EventLogger())
            player?.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            player?.playWhenReady = true
            playerView?.player = player
        }
        player!!.setMediaItem(mediaItems)
        player!!.prepare()
        val haveStartPosition = startItemIndex != C.INDEX_UNSET
        if (haveStartPosition) {
            player?.seekTo(startPosition)
        }
        return true
    }

    protected fun releasePlayer() {
        if (player != null) {
            updateStartPosition()
            player!!.release()
            player = null
            playerView!!.player = null
            playerView!!.adViewGroup.removeAllViews()
        }
    }

    private fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player!!.playWhenReady
            startItemIndex = player!!.currentMediaItemIndex
            startPosition = Math.max(0, player!!.contentPosition)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        FragmentStateForVideo.appStateObserver.onNext(AppState.destroy)
    }

}