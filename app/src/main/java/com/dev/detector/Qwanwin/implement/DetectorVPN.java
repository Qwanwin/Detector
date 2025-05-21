package com.dev.detector.Qwanwin.implement;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DetectorVPN {
    private final Context context;
    private Boolean cachedResult = null;

    private static final String[] VPN_INTERFACES = {
            "tun0", "ppp0", "pptp0", "l2tp0", "ipsec0", "vpn0"
    };

    private static final String[] VPN_DNS = {
            "8.8.8.8", "8.8.4.4",           // Google DNS
            "208.67.222.222", "208.67.220.220", // OpenDNS
            "1.1.1.1", "1.0.0.1"            // Cloudflare DNS
    };

    private static final int[] VPN_PORTS = {
            1194,  // OpenVPN
            1723,  // PPTP
            500,   // IKEv2
            4500,  // IKEv2 NAT
            1701   // L2TP
    };

   
    public static final int SECURITY_CLEAN = 0x0;
    public static final int SECURITY_VPN = 0x1;
    public static final int SECURITY_TAMPERED = 0x2;

    public DetectorVPN(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
    }


    public boolean detect() {
        if (cachedResult != null) {
            return cachedResult;
        }

        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        boolean result = isVPNActive() || checkNetworkInterfaces() || checkVPNPackages() || (nativeCheckVPN() == SECURITY_VPN);
        cachedResult = result;
        return result;
    }

    public VPNStatus getDetailedStatus() {
        VPNStatus status = new VPNStatus();
        status.setActiveVPNConnection(isVPNActive());
        status.setVPNAppInstalled(checkVPNPackages());
        status.setVPNInterface(checkNetworkInterfaces());
        status.setNativeCheckResult(nativeCheckVPN());
        return status;
    }

    private boolean isVPNActive() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) return false;

                NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                if (caps == null) return false;

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return true;
                }

                if (checkVPNDNS(activeNetwork, cm)) {
                    return true;
                }
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_VPN) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkVPNDNS(Network network, ConnectivityManager cm) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<InetAddress> dnsServers = cm.getLinkProperties(network).getDnsServers();
                for (InetAddress dns : dnsServers) {
                    String dnsAddress = dns.getHostAddress();
                    for (String vpnDns : VPN_DNS) {
                        if (vpnDns.equals(dnsAddress)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean checkVPNPackages() {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            for (PackageInfo pkg : packages) {
                if (pkg.requestedPermissions != null) {
                    for (String perm : pkg.requestedPermissions) {
                        if ("android.permission.BIND_VPN_SERVICE".equals(perm)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean checkNetworkInterfaces() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                if (!networkInterface.isUp()) continue;
                String interfaceName = networkInterface.getName().toLowerCase();
                for (String vpnInterface : VPN_INTERFACES) {
                    if (interfaceName.contains(vpnInterface)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private native int nativeCheckVPN();

    static {
        try {
            System.loadLibrary("detector");
        } catch (UnsatisfiedLinkError e) {
        }
    }

   
    public static class VPNStatus {
        private boolean activeVPNConnection;
        private boolean vpnAppInstalled;
        private boolean vpnInterface;
        private int nativeCheckResult;

        public boolean isActiveVPNConnection() {
            return activeVPNConnection;
        }

        public void setActiveVPNConnection(boolean activeVPNConnection) {
            this.activeVPNConnection = activeVPNConnection;
        }

        public boolean isVPNAppInstalled() {
            return vpnAppInstalled;
        }

        public void setVPNAppInstalled(boolean vpnAppInstalled) {
            this.vpnAppInstalled = vpnAppInstalled;
        }

        public boolean isVPNInterface() {
            return vpnInterface;
        }

        public void setVPNInterface(boolean vpnInterface) {
            this.vpnInterface = vpnInterface;
        }

        public int getNativeCheckResult() {
            return nativeCheckResult;
        }

        public void setNativeCheckResult(int nativeCheckResult) {
            this.nativeCheckResult = nativeCheckResult;
        }

        public boolean isVPNDetected() {
            return activeVPNConnection || vpnAppInstalled || vpnInterface || nativeCheckResult == SECURITY_VPN;
        }

        @Override
        public String toString() {
            return "VPNStatus{" +
                    "activeVPNConnection=" + activeVPNConnection +
                    ", vpnAppInstalled=" + vpnAppInstalled +
                    ", vpnInterface=" + vpnInterface +
                    ", nativeCheckResult=" + nativeCheckResult +
                    '}';
        }
    }
}