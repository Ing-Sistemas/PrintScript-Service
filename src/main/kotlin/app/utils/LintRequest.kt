package com.example.springboot.app.utils

data class LintRequest (
    val snippetId: String,
    val rule: String,
    val userId: String
)