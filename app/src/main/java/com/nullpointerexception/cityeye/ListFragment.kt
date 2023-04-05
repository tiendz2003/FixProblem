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
import com.nullpointerexception.cityeye.data.ProblemPreviewViewModel
import com.nullpointerexception.cityeye.databinding.FragmentListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.ui.adapters.RecyclerViewProblemsAdapter


class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ProblemPreviewViewModel
    private lateinit var adapter: RecyclerViewProblemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentListBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[ProblemPreviewViewModel::class.java]

        val query = FirebaseFirestore.getInstance().collection("problems")
        val options: FirestoreRecyclerOptions<Problem> = FirestoreRecyclerOptions.Builder<Problem>()
            .setQuery(query, Problem::class.java)
            .build()
        adapter = RecyclerViewProblemsAdapter(options, requireContext())

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