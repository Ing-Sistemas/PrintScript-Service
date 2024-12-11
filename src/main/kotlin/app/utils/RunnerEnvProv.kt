package com.example.springboot.app.utils

import com.printscript.interpreter.interfaces.EnvProvider

class RunnerEnvProv : EnvProvider {
    override fun getEnv(name: String): String? {
        return ""
    }
}