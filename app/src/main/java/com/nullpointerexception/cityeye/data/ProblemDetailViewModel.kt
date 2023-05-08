package com.nullpointerexception.cityeye.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointerexception.cityeye.entities.Answer
import com.nullpointerexception.cityeye.entities.Problem
import com.nullpointerexception.cityeye.firebase.FirebaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getThisProblem(problemID: String) {
        viewModelScope.launch {
            val problemResponse = async {
                FirebaseDatabase.getProblemById(problemID)
            }
            val answerResponse = async {
                FirebaseDatabase.getAnswerByID(problemID)
            }

            problemResponse.await()
            answerResponse.await()

            problemResponse.getCompleted()?.let { setProblem(it) }
            answerResponse.getCompleted()?.let { setAnswer(it) }
        }
    }

    private val _answer = MutableLiveData<Answer>()
    var answer: LiveData<Answer> = _answer

    fun setAnswer(answer: Answer) {
        _answer.value = answer
    }

    fun getAnswer(): MutableLiveData<Answer> {
        return _answer
    }

}