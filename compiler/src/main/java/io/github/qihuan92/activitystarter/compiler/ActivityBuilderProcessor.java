package io.github.qihuan92.activitystarter.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.annotation.Extra;
import io.github.qihuan92.activitystarter.annotation.Generated;
import io.github.qihuan92.activitystarter.compiler.entity.ActivityClass;
import io.github.qihuan92.activitystarter.compiler.entity.RequestFieldEntity;
import io.github.qihuan92.activitystarter.compiler.entity.ResultFieldEntity;
import io.github.qihuan92.activitystarter.compiler.utils.AptContext;
import io.github.qihuan92.activitystarter.compiler.utils.PrebuiltTypes;
import io.github.qihuan92.activitystarter.compiler.utils.TypeUtils;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ActivityBuilderProcessor extends AbstractProcessor {

    private final Map<Element, ActivityClass> activityClasses = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        AptContext.getInstance().init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Builder.class.getCanonicalName());
        types.add(Extra.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return true;
        }
        parseClass(roundEnv);
        parseFields(roundEnv);
        buildFiles();
        return true;
    }

    private void parseClass(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Builder.class)
                .stream()
                .filter(element -> element.getKind().isClass())
                .forEach(element -> {
                    if (TypeUtils.isSubType(element.asType(), "android.app.Activity")) {
                        activityClasses.put(element, new ActivityClass((TypeElement) element));
                    }
                });
    }

    private void parseFields(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Extra.class)
                .stream()
                .filter(element -> element.getKind().isField())
                .forEach(element -> {
                    ActivityClass activityClass = activityClasses.get(element.getEnclosingElement());
                    if (activityClass != null) {
                        activityClass.addFiled(new RequestFieldEntity((VariableElement) element));
                    }
                });
    }

    private void buildFiles() {
        activityClasses.forEach((element, activityClass) -> buildFile(activityClass));
    }

    private void buildFile(ActivityClass activityClass) {
        if (activityClass.isAbstract()) {
            return;
        }
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(activityClass.getBuilderClassName())
                .addAnnotation(Generated.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        buildClass(activityClass, typeBuilder);
        writeJavaToFile(activityClass, typeBuilder.build());
    }

    private void buildClass(ActivityClass activityClass, TypeSpec.Builder builder) {
        buildConstant(activityClass, builder);
        buildField(activityClass, builder);
        buildIntentMethod(activityClass, builder);
        buildInjectMethod(activityClass, builder);
        buildSaveStateMethod(activityClass, builder);
        buildNewIntentMethod(activityClass, builder);
        buildStartMethod(builder);
        buildFinishMethod(activityClass, builder);
        buildResultContractTypes(activityClass, builder);
        new KotlinExtGenerator(activityClass).execute();
    }

    private void buildConstant(ActivityClass activityClass, TypeSpec.Builder builder) {
        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            builder.addField(
                    FieldSpec.builder(String.class, requestFieldEntity.getConstFieldName(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", requestFieldEntity.getName())
                            .build()
            );
        }
    }

    private void buildField(ActivityClass activityClass, TypeSpec.Builder builder) {
        ClassName builderClassTypeName = ClassName.get(activityClass.getPackageName(), activityClass.getBuilderClassName());

        // 创建 builder() 方法
        MethodSpec.Builder builderMethodBuilder = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderClassTypeName)
                .addStatement("$T builder = new $T()", builderClassTypeName, builderClassTypeName);

        // 必传参数注释
        CodeBlock.Builder requiredParamDocsBuilder = CodeBlock.builder();

        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            // 变量
            builder.addField(
                    FieldSpec.builder(requestFieldEntity.asTypeName(), requestFieldEntity.getName(), Modifier.PRIVATE).build()
            );

            if (requestFieldEntity.isRequired()) {
                // 添加到 builder() 参数
                builderMethodBuilder.addParameter(
                        ParameterSpec.builder(requestFieldEntity.asTypeName(), requestFieldEntity.getName())
                                .build()
                );

                // 变量赋值
                builderMethodBuilder.addStatement("builder.$L = $L", requestFieldEntity.getName(), requestFieldEntity.getName());

                // 注释
                requiredParamDocsBuilder.add("@param $L $L", requestFieldEntity.getName(), requestFieldEntity.getDescription())
                        .add("\n");
            } else {
                // setter
                builder.addMethod(
                        MethodSpec.methodBuilder(requestFieldEntity.getName())
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(requestFieldEntity.asTypeName(), requestFieldEntity.getName())
                                .addJavadoc("@param $L $L", requestFieldEntity.getName(), requestFieldEntity.getDescription())
                                .addStatement("this.$L = $L", requestFieldEntity.getName(), requestFieldEntity.getName())
                                .addStatement("return this")
                                .returns(builderClassTypeName)
                                .build()
                );
            }

        }

        // 为 builder() 方法添加注释
        builderMethodBuilder.addJavadoc(requiredParamDocsBuilder.build());

        builder.addMethod(builderMethodBuilder.addStatement("return builder").build());
    }

    private void buildIntentMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        MethodSpec.Builder intentMethodBuilder = MethodSpec.methodBuilder("getIntent")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.CONTEXT.typeName(), "context")
                .returns(PrebuiltTypes.INTENT.typeName())
                .addStatement("$T intent = new $T(context, $T.class)", PrebuiltTypes.INTENT.typeName(), PrebuiltTypes.INTENT.typeName(), activityClass.getTypeElement());
        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            intentMethodBuilder.addStatement("intent.putExtra($L, $L)", requestFieldEntity.getConstFieldName(), requestFieldEntity.getName());
        }
        intentMethodBuilder.addStatement("return intent");
        builder.addMethod(intentMethodBuilder.build());
    }

    private void buildInjectMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(PrebuiltTypes.ACTIVITY.typeName(), "instance")
                .addParameter(PrebuiltTypes.BUNDLE.typeName(), "savedInstanceState")
                .beginControlFlow("if(instance instanceof $T)", activityClass.getTypeElement())
                .addStatement("$T typedInstance = ($T) instance", activityClass.getTypeElement(), activityClass.getTypeElement())
                .beginControlFlow("if(savedInstanceState != null)");

        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            String name = requestFieldEntity.getName();
            TypeName typeName = requestFieldEntity.asTypeName().box();
            injectMethodBuilder.addStatement("typedInstance.$L = $T.<$T>get(savedInstanceState, $S, $L)",
                    name,
                    PrebuiltTypes.BUNDLE_UTILS.typeName(),
                    typeName,
                    name,
                    requestFieldEntity.getDefaultValue());
        }

        injectMethodBuilder.endControlFlow().endControlFlow();
        builder.addMethod(injectMethodBuilder.build());
    }

    private void buildSaveStateMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        MethodSpec.Builder saveStateMethodBuilder = MethodSpec.methodBuilder("saveState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(PrebuiltTypes.ACTIVITY.typeName(), "instance")
                .addParameter(PrebuiltTypes.BUNDLE.typeName(), "outState")
                .beginControlFlow("if(instance instanceof $T)", activityClass.getTypeElement())
                .addStatement("$T typedInstance = ($T) instance", activityClass.getTypeElement(), activityClass.getTypeElement())
                .addStatement("$T intent = new $T()", PrebuiltTypes.INTENT.typeName(), PrebuiltTypes.INTENT.typeName());

        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            String name = requestFieldEntity.getName();
            saveStateMethodBuilder.addStatement("intent.putExtra($S, typedInstance.$L)", name, name);
        }

        saveStateMethodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow();
        builder.addMethod(saveStateMethodBuilder.build());
    }

    private void buildNewIntentMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        MethodSpec.Builder newIntentMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(activityClass.getTypeElement().asType()), "activity")
                .addParameter(PrebuiltTypes.INTENT.typeName(), "intent");
        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)");

        builder.addMethod(newIntentMethodBuilder.build());

        MethodSpec.Builder newIntentWithUpdateMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(activityClass.getTypeElement().asType()), "activity")
                .addParameter(PrebuiltTypes.INTENT.typeName(), "intent")
                .addParameter(Boolean.class, "updateIntent");

        newIntentWithUpdateMethodBuilder.beginControlFlow("if(updateIntent)")
                .addStatement("activity.setIntent(intent)")
                .endControlFlow();

        newIntentWithUpdateMethodBuilder.beginControlFlow("if(intent != null)")
                .addStatement("inject(activity, intent.getExtras())")
                .endControlFlow();

        builder.addMethod(newIntentWithUpdateMethodBuilder.build());
    }

    private void buildStartMethod(TypeSpec.Builder builder) {
        builder.addMethod(startMethodBuilder(false).build());
        builder.addMethod(startMethodBuilder(true).build());
        builder.addMethod(startForResultMethodBuilder(false).build());
        builder.addMethod(startForResultMethodBuilder(true).build());
    }

    private MethodSpec.Builder startMethodBuilder(boolean withOptions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.CONTEXT.typeName(), "context")
                .addStatement("$T intent = getIntent(context)", PrebuiltTypes.INTENT.typeName());

        builder.beginControlFlow("if(!(context instanceof Activity))");
        builder.addStatement("intent.addFlags($T.FLAG_ACTIVITY_NEW_TASK)", PrebuiltTypes.INTENT.typeName());
        builder.endControlFlow();

        if (withOptions) {
            builder.addParameter(PrebuiltTypes.BUNDLE.typeName(), "options");
            builder.addStatement("context.startActivity(intent, options)");
        } else {
            builder.addStatement("context.startActivity(intent)");
        }
        return builder;
    }

    private MethodSpec.Builder startForResultMethodBuilder(boolean withOptions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.ACTIVITY.typeName(), "activity")
                .addParameter(TypeName.INT, "requestCode")
                .addStatement("$T intent = getIntent(activity)", PrebuiltTypes.INTENT.typeName());

        if (withOptions) {
            builder.addParameter(PrebuiltTypes.BUNDLE.typeName(), "options");
            builder.addStatement("activity.startActivityForResult(intent, requestCode, options)");
        } else {
            builder.addStatement("activity.startActivityForResult(intent, requestCode)");
        }

        return builder;
    }

    private void buildFinishMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        Set<ResultFieldEntity> resultFieldEntities = activityClass.getResultFieldEntities();

        // 生成返回方法
        MethodSpec.Builder finishMethodBuilder = MethodSpec.methodBuilder("finish")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(PrebuiltTypes.ACTIVITY.typeName(), "activity")
                .returns(TypeName.VOID);

        if (!resultFieldEntities.isEmpty()) {
            finishMethodBuilder.addStatement("$T intent = new $T()", PrebuiltTypes.INTENT.typeName(), PrebuiltTypes.INTENT.typeName());
            finishMethodBuilder.addStatement("activity.setResult($T.RESULT_OK, intent)", PrebuiltTypes.ACTIVITY.typeName());
            for (ResultFieldEntity resultFieldEntity : resultFieldEntities) {
                String name = resultFieldEntity.getName();
                TypeName typeName = resultFieldEntity.getTypeName();
                finishMethodBuilder.addParameter(typeName, name);

                finishMethodBuilder.addStatement("intent.putExtra($S, $L)", name, name);
            }
        }

        finishMethodBuilder.addStatement("$T.finishAfterTransition(activity)", PrebuiltTypes.ACTIVITY_COMPAT.typeName());
        builder.addMethod(finishMethodBuilder.build());
    }

    private void buildResultContractTypes(ActivityClass activityClass, TypeSpec.Builder builder) {
        Set<ResultFieldEntity> resultFieldEntities = activityClass.getResultFieldEntities();
        if (resultFieldEntities.isEmpty()) {
            return;
        }
        // 生成返回结果实体类
        TypeSpec.Builder resultClassBuilder = TypeSpec.classBuilder("Result")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(TypeName.INT, "resultCode", Modifier.PUBLIC);
        for (ResultFieldEntity resultFieldEntity : resultFieldEntities) {
            resultClassBuilder.addField(resultFieldEntity.getTypeName(), resultFieldEntity.getName(), Modifier.PUBLIC);
        }
        builder.addType(resultClassBuilder.build());

        ClassName resultClassName = ClassName.bestGuess("Result");

        // 生成结果解析方法
        MethodSpec.Builder obtainResultMethodBuilder = MethodSpec.methodBuilder("obtainResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.INT, "resultCode")
                .addParameter(PrebuiltTypes.INTENT.typeName(), "intent")
                .returns(resultClassName)
                .addStatement("$T result = new $T()", resultClassName, resultClassName)
                .addStatement("result.resultCode = resultCode")
                .beginControlFlow("if(intent != null)")
                .addStatement("$T bundle = intent.getExtras()", PrebuiltTypes.BUNDLE.typeName());
        for (ResultFieldEntity resultFieldEntity : resultFieldEntities) {
            String name = resultFieldEntity.getName();
            TypeName typeName = resultFieldEntity.getTypeName();
            obtainResultMethodBuilder.addStatement("result.$L = $T.<$T>get(bundle, $S)", name, PrebuiltTypes.BUNDLE_UTILS.typeName(), typeName, name);
        }
        obtainResultMethodBuilder.endControlFlow();
        obtainResultMethodBuilder.addStatement("return result");
        builder.addMethod(obtainResultMethodBuilder.build());


        // 生成 ResultContract
        TypeSpec.Builder resultContractClassBuilder = TypeSpec.classBuilder("ResultContract")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        ClassName builderClassTypeName = ClassName.get(activityClass.getPackageName(), activityClass.getBuilderClassName());
        ParameterizedTypeName activityContractTypeName = ParameterizedTypeName.get(PrebuiltTypes.ACTIVITY_RESULT_CONTRACT, builderClassTypeName, resultClassName);
        resultContractClassBuilder.superclass(activityContractTypeName);

        resultContractClassBuilder.addMethod(MethodSpec.methodBuilder("createIntent")
                .addModifiers(Modifier.PUBLIC)
                .returns(PrebuiltTypes.INTENT.typeName())
                .addAnnotation(PrebuiltTypes.NON_NULL)
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(PrebuiltTypes.CONTEXT.typeName(), "context")
                        .addAnnotation(PrebuiltTypes.NON_NULL)
                        .build())
                .addParameter(builderClassTypeName, "input")
                .addStatement("return input.getIntent(context)")
                .build());

        MethodSpec.Builder parseResultMethodBuilder = MethodSpec.methodBuilder("parseResult")
                .addModifiers(Modifier.PUBLIC)
                .returns(resultClassName)
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "resultCode")
                .addParameter(ParameterSpec.builder(PrebuiltTypes.INTENT.typeName(), "intent")
                        .addAnnotation(PrebuiltTypes.NULLABLE)
                        .build())
                .addStatement("return obtainResult(resultCode, intent)");
        resultContractClassBuilder.addMethod(parseResultMethodBuilder.build());

        builder.addType(resultContractClassBuilder.build());

        // 生成 registerForActivityResult 方法
        MethodSpec.Builder registerForActivityResultMethodBuilder = MethodSpec.methodBuilder("registerForActivityResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(PrebuiltTypes.ACTIVITY_RESULT_CALLER, "resultCaller")
                        .addAnnotation(PrebuiltTypes.NON_NULL)
                        .build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(PrebuiltTypes.ACTIVITY_RESULT_CALLBACK, resultClassName), "callback")
                        .addAnnotation(PrebuiltTypes.NON_NULL)
                        .build())
                .returns(ParameterizedTypeName.get(PrebuiltTypes.ACTIVITY_RESULT_LAUNCHER, builderClassTypeName))
                .addStatement("return resultCaller.registerForActivityResult(new ResultContract(), callback)");
        builder.addMethod(registerForActivityResultMethodBuilder.build());
    }

    private void writeJavaToFile(ActivityClass activityClass, TypeSpec typeSpec) {
        try {
            JavaFile file = JavaFile.builder(activityClass.getPackageName(), typeSpec).build();
            file.writeTo(AptContext.getInstance().getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}