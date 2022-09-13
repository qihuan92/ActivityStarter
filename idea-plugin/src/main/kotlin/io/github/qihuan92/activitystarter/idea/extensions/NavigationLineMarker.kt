package io.github.qihuan92.activitystarter.idea.extensions

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.navigation.NavigationItem
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import java.awt.event.MouseEvent

class NavigationLineMarker : LineMarkerProviderDescriptor(), GutterIconNavigationHandler<PsiElement> {

    companion object {
        const val GENERATED_ANNOTATION_NAME = "io.github.qihuan92.activitystarter.annotation.Generated"
        const val NOTIFY_SERVICE_NAME = "ActivityStarter Plugin Tips"
        const val NOTIFY_TITLE = "Road Sign"
        const val NOTIFY_NO_TARGET_TIPS = "No destination found or unsupported type."
        val logger = Logger.getInstance(NavigationLineMarker::class.java)
    }

    private val navigationOnIcon = IconLoader.getIcon("/icon/ic_jump.png")

    override fun getName() = "ActivityStarter Location"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (isNavigationCall(element)) {
            return LineMarkerInfo(
                element,
                element.textRange,
                navigationOnIcon,
                Pass.UPDATE_ALL,
                null,
                this,
                GutterIconRenderer.Alignment.LEFT
            )
        }
        return null
    }

    override fun navigate(e: MouseEvent?, element: PsiElement?) {
        if (element == null) {
            return
        }

        if (element is PsiCallExpression) {
            val method = element.resolveMethod() ?: return
            val parent = method.parent
            val activityName = (parent as PsiClass).qualifiedName?.dropLast("Builder".length) ?: return
            logger.info("activityName: $activityName")

            val fullScope = GlobalSearchScope.allScope(element.project)
            val findClass = JavaPsiFacade.getInstance(element.project).findClass(activityName, fullScope)
            NavigationItem::class.java.cast(findClass).navigate(true)
            return
        }

        Notifications.Bus.notify(
            Notification(
                NOTIFY_SERVICE_NAME, NOTIFY_TITLE, NOTIFY_NO_TARGET_TIPS, NotificationType.WARNING
            )
        )
    }

    private fun isNavigationCall(psiElement: PsiElement): Boolean {
        if (psiElement is PsiCallExpression) {
            val method = psiElement.resolveMethod() ?: return false
            val parent = method.parent

            if (method.name == "builder" && parent is PsiClass) {
                logger.info("builder caller: ${parent.name}")
                if (parent.hasAnnotation(GENERATED_ANNOTATION_NAME)) {
                    return true
                }
            }
        }
        return false
    }
}