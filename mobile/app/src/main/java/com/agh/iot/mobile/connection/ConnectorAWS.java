package com.agh.iot.mobile.connection;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;

public class ConnectorAWS implements RequestManager {
    private static final String URL = "https://8wrzkt10cc.execute-api.eu-central-1.amazonaws.com/prod/";
    private static final String X_API_KEY = "O7Q0basw0w5vwo0BXuBEb30X5clhb8x7gQ9RRlS7";

    @Override
    public int insertNewUser(String name, String email, String username, String password) {

        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("email", email);
            json.put("username", username);
            json.put("password", password);//BCrypt.hashpw(password, BCrypt.gensalt()));
        } catch (JSONException e) {
            return 403;
        }

        AtomicInteger responseCode = new AtomicInteger();

        Thread thread = new Thread(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(URL + "register").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("x-api-key", X_API_KEY);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);


                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(json.toString());
                outputStreamWriter.flush();

                responseCode.set(urlConnection.getResponseCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return responseCode.get();
    }


    @Override
    public String areCredentialsValid(String username, String password) {
        JSONObject json = new JSONObject();

        try {
            json.put("username", username);
            json.put("password", password); //BCrypt.hashpw(password, BCrypt.gensalt()));
        } catch (JSONException e) {
            return null;
        }

        AtomicReference<String> token = new AtomicReference<>();
        AtomicBoolean requestAccepted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(URL + "login").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("x-api-key", X_API_KEY);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(json.toString());
                outputStreamWriter.flush();

                int responseCode = urlConnection.getResponseCode();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    token.compareAndSet(null, responseJson.getString("token"));
                }
                if (responseCode == 200) requestAccepted.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return requestAccepted.get() ? token.get() : null;
    }

    @Override
    public List<String> getAllDevices(String tokenJWT) {

        AtomicReference<List<String>> devicesList = new AtomicReference<>();
        AtomicBoolean requestAccepted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(URL + "devices").openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("x-api-key", X_API_KEY);
                urlConnection.setRequestProperty("Authorization", tokenJWT);





                int responseCode = urlConnection.getResponseCode();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    List<String> temp = new ArrayList<>();
                    JSONObject responseJson = new JSONObject(response.toString());

                    for (int i = 0; i < responseJson.getJSONArray("devices").length(); i++) {
                        temp.add(responseJson.getJSONArray("devices").getString(i));
                    }

                    devicesList.compareAndSet(null, temp);
                }
                if (responseCode == 200) requestAccepted.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return devicesList.get();
    }

    @Nullable
    @Override
    public String getData(String tokenJWT, String deviceID) {

        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", deviceID);
        } catch (JSONException e) {
            return null;
        }

        AtomicReference<String> responseData = new AtomicReference<>();
        AtomicBoolean requestAccepted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(URL + "device-data").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("x-api-key", X_API_KEY);
                urlConnection.setRequestProperty("Authorization", tokenJWT);

                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(json.toString());
                outputStreamWriter.flush();

                int responseCode = urlConnection.getResponseCode();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject responseJson = new JSONObject(response.toString());

                    responseData.compareAndSet(null, responseJson.getString("device_data"));
                }
                if (responseCode == 200) requestAccepted.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return requestAccepted.get() ? responseData.get() : null;
    }

    @Nullable
    @Override
    public String pairDevice(String tokenJWT, String deviceID, String secret) {

        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", deviceID);
            json.put("secret", secret);
        } catch (JSONException e) {
            return null;
        }

        AtomicReference<String> responseData = new AtomicReference<>();
        AtomicBoolean requestAccepted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            try {
                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(URL + "device").openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("x-api-key", X_API_KEY);
                urlConnection.setRequestProperty("Authorization", tokenJWT);

                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(json.toString());
                outputStreamWriter.flush();
                String tempMessage = "";
                int responseCode = urlConnection.getResponseCode();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    tempMessage = responseJson.getString("message");
                    responseData.compareAndSet(null, tempMessage);
                }
                if (responseCode == 200) requestAccepted.set(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return requestAccepted.get() ? responseData.get() : null;
    }



}



