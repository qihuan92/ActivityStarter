package io.github.qihuan92.activitystarter;

import android.app.Application;

/**
 * ActivityStarter
 *
 * @author qi
 * @since 2021/8/4
 */
public class ActivityStarter {
    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new StarterActivityLifecycleCallbacks());
    }
}
