package com.example.dronepet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Haleema on 26/02/2018.
 */

public class ControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null){

            ControlFragment controlFragment = new ControlFragment();
            fm.beginTransaction().add(R.id.fragment_container, controlFragment).commit();
        }

        FloatingActionButton followMe = (FloatingActionButton) findViewById(R.id.FloatingBttnFollowMe);
        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "follow me is selected", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ControlActivity.this, FollowMeActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton takeOffLand = (FloatingActionButton) findViewById(R.id.FloatingBttnTakeOff);
        FloatingActionButton mic = (FloatingActionButton) findViewById(R.id.FloatingBttnMic);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Mic is selected", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ControlActivity.this, VoiceCommandActivity.class);
                startActivity(intent);
            }
        });
    }
}
