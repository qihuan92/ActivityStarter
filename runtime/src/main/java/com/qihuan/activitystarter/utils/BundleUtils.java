package com.qihuan.activitystarter.utils;

import android.os.Bundle;

public class BundleUtils {

    @SuppressWarnings("unchecked")
    public static <T> T get(Bundle bundle, String key) {
        return (T) bundle.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Bundle bundle, String key, Object defaultValue) {
        Object obj = bundle.get(key);
        if (obj == null) {
            obj = defaultValue;
        }
        return (T) obj;
    }
}
