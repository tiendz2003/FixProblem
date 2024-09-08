package com.nullpointerexception.cityeyev1.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cityeyev1.R
import cityeyev1.databinding.EventListItemBinding
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeyev1.EventDetailActivity
import com.nullpointerexception.cityeyev1.entities.Event
import com.nullpointerexception.cityeyev1.util.OtherUtilities


class RecyclerViewEvents(
    val context: Context,
    val activity: Activity,
    val events: List<Event>
) :
    RecyclerView.Adapter<RecyclerViewEvents.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = EventListItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return if (viewType == 1) {
            EventViewHolder(
                LayoutInflater.from(context).inflate(R.layout.event_list_first_item, parent, false)
            )
        } else {
            EventViewHolder(
                LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.binding.title.text = event.title
        holder.binding.locationText.text = event.location
        holder.binding.date.text = OtherUtilities().epochToFormattedString(event.epochStart!!)

        Firebase.storage.reference.child("eventImages/${event.imageUrl}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url)

            val request = ImageRequest.Builder(context)
                .data(url)
                .target { drawable ->
                    holder.binding.image.setImageDrawable(drawable)
                    holder.binding.image.visibility = View.VISIBLE
                }
                .build()
            Coil.imageLoader(context).enqueue(request)
        }

        holder.binding.share.setOnClickListener {
            var textToShare = context.getString(
                R.string.shareEvent,
                event.title,
                event.location,
                OtherUtilities().epochToFormattedString(event.epochStart) + " - " + OtherUtilities().epochToFormattedString(
                    event.epochEnd!!
                ),
                event.price.toString()
            )
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
            shareIntent.type = "text/plain"
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        holder.binding.layout.setOnClickListener {
            startEventDetailActivity(event)
        }

    }

    override fun getItemCount() = events.size


    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 1
        else 2
    }

    fun startEventDetailActivity(event: Event) {
        val intent = Intent(activity, EventDetailActivity::class.java)
        intent.putExtra("event", event)
        activity.startActivity(intent)
    }
}