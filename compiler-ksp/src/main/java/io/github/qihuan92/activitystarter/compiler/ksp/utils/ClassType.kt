package io.github.qihuan92.activitystarter.compiler.ksp.utils

import com.squareup.kotlinpoet.ClassName

/**
 * ClassType
 *
 * @author qi
 * @since 2021/8/4
 */
class ClassType(val className: String) {
    val kotlinClassName
        get() = ClassName.bestGuess(className)
}