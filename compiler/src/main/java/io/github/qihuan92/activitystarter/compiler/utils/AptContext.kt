package io.github.qihuan92.activitystarter.compiler.utils

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * AptContext
 *
 * @author qi
 * @since 2021/8/4
 */
object AptContext {

    lateinit var types: Types
        private set
    lateinit var elements: Elements
        private set
    lateinit var messager: Messager
        private set
    lateinit var filer: Filer
        private set

    fun init(env: ProcessingEnvironment) {
        elements = env.elementUtils
        types = env.typeUtils
        messager = env.messager
        filer = env.filer
    }
}