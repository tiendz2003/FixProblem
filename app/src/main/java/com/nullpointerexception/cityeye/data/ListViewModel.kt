package com.nullpointerexception.cityeye.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.firebase.Database

class ListViewModel : ViewModel() {

    private val _problems = MutableLiveData<List<Problem>>()
    var problems : LiveData<List<Problem>> = _problems

    fun addProblem(newProblem: Problem){
        val list = problems.value?.toMutableList() ?: mutableListOf()
        list.add(newProblem)
        _problems.value = list
    }

    fun getProblems(): List<Problem>? {
        return _problems.value
    }

}