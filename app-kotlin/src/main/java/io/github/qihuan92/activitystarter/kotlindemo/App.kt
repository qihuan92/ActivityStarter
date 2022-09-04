package io.github.qihuan92.activitystarter.kotlindemo

import android.app.Application
import io.github.qihuan92.activitystarter.ActivityStarter

/**
 * Application
 *
 * @author Qi
 * @since 2022/9/4
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ActivityStarter.init(this)
    }
}