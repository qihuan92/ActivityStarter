package com.qihuan.activitystarter.compiler.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * ElementUtils
 *
 * @author qi
 * @since 2021/8/4
 */
public class ElementUtils {
    public static String getPackageName(TypeElement typeElement) {
        Element element = typeElement.getEnclosingElement();
        while (element != null && element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        if (element == null) {
            throw new IllegalArgumentException(typeElement + " does not have an enclosing element of package.");
        }
        return element.asType().toString();
    }
}
