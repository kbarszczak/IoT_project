package com.agh.iot.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.agh.iot.mobile.connection.ConnectorAWS;
import com.agh.iot.mobile.connection.RequestManager;

public class DataReview extends AppCompatActivity {

    private String token;
    private String deviceName;
    private RequestManager requestManager;

    private TextView textViewDataDisplay;
    private Button buttonRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_review);

        token = getIntent().getStringExtra("token");
        deviceName = getIntent().getStringExtra("deviceName");

        requestManager = new ConnectorAWS();

        textViewDataDisplay = findViewById(R.id.textView);
        buttonRefresh = findViewById(R.id.refresh_button);

        buttonRefresh.setOnClickListener(v -> displayData());

        displayData();
    }

    public void displayData(){
        String data;
        try {
            data = requestManager.getData(token, deviceName);
            textViewDataDisplay.setText(data==null ? "No data" : data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}