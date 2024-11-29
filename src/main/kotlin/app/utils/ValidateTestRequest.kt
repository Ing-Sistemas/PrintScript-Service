package com.example.springboot.app.utils

import org.jetbrains.annotations.NotNull

data class ValidateTestRequest (
    val testCaseDTO: TestCaseDTO,
    val snippetId: String
)

data class TestCaseDTO (
    val id: String,
    val name: String,
    val input: List<String>,
    val output: List<String>,
    val status: TestStatus?,
)

data class SnippetEntity(
    val id: String,
    @NotNull val title: String,
    @NotNull val extension: String,
    @NotNull val language: String,
    @NotNull val version: String
)
data class SnippetTest(
    val id: String,
    val status: TestStatus = TestStatus. PENDING,
    val testCase: TestCase? = null
)
enum class TestStatus {
    PENDING,
    SUCCESS,
    FAIL
}
data class TestCase(
    val id: String,
    val name: String,
    val input: List<String> = listOf(),
    val output: List<String> = listOf(),
    val snippet: SnippetEntity? = null,
    val snippetTests: List<SnippetTest> = listOf(),
)