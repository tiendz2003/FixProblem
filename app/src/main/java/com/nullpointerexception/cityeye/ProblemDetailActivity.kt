package com.nullpointerexception.cityeye

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.data.ProblemDetailViewModel
import com.nullpointerexception.cityeye.databinding.ActivityProblemDetailBinding
import com.nullpointerexception.cityeye.util.OtherUtilities

class ProblemDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: ProblemDetailViewModel
    private lateinit var binding: ActivityProblemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProblemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ProblemDetailViewModel::class.java]

        viewModel.getThisProblem(intent.getStringExtra("problemID")!!)

        viewModel.getProblem().observe(this) {
            binding.title.text = it.title
            binding.description.text = it.description
            binding.address.text = it.address
            binding.date.text = it.epoch?.let { it1 -> OtherUtilities().getDateFromEpoch(it1) }


            Firebase.storage.reference.child("images/${it.imageName}").downloadUrl.addOnSuccessListener { url ->
                binding.image.load(url)
            }
        }

        binding.back.setOnClickListener {
            finish()
        }


    }
}