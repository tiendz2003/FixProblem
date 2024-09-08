package com.nullpointerexception.cityeyev1.util

import com.nullpointerexception.cityeyev1.entities.UserNotification
import java.text.SimpleDateFormat
import java.util.Locale

class DateComparator : Comparator<UserNotification> {
    override fun compare(item1: UserNotification, item2: UserNotification): Int {
        val dateFormat = SimpleDateFormat("M/d/yyyy, HH:mm:ss", Locale.getDefault())
        val date1 = item1.time?.let { dateFormat.parse(it) }
        val date2 = item2.time?.let { dateFormat.parse(it) }
        return date2?.compareTo(date1)!!
    }
}
