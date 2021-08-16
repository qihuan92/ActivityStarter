package io.github.qihuan92.activitystarter.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ResultField
 *
 * @author qi
 * @since 2021/8/16
 */
@Retention(RetentionPolicy.CLASS)
public @interface ResultField {
    String name();

    Class<?> type();
}
