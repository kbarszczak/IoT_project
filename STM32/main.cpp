#include <cstdio>
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
    PUBLISH_DATA,
    RESTART,
    EXIT
};

const char *DEVICE_ID = "DEVICE001";

int main(int argc, char* argv[]){
    // initialize mbed
    mbed_trace_init();

    // const fields
    const int BUFFER_SIZE = 256;
    const int DELAY_MS = 500;
    const bool LOG = true;
    const bool FORCE = true;
    
    // endpoints
    char MQTT_TOPIC_PUB[] = "send_data";

    // fields
    Action action = GET_CREDENTIALS;
    ISM43362Interface *interface = nullptr;
    AWSService *service = nullptr;
    DigitalIn userButton(USER_BUTTON);

    while(1){
        if(userButton.read() == 1){ // the user button was pressed
            action = RESTART;
        }

        if(action == GET_CREDENTIALS){ // pair with the device and get credentials
            if(interface == nullptr) interface = new ISM43362Interface();

            AccessPoint access("IoT_PROJECT", "aghproject", NSAPI_SECURITY_WPA2, LOG);
            if(!access.setUpAp(interface)){
                action = RESTART;
                continue;
            }

            if(!access.startServer(interface)){
                action = RESTART;
                continue;
            }

            action = CONNECT_AWS_MQTT;
        }
        else if(action == CONNECT_AWS_MQTT){ // connect to the wifi
            // connect to AWS MQTT
            service = new AWSService(BUFFER_SIZE, LOG);
            if(!service->connect(interface)){ // server is neither available nor the certificates are ok
                action = RESTART;
                continue;
            }

            action = PUBLISH_DATA;
        }
        else if(action == PUBLISH_DATA){ // publishing the data
            service->yield();

            // creating data to send
            char payload[50];
            sprintf(payload, "{\n\tdeviceId: \"%s\",\n\tdata: \"%d\"\n}", DEVICE_ID, ((rand() % 20) + 20));

            // sending data to the mqtt server
            if(!service->publish(MQTT_TOPIC_PUB, payload)){
                action = RESTART;
                continue;
            }
            thread_sleep_for(DELAY_MS);
        }
        else if(action == RESTART || action == EXIT){ // either restarting or exiting the app
            if(service != nullptr){
                service->disconnect();
                delete service;
                service = nullptr;
            }

            if(interface != nullptr){
                interface->disconnect();
                delete interface;
                interface = nullptr;
            }

            if(action == RESTART) action = GET_CREDENTIALS;
            else break;
        }
    }

    printf("Exiting the application...\n");

    return EXIT_SUCCESS;
}
