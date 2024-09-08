    package com.nullpointerexception.cityeyev1.ui.adapters


    import android.annotation.SuppressLint
    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.graphics.drawable.Drawable
    import android.text.TextUtils
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.view.animation.AnimationUtils
    import androidx.recyclerview.widget.RecyclerView
    import cityeyev1.R
    import cityeyev1.databinding.ProblemLayoutListBinding
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.DataSource
    import com.bumptech.glide.load.engine.DiskCacheStrategy
    import com.bumptech.glide.load.engine.GlideException
    import com.bumptech.glide.request.RequestListener
    import com.bumptech.glide.request.RequestOptions
    import com.firebase.ui.firestore.FirestoreRecyclerAdapter
    import com.firebase.ui.firestore.FirestoreRecyclerOptions
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.ktx.storage
    import com.nullpointerexception.cityeyev1.ProblemDetailActivity
    import com.nullpointerexception.cityeyev1.entities.Problem
    import com.nullpointerexception.cityeyev1.util.OtherUtilities
    import com.bumptech.glide.request.target.Target

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
                Glide.with(context)
                    .load(url)
                    .apply(
                        RequestOptions()
                        .placeholder(R.drawable.landscape_placeholder)
                        .error(com.firebase.ui.auth.R.drawable.mtrl_ic_error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            holder.binding.loadIndicator.hide()
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            holder.binding.loadIndicator.hide()
                            holder.binding.image.visibility = View.VISIBLE
                            return false
                        }
                    })
                    .into(holder.binding.image)
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