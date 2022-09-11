package io.github.qihuan92.activitystarter.compiler.utils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * AptContext
 *
 * @author qi
 * @since 2021/8/4
 */
public class AptContext {
    private Types types;
    private Elements elements;
    private Messager messager;
    private Filer filer;

    private static class AptContextHolder {
        private static final AptContext INSTANCE = new AptContext();
    }

    private AptContext() {
    }

    public static AptContext getInstance() {
        return AptContextHolder.INSTANCE;
    }

    public void init(ProcessingEnvironment env) {
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        messager = env.getMessager();
        filer = env.getFiler();
    }

    public Types getTypes() {
        return types;
    }

    public Elements getElements() {
        return elements;
    }

    public Messager getMessager() {
        return messager;
    }

    public Filer getFiler() {
        return filer;
    }
}
