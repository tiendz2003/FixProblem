package com.nullpointerexception.cityeye

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.data.NotificationsViewModel
import com.nullpointerexception.cityeye.databinding.ActivityNotificationsBinding
import com.nullpointerexception.cityeye.databinding.NotificationLayoutBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.entities.UserNotification
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import com.nullpointerexception.cityeye.ui.adapters.RecyclerViewNotificationsAdapter
import com.nullpointerexception.cityeye.ui.adapters.RecyclerViewProfileAdapter

class NotificationsActivity : AppCompatActivity() {

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        Firebase.auth.currentUser?.let { viewModel.getUserFromDatabase(it.uid) }

        viewModel.getUser().observe(this) {
            viewModel.getUserNotifications()
        }
        viewModel.getNotifications().observe(this) {
            setNotifications()
        }

    }

    override fun onStart() {
        super.onStart()

        Firebase.auth.currentUser?.let { viewModel.getUserFromDatabase(it.uid) }

        viewModel.getUser().observe(this) {
            viewModel.getUserNotifications()
        }
        viewModel.getNotifications().observe(this) {
            setNotifications()
        }
    }

    fun setNotifications() {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerViewNotificationsAdapter(
            this,
            viewModel.getNotifications().value as ArrayList<UserNotification>
        )
    }

}