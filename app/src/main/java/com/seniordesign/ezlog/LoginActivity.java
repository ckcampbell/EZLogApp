package com.seniordesign.ezlog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /** Called when the user taps the Send button */
    public void sendLogin(View view) {
        Intent i= new Intent(this, LoggedInMainActivity.class);
        Bundle login = new Bundle();
        EditText user = (EditText) findViewById(R.id.loginUsername);
        EditText pass = (EditText) findViewById(R.id.loginPassword);
        String username = user.getText().toString();
        String password = pass.getText().toString();
        login.putString("uname", username);
        login.putString("pword", password);
        i.putExtras(login);
        startActivity(i);
    }
}
