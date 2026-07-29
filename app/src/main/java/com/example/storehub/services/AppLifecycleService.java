package com.example.storehub.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.storehub.model.response.Response;
import com.example.storehub.model.User;
import com.example.storehub.utils.SharedPreferencesManager;

public class AppLifecycleService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        setOfflineSync();
        stopSelf();
    }

    private void setOfflineSync() {
        SharedPreferencesManager prefManager = SharedPreferencesManager.getInstance(this);
        User currentUser = prefManager.getUser();
        String token = prefManager.getToken();
        if (currentUser != null && token != null && !token.isEmpty()) {
            ApiServices apiServices = new HttpResquest().callAPI();
            try {
                // Thực hiện cuộc gọi đồng bộ (execute) vì app sắp bị kill
                apiServices.setOffline("Bearer " + token).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
