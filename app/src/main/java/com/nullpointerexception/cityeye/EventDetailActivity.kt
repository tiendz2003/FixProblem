package com.nullpointerexception.cityeye

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.nullpointerexception.cityeye.data.EventDetailViewModel
import com.nullpointerexception.cityeye.databinding.ActivityEventDetailBinding
import com.nullpointerexception.cityeye.entities.Event
import com.nullpointerexception.cityeye.util.OtherUtilities

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
            binding.toolbarLayout.title = viewModel.event.value!!.title
            binding.image.load(viewModel.event.value!!.imageUrl)
            binding.content.price!!.price.text = "$ ${viewModel.event.value!!.price.toString()}"
            binding.content.datetime!!.fullTime.text =
                viewModel.event.value!!.epochStart?.let { it1 ->
                    OtherUtilities().epochToFormattedString(
                        it1
                    )
                }
            binding.content.datetime!!.durationTime.text =
                "${OtherUtilities().getTimeFromEpoch(viewModel.event.value!!.epochStart!!)} - ${
                    OtherUtilities().getTimeFromEpoch(
                        viewModel.event.value!!.epochEnd!!
                    )
                }"
        }

        binding.content.datetime?.addToCalendar?.setOnClickListener {
            addToCalendar()
        }

    }

    fun addToCalendar() {

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
}