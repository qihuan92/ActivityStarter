package io.github.qihuan92.activitystarter.compiler.utils;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * TypeUtils
 *
 * @author qi
 * @since 2021/8/4
 */
public class TypeUtils {
    public static TypeMirror getTypeFromClassName(String className) {
        Elements elements = AptContext.getInstance().getElements();
        return elements.getTypeElement(className).asType();
    }

    public static TypeMirror getTypeMirror(Class<?> type) {
        Elements elements = AptContext.getInstance().getElements();
        return elements.getTypeElement(type.getCanonicalName()).asType();
    }

    public static TypeName getTypeName(String className) {
        Types types = AptContext.getInstance().getTypes();
        TypeMirror typeMirror = getTypeFromClassName(className);
        types.erasure(typeMirror);
        return TypeName.get(typeMirror);
    }

    public static boolean isSubType(TypeMirror t1, TypeMirror t2) {
        Types types = AptContext.getInstance().getTypes();
        return types.isSubtype(t1, t2);
    }

    public static boolean isSubType(TypeMirror typeMirror, String className) {
        TypeMirror t2 = AptContext.getInstance().getElements().getTypeElement(className).asType();
        return isSubType(typeMirror, t2);
    }

    public static boolean isSameType(TypeMirror type, Class<?> clazz) {
        return isSameType(type, clazz.getCanonicalName());
    }

    public static boolean isSameType(TypeMirror type, String className) {
        Types types = AptContext.getInstance().getTypes();
        return types.isSameType(type, getTypeFromClassName(className));
    }
}
