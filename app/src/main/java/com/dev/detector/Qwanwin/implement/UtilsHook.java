package com.dev.detector.Qwanwin.implement;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

public class UtilsHook {
    private static final String TAG = UtilsHook.class.getSimpleName();
    private final Context context;
    private AlertDialog currentDialog;

    public UtilsHook(Context context) {
        this.context = context;
    }

    public void showHookWarningDialog() {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dismissCurrentDialog();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Security Alert")
                .setMessage("Abnormal app. This app cannot run in a modified environment.")
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, which) -> exitApp());

            currentDialog = builder.create();
            currentDialog.show();
        }
    }

    public void exitApp() {
        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }
    }

    public void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    public void cleanup() {
        dismissCurrentDialog();
    }
}