package com.seniordesign.ezlog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Send button */
    public void toLogin(View view) {
        Intent i= new Intent(this, LoginActivity.class);
        startActivity(i);
    }
    /** Called when the user taps the Send button */
    public void toLocalBatch(View view) {
        Intent i = new Intent(this, BatchActivity.class);
        startActivity(i);
    }

}
