package io.github.qihuan92.activitystarter.compiler

import com.squareup.javapoet.*
import io.github.qihuan92.activitystarter.annotation.Builder
import io.github.qihuan92.activitystarter.annotation.Extra
import io.github.qihuan92.activitystarter.annotation.Generated
import io.github.qihuan92.activitystarter.compiler.entity.ActivityClass
import io.github.qihuan92.activitystarter.compiler.entity.RequestFieldEntity
import io.github.qihuan92.activitystarter.compiler.utils.*
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ActivityBuilderProcessor : AbstractProcessor() {
    private val activityClasses: MutableMap<Element, ActivityClass> = HashMap()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        AptContext.init(processingEnv)
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
            Builder::class.java.canonicalName,
            Extra::class.java.canonicalName
        )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return true
        }
        parseClass(roundEnv)
        parseFields(roundEnv)
        buildFiles()
        return true
    }

    private fun parseClass(roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(Builder::class.java)
            .filter { it.kind.isClass }
            .forEach {
                if (it.asType().isSubType(ACTIVITY.type)) {
                    activityClasses[it] = ActivityClass(it as TypeElement)
                }
            }
    }

    private fun parseFields(roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(Extra::class.java)
            .filter { it.kind.isField }
            .forEach {
                val activityClass = activityClasses[it.enclosingElement]
                activityClass?.addFiled(RequestFieldEntity(it as VariableElement))
            }
    }

    private fun buildFiles() {
        activityClasses.forEach { (_, activityClass) ->
            buildFile(activityClass)
        }
    }

    private fun buildFile(activityClass: ActivityClass) {
        if (activityClass.isAbstract) {
            return
        }
        val typeBuilder = TypeSpec.classBuilder(activityClass.builderClassName)
            .addAnnotation(Generated::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        buildClass(activityClass, typeBuilder)
        writeJavaToFile(activityClass, typeBuilder.build())
    }

    private fun buildClass(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        buildConstant(activityClass, builder)
        buildField(activityClass, builder)
        buildIntentMethod(activityClass, builder)
        buildInjectMethod(activityClass, builder)
        buildSaveStateMethod(activityClass, builder)
        buildNewIntentMethod(activityClass, builder)
        buildStartMethod(builder)
        buildFinishMethod(activityClass, builder)
        buildResultContractTypes(activityClass, builder)
        KotlinExtGenerator(activityClass).execute()
    }

    private fun buildConstant(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            builder.addField(
                FieldSpec.builder(
                    String::class.java,
                    requestFieldEntity.constFieldName,
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .initializer("\$S", requestFieldEntity.key)
                    .build()
            )
        }
    }

    private fun buildField(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val builderClassTypeName =
            ClassName.get(activityClass.packageName, activityClass.builderClassName)

        // 创建 builder() 方法
        val builderMethodBuilder = MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(builderClassTypeName)
            .addStatement("\$T builder = new \$T()", builderClassTypeName, builderClassTypeName)

        // 必传参数注释
        val requiredParamDocsBuilder = CodeBlock.builder()
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            // 变量
            builder.addField(
                FieldSpec.builder(
                    requestFieldEntity.javaTypeName,
                    requestFieldEntity.name,
                    Modifier.PRIVATE
                ).build()
            )
            if (requestFieldEntity.isRequired) {
                // 添加到 builder() 参数
                builderMethodBuilder.addParameter(
                    ParameterSpec.builder(requestFieldEntity.javaTypeName, requestFieldEntity.name)
                        .build()
                )

                // 变量赋值
                builderMethodBuilder.addStatement(
                    "builder.\$L = \$L",
                    requestFieldEntity.name,
                    requestFieldEntity.name
                )

                // 注释
                requiredParamDocsBuilder.add(
                    "@param \$L \$L",
                    requestFieldEntity.name,
                    requestFieldEntity.description
                )
                    .add("\n")
            } else {
                // setter
                builder.addMethod(
                    MethodSpec.methodBuilder(requestFieldEntity.name)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(requestFieldEntity.javaTypeName, requestFieldEntity.name)
                        .addJavadoc(
                            "@param \$L \$L",
                            requestFieldEntity.name,
                            requestFieldEntity.description
                        )
                        .addStatement(
                            "this.\$L = \$L",
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
        builderMethodBuilder.addJavadoc(requiredParamDocsBuilder.build())
        builder.addMethod(builderMethodBuilder.addStatement("return builder").build())
    }

    private fun buildIntentMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val intentMethodBuilder = MethodSpec.methodBuilder("getIntent")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(CONTEXT.javaTypeName, "context")
            .returns(INTENT.javaTypeName)
            .addStatement(
                "\$T intent = new \$T(context, \$T.class)",
                INTENT.javaTypeName,
                INTENT.javaTypeName,
                activityClass.typeElement
            )
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            intentMethodBuilder.addStatement(
                "intent.putExtra(\$L, \$L)",
                requestFieldEntity.constFieldName,
                requestFieldEntity.name
            )
        }
        intentMethodBuilder.addStatement("return intent")
        builder.addMethod(intentMethodBuilder.build())
    }

    private fun buildInjectMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val injectMethodBuilder = MethodSpec.methodBuilder("inject")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ACTIVITY.javaTypeName, "instance")
            .addParameter(BUNDLE.javaTypeName, "savedInstanceState")
            .beginControlFlow("if(instance instanceof \$T)", activityClass.typeElement)
            .addStatement(
                "\$T typedInstance = (\$T) instance",
                activityClass.typeElement,
                activityClass.typeElement
            )
            .beginControlFlow("if(savedInstanceState != null)")
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            injectMethodBuilder.addStatement(
                "typedInstance.\$L = \$T.<\$T>get(savedInstanceState, \$L, \$L)",
                requestFieldEntity.name,
                BUNDLE_UTILS.javaTypeName,
                requestFieldEntity.javaTypeName.box(),
                requestFieldEntity.constFieldName,
                requestFieldEntity.defaultValue
            )
        }
        injectMethodBuilder.endControlFlow().endControlFlow()
        builder.addMethod(injectMethodBuilder.build())
    }

    private fun buildSaveStateMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val saveStateMethodBuilder = MethodSpec.methodBuilder("saveState")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.VOID)
            .addParameter(ACTIVITY.javaTypeName, "instance")
            .addParameter(BUNDLE.javaTypeName, "outState")
            .beginControlFlow("if(instance instanceof \$T)", activityClass.typeElement)
            .addStatement(
                "\$T typedInstance = (\$T) instance",
                activityClass.typeElement,
                activityClass.typeElement
            )
            .addStatement(
                "\$T intent = new \$T()",
                INTENT.javaTypeName,
                INTENT.javaTypeName
            )
        val requestFieldEntities = activityClass.requestFieldEntities
        for (requestFieldEntity in requestFieldEntities) {
            saveStateMethodBuilder.addStatement(
                "intent.putExtra(\$L, typedInstance.\$L)",
                requestFieldEntity.constFieldName,
                requestFieldEntity.name
            )
        }
        saveStateMethodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow()
        builder.addMethod(saveStateMethodBuilder.build())
    }

    private fun buildNewIntentMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val newIntentMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(TypeName.get(activityClass.typeElement.asType()), "activity")
            .addParameter(INTENT.javaTypeName, "intent")
        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)")
        builder.addMethod(newIntentMethodBuilder.build())
        val newIntentWithUpdateMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(TypeName.get(activityClass.typeElement.asType()), "activity")
            .addParameter(INTENT.javaTypeName, "intent")
            .addParameter(Boolean::class.java, "updateIntent")
        newIntentWithUpdateMethodBuilder.beginControlFlow("if(updateIntent)")
            .addStatement("activity.setIntent(intent)")
            .endControlFlow()
        newIntentWithUpdateMethodBuilder.beginControlFlow("if(intent != null)")
            .addStatement("inject(activity, intent.getExtras())")
            .endControlFlow()
        builder.addMethod(newIntentWithUpdateMethodBuilder.build())
    }

    private fun buildStartMethod(builder: TypeSpec.Builder) {
        builder.addMethod(startMethodBuilder(false).build())
        builder.addMethod(startMethodBuilder(true).build())
        builder.addMethod(startForResultMethodBuilder(false).build())
        builder.addMethod(startForResultMethodBuilder(true).build())
    }

    private fun startMethodBuilder(withOptions: Boolean): MethodSpec.Builder {
        val builder = MethodSpec.methodBuilder("start")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(CONTEXT.javaTypeName, "context")
            .addStatement("\$T intent = getIntent(context)", INTENT.javaTypeName)
        builder.beginControlFlow("if(!(context instanceof Activity))")
        builder.addStatement(
            "intent.addFlags(\$T.FLAG_ACTIVITY_NEW_TASK)",
            INTENT.javaTypeName
        )
        builder.endControlFlow()
        if (withOptions) {
            builder.addParameter(BUNDLE.javaTypeName, "options")
            builder.addStatement("context.startActivity(intent, options)")
        } else {
            builder.addStatement("context.startActivity(intent)")
        }
        return builder
    }

    private fun startForResultMethodBuilder(withOptions: Boolean): MethodSpec.Builder {
        val builder = MethodSpec.methodBuilder("start")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ACTIVITY.javaTypeName, "activity")
            .addParameter(TypeName.INT, "requestCode")
            .addStatement("\$T intent = getIntent(activity)", INTENT.javaTypeName)
        if (withOptions) {
            builder.addParameter(BUNDLE.javaTypeName, "options")
            builder.addStatement("activity.startActivityForResult(intent, requestCode, options)")
        } else {
            builder.addStatement("activity.startActivityForResult(intent, requestCode)")
        }
        return builder
    }

    private fun buildFinishMethod(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val resultFieldEntities = activityClass.resultFieldEntities

        // 生成返回方法
        val finishMethodBuilder = MethodSpec.methodBuilder("finish")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ACTIVITY.javaTypeName, "activity")
            .returns(TypeName.VOID)
        if (resultFieldEntities.isNotEmpty()) {
            finishMethodBuilder.addStatement(
                "\$T intent = new \$T()",
                INTENT.javaTypeName,
                INTENT.javaTypeName
            )
            finishMethodBuilder.addStatement(
                "activity.setResult(\$T.RESULT_OK, intent)",
                ACTIVITY.javaTypeName
            )
            for (resultFieldEntity in resultFieldEntities) {
                val name = resultFieldEntity.name
                val typeName = resultFieldEntity.javaTypeName
                finishMethodBuilder.addParameter(typeName, name)
                finishMethodBuilder.addStatement("intent.putExtra(\$S, \$L)", name, name)
            }
        }
        finishMethodBuilder.addStatement(
            "\$T.finishAfterTransition(activity)",
            ACTIVITY_COMPAT.javaTypeName
        )
        builder.addMethod(finishMethodBuilder.build())
    }

    private fun buildResultContractTypes(activityClass: ActivityClass, builder: TypeSpec.Builder) {
        val resultFieldEntities = activityClass.resultFieldEntities
        if (resultFieldEntities.isEmpty()) {
            return
        }
        // 生成返回结果实体类
        val resultClassBuilder = TypeSpec.classBuilder("Result")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addField(TypeName.INT, "resultCode", Modifier.PUBLIC)
        for (resultFieldEntity in resultFieldEntities) {
            resultClassBuilder.addField(
                resultFieldEntity.javaTypeName,
                resultFieldEntity.name,
                Modifier.PUBLIC
            )
        }
        builder.addType(resultClassBuilder.build())
        val resultClassName = ClassName.bestGuess("Result")

        // 生成结果解析方法
        val obtainResultMethodBuilder = MethodSpec.methodBuilder("obtainResult")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(TypeName.INT, "resultCode")
            .addParameter(INTENT.javaTypeName, "intent")
            .returns(resultClassName)
            .addStatement("\$T result = new \$T()", resultClassName, resultClassName)
            .addStatement("result.resultCode = resultCode")
            .beginControlFlow("if(intent != null)")
            .addStatement("\$T bundle = intent.getExtras()", BUNDLE.javaTypeName)
        for (resultFieldEntity in resultFieldEntities) {
            val name = resultFieldEntity.name
            val typeName = resultFieldEntity.javaTypeName
            obtainResultMethodBuilder.addStatement(
                "result.\$L = \$T.<\$T>get(bundle, \$S)",
                name,
                BUNDLE_UTILS.javaTypeName,
                typeName,
                name
            )
        }
        obtainResultMethodBuilder.endControlFlow()
        obtainResultMethodBuilder.addStatement("return result")
        builder.addMethod(obtainResultMethodBuilder.build())


        // 生成 ResultContract
        val resultContractClassBuilder = TypeSpec.classBuilder("ResultContract")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        val builderClassTypeName =
            ClassName.get(activityClass.packageName, activityClass.builderClassName)
        val activityContractTypeName = ParameterizedTypeName.get(
            ACTIVITY_RESULT_CONTRACT.javaClassName,
            builderClassTypeName,
            resultClassName
        )
        resultContractClassBuilder.superclass(activityContractTypeName)
        resultContractClassBuilder.addMethod(
            MethodSpec.methodBuilder("createIntent")
                .addModifiers(Modifier.PUBLIC)
                .returns(INTENT.javaTypeName)
                .addAnnotation(NON_NULL.javaClassName)
                .addAnnotation(Override::class.java)
                .addParameter(
                    ParameterSpec.builder(CONTEXT.javaTypeName, "context")
                        .addAnnotation(NON_NULL.javaClassName)
                        .build()
                )
                .addParameter(builderClassTypeName, "input")
                .addStatement("return input.getIntent(context)")
                .build()
        )
        val parseResultMethodBuilder = MethodSpec.methodBuilder("parseResult")
            .addModifiers(Modifier.PUBLIC)
            .returns(resultClassName)
            .addAnnotation(Override::class.java)
            .addParameter(TypeName.INT, "resultCode")
            .addParameter(
                ParameterSpec.builder(INTENT.javaTypeName, "intent")
                    .addAnnotation(NULLABLE.javaClassName)
                    .build()
            )
            .addStatement("return obtainResult(resultCode, intent)")
        resultContractClassBuilder.addMethod(parseResultMethodBuilder.build())
        builder.addType(resultContractClassBuilder.build())

        // 生成 registerForActivityResult 方法
        val registerForActivityResultMethodBuilder =
            MethodSpec.methodBuilder("registerForActivityResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(
                    ParameterSpec.builder(ACTIVITY_RESULT_CALLER.javaTypeName, "resultCaller")
                        .addAnnotation(NON_NULL.javaClassName)
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder(
                        ParameterizedTypeName.get(
                            ACTIVITY_RESULT_CALLBACK.javaClassName,
                            resultClassName
                        ), "callback"
                    )
                        .addAnnotation(NON_NULL.javaClassName)
                        .build()
                )
                .returns(
                    ParameterizedTypeName.get(
                        ACTIVITY_RESULT_LAUNCHER.javaClassName,
                        builderClassTypeName
                    )
                )
                .addStatement("return resultCaller.registerForActivityResult(new ResultContract(), callback)")
        builder.addMethod(registerForActivityResultMethodBuilder.build())
    }

    private fun writeJavaToFile(activityClass: ActivityClass, typeSpec: TypeSpec) {
        try {
            val file = JavaFile.builder(activityClass.packageName, typeSpec).build()
            file.writeTo(AptContext.filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}