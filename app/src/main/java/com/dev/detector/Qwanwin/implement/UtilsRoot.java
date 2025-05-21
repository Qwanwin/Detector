package com.dev.detector.Qwanwin.implement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

public class UtilsRoot {
    private Context context;
    private AlertDialog currentDialog;
    private Handler mainHandler;

    public UtilsRoot(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void showRootDetectedDialog() {
        showSecurityDialog(
            "Root Detection Alert",
            "This device appears to be rooted. For security reasons, some features may be restricted.",
            true
        );
    }

    public void showMagiskDetectedDialog() {
        showSecurityDialog(
            "Magisk Detected",
            "Magisk root management has been detected on this device. This may compromise security.",
            true
        );
    }

    public void showKernelSUDetectedDialog() {
        showSecurityDialog(
            "KernelSU Detected",
            "KernelSU root management has been detected. This may affect system security.",
            true
        );
    }

    public void showXposedDetectedDialog() {
        showSecurityDialog(
            "Xposed Framework Detected",
            "Xposed Framework has been detected. This can modify app behavior and compromise security.",
            true
        );
    }

    public void showLSPosedDetectedDialog() {
        showSecurityDialog(
            "LSPosed Framework Detected",
            "LSPosed Framework has been detected. This can modify app behavior and pose security risks.",
            true
        );
    }

    public void showBusyboxDetectedDialog() {
        showSecurityDialog(
            "Busybox Detected",
            "Busybox installation detected. This tool is often used with root access.",
            false
        );
    }

    private void showSecurityDialog(final String title, final String message, final boolean isCritical) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            mainHandler.post(() -> {
                dismissCurrentDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false);

                if (isCritical) {
                    builder.setPositiveButton("Exit", (dialog, which) -> {
                        dialog.dismiss();
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    });
                } else {
                    builder.setPositiveButton("Continue", (dialog, which) -> dialog.dismiss())
                           .setNegativeButton("Exit", (dialog, which) -> {
                               dialog.dismiss();
                               if (context instanceof Activity) {
                                   ((Activity) context).finish();
                               }
                           });
                }

                currentDialog = builder.create();
                currentDialog.show();
            });
        }
    }

    public void showSecurityException(Exception e) {
        showSecurityDialog(
            "Security Alert",
            "An unexpected security issue was detected: " + e.getMessage(),
            true
        );
    }

    public void dismissDialogs() {
        mainHandler.post(this::dismissCurrentDialog);
    }

    private void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }

    public void cleanup() {
        dismissDialogs();
        context = null;
    }

    private static class DialogStyles {
        public static void applyCustomStyle(AlertDialog dialog) {
            if (dialog != null && dialog.getWindow() != null) {
                
            }
        }
    }
}