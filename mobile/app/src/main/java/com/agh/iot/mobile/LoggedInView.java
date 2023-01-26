package com.agh.iot.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agh.iot.mobile.connection.ConnectorAWS;
import com.agh.iot.mobile.connection.RequestManager;
import com.agh.iot.mobile.wifi_management.WifiSelection;

public class LoggedInView extends AppCompatActivity {

    String token;
    Button buttonWifi;
    Button buttonLogout;
    ListView listView;
    RequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_view);

        requestManager = new ConnectorAWS();

        token = getIntent().getStringExtra("token");


        listView = findViewById(R.id.devices_paired_list);
        buttonWifi = findViewById(R.id.btn_wifi);
        buttonLogout = findViewById(R.id.button_logout);

        buttonWifi.setOnClickListener(v -> startWifiActivity());
        buttonLogout.setOnClickListener(v -> logout());
        listView.setOnItemClickListener((adapterView, view, i, l) -> onDeviceSelected(i));




        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.frame_layout, new DisplayData())
                    .commit();
        }

        fillAvailableDevicesList();
    }

    private void logout(){
        token = null;
        Intent intent = new Intent(LoggedInView.this, Login.class);
        startActivity(intent);
        this.finish();
    }

    private void onDeviceSelected(int idx){
        String deviceName = (String) listView.getItemAtPosition(idx);
        Intent intent = new Intent(this, DataReview.class);
        intent.putExtra("token", token);
        intent.putExtra("deviceName", deviceName);
        startActivity(intent);
    }

    private void fillAvailableDevicesList() {
        Log.d("LISTVIEW", requestManager.getAllDevices(token) == null ? "null" : "not null");
        try {
            Log.d("LISTVIEW", requestManager.getAllDevices(token).toString());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requestManager.getAllDevices(token));
            listView.setAdapter(adapter);

        }
        catch (Exception nullPointerException){
            Toast.makeText(getApplicationContext(), "No devices paired", Toast.LENGTH_SHORT).show();
        }
    }

    private void startWifiActivity() {
        Intent intent = new Intent(LoggedInView.this, WifiSelection.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}