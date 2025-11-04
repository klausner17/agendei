package com.klausner.infraestructure

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

val objectMapper = defaultMapper()

private fun defaultMapper() = JsonMapper.builder()
    .addModule(JavaTimeModule())
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .addModule(KotlinModule.Builder().build())
    .propertyNamingStrategy(PropertyNamingStrategies.LowerCamelCaseStrategy())
    .build()!!
