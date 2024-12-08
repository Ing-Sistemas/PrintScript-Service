package com.example.springboot.app.utils

import com.printscript.formatter.config.FormatterConfig

data class FormatRequest(
    val snippetId: String,
    val config: FormatterConfig
)