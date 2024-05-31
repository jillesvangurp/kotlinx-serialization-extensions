@file:OptIn(ExperimentalSerializationApi::class)

package com.jillesvangurp.serializationext

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Sane defaults for [Json].
 */
val DEFAULT_JSON: Json = Json {
    // don't rely on external systems being written in kotlin or even having a language with default
    // values the default of false is dangerous
    encodeDefaults = true
    // save space
    prettyPrint = false
    // people adding things to the json is OK, we're forward compatible and will just ignore it
    isLenient = true
    // encoding nulls is meaningless and a waste of space.
    explicitNulls = false
    // adding new fields is OK even if older clients won't understand it
    ignoreUnknownKeys = true
    // ignore unknown enum values
    coerceInputValues = true
    // handle NaN and infinity
    allowSpecialFloatingPointValues = true
}

/**
 * Sane defaults for [Json] with pretty printing.
 */
val DEFAULT_PRETTY_JSON: Json = Json {
    encodeDefaults = true
    prettyPrint = true
    isLenient = true
    explicitNulls = false
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowSpecialFloatingPointValues = true
}
