package com.nullpointerexception.cityeye

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.nullpointerexception.cityeye.data.ListViewModel
import com.nullpointerexception.cityeye.databinding.FragmentListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.ui.adapters.RecyclerViewProblems


class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel:ListViewModel
    private lateinit var adapter:RecyclerViewProblems

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentListBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]

        val query = FirebaseFirestore.getInstance().collection("problems")
        val options: FirestoreRecyclerOptions<Problem> = FirestoreRecyclerOptions.Builder<Problem>()
            .setQuery(query, Problem::class.java)
            .build()
        adapter = RecyclerViewProblems(options, requireContext())

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.itemAnimator = null

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutSwitch.setOnClickListener{
            binding.recyclerView.layoutManager = if(adapter.toggleItemViewType()) LinearLayoutManager(requireContext()) else GridLayoutManager(requireContext(), 2)

        }

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}