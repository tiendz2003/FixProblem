package com.nullpointerexception.cityeye

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.databinding.FragmentFirstBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FirstFragment : Fragment() {

    private var binding: FragmentFirstBinding? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String? = null
    private lateinit var storage:FirebaseStorage
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted:Boolean -> if(isGranted){
            Log.i("PERMISSION", "Granted")
        }else{
            Log.i("PERMISSION", "Denied")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        storage = Firebase.storage

        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.capture?.setOnClickListener{
            if(requestPermission()){
                startCamera()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun startCamera(){
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        photoFile?.also {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "com.nullpointerexception.cityeye.fileprovider", it)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            retrieveImage()
        }
    }
    private fun retrieveImage() {
        val savedImageFile = currentPhotoPath?.let { File(it) }
        Glide.with(this).load(savedImageFile).into(binding!!.imageView)
        val imagesRef = storage.reference.child("images/${savedImageFile?.name}")
        val uploadTask = imagesRef.putFile(Uri.fromFile(savedImageFile))
        uploadTask.addOnSuccessListener {
            Toast.makeText(requireContext(),"Image uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),"Error uploading image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun requestPermission() : Boolean {

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.CAMERA
            ) -> {
                Snackbar.make(requireView(), "PLEASE ACCEPT IT!", Snackbar.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                Snackbar.make(requireView(), "Please enable camera permission in app settings",Snackbar.LENGTH_SHORT)
                    .setAction("Settings"){
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .show()
            }
        }
        return false
    }

}