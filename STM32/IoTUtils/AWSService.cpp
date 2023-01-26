#include "AWSService.h"

AWSService::AWSService(int bufferSize, bool log)
:client(nullptr), bufferSize(bufferSize), buffer(nullptr), log(log){
    setBufferSize(bufferSize);
}

AWSService::~AWSService(){
    if(buffer == nullptr) return;
    delete []buffer;
    buffer = nullptr;
}

int AWSService::getBufferSize() const{
    return bufferSize;
}

bool AWSService::isLogging() const{
    return log;
}

const std::list<char*> &AWSService::getSubscribed() const{
    return subscribes;
}

void AWSService::setBufferSize(int bufferSize){
    if(bufferSize <= 0) bufferSize = 100;
    this->bufferSize = bufferSize;
    if(buffer != nullptr) delete [] buffer;
    buffer = new char [bufferSize];
}

void AWSService::setLogging(bool log){
    this->log = log;
}

bool AWSService::connect(NetworkInterface *network){
    if(log) printf("Connecting to the AWS MQTT host '%s:%d'...\n", MQTT_SERVER_HOST_NAME, MQTT_SERVER_PORT);
    if(network == nullptr){
        if(log) printf("Network interface is null.\n");
        return false;
    }

    socket = new TLSSocket();
    nsapi_error_t ret = socket->open(network);
    if (ret != NSAPI_ERROR_OK) {
        if(log) printf("Could not open socket! Returned '%d'.\n", ret);
        return false;
    }
    if(log) printf("Socket for AWS opened\n");
    thread_sleep_for(300);

    ret = socket->set_root_ca_cert(SSL_CA_PEM);
    if (ret != NSAPI_ERROR_OK) {
        if(log) printf("Could not set ca cert! Returned '%d'.\n", ret);
        return false;
    }
    if(log) printf("CA CERT set\n");
    thread_sleep_for(300);

    ret = socket->set_client_cert_key(SSL_CLIENT_CERT_PEM, SSL_CLIENT_PRIVATE_KEY_PEM);
    if (ret != NSAPI_ERROR_OK) {
        if(log) printf("Could not set keys! Returned '%d'.\n", ret);
        return false;
    }
    if(log) printf("Private key set\n");
    thread_sleep_for(300);

    ret = socket->connect(MQTT_SERVER_HOST_NAME, MQTT_SERVER_PORT);
    if (ret != NSAPI_ERROR_OK) {
        if(log) printf("Could not connect! Returned '%d'.\n", ret);
        return false;
    }
    if(log) printf("Socket connected\n");
    thread_sleep_for(300);

    MQTTPacket_connectData data = MQTTPacket_connectData_initializer;
    data.MQTTVersion = 3;
    data.clientID.cstring = (char *)MQTT_CLIENT_ID;
    data.username.cstring = (char *)MQTT_USERNAME;
    data.password.cstring = (char *)MQTT_PASSWORD;

    client = new MQTTClient(socket);
    int rc = client->connect(data);
    if (rc != MQTT::SUCCESS) {
        if(log) printf("Could not connect to MQTT host. Returned '%d'.\n", rc);
        return false;
    }

    if(log) printf("Connection established.\n");
    return true;
}

bool AWSService::disconnect(){
    if(client == nullptr) return false;

    for(const auto&topic : subscribes){
        client->unsubscribe(topic);
    }
    subscribes.clear();

    if(client->isConnected()) client->disconnect();
    delete client;
    client = nullptr;

    socket->close();
    delete socket;
    socket = nullptr;

    if(log) printf("AWS Service disconnected successfully.\n");

    return true;
}

bool AWSService::subscribe(char topic[]){
    if(log) printf("Subscribing the following topic '%s'...\n", topic);

    int rc = client->subscribe(topic, MQTT::QOS0, handleMessage);
    if (rc != MQTT::SUCCESS) {
        if(log) printf("Could not subscribe the topic. Returned: %d.\n", rc);
        return false;
    }

    subscribes.push_back(topic);
    if(log) printf("The topic was subscribed successfully.\n");
    return true;
}

bool AWSService::unsubscribe(char topic[]){
    if(log) printf("Unsubscribing the following topic '%s'...\n", topic);

    int rc = client->unsubscribe(topic);
     if (rc != MQTT::SUCCESS) {
        if(log) printf("Could not unsubscribe the topic. Returned: %d.\n", rc);
        return false;
    }

    subscribes.remove(topic);
    if(log) printf("The topic was unsubscribed successfully.\n");
    return true;
}

bool AWSService::publish(char topic[], char value[]){
    if(log) printf("Publishing '%s' to the following topic '%s'...\n", value, topic);

    if(!client->isConnected()){
        if(log) printf("Could not publish. Client is not connected.\n");
        return false;
    }

    if(strlen(value) >= bufferSize){
        if(log) printf("Could not publish. The buffer size is not big enough to contain the value.\n");
        return false;
    }

    static unsigned short id = 0;

    MQTT::Message message;
    message.retained = false;
    message.dup = false;
    message.payload = (void*)buffer;
    message.qos = MQTT::QOS0;
    message.id = id;

    int ret = snprintf(buffer, bufferSize, "%s", value);
    if(ret < 0) {
        printf("Could not prepare the message. Returned: %d.\n", ret);
        return false;
    }
    message.payloadlen = ret;

    ret = client->publish(topic, message);
    if(ret != MQTT::SUCCESS) {
        printf("Could not publish the value. Returned: %d.\n", ret);
        return false;
    }

    if(log) printf("The value was successfully published.\n");
    return true;
}

bool AWSService::yield() const{
    int ret = client->yield();
    if(ret != MQTT::SUCCESS) {
        return false;
    }
    return true;
}

void AWSService::handleMessage(MQTT::MessageData &md){
    //     void messageArrived(MQTT::MessageData& md)
    // {
    //     // Copy payload to the buffer.
    //     MQTT::Message &message = md.message;
    //     if(message.payloadlen >= MESSAGE_BUFFER_SIZE) {
    //         // TODO: handling error
    //     } else {
    //         memcpy(messageBuffer, message.payload, message.payloadlen);
    //     }
    //     messageBuffer[message.payloadlen] = '\0';
    // }
    printf("Received: '%s'.\n", (char *)md.message.payload);
}