package io.github.qihuan92.activitystarter.compiler.utils;

import com.squareup.javapoet.ClassName;

import io.github.qihuan92.activitystarter.compiler.entity.ClassType;

/**
 * PrebuiltTypes
 *
 * @author qi
 * @since 2021/8/4
 */
public interface PrebuiltTypes {
    ClassType CONTEXT = new ClassType("android.content.Context");
    ClassType INTENT = new ClassType("android.content.Intent");
    ClassType ACTIVITY = new ClassType("android.app.Activity");
    ClassType ACTIVITY_COMPAT = new ClassType("androidx.core.app.ActivityCompat");
    ClassType BUNDLE = new ClassType("android.os.Bundle");
    ClassType ACTIVITY_OPTIONS = new ClassType("androidx.core.app.ActivityOptionsCompat");
    ClassType BUNDLE_UTILS = new ClassType("io.github.qihuan92.activitystarter.utils.BundleUtils");

    ClassName ACTIVITY_RESULT_LAUNCHER = ClassName.get("androidx.activity.result", "ActivityResultLauncher");
    ClassName ACTIVITY_RESULT_CONTRACT = ClassName.get("androidx.activity.result.contract", "ActivityResultContract");
    ClassName ACTIVITY_RESULT_CALLER = ClassName.get("androidx.activity.result", "ActivityResultCaller");
    ClassName ACTIVITY_RESULT_CALLBACK = ClassName.get("androidx.activity.result", "ActivityResultCallback");
    ClassName NON_NULL = ClassName.get("androidx.annotation", "NonNull");
    ClassName NULLABLE = ClassName.get("androidx.annotation", "Nullable");
}
