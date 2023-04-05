package com.nullpointerexception.cityeye.ui.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutListBinding
import com.nullpointerexception.cityeye.entities.Problem
import kotlin.random.Random

class RecyclerViewProblems(options: FirestoreRecyclerOptions<Problem>, val context:Context) :
    FirestoreRecyclerAdapter<Problem, RecyclerViewProblems.ProblemHolderView>(options) {

    var isSwitchView = true

    class ProblemHolderView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProblemLayoutListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemHolderView {

        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.problem_layout_list, parent, false)

        return ProblemHolderView(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ProblemHolderView, position: Int, model: Problem) {

        holder.binding.title.text = model.title
        holder.binding.location.text = model.address

        holder.binding.layout.setBackgroundColor(
            Color.argb(
                100,
                Random.nextInt(256),
                Random.nextInt(256),
                Random.nextInt(256)
            )
        )
        Firebase.storage.reference.child("images/${model.imageName}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url) {
                transformations(CircleCropTransformation())
            }

        }

        holder.binding.layout.animation =
            AnimationUtils.loadAnimation(context, R.anim.recycler_problem_animation)

    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }


}