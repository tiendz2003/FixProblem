package com.nullpointerexception.cityeyev1.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cityeyev1.databinding.FragmentPlacesBinding
import com.nullpointerexception.cityeyev1.data.SharedViewModel
import com.nullpointerexception.cityeyev1.ui.adapters.RecyclerViewPlacesAdapter

class PlacesFragment : Fragment() {

    private lateinit var binding: FragmentPlacesBinding
    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding = FragmentPlacesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadIndicator.show()

        viewModel.getNearbyPlaces(requireActivity(), viewModel.getMyCoordinates().value, 5000)

        viewModel.getPlaces().observe(viewLifecycleOwner) { places ->
            Handler(Looper.getMainLooper()).postDelayed({
                binding.loadIndicator.hide()
                val adapter = RecyclerViewPlacesAdapter(requireContext(), places.results.toList())
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.itemAnimator = null
            }, 50)
        }
    }

}