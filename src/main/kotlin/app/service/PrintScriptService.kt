package com.example.springboot.app.service

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.controller.PrintScriptController
import com.example.springboot.app.utils.ConfigReader
import com.example.springboot.app.utils.ExecuteResult
import com.example.springboot.app.utils.ValidationResult
import com.printscript.cli.logic.AnalyzeLogic
import com.printscript.cli.logic.FormatLogic
import org.springframework.stereotype.Service
import com.printscript.cli.logic.ValidateLogic
import com.printscript.formatter.config.FormatterConfig
import com.printscript.interpreter.providers.DefaultEnvProvider
import com.printscript.interpreter.providers.DefaultInputProvider
import com.printscript.interpreter.providers.DefaultOutPutProvider
import com.printscript.interpreter.results.InterpreterFailure
import com.printscript.interpreter.results.InterpreterSuccess
import com.printscript.runner.Runner
import org.slf4j.LoggerFactory
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Service
class PrintScriptService(
    private val assetService: AssetService
) {
    private val logger = LoggerFactory.getLogger(PrintScriptService::class.java)
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

    fun lintSnippet(snippetId: String, configId: String): List<String> {
        try {
            val snippet = fetchMultipartFile(snippetId)
            val config = genFile(fetchMultipartFile(configId), "json")
            logger.info("Linting snippet with config: $config")
            return AnalyzeLogic().analyse("1.1", snippet.inputStream, config)
        } catch (e: Exception) {
            logger.error("Error linting snippet: {}", e.message)
            return emptyList()
        }
    }

    fun formatSnippet(snippetId: String, config: FormatterConfig) {
        try {
            val snippet = genFile(fetchMultipartFile(snippetId), "ps")
            logger.info("Formatting snippet with config: $config")
            println(String(snippet.readBytes()))
            FormatLogic().format("1.1", snippet, config)
            logger.info("Snippet formatted successfully")
            assetService.saveSnippet(snippetId, genMultiPartFile(snippet))
        } catch (e: Exception) {
            logger.error("Error formatting snippet: {}", e.message)
        }

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