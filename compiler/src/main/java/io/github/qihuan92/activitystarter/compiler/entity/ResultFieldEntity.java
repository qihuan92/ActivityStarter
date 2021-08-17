package io.github.qihuan92.activitystarter.compiler.entity;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

/**
 * ResultField
 *
 * @author qi
 * @since 2021/8/16
 */
public class ResultFieldEntity implements Comparable<ResultFieldEntity> {

    private final String name;
    private final TypeMirror type;

    public ResultFieldEntity(String name, TypeMirror type) {
        this.name = name;
        this.type = type;
    }

    public TypeName getTypeName() {
        return TypeName.get(type);
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(ResultFieldEntity o) {
        return name.compareTo(o.name);
    }
}
