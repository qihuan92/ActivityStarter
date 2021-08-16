package io.github.qihuan92.activitystarter.compiler.entity;

import com.squareup.javapoet.TypeName;

import io.github.qihuan92.activitystarter.compiler.utils.TypeUtils;

/**
 * ClassType
 *
 * @author qi
 * @since 2021/8/4
 */
public class ClassType {
    private final String className;

    public ClassType(String className) {
        this.className = className;
    }

    public TypeName java() {
        return TypeUtils.getJavaTypeName(className);
    }
}
