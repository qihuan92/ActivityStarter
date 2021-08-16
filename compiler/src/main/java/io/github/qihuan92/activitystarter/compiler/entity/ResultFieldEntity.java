package io.github.qihuan92.activitystarter.compiler.entity;

import javax.lang.model.type.TypeMirror;

/**
 * ResultField
 *
 * @author qi
 * @since 2021/8/16
 */
public class ResultFieldEntity implements Comparable<ResultFieldEntity> {

    private String name;
    private TypeMirror type;

    public ResultFieldEntity(String name, TypeMirror type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeMirror getType() {
        return type;
    }

    public void setType(TypeMirror type) {
        this.type = type;
    }

    @Override
    public int compareTo(ResultFieldEntity o) {
        return name.compareTo(o.name);
    }
}
