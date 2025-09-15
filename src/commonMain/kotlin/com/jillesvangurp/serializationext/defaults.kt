@file:OptIn(ExperimentalSerializationApi::class)

package com.jillesvangurp.serializationext

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

// begin-jsondefaults
/**
 * Sane/safe defaults for [Json].
 */
val DEFAULT_JSON: Json = Json {
    // preserving defaults is important
    // don't assume users of your json has access to your model classes
    encodeDefaults = true
    // no new lines, useful if you are generating e.g. ndjson or
    // just want to save space on redundant whitespace
    prettyPrint = false
    // tolerate minor json issues in favor of still being able to parse things
    // this allows you to handle the quite common case of e.g. numbers being encoded as strings ("42")
    // and still parse this correctly
    isLenient = true
    // encoding nulls can waste a lot of space and client code should not depend on nulls being
    // present to begin with . Unless you have parsing logic
    // depending on explicit nulls (why?!) don't turn this on
    explicitNulls = false
    // forward compatibility: new fields in the json are OK, just ignore them
    // true is critical for clients when servers add new fields.
    // Without it, even harmless additions can break consumers.
    ignoreUnknownKeys = true
    // forward compatibility: ignore unknown enum values
    coerceInputValues = true
    // handle serialized NaN and infinity double values instead of having them default to null
    allowSpecialFloatingPointValues = true
}

/**
 * Same as [DEFAULT_JSON] with pretty printing turned on
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
// end-jsondefaults
