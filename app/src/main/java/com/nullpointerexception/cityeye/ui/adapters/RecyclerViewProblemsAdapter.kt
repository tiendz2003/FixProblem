package com.nullpointerexception.cityeye.ui.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.ProblemDetailActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.util.OtherUtilities

class RecyclerViewProblemsAdapter(
    options: FirestoreRecyclerOptions<Problem>,
    val context: Context,
    val activity: Activity
) :
    FirestoreRecyclerAdapter<Problem, RecyclerViewProblemsAdapter.ProblemHolderView>(options) {

    class ProblemHolderView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProblemLayoutListBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemHolderView {

        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.problem_layout_list, parent, false)

        return ProblemHolderView(view)
    }

    override fun onDataChanged() {
        super.onDataChanged()
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ProblemHolderView, position: Int, model: Problem) {

        holder.binding.loadIndicator.show()
        holder.binding.title.text = model.title
        holder.binding.address.text = model.address
        holder.binding.date.text = OtherUtilities().getDateFromEpoch(model.epoch!!)

        holder.binding.title.isSelected = true
        holder.binding.title.isSingleLine = true
        holder.binding.title.marqueeRepeatLimit = -1
        holder.binding.title.ellipsize = TextUtils.TruncateAt.MARQUEE

        Firebase.storage.reference.child("images/${model.imageName}").downloadUrl.addOnSuccessListener { url ->
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

        holder.binding.layout.animation =
            AnimationUtils.loadAnimation(context, R.anim.recycler_problem_animation)



        holder.binding.details.setOnClickListener {
            startProfileActivity(model.problemID!!)
        }

    }


    fun startProfileActivity(problemID: String) {
        val intent = Intent(activity, ProblemDetailActivity::class.java)
        intent.putExtra("problemID", problemID)
        activity.startActivity(intent)
    }
    
}