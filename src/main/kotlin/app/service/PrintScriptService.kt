package com.example.springboot.app.service

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.utils.ExecuteResult
import com.example.springboot.app.utils.RunTestDTO
import com.example.springboot.app.utils.RunnerEnvProv
import com.example.springboot.app.utils.RunnerInputProv
import com.example.springboot.app.utils.RunnerOutPutProv
import com.example.springboot.app.utils.ValidationResult
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.printscript.cli.logic.AnalyzeLogic
import com.printscript.cli.logic.FormatLogic
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
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Service
class PrintScriptService(
    private val assetService: AssetService,
) {
    private val logger = LoggerFactory.getLogger(PrintScriptService::class.java)

    fun validateSnippet(
        version: String,
        snippetId: String,
    ): ValidationResult {
        try {
            val snippet = fetchMultipartFile(snippetId)
            ValidateLogic().validate(version, snippet.inputStream)
            return ValidationResult("Snippet validated", null)
        } catch (e: Exception) {
            println(e)
            return ValidationResult(null, e.message)
        }
    }

    fun executeSnippet(
        version: String,
        snippetId: String,
    ): ExecuteResult {
        try {
            val snippet = fetchMultipartFile(snippetId)
            val defaultInputProvider = DefaultInputProvider()
            val defaultOutputProvider = DefaultOutPutProvider()
            val defaultEnvProvider = DefaultEnvProvider()

            val result =
                Runner(
                    defaultInputProvider,
                    defaultOutputProvider,
                    defaultEnvProvider,
                ).run(snippet.inputStream, version)
            return when (result) {
                is InterpreterSuccess -> {
                    ExecuteResult(result.getOriginalValue().toString(), null)
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

    fun executeSnippetTest(
        version: String,
        sId: String,
        testCase: RunTestDTO,
    ): ExecuteResult {
        return try {
            val snippet = fetchMultipartFile(sId)
            val inputProvider = RunnerInputProv(testCase.input)
            val outputProvider = RunnerOutPutProv()
            val envProvider = RunnerEnvProv()

            val runner = Runner(inputProvider, outputProvider, envProvider)

            val result = runner.run(snippet.inputStream, version)

            when (result) {
                is InterpreterSuccess -> {
                    ExecuteResult(result.getOriginalValue().toString(), null)
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

    fun lintSnippet(
        snippetId: String,
        configJson: JsonNode,
    ): List<String> {
        try {
            val snippet = fetchMultipartFile(snippetId)
            val result = AnalyzeLogic().analyse("1.1", snippet.inputStream, toJsonFile(toJsonMap(configJson)))
            return result
        } catch (e: Exception) {
            logger.error("Error linting snippet: {}", e.message)
            return emptyList()
        }
    }

    fun formatSnippet(
        snippetId: String,
        config: JsonNode,
    ) {
        try {
            val formatConfig = genConfig(toJsonMap(config))
            val snippet = genFile(fetchMultipartFile(snippetId), "ps")
            println(String(snippet.readBytes()))
            FormatLogic().format("1.1", snippet, formatConfig)
            assetService.saveSnippet(snippetId, genMultiPartFile(snippet))
        } catch (e: Exception) {
            logger.error("Error formatting snippet: {}", e.message)
        }
    }

    fun toJsonMap(json: JsonNode): Map<String, String> {
        return json.filter { it["isActive"].asBoolean() }
            .associate { it["name"].asText() to it["value"].asText() }
    }

    fun toJsonFile(map: Map<String, String>): File {
        val tempFile = Files.createTempFile("config", ".json").toFile()
        tempFile.writeText(ObjectMapper().writeValueAsString(map))
        logger.trace("Config file: {}", tempFile.readText())
        return tempFile
    }

    fun genConfig(map: Map<String, String>): FormatterConfig {
        // abomination v2
        return FormatterConfig(
            spaceBeforeColon = map["spaceBeforeColon"]?.toBoolean(),
            spaceAfterColon = map["spaceAfterColon"]?.toBoolean(),
            spaceAroundEquals = map["spaceAroundEquals"]?.toBoolean(),
            lineJumpBeforePrintln = map["lineJumpBeforePrintln"]?.toIntOrNull() ?: 1,
            lineJumpAfterSemicolon = map["lineJumpAfterSemicolon"]?.toBoolean() ?: true,
            singleSpaceBetweenTokens = map["singleSpaceBetweenTokens"]?.toBoolean() ?: true,
            spaceAroundOperators = map["spaceAroundOperators"]?.toBoolean() ?: true,
        )
    }

    fun fetchMultipartFile(snippetId: String): MultipartFile {
        val snippet = assetService.getSnippet(snippetId)
        return snippet.body!!
    }

    fun genFile(
        multipartFile: MultipartFile,
        suffix: String,
    ): File {
        val tempFile = Files.createTempFile(multipartFile.name, suffix).toFile()
        multipartFile.transferTo(tempFile)
        return tempFile
    }

    fun genMultiPartFile(file: File): MultipartFile {
        return MockMultipartFile(file.name, file.name, Files.probeContentType(file.toPath()), file.readBytes())
    }
}
