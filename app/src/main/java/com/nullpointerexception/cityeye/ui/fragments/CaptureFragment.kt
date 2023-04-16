package com.nullpointerexception.cityeye.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.data.CaptureViewModel
import com.nullpointerexception.cityeye.databinding.FragmentCaptureBinding
import com.nullpointerexception.cityeye.util.CameraUtil
import com.nullpointerexception.cityeye.util.OtherUtilities
import com.nullpointerexception.cityeye.util.PermissionUtils
import java.io.File
import java.io.IOException


class CaptureFragment : Fragment() {

    private lateinit var binding: FragmentCaptureBinding
    private lateinit var viewModel: CaptureViewModel
    private val REQUEST_IMAGE_CAPTURE = 1
    private var hasZoomed = false
    private lateinit var myMap: SupportMapFragment


    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        val uiSettings = googleMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = true
        uiSettings.isCompassEnabled = true

        googleMap.isMyLocationEnabled = true

        val myLocation =
            LatLng(
                viewModel.myCoordinates.value!!.latitude,
                viewModel.myCoordinates.value!!.longitude
            )
        googleMap.clear()

        if (!hasZoomed) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    myLocation, 17.0f
                )
            )
            hasZoomed = true
        }
        viewModel.getCoordinates().observe(viewLifecycleOwner) {
            makeHeatmap(googleMap)
        }
        binding.capture.visibility = View.VISIBLE
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCaptureBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[CaptureViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.capture.setOnClickListener {
            if (PermissionUtils.requestPermission(requireActivity())) {
                startCamera()
            }
        }

        viewModel.getMyCoordinates().observe(viewLifecycleOwner) {
            myMap = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
            myMap.getMapAsync(callback)
        }

        viewModel.loadProblemCoordinates()


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
            if (viewModel.myCoordinates.value != null) {
                OtherUtilities().startProblemPreviewActivity(
                    CameraUtil.retrieveImage(),
                    requireActivity(),
                    viewModel.myCoordinates.value!!.latitude,
                    viewModel.myCoordinates.value!!.longitude
                )
            }
        }

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
                    Log.i("PERMISSION", "All good")
                } else {

                    Log.i("TAG", "LMAO")
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener(activity: AppCompatActivity) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        val locationRequest = LocationRequest().setInterval(1).setFastestInterval(5)
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
                        viewModel.setMyCoordinates(LatLng(location.latitude, location.longitude))
                    }
                }
            },
            Looper.myLooper()
        )
    }

    private fun makeHeatmap(googleMap: GoogleMap) {
        val heatmapProvider = HeatmapTileProvider.Builder()
            .data(viewModel.getCoordinates().value)
            .radius(40)
            .build()

        val tileOverlayOptions = TileOverlayOptions().tileProvider(heatmapProvider).fadeIn(true)
        googleMap.addTileOverlay(tileOverlayOptions)


    }
}