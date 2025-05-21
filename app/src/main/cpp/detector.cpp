#include "detector.h"
#include <dlfcn.h>
#include <android/log.h>
#include <sys/mman.h>
#include <link.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

namespace dev {
    namespace Qwanwin {
        namespace implement {
            class PineDetector {
            private:
                static bool checkArtMethodHook() {
                    void* handle = dlopen("libart.so", RTLD_NOW);
                    if (!handle) return false;

                    bool detected = false;
                    void* artMethod = dlsym(handle, "_ZN3art9ArtMethod12PrettyMethodEb");

                    if (artMethod) {
                        Dl_info info;
                        if (dladdr(artMethod, &info)) {
                            if (info.dli_saddr != artMethod && 
                                strstr(info.dli_fname, "libart.so") == nullptr) {
                                detected = true;
                            }
                        }
                    }

                    dlclose(handle);
                    return detected;
                }

                static bool checkMemoryPermissions() {
                    char maps_path[64];
                    snprintf(maps_path, sizeof(maps_path), "/proc/%d/maps", getpid());

                    FILE* maps = fopen(maps_path, "r");
                    if (!maps) return false;

                    char line[512];
                    bool detected = false;

                    while (fgets(line, sizeof(line), maps)) {
                        if (strstr(line, "libart.so")) {
                            if ((strstr(line, "rwxp") && !strstr(line, "/system/")) || 
                                (strstr(line, "wx") && strstr(line, "private"))) {
                                detected = true;
                                break;
                            }
                        }
                    }

                    fclose(maps);
                    return detected;
                }

                static bool checkInterpreterHook() {
                    void* handle = dlopen("libart.so", RTLD_NOW);
                    if (!handle) return false;

                    bool detected = false;
                    const char* symbolsToCheck[] = {
                        "art_quick_to_interpreter_bridge",
                        "_ZN3art11interpreter6DoCallEPNS_9ArtMethodEPNS_6ThreadE",
                        "_ZN3art11interpreter15ExecuteGotoImplEPNS_6ThreadE"
                    };

                    for (const char* symbol : symbolsToCheck) {
                        void* func = dlsym(handle, symbol);
                        if (func) {
                            Dl_info info;
                            if (dladdr(func, &info)) {
                                if (info.dli_saddr != func) {
                                    unsigned char* code = (unsigned char*)func;
                                    if (code[0] == 0xFF || code[0] == 0xE9 || code[0] == 0xB8) {
                                        detected = true;
                                        break;
                                    }
                                }
                                if (info.dli_fname && 
                                    (!strstr(info.dli_fname, "/system/") || 
                                     strstr(info.dli_fname, "hook") || 
                                     strstr(info.dli_fname, "pine"))) {
                                    detected = true;
                                    break;
                                }
                            }
                        }
                    }

                    dlclose(handle);
                    return detected;
                }

                static bool verifyCodeIntegrity(void* address) {
                    if (!address) return false;

                    Dl_info info;
                    if (!dladdr(address, &info)) return false;

                    char maps_path[64];
                    snprintf(maps_path, sizeof(maps_path), "/proc/%d/maps", getpid());

                    FILE* maps = fopen(maps_path, "r");
                    if (!maps) return false;

                    char line[512];
                    bool isValid = false;

                    while (fgets(line, sizeof(line), maps)) {
                        if (strstr(line, info.dli_fname)) {
                            if (strstr(line, "r-xp") && strstr(line, "/system/")) {
                                isValid = true;
                                break;
                            }
                        }
                    }

                    fclose(maps);
                    return isValid;
                }

            public:
                static int detectPine() {
                    if (checkArtMethodHook()) {
                        return SECURITY_ART_METHOD;
                    }

                    if (checkMemoryPermissions()) {
                        return SECURITY_MEMORY_PERM;
                    }

                    if (checkInterpreterHook()) {
                        return SECURITY_INTERPRETER;
                    }

                    return SECURITY_CLEAN;
                }
            };
        }
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_dev_detector_Qwanwin_implement_DetectorVPN_nativeCheckVPN(JNIEnv* env, jobject) {
    dev::Qwanwin::implement::AntiVPN vpnChecker;
    return vpnChecker.isVPNActive() ? SECURITY_VPN : SECURITY_CLEAN;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_dev_detector_Qwanwin_implement_PineDetector_nativeCheck(JNIEnv* env, jobject) {
    return dev::Qwanwin::implement::PineDetector::detectPine();
}
