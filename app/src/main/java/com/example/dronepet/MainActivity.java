package com.example.dronepet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;


public class MainActivity extends AppCompatActivity implements ARDeviceControllerListener, ARDeviceControllerStreamListener {

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private ARDiscoveryDeviceService mDeviceService;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ARDeviceController deviceController;
    ARDiscoveryDevice device = null;

    private Context mContext;
    private static final String TAG = "Discoverer";

    static {
        ARSDK.loadSDKLibs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null){

            MainFragment mainFragment = new MainFragment();
            fm.beginTransaction().add(R.id.fragment_container, mainFragment).commit();
        }

        //Implement floating action buttons
        FloatingActionButton followMe = (FloatingActionButton) findViewById(R.id.FloatingBttnFollowMe);
        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "follow me is selected", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, FollowMeActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton takeOffLand = (FloatingActionButton) findViewById(R.id.FloatingBttnTakeOff);
        takeOffLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "take off is selected", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton mic = (FloatingActionButton) findViewById(R.id.FloatingBttnMic);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Mic is selected", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, VoiceCommandActivity.class);
                startActivity(intent);
            }
        });

        // Begins initial discovery
        initDiscoveryService();

//registers recievers
        registerReceivers();

//Create the device controller

        ARDiscoveryDevice discoveryDevice = createDiscoveryDevice(mDeviceService);

        if (discoveryDevice != null) {
            try
            {
                deviceController = new ARDeviceController(device);
                discoveryDevice.dispose();
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }
        }

// your class should implement ARDeviceControllerListener
        deviceController.addListener (MainActivity.this);


        deviceController.addStreamListener(this);

        ARCONTROLLER_ERROR_ENUM error = deviceController.start();
        error = deviceController.stop();

// only when the deviceController is stopped
        deviceController.dispose();


    }

    private void initDiscoveryService(){

        //create the service connection
        if (mArdiscoveryServiceConnection == null){

            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {

                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
                    startDiscovery();

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                    mArdiscoveryService = null;

                }
            };
        }

        if(mArdiscoveryService == null){

            //if the discovery service doesn't exist, bind to it
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else{

            // If the discovery service already exists, start discovery
            startDiscovery();
        }

        Toast.makeText(MainActivity.this, "done",Toast.LENGTH_SHORT).show();
    }

    private void startDiscovery(){

        if(mArdiscoveryService != null){
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers(){

        ARDiscoveryServicesDevicesListUpdatedReceiver receiver =
                new ARDiscoveryServicesDevicesListUpdatedReceiver(mDiscoveryDelegate);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(receiver,
                new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDiscoveryDelegate =
            new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {

                @Override
                public void onServicesDevicesListUpdated() {

                    if(mArdiscoveryService != null) {
                        List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();

                        //Do what you want with the device list
                    }

                }
            };

    private ARDiscoveryDevice createDiscoveryDevice(@NonNull ARDiscoveryDeviceService service) {

        try {
            device = new ARDiscoveryDevice(mContext, service);
        } catch (ARDiscoveryException e) {
            Log.e(TAG, "Exception", e);
        }

        return device;
    }

    private void unregisterReceivers()
    {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    private void closeServices()
    {
        Log.d(TAG, "closeServices ...");

        if (mArdiscoveryService != null)
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    mArdiscoveryService.stop();

                    getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {

        switch (newState)
        {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {

        if (elementDictionary != null)
        {
            // if the command received is a battery state changed
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED)
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null)
                {
                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);

                    // do what you want with the battery level
                }
            }
        }
        else
        {
            Log.e(TAG, "elementDictionary is null");
        }

    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {
        // configure your decoder
        // return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK if display went well
        // otherwise, return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR. In that case,
        // configDecoderCallback will be called again
        return null;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {

        // display the frame
        // return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK if display went well
        // otherwise, return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR. In that case,
        // configDecoderCallback will be called again
        return null;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {

    }

}
