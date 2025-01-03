package com.example.springboot.app.controller.health

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api/com/health")
class TestCommunication(private val restTemplate: RestTemplate) {
    private val host = System.getenv().getOrDefault("HOST", "localhost")
    private val permissionPort = System.getenv().getOrDefault("PERMISSION_SERVICE_PORT", "none")
    private val snippetPort = System.getenv().getOrDefault("SNIPPET_SERVICE_PORT", "none")

    @GetMapping("/permission/ping")
    fun getPermissionData(): ResponseEntity<String> {
        val url = "http://$host:$permissionPort/api/health/ping"
        try {
            val response = restTemplate.getForObject(url, String::class.java) ?: "No response"
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            println(e.message)
            return ResponseEntity.status(500).body("Permission service is down")
        }
    }

    @GetMapping("/snippet/ping")
    fun getSnippetData(): ResponseEntity<String> {
        val url = "http://$host:$snippetPort/api/health/ping"
        try {
            val response = restTemplate.getForObject(url, String::class.java) ?: "No response"
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            println(e.message)
            return ResponseEntity.status(500).body("Snippet service is down")
        }
    }
}
