package com.example.springboot.app.controller

import com.example.springboot.app.utils.ValidateRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.printscript.cli.logic.ValidateLogic
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/api")
class PrintScriptController {

    @RequestMapping("/validate")
    fun validateSnippet(
        @RequestBody validateRequest: ValidateRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        val logic = ValidateLogic()
    }
}