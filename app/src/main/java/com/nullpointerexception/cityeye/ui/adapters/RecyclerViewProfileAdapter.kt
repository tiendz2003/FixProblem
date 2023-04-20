package com.nullpointerexception.cityeye.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.ProblemDetailActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProfileProblemItemBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.util.OtherUtilities

class RecyclerViewProfileAdapter(val context: Context, val problems: ArrayList<Problem>) :
    RecyclerView.Adapter<RecyclerViewProfileAdapter.ProblemViewHolder>() {

    class ProblemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ProfileProblemItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        return ProblemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.profile_problem_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val binding = holder.binding
        val problem = problems[position]

        binding.title.text = problem.title
        binding.description.text = problem.description
        binding.address.text = problem.address
        binding.time.text = OtherUtilities().getTimeFromEpoch(problem.epoch!!)

        Firebase.storage.reference.child("images/${problem.imageName}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url) {
                transformations(CircleCropTransformation())
            }
        }

        holder.binding.layout.setOnClickListener {
            val intent = Intent(context, ProblemDetailActivity::class.java)
            intent.putExtra("problemID", problem.problemID!!)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = problems.size

}