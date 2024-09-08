package com.nullpointerexception.cityeyev1.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointerexception.cityeyev1.entities.Problem
import com.nullpointerexception.cityeyev1.entities.User
import com.nullpointerexception.cityeyev1.firebase.FirebaseDatabase
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user

    private val _problems = MutableLiveData<List<Problem>>()
    var problems: LiveData<List<Problem>> = _problems

    fun setProblems(problems: List<Problem>) {
        _problems.value = problems
    }

    fun getProblems(): MutableLiveData<List<Problem>> {
        return _problems
    }

    fun setUser(user: User) {
        _user.value = user
    }

    fun getUser(): MutableLiveData<User> {
        return _user
    }

    fun getCurrentUser(uid: String) {
        viewModelScope.launch {
            val user = FirebaseDatabase.getUser(uid)
            if (user != null) {
                setUser(user)
            }
        }
    }

    fun getUserProblems(problems: List<String>) {
        viewModelScope.launch {
            val problems = FirebaseDatabase.getUserProblems(problems)
            setProblems(problems)
        }
    }


}