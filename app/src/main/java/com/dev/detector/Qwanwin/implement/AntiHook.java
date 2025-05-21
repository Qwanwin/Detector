package com.dev.detector.Qwanwin.implement;

import android.content.Context;
import android.util.Log;

public class AntiHook {
    private static final String TAG = AntiHook.class.getSimpleName();
    private final Context context;

    public AntiHook(Context context) {
        this.context = context.getApplicationContext();
        nativeInit();
    }

    public boolean protect(String libraryName) {
        return nativeProtectLibrary(libraryName);
    }

    public boolean checkIntegrity() {
        return nativeCheckIntegrity();
    }

    private native boolean nativeInit();
    private native boolean nativeProtectLibrary(String libraryName);
    private native boolean nativeCheckIntegrity();

    static {
        try {
            System.loadLibrary("detector");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
}