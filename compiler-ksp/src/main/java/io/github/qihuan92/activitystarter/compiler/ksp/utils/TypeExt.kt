package io.github.qihuan92.activitystarter.compiler.ksp.utils

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

fun KClass<*>.toKsType(): KSType {
    return KspContext.resolver
        .getClassDeclarationByName(qualifiedName!!)!!
        .asType(emptyList())
}

fun KSType.isSubTypeOf(typeName: String): Boolean {
    return KspContext.resolver.getClassDeclarationByName(typeName)
        ?.asStarProjectedType()
        ?.isAssignableFrom(this) == true
}

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.toTypeName() = asStarProjectedType().toTypeName()