package com.nullpointerexception.cityeyev1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cityeyev1.R
import cityeyev1.databinding.ActivityEventDetailBinding
import coil.Coil
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeyev1.data.EventDetailViewModel
import com.nullpointerexception.cityeyev1.entities.Event
import com.nullpointerexception.cityeyev1.util.OtherUtilities


class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var viewModel: EventDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[EventDetailViewModel::class.java]


        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.setEvent(intent.getSerializableExtra("event") as Event)

        viewModel.getEvent().observe(this) {

            binding.title.text = viewModel.event.value!!.title

            val request = ImageRequest.Builder(this)
                .data(viewModel.event.value!!.imageUrl)
                .target(binding.image)
                .build()

            ImageLoader(this).enqueue(request)

            binding.image.load(viewModel.event.value!!.imageUrl)

            Firebase.storage.reference.child("eventImages/${viewModel.event.value!!.imageUrl}").downloadUrl.addOnSuccessListener { url ->

                val request = ImageRequest.Builder(this)
                    .data(url)
                    .target { drawable ->
                        binding.image.setImageDrawable(drawable)
                        binding.image.visibility = View.VISIBLE
                    }
                    .build()
                Coil.imageLoader(this).enqueue(request)
            }

            binding.price.price.text =
                getString(R.string.priceText, viewModel.event.value!!.price.toString())

            binding.datetime.fullTime.text =
                viewModel.event.value!!.epochStart?.let { it1 ->
                    OtherUtilities().epochToFormattedString(
                        it1
                    )
                }

            binding.datetime.durationTime.text =
                getString(
                    R.string.event_duration,
                    OtherUtilities().getTimeFromEpoch(viewModel.event.value!!.epochStart!!),
                    OtherUtilities().getTimeFromEpoch(
                        viewModel.event.value!!.epochEnd!!
                    )
                )

            binding.location.locationTitle.text = viewModel.event.value!!.location
            binding.location.locationAddress.text = viewModel.event.value!!.locationAddress

            binding.about.aboutText.text = viewModel.event.value!!.description
        }

        binding.datetime.addToCalendar.setOnClickListener {
            addToCalendar()
        }

        binding.location.viewOnMaps.setOnClickListener {
            openMaps()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.shareButton.setOnClickListener {
            var textToShare = getString(
                R.string.shareEvent,
                viewModel.event.value?.title,
                viewModel.event.value?.location,
                viewModel.event.value?.epochStart?.let { it1 ->
                    OtherUtilities().epochToFormattedString(
                        it1
                    )
                } + " - " + OtherUtilities().epochToFormattedString(
                    viewModel.event.value?.epochEnd!!
                ),
                viewModel.event.value?.price.toString()
            )
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

    }

    private fun addToCalendar() {

        val startTimeMillis = viewModel.event.value?.epochStart?.times(1000)
        val endTimeMillis = viewModel.event.value?.epochEnd?.times(1000)

        val intent = Intent(Intent.ACTION_INSERT)
        intent.data = CalendarContract.Events.CONTENT_URI
        intent.putExtra(CalendarContract.Events.TITLE, viewModel.event.value?.title)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, viewModel.event.value?.description)
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, viewModel.event.value?.location)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)

        startActivity(intent)
    }

    private fun openMaps() {
        val uri = Uri.parse("geo:0,0?q=${viewModel.event.value?.locationAddress}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }
}