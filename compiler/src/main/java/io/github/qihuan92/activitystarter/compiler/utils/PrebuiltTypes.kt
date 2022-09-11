package io.github.qihuan92.activitystarter.compiler.utils

/**
 * PrebuiltTypes
 *
 * @author qi
 * @since 2021/8/4
 */
val CONTEXT = ClassType("android.content.Context")
val INTENT = ClassType("android.content.Intent")
val ACTIVITY = ClassType("android.app.Activity")
val ACTIVITY_COMPAT = ClassType("androidx.core.app.ActivityCompat")
val BUNDLE = ClassType("android.os.Bundle")
val ACTIVITY_OPTIONS = ClassType("androidx.core.app.ActivityOptionsCompat")
val BUNDLE_UTILS = ClassType("io.github.qihuan92.activitystarter.utils.BundleUtils")
val ACTIVITY_RESULT_LAUNCHER = ClassType("androidx.activity.result.ActivityResultLauncher")
val ACTIVITY_RESULT_CONTRACT = ClassType("androidx.activity.result.contract.ActivityResultContract")
val ACTIVITY_RESULT_CALLER = ClassType("androidx.activity.result.ActivityResultCaller")
val ACTIVITY_RESULT_CALLBACK = ClassType("androidx.activity.result.ActivityResultCallback")
val NON_NULL = ClassType("androidx.annotation.NonNull")
val NULLABLE = ClassType("androidx.annotation.Nullable")