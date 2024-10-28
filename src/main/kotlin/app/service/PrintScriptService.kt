package com.example.springboot.app.service

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
import java.io.InputStream

@Service
class PrintScriptService {

    fun validateSnippet(version: String, snippetId: String): ValidationResult {
        try {
            val snippet = fetchSnippet(snippetId)
            val result = ValidateLogic().validate(version, snippet)
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
                ).run(snippet, version)
            //todo change this abomination, how? idk man...
            return when (result) {
                is InterpreterSuccess -> {
                    //what the dog doin'?
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

    fun fetchSnippet(snippetId: String): InputStream {
        val mockFile = "valid-mock-file.ps"
        //todo adapt asset service here
        //this fun is mocked atm, it should fetch the snippet from the asset service and return it
        //todo, see if the asset service returns files or snippetId streams (? idk kow)
        val resource = ClassPathResource(mockFile)
        return resource.inputStream
    }

}