package io.github.qihuan92.activitystarter.compiler.ksp.utils

import java.util.*

fun String.camelToUnderline(): String {
    val sb = StringBuilder(length)
    for (i in indices) {
        val c = get(i)
        if (Character.isUpperCase(c)) {
            sb.append("_")
            sb.append(c.lowercaseChar())
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

fun String.capitalize(): String? {
    if (isEmpty() || Character.isLowerCase(first())) {
        return null
    }

    return substring(0, 1).uppercase(Locale.getDefault()) + substring(1)
}