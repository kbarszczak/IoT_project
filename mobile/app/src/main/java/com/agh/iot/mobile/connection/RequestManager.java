package com.agh.iot.mobile.connection;

import java.util.List;

public interface RequestManager {

    int insertNewUser(String name, String email, String username, String password);

    String areCredentialsValid(String username, String password);

    List<String> getAllDevices(String tokenJWT);

    String getData(String tokenJWT, String deviceID);

    String pairDevice(String tokenJWT, String deviceID, String secret);
}
