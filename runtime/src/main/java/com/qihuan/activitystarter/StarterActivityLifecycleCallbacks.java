package com.qihuan.activitystarter;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * StarterActivityLifecycleCallbacks
 *
 * @author qi
 * @since 2021/8/4
 */
class StarterActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        performInject(activity, bundle);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        performSaveState(activity, bundle);
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private void performInject(Activity activity, Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                Intent intent = activity.getIntent();
                if (intent == null) {
                    return;
                }
                savedInstanceState = intent.getExtras();
            }
            BuilderClassFinder.findBuilderClass(activity).getDeclaredMethod("inject", Activity.class, Bundle.class).invoke(null, activity, savedInstanceState);
        } catch (Exception e) {
            Log.w("ActivityStarter", e);
        }
    }

    private void performSaveState(Activity activity, Bundle outState) {
        try {
            BuilderClassFinder.findBuilderClass(activity).getDeclaredMethod("saveState", Activity.class, Bundle.class).invoke(null, activity, outState);
        } catch (Exception e) {
            Log.w("ActivityStarter", e);
        }
    }
}
