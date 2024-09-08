package com.nullpointerexception.cityeyev1.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cityeyev1.R
import cityeyev1.databinding.NearbyPlaceBinding
import coil.Coil
import coil.request.ImageRequest
import com.google.maps.model.PlacesSearchResult



class RecyclerViewPlacesAdapter(
    val context: Context,
    val places: List<PlacesSearchResult>
) :
    RecyclerView.Adapter<RecyclerViewPlacesAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = NearbyPlaceBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater.from(context).inflate(R.layout.nearby_place, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]

        holder.binding.loadIndicator.show()

        holder.binding.title.text = place.name
        holder.binding.title.isSelected = true
        holder.binding.title.isSingleLine = true
        holder.binding.title.marqueeRepeatLimit = -1
        holder.binding.title.ellipsize = TextUtils.TruncateAt.MARQUEE


        holder.binding.rating.text =
            context.getString(R.string.rating, place.rating.toString(), "5.0")

        val workingTime = place.openingHours

        var hoursText = if (workingTime != null) {
            if (workingTime.openNow != null && !workingTime.openNow) {
                "Closed"
            } else if (workingTime.permanentlyClosed != null && workingTime.permanentlyClosed == true) {
                "Permanenty closed"
            } else {
                "Open"
            }
        } else {
            "No information"
        }

        holder.binding.workingHours.text = hoursText


        if (!place.photos.isNullOrEmpty()) {
            val apiKey = context.resources.getString(R.string.maps_key)
            Log.i(
                "LINK",
                context.resources.getString(
                    R.string.place_image,
                    place.photos[0].photoReference,
                    apiKey
                )
            )
            val request = ImageRequest.Builder(context)
                .data(
                    context.resources.getString(
                        R.string.place_image,
                        place.photos[0].photoReference,
                        apiKey
                    )
                )
                .target { drawable ->
                    holder.binding.loadIndicator.hide()
                    holder.binding.image.setImageDrawable(drawable)
                    holder.binding.image.visibility = View.VISIBLE
                }
                .build()
            Coil.imageLoader(context).enqueue(request)
        }

        holder.binding.direction.setOnClickListener {
            val placeId = place.placeId
            val uri =
                Uri.parse("https://www.google.com/maps/search/?api=1&query=Google&query_place_id=$placeId")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = places.size

}