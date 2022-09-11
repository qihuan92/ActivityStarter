package io.github.qihuan92.activitystarter.compiler.entity

import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.compiler.utils.packageName
import io.github.qihuan92.activitystarter.compiler.utils.type
import java.util.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * ActivityClass
 *
 * @author qi
 * @since 2021/8/3
 */
data class ActivityClass(val typeElement: TypeElement) {

    companion object {
        const val POSIX = "Builder"
    }

    val requestFieldEntities: MutableSet<RequestFieldEntity> = TreeSet()
    val resultFieldEntities: MutableSet<ResultFieldEntity> = TreeSet()

    val builderClassName: String
        get() {
            val list: MutableList<String?> = ArrayList()
            list.add(typeElement.simpleName.toString())
            var element = typeElement.enclosingElement
            while (element != null && element.kind != ElementKind.PACKAGE) {
                list.add(element.simpleName.toString())
                element = element.enclosingElement
            }
            list.reverse()
            return list.joinToString(separator = "_") + POSIX
        }
    val packageName: String
        get() = typeElement.packageName
    val isAbstract: Boolean
        get() = typeElement.modifiers.contains(Modifier.ABSTRACT)

    init {
        val builderAnnotation = typeElement.getAnnotation(Builder::class.java)
        for (resultField in builderAnnotation.resultFields) {
            val typeMirror = try {
                resultField.type.java.type
            } catch (e: MirroredTypeException) {
                e.typeMirror
            }
            resultFieldEntities.add(ResultFieldEntity(resultField.name, typeMirror))
        }
    }

    fun addFiled(requestFieldEntity: RequestFieldEntity) {
        requestFieldEntities.add(requestFieldEntity)
    }
}