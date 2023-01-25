#ifndef IOT_UTIL_ACCESS_POINT
#define IOT_UTIL_ACCESS_POINT

#include <string>
#include "mbed.h"
#include "ISM43362Interface.h"

class AccessPoint{

private:

    std::string ssid;
    std::string password;
    nsapi_security security;
    bool log;

public:

    explicit AccessPoint(std::string ssid, std::string password, nsapi_security security, bool log);
    ~AccessPoint();

    bool setUpAp(ISM43362Interface *interface);
    bool startServer(ISM43362Interface *interface);

};

#endif