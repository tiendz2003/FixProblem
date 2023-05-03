package com.nullpointerexception.cityeye

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nullpointerexception.cityeye.data.ProblemPreviewViewModel
import com.nullpointerexception.cityeye.databinding.ActivityProblemPreviewBinding
import java.io.File


class ProblemPreview : AppCompatActivity() {

    private lateinit var binding: ActivityProblemPreviewBinding
    private lateinit var viewModel: ProblemPreviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProblemPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.loadIndicator.hide()

        viewModel = ViewModelProvider(this)[ProblemPreviewViewModel::class.java]

        viewModel.setCoordinates(intent.getParcelableExtra("coordinates"))
        viewModel.setImage(intent.getSerializableExtra("image") as File)

        viewModel.getAddressFromLocation(
            this,
            LatLng(viewModel.coordinates.value!!.latitude, viewModel.coordinates.value!!.longitude)
        )

        viewModel.address.observe(this) {
            if (it.isNullOrEmpty()) {
                finish()
            } else {
                setAddressText(it)
            }
        }

        setProblemImage()


        binding.fab.setOnClickListener {


            if (!binding.problemTitleEditText.checkIfCharactersExceed(20) && !binding.problemDescriptionEditText.checkIfCharactersExceed(
                    70
                )
            ) {

                if (!binding.problemTitleEditText.checkIfHasLessAmountOfCharacters() && !binding.problemDescriptionEditText.checkIfHasLessAmountOfCharacters()) {

                    viewModel.addProblem(
                        this,
                        binding.problemTitleEditText.text(),
                        binding.problemDescriptionEditText.text(),
                        viewModel.image.value!!,
                        viewModel.coordinates.value!!,
                        viewModel.address.value!!
                    )
                    binding.loadIndicator.show()

                } else {
                    Snackbar.make(
                        window.decorView.rootView,
                        resources.getString(R.string.titleOrDescriptionNotLongEnough),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    window.decorView.rootView,
                    resources.getString(R.string.titleOrDescriptionTooLong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }


        binding.back.setOnClickListener {
            finish()
        }

        viewModel.response.observe(this) { response ->
            binding.layout.children.iterator().forEach { it.visibility = View.INVISIBLE }
            if (response) {
                binding.animationDone.visibility = View.VISIBLE
                binding.animationDone.playAnimation()
                delay(3000)
            } else {
                binding.animationError.visibility = View.VISIBLE
                binding.animationError.playAnimation()
                delay(4000)
            }
        }
    }

    private fun delay(milis: Long) {
        Handler().postDelayed({
            finish()
        }, milis)
    }

    private fun setAddressText(address: String) {
        binding.address.text = address
    }

    private fun setProblemImage() {
        binding.problemImage.load(viewModel.image.value) {
            transformations(CircleCropTransformation())
        }
    }

    fun TextInputLayout.text(): String {
        return this.editText?.text.toString()
    }

    private fun TextInputLayout.empty() {
        this.editText?.text?.clear()
    }

    private fun TextInputLayout.checkIfCharactersExceed(amount: Int): Boolean {
        if (this.text().length > amount) {
            return true
        }
        return false
    }

    private fun TextInputLayout.checkIfHasLessAmountOfCharacters(): Boolean {
        if (this.text().length < 5) {
            return true
        }
        return false
    }

}