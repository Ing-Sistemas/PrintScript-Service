package com.example.springboot.app.utils

import com.fasterxml.jackson.databind.JsonNode

data class FormatRequest(
    val snippetId: String,
    val config: JsonNode,
)
