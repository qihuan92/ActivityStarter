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
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import io.github.qihuan92.activitystarter.annotation.Builder;
import io.github.qihuan92.activitystarter.annotation.ResultField;
import io.github.qihuan92.activitystarter.compiler.utils.ElementUtils;
import io.github.qihuan92.activitystarter.compiler.utils.TypeUtils;

/**
 * ActivityClass
 *
 * @author qi
 * @since 2021/8/3
 */
public class ActivityClass {
    public static final String POSIX = "Builder";

    private final TypeElement typeElement;
    private final Set<RequestFieldEntity> requestFieldEntities = new TreeSet<>();
    private final Set<ResultFieldEntity> resultFieldEntities = new TreeSet<>();

    public ActivityClass(TypeElement typeElement) {
        this.typeElement = typeElement;

        Builder builderAnnotation = typeElement.getAnnotation(Builder.class);
        ResultField[] resultFields = builderAnnotation.resultFields();
        if (resultFields.length > 0) {
            for (ResultField resultField : resultFields) {
                TypeMirror typeMirror;
                try {
                    typeMirror = TypeUtils.getTypeMirror(resultField.type());
                } catch (MirroredTypeException e) {
                    typeMirror = e.getTypeMirror();
                }
                resultFieldEntities.add(new ResultFieldEntity(resultField.name(), typeMirror));
            }
        }
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public Set<RequestFieldEntity> getRequestFields() {
        return requestFieldEntities;
    }

    public Set<ResultFieldEntity> getResultFieldEntities() {
        return resultFieldEntities;
    }

    public void addFiled(RequestFieldEntity requestFieldEntity) {
        requestFieldEntities.add(requestFieldEntity);
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
