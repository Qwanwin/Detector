package com.dev.detector.Qwanwin.implement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

public class UtilsVPN {
    private static final String TAG = UtilsVPN.class.getSimpleName();
    private final Context context;
    private AlertDialog currentDialog;

    public UtilsVPN(Context context) {
        this.context = context;
    }

    public void handleVPNDetection(DetectorVPN.VPNStatus status) {
        if (status.isVPNDetected()) {
            showVPNWarningDialog();
        }
    }

    public void showVPNWarningDialog() {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dismissCurrentDialog();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("VPN Detected")
                .setMessage("VPN usage detected. Please disable your VPN to continue.")
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, which) -> openVPNSettings())
                .setNegativeButton("Exit", (dialog, which) -> exitApp());

            currentDialog = builder.create();
            currentDialog.show();
        }
    }

    public void showBlockingDialog() {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dismissCurrentDialog();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Access Denied")
                .setMessage("VPN usage is not allowed. Please disable your VPN to continue.")
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, which) -> openVPNSettings())
                .setNegativeButton("Exit", (dialog, which) -> exitApp());

            currentDialog = builder.create();
            currentDialog.show();
        }
    }

    public void showSecurityAlert(String message) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dismissCurrentDialog();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Security Warning")
                .setMessage(message)
                .setPositiveButton("OK", null);

            currentDialog = builder.create();
            currentDialog.show();
        }
    }

    public void openVPNSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_VPN_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening VPN settings", e);
            showError("Cannot open VPN settings");
        }
    }

    public void exitApp() {
        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }
    }

    public void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    public String getDetailedStatus(DetectorVPN.VPNStatus status) {
        StringBuilder details = new StringBuilder();
        
        if (status.isActiveVPNConnection()) {
            details.append("• Active VPN connection detected\n");
        }
        if (status.isVPNAppInstalled()) {
            details.append("• VPN application installed\n");
        }
        if (status.isVPNInterface()) {
            details.append("• VPN interface detected\n");
        }
        
        return details.toString();
    }

    public void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
            showError("Cannot open app settings");
        }
    }

    public void handleSecurityException(Exception e) {
        Log.e(TAG, "Security Exception", e);
        showSecurityAlert("Security issue detected:\n" + e.getMessage());
    }

    public void showVPNBlockedNotification() {
        Toast.makeText(context, 
            "VPN detected and blocked", 
            Toast.LENGTH_LONG).show();
    }

    public void showCustomSecurityDialog(String title, String message, 
            Runnable positiveAction, Runnable negativeAction) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dismissCurrentDialog();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (positiveAction != null) positiveAction.run();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (negativeAction != null) negativeAction.run();
                });

            currentDialog = builder.create();
            currentDialog.show();
        }
    }

    public void handleVPNStatusChange(boolean wasDetected, boolean isNowDetected) {
        if (!wasDetected && isNowDetected) {
            showVPNWarningDialog();
        } else if (wasDetected && !isNowDetected) {
            Toast.makeText(context, 
                "VPN has been disabled", 
                Toast.LENGTH_SHORT).show();
        }
    }

    public void cleanup() {
        dismissCurrentDialog();
    }
}