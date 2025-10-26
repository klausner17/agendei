package com.klausner.infraestructure

import com.klausner.infraestructure.serializers.LocalDateTimeSerializer
import com.klausner.infraestructure.serializers.UUIDSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val json = Json {
    this.ignoreUnknownKeys = true
    SerializersModule {
        contextual(LocalDateTimeSerializer)
        contextual(UUIDSerializer)
    }
}
