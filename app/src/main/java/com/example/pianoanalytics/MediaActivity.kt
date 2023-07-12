package com.example.pianoanalytics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.pianoanalytics.databinding.ActivityMediaBinding
import io.piano.android.analytics.PianoAnalytics
import io.piano.android.analytics.MediaHelper
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import kotlin.random.Random

class MediaActivity: AppCompatActivity(R.layout.activity_media) {
    private val binding: ActivityMediaBinding by viewBinding(R.id.media_root)
    private lateinit var contentId: String
    private lateinit var mediaHelper: MediaHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentId = intent.getStringExtra(CONTENT_ID_KEY) ?: "unknown"
        if (savedInstanceState == null) {
            mediaHelper = PianoAnalytics.getInstance().mediaHelper(contentId)
        }
        binding.apply {
            play.setOnClickListener {
                mediaHelper.play(
                    Random.Default.nextInt(),
                    Property(PropertyName("av_content"), contentId)
                )
            }
            playbackStart.setOnClickListener {
                mediaHelper.playbackStart(Random.Default.nextInt())
            }
            playbackStop.setOnClickListener {
                mediaHelper.playbackStopped(Random.Default.nextInt())
            }
            bufferStart.setOnClickListener {
                mediaHelper.bufferStart(Random.Default.nextInt())
            }
            seekStart.setOnClickListener {
                mediaHelper.seekStart(Random.Default.nextInt())
            }
            volume.setOnClickListener {
                mediaHelper.volume(Property(PropertyName("av_content_duration"), Random.Default.nextInt()))
            }
            share.setOnClickListener {
                mediaHelper.volume(Property(PropertyName("av_publication_date"), Random.Default.nextInt()))
            }
        }
    }

    companion object {
        const val CONTENT_ID_KEY = "content_id"
    }
}
