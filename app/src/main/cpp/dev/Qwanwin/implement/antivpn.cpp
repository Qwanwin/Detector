#include "antivpn.h"
#include <dirent.h>
#include <algorithm>

namespace dev {
namespace Qwanwin {
namespace implement {

AntiVPN::AntiVPN() {}

AntiVPN::~AntiVPN() {}

bool AntiVPN::isVPNActive() const {
    return checkVPNInterfaces() || checkVPNProcesses();
}

bool AntiVPN::checkVPNInterfaces() const {
    auto interfaces = getNetworkInterfaces();
    for (const auto& interface : interfaces) {
        for (const auto& vpn : vpnInterfaces) {
            if (interface.find(vpn) != std::string::npos) {
                return true;
            }
        }
    }
    return false;
}

bool AntiVPN::checkVPNProcesses() const {
    DIR* dir = opendir("/proc");
    if (!dir) {
        return false;
    }

    bool found = false;
    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type == DT_DIR && isdigit(entry->d_name[0])) {
            std::string cmdlinePath = "/proc/" + std::string(entry->d_name) + "/cmdline";
            FILE* cmdline = fopen(cmdlinePath.c_str(), "r");
            if (cmdline) {
                char buffer[256];
                if (fgets(buffer, sizeof(buffer), cmdline)) {
                    std::string processName(buffer);
                    for (const auto& vpn : vpnProcesses) {
                        if (processName.find(vpn) != std::string::npos) {
                            found = true;
                            break;
                        }
                    }
                }
                fclose(cmdline);
            }
        }
        if (found) break;
    }
    
    closedir(dir);
    return found;
}

std::vector<std::string> AntiVPN::getNetworkInterfaces() const {
    std::vector<std::string> interfaces;
    DIR* dir = opendir("/sys/class/net");
    if (dir) {
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (entry->d_name[0] != '.') {
                interfaces.push_back(entry->d_name);
            }
        }
        closedir(dir);
    }
    return interfaces;
}

}
} 
} 