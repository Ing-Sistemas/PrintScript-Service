package com.example.springboot.app.service

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.utils.ExecuteResult
import com.example.springboot.app.utils.ValidationResult
import org.springframework.stereotype.Service
import com.printscript.cli.logic.ValidateLogic
import com.printscript.interpreter.providers.DefaultEnvProvider
import com.printscript.interpreter.providers.DefaultInputProvider
import com.printscript.interpreter.providers.DefaultOutPutProvider
import com.printscript.interpreter.results.InterpreterFailure
import com.printscript.interpreter.results.InterpreterSuccess
import com.printscript.runner.Runner
import org.springframework.core.io.ClassPathResource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.nio.file.Files

@Service
class PrintScriptService(
    private val assetService: AssetService
) {

    fun validateSnippet(version: String, snippetId: String): ValidationResult {
        try {
            val snippet = fetchSnippet(snippetId)
            val result = ValidateLogic().validate(version, snippet.inputStream)
            return ValidationResult(result, null)//todo add result pattern matching maybe
        } catch (e: Exception) {
            println(e)
            return ValidationResult(null, e.message)
        }
    }

    fun executeSnippet(version: String, snippetId: String): ExecuteResult {
        try {
            val snippet = fetchSnippet(snippetId)
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

    fun fetchSnippet(snippetId: String): MultipartFile {
        val snippet = assetService.getSnippet(snippetId)
        return snippet.body!!
    }

    fun genFile(multipartFile: MultipartFile): File {
        val tempFile = Files.createTempFile(multipartFile.name, ".ps").toFile()
        multipartFile.transferTo(tempFile)
        return tempFile
    }

}