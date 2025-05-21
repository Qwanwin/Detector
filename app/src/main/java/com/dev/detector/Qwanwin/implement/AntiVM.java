package com.dev.detector.Qwanwin.implement;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntiVM {
    private Context context;

    public AntiVM(Context context) {
        this.context = context;
    }

    public boolean isVM() {
        return checkVMFiles() || 
               checkVMPackages() || 
               checkCPUInfo() || 
               checkBuildProps() ||
               checkVMProcesses();
    }

    private boolean checkVMFiles() {
        String[] vmFiles = {
            "/system/lib/libdroid4x.so",
            "/system/bin/windroyed",
            "/system/bin/microvirtd",
            "/system/bin/nox-prop",
            "/system/bin/ttVM-prop",
            "/system/lib/libhoudini.so",
            "/system/lib/libAMVM.so",
            "/system/lib/libdvmvmrun.so",
            "/system/lib/libvmconfig.so",
            "/system/lib/libvmmanager.so",
            "/data/data/com.bluestacks.settings",
            "/data/data/com.bignox.app",
            "/data/data/com.vphone.launcher",
            "/data/data/com.microvirt.market",
            "/data/data/com.droid4x.avd",
            "/system/priv-app/droid4xService",
            "/system/app/MEmuService"
        };

        for (String file : vmFiles) {
            if (new File(file).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkVMPackages() {
        PackageManager pm = context.getPackageManager();
        String[] vmPackages = {
     "com.lbe.parallel.intl",
    "com.parallel.space.lite",
    "com.parallel.space.pro",
    "com.parallel.space",
    "com.parallel.space.arm64",
    "com.ludashi.dualspace",
    "com.ludashi.superclone",
    "com.dualspace.im",
    "com.excelliance.dualaid",
    "com.excelliance.dualapp",
    "com.excelliance.multiaccounts",
    "com.excelliance.multiaccounts.lite",
    "multiple.parallel.accounts.cloner",
    "com.clone.parallel.space.multiple.accounts",
    "do.multiple.space",
    "com.domultiple.clone",
    "com.do.multiple.cloner",
    "com.thinkmobile.twoface",
    "com.thinkmobile.duplicateapp",
    "com.jiubang.commerce.gomultiple",
    "com.jumobile.multiapp",
    "com.jumobile.multipro",
    "com.clone.android.app",
    "com.clone.parallel",
    "com.cloneapp.parallelspace",
    "com.cloner.parallel.space",
    "com.polestar.super.clone",
    "com.multiaccount.parallelspace",
    "com.multipleaccounts.parallelapp",
    "com.multiply.multiples",
    "com.mobile.dual.app",
    "com.dual.app.cloner",
    "com.dual.space.cloner",
    "com.dual.space.lite",
    "com.dual.space.pro",
    "com.multi.parallel",
    "com.multiparallel.dualspace",
    "com.multipleaccounts.parallelapp.dualspace",
    "com.applisto.appcloner",
    "com.app.cloner",
    "com.oasisfeng.island",
    "com.userclone.appcloner",
    "com.hecorat.multiplespace",
    "com.x8bit.bitwarden",
    "com.prime.multispace",
    "com.doublelabs.insclone",
    "com.augmented.space",
    "com.infinix.xclone",
    "com.vivo.clone",
    "com.samsung.android.dualapp",
    "com.miui.dualapp",
    "com.huawei.clone",
    "com.oppo.clone",
    "com.realme.clone",
    "com.asus.clone",
    "com.secure.space",
    "com.virtual.box",
    "com.shelter.android",
    "com.switchme.app",
    "com.assistivetouch.mirror",
    "com.jiubang.commerce.gomultiple",
    "com.clone.space.galaxy",
    "com.parallel.cloner.multiple",
    "com.dual.messenger.apps",
    "com.multi.clone.pro",
    "com.multiply.space.pro",
    "com.dual.space.master",
    "com.parallel.space.master",
    "com.clone.space.lite",
    "com.dual.apps.cloner",
    "com.multiple.space.clone"

        };

        for (String pkg : vmPackages) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                
            }
        }
        return false;
    }

    private boolean checkCPUInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("hypervisor") ||
                    line.toLowerCase().contains("virtual") ||
                    line.toLowerCase().contains("vmware") ||
                    line.toLowerCase().contains("qemu")) {
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

    private boolean checkBuildProps() {
        String hardware = Build.HARDWARE.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String product = Build.PRODUCT.toLowerCase();
        String brand = Build.BRAND.toLowerCase();

        return hardware.contains("virtual") ||
               hardware.contains("vm") ||
               model.contains("virtual") ||
               model.contains("vm") ||
               manufacturer.contains("virtual") ||
               manufacturer.contains("vm") ||
               product.contains("virtual") ||
               product.contains("vm") ||
               brand.contains("virtual") ||
               brand.contains("vm");
    }

    private boolean checkVMProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            List<String> vmProcesses = Arrays.asList(
                "vboxservice",
                "vboxtray",
                "vmtoolsd",
                "vmwaretray",
                "vmwareuser",
                "VGAuthService",
                "vmacthlp",
                "vmusrvc",
                "qemu-system",
                "windroyed",
                "microvirtd",
                "noxd",
                "ttVM",
                "droid4xd"
            );

            while ((line = reader.readLine()) != null) {
                for (String vmProcess : vmProcesses) {
                    if (line.toLowerCase().contains(vmProcess)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public List<String> getVMDetails() {
        List<String> details = new ArrayList<>();
        details.add("Hardware: " + Build.HARDWARE);
        details.add("Model: " + Build.MODEL);
        details.add("Manufacturer: " + Build.MANUFACTURER);
        details.add("Product: " + Build.PRODUCT);
        details.add("Brand: " + Build.BRAND);
        details.add("Device: " + Build.DEVICE);
        details.add("Board: " + Build.BOARD);
        
        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("virtual") || line.contains("vm") || 
                    line.contains("qemu") || line.contains("simulator")) {
                    details.add("Property: " + line);
                }
            }
            reader.close();
        } catch (Exception e) {
            details.add("Error reading properties: " + e.getMessage());
        }
        
        return details;
    }
}