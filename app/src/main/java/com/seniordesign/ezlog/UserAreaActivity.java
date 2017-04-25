package com.seniordesign.ezlog;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import java.util.Queue;

public class UserAreaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button btnAction;
    Button btnBluetooth;
    ListView lv;
    TextView loginName;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    Spinner spinner;
    private String user;
    private String session_id;
    private ArrayList<String> tableColumns = new ArrayList<String>();
    private Map<String, Integer> listIDs = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        // Define XML variables
        loginName = (TextView) findViewById(R.id.userText);
        btnAction = (Button) findViewById(R.id.btnAction);
        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        lv = (ListView) findViewById(R.id.lv);
        user = getIntent().getStringExtra("user");
        session_id = getIntent().getStringExtra("SESSION_ID");
        spinner = (Spinner) findViewById(R.id.menu);

        // Prepare Array List
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(UserAreaActivity.this, android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);

        // Prepare Page
        loginName.setText("Hello " + user + ": Click here to log out");
        loadInventory(user, session_id);
        loadTableColumns();
        onLogoutClick();
        onBluetoothClick();
        spinner.setOnItemSelectedListener(this);

    }

    public void onActionSelect(String string){

        final Dialog dialog = new Dialog(UserAreaActivity.this);
        dialog.setContentView(R.layout.activity_input_popup);
        dialog.setTitle("Add a New Item");

        final LinearLayout dialogLayout = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        Button dialogButton = (Button)dialog.findViewById(R.id.button);

        loadInputDialog(dialogLayout);
        dialog.show();

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> values = new HashMap<>();
                for(int i = 0; i < tableColumns.size(); i++){
                    EditText view = (EditText) dialogLayout.getChildAt(i*2+1);
                    String string = view.getText().toString();
                    values.put(tableColumns.get(i), string);
                }
                JsonObjectRequest request = AddItemRequest.addItemRequest(user, session_id, values);
                RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
                queue.add(request);
                dialog.dismiss();
                loadInventory(user, session_id);
            }
        });
    }

    public void onBluetoothClick(){
        btnBluetooth.setOnClickListener(new View.OnClickListener(){
            public static final int REQUEST_ENABLE_BT = 1;

            @Override
            public void onClick(View v){
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            }
        });
    }

    private TextView createNewTextView(String text) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(this);
        textView.setLayoutParams(lparams);
        textView.setText(text + ":");
        return textView;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private EditText createNewEditText(String text) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final EditText editText = new EditText(this);
        editText.setLayoutParams(lparams);
        editText.setHint(text);
        editText.setId(View.generateViewId());
        listIDs.put(text, editText.getId());
        return editText;
    }

    private void loadInputDialog(LinearLayout dialogLayout){
        for (int i = 0; i < tableColumns.size(); i++){
            dialogLayout.addView(createNewTextView(tableColumns.get(i)));
            dialogLayout.addView(createNewEditText(tableColumns.get(i)));
        }
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        String spinnerChoice = parent.getItemAtPosition(pos).toString();
        onActionSelect(spinnerChoice);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
                                    if(!response.equals(null)) {
                                        arrayList.clear();

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

    public void loadTableColumns(){
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
        Map<String, String> vals = new HashMap<String, String>();
        vals.put("Key", "Value");
        String url = "http://128.4.25.218:8080/EZlog/appGetHeaders?user=" + user + "&sessionID=" + session_id;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        tableColumns.clear();
                        String [] array = response.split(" ");
                        for (int i = 0; i < array.length; i++){
                            tableColumns.add(i, array[i]);
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        AlertDialog alertDialog = new AlertDialog.Builder(UserAreaActivity.this).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage(error.toString());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
        );
        queue.add(request);
    }
}
