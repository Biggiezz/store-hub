package com.nguyenmanhphuc.storehubapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.widget.Toast;
import com.nguyenmanhphuc.storehubapp.R;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    public static boolean checkNetworkOrShowToast(Context context) {
        if (!isNetworkAvailable(context)) {
            if (context != null) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.network_unavailable_toast), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public static void showNoNetworkToast(Context context) {
        if (context != null) {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.network_unavailable_toast), Toast.LENGTH_SHORT).show();
        }
    }
}
