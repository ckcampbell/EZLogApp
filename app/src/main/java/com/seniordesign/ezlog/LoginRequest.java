package com.seniordesign.ezlog;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by casey on 4/6/2017.
 */

public class LoginRequest extends StringRequest {
    public static final String LOGIN_REQUEST_URL = "https://128.4.25.41:8080/EZlog/android_login.php";
    private Map<String, String> params;

    public LoginRequest(String username, String password, Response.Listener<String> listener){
        super(Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
    }

}
