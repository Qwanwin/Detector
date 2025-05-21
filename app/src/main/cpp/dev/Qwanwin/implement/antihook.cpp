#include "antihook.h"
#include <android/log.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fstream>
#include <sstream>
#include <string>

namespace dev {
namespace Qwanwin {
namespace implement {

AntiHook::AntiHook() {}

bool AntiHook::protectLibrary(const std::string& libName) {
    void* base = getLibraryBase(libName);
    if (!base) {
        return false;
    }
    
    size_t size = getLibrarySize(base);
    if (size == 0) {
        return false;
    }
    
    return lockMemoryRegion(base, size);
}

bool AntiHook::verifyLibraryIntegrity() {
    return true;
}

bool AntiHook::checkMemoryIntegrity() {
    return true;
}

void* AntiHook::getLibraryBase(const std::string& libName) {
    try {
        std::ifstream maps("/proc/self/maps");
        std::string line;

        while (std::getline(maps, line)) {
            if (line.find(libName) != std::string::npos &&
                line.find("r-xp") != std::string::npos) {
                std::istringstream iss(line);
                std::string addr;
                if (std::getline(iss, addr, '-')) {
                    uintptr_t base = std::stoul(addr, nullptr, 16);
                    return reinterpret_cast<void*>(base);
                }
            }
        }
    } catch (const std::exception& e) {
    }

    return nullptr;
}

size_t AntiHook::getLibrarySize(void* base) {
    if (!base) return 0;

    try {
        uintptr_t baseAddr = reinterpret_cast<uintptr_t>(base);
        uintptr_t endAddr = 0;

        std::ifstream maps("/proc/self/maps");
        std::string line;

        while (std::getline(maps, line)) {
            std::istringstream iss(line);
            std::string addrRange;
            if (std::getline(iss, addrRange, ' ')) {
                size_t dash = addrRange.find('-');
                if (dash == std::string::npos) continue;

                uintptr_t start = std::stoul(addrRange.substr(0, dash), nullptr, 16);
                uintptr_t end = std::stoul(addrRange.substr(dash + 1), nullptr, 16);

                if (start == baseAddr) {
                    endAddr = end;
                    break;
                }
            }
        }

        return endAddr > baseAddr ? (endAddr - baseAddr) : 0;
    } catch (const std::exception& e) {
        return 0;
    }
}

bool AntiHook::lockMemoryRegion(void* addr, size_t size) {
    if (!addr || size == 0) {
        return false;
    }

    long pageSize = sysconf(_SC_PAGESIZE);
    void* alignedAddr = reinterpret_cast<void*>(
        reinterpret_cast<uintptr_t>(addr) & ~(pageSize - 1));
    
    size_t alignedSize = (size + pageSize - 1) & ~(pageSize - 1);

    std::ifstream maps("/proc/self/maps");
    std::string line;
    bool found = false;
    int currentProt = PROT_READ;

    while (std::getline(maps, line)) {
        uintptr_t start, end;
        char perms[5];
        if (sscanf(line.c_str(), "%lx-%lx %4s", &start, &end, perms) == 3) {
            if (start == reinterpret_cast<uintptr_t>(alignedAddr)) {
                found = true;
                if (perms[0] == 'r') currentProt |= PROT_READ;
                if (perms[1] == 'w') currentProt |= PROT_WRITE;
                if (perms[2] == 'x') currentProt |= PROT_EXEC;
                break;
            }
        }
    }

    if (!found) {
        return false;
    }

   
    int newProt = currentProt & ~PROT_WRITE;
    
    return mprotect(alignedAddr, alignedSize, newProt) == 0;
}

} 
} 
} 

extern "C" {

static dev::Qwanwin::implement::AntiHook* gAntiHook = nullptr;

JNIEXPORT jboolean JNICALL
Java_com_dev_detector_Qwanwin_implement_AntiHook_nativeInit(JNIEnv* env, jobject thiz) {
    if (!gAntiHook) {
        gAntiHook = new dev::Qwanwin::implement::AntiHook();
    }
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_dev_detector_Qwanwin_implement_AntiHook_nativeProtectLibrary(JNIEnv* env, jobject thiz, jstring libName) {
    if (!gAntiHook) return false;
    
    const char* str = env->GetStringUTFChars(libName, nullptr);
    bool result = gAntiHook->protectLibrary(str);
    env->ReleaseStringUTFChars(libName, str);
    
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_dev_detector_Qwanwin_implement_AntiHook_nativeCheckIntegrity(JNIEnv* env, jobject thiz) {
    if (!gAntiHook) return false;
    return gAntiHook->verifyLibraryIntegrity() && gAntiHook->checkMemoryIntegrity();
}

}