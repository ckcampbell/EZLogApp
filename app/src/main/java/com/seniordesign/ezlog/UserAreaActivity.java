package com.seniordesign.ezlog;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class UserAreaActivity extends AppCompatActivity {

    Button btnAdd;
    Button btnBluetooth;
    ListView lv;
    ListView btList;
    TextView loginName;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;

    private String user;
    private String session_id;
    private ArrayList<String> tableColumns = new ArrayList<String>();
    private Map<String, Integer> listIDs = new HashMap();
    private JSONArray Inventory = null;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        // Define XML variables
        loginName = (TextView) findViewById(R.id.userText);
        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        btnAdd = (Button) findViewById(R.id.addItem);
        lv = (ListView) findViewById(R.id.lv);
        btList = (ListView) findViewById(R.id.btList);
        user = getIntent().getStringExtra("user");
        session_id = getIntent().getStringExtra("SESSION_ID");

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
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editItemAction(position);
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemAction("-1");
            }
        });




    }

    public void onBluetoothClick(){
        btnBluetooth.setOnClickListener(new View.OnClickListener(){
            public static final int REQUEST_ENABLE_BT = 1;

            @Override
            public void onClick(View v){
                // Get the default adapter
                myBluetooth = BluetoothAdapter.getDefaultAdapter();

                // Enable Bluetooth
                if(myBluetooth == null)
                {
                    //Show a message. that thedevice has no bluetooth adapter
                    Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
                    //finish apk
                    finish();
                }
                else
                {
                    if (myBluetooth.isEnabled())
                    {
                        pairedDevicesList();
                    }
                    else
                    {
                        //Ask to the user turn the bluetooth on
                        Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnBTon,1);
                    }
                }
            }
        });
    }

    private void pairedDevicesList(){
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices) {
                if (bt.getName().equals("HC-05")) {
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        btList.setAdapter(adapter);
        btList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            btList.setAdapter(null);
            for(BluetoothDevice bt : pairedDevices)
            {
                if(bt.getAddress().equals(address)){
                    Toast.makeText(UserAreaActivity.this, "Connected to: " + address, Toast.LENGTH_SHORT).show();
                    mmDevice = bt;
                    try {
                        openBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("problem", "here");
                    }
                }
            }
        }
    };

    void openBT() throws IOException    {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        try {
            mmSocket.connect();
            Log.e("","Connected");
        } catch (IOException e) {
            Log.e("",e.getMessage());
            try {
                Log.e("","trying fallback...");

                mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                mmSocket.connect();

                Log.e("","Connected");
            }
            catch (Exception e2) {
                Log.e("failed", "Couldn't establish Bluetooth connection!");
            }
        }
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

    }

    void beginListenForData(){
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    final String data2 = data.substring(0, 26);
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(inventoryPosition(data2) != -1){
                                                editItemAction(inventoryPosition(data2));
                                            }
                                            else{
                                                addItemAction(data2);
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private int inventoryPosition(String item_id){
        for (int i = 0; i < Inventory.length(); i++){
            JSONObject item = null;
            try {
                item = Inventory.getJSONObject(i);
                if (item.get("ID").equals(item_id)){
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
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

    private void loadInputDialog(LinearLayout dialogLayout, String item_id){
        for (int i = 0; i < tableColumns.size(); i++){
            TextView tv = createNewTextView(tableColumns.get(i));
            EditText et = createNewEditText(tableColumns.get(i));
            if(tableColumns.get(i).equals("ID") && !item_id.equals("-1")){
                et.setText(item_id);
            }
            dialogLayout.addView(tv);
            dialogLayout.addView(et);
        }
    }

    private void loadEditDialog(LinearLayout dialogLayout, int position){
        for (int i = 0; i < tableColumns.size(); i++){
            TextView tv = createNewTextView(tableColumns.get(i));
            EditText et = createNewEditText(tableColumns.get(i));
            if(tableColumns.get(i).equals("ID")){
                try {
                    et.setText(Inventory.getJSONObject(position).getString(tableColumns.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    et.setText(Inventory.getJSONObject(position).getString(tableColumns.get(i)), TextView.BufferType.EDITABLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            dialogLayout.addView(tv);
            dialogLayout.addView(et);
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
                                        Inventory = response;
                                        Log.d("loadedinv",Inventory.toString());
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

    private void addItemAction(String item_id) {
        final Dialog dialog = new Dialog(UserAreaActivity.this);
        dialog.setContentView(R.layout.activity_input_popup);
        dialog.setTitle("Add a New Item");

        final LinearLayout dialogLayout = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        Button dialogButton = (Button)dialog.findViewById(R.id.button);

        loadInputDialog(dialogLayout, item_id);
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
                Log.d("values", values.toString());
                JsonObjectRequest request = AddItemRequest.addItemRequest(user, session_id, values);
                RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
                queue.add(request);
                dialog.dismiss();
                loadInventory(user, session_id);
            }
        });
    }

    private void editItemAction(final int position) {
        if(Inventory != null) {
            final Dialog dialog = new Dialog(UserAreaActivity.this);
            dialog.setContentView(R.layout.activity_edit_item_popup);
            dialog.setTitle("Edit Item");

            final LinearLayout dialogLayout = (LinearLayout)dialog.findViewById(R.id.linearLayout);
            Button editButton = (Button)dialog.findViewById(R.id.editButton);
            Button deleteButton = (Button)dialog.findViewById(R.id.deleteButton);
            Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);

            loadEditDialog(dialogLayout, position);
            dialog.show();

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        JsonObjectRequest request = null;
                        // continue with delete
                        try {
                            request = RemoveItemRequest.removeItemRequest(user, session_id, Inventory.getJSONObject(position).get("ID").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.this);
                        queue.add(request);
                        dialog.dismiss();
                        loadInventory(user, session_id);
                        Log.d("invafterdelete", Inventory.toString());
                    }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

}
