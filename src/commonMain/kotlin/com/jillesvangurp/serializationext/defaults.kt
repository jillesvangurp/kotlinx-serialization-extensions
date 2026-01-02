@file:OptIn(ExperimentalSerializationApi::class)

package com.jillesvangurp.serializationext

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

// begin-jsondefaults
/**
 * Sane/safe defaults for [Json] that maximize forward/backward compatibility.
 *
 * The goal of these defaults is to make a best effort to parse json and not fail parsing needlessly
 * over recoverable situations like omitting nullable fields, having enum values that are new, extra
 * fields that aren't in your model, etc. If you want to be strict about this, then don't
 * use this library.
 *
 * Also has some sane defaults for generating Json that ensure the other side doesn't have to be
 * a Kotlin program using the exact same model classes and kotlinx.serialization configuration.
 */
val DEFAULT_JSON: Json = Json {
    // defaults that maximize compatibility for parsing:

    // tolerate minor json issues in favor of still being able to parse things
    // this allows you to handle the quite common case of e.g. numbers being encoded as strings ("42")
    // and still parse this correctly.
    isLenient = true
    // forward compatibility: new fields in the json are OK, just ignore them
    // true is critical for clients when servers add new fields.
    // Without it, even harmless API optional field additions can break consumers.
    ignoreUnknownKeys = true
    // forward compatibility: ignore unknown enum values
    coerceInputValues = true
    // handle serialized NaN and infinity double values instead of having them default to null
    allowSpecialFloatingPointValues = true
    // allow people to use comments with // and /* ... */
    allowComments = true
    // allow trailing commas for list items / map entries as is common in Javascript; and indeed Kotlin
    allowTrailingComma = true

    // defaults that maximize compatibility for serialized json:

    // no new lines, useful if you are generating e.g. ndjson or
    // just want to save space on redundant whitespace
    prettyPrint = false
    // preserving defaults is important
    // don't assume users of your json has access to your model classes with their defaults and
    // explicitly encode defaults
    encodeDefaults = true
    // encoding nulls can waste a lot of space and client code should not depend on nulls being
    // present to begin with . Unless you have parsing logic
    // depending on explicit nulls (why?!) don't turn this on
    explicitNulls = false

}

/**
 * Same as [DEFAULT_JSON] with pretty printing turned on
 */
val DEFAULT_PRETTY_JSON: Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowSpecialFloatingPointValues = true
    allowComments = true
    allowTrailingComma = true

    encodeDefaults = true
    prettyPrint = true
    explicitNulls = false
}
// end-jsondefaults
