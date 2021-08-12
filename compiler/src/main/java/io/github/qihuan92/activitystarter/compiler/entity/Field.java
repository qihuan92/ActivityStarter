package io.github.qihuan92.activitystarter.compiler.entity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Locale;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import io.github.qihuan92.activitystarter.annotation.Extra;
import io.github.qihuan92.activitystarter.compiler.utils.StringUtils;
import io.github.qihuan92.activitystarter.compiler.utils.TypeUtils;

/**
 * Field
 *
 * @author qi
 * @since 2021/8/3
 */
public class Field implements Comparable<Field> {
    public static final String CONST_EXTRA_PREFIX = "EXTRA_";

    private final VariableElement variableElement;
    private final String name;
    private final boolean required;
    private Object defaultValue;

    public Field(VariableElement variableElement) {
        this.variableElement = variableElement;

        Extra extraAnnotation = variableElement.getAnnotation(Extra.class);
        String name = extraAnnotation.value();

        this.required = extraAnnotation.required();
        if (!name.isEmpty()) {
            this.name = name;
        } else {
            this.name = variableElement.getSimpleName().toString();
        }
        setDefaultValue(extraAnnotation, variableElement);
    }

    private void setDefaultValue(Extra extraAnnotation, VariableElement variableElement) {
        TypeKind kind = variableElement.asType().getKind();
        switch (kind) {
            case CHAR:
                this.defaultValue = String.format("'%c'", extraAnnotation.charValue());
                break;
            case BYTE:
                this.defaultValue = String.format(Locale.getDefault(), "(byte) %d", extraAnnotation.byteValue());
                break;
            case SHORT:
                this.defaultValue = String.format(Locale.getDefault(), "(short) %d", extraAnnotation.shortValue());
                break;
            case INT:
                this.defaultValue = extraAnnotation.intValue();
                break;
            case LONG:
                this.defaultValue = String.format(Locale.getDefault(), "%dL", extraAnnotation.longValue());
                break;
            case FLOAT:
                this.defaultValue = String.format(Locale.getDefault(), "%ff", extraAnnotation.floatValue());
                break;
            case DOUBLE:
                this.defaultValue = extraAnnotation.doubleValue();
                break;
            case BOOLEAN:
                this.defaultValue = extraAnnotation.booleanValue();
                break;
            default:
                if (TypeUtils.isSameType(variableElement.asType(), String.class)) {
                    this.defaultValue = String.format("\"%s\"", extraAnnotation.stringValue());
                }
                break;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public TypeName asTypeName() {
        return ClassName.get(variableElement.asType());
    }

    public String getConstFieldName() {
        return CONST_EXTRA_PREFIX + StringUtils.camelToUnderline(name).toUpperCase();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int compareTo(Field other) {
        return name.compareTo(other.name);
    }
}
