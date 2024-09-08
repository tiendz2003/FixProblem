package com.nullpointerexception.cityeyev1.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import cityeyev1.R
import cityeyev1.databinding.NotificationLayoutBinding
import com.nullpointerexception.cityeyev1.ProblemDetailActivity
import com.nullpointerexception.cityeyev1.entities.UserNotification
import com.nullpointerexception.cityeyev1.firebase.FirebaseDatabase

class RecyclerViewNotificationsAdapter(
    val context: Context,
    val notifications: List<UserNotification>
) :
    RecyclerView.Adapter<RecyclerViewNotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = NotificationLayoutBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        if (!notification.isRead!!) {
            val colorResId = R.color.green
            val colorValue = ContextCompat.getColor(context, colorResId)
            val backgroundColor = ColorUtils.setAlphaComponent(colorValue, 50)
            holder.binding.layout.setBackgroundColor(backgroundColor)
        }

        holder.binding.title.text = notification.problemTitle
        holder.binding.time.text = notification.time.toString()

        holder.binding.layout.setOnClickListener {
            notification.notificationID?.let { it1 ->
                FirebaseDatabase.markNotificationAsRead(it1)
            }
            val intent = Intent(context, ProblemDetailActivity::class.java)
            intent.putExtra("problemID", notification.problemID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = notifications.size

}