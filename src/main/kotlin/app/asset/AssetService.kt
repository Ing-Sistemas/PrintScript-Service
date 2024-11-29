package com.example.springboot.app.asset

import com.example.springboot.app.controller.PrintScriptController
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient

@Component
class AssetService {
    private val logger = LoggerFactory.getLogger(PrintScriptController::class.java)
    private val client: WebClient = WebClient.builder()
        .baseUrl("http://host.docker.internal:8083")
        .build()

    fun saveSnippet(snippetId: String, snippetFile: MultipartFile): ResponseEntity<String> {
        logger.info("Saving snippet with id: $snippetId")
        val container = "test-container" //CHANGE
        return try {

            val response = client.put()
                .uri("/v1/asset/{container}/{snippetId}", container, snippetId)
                .header("accept", "*/*")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(snippetFile.bytes)
                .retrieve()
                .toEntity(String::class.java)
                .block()!!

            logger.info("Response: $response")
            response
        } catch (e: Exception) {
            logger.error("Failed to save snippet: ${e.message}")
            ResponseEntity.status(500).body("Failed to save snippet: ${e.message}")
        }
    }

    fun getSnippet(snippetId: String): ResponseEntity<MultipartFile> {
        val container = "test-container"//CHANGE
        return try {
            val response = client.get()
                .uri("/v1/asset/{container}/{snippetId}", container,snippetId)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(ByteArray::class.java)
                .block()!!

            val multipartFile = MockMultipartFile(
                snippetId,
                snippetId,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                response
            )

            ResponseEntity.ok(multipartFile)
        } catch (e: Exception) {
            logger.error("Failed to retrieve snippet: ${e.message}")
            ResponseEntity.status(500).body(null)
        }
    }
}