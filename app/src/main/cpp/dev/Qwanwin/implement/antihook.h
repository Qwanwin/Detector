#ifndef ANTIHOOK_H
#define ANTIHOOK_H

#include <jni.h>
#include <string>

namespace dev {
namespace Qwanwin {
namespace implement {

class AntiHook {
public:
    AntiHook();
    bool protectLibrary(const std::string& libName);
    bool verifyLibraryIntegrity();
    bool checkMemoryIntegrity();

private:
    void* getLibraryBase(const std::string& libName);
    size_t getLibrarySize(void* base);
    bool lockMemoryRegion(void* addr, size_t size);
};

} 
} 
} 

#endif 