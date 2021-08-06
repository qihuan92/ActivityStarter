package io.github.qihuan92.activitystarter.compiler.utils;

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
    ClassType BUNDLE = new ClassType("android.os.Bundle");
    ClassType BUNDLE_UTILS = new ClassType("io.github.qihuan92.activitystarter.utils.BundleUtils");
}
