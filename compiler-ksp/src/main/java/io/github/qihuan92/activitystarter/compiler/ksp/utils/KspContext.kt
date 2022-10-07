package io.github.qihuan92.activitystarter.compiler.ksp.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

/**
 * @author Qi
 * @since 2022/10/6
 */
object KspContext {
    lateinit var environment: SymbolProcessorEnvironment
    lateinit var resolver: Resolver
}