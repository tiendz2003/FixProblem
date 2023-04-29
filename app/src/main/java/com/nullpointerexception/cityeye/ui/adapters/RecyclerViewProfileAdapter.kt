package com.nullpointerexception.cityeye.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.ProblemDetailActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.util.OtherUtilities

class RecyclerViewProfileAdapter(val context: Context, val problems: ArrayList<Problem>) :
    RecyclerView.Adapter<RecyclerViewProfileAdapter.ProblemViewHolder>() {

    class ProblemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ProblemLayoutListBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        return ProblemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.problem_layout_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val binding = holder.binding
        val problem = problems[position]

        binding.title.text = problem.title
        binding.address.text = problem.address
        binding.date.text = OtherUtilities().getDateFromEpoch(problem.epoch!!)

        Firebase.storage.reference.child("images/${problem.imageName}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url)
        }

        holder.binding.details.setOnClickListener {
            val intent = Intent(context, ProblemDetailActivity::class.java)
            intent.putExtra("problemID", problem.problemID!!)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = problems.size

}