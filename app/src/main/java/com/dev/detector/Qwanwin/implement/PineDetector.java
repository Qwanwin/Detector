package com.dev.detector.Qwanwin.implement;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class PineDetector {
    private Context context;
    private UtilsPine utilsPine;
    private Handler handler;
    private boolean isRunning = false;
    private static final long CHECK_INTERVAL = 1000; // 1 second

    static {
        System.loadLibrary("detector");
    }

    public static final int SECURITY_CLEAN = 0;
    public static final int SECURITY_ART_METHOD = 1;
    public static final int SECURITY_QUICK_BRIDGE = 2;
    public static final int SECURITY_MEMORY_PERM = 3;
    public static final int SECURITY_INLINE_HOOK = 4;
    public static final int SECURITY_INTERPRETER = 5;

    public PineDetector(Context context) {
        this.context = context;
        this.utilsPine = UtilsPine.getInstance(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    private native int nativeCheck();

    public void startDetection() {
        if (!isRunning) {
            isRunning = true;
            handler.post(detectionRunnable);
        }
    }

    public void stopDetection() {
        isRunning = false;
        if (handler != null) {
            handler.removeCallbacks(detectionRunnable);
        }
    }

    private final Runnable detectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                int status = checkSecurity();
                if (status != SECURITY_CLEAN) {
                    String message = getSecurityStatus(status);
                    utilsPine.showPineDetectedDialog(message);
                    return;
                }
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }
    };

    public int checkSecurity() {
        return nativeCheck();
    }

    public String getSecurityStatus(int status) {
        switch (status) {
            case SECURITY_CLEAN:
                return "Clean";
            case SECURITY_ART_METHOD:
                return "ART Method Hook Detected";
            case SECURITY_QUICK_BRIDGE:
                return "Quick Bridge Hook Detected";
            case SECURITY_MEMORY_PERM:
                return "Suspicious Memory Permissions";
            case SECURITY_INLINE_HOOK:
                return "Inline Hook Detected";
            case SECURITY_INTERPRETER:
                return "Interpreter Hook Detected";
            default:
                return "Unknown Status";
        }
    }

    public void cleanup() {
        stopDetection();
        if (utilsPine != null) {
            utilsPine.dismissDialogs();
        }
    }
}