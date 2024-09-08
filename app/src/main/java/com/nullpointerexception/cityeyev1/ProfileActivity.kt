package com.nullpointerexception.cityeyev1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cityeyev1.R
import cityeyev1.databinding.ActivityProfileBinding
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeyev1.data.ProfileViewModel
import com.nullpointerexception.cityeyev1.ui.adapters.RecyclerViewProfileAdapter

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by lazy { ViewModelProvider(this)[ProfileViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setImage()

        viewModel.getUser().observe(this) {
            setInfo()
            viewModel.getUserProblems(viewModel.user.value!!.problems!!)
        }
        viewModel.getProblems().observe(this) {
            setProblems()
            binding.content.problems.pullToRefresh.isRefreshing = false
        }

        viewModel.getCurrentUser(Firebase.auth.currentUser!!.uid)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.content.problems.pullToRefresh.setOnRefreshListener {
            viewModel.getUserProblems(viewModel.getUser().value!!.problems!!)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    fun setImage() {
        val imageUrl = Firebase.auth.currentUser!!.photoUrl
        if (imageUrl.toString() == "null") return
        binding.content.header.userImage.load(imageUrl) {
            transformations(CircleCropTransformation())
            size(1000)
            scale(Scale.FILL)
        }
    }

    fun setInfo() {
        binding.content.header.username.text = viewModel.user.value!!.displayName ?: "Van Tien Bat Bai"
        binding.content.header.problemNumber.text =
            getString(R.string.profileNoProblems, viewModel.user.value!!.problems!!.size)
        binding.content.header.city.text = "Ha Noi"
    }

    fun setProblems() {
        val recyclerView = binding.content.problems.problemsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val problemsSorted =
            viewModel.getProblems().value?.sortedWith(compareByDescending { it.timestamp })
        recyclerView.adapter = RecyclerViewProfileAdapter(
            this,
            ArrayList(problemsSorted)
        )
    }
}