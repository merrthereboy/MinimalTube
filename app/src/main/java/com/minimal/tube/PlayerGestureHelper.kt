package com.minimal.tube

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.media3.exoplayer.ExoPlayer
import kotlin.math.abs

class PlayerGestureHelper(
    private val activity: Activity,
    private val exoPlayer: () -> ExoPlayer?,
    private val hudVolume: View,
    private val txtVolumePercent: TextView,
    private val progressVolume: ProgressBar,
    private val hudBrightness: View,
    private val txtBrightnessPercent: TextView,
    private val progressBrightness: ProgressBar,
    private val hudSeek: View,
    private val txtSeekText: TextView
) : View.OnTouchListener {

    private val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    private var initialVolume = 0
    private var initialBrightness = 0.5f
    private var isDragging = false
    private var isRightSide = false

    private val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return false // Let PlayerView handle single tap for showing/hiding controls
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val player = exoPlayer() ?: return false
            val width = activity.window.decorView.width
            val isRight = e.x > (width / 2)

            if (isRight) {
                // Seek forward 10s
                val targetPos = (player.currentPosition + 10_000).coerceAtMost(player.duration)
                player.seekTo(targetPos)
                showSeekHud("+10 giây ▶▶")
            } else {
                // Seek backward 10s
                val targetPos = (player.currentPosition - 10_000).coerceAtLeast(0)
                player.seekTo(targetPos)
                showSeekHud("◀◀ -10 giây")
            }
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e1 == null) return false
            val viewWidth = activity.window.decorView.width
            val deltaY = e1.y - e2.y

            if (!isDragging && abs(distanceY) > abs(distanceX)) {
                isDragging = true
                isRightSide = e1.x > (viewWidth / 2)
                if (isRightSide) {
                    initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                } else {
                    val lp = activity.window.attributes
                    initialBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                }
            }

            if (isDragging) {
                val screenHeight = activity.resources.displayMetrics.heightPixels.toFloat()
                val deltaPercent = deltaY / (screenHeight * 0.75f)

                if (isRightSide) {
                    // Adjust Volume
                    val newVol = (initialVolume + (deltaPercent * maxVolume)).toInt()
                        .coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                    val percent = (newVol * 100) / maxVolume
                    showVolumeHud(percent)
                } else {
                    // Adjust Brightness
                    val newBrightness = (initialBrightness + deltaPercent).coerceIn(0.01f, 1.0f)
                    val lp = activity.window.attributes
                    lp.screenBrightness = newBrightness
                    activity.window.attributes = lp
                    val percent = (newBrightness * 100).toInt()
                    showBrightnessHud(percent)
                }
            }
            return true
        }
    })

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (isDragging) {
                isDragging = false
                hudVolume.postDelayed({ hudVolume.visibility = View.GONE }, 1000)
                hudBrightness.postDelayed({ hudBrightness.visibility = View.GONE }, 1000)
            }
        }
        return false // Return false so controller buttons inside PlayerView still receive taps
    }

    private fun showVolumeHud(percent: Int) {
        hudVolume.visibility = View.VISIBLE
        hudBrightness.visibility = View.GONE
        txtVolumePercent.text = "Âm lượng: $percent%"
        progressVolume.progress = percent
    }

    private fun showBrightnessHud(percent: Int) {
        hudBrightness.visibility = View.VISIBLE
        hudVolume.visibility = View.GONE
        txtBrightnessPercent.text = "Độ sáng: $percent%"
        progressBrightness.progress = percent
    }

    private fun showSeekHud(text: String) {
        hudSeek.visibility = View.VISIBLE
        txtSeekText.text = text
        hudSeek.removeCallbacks(hideSeekRunnable)
        hudSeek.postDelayed(hideSeekRunnable, 900)
    }

    private val hideSeekRunnable = Runnable {
        hudSeek.visibility = View.GONE
    }
}
