package com.example.exoplayerkotlindemo.ui

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.exoplayerkotlindemo.databinding.FragmentShowVideoBinding
import com.example.exoplayerkotlindemo.ui.home.HomeFragmentDirections
import com.example.exoplayerkotlindemo.ui.home.PlayerErrorMessageProvider
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


/**
 * A simple [Fragment] subclass.
 * Use the [ShowVideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShowVideoFragment : Fragment() {

    private val args : ShowVideoFragmentArgs by navArgs()

    private var _binding: FragmentShowVideoBinding? = null

    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var trackSelectionParameters: TrackSelectionParameters? = null

    private var startAutoPlay = false
    private var startItemIndex = 0
    private var startPosition: Long = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        // Inflate the layout for this fragment
        _binding =  FragmentShowVideoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        startAutoPlay= args.startAutoPlay
        startItemIndex = args.startItemIndex
        startPosition = args.startPosition


        trackSelectionParameters = TrackSelectionParameters.Builder(requireContext()).build();


        playerView = binding.playerView
        playerView?.setErrorMessageProvider(PlayerErrorMessageProvider())
        playerView?.requestFocus()
        playerView?.useArtwork = true

        playerView?.setFullscreenButtonClickListener {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            updateStartPosition()

            val action = ShowVideoFragmentDirections.actionShowVideoFragmentToNavigationHome(
                startAutoPlay,
                startItemIndex,
                startPosition
            )
            findNavController().navigate(action)
        }
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
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (playerView != null) {
            playerView!!.onPause()
        }
        releasePlayer()
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
            player?.setAudioAttributes(AudioAttributes.DEFAULT, true)
            player?.playWhenReady = false
            playerView?.player = player
        }

        player?.setMediaItem(mediaItems)
        player?.prepare()
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



}