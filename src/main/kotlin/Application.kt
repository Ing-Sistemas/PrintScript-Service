package com.example.springboot.app
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrintScriptServiceApplication

fun main(args: Array<String>) {
	runApplication<PrintScriptServiceApplication>(*args)
}

