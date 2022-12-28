#define MQTTCLIENT_QOS1 0
#define MQTTCLIENT_QOS2 0

#include "mbed.h"
#include "mbed-trace/mbed_trace.h"
#include "mbed_events.h"
#include "mbedtls/error.h"

#include "Config/MQTT_server_setting.h"
#include "IoTUtils/Network.h"
#include "IoTUtils/AccessPoint.h"
#include "IoTUtils/AWSService.h"

enum Action{
    GET_CREDENTIALS,
    CONNECT_AWS_MQTT,
    SUBSCRIBE_TOPICS,
    PUBLISH_DATA,
    RESTART,
    EXIT
};

int main(int argc, char* argv[]){
    // ----------------------------------- TMP to delete
    char SSID[] = "bogdandorota162";
    char PASSWORD[] = "internetBARSZCZAK";
    nsapi_security WIFI_SECURITY = NSAPI_SECURITY_WPA_WPA2;
    // ----------------------------------- TMP to delete

    // initialize mbed
    mbed_trace_init();

    // const fields
    const int BUFFER_SIZE = 256;
    const int DELAY_MS = 500;
    const bool LOG = true;
    const bool FORCE = true;
    
    // endpoints
    char MQTT_TOPIC_PUB[] = "send_data";
    char MQTT_TOPIC_SUB[] = "send_data";

    // fields
    Action action = GET_CREDENTIALS;
    AccessPoint *accessPoint = nullptr;
    Network *network = nullptr;
    AWSService *service = nullptr;
    DigitalIn userButton(USER_BUTTON);

    while(1){
        if(userButton.read() == 1){ // the user button was pressed
            action = RESTART;
        }

        if(action == GET_CREDENTIALS){ // pair with the device and get credentials
            // todo: pair with the device and get the credentials
            action = CONNECT_AWS_MQTT;
        }
        else if(action == CONNECT_AWS_MQTT){ // connect to the wifi
            // connect to wifi
            network = new Network(SSID, PASSWORD, WIFI_SECURITY, LOG);
            if(!network->init()){ // no wifi interface was found
                action = EXIT;
                continue;
            }
            if(!network->connect(FORCE)){ // propably wrong credentials
                action = RESTART;
                continue;
            }

            // connect to AWS MQTT
            service = new AWSService(BUFFER_SIZE, LOG);
            if(!service->connect(network->getWiFiInterface())){ // server is neither available nor the certificates are ok
                action = EXIT;
                continue;
            }

            action = SUBSCRIBE_TOPICS;
        }
        else if(action == SUBSCRIBE_TOPICS){ // subscribing the topics
            if(!service->subscribe(MQTT_TOPIC_SUB)){ // the connection could be interrupted
                action = RESTART;
                continue;
            }
            action = PUBLISH_DATA;
        }
        else if(action == PUBLISH_DATA){ // publishing the data
            service->yield();
            if(!service->publish(MQTT_TOPIC_PUB, "hello world!")){  // the connection could be interrupted
                action = RESTART;
                continue;
            }
            thread_sleep_for(DELAY_MS);
        }
        else if(action == RESTART || action == EXIT){ // either restarting or exiting the app
            if(accessPoint != nullptr){
                //accessPoint->disconnect();
                delete accessPoint;
                accessPoint = nullptr;
            }

            if(service != nullptr){
                service->disconnect();
                delete service;
                service = nullptr;
            }

            if(network!=nullptr){
                network->disconnect();
                delete network;
                network = nullptr;
            }

            if(action == RESTART) action = GET_CREDENTIALS;
            else break;
        }
    }

    return EXIT_SUCCESS;
}
