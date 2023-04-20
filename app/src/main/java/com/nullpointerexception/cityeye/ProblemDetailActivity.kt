package com.nullpointerexception.cityeye

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.data.ProblemDetailViewModel
import com.nullpointerexception.cityeye.databinding.ActivityProblemDetailBinding

class ProblemDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: ProblemDetailViewModel
    private lateinit var binding: ActivityProblemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProblemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProblemDetailViewModel::class.java]

        viewModel.getThisProblem(intent.getStringExtra("problemID")!!)

        viewModel.getProblem().observe(this) {
            binding.title.append(it.title)
            binding.description.append(it.description)
            binding.address.append(it.address)
            binding.isSolved.append(if (it.solved.toString() === "true") "Yes" else "No")


            Firebase.storage.reference.child("images/${it.imageName}").downloadUrl.addOnSuccessListener { url ->
                binding.image.load(url) {
                    transformations(CircleCropTransformation())
                }
            }
        }

        binding.back.setOnClickListener {
            finish()
        }


    }
}