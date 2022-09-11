package io.github.qihuan92.activitystarter.compiler.entity

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import io.github.qihuan92.activitystarter.annotation.Extra
import io.github.qihuan92.activitystarter.compiler.utils.asKotlinTypeName
import io.github.qihuan92.activitystarter.compiler.utils.camelToUnderline
import io.github.qihuan92.activitystarter.compiler.utils.isSameType
import io.github.qihuan92.activitystarter.compiler.utils.type
import java.util.*
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

/**
 * Field
 *
 * @author qi
 * @since 2021/8/3
 */
class RequestFieldEntity(val variableElement: VariableElement) : Comparable<RequestFieldEntity> {
    var name: String
    val isRequired: Boolean
    val description: String
    var defaultValue: Any? = null
        private set
    val constFieldName: String
        get() = CONST_EXTRA_PREFIX + name.camelToUnderline().uppercase(Locale.getDefault())
    val javaTypeName: TypeName
        get() = ClassName.get(variableElement.asType())
    val kotlinTypeName
        get() = variableElement.asType().asKotlinTypeName()

    companion object {
        const val CONST_EXTRA_PREFIX = "EXTRA_"
    }

    init {
        val extraAnnotation = variableElement.getAnnotation(Extra::class.java)
        val name: String = extraAnnotation.value
        isRequired = extraAnnotation.required
        if (name.isNotEmpty()) {
            this.name = name
        } else {
            this.name = variableElement.simpleName.toString()
        }
        description = extraAnnotation.description
        setDefaultValue(extraAnnotation, variableElement)
    }

    private fun setDefaultValue(extraAnnotation: Extra, variableElement: VariableElement) {
        when (variableElement.asType().kind) {
            TypeKind.CHAR -> defaultValue = "'${extraAnnotation.charValue}'"
            TypeKind.BYTE -> defaultValue = "(byte) ${extraAnnotation.byteValue}"
            TypeKind.SHORT -> defaultValue = "(short) ${extraAnnotation.shortValue}"
            TypeKind.INT -> defaultValue = extraAnnotation.intValue
            TypeKind.LONG -> defaultValue = "${extraAnnotation.longValue}L"
            TypeKind.FLOAT -> defaultValue = "${extraAnnotation.floatValue}f"
            TypeKind.DOUBLE -> defaultValue = extraAnnotation.doubleValue
            TypeKind.BOOLEAN -> defaultValue = extraAnnotation.booleanValue
            else -> if (variableElement.asType().isSameType(String::class.java.type)) {
                defaultValue = "\"${extraAnnotation.stringValue}\""
            }
        }
    }

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