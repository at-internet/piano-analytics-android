package com.example.pianoanalytics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.pianoanalytics.databinding.ActivityMainBinding
import io.piano.android.analytics.PianoAnalytics
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding: ActivityMainBinding by viewBinding(R.id.recyclerview)

    private val animals = listOf(
        "___media___",
        "alligator", "ant", "bear", "bee", "bird", "camel", "cat",
        "cheetah", "chicken", "chimpanzee", "cow", "crocodile", "deer", "dog", "dolphin", "duck",
        "eagle", "elephant", "fish", "fly", "fox", "frog", "giraffe", "goat", "goldfish", "hamster",
        "hippopotamus", "horse", "kangaroo", "kitten", "lion", "lobster", "monkey", "octopus", "owl",
        "panda", "pig", "puppy", "rabbit", "rat", "scorpion", "seal", "shark", "sheep", "snail",
        "snake", "spider", "squirrel", "tiger", "turtle", "wolf", "zebra"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = MainAdapter(animals, this@MainActivity::onItemClick)
        }
    }

    override fun onResume() {
        super.onResume()
        PianoAnalytics.getInstance().sendEvents(
            Event.Builder(Event.PAGE_DISPLAY)
                .properties(
                    Property(PropertyName.PAGE_FULL_NAME, "main"),
                )
                .build()
        )
    }

    private fun onItemClick(item: String) {
        PianoAnalytics.getInstance().sendEvents(
            Event.Builder(Event.CLICK_NAVIGATION)
                .properties(
                    Property(PropertyName.CLICK, item),
                )
                .build()
        )
        val intent = if (item == animals.first()) {
            Intent(this, MediaActivity::class.java)
                .putExtra(MediaActivity.CONTENT_ID_KEY, item)
        } else {
            Intent(this, AnimalActivity::class.java)
                .putExtra(AnimalActivity.ITEM_KEY, item)
        }
        startActivity(intent)
    }
}
