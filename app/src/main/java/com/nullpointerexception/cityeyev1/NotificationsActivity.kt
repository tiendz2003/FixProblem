package com.nullpointerexception.cityeyev1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cityeyev1.databinding.ActivityNotificationsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeyev1.data.NotificationsViewModel
import com.nullpointerexception.cityeyev1.entities.UserNotification
import com.nullpointerexception.cityeyev1.ui.adapters.RecyclerViewNotificationsAdapter
import com.nullpointerexception.cityeyev1.util.DateComparator

class NotificationsActivity : AppCompatActivity() {

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        Firebase.auth.currentUser?.let { viewModel.getUserFromDatabase(it.uid) }

        viewModel.getUser().observe(this) {
            viewModel.getUserNotifications()
        }
        viewModel.getNotifications().observe(this) {
            setNotifications()
        }

        binding.backButton.setOnClickListener {
            finish()
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
            (viewModel.getNotifications().value as List<UserNotification>).sortedWith(DateComparator())
        )
    }

}