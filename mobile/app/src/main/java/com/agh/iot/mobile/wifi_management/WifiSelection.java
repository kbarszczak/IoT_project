package com.agh.iot.mobile.wifi_management;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agh.iot.mobile.LoggedInView;
import com.agh.iot.mobile.R;
import com.agh.iot.mobile.WebSearch;
import com.agh.iot.mobile.connection.ConnectorAWS;
import com.agh.iot.mobile.connection.RequestManager;

import java.util.Objects;

public class WifiSelection extends AppCompatActivity {

    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    private ListView wifiList;
    private WifiManager wifiManager;
    private WifiReceiver receiverWifi;
    private Button buttonConnectWifi;
    private Button buttonScan;
    private String wifiName;
    private EditText wifiPassword;
    private EditText editTextDeviceID;
    private EditText editTextSecret;
    private RequestManager requestManager;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_selection);
        token = getIntent().getStringExtra("token");
        buttonScan = findViewById(R.id.scanBtn);
        editTextDeviceID = findViewById(R.id.et_device_id);
        buttonConnectWifi = findViewById(R.id.btn_wifi_login);
        wifiPassword = findViewById(R.id.et_wifi_password);
        wifiList = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        editTextSecret = findViewById(R.id.et_wifi_secret);

        requestManager = new ConnectorAWS();

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        buttonScan.setOnClickListener(v -> scanning());
        wifiList.setOnItemClickListener((adapterView, view, i, l) -> wifiName = wifiList.getAdapter().getItem(i).toString());
        buttonConnectWifi.setOnClickListener(view -> connectToWiFi());
    }

    private void scanning() {
        if (ActivityCompat.checkSelfPermission(WifiSelection.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    WifiSelection.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            wifiManager.startScan();
        }
    }

    private void connectToWiFi() {
        WifiConfiguration conf = buildWifiConfig();
        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        Intent intent = new Intent(WifiSelection.this, WebSearch.class);
        intent.putExtra("token", token);
        startActivity(intent);
        attemptToPair(false);
    }

    private void attemptToPair(boolean isRetry) {
        String message = requestManager.pairDevice(token, editTextDeviceID.getText().toString(), editTextSecret.getText().toString());
        if (message != null && message.equals("Success")) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            goToLoggedInActivity();
        }else if (!isRetry){
            attemptToPair(true);
        } else {
            Toast.makeText(getApplicationContext(), "Failed to pair device", Toast.LENGTH_LONG).show();
        }

    }

    private void goToLoggedInActivity(){
        Intent intent = new Intent(this,  WebSearch.class);
        intent.putExtra("token", token);
        this.finish();
        startActivity(intent);
    }


    private WifiConfiguration buildWifiConfig() {
        String networkSSID = wifiName.split(" - ")[0];
        String networkPass = wifiPassword.getText().toString();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", networkSSID);
        conf.preSharedKey = String.format("\"%s\"", networkPass);
        return conf;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }

    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(WifiSelection.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();

            // check if we have permission to access coarse location
            if (ContextCompat.checkSelfPermission(WifiSelection.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WifiSelection.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(WifiSelection.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(WifiSelection.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(WifiSelection.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WifiSelection.this, "permission granted", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            } else {
                Toast.makeText(WifiSelection.this, "permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}