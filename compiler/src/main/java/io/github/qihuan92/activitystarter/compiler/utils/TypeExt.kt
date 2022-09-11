package io.github.qihuan92.activitystarter.compiler.utils

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

private val STRING_ARRAY = ClassName("kotlin", "Array").parameterizedBy(STRING)

val String.type: TypeMirror
    get() = AptContext.elements.getTypeElement(this).asType()

val ClassType.type
    get() = className.type

val Class<*>.type: TypeMirror
    get() = AptContext.elements.getTypeElement(canonicalName).asType()

val String.javaTypeName: com.squareup.javapoet.TypeName
    get() = com.squareup.javapoet.TypeName.get(type.apply { AptContext.types.erasure(this) })

val TypeElement.packageName: String
    get() = run {
        var element = enclosingElement
        while (element != null && element.kind != ElementKind.PACKAGE) {
            element = element.enclosingElement
        }
        if (element == null) {
            throw IllegalArgumentException(toString() + " does not have an enclosing element of package.")
        }
        return element.asType().toString()
    }

fun TypeMirror.isSubType(other: TypeMirror?): Boolean {
    if (other == null) {
        return false
    }
    return AptContext.types.isSubtype(this, other)
}

fun TypeMirror.isSameType(other: TypeMirror?): Boolean {
    if (other == null) {
        return false
    }
    return AptContext.types.isSameType(this, other)
}

@OptIn(DelicateKotlinPoetApi::class)
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