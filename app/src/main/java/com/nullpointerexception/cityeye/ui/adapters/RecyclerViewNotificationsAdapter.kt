package com.nullpointerexception.cityeye.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.nullpointerexception.cityeye.ProblemDetailActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.NotificationLayoutBinding
import com.nullpointerexception.cityeye.entities.UserNotification
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase

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
        holder.binding.time.text = notification.time

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