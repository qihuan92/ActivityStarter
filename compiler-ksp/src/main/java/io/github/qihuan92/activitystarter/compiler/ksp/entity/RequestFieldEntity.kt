package io.github.qihuan92.activitystarter.compiler.ksp.entity

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.qihuan92.activitystarter.annotation.Extra
import io.github.qihuan92.activitystarter.compiler.ksp.utils.camelToUnderline
import java.util.*

/**
 * 请求参数字段
 *
 * @author Qi
 * @since 2022/10/6
 */
@OptIn(KotlinPoetKspPreview::class, KspExperimental::class)
data class RequestFieldEntity(
    private val ksPropertyDeclaration: KSPropertyDeclaration
) : Comparable<RequestFieldEntity> {
    companion object {
        const val CONST_EXTRA_PREFIX = "EXTRA_"
    }

    private val extraAnnotation = ksPropertyDeclaration.getAnnotationsByType(Extra::class).first()
    val name = ksPropertyDeclaration.simpleName.asString()
    val key = extraAnnotation.value.ifEmpty { name }
    val isRequired = extraAnnotation.required
    val description = extraAnnotation.description
    var defaultValue: Any? = null
        private set
    val constFieldName: String
        get() = CONST_EXTRA_PREFIX + name.camelToUnderline().uppercase(Locale.getDefault())

    val typeName = ksPropertyDeclaration.type.toTypeName()

    override fun compareTo(other: RequestFieldEntity): Int {

        return if (!other.isRequired) {
            name.compareTo(other.name)
        } else {
            1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as RequestFieldEntity
        return name == that.name
    }

    override fun hashCode(): Int {
        return Objects.hash(name)
    }
}