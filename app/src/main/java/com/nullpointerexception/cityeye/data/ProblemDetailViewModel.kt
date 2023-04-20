package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class ProblemDetailViewModel : ViewModel() {

    private val _problem = MutableLiveData<Problem>()
    var problem: LiveData<Problem> = _problem

    fun setProblem(problem: Problem) {
        _problem.value = problem
    }

    fun getProblem(): MutableLiveData<Problem> {
        return _problem
    }

    fun getThisProblem(problemID: String) {
        viewModelScope.launch {
            val response = FirebaseDatabase.getProblemById(problemID)
            if (response != null) {
                setProblem(response)
            }
        }
    }

}