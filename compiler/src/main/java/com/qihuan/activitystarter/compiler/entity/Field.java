package com.qihuan.activitystarter.compiler.entity;

import com.qihuan.activitystarter.annotation.Extra;
import com.qihuan.activitystarter.compiler.utils.StringUtils;
import com.qihuan.activitystarter.compiler.utils.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;

import java.util.Locale;

import javax.lang.model.type.TypeKind;

/**
 * ArgInfo
 *
 * @author qi
 * @since 2021/8/3
 */
public class Field implements Comparable<Field> {
    public static final String CONST_EXTRA_PREFIX = "EXTRA_";

    private final Symbol.VarSymbol symbol;
    private final String name;
    private final boolean required;
    private Object defaultValue;

    public Field(Symbol.VarSymbol symbol) {
        this.symbol = symbol;

        Extra extraAnnotation = symbol.getAnnotation(Extra.class);
        String name = extraAnnotation.value();

        this.required = extraAnnotation.required();
        if (!name.isEmpty()) {
            this.name = name;
        } else {
            this.name = symbol.getQualifiedName().toString();
        }
        setDefaultValue(extraAnnotation, symbol);
    }

    private void setDefaultValue(Extra extraAnnotation, Symbol.VarSymbol symbol) {
        TypeKind kind = symbol.type.getKind();
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
                if (TypeUtils.isSameType(symbol.type, String.class)) {
                    this.defaultValue = String.format("\"%s\"", extraAnnotation.stringValue());
                }
                break;
        }
    }

    public Symbol.VarSymbol getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public TypeName asTypeName() {
        return ClassName.get(symbol.type);
    }

    public String getConstFieldName() {
        return CONST_EXTRA_PREFIX + StringUtils.camelToUnderline(name).toUpperCase();
    }

    public boolean isPrivate() {
        return symbol.isPrivate();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int compareTo(Field other) {
        return name.compareTo(other.name);
    }
}
