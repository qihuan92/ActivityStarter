package io.github.qihuan92.activitystarter.compiler;

import com.squareup.javapoet.ClassName;
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
        buildStartMethod(activityClass, builder);
        buildFinishMethod(activityClass, builder);
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
            } else {
                // setter
                builder.addMethod(
                        MethodSpec.methodBuilder(requestFieldEntity.getName())
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(requestFieldEntity.asTypeName(), requestFieldEntity.getName())
                                .addStatement("this.$L = $L", requestFieldEntity.getName(), requestFieldEntity.getName())
                                .addStatement("return this")
                                .returns(builderClassTypeName)
                                .build()
                );
            }

        }

        builder.addMethod(builderMethodBuilder.addStatement("return builder").build());
    }

    private void buildIntentMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        MethodSpec.Builder intentMethodBuilder = MethodSpec.methodBuilder("getIntent")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.CONTEXT.java(), "context")
                .returns(PrebuiltTypes.INTENT.java())
                .addStatement("$T intent = new $T(context, $T.class)", PrebuiltTypes.INTENT.java(), PrebuiltTypes.INTENT.java(), activityClass.getTypeElement());
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
                .addParameter(PrebuiltTypes.ACTIVITY.java(), "instance")
                .addParameter(PrebuiltTypes.BUNDLE.java(), "savedInstanceState")
                .beginControlFlow("if(instance instanceof $T)", activityClass.getTypeElement())
                .addStatement("$T typedInstance = ($T) instance", activityClass.getTypeElement(), activityClass.getTypeElement())
                .beginControlFlow("if(savedInstanceState != null)");

        Set<RequestFieldEntity> requestFieldEntities = activityClass.getRequestFields();
        for (RequestFieldEntity requestFieldEntity : requestFieldEntities) {
            String name = requestFieldEntity.getName();
            TypeName typeName = requestFieldEntity.asTypeName().box();
            injectMethodBuilder.addStatement("typedInstance.$L = $T.<$T>get(savedInstanceState, $S, $L)",
                    name,
                    PrebuiltTypes.BUNDLE_UTILS.java(),
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
                .addParameter(PrebuiltTypes.ACTIVITY.java(), "instance")
                .addParameter(PrebuiltTypes.BUNDLE.java(), "outState")
                .beginControlFlow("if(instance instanceof $T)", activityClass.getTypeElement())
                .addStatement("$T typedInstance = ($T) instance", activityClass.getTypeElement(), activityClass.getTypeElement())
                .addStatement("$T intent = new $T()", PrebuiltTypes.INTENT.java(), PrebuiltTypes.INTENT.java());

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
                .addParameter(PrebuiltTypes.INTENT.java(), "intent");
        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)");

        builder.addMethod(newIntentMethodBuilder.build());

        MethodSpec.Builder newIntentWithUpdateMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(activityClass.getTypeElement().asType()), "activity")
                .addParameter(PrebuiltTypes.INTENT.java(), "intent")
                .addParameter(Boolean.class, "updateIntent");

        newIntentWithUpdateMethodBuilder.beginControlFlow("if(updateIntent)")
                .addStatement("activity.setIntent(intent)")
                .endControlFlow();

        newIntentWithUpdateMethodBuilder.beginControlFlow("if(intent != null)")
                .addStatement("inject(activity, intent.getExtras())")
                .endControlFlow();

        builder.addMethod(newIntentWithUpdateMethodBuilder.build());
    }

    private void buildStartMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        builder.addMethod(startMethodBuilder(false).build());
        builder.addMethod(startMethodBuilder(true).build());
        builder.addMethod(startForResultMethodBuilder(false).build());
        builder.addMethod(startForResultMethodBuilder(true).build());
        if (!activityClass.getResultFieldEntities().isEmpty()) {
            builder.addMethod(startLauncherMethodBuilder(false).build());
            builder.addMethod(startLauncherMethodBuilder(true).build());
        }
    }

    private MethodSpec.Builder startMethodBuilder(boolean withOptions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.CONTEXT.java(), "context")
                .addStatement("$T intent = getIntent(context)", PrebuiltTypes.INTENT.java());

        builder.beginControlFlow("if(!(context instanceof Activity))");
        builder.addStatement("intent.addFlags($T.FLAG_ACTIVITY_NEW_TASK)", PrebuiltTypes.INTENT.java());
        builder.endControlFlow();

        if (withOptions) {
            builder.addParameter(PrebuiltTypes.BUNDLE.java(), "options");
            builder.addStatement("context.startActivity(intent, options)");
        } else {
            builder.addStatement("context.startActivity(intent)");
        }
        return builder;
    }

    private MethodSpec.Builder startForResultMethodBuilder(boolean withOptions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.ACTIVITY.java(), "activity")
                .addParameter(TypeName.INT, "requestCode")
                .addStatement("$T intent = getIntent(activity)", PrebuiltTypes.INTENT.java());

        if (withOptions) {
            builder.addParameter(PrebuiltTypes.BUNDLE.java(), "options");
            builder.addStatement("activity.startActivityForResult(intent, requestCode, options)");
        } else {
            builder.addStatement("activity.startActivityForResult(intent, requestCode)");
        }

        return builder;
    }

    private MethodSpec.Builder startLauncherMethodBuilder(boolean withOptions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PrebuiltTypes.CONTEXT.java(), "context")
                .addStatement("$T intent = getIntent(context)", PrebuiltTypes.INTENT.java());

        builder.beginControlFlow("if(!(context instanceof Activity))");
        builder.addStatement("intent.addFlags($T.FLAG_ACTIVITY_NEW_TASK)", PrebuiltTypes.INTENT.java());
        builder.endControlFlow();

        if (withOptions) {
            builder.addParameter(PrebuiltTypes.ACTIVITY_OPTIONS.java(), "options");
            builder.addStatement("launcher.launch(intent, options)");
        } else {
            builder.addStatement("launcher.launch(intent)");
        }

        ClassName launcherTypeNameClassName = ClassName.get("androidx.activity.result", "ActivityResultLauncher");
        ParameterizedTypeName launcherTypeName = ParameterizedTypeName.get(launcherTypeNameClassName, PrebuiltTypes.INTENT.java());
        ParameterSpec LauncherParameterSpec = ParameterSpec.builder(launcherTypeName, "launcher")
                .build();
        builder.addParameter(LauncherParameterSpec);

        return builder;
    }

    private void buildFinishMethod(ActivityClass activityClass, TypeSpec.Builder builder) {
        // todo 生成结果数据
        // builder.addType();
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