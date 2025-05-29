package com.example.healthmanagecenter.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimeListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromTimeList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTimeList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
} 