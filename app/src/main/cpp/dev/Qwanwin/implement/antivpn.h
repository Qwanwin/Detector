#ifndef ANTIVPN_H
#define ANTIVPN_H

#include <string>
#include <vector>

namespace dev {
namespace Qwanwin {
namespace implement {

class AntiVPN {
public:
    AntiVPN();
    ~AntiVPN();
    
    bool isVPNActive() const;

private:
    bool checkVPNInterfaces() const;
    bool checkVPNProcesses() const;
    std::vector<std::string> getNetworkInterfaces() const;
    
    const std::vector<std::string> vpnInterfaces = {
        "tun0", "ppp0", "pptp0", "l2tp0", "ipsec0", "vpn0"
    };
    
    const std::vector<std::string> vpnProcesses = {
        "openvpn", "vpnc", "vpnd", "pptp", "strongswan", "nordvpn", "expressvpn", "pandavpn"
    };
};

} 
} 
} 

#endif 