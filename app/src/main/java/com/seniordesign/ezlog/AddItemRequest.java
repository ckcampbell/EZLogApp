package com.seniordesign.ezlog;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by casey on 4/6/2017.
 */

public class AddItemRequest {
    public static JsonObjectRequest addItemRequest(String user, String sessionID, Map<String, String> values) {

        String url = "http://128.4.25.218:8080/EZlog/appAddItem?user=" + user + "&sessionID=" + sessionID;
        Iterator it = values.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            url += "&" + pair.getKey() + "=" + pair.getValue();
        }
        Log.d("url", url);
        JSONObject params = new JSONObject(values);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error:addItemRequest", error.toString());
                    }
                }
        );
        return request;
    }

}
