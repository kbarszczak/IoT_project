#include "AccessPoint.h"

AccessPoint::AccessPoint(std::string ssid, std::string password, nsapi_security security,  bool log)
:ssid(ssid), password(password), security(security), log(log){}

AccessPoint::~AccessPoint(){}

bool AccessPoint::setUpAp(ISM43362Interface *interface){
    // set up ap ssid & password & security
    if(interface->getIsm().setCredentialsForAP(ssid.c_str(), password.c_str(), security) != 0){
        if(log) printf("Could not set credentials for soft AP\n");
        return false;
    }
    if(log) printf("Credentials set up correctly. SSID: '%s' PASSWORD: '%s' SECURITY: '%d'\n", ssid.c_str(), password.c_str(), security);

    // start soft ap and create server for exchanging data
    if(interface->getIsm().startSoftAp() != 0){
        if(log) printf("Could not start soft AP\n");
        return false;
    }
    if(log) printf("Soft AP successfully started\n");

    return true;
}

bool AccessPoint::startServer(ISM43362Interface *interface){
    // wait for client to set up internet details on the page
    if(interface->getIsm().listenForConnection() != 0){
        if(log) printf("Could not exchange data\n");
        return false;
    }

    if(log) printf("SSID & PASSWORD successfully exchanged. Connected to the network '%s'\n", interface->get_ip_address());
    return true;
}
