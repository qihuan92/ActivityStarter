package io.github.qihuan92.activitystarter.compiler.utils

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.qihuan92.activitystarter.compiler.entity.ClassType
import io.github.qihuan92.activitystarter.compiler.entity.RequestFieldEntity
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

val ClassType.kotlinTypeName
    get() = TypeUtils.getTypeFromClassName(className).asKotlinTypeName()

val RequestFieldEntity.kotlinTypeName
    get() = variableElement.asType().asKotlinTypeName()

private val STRING_ARRAY = ClassName("kotlin", "Array").parameterizedBy(STRING)

val com.squareup.javapoet.ClassName.kotlinClassName
    get() = ClassName(packageName(), simpleName())

fun TypeMirror.asKotlinTypeName(): TypeName {
    return when (kind) {
        TypeKind.BOOLEAN -> BOOLEAN
        TypeKind.BYTE -> BYTE
        TypeKind.SHORT -> SHORT
        TypeKind.INT -> INT
        TypeKind.LONG -> LONG
        TypeKind.CHAR -> CHAR
        TypeKind.FLOAT -> FLOAT
        TypeKind.DOUBLE -> DOUBLE
        TypeKind.ARRAY -> {
            val arrayType = this as ArrayType
            when (arrayType.componentType.kind) {
                TypeKind.BOOLEAN -> BOOLEAN_ARRAY
                TypeKind.BYTE -> BYTE_ARRAY
                TypeKind.SHORT -> SHORT_ARRAY
                TypeKind.INT -> INT_ARRAY
                TypeKind.LONG -> LONG_ARRAY
                TypeKind.CHAR -> CHAR_ARRAY
                TypeKind.FLOAT -> FLOAT_ARRAY
                TypeKind.DOUBLE -> DOUBLE_ARRAY
                else -> if (toString() == "java.lang.String[]") STRING_ARRAY else asTypeName()
            }
        }
        else -> if (toString() == "java.lang.String") STRING else asTypeName()
    }
}