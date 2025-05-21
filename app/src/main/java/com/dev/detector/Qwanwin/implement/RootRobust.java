package com.dev.detector.Qwanwin.implement;

import android.content.Context;

public class RootRobust {
    private static final int ROOT_DETECTED = 0x1;
    private static final int MAGISK_DETECTED = 0x2;
    private static final int KERNELSU_DETECTED = 0x4;
    private static final int XPOSED_DETECTED = 0x8;
    private static final int LSPOSED_DETECTED = 0x10;
    private static final int BUSYBOX_DETECTED = 0x20;

    private Context context;
    private UtilsRoot utilsRoot;

    public RootRobust(Context context) {
        this.context = context;
        this.utilsRoot = new UtilsRoot(context);
    }

    public native int nativeCheck();

    static {
        System.loadLibrary("detector");
    }

    public void checkSecurityStatus() {
        int status = nativeCheck();
        handleSecurityStatus(status);
    }

    private void handleSecurityStatus(int status) {
        if ((status & ROOT_DETECTED) != 0) {
            utilsRoot.showRootDetectedDialog();
        }
        if ((status & MAGISK_DETECTED) != 0) {
            utilsRoot.showMagiskDetectedDialog();
        }
        if ((status & KERNELSU_DETECTED) != 0) {
            utilsRoot.showKernelSUDetectedDialog();
        }
        if ((status & XPOSED_DETECTED) != 0) {
            utilsRoot.showXposedDetectedDialog();
        }
        if ((status & LSPOSED_DETECTED) != 0) {
            utilsRoot.showLSPosedDetectedDialog();
        }
        if ((status & BUSYBOX_DETECTED) != 0) {
            utilsRoot.showBusyboxDetectedDialog();
        }
    }

    public boolean isRooted() {
        return (nativeCheck() & ROOT_DETECTED) != 0;
    }

    public boolean hasMagisk() {
        return (nativeCheck() & MAGISK_DETECTED) != 0;
    }

    public boolean hasKernelSU() {
        return (nativeCheck() & KERNELSU_DETECTED) != 0;
    }

    public boolean hasXposed() {
        return (nativeCheck() & XPOSED_DETECTED) != 0;
    }

    public boolean hasLSPosed() {
        return (nativeCheck() & LSPOSED_DETECTED) != 0;
    }

    public boolean hasBusybox() {
        return (nativeCheck() & BUSYBOX_DETECTED) != 0;
    }

    public boolean hasAnyThreat() {
        return nativeCheck() != 0;
    }
}