package com.nullpointerexception.cityeye

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.installations.Utils
import com.nullpointerexception.cityeye.databinding.FragmentCaptureBinding
import com.nullpointerexception.cityeye.util.CameraUtil
import com.nullpointerexception.cityeye.util.OtherUtilities
import com.nullpointerexception.cityeye.util.PermissionUtils
import java.io.File
import java.io.IOException


class CaptureFragment : Fragment() {

    private var binding: FragmentCaptureBinding? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentCaptureBinding.inflate(inflater, container, false)

        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.capture?.setOnClickListener {
            if (requestPermission()) {
                startCamera()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this.activity as AppCompatActivity) -> {
                when {
                    PermissionUtils.isLocationEnabled(this.activity as AppCompatActivity) -> {
                        setUpLocationListener(this.activity as AppCompatActivity)
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this.activity as AppCompatActivity)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this.activity as AppCompatActivity,
                    10
                )
            }
        }
    }

    private fun startCamera() {
        val photoFile: File? = try {
            CameraUtil.createImageFile(requireContext())
        } catch (ex: IOException) {
            null
        }

        photoFile?.also {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.nullpointerexception.cityeye.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            startProblemPreviewActivity(CameraUtil.retrieveImage())
        }

    }

    private fun requestPermission(): Boolean {

        val permissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, 100)
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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

    private fun setUpLocationListener(activity: AppCompatActivity) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        val locationRequest = LocationRequest().setInterval(500).setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionUtils.requestAccessFineLocationPermission(activity, 10)
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
            },
            Looper.myLooper()
        )
    }

    fun startProblemPreviewActivity(image: File) {
        val activity = Intent(requireContext(), ProblemPreview::class.java)
        activity.putExtra("image", image).putExtras(
            OtherUtilities().makeCoordinatesBundle(
                LatLng(latitude, longitude)
            )
        ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(activity)
    }


}