package com.dev.detector;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.dev.detector.Qwanwin.implement.DetectorVPN;
import com.dev.detector.Qwanwin.implement.UtilsVPN;
import com.dev.detector.Qwanwin.implement.AntiHook;
import com.dev.detector.Qwanwin.implement.UtilsHook;
import com.dev.detector.Qwanwin.implement.Antiemulator;
import com.dev.detector.Qwanwin.implement.UtilsEmu;
import com.dev.detector.Qwanwin.implement.AntiVM;
import com.dev.detector.Qwanwin.implement.UtilsVM;
import com.dev.detector.Qwanwin.implement.PineDetector;
import com.dev.detector.Qwanwin.implement.UtilsPine;
import com.dev.detector.Qwanwin.implement.RootRobust;
import com.dev.detector.Qwanwin.implement.UtilsRoot;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private DetectorVPN vpnDetector;
    private UtilsVPN utilsVPN;
    private AntiHook antiHook;
    private UtilsHook utilsHook;
    private Antiemulator emulatorDetector;
    private UtilsEmu utilsEmu;
    private AntiVM vmDetector;
    private UtilsVM utilsVM;
    private PineDetector pineDetector;
    private UtilsPine utilsPine;
    private RootRobust rootDetector;
    private UtilsRoot utilsRoot;
    private Handler handler;
    private boolean isRunning = false;
    private boolean lastVPNState = false;
    private static final long CHECK_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeSecurityComponents();
        performSecurityChecks();
    }

    private void initializeSecurityComponents() {
        
        rootDetector = new RootRobust(this);
        utilsRoot = new UtilsRoot(this);

        
        antiHook = new AntiHook(this);
        utilsHook = new UtilsHook(this);
        emulatorDetector = new Antiemulator(this);
        utilsEmu = UtilsEmu.getInstance(this);
        vmDetector = new AntiVM(this);
        utilsVM = UtilsVM.getInstance(this);
        vpnDetector = new DetectorVPN(this);
        utilsVPN = new UtilsVPN(this);
        pineDetector = new PineDetector(this);
        utilsPine = UtilsPine.getInstance(this);
        handler = new Handler(Looper.getMainLooper());

        
        antiHook.protect("libdetector.so");
    }

    private void performSecurityChecks() {
        
        if (rootDetector.hasAnyThreat()) {
            rootDetector.checkSecurityStatus();
            return;
        }

        
        if (vmDetector.isVM()) {
            utilsVM.showVMDetectedDialog();
            return;
        }

        
        if (emulatorDetector.isEmulator()) {
            utilsEmu.showEmulatorDetectedDialog();
            return;
        }

        
        if (!antiHook.checkIntegrity()) {
            utilsHook.showHookWarningDialog();
            return;
        }

        
        int pineStatus = pineDetector.checkSecurity();
        if (pineStatus != PineDetector.SECURITY_CLEAN) {
            utilsPine.showPineDetectedDialog(pineDetector.getSecurityStatus(pineStatus));
            return;
        }

        startDetection();
    }

    private void startDetection() {
        isRunning = true;
        handler.post(detectionRunnable);
    }

    private final Runnable detectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                
                if (rootDetector.hasAnyThreat()) {
                    rootDetector.checkSecurityStatus();
                    return;
                }

                
                if (vmDetector.isVM()) {
                    utilsVM.showVMDetectedDialog();
                    return;
                }

                
                if (emulatorDetector.isEmulator()) {
                    utilsEmu.showEmulatorDetectedDialog();
                    return;
                }

                if (!antiHook.checkIntegrity()) {
                    utilsHook.showHookWarningDialog();
                    return;
                }

                int pineStatus = pineDetector.checkSecurity();
                if (pineStatus != PineDetector.SECURITY_CLEAN) {
                    utilsPine.showPineDetectedDialog(pineDetector.getSecurityStatus(pineStatus));
                    return;
                }
                
                
                checkVPN();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }
    };

    private void checkVPN() {
        try {
            DetectorVPN.VPNStatus status = vpnDetector.getDetailedStatus();
            boolean currentVPNState = status.isVPNDetected();
            
            if (currentVPNState != lastVPNState) {
                utilsVPN.handleVPNStatusChange(lastVPNState, currentVPNState);
                lastVPNState = currentVPNState;
            }

            if (currentVPNState) {
                utilsVPN.handleVPNDetection(status);
            }
        } catch (Exception e) {
            utilsVPN.handleSecurityException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        performSecurityChecks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDetection();
    }

    private void stopDetection() {
        isRunning = false;
        if (handler != null) {
            handler.removeCallbacks(detectionRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        if (utilsRoot != null) utilsRoot.cleanup();
        if (utilsVPN != null) utilsVPN.cleanup();
        if (utilsHook != null) utilsHook.cleanup();
        if (utilsEmu != null) utilsEmu.dismissDialogs();
        if (utilsVM != null) utilsVM.dismissDialogs();
        if (utilsPine != null) utilsPine.dismissDialogs();
        stopDetection();
        super.onDestroy();
    }

    
    public boolean isDeviceRooted() {
        return rootDetector.isRooted();
    }

    public boolean hasMagisk() {
        return rootDetector.hasMagisk();
    }

    public boolean hasKernelSU() {
        return rootDetector.hasKernelSU();
    }

    public boolean hasXposed() {
        return rootDetector.hasXposed();
    }

    public boolean hasLSPosed() {
        return rootDetector.hasLSPosed();
    }

    public boolean hasBusybox() {
        return rootDetector.hasBusybox();
    }
}