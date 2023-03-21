package com.nullpointerexception.cityeye.ui.adapters


import android.app.Person
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutBinding
import com.nullpointerexception.cityeye.entities.Problem
import kotlin.random.Random

class RecyclerViewProblems(options: FirestoreRecyclerOptions<Problem>) :
    FirestoreRecyclerAdapter<Problem, RecyclerViewProblems.MyViewHolder>(options) {



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProblemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.problem_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Problem) {
        holder.binding.title.text = model.title
        holder.binding.imageTitle.text = model.image
        holder.binding.user.text = model.uid

        holder.binding.layout.setBackgroundColor(Color.argb(100, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)))

        Firebase.storage.reference.child("images/${model.image}").downloadUrl.addOnSuccessListener {
            url -> holder.binding.image.load(url)
        }
    }

    // Optional: Override this method to add a click listener to your items
    override fun onDataChanged() {
        super.onDataChanged()
        // Implement your logic here
    }
}