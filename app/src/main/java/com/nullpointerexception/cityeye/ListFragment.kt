package com.nullpointerexception.cityeye

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.nullpointerexception.cityeye.data.ListViewModel
import com.nullpointerexception.cityeye.databinding.FragmentListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.ui.adapters.ProblemViewHolder
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
        adapter = RecyclerViewProblems(options)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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