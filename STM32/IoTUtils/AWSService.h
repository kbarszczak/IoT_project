#ifndef IOT_UTIL_AWS_SERVICE
#define IOT_UTIL_AWS_SERVICE

#include "Config/MQTT_server_setting.h"
#include "MQTTClientMbedOs.h"
#include "TLSSocket.h"
#include <list>

class AWSService{

private:

    // private attributes
    MQTTClient *client;
    TLSSocket *socket;
    int bufferSize;
    char *buffer;
    bool log;
    std::list<char*> subscribes;

public:

    // constructors & destructors
    AWSService(int bufferSize = 1000, bool log = false);
    ~AWSService();

    // get methods
    [[nodiscard]] int getBufferSize() const;
    [[nodiscard]] bool isLogging() const;
    [[nodiscard]] const std::list<char*> &getSubscribed() const;

    // set methods
    void setBufferSize(int bufferSize);
    void setLogging(bool log);

    // public methods
    bool connect(NetworkInterface *network);
    bool disconnect();
    bool subscribe(char topic[]);
    bool unsubscribe(char topic[]);
    bool publish(char topic[], char value[]);
    bool yield() const;

    // static methods
    static void handleMessage(MQTT::MessageData &md);

};

#endif // IOT_UTIL_AWS_SERVICE