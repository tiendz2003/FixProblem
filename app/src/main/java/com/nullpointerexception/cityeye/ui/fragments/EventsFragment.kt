package com.nullpointerexception.cityeye.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nullpointerexception.cityeye.data.SharedViewModel
import com.nullpointerexception.cityeye.databinding.FragmentEventsBinding
import com.nullpointerexception.cityeye.ui.adapters.RecyclerViewEvents


class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding = FragmentEventsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getAllEvents()

        viewModel.getEvents().observe(viewLifecycleOwner) {
            binding.pullToRefresh.isRefreshing = false

            val adapter = RecyclerViewEvents(requireActivity(), requireActivity(), it)

            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.itemAnimator = null
        }

        binding.pullToRefresh.setOnRefreshListener {
            viewModel.getAllEvents()
        }

    }
}