package com.example.springboot.app.utils

import com.google.gson.Gson
import com.printscript.formatter.config.FormatterConfig
import java.io.File

class ConfigReader {
    fun readConfig(file: File): FormatterConfig {
        val gson = Gson()
        val jsonString = file.readText()
        return gson.fromJson(jsonString, FormatterConfig::class.java)
    }
}