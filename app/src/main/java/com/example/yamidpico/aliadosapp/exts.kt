package com.example.yamidpico.aliadosapp

import java.util.*

fun getCalendar(date: Date): Calendar {
    val cal = Calendar.getInstance(Locale.US)
    cal.time = date
    return cal
}

fun Date.getAge(): Int {
    return this.getDiffYears(Date())
}

fun Date.getDiffYears(last: Date): Int {
    val a = getCalendar(this)
    val b = getCalendar(last)
    var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
    if (a.get(Calendar.DAY_OF_YEAR) > b.get(Calendar.DAY_OF_YEAR)) {
        diff--
    }
    return diff
}