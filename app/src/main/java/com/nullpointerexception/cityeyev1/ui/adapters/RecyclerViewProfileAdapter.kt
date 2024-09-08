package com.nullpointerexception.cityeyev1.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cityeyev1.R
import cityeyev1.databinding.ProblemLayoutListBinding
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeyev1.ProblemDetailActivity
import com.nullpointerexception.cityeyev1.entities.Problem
import com.nullpointerexception.cityeyev1.util.OtherUtilities

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

        holder.binding.loadIndicator.show()
        binding.title.text = problem.title
        binding.address.text = problem.address
        binding.date.text = OtherUtilities().getDateFromEpoch(problem.epoch!!)

        Firebase.storage.reference.child("images/${problem.imageName}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url)

            val request = ImageRequest.Builder(context)
                .data(url)
                .target { drawable ->
                    holder.binding.loadIndicator.hide()
                    holder.binding.image.setImageDrawable(drawable)
                    holder.binding.image.visibility = View.VISIBLE
                }
                .build()
            Coil.imageLoader(context).enqueue(request)
        }

        holder.binding.details.setOnClickListener {
            val intent = Intent(context, ProblemDetailActivity::class.java)
            intent.putExtra("problemID", problem.problemID!!)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = problems.size

}