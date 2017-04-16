package com.seniordesign.ezlog;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserAreaActivity extends AppCompatActivity {

    Button btn;
    ListView lv;
    TextView loginName;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    private String user;
    private String session_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        // Define XML variables
        loginName = (TextView) findViewById(R.id.userText);
        btn = (Button) findViewById(R.id.btn);
        lv = (ListView) findViewById(R.id.lv);
        user = getIntent().getStringExtra("user");
        session_id = getIntent().getStringExtra("SESSION_ID");

        // Prepare Array List
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(UserAreaActivity.this, android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);

        // Prepare Page
        loginName.setText("Hello " + user + ": Click here to log out");
        loadInventory(user, session_id);
        onLogoutClick();
        onAddClick();

    }
    public void onAddClick(){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
                Map<String, String> vals = new HashMap<String, String>();
                vals.put("Key", "Value");
                JsonObjectRequest request = AddItemRequest.addItemRequest(user, session_id, vals);
                queue.add(request);
            }
        });
    }

    public void onLogoutClick(){
        loginName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
                String url = "http://128.4.25.218:8080/EZlog/appLogout?user=" + user + "&sessionID=" + session_id;
                StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AlertDialog alertDialog = new AlertDialog.Builder(UserAreaActivity.this).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage("Logout successful");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        Intent i = new Intent(UserAreaActivity.this, LoginActivity.class);
                        startActivity(i);
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // need error response
                        loginName.setText(error.toString());
                    }
                });
                // Add the request to the RequestQueue.
                queue.add(sr);
            }
        });
    }

    public void loadInventory(String name, String id){

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url ="http://128.4.25.218:8080/EZlog/appDisplayInventory?user=" + name + "&sessionID=" + id;

        // Request a string response from the provided URL.
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if(response.toString().equals("session=invalid")){
                                    AlertDialog alertDialog = new AlertDialog.Builder(UserAreaActivity.this).create();
                                    alertDialog.setTitle("Error: Invalid Session");
                                    alertDialog.setMessage("There was a problem with the session, returning to home page...");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                    Intent i = new Intent(UserAreaActivity.this, LoginActivity.class);
                                    startActivity(i);

                                }
                                else {
                                    for (int i = 0; i < response.length(); i++) {
                                        try {
                                            JSONObject item = response.getJSONObject(i);
                                            Iterator<String> it = item.keys();

                                            String output = "";
                                            while (it.hasNext()) {
                                                String key = it.next();
                                                output = output + key + ": " + item.getString(key) + "\n";
                                            }
                                            arrayList.add(output);
                                            adapter.notifyDataSetChanged();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                Intent i = new Intent(UserAreaActivity.this, LoginActivity.class);
                                i.putExtra("ERROR", "Invalid user authentication: Returning to login screen");
                                startActivity(i);
                            }
                        }
                );
        queue.add(jsArrayRequest);
    }
}
