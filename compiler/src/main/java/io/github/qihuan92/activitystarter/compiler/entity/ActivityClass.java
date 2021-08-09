package io.github.qihuan92.activitystarter.compiler.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.github.qihuan92.activitystarter.compiler.utils.ElementUtils;

/**
 * ActivityClass
 *
 * @author qi
 * @since 2021/8/3
 */
public class ActivityClass {
    public static final String POSIX = "Builder";

    private TypeElement typeElement;
    private Set<Field> fields = new TreeSet<>();

    public ActivityClass(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void setFields(Set<Field> fields) {
        this.fields = fields;
    }

    public void addFiled(Field field) {
        fields.add(field);
    }

    public String getBuilderClassName() {
        List<String> list = new ArrayList<>();
        list.add(typeElement.getSimpleName().toString());
        Element element = typeElement.getEnclosingElement();
        while (element != null && element.getKind() != ElementKind.PACKAGE) {
            list.add(element.getSimpleName().toString());
            element = element.getEnclosingElement();
        }
        Collections.reverse(list);
        return String.join("_", list) + POSIX;
    }

    public String getPackageName() {
        return ElementUtils.getPackageName(typeElement);
    }

    public boolean isAbstract() {
        return typeElement.getModifiers().contains(Modifier.ABSTRACT);
    }
}
