package com.example

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*

class ConfigValidator(
    private val config: ApplicationConfig,
    private val parent: String = "",
    private val errors: MutableList<String> = mutableListOf(),
) {
    fun obj(name: String, function: ConfigValidator.() -> Unit) {
        val configName = if (parent.isNotEmpty()) { "$parent.$name" } else { name }
        val parentConfig = config.config(name)
        function.invoke(ConfigValidator(parentConfig, configName, errors))
    }

    fun field(parameter: String) {
        val error = if (parent.isNotEmpty()) { "$parent.$parameter" } else { parameter }
        config.tryGetString(parameter) ?: errors.add(error)
    }

    fun validate() {
        if (errors.isNotEmpty()){
            throw RuntimeException("Missing properties: $errors")
        }
    }
}

fun validateConfig(config: ApplicationConfig, function: ConfigValidator.() -> Unit): ConfigValidator{
    val configValidator = ConfigValidator(config)
    configValidator.function()
    configValidator.validate()
    return configValidator
}


fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {

    validateConfig(environment.config){
        obj("parent1"){
            field("child1")
        }

        field("child3")

        obj("parent2"){
            obj("parent3"){
                field("child1")
            }
        }
    }
}
