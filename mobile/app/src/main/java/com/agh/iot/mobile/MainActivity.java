package com.agh.iot.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agh.iot.mobile.connection.ConnectorAWS;
import com.agh.iot.mobile.connection.RequestManager;

public class MainActivity extends AppCompatActivity {
    RequestManager requestManager;

    EditText editTextName;
    EditText editTextEmail;
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextConfirmPassword;
    Button buttonRegister;
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Some stuff for android activity to work properly
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the request manager
        requestManager = new ConnectorAWS();

        // binding the views
        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextUsername = findViewById(R.id.et_username);
        editTextPassword = findViewById(R.id.et_password);
        editTextConfirmPassword = findViewById(R.id.et_cpassword);
        buttonRegister = findViewById(R.id.btn_register);
        buttonLogin = findViewById(R.id.btn_login);

        // setting listeners
        buttonLogin.setOnClickListener(v -> startLoginActivity());
        buttonRegister.setOnClickListener(v -> onRegisterButtonClick());
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }

    private void onRegisterButtonClick(){
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmedPassword = editTextConfirmPassword.getText().toString();

        if(name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()){
            // Some fields are empty
            Toast.makeText(getApplicationContext(), "Fields Required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(confirmedPassword)){
            // Passwords do not match
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        int responseCode = requestManager.insertNewUser(name,email,username,password);
        if(responseCode == 401){
            // Username is not available
            Toast.makeText(getApplicationContext(), "Username already taken", Toast.LENGTH_SHORT).show();
        } else if(responseCode == 200){
            // User registered successfully
            clearFields();
            Toast.makeText(getApplicationContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
            startLoginActivity();
        }else{
            // Something went wrong
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }

    }

    private void clearFields(){
        editTextName.setText("");
        editTextEmail.setText("");
        editTextUsername.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
    }


}