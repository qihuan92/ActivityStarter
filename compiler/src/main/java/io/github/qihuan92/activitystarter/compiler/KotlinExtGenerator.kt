package io.github.qihuan92.activitystarter.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.qihuan92.activitystarter.annotation.Generated
import io.github.qihuan92.activitystarter.compiler.entity.ActivityClass
import io.github.qihuan92.activitystarter.compiler.utils.*

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
        if (activityClass.resultFieldEntities.isNotEmpty()) {
            // 生成 Activity Result API 扩展函数
            buildResultAPIFun(builder)
            buildResultAPILauncherExtFun(builder)
            // 生成 finish 扩展函数
            buildFinishFun(builder)
        }
        // 写入文件
        writeKotlinToFile(builder.build())
    }

    private fun buildStartFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("start${activityName}")
            .receiver(CONTEXT.kotlinTypeName)
            .addActivityParameters()
            .addActivityBuilderStatement()
            .addStatement("builder.start(this)")
        builder.addFunction(funBuilder.build())
    }

    private fun buildFinishFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("finish")
            .receiver(activityClass.typeElement.asType().asKotlinTypeName())

        activityClass.resultFieldEntities.forEach {
            funBuilder.addParameter(it.name, it.kotlinTypeName)
        }

        val resultParams = activityClass.resultFieldEntities
            .map { it.name }
            .toMutableList()
            .apply { add(0, "this") }
            .joinToString(separator = ", ")

        funBuilder.addStatement("%L.finish(${resultParams})", activityClass.builderClassName)

        builder.addFunction(funBuilder.build())
    }

    private fun FunSpec.Builder.addActivityParameters(): FunSpec.Builder {
        val kdocBuilder = CodeBlock.builder()
        activityClass.requestFieldEntities.forEach {
            if (it.isRequired) {
                addParameter(it.name, it.kotlinTypeName)
            } else {
                addParameter(
                    ParameterSpec.builder(it.name, it.kotlinTypeName.copy(true))
                        .defaultValue("%L", it.defaultValue)
                        .build()
                )
            }
            kdocBuilder.add("@param ${it.name} ${it.description}\n")
        }
        addKdoc(kdocBuilder.build())
        return this
    }

    private fun FunSpec.Builder.addActivityBuilderStatement(): FunSpec.Builder {
        val requiredParams = activityClass.requestFieldEntities
            .filter { it.isRequired }
            .joinToString(separator = ", ") { it.name }
        // 必传参数
        addStatement("val builder = %L.builder(${requiredParams})", activityClass.builderClassName)
        // 可选参数
        activityClass.requestFieldEntities
            .filter { !it.isRequired }
            .forEach {
                addStatement(".${it.name}(${it.name})")
            }
        return this
    }

    private fun buildResultAPIFun(builder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("registerFor${activityName}Result")
            .receiver(ACTIVITY_RESULT_CALLER.kotlinClassName)
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
                ACTIVITY_RESULT_LAUNCHER.kotlinClassName.parameterizedBy(
                    ClassName(activityClass.packageName, activityClass.builderClassName)
                )
            )
            .addActivityParameters()
            .addActivityBuilderStatement()
            .addStatement("launch(builder)")
        builder.addFunction(funBuilder.build())
    }

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(AptContext.filer)
    }
}