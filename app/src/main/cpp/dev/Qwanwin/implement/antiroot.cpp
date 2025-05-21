#include <jni.h>
#include <string>
#include <fstream>
#include <unistd.h>
#include <sys/stat.h>
#include <android/log.h>
#include <vector>
#include <dirent.h>
#include <sys/system_properties.h>

namespace dev {
    namespace Qwanwin {
        namespace implement {
            class AntiRoot {
            private:
                static const std::vector<std::string>& getRootBinaries() {
                    static const std::vector<std::string> paths = {
                        "/system/bin/su",
                        "/system/xbin/su",
                        "/sbin/su",
                        "/system/app/Superuser.apk",
                        "/system/app/SuperSU.apk",
                        "/data/local/xbin/su",
                        "/data/local/bin/su",
                        "/system/sd/xbin/su",
                        "/system/bin/failsafe/su",
                        "/data/local/su",
                        "/su/bin/su"
                    };
                    return paths;
                }

                static const std::vector<std::string>& getRootPackages() {
                    static const std::vector<std::string> packages = {
                        "com.noshufou.android.su",
                        "com.thirdparty.superuser",
                        "eu.chainfire.supersu",
                        "com.topjohnwu.magisk",
                        "io.github.vvb2060.magisk",
                        "com.kingroot.kinguser",
                        "com.kingo.root",
                        "com.smedialink.oneclickroot",
                        "com.zhiqupk.root.global",
                        "com.alephzain.framaroot",
                        "me.weishu.kernelsu"
                    };
                    return packages;
                }

                static const std::vector<std::string>& getXposedPackages() {
                    static const std::vector<std::string> packages = {
                        "de.robv.android.xposed.installer",
                        "org.meowcat.edxposed.manager",
                        "org.lsposed.manager",
                        "com.ryansteckler.xposed",
                        "com.saurik.substrate",
                        "de.robv.android.xposed.installer.lpparam",
                        "com.topjohnwu.lsposed"
                    };
                    return packages;
                }

                static const std::vector<std::string>& getBusyboxPaths() {
                    static const std::vector<std::string> paths = {
                        "/system/xbin/busybox",
                        "/system/bin/busybox",
                        "/sbin/busybox",
                        "/data/local/xbin/busybox",
                        "/data/local/bin/busybox",
                        "/system/bin/.ext/busybox"
                    };
                    return paths;
                }

                static const std::vector<std::string>& getXposedPaths() {
                    static const std::vector<std::string> paths = {
                        "/system/framework/XposedBridge.jar",
                        "/system/lib/libxposed_art.so",
                        "/system/lib64/libxposed_art.so",
                        "/system/lib/liblsp.so",
                        "/system/lib64/liblsp.so",
                        "/data/misc/riru/modules/lspd",
                        "/data/adb/lspd",
                        "/data/adb/modules/riru_lsposed",
                        "/data/adb/modules/lsposed",
                        "/data/adb/riru/modules/lspd.prop",
                        "/data/misc/riru/modules/edxp",
                        "/system/lib/libriru_edxp.so",
                        "/system/lib64/libriru_edxp.so"
                    };
                    return paths;
                }

                static const std::vector<std::string>& getRootPaths() {
                    static const std::vector<std::string> paths = {
                        "/cache/.su",
                        "/data/su",
                        "/dev/su",
                        "/system/su",
                        "/system/bin/.ext/.su",
                        "/system/usr/we-need-root/su-backup",
                        "/data/adb/magisk",
                        "/sbin/.magisk",
                        "/cache/.magisk",
                        "/dev/.magisk",
                        "/.magisk",
                        "/data/adb/ksu",
                        "/data/adb/ksud",
                        "/data/adb/kernelsu"
                    };
                    return paths;
                }

                static const std::vector<std::string>& getMagiskModulePaths() {
                    static const std::vector<std::string> paths = {
                        "/data/adb/modules",
                        "/data/adb/magisk",
                        "/data/adb/magisk_debug.log",
                        "/data/adb/magisk.log",
                        "/data/adb/magisk.db",
                        "/data/adb/magisk_simple",
                        "/data/adb/post-fs-data.d",
                        "/data/adb/service.d",
                        "/data/magisk",
                        "/data/magisk.img"
                    };
                    return paths;
                }

