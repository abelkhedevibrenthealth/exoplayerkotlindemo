package com.example.exoplayerkotlindemo.ui.home

import android.util.Pair
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.util.ErrorMessageProvider

class PlayerErrorMessageProvider: ErrorMessageProvider<PlaybackException> {
    override fun getErrorMessage(throwable: PlaybackException): Pair<Int, String> {
        var errorString: String = "Playback failed"
        val cause: Throwable? = throwable.cause
        if (cause is DecoderInitializationException) {
            // Special case for decoder initialization failures.
            val decoderInitializationException = cause
            if (decoderInitializationException.codecInfo == null) {
                if (decoderInitializationException.cause is DecoderQueryException) {
                    errorString = "Unable to query device decoders"
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = "This device does not provide a secure decoder for ${decoderInitializationException.mimeType}"

                } else {
                    errorString = "This device does not provide a decoder for ${decoderInitializationException.mimeType} "

                }
            } else {
                errorString = "Unable to instantiate decoder ${decoderInitializationException.codecInfo!!.name}"

            }
        }
        return Pair.create(0, errorString)
    }
}