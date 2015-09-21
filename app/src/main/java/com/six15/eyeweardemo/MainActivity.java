package com.six15.eyeweardemo;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "BLEService Connected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.isInitialized() && !mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "BLEService Disconnected");
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    // ACTION_BLE_SCAN_START: BLE Device scan started.
    // ACTION_BLE_SCAN_STOP: BLE Device scan stopped.
    // ACTION_DEVICE_FOUND: BLE Device found in scan.
    // ACTION_DATA_WRITE_COMPLETED: Write to characteristic completed.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_BLE_SCAN_START.equals(action)){
                invalidateOptionsMenu();
            } else if(BluetoothLeService.ACTION_BLE_SCAN_STOP.equals(action)) {
                invalidateOptionsMenu();
            }else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "Device Connected");
                // send to main screen
                Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_SHORT).show();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Device Disconnected");
                Toast.makeText(getBaseContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            } else if (BluetoothLeService.ACTION_DEVICE_FOUND.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_WRITE_COMPLETED.equals(action)) {
            }
        }
    };

    private void sendCommand(String commandString){
        Log.d(TAG, "Sending Command: " + commandString);
        if(mBluetoothLeService != null){
            mBluetoothLeService.sendCommandString(commandString);
        }
        else{
            Log.d(TAG, "BluetoothSevice is NULL");
            Toast.makeText(getBaseContext(), "Failed to connect to BLE Service", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        boolean bConnected = false;
        if(mBluetoothLeService != null && mBluetoothLeService.isConnected())
        {
            bConnected = true;
        }

        if (bConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
               // mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                if(mBluetoothLeService != null && mBluetoothLeService.isConnected())
                    mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Button btnEmail = (Button) findViewById(R.id.btnEmailNotify);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String theCommand  = String.format("%s","bem");
                sendCommand(theCommand);
            }
        });

        Button btnLeftBlinker = (Button) findViewById(R.id.btnLtBlinker);
        btnLeftBlinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String theCommand = String.format("%s %d,%d,%d,", "blt", 35, 45, 0);
                sendCommand(theCommand);
            }
        });

        Button btnRightBlinker = (Button) findViewById(R.id.btnRtBlinker);
        btnRightBlinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String theCommand = String.format("%s %d,%d,%d,", "brt", 318, 45, 0);
                sendCommand(theCommand);
            }
        });


        // bind to BLE Service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbindService(mServiceConnection);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE_COMPLETED);
        intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_FOUND);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_SCAN_START);
        intentFilter.addAction(BluetoothLeService.ACTION_BLE_SCAN_STOP);
        return intentFilter;
    }


}
