package com.nullpointerexception.cityeyev1.ui.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cityeyev1.databinding.FragmentListBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nullpointerexception.cityeyev1.data.ProblemPreviewViewModel
import com.nullpointerexception.cityeyev1.entities.Problem
import com.nullpointerexception.cityeyev1.ui.adapters.RecyclerViewProblemsAdapter


class ListFragment : Fragment() {
    private var recyclerViewState: Parcelable? = null
    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: ProblemPreviewViewModel
    private lateinit var adapter: RecyclerViewProblemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentListBinding.inflate(inflater, container, false)
        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable("recyclerViewState")
        }
        viewModel = ViewModelProvider(requireActivity())[ProblemPreviewViewModel::class.java]
        viewModel.loadProblems()

        val query = FirebaseFirestore.getInstance().collection("problems")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<Problem> = FirestoreRecyclerOptions.Builder<Problem>()
            .setQuery(query, Problem::class.java)
            .build()
        adapter = RecyclerViewProblemsAdapter(options, requireContext(), requireActivity())


        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.itemAnimator = null
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }
    override fun onPause() {
        super.onPause()

    }
    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("recyclerViewState", binding.recyclerView.layoutManager?.onSaveInstanceState())
    }

}