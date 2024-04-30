package com.jainhardik120.passbud.domain

data class BankCard(
    val cardNumber: String,
    val validFrom: String?,
    val validThru: String?,
    val cvv: String?
) {
    fun formatToString(): String {
        return "$cardNumber|||$validFrom|||$validThru|||$cvv"
    }
}

fun String.toBankCard(): BankCard? {
    val parts = this.split("|||")
    if (parts.size != 4) {
        return null
    }
    return BankCard(
        cardNumber = parts[0],
        validFrom = parts[1].ifEmpty { null },
        validThru = parts[2].ifEmpty { null },
        cvv = parts[3].ifEmpty { null }
    )
}

fun formatMMYY(validFrom: String): String {
    if (validFrom.isBlank()) return ""

    val firstTwoChars = validFrom.take(2)

    val remainingChars = validFrom.drop(2)
    return if (remainingChars.isNotBlank()) {
        "$firstTwoChars/$remainingChars"
    } else {
        firstTwoChars
    }
}