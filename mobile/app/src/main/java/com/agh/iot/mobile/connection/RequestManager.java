package com.agh.iot.mobile.connection;

public interface RequestManager {

    int insertNewUser(String name, String email, String username, String password);

    String areCredentialsValid(String username, String password);
}
