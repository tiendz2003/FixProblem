package com.nullpointerexception.cityeye

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.Coil
import coil.request.ImageRequest
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
                val request = ImageRequest.Builder(this)
                    .data(url)
                    .target { drawable ->
                        binding.imageLoadingIndicator.hide()
                        binding.image.setImageDrawable(drawable)
                        binding.image.visibility = View.VISIBLE
                    }
                    .build()
                Coil.imageLoader(this).enqueue(request)
            }


        }

        viewModel.getAnswer().observe(this) {
            if (it.response.isNullOrEmpty()) {
                binding.answer.text = getString(R.string.answer, getString(R.string.no_answer))
            } else {
                binding.answer.text = getString(R.string.answer, it.response)
            }
        }

        binding.back.setOnClickListener {
            finish()
        }


    }
}