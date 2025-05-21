package com.dev.detector.Qwanwin.implement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Process;

public class UtilsVM {
    private Context context;
    private static UtilsVM instance;
    private boolean isDialogShowing = false;
    private AlertDialog currentDialog;

    private UtilsVM(Context context) {
        this.context = context;
    }

    public static synchronized UtilsVM getInstance(Context context) {
        if (instance == null) {
            instance = new UtilsVM(context);
        }
        return instance;
    }

    public void showVMDetectedDialog() {
        if (isDialogShowing || !(context instanceof Activity)) {
            return;
        }

        final Activity activity = (Activity) context;
        if (activity.isFinishing()) {
            return;
        }

        isDialogShowing = true;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("⚠️ Peringatan Keamanan")
                        .setMessage("Aplikasi ini tidak dapat dijalankan pada mesin virtual karena alasan keamanan.")
                        .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exitApp();
                            }
                        });

                    currentDialog = builder.create();
                    currentDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            isDialogShowing = false;
                            currentDialog = null;
                        }
                    });

                    if (!activity.isFinishing()) {
                        currentDialog.show();
                    }
                } catch (Exception e) {
                    isDialogShowing = false;
                    currentDialog = null;
                }
            }
        });
    }

    private void exitApp() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (context instanceof Activity) {
                        ((Activity) context).finishAffinity();
                    }
                    Process.killProcess(Process.myPid());
                    System.exit(1);
                } catch (Exception e) {
                    System.exit(0);
                }
            }
        }, 300);
    }

    public void dismissDialogs() {
        try {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
        } catch (Exception e) {
            // Ignore any exception during dismissal
        }
        isDialogShowing = false;
        currentDialog = null;
    }

    public boolean isShowingDialog() {
        return isDialogShowing;
    }
}