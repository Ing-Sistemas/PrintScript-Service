package com.example.springboot.app.utils

import com.printscript.ast.ASTNode

data class ValidationResult(
    val ast: ASTNode?,
    val error: String?
)