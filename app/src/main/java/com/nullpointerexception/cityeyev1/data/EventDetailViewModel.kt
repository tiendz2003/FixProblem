package com.nullpointerexception.cityeyev1.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nullpointerexception.cityeyev1.entities.Event

class EventDetailViewModel : ViewModel() {

    private val _event = MutableLiveData<Event>()
    var event: LiveData<Event> = _event

    fun getEvent(): MutableLiveData<Event> {
        return _event
    }

    fun setEvent(event: Event) {
        _event.value = event
    }

}