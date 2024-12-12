package com.example.springboot.app.utils

import com.printscript.interpreter.interfaces.OutPutProvider

class RunnerOutPutProv : OutPutProvider {
    val outPuts: MutableList<String> = mutableListOf()

    override fun output(message: String) {
        outPuts.add(message)
    }
}
