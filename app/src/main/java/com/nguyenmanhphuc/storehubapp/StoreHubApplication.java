package com.nguyenmanhphuc.storehubapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

public class StoreHubApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private long lastBackgroundTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Fix for SplashActivity launcher bug
        if (activity instanceof SplashActivity) {
            if (!activity.isTaskRoot()
                    && activity.getIntent() != null
                    && activity.getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                    && Intent.ACTION_MAIN.equals(activity.getIntent().getAction())) {
                activity.finish();
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters foreground
            if (lastBackgroundTime > 0) {
                long elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTime) / 1000;
                lastBackgroundTime = 0;
                if (elapsedSeconds > 300 && !(activity instanceof PaymentConfirmationActivity)) {
                    // Reload app by launching SplashActivity and clearing stack
                    Intent intent = new Intent(activity, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        }
        if (!isActivityChangingConfigurations) {
            activityReferences++;
        }
        isActivityChangingConfigurations = false;
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            if (activityReferences == 0) {
                // App enters background
                lastBackgroundTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
