package com.rolandlin.shelfie.core.contract

import kotlinx.serialization.json.Json

/**
 * The one JSON setup for both sides. Unknown keys are ignored so the server can add
 * fields without breaking app versions that are already installed.
 */
val ContractJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}
