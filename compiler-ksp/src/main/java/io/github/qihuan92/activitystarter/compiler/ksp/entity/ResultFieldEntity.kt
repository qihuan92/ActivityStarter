package io.github.qihuan92.activitystarter.compiler.ksp.entity

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.*

/**
 * 返回参数字段
 *
 * @author Qi
 * @since 2022/10/6
 */
@OptIn(KotlinPoetKspPreview::class)
data class ResultFieldEntity(
    val name: String,
    val ksType: KSType,
) : Comparable<ResultFieldEntity> {

    val typeName = ksType.toTypeName()

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