package com.agh.iot.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.agh.iot.mobile.wifi_management.WifiSelection;

public class LoggedInView extends AppCompatActivity {

    String token;
    Button buttonWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_view);

        token = getIntent().getStringExtra("token");

        buttonWifi = findViewById(R.id.btn_wifi);

        buttonWifi.setOnClickListener(v -> startWifiActivity());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.frame_layout, new DisplayData())
                    .commit();
        }
    }

    private void startWifiActivity() {
        startActivity(new Intent(LoggedInView.this, WifiSelection.class));
    }
}