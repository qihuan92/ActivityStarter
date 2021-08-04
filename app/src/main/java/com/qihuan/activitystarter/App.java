package com.qihuan.activitystarter;

import android.app.Application;

/**
 * App
 *
 * @author qi
 * @since 2021/8/4
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityStarter.init(this);
    }
}
