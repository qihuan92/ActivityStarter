package com.qihuan.activitystarter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * BuilderClassFinder
 *
 * @author qi
 * @since 2021/8/4
 */
class BuilderClassFinder {
    private static final String BUILDER_NAME_POSIX = "Builder";
    private static final HashMap<String, Class<?>> builderClassCache = new HashMap<>();

    public static Class<?> findBuilderClass(Object object) throws ClassNotFoundException {
        Class<?> cls = object.getClass();
        Class<?> builderClass = builderClassCache.get(cls.getCanonicalName());
        if (builderClass == null) {
            builderClass = Class.forName(findBuilderClassName(object));
            builderClassCache.put(cls.getCanonicalName(), builderClass);
        }
        return builderClass;
    }

    static String findBuilderClassName(Object object) {
        Class<?> cls = object.getClass();
        Class<?> enclosingClass = cls.getEnclosingClass();
        ArrayList<String> names = new ArrayList<>();
        names.add(cls.getSimpleName());
        while (enclosingClass != null) {
            names.add(enclosingClass.getSimpleName());
            enclosingClass = enclosingClass.getEnclosingClass();
        }
        StringBuilder stringBuilder = new StringBuilder();
        Package clsPackage = cls.getPackage();
        if (clsPackage != null) {
            stringBuilder.append(clsPackage.getName()).append(".");
        }
        for (int i = names.size() - 1; i >= 0; i--) {
            stringBuilder.append(names.get(i));
            if (i != 0) {
                stringBuilder.append("_");
            }
        }
        stringBuilder.append(BUILDER_NAME_POSIX);
        return stringBuilder.toString();
    }
}
