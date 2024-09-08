package com.nullpointerexception.cityeyev1.ui.custom

import android.content.Context
import android.view.LayoutInflater
import cityeyev1.R
import cityeyev1.databinding.ProblemLayoutListBinding
import com.google.android.material.card.MaterialCardView


class ProblemCardView(context:Context) : MaterialCardView(context) {

    private lateinit var binding: ProblemLayoutListBinding

    init{
        inflate(context, R.layout.problem_layout_list, null)
        binding = ProblemLayoutListBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
    }

}