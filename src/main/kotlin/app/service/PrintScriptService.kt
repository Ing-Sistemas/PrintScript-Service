package com.example.springboot.app.service

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.utils.*
import com.printscript.cli.logic.AnalyzeLogic
import com.printscript.cli.logic.FormatLogic
import org.springframework.stereotype.Service
import com.printscript.cli.logic.ValidateLogic
import com.printscript.interpreter.providers.DefaultEnvProvider
import com.printscript.interpreter.providers.DefaultInputProvider
import com.printscript.interpreter.providers.DefaultOutPutProvider
import com.printscript.interpreter.results.InterpreterFailure
import com.printscript.interpreter.results.InterpreterSuccess
import com.printscript.runner.Runner
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Service
class PrintScriptService(
    private val assetService: AssetService
) {

    fun validateSnippet(version: String, snippetId: String): ValidationResult {
        try {
            val snippet = fetchMultipartFile(snippetId)
            val result = ValidateLogic().validate(version, snippet.inputStream)
            return ValidationResult(result, null)//todo add result pattern matching maybe
        } catch (e: Exception) {
            println(e)
            return ValidationResult(null, e.message)
        }
    }

    fun executeSnippet(version: String, snippetId: String): ExecuteResult {
        try {
            val snippet = fetchMultipartFile(snippetId)
            val defaultInputProvider = DefaultInputProvider()
            val defaultOutputProvider = DefaultOutPutProvider()
            val defaultEnvProvider = DefaultEnvProvider()

            val result = Runner(
                defaultInputProvider,
                defaultOutputProvider,
                defaultEnvProvider
                ).run(snippet.inputStream, version)
            return when (result) {
                is InterpreterSuccess -> {
                    ExecuteResult(result.getOriginalValue().toString(),null)
                }
                is InterpreterFailure -> {
                    ExecuteResult(null, result.getErrorMessage())
                }
                else -> {
                    ExecuteResult(null, null)
                }
            }
        } catch (e: Exception) {
            println(e)
            return ExecuteResult(null, e.message)
        }
    }

    fun executeSnippetTest(version: String, sId: String, testCase: RunTestDTO): ExecuteResult {
        return try {
            val snippet = fetchMultipartFile(sId)
            val inputProvider = DefaultInputProvider()
            val outputProvider = DefaultOutPutProvider()
            val envProvider = DefaultEnvProvider()

            testCase.input?.forEach { inputProvider.readInput(it) }

            val result = Runner(inputProvider, outputProvider, envProvider).run(snippet.inputStream, version)

            when (result) {
                is InterpreterSuccess -> {
                    val actualOutput = result.getOriginalValue().toString()
                    if (testCase.output?.any { it == actualOutput } == true) {
                        ExecuteResult("success", null)
                    } else {
                        ExecuteResult("fail", null)
                    }
                }
                is InterpreterFailure -> {
                    ExecuteResult(null, result.getErrorMessage())
                }
                else -> {
                    ExecuteResult(null, "Unexpected runner result")
                }
            }
        } catch (e: Exception) {
            println("Error executing snippet test: ${e.message}")
            ExecuteResult(null, e.message)
        }
    }


    fun lintSnippet(snippetId: String, configId: String): List<String> {
        val snippet = fetchMultipartFile(snippetId)
        val config = genFile(fetchMultipartFile(configId), "json")
        return AnalyzeLogic().analyse("1.1", snippet.inputStream, config)
    }

    fun formatSnippet(snippetId: String, configId: String) {
        val snippet = genFile(fetchMultipartFile(snippetId), "ps")
        val config = ConfigReader().readConfig(genFile(fetchMultipartFile(configId), "json"))
        FormatLogic().format("1.1", snippet, config)
        assetService.saveSnippet(snippetId, genMultiPartFile(snippet))
    }

    fun fetchMultipartFile(snippetId: String): MultipartFile {
        val snippet = assetService.getSnippet(snippetId)
        return snippet.body!!
    }

    fun genFile(multipartFile: MultipartFile, suffix : String): File {
        val tempFile = Files.createTempFile(multipartFile.name, suffix).toFile()
        multipartFile.transferTo(tempFile)
        return tempFile
    }

    fun genMultiPartFile(file: File): MultipartFile {
        return MockMultipartFile(file.name, file.name, Files.probeContentType(file.toPath()), file.readBytes())
    }

}