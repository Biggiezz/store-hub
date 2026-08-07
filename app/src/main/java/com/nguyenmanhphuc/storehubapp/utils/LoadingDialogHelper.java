package com.nguyenmanhphuc.storehubapp.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.nguyenmanhphuc.storehubapp.R;

public class LoadingDialogHelper {
    private final AlertDialog dialog;
    private final TextView tvMessage;

    public LoadingDialogHelper(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        tvMessage = view.findViewById(R.id.tvLoadingMessage);
        
        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setMessage(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
