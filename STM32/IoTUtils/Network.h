#ifndef IOT_UTIL_NETWORK
#define IOT_UTIL_NETWORK

#include "TLSSocket.h"
#include "mbed.h"
#include <string>
#include <utility>

class Network{

private:

    // private attributes
    WiFiInterface *wifi;
    std::string ssid;
    std::string password;
    nsapi_security security;
    bool log;

public:

    // construcotrs & destrucotrs
    explicit Network(std::string ssid="", std::string password="", nsapi_security security = NSAPI_SECURITY_WPA_WPA2, bool log=true);
    ~Network();

    // get methods
    [[nodiscard]] WiFiInterface* getWiFiInterface();
    [[nodiscard]] const std::string&getSSID() const;
    [[nodiscard]] const std::string&getPassword() const;
    [[nodiscard]] nsapi_connection_status_t getStatus() const;
    [[nodiscard]] bool isLogging() const;

    // set methods
    void setSSID(std::string ssid);
    void setPassword(std::string password);
    void setLogging(bool log);

    // public methods
    bool init();
    bool connect(bool force = false);
    bool reconnect(bool force = false);
    bool disconnect();

};

#endif // IOT_UTIL_NETWORK