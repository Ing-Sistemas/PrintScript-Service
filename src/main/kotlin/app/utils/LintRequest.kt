package com.example.springboot.app.utils

data class LintRequest (
    val snippetId: String,
    val ruleId: String,
    val userId: String
)