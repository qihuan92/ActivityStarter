package io.github.qihuan92.activitystarter.compiler.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.annotation.Generated
import io.github.qihuan92.activitystarter.compiler.ksp.entity.ActivityClass
import io.github.qihuan92.activitystarter.compiler.ksp.utils.*

/**
 * Builder注解处理器
 *
 * @author Qi
 * @since 2022/9/2
 */
@OptIn(KotlinPoetKspPreview::class)
class ActivityBuilderProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val activityClasses = HashMap<KSNode, ActivityClass>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        KspContext.environment = environment
        KspContext.resolver = resolver

        processClass(resolver)
        buildFiles()
        return emptyList()
    }

    private fun processClass(resolver: Resolver) {
        val qualifiedName = Builder::class.qualifiedName ?: return
        resolver.getSymbolsWithAnnotation(qualifiedName)
            .filterIsInstance<KSClassDeclaration>()
            .forEach {
                val type = it.asStarProjectedType()
                if (type.isSubTypeOf(ACTIVITY.className)) {
                    activityClasses[it] = ActivityClass(it)
                }
            }
    }

    private fun buildFiles() {
        activityClasses.forEach {
            buildFile(it.value)
        }
    }

    private fun buildFile(activityClass: ActivityClass) {
        if (activityClass.isAbstract) {
            return
        }

        val typeBuilder = TypeSpec.classBuilder(activityClass.builderClassName)
            .addAnnotation(Generated::class)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
        buildClass(activityClass, typeBuilder)

        val fileSpecBuilder =
            FileSpec.builder(activityClass.packageName, activityClass.builderClassName)
                .addAnnotation(Generated::class)
                .addType(typeBuilder.build())
        KotlinExtGenerator(activityClass).execute(fileSpecBuilder)
        writeKotlinToFile(activityClass, fileSpecBuilder.build())
    }

    private fun buildClass(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val companionBuilder = TypeSpec.companionObjectBuilder()
        buildConstant(activityClass, companionBuilder)
        buildField(activityClass, builder, companionBuilder)
        buildIntentMethod(activityClass, builder)
        buildInjectMethod(activityClass, companionBuilder)
        buildSaveStateMethod(activityClass, companionBuilder)
        buildNewIntentMethod(activityClass, companionBuilder)
        buildStartMethod(builder)
        buildFinishMethod(activityClass, companionBuilder)
        buildResultContractTypes(activityClass, builder, companionBuilder)
        builder.addType(companionBuilder.build())
    }

    private fun buildConstant(activityClass: ActivityClass, companionBuilder: TypeSpec.Builder) {
        activityClass.requestFieldEntities.forEach {
            val propertySpec = PropertySpec.builder(it.constFieldName, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", it.key)
                .build()
            companionBuilder.addProperty(propertySpec)
        }
    }

    private fun buildField(
        activityClass: ActivityClass,
        builder: TypeSpec.Builder,
        companionBuilder: TypeSpec.Builder
    ) {
        val builderClassTypeName =
            ClassName(activityClass.packageName, activityClass.builderClassName)

        // 创建 builder() 方法
        val builderFunBuilder = FunSpec.builder("builder")
            .returns(builderClassTypeName)
            .addStatement("val builder = %T()", builderClassTypeName)

        // 必传参数注释
        val requiredParamDocsBuilder = CodeBlock.builder()
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            // 变量
            builder.addProperty(
                PropertySpec.builder(
                    requestFieldEntity.name,
                    requestFieldEntity.typeName.copy(nullable = true),
                    KModifier.PRIVATE
                )
                    .mutable(true)
                    .initializer("null")
                    .build()
            )
            if (requestFieldEntity.isRequired) {
                // 添加到 builder() 参数
                builderFunBuilder.addParameter(
                    ParameterSpec.builder(requestFieldEntity.name, requestFieldEntity.typeName)
                        .build()
                )

                // 变量赋值
                builderFunBuilder.addStatement(
                    "builder.%L = %L",
                    requestFieldEntity.name,
                    requestFieldEntity.name
                )

                // 注释
                requiredParamDocsBuilder.add(
                    "@param %L %L",
                    requestFieldEntity.name,
                    requestFieldEntity.description
                ).add("\n")
            } else {
                // setter
                builder.addFunction(
                    FunSpec.builder(requestFieldEntity.name)
                        .addModifiers(KModifier.PUBLIC)
                        .addParameter(requestFieldEntity.name, requestFieldEntity.typeName)
                        .addKdoc(
                            "@param %L %L",
                            requestFieldEntity.name,
                            requestFieldEntity.description
                        )
                        .addStatement(
                            "this.%L = %L",
                            requestFieldEntity.name,
                            requestFieldEntity.name
                        )
                        .addStatement("return this")
                        .returns(builderClassTypeName)
                        .build()
                )
            }
        }

        // 为 builder() 方法添加注释
        builderFunBuilder.addKdoc(requiredParamDocsBuilder.build())
        builderFunBuilder.addAnnotation(JvmStatic::class)
        companionBuilder.addFunction(builderFunBuilder.addStatement("return builder").build())
    }

    private fun buildIntentMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val intentMethodBuilder = FunSpec.builder("getIntent")
            .addParameter("context", CONTEXT.kotlinClassName)
            .returns(INTENT.kotlinClassName)
            .addStatement(
                "val intent = %T(context, %T::class.java)",
                INTENT.kotlinClassName,
                activityClass.declaration.toTypeName()
            )
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            intentMethodBuilder.addStatement(
                "intent.putExtra(%L, %L)",
                requestFieldEntity.constFieldName,
                requestFieldEntity.name
            )
        }
        intentMethodBuilder.addStatement("return intent")
        builder.addFunction(intentMethodBuilder.build())
    }

    private fun buildInjectMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val injectMethodBuilder = FunSpec.builder("inject")
            .addAnnotation(JvmStatic::class)
            .addParameter("instance", ACTIVITY.kotlinClassName)
            .addParameter("savedInstanceState", BUNDLE.kotlinClassName.copy(nullable = true))
            .beginControlFlow("if(instance is %T)", activityClass.declaration.toTypeName())
            .beginControlFlow("if(savedInstanceState != null)")
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            val defaultValue = requestFieldEntity.defaultValue
            if (defaultValue == null) {
                injectMethodBuilder.addStatement(
                    "instance.%L = %T.get(savedInstanceState, %L)",
                    requestFieldEntity.name,
                    BUNDLE_UTILS.kotlinClassName,
                    requestFieldEntity.constFieldName
                )
            } else {
                injectMethodBuilder.addStatement(
                    "instance.%L = %T.get(savedInstanceState, %L, %L)",
                    requestFieldEntity.name,
                    BUNDLE_UTILS.kotlinClassName,
                    requestFieldEntity.constFieldName,
                    defaultValue
                )
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()
        builder.addFunction(injectMethodBuilder.build())
    }

    private fun buildSaveStateMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val saveStateMethodBuilder = FunSpec.builder("saveState")
            .addAnnotation(JvmStatic::class)
            .addParameter("instance", ACTIVITY.kotlinClassName)
            .addParameter("outState", BUNDLE.kotlinClassName)
            .beginControlFlow("if(instance is %T)", activityClass.declaration.toTypeName())
            .addStatement("val intent = %T()", INTENT.kotlinClassName)
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            saveStateMethodBuilder.addStatement(
                "intent.putExtra(%L, instance.%L)",
                requestFieldEntity.constFieldName,
                requestFieldEntity.name
            )
        }
        saveStateMethodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow()
        builder.addFunction(saveStateMethodBuilder.build())
    }

    private fun buildNewIntentMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val newIntentMethodBuilder = FunSpec.builder("processNewIntent")
            .addAnnotation(JvmStatic::class)
            .addParameter("activity", activityClass.declaration.toTypeName())
            .addParameter("intent", INTENT.kotlinClassName.copy(true))
        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)")
        builder.addFunction(newIntentMethodBuilder.build())
        val newIntentWithUpdateMethodBuilder = FunSpec.builder("processNewIntent")
            .addAnnotation(JvmStatic::class)
            .addParameter("activity", activityClass.declaration.toTypeName())
            .addParameter("intent", INTENT.kotlinClassName.copy(true))
            .addParameter("updateIntent", Boolean::class)
        newIntentWithUpdateMethodBuilder.beginControlFlow("if(updateIntent)")
            .addStatement("activity.setIntent(intent)")
            .endControlFlow()
        newIntentWithUpdateMethodBuilder.beginControlFlow("if(intent != null)")
            .addStatement("inject(activity, intent.getExtras())")
            .endControlFlow()
        builder.addFunction(newIntentWithUpdateMethodBuilder.build())
    }

    private fun buildStartMethod(builder: TypeSpec.Builder) {
        builder.addFunction(startMethodBuilder(false).build())
        builder.addFunction(startMethodBuilder(true).build())
        builder.addFunction(startForResultMethodBuilder(false).build())
        builder.addFunction(startForResultMethodBuilder(true).build())
    }

    private fun startMethodBuilder(withOptions: Boolean): FunSpec.Builder {
        val builder = FunSpec.builder("start")
            .addParameter("context", CONTEXT.kotlinClassName)
            .addStatement("val intent = getIntent(context)")
            .beginControlFlow("if(!(context is Activity))")
            .addStatement("intent.addFlags(%T.FLAG_ACTIVITY_NEW_TASK)", INTENT.kotlinClassName)
            .endControlFlow()
        if (withOptions) {
            builder.addParameter("options", BUNDLE.kotlinClassName)
            builder.addStatement("context.startActivity(intent, options)")
        } else {
            builder.addStatement("context.startActivity(intent)")
        }
        return builder
    }

    private fun startForResultMethodBuilder(withOptions: Boolean): FunSpec.Builder {
        val builder = FunSpec.builder("start")
            .addParameter("activity", ACTIVITY.kotlinClassName)
            .addParameter("requestCode", INT)
            .addStatement("val intent = getIntent(activity)")
        if (withOptions) {
            builder.addParameter("options", BUNDLE.kotlinClassName)
            builder.addStatement("activity.startActivityForResult(intent, requestCode, options)")
        } else {
            builder.addStatement("activity.startActivityForResult(intent, requestCode)")
        }
        return builder
    }

    private fun buildFinishMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val resultFieldEntities = activityClass.resultFieldEntities

        // 生成返回方法
        val finishMethodBuilder = FunSpec.builder("finish")
            .addAnnotation(JvmStatic::class)
            .addParameter("activity", ACTIVITY.kotlinClassName)
        if (resultFieldEntities.isNotEmpty()) {
            finishMethodBuilder.addStatement("val intent = %T()", INTENT.kotlinClassName)
            finishMethodBuilder.addStatement(
                "activity.setResult(%T.RESULT_OK, intent)",
                ACTIVITY.kotlinClassName
            )
            resultFieldEntities.forEach {
                finishMethodBuilder.addParameter(it.name, it.typeName)
                finishMethodBuilder.addStatement("intent.putExtra(%S, %L)", it.name, it.name)
            }
        }
        finishMethodBuilder.addStatement(
            "%T.finishAfterTransition(activity)",
            ACTIVITY_COMPAT.kotlinClassName
        )
        builder.addFunction(finishMethodBuilder.build())
    }

    private fun buildResultContractTypes(
        activityClass: ActivityClass,
        builder: TypeSpec.Builder,
        companionBuilder: TypeSpec.Builder
    ) {
        val resultFieldEntities = activityClass.resultFieldEntities
        if (resultFieldEntities.isEmpty()) {
            return
        }

        // 生成返回结果实体类
        val resultConstructorBuilder = FunSpec.constructorBuilder()
            .addParameter("resultCode", INT)
        resultFieldEntities.forEach {
            resultConstructorBuilder.addParameter(
                ParameterSpec.builder(it.name, it.ksType.toTypeName().copy(true))
                    .defaultValue("null")
                    .build()
            )
        }
        resultConstructorBuilder.parameters
        val resultClassBuilder = TypeSpec.classBuilder("Result")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(resultConstructorBuilder.build())
            .addProperty(
                PropertySpec.builder("resultCode", INT)
                    .initializer("resultCode")
                    .build()
            )
        resultFieldEntities.forEach {
            resultClassBuilder.addProperty(
                PropertySpec.builder(it.name, it.ksType.toTypeName().copy(true))
                    .initializer(it.name)
                    .mutable(true)
                    .build()
            )
        }
        builder.addType(resultClassBuilder.build())
        val resultClassName = ClassName.bestGuess("Result")

        // 生成结果解析方法
        val obtainResultMethodBuilder = FunSpec.builder("obtainResult")
            .addAnnotation(JvmStatic::class)
            .addParameter("resultCode", INT)
            .addParameter("intent", INTENT.kotlinClassName.copy(nullable = true))
            .returns(resultClassName)
            .addStatement("val result = %T(resultCode)", resultClassName)
            .beginControlFlow("if(intent != null)")
            .addStatement("val bundle = intent.getExtras()")
        resultFieldEntities.forEach {
            obtainResultMethodBuilder.addStatement(
                "result.%L = %T.get(bundle, %S)",
                it.name, BUNDLE_UTILS.kotlinClassName, it.name
            )
        }
        obtainResultMethodBuilder.endControlFlow()
        obtainResultMethodBuilder.addStatement("return result")
        companionBuilder.addFunction(obtainResultMethodBuilder.build())

        // 生成 ResultContract
        val resultContractClassBuilder = TypeSpec.classBuilder("ResultContract")
        val builderClassTypeName =
            ClassName(activityClass.packageName, activityClass.builderClassName)
        val activityContractTypeName = ACTIVITY_RESULT_CONTRACT.kotlinClassName.parameterizedBy(
            builderClassTypeName,
            resultClassName
        )
        resultContractClassBuilder.superclass(activityContractTypeName)
        resultContractClassBuilder.addFunction(
            FunSpec.builder("createIntent")
                .returns(INTENT.kotlinClassName)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder("context", CONTEXT.kotlinClassName).build()
                )
                .addParameter("input", builderClassTypeName)
                .addStatement("return input.getIntent(context)")
                .build()
        )
        val parseResultMethodBuilder = FunSpec.builder("parseResult")
            .addModifiers(KModifier.OVERRIDE)
            .returns(resultClassName)
            .addParameter("resultCode", INT)
            .addParameter(
                ParameterSpec.builder("intent", INTENT.kotlinClassName.copy(true))
                    .build()
            )
            .addStatement("return obtainResult(resultCode, intent)")
        resultContractClassBuilder.addFunction(parseResultMethodBuilder.build())
        builder.addType(resultContractClassBuilder.build())

        // 生成 registerForActivityResult 方法
        val registerForActivityResultMethodBuilder =
            FunSpec.builder("registerForActivityResult")
                .addAnnotation(JvmStatic::class)
                .addParameter(
                    ParameterSpec.builder("resultCaller", ACTIVITY_RESULT_CALLER.kotlinClassName)
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(
                        "callback",
                        ACTIVITY_RESULT_CALLBACK.kotlinClassName.parameterizedBy(resultClassName)
                    ).build()
                )
                .returns(
                    ACTIVITY_RESULT_LAUNCHER.kotlinClassName.parameterizedBy(builderClassTypeName)
                )
                .addStatement("return resultCaller.registerForActivityResult(ResultContract(), callback)")
        companionBuilder.addFunction(registerForActivityResultMethodBuilder.build())
    }

    private fun writeKotlinToFile(activityClass: ActivityClass, fileSpec: FileSpec) {
        val containingFile = activityClass.declaration.containingFile ?: return
        runCatching {
            fileSpec.writeTo(environment.codeGenerator, false, listOf(containingFile))
        }
    }
}