package com.nullpointerexception.cityeye

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.nullpointerexception.cityeye.databinding.FragmentCaptureBinding
import com.nullpointerexception.cityeye.util.CameraUtil
import org.json.JSONArray
import java.io.File
import java.io.IOException


class CaptureFragment : Fragment() {

    private var binding: FragmentCaptureBinding? = null
    private val REQUEST_IMAGE_CAPTURE = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentCaptureBinding.inflate(inflater, container, false)

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
            CameraUtil.createImageFile(requireContext())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            CameraUtil.retrieveImage(requireView())
        }
    }

    private fun requestPermission() : Boolean {

        val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, 100)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("PERMISSIONG", "All good")
                } else {

                    Log.i("TAG", "LMAO")
                }
                return
            }
        }
    }

    //val data = listOf(WeightedLatLng(LatLng(0.0, 0.0), 1.0), WeightedLatLng(LatLng(1.0, 1.0), 100.0))



}