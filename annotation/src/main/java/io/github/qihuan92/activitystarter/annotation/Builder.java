package io.github.qihuan92.activitystarter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Builder
 *
 * @author qi
 * @since 2021/8/3
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Builder {
    ResultField[] resultFields() default {};
}
