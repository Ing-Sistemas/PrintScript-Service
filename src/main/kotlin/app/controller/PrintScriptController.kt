package com.example.springboot.app.controller

import com.example.springboot.app.service.PrintScriptService
import com.example.springboot.app.utils.ValidateRequest
import com.example.springboot.app.utils.ValidateResponse
import com.printscript.ast.ASTNode
import org.springframework.http.ResponseEntity

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class PrintScriptController(
    private val printScriptService: PrintScriptService,
) {

    @PostMapping("/validate")
    fun validateSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ValidateResponse> {
        return try {
            val result = printScriptService.validateSnippet(validateRequest.version, validateRequest.snippetId)
            println(result)
            if(result.error != null){
                ResponseEntity.status(400).body(ValidateResponse(null,result.error))
            } else {
                ResponseEntity.ok(ValidateResponse("Snippet is valid!", null))
            }
        } catch (e: Exception) {
            println(e.message)
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
            println(e.message)
            ResponseEntity.status(500).body(null)
        }
    }

    @PostMapping("/lint")
    fun lintSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
       //TODO  async fun, this will send event to redis
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        //TODO  async fun, this will send event to redis
    }

    @GetMapping("/fetch/{snippetId}")
    fun fetchSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        return try {
            val result = printScriptService.fetchSnippet(snippetId)
            //val file = printScriptService.genFile(result)
            ResponseEntity.ok(String(result.resource.contentAsByteArray))
        } catch (e: Exception) {
            println(e.message)
            ResponseEntity.status(500).body(null)
        }
    }
}