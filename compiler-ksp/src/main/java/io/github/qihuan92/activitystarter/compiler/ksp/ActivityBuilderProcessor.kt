package io.github.qihuan92.activitystarter.compiler.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Builder注解处理器
 *
 * @author Qi
 * @since 2022/9/2
 */
class ActivityBuilderProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // todo 处理注解
        return emptyList()
    }
}