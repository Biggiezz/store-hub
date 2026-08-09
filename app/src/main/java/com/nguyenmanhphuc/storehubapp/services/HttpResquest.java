package com.nguyenmanhphuc.storehubapp.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.nguyenmanhphuc.storehubapp.utils.NetworkUtils;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.nguyenmanhphuc.storehubapp.R;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpResquest {

    public static final String BASE_URL = "https://storehub-server.vercel.app/";

    public ApiServices apiServices;

    public HttpResquest() {
        this(null);
    }

    public HttpResquest(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (context != null) {
            final Context appContext = context.getApplicationContext();
            builder.addInterceptor(chain -> {
                if (!NetworkUtils.isNetworkAvailable(appContext)) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(appContext, appContext.getString(R.string.network_unavailable_toast), Toast.LENGTH_SHORT).show()
                    );
                    throw new IOException(appContext.getString(R.string.network_unavailable_toast));
                }
                return chain.proceed(chain.request());
            });
        }
        apiServices = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiServices.class);
    }

    public ApiServices callAPI() {
        return apiServices;
    }

    public static String authorizationHeader(Context context) {
        String token = new SharedPreferencesManager(context).getToken();
        return token == null || token.trim().isEmpty() ? null : "Bearer " + token;
    }
}
