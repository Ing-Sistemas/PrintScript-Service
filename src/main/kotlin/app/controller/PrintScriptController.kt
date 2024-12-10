package com.example.springboot.app.controller

import com.example.springboot.app.service.PrintScriptService
import com.example.springboot.app.utils.*
import org.springframework.http.ResponseEntity
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class PrintScriptController(
    private val printScriptService: PrintScriptService,
) {
    private val logger = LoggerFactory.getLogger(PrintScriptController::class.java)
    @PostMapping("/validate")
    fun validateSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ValidateResponse> {
        return try {
            logger.trace("Validating snippet with id: ${validateRequest.snippetId}")
            val result = printScriptService.validateSnippet(validateRequest.version, validateRequest.snippetId)
            if(result.error != null){
                ResponseEntity.status(400).body(ValidateResponse(null,result.error))
            } else {
                ResponseEntity.ok(ValidateResponse("Snippet is valid!", null))
            }
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/execute")
    fun executeSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ValidateResponse> {
        return try {
            logger.trace("Executing snippet with id: ${validateRequest.snippetId}")
            val result = printScriptService.executeSnippet(validateRequest.version, validateRequest.snippetId)
            if(result.error != null){
                ResponseEntity.status(400).body(ValidateResponse(null,result.error))
            } else {
                ResponseEntity.ok(ValidateResponse(result.output, null))
            }
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/lint")
    fun lintSnippet(
        @RequestBody lintRequest: LintRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<String>> {
        return try {
            logger.trace("Linting snippet with id: ${lintRequest.snippetId}")
            val result = printScriptService.lintSnippet(lintRequest.snippetId,lintRequest.snippetId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody formatRequest: FormatRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            logger.trace("Formatting snippet with id: ${formatRequest.snippetId}")
            printScriptService.formatSnippet(formatRequest.snippetId, formatRequest.config)
            ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("Snippet formatted successfully")
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(e.message!!)
        }
    }

    @GetMapping("/fetch/{snippetId}")
    fun fetchSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            logger.trace("Fetching snippet with id: $snippetId")
            val result = printScriptService.fetchMultipartFile(snippetId)
            ResponseEntity.ok(String(result.resource.contentAsByteArray))
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }
}