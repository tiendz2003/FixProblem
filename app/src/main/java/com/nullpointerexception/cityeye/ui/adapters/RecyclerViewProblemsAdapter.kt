package com.nullpointerexception.cityeye.ui.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.ProblemDetailActivity
import com.nullpointerexception.cityeye.ProfileActivity
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutListBinding
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.util.OtherUtilities
import kotlin.random.Random

class RecyclerViewProblemsAdapter(
    options: FirestoreRecyclerOptions<Problem>,
    val context: Context,
    val activity: Activity
) :
    FirestoreRecyclerAdapter<Problem, RecyclerViewProblemsAdapter.ProblemHolderView>(options) {

    var isSwitchView = true

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

        holder.binding.userNameCreator.text = model.userName
        holder.binding.title.text = model.title
        holder.binding.location.text = model.address
        holder.binding.dateTime.text = OtherUtilities().getTimeFromEpoch(model.epoch!!)

        Firebase.storage.reference.child("images/${model.imageName}").downloadUrl.addOnSuccessListener { url ->
            holder.binding.image.load(url)
        }

        holder.binding.layout.animation =
            AnimationUtils.loadAnimation(context, R.anim.recycler_problem_animation)



        holder.binding.details.setOnClickListener {
            startProfileActivity(holder.binding.image, model.problemID!!)
        }

    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    fun startProfileActivity(image: ImageView, problemID: String) {
        val intent = Intent(activity, ProblemDetailActivity::class.java)
        intent.putExtra("problemID", problemID)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            image,
            "image"
        )
        activity.startActivity(intent, options.toBundle())
    }


}