package com.seniordesign.ezlog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.seniordesign.ezlog.R.id.text;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView homeLink = (TextView) findViewById(R.id.tvToHome);
        final TextView text = (TextView) findViewById(R.id.text);

        homeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(homeIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                String url ="http://128.4.25.218:8080/EZLog/appLogin";

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            text.setText("Response is: "+ response);
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //text.setText(error.getMessage());
                        }
                    }
                );
                // Add the request to the RequestQueue.
                queue.add(stringRequest);


//                Response.Listener<String> responseListener = new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response){
//                        try {
//                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean success = jsonResponse.getBoolean("success");
//                            if(success){
//                                Intent intent = new Intent(LoginActivity.this, UserArea_Home.class);
//                                intent.putExtra("username", username);
//                                intent.putExtra("password", password);
//                                LoginActivity.this.startActivity(intent);
//
//                            }
//                            else {
//                                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
//                                alertDialog.setTitle("Alert");
//                                alertDialog.setMessage("Alert message to be shown");
//                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                alertDialog.show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                LoginRequest loginRequest = new LoginRequest(username, password, responseListener);
//                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
//                queue.add(loginRequest);
            }
        });
    }
}

