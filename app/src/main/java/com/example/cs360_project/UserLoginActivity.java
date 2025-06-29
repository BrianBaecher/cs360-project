package com.example.cs360_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UserLoginActivity extends AppCompatActivity {
    // input components
    private EditText usernameInput;
    private EditText passwordInput;
    // buttons
    private Button loginButton;
    private Button registerButton;

    //usernameInput = findViewById(R.id.usernameInput);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDB LiteDb = SQLiteDB.getInstance();

        // set content to login layout
        setContentView(R.layout.login_screen);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginBtn);
        registerButton = findViewById(R.id.registerBtn);

        loginButton.setOnClickListener(v -> {
            var userVal = String.valueOf(usernameInput.getText());
            var passVal = String.valueOf(passwordInput.getText());

            boolean valid = LiteDb.tryLogin(userVal, passVal);

            Toast.makeText(this, valid ? "Login Successful" : "Login Failure", Toast.LENGTH_LONG).show();

            if (valid) {
                WarehouseApplication.getInstance().setUser(userVal);

                AdvanceScreen();
            }
        });

        registerButton.setOnClickListener(v -> {
            // check username uniqueness
            var userVal = usernameInput.getText().toString().trim();

            if (!userVal.isEmpty()) {
                if (!LiteDb.isUsernameUnique(userVal)) {
                    Toast.makeText(this, "This username is not available", Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                Toast.makeText(this, "username cannot be blank", Toast.LENGTH_LONG).show();
                return;
            }

            var passVal = passwordInput.getText().toString().trim();
            if(passVal.isEmpty()){
                Toast.makeText(this, "password cannot be blank", Toast.LENGTH_LONG).show();
                return;
            }

            // is unique, name/pwd not empty, add user to db
            var userAdded = LiteDb.addUser(userVal, passVal);

            if (userAdded) {
                Toast.makeText(this, "Registered!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void AdvanceScreen() {
        Intent nextScreen = new Intent(this, MainActivity.class);
        startActivity(nextScreen);
        finish();
    }


}
