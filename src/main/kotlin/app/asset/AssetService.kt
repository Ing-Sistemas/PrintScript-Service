package com.example.springboot.app.asset

import com.example.springboot.app.controller.PrintScriptController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient

@Component
class AssetService @Autowired constructor(
    @Value("\${spring.constants.asset_url}") private val assetURL: String
){

    private val logger = LoggerFactory.getLogger(AssetService::class.java)
    private val client: WebClient = WebClient.builder()
        .baseUrl(assetURL)
        .build()

    fun saveSnippet(snippetId: String, snippetFile: MultipartFile): ResponseEntity<String> {
        val container = "test-container" //CHANGE
        return try {

            val response = client.put()
                .uri("/{container}/{snippetId}", container, snippetId)
                .header("accept", "*/*")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(snippetFile.bytes)
                .retrieve()
                .toEntity(String::class.java)
                .block()!!

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
                .uri("/{container}/{snippetId}", container,snippetId)
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