package com.seniordesign.ezlog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
import java.util.Iterator;

public class UserAreaActivity extends AppCompatActivity {

    Button btn;
    ListView lv;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        // et = (EditText) findViewById(R.id.et);
        btn = (Button) findViewById(R.id.btn);
        lv = (ListView) findViewById(R.id.lv);

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(UserAreaActivity.this, android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);

        onBtnClick();

    }

    public void onBtnClick(){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url ="http://128.4.25.218:8080/EZlog/appDisplayInventory";

                // Request a string response from the provided URL.
                JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                        (Request.Method.GET, url, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        for (int i = 0; i < response.length(); i++){
                                            try {
                                                JSONObject item = response.getJSONObject(i);
                                                Iterator<String> it = item.keys();

                                                String output = "";
                                                while(it.hasNext()) {
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
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // TODO Auto-generated method stub
                                        arrayList.add(error.toString());
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                        );
                queue.add(jsArrayRequest);
            }
        });
    };
}
