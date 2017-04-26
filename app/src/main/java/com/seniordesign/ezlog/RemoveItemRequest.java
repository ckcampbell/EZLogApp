package com.seniordesign.ezlog;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by casey on 4/25/2017.
 */

public class RemoveItemRequest {
    public static JsonObjectRequest removeItemRequest(String user, String sessionID, String ID) {
        String url = "http://128.4.25.218:8080/EZlog/appRemoveItem?user=" + user + "&sessionID=" + sessionID + "&itemID=" + ID;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error:removeItemRequest", error.toString());
                    }
                }
        );
        return request;
    }
}
