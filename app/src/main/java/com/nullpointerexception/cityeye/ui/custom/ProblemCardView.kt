package com.nullpointerexception.cityeye.ui.custom

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.nullpointerexception.cityeye.R
import com.nullpointerexception.cityeye.databinding.ProblemLayoutListBinding

class ProblemCardView(context:Context) : MaterialCardView(context) {

    private lateinit var binding:ProblemLayoutListBinding

    init{
        inflate(context, R.layout.problem_layout_list, null)
        binding = ProblemLayoutListBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
    }

}