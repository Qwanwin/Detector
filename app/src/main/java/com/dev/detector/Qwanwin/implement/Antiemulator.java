package com.dev.detector.Qwanwin.implement;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Antiemulator {
    private Context context;
    private static final Map<String, String> VIRTUAL_PROPERTIES = new HashMap<>();

    static {
        
        VIRTUAL_PROPERTIES.put("ro.hardware", "goldfish|ranchu|vbox|nox|ttvm");
        VIRTUAL_PROPERTIES.put("ro.product.model", "google_sdk|sdk|sdk_x86|sdk_gphone|generic");
        VIRTUAL_PROPERTIES.put("ro.kernel.qemu", "1");
        VIRTUAL_PROPERTIES.put("ro.boot.selinux", "disabled");
    }

    public Antiemulator(Context context) {
        this.context = context;
    }

    public boolean isEmulator() {
        return checkQemuProperties() 
            && (buildCharacteristics() 
            || checkFiles() 
            || checkBuildInfo()
            || checkSystemProperties()
            || checkVirtualBoxFiles());
    }

    private boolean buildCharacteristics() {
        String fingerprint = Build.FINGERPRINT.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String brand = Build.BRAND.toLowerCase();
        String device = Build.DEVICE.toLowerCase();
        String product = Build.PRODUCT.toLowerCase();

        return (fingerprint.contains("generic") 
                && fingerprint.contains("test-keys"))
            || model.contains("google_sdk") 
            || model.contains("emulator")
            || product.contains("sdk_gphone") 
            || product.contains("sdk")
            || product.contains("sdk_x86")
            || product.contains("vbox")
            || device.contains("generic");
    }

    private boolean checkFiles() {
        String[] knownFiles = {
                "/system/lib/libc_malloc_debug_qemu.so",
                "/sys/qemu_trace",
                "/system/bin/qemu-props",
                "/dev/socket/qemud",
                "/dev/qemu_pipe",
                "/dev/goldfish_pipe",
                "/system/bin/androVM-prop",
                "/system/bin/microvirt-prop",
                "/system/lib/libdroid4x.so",
                "/system/bin/windroyed",
                "/system/bin/microvirtd",
                "/system/bin/nox-prop",
                "/system/bin/ttVM-prop"
        };

        for (String file : knownFiles) {
            if (new File(file).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkQemuProperties() {
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("qemu") && line.contains("1")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean checkSystemProperties() {
        try {
            String[] properties = {
                "init.svc.qemud",
                "init.svc.qemu-props",
                "qemu.hw.mainkeys",
                "qemu.sf.fake_camera"
            };

            for (String property : properties) {
                String value = getSystemProperty(property);
                if (value != null && !value.isEmpty() && value.equals("1")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean checkBuildInfo() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private boolean checkVirtualBoxFiles() {
        String[] virtualBoxFiles = {
            "/data/data/com.virtualbox.android",
            "/system/lib/libvboxjni.so",
            "/system/lib64/libvboxjni.so"
        };

        for (String file : virtualBoxFiles) {
            if (new File(file).exists()) {
                return true;
            }
        }
        return false;
    }

    private String getSystemProperty(String propName) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + propName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            reader.close();
            return line;
        } catch (Exception e) {
            return null;
        }
    }
}