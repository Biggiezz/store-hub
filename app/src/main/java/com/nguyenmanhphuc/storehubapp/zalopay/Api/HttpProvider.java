package com.nguyenmanhphuc.storehubapp.zalopay.Api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpProvider {
    public static JSONObject sendPost(String URL, RequestBody formBody) {
        JSONObject data = new JSONObject();
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.println(Log.ERROR, "BAD_REQUEST", response.body().string());
                data = null;
            } else {
                data = new JSONObject(response.body().string());
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
