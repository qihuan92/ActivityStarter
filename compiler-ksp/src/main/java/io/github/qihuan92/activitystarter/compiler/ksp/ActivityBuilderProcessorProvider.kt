package io.github.qihuan92.activitystarter.compiler.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * ActivityBuilderProcessorProvider
 *
 * @author Qi
 * @since 2022/9/2
 */
class ActivityBuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ActivityBuilderProcessor(environment)
    }
}