#include "Network.h"
#include <stdexcept>

Network::Network(std::string ssid, std::string password, nsapi_security security, bool log)
:ssid(std::move(ssid)), password(std::move(password)), security(security), log(log), wifi(nullptr){}

Network::~Network(){}

WiFiInterface* Network::getWiFiInterface(){
    return wifi;
}

const std::string&Network::getSSID() const{
    return ssid;
}

const std::string&Network::getPassword() const{
    return password;
}

nsapi_connection_status_t Network::getStatus() const{
    if(wifi == nullptr) return NSAPI_STATUS_DISCONNECTED;
    return wifi->get_connection_status();
}

bool Network::isLogging() const{
    return log;
}

void Network::setSSID(std::string ssid){
    this->ssid.replace(0, this->ssid.length(), ssid);
    if(log) printf("SSID set to '%s'\n", this->ssid.c_str());
}

void Network::setPassword(std::string password){
    this->password.replace(0, this->password.length(), password);
    if(log) printf("Password set to '%s'\n", this->password.c_str());
}

void Network::setLogging(bool log){
    this->log = log;
    if(log) printf("Log set to '%s'\n", log ? "true" : "false");
}

bool Network::init(){
    if(log) printf("Initializing wifi interface.\n");
    wifi = WiFiInterface::get_default_instance();
    if(wifi == nullptr){
        if(log) printf("The wifi interface was not found.\n");
        return false;
    }
    return true;
}

bool Network::connect(bool force){
    if(log) printf("Connecting to '%s'...\n", ssid.c_str());
    wifi->set_credentials(ssid.c_str(), password.c_str(), security);
    nsapi_size_or_error_t status = wifi->connect();
    if (status) {
        if(log) printf("Unable to connect! WiFiInterface returned: %d.\n", status);
        if(!force) return false;

        if(log) printf("Trying to connect with other security modes.\n");
        nsapi_security securities[] = {
            NSAPI_SECURITY_NONE, NSAPI_SECURITY_WPA_WPA2, 
            NSAPI_SECURITY_WPA, NSAPI_SECURITY_WPA2, 
            NSAPI_SECURITY_CHAP, NSAPI_SECURITY_EAP_TLS, 
            NSAPI_SECURITY_PAP, NSAPI_SECURITY_PEAP, 
            NSAPI_SECURITY_WEP, NSAPI_SECURITY_WPA2_ENT
        };

        for(int i=0; i<10; ++i){
            if(log) printf("Attempt %d. Connecting with '%d' security mode.\n", (i+1), securities[i]);
            wifi->set_credentials(ssid.c_str(), password.c_str(), securities[i]);
            status = wifi->connect();
            if(status == 0){
                if(log) printf("Connected via '%d' security mode.\n", securities[i]);
                break;
            }
        }

        if(status) {
            if(log) printf("Unable to connect the '%s'. Please verify the credentials.\n", ssid.c_str());
            return false;
        }
    }
    if(log) printf("Successfully connected to '%s'.\n", ssid.c_str());
    return true;
}

bool Network::reconnect(bool force){
    if(log) printf("Reconnecting the wifi...\n");
    nsapi_connection_status_t status = wifi->get_connection_status();
    if(!force && status != NSAPI_STATUS_DISCONNECTED && status != NSAPI_STATUS_ERROR_UNSUPPORTED){
        if(log) printf("Cannot reconnect due to the current connection status. Try with the force flag set to true.\n");
        return false;
    }

    if(!wifi->disconnect()) return false;
    if(connect(force)){
        if(log) printf("Successfully reconnected.\n");
        return true;
    } else {
        if(log) printf("Could not reconnect.\n");
        return false;
    }
}

bool Network::disconnect(){
    if(wifi == nullptr) return false;
    wifi->disconnect();
    wifi = nullptr;
    if(log) printf("Network disconnected successfully.\n");
    return true;
}