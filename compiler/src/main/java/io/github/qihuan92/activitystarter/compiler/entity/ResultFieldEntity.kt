package io.github.qihuan92.activitystarter.compiler.entity

import com.squareup.javapoet.TypeName
import io.github.qihuan92.activitystarter.compiler.utils.asKotlinTypeName
import java.util.*
import javax.lang.model.type.TypeMirror

/**
 * ResultField
 *
 * @author qi
 * @since 2021/8/16
 */
data class ResultFieldEntity(
    val name: String,
    private val type: TypeMirror
) : Comparable<ResultFieldEntity> {

    val javaTypeName: TypeName
        get() = TypeName.get(type)
    val kotlinTypeName: com.squareup.kotlinpoet.TypeName
        get() = type.asKotlinTypeName()

    override fun compareTo(other: ResultFieldEntity): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as ResultFieldEntity
        return name == that.name
    }

    override fun hashCode(): Int {
        return Objects.hash(name)
    }
}