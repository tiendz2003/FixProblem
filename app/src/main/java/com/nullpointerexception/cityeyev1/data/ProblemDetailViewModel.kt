package com.nullpointerexception.cityeyev1.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointerexception.cityeyev1.entities.Answer
import com.nullpointerexception.cityeyev1.entities.Problem
import com.nullpointerexception.cityeyev1.firebase.FirebaseDatabase
import kotlinx.coroutines.launch

class ProblemDetailViewModel : ViewModel() {

    private val _problem = MutableLiveData<Problem>()
    var problem: LiveData<Problem> = _problem

    fun setProblem(problem: Problem) {
        _problem.value = problem
    }

    fun getProblem(): MutableLiveData<Problem> {
        return _problem
    }


    fun getProblem(problemID: String) {
        viewModelScope.launch {
            val problemResponse = FirebaseDatabase.getProblemById(problemID)
            if (problemResponse != null) {
                setProblem(problemResponse)
            }
        }
    }

    fun getAnswer(problemID: String) {
        viewModelScope.launch {
            val answerResponse = FirebaseDatabase.getAnswerByID(problemID)
            if (answerResponse != null) {
                setAnswer(answerResponse)
            } else {
                setAnswer(null)
            }
        }
    }

    private val _answer = MutableLiveData<Answer?>()

    fun setAnswer(answer: Answer?) {
        _answer.value = answer
    }

    fun getAnswer(): MutableLiveData<Answer?> {
        return _answer
    }

}