package io.github.qihuan92.activitystarter.compiler.ksp.entity

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.annotation.Extra
import io.github.qihuan92.activitystarter.compiler.ksp.utils.toKsType
import java.util.*

/**
 * @author Qi
 * @since 2022/10/6
 */
@OptIn(KspExperimental::class)
data class ActivityClass(val declaration: KSClassDeclaration) {
    companion object {
        const val POSIX = "Builder"
    }

    val requestFieldEntities: MutableSet<RequestFieldEntity> = TreeSet()
    val resultFieldEntities: MutableSet<ResultFieldEntity> = TreeSet()

    val builderClassName: String
        get() {
            val list: MutableList<String?> = ArrayList()
            list.add(declaration.simpleName.asString())
            var declaration = declaration.parentDeclaration
            while (declaration != null) {
                list.add(declaration.simpleName.asString())
                declaration = declaration.parentDeclaration
            }
            list.reverse()
            return list.joinToString(separator = "_") + POSIX
        }
    val packageName: String
        get() = declaration.packageName.asString()
    val isAbstract: Boolean
        get() = declaration.modifiers.contains(Modifier.ABSTRACT)

    init {
        declaration.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .forEach {
                if (it.isAnnotationPresent(Extra::class)) {
                    addFiled(RequestFieldEntity(it))
                }
            }

        val builderAnnotation =
            declaration.getAnnotationsByType(Builder::class).firstOrNull()
        builderAnnotation?.resultFields?.forEach {
            val type = try {
                it.type.toKsType()
            } catch (e: KSTypeNotPresentException) {
                e.ksType
            }
            resultFieldEntities.add(ResultFieldEntity(it.name, type))
        }
    }

    fun addFiled(requestFieldEntity: RequestFieldEntity) {
        requestFieldEntities.add(requestFieldEntity)
    }
}