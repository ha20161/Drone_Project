package com.example.dronepet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parrot.arsdk.ARSDK;

public class MainActivity extends AppCompatActivity {

    static {
        ARSDK.loadSDKLibs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
