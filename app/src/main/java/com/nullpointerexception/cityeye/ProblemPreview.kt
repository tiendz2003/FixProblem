package com.nullpointerexception.cityeye

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nullpointerexception.cityeye.data.ProblemPreviewViewModel
import com.nullpointerexception.cityeye.databinding.ActivityProblemPreviewBinding
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import com.nullpointerexception.cityeye.util.LocationUtil
import java.io.File

class ProblemPreview : AppCompatActivity() {

    private lateinit var binding: ActivityProblemPreviewBinding
    private lateinit var viewModel: ProblemPreviewViewModel
    private var areFieldsEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityProblemPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title

        viewModel = ViewModelProvider(this)[ProblemPreviewViewModel::class.java]

        viewModel.setCoordinates(intent.getParcelableExtra("coordinates"))
        viewModel.setImage(intent.getSerializableExtra("image") as File)
        LocationUtil.getAddressFromCo(
            this,
            LatLng(viewModel.coordinates.value!!.latitude, viewModel.coordinates.value!!.longitude)
        )
            ?.let { viewModel.setAddress(it) }

        setProblemImage()
        viewModel.address.value?.let { setAddressText(it) }

        binding.fab.setOnClickListener {
            swapFocusFields()

            if (!binding.contentScrolling.problemTitleEditText!!.checkIfCharactersExceed(20) && !binding.contentScrolling.problemDescriptionEditText!!.checkIfCharactersExceed(
                    50
                )
            ) {
                if (FirebaseDatabase.addNormalProblem(
                        this,
                        binding.contentScrolling.problemTitleEditText!!.text(),
                        binding.contentScrolling.problemDescriptionEditText!!.text(),
                        viewModel.image.value!!,
                        viewModel.coordinates.value!!,
                        viewModel.address.value!!
                    )
                ) {
                    finish()
                } else {
                    swapFocusFields()
                    clearAllFields()

                }
            } else {
                Snackbar.make(
                    window.decorView.rootView,
                    resources.getString(R.string.titleOrDescriptionTooLong),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setAddressText(address: String) {
        binding.contentScrolling.address!!.text = address
    }

    private fun setProblemImage() {
        binding.problemImage.load(viewModel.image.value) {
            transformations(CircleCropTransformation())
        }
    }

    fun TextInputLayout.text(): String {
        return this.editText?.text.toString()
    }

    fun TextInputLayout.empty() {
        this.editText?.text?.clear()
    }

    fun clearAllFields() {
        val fields =
            this.binding.contentScrolling.constraintLayout!!.children.filterIsInstance<TextInputLayout>()
        for (field in fields) {
            field.empty()
        }
    }

    fun swapFocusFields() {
        val fields =
            this.binding.contentScrolling.constraintLayout!!.children.filterIsInstance<TextInputEditText>()

        for (field in fields) {
            field.isEnabled = !areFieldsEnabled
        }
        areFieldsEnabled = !areFieldsEnabled
    }

    fun TextInputLayout.checkIfCharactersExceed(amount: Int): Boolean {
        if (this.text().length > amount) {
            return true
        }
        return false
    }

}