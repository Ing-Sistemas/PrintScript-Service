package com.example.springboot.app.controller

import com.example.springboot.app.asset.AssetService
import com.example.springboot.app.service.PrintScriptService
import com.example.springboot.app.utils.*
import com.printscript.ast.ASTNode
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
            printScriptService.formatSnippet(formatRequest.snippetId, formatRequest.ruleId)
            ResponseEntity.ok("Snippet formatted successfully")
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @GetMapping("/fetch/{snippetId}")
    fun fetchSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val result = printScriptService.fetchMultipartFile(snippetId)
            ResponseEntity.ok(String(result.resource.contentAsByteArray))
        } catch (e: Exception) {
            logger.error(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/test/run_tests/{sId}")
    fun runTests(
        @PathVariable sId: String,
        @RequestBody runTestDTO: RunTestDTO,
        @AuthenticationPrincipal jwt: Jwt
    ) : ResponseEntity<String> {
        return try {
            val result = printScriptService.executeSnippetTest(
                "1.1", sId, runTestDTO
            )
            logger.info(result.error)
            logger.info(result.output)
            if(result.error != null){
                logger.info(result.error)
                logger.error("Fails in PS controller runTests: ${result.error}")
                ResponseEntity.status(400).contentType(MediaType.TEXT_PLAIN).body("fail: ${result.error}")
            } else {
                ResponseEntity.ok(ValidateResponse(result.output, null).message)
            }
        } catch (e: Exception) {
            logger.error("Error at run test method in ps contr: ${ e.message }")
            ResponseEntity.status(500).body(null + "Error in ps: ${e.message}")
        }
    }
}