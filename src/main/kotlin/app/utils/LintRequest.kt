package com.example.springboot.app.utils

import com.fasterxml.jackson.databind.JsonNode

data class LintRequest (
    val snippetId: String,
    val rules: JsonNode,
)