package com.example.springboot.app.utils

import com.printscript.interpreter.interfaces.InputProvider

class RunnerInputProv (
    private val inputs: List<String>
) : InputProvider{

    private var index: Int = 0

    override fun readInput(name: String): String? {
        val res = inputs[index]
        this.index += 1
        return res
    }
}