package io.github.qihuan92.activitystarter.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.qihuan92.activitystarter.annotation.Generated
import io.github.qihuan92.activitystarter.compiler.entity.ActivityClass
import io.github.qihuan92.activitystarter.compiler.utils.AptContext
import io.github.qihuan92.activitystarter.compiler.utils.PrebuiltTypes
import io.github.qihuan92.activitystarter.compiler.utils.kotlinClassName
import io.github.qihuan92.activitystarter.compiler.utils.kotlinTypeName

/**
 * Kotlin 扩展函数生成器
 *
 * @author Qi
 * @since 2022/8/21
 */
class KotlinExtGenerator(private val activityClass: ActivityClass) {

    private val activityName = activityClass.typeElement.simpleName

    fun execute() {
        val builder = FileSpec.builder(activityClass.packageName, activityClass.builderClassName)
            .addAnnotation(Generated::class)
        // 生成 start 扩展函数
        buildStartFun(builder)
        // 生成 Activity Result API 扩展函数
        if (activityClass.resultFieldEntities.isNotEmpty()) {
            buildResultAPIFun(builder)
            buildResultAPILauncherExtFun(builder)
        }
        // 写入文件
        writeKotlinToFile(builder.build())
    }

    private fun buildStartFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("start${activityName}")
            .receiver(PrebuiltTypes.CONTEXT.kotlinTypeName)
            .addActivityParameters()
            .addActivityBuilderStatement()
            .addStatement("builder.start(this)")
        builder.addFunction(funBuilder.build())
    }

    private fun FunSpec.Builder.addActivityParameters(): FunSpec.Builder {
        activityClass.requestFields.forEach {
            if (it.isRequired) {
                addParameter(it.name, it.kotlinTypeName)
            } else {
                addParameter(
                    ParameterSpec.builder(it.name, it.kotlinTypeName.copy(true))
                        .defaultValue("%L", it.defaultValue)
                        .build()
                )
            }
        }
        return this
    }

    private fun FunSpec.Builder.addActivityBuilderStatement(): FunSpec.Builder {
        val requiredParams = activityClass.requestFields
            .filter { it.isRequired }
            .joinToString(separator = ", ") { it.name }
        // 必传参数
        addStatement("val builder = %L.builder(${requiredParams})", activityClass.builderClassName)
        // 可选参数
        activityClass.requestFields
            .filter { !it.isRequired }
            .forEach {
                addStatement(".${it.name}(${it.name})")
            }
        return this
    }

    private fun buildResultAPIFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("registerFor${activityName}Result")
            .receiver(PrebuiltTypes.ACTIVITY_RESULT_CALLER.kotlinClassName)
            .addParameter(
                "callback",
                LambdaTypeName.get(
                    parameters = arrayOf(TypeVariableName("${activityClass.builderClassName}.Result")),
                    returnType = Unit::class.asTypeName()
                )
            )
            .addStatement(
                "return %L.registerForActivityResult(this) { callback(it) }",
                activityClass.builderClassName
            )
        builder.addFunction(funBuilder.build())
    }

    private fun buildResultAPILauncherExtFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("launch")
            .receiver(
                PrebuiltTypes.ACTIVITY_RESULT_LAUNCHER.kotlinClassName.parameterizedBy(
                    ClassName(activityClass.packageName, activityClass.builderClassName)
                )
            )
            .addActivityParameters()
            .addActivityBuilderStatement()
            .addStatement("launch(builder)")
        builder.addFunction(funBuilder.build())
    }

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(AptContext.getInstance().filer)
    }
}