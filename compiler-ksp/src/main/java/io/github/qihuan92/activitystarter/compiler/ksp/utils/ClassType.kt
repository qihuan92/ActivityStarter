package io.github.qihuan92.activitystarter.compiler.ksp.utils

import com.squareup.kotlinpoet.ClassName

/**
 * ClassType
 *
 * @author qi
 * @since 2021/8/4
 */
class ClassType(val className: String) {
//    val javaTypeName
//        get() = className.javaTypeName
//
//    val kotlinTypeName
//        get() = className.type.asKotlinTypeName()
//
//    val javaClassName: com.squareup.javapoet.ClassName
//        get() = com.squareup.javapoet.ClassName.bestGuess(className)
//
    val kotlinClassName
        get() = ClassName.bestGuess(className)
}