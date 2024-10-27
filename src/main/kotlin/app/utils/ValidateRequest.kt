package com.example.springboot.app.utils

import java.io.InputStream

data class ValidateRequest(
    val version: String,
    val input: String //todo Change to snippet id in order to look it up in asset
)