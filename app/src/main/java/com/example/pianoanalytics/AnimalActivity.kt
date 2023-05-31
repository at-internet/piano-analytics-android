package com.example.pianoanalytics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.pianoanalytics.databinding.ActivityAnimalBinding
import io.piano.android.analytics.PianoAnalytics
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

class AnimalActivity: AppCompatActivity(R.layout.activity_animal) {
    private val binding: ActivityAnimalBinding by viewBinding(R.id.animalText)
    private lateinit var item: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = intent.getStringExtra(ITEM_KEY) ?: ""
        binding.animalText.text = getString(R.string.item_text, item)
    }

    override fun onResume() {
        super.onResume()
        PianoAnalytics.getInstance().apply {
            screenName(item)
            sendEvents(
                Event.Builder(Event.PAGE_DISPLAY)
                    .properties(
                        Property(PropertyName.PAGE_FULL_NAME, item),
                        Property(PropertyName.PAGE_CHAPTER1, item),
                    )
                    .build()
            )
        }
    }

    companion object {
        const val ITEM_KEY = "item"
    }
}
