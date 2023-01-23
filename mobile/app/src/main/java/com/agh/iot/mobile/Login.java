package com.agh.iot.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agh.iot.mobile.connection.ConnectorAWS;
import com.agh.iot.mobile.connection.RequestManager;

public class Login extends AppCompatActivity {
    RequestManager requestManager;

    Button loginButtonRegister;
    Button loginButtonLogin;
    EditText loginEditTextUserName;
    EditText loginEditTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Some stuff for android activity to work properly
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the request manager
        requestManager = new ConnectorAWS();

        // binding the views
        loginEditTextUserName = findViewById(R.id.et_lusername);
        loginEditTextPassword = findViewById(R.id.et_lpassword);
        loginButtonLogin = findViewById(R.id.btn_llogin);
        loginButtonRegister = findViewById(R.id.btn_lregister);

        // setting listeners
        loginButtonRegister.setOnClickListener(v -> startRegisterActivity());
        loginButtonLogin.setOnClickListener(v -> logInAttempt());
    }

    private void startLoggedInViewActivity(String token){
        Intent intent = new Intent(Login.this, LoggedInView.class);
        intent.putExtra("token",token);
        startActivity(intent);
    }

    private void startRegisterActivity(){
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }

    private void logInAttempt(){
        String username = loginEditTextUserName.getText().toString();
        String password = loginEditTextPassword.getText().toString();

        String token;

        if((token = requestManager.areCredentialsValid(username, password)) != null){
            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            startLoggedInViewActivity(token);
        }else{
            Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

}