                static const std::vector<std::string>& getKernelsuPaths() {
                    static const std::vector<std::string> paths = {
                        "/data/adb/ksud",
                        "/data/adb/ksu",
                        "/data/adb/kernelsu.conf",
                        "/sys/kernel/ksu",
                        "/sys/kernel/ksud"
                    };
                    return paths;
                }

                static bool checkBinaryExists(const std::string& path) {
                    struct stat buffer;
                    return (stat(path.c_str(), &buffer) == 0);
                }

                static bool checkRWAccess() {
                    const char* paths[] = {
                        "/system",
                        "/system/bin",
                        "/system/sbin",
                        "/system/xbin",
                        "/vendor/bin",
                        "/sbin",
                        "/etc"
                    };

                    for (const char* path : paths) {
                        if (access(path, W_OK) == 0) {
                            return true;
                        }
                    }
                    return false;
                }

                static bool checkProcMaps() {
                    std::ifstream maps("/proc/self/maps");
                    std::string line;
                    
                    if (maps.is_open()) {
                        while (std::getline(maps, line)) {
                            if (line.find("magisk") != std::string::npos ||
                                line.find("su") != std::string::npos ||
                                line.find("kernelsu") != std::string::npos ||
                                line.find("ksu") != std::string::npos ||
                                line.find("xposed") != std::string::npos ||
                                line.find("lspd") != std::string::npos ||
                                line.find("busybox") != std::string::npos) {
                                return true;
                            }
                        }
                        maps.close();
                    }
                    return false;
                }

                static bool checkMountPoints() {
                    std::ifstream mounts("/proc/mounts");
                    std::string line;

                    if (mounts.is_open()) {
                        while (std::getline(mounts, line)) {
                            if (line.find("magisk") != std::string::npos ||
                                line.find("kernelsu") != std::string::npos ||
                                line.find("xposed") != std::string::npos ||
                                line.find("lspd") != std::string::npos ||
                                line.find("tmpfs /system") != std::string::npos) {
                                return true;
                            }
                        }
                        mounts.close();
                    }
                    return false;
                }

                static bool checkClassLoader() {
                    std::ifstream maps("/proc/self/maps");
                    std::string line;
                    
                    if (maps.is_open()) {
                        while (std::getline(maps, line)) {
                            if (line.find("XposedBridge.jar") != std::string::npos ||
                                line.find("lspd.dex") != std::string::npos) {
                                return true;
                            }
                        }
                        maps.close();
                    }
                    return false;
                }

                static bool checkEnvironmentVariables() {
                    if (getenv("CLASSPATH") != nullptr) {
                        std::string classpath = getenv("CLASSPATH");
                        if (classpath.find("XposedBridge") != std::string::npos ||
                            classpath.find("lspd") != std::string::npos) {
                            return true;
                        }
                    }
                    
                    return getenv("PATH") != nullptr && 
                           std::string(getenv("PATH")).find("su") != std::string::npos;
                }

                static bool checkProps() {
                    char buf[PROP_VALUE_MAX];
                    
                    __system_property_get("ro.debuggable", buf);
                    if (strcmp(buf, "1") == 0) return true;

                    __system_property_get("ro.secure", buf);
                    if (strcmp(buf, "0") == 0) return true;

                    __system_property_get("init.svc.su", buf);
                    if (strlen(buf) > 0) return true;

                    __system_property_get("xposed", buf);
                    if (strlen(buf) > 0) return true;

                    __system_property_get("lsp", buf);
                    if (strlen(buf) > 0) return true;

                    return false;
                }

                static bool checkBusyboxCommands() {
                    const char* commands[] = {
                        "ash", "awk", "chattr", "chmod", "chown", "crond", "crontab",
                        "cut", "date", "dd", "df", "dmesg", "du", "grep", "kill",
                        "killall", "mount", "ps", "renice", "sed", "su", "sync",
                        "tar", "touch", "umount", "watch"
                    };

                    for (const char* cmd : commands) {
                        std::string path = "/system/xbin/" + std::string(cmd);
                        if (checkBinaryExists(path)) {
                            std::ifstream binary(path, std::ios::binary);
                            if (binary.is_open()) {
                                char buffer[1024];
                                binary.read(buffer, sizeof(buffer));
                                std::string content(buffer, binary.gcount());
                                if (content.find("BusyBox") != std::string::npos) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }

                static bool checkMagiskHide() {
                    DIR* dir = opendir("/proc");
                    if (dir == nullptr) return false;

                    struct dirent* entry;
                    while ((entry = readdir(dir)) != nullptr) {
                        if (entry->d_type == DT_DIR && isdigit(entry->d_name[0])) {
                            std::string cmdlinePath = "/proc/" + std::string(entry->d_name) + "/cmdline";
                            std::ifstream cmdline(cmdlinePath);
                            std::string line;
                            if (std::getline(cmdline, line)) {
                                if (line.find("magiskd") != std::string::npos ||
                                    line.find("magiskhide") != std::string::npos) {
                                    closedir(dir);
                                    return true;
                                }
                            }
                        }
                    }
                    closedir(dir);
                    return false;
                }

            public:
                struct SecurityStatus {
                    bool root = false;
                    bool magisk = false;
                    bool kernelsu = false;
                    bool xposed = false;
                    bool lsposed = false;
                    bool busybox = false;
                };

                static SecurityStatus detectThreats() {
                    SecurityStatus status;
                    int rootScore = 0;
                    int xposedScore = 0;
                    int busyboxScore = 0;

                    // Check binaries and paths
                    for (const auto& path : getRootBinaries()) {
                        if (checkBinaryExists(path)) rootScore += 3;
                    }

                    for (const auto& path : getMagiskModulePaths()) {
                        if (checkBinaryExists(path)) {
                            rootScore += 2;
                            status.magisk = true;
                        }
                    }

                    for (const auto& path : getKernelsuPaths()) {
                        if (checkBinaryExists(path)) {
                            rootScore += 2;
                            status.kernelsu = true;
                        }
                    }

                    for (const auto& path : getXposedPaths()) {
                        if (checkBinaryExists(path)) {
                            xposedScore += 2;
                            if (path.find("lsp") != std::string::npos) {
                                status.lsposed = true;
                            } else {
                                status.xposed = true;
                            }
                        }
                    }

                    for (const auto& path : getBusyboxPaths()) {
                        if (checkBinaryExists(path)) busyboxScore += 3;
                    }

                    // Additional checks
                    if (checkRWAccess()) rootScore += 3;
                    if (checkProcMaps()) {
                        rootScore += 2;
                        xposedScore += 2;
                    }
                    if (checkMountPoints()) {
                        rootScore += 2;
                        xposedScore += 2;
                    }
                    if (checkEnvironmentVariables()) rootScore += 1;
                    if (checkProps()) {
                        rootScore += 2;
                        xposedScore += 2;
                    }
                    if (checkMagiskHide()) {
                        rootScore += 3;
                        status.magisk = true;
                    }
                    if (checkClassLoader()) {
                        xposedScore += 3;
                    }
                    if (checkBusyboxCommands()) {
                        busyboxScore += 3;
                    }

                    // Set final status
                    status.root = (rootScore >= 5);
                    if (!status.xposed && !status.lsposed) {
                        status.xposed = (xposedScore >= 4);
                    }
                    status.busybox = (busyboxScore >= 3);

                    return status;
                }
            };
        }
    }
}


extern "C" JNIEXPORT jint JNICALL
Java_com_dev_detector_Qwanwin_implement_RootRobust_nativeCheck(JNIEnv* env, jobject /* this */) {
    auto status = dev::Qwanwin::implement::AntiRoot::detectThreats();
    
    int result = 0;
    if (status.root) result |= 0x1;
    if (status.magisk) result |= 0x2;
    if (status.kernelsu) result |= 0x4;
    if (status.xposed) result |= 0x8;
    if (status.lsposed) result |= 0x10;
    if (status.busybox) result |= 0x20;
    
    return result;
}