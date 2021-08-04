package com.qihuan.activitystarter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Arg
 *
 * @author qi
 * @since 2021/8/3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Arg {
    String value() default "";

    boolean required() default true;

    String stringValue() default "";

    char charValue() default '0';

    byte byteValue() default 0;

    short shortValue() default 0;

    int intValue() default 0;

    long longValue() default 0;

    float floatValue() default 0f;

    double doubleValue() default 0.0;

    boolean booleanValue() default false;
}
