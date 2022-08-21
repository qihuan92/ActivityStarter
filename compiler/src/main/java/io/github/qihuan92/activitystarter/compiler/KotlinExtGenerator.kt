package io.github.qihuan92.activitystarter.compiler

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import io.github.qihuan92.activitystarter.annotation.Generated
import io.github.qihuan92.activitystarter.compiler.entity.ActivityClass
import io.github.qihuan92.activitystarter.compiler.utils.AptContext
import io.github.qihuan92.activitystarter.compiler.utils.PrebuiltTypes
import io.github.qihuan92.activitystarter.compiler.utils.kotlinTypeName

/**
 * Kotlin 扩展函数生成器
 *
 * @author Qi
 * @since 2022/8/21
 */
class KotlinExtGenerator(private val activityClass: ActivityClass) {

    fun execute() {
        val builder = FileSpec.builder(activityClass.packageName, activityClass.builderClassName)
            .addAnnotation(Generated::class)
        // 生成 start 扩展函数
        buildStartFun(builder)
        // 生成 Activity Result API 扩展函数
        if (activityClass.resultFieldEntities.isNotEmpty()) {
            buildResultAPIFun(builder)
        }
        // 写入文件
        writeKotlinToFile(builder.build())
    }

    private fun buildStartFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("start${activityClass.typeElement.simpleName}")
            .receiver(PrebuiltTypes.CONTEXT.kotlinTypeName)
        activityClass.requestFields.forEach {
            if (it.isRequired) {
                funBuilder.addParameter(it.name, it.kotlinTypeName)
            } else {
                funBuilder.addParameter(
                    ParameterSpec.builder(it.name, it.kotlinTypeName.copy(true))
                        .defaultValue("%L", it.defaultValue)
                        .build()
                )
            }
        }

        funBuilder.addStatement(
            "val intent = %T(this, %T::class.java)",
            PrebuiltTypes.INTENT.kotlinTypeName,
            activityClass.typeElement
        )

        funBuilder.beginControlFlow("if(!(this is %L))", PrebuiltTypes.ACTIVITY.kotlinTypeName)
        funBuilder.addStatement(
            "intent.addFlags(%T.FLAG_ACTIVITY_NEW_TASK)",
            PrebuiltTypes.INTENT.kotlinTypeName
        )
        funBuilder.endControlFlow()

        activityClass.requestFields.forEach {
            funBuilder.addStatement(
                "intent.putExtra(%L.%L, %L)",
                activityClass.builderClassName,
                it.constFieldName,
                it.name
            )
        }

        funBuilder.addStatement("startActivity(intent)")

        builder.addFunction(funBuilder.build())
    }

    private fun buildResultAPIFun(builder: FileSpec.Builder) {
        // TODO
    }

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(AptContext.getInstance().filer)
    }
}