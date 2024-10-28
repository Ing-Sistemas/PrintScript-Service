package com.example.springboot.app.utils

data class ValidateRequest(
    val version: String,
    val snippetId: String //todo Change to snippet id in order to look it up in asset
)