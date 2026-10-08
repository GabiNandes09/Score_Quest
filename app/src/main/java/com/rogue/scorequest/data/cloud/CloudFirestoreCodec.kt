package com.rogue.scorequest.data.cloud

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.longOrNull

private val codecJson = Json { encodeDefaults = true; ignoreUnknownKeys = true }

/**
 * Converte um @Serializable qualquer pra um Map<String, Any?> de tipos
 * nativos do Firestore (String/Long/Double/Boolean/List/Map/null) — o
 * mapeador de POJO do próprio Firestore exige construtor sem argumentos (as
 * data classes de CloudBackup.kt não têm, por design) e não entende sealed
 * class (ScoreFieldType/ScoreFormula, usados em CloudScoreSchema), então todo
 * documento normalizado passa por aqui nos dois sentidos.
 */
fun <T> toFirestoreMap(serializer: KSerializer<T>, value: T): Map<String, Any?> =
    (codecJson.encodeToJsonElement(serializer, value) as JsonObject).toPlainMap()

fun <T> fromFirestoreMap(serializer: KSerializer<T>, map: Map<String, Any?>): T =
    codecJson.decodeFromJsonElement(serializer, map.toJsonObject())

private fun JsonObject.toPlainMap(): Map<String, Any?> = mapValues { (_, v) -> v.toPlain() }

private fun JsonElement.toPlain(): Any? = when (this) {
    is JsonNull -> null
    is JsonObject -> toPlainMap()
    is JsonArray -> map { it.toPlain() }
    is JsonPrimitive -> when {
        isString -> content
        booleanOrNull != null -> booleanOrNull
        longOrNull != null -> longOrNull
        else -> content.toDoubleOrNull() ?: content
    }
}

private fun Map<String, Any?>.toJsonObject(): JsonObject =
    JsonObject(mapValues { (_, v) -> v.toJsonElement() })

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Map<*, *> -> JsonObject(entries.associate { (k, v) -> k.toString() to v.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    else -> JsonPrimitive(toString())
}
