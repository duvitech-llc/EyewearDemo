package com.six15.eyeweardemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceScanActivity extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private static final String TAG = "DeviceScanActivity";
    private ApplicationPreferences sharedPreference;
    Activity context = this;
    private String mDeviceAddress = null;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mConnecting = false;
    private ListView mListView = null;
    private Handler mHandler;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            if(mDeviceAddress != null) {

                Log.d(TAG, "Trying to Auto-Connect to Paired Device: " + mDeviceAddress);
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(mDeviceAddress);
                // go to main activity
            }else {
                mBluetoothLeService.scanLeDevice(true);
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService.scanLeDevice(false);
            mBluetoothLeService = null;
            invalidateOptionsMenu();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        boolean mScanning = false;
        if(mBluetoothLeService != null && mBluetoothLeService.isScanning())
            mScanning = true;

        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                Log.d(TAG, "Start Scan Pressed");
                if(mBluetoothLeService != null) {
                    mLeDeviceListAdapter.clear();
                    mBluetoothLeService.scanLeDevice(true);
                }
                break;
            case R.id.menu_stop:
                Log.d(TAG, "Stop Scan Pressed");
                if(mBluetoothLeService != null) {
                    mBluetoothLeService.scanLeDevice(false);
                }
                break;
        }
        invalidateOptionsMenu();
        return true;
    }

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
            } else if(BluetoothLeService.ACTION_BLE_SCAN_STOP.equals(action)){
                invalidateOptionsMenu();
            }else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mConnecting = false;
                Log.d(TAG, "Device Connected");
                // send to main screen
                Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Calling MainActivity");

                        final Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        // pass data here if needed
                        if (mBluetoothLeService.isScanning()) {
                            mBluetoothLeService.scanLeDevice(false);
                        }
                        startActivity(i);
                    }
                }, 500);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d(TAG, "Device Disconnected");
                Toast.makeText(getBaseContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            } else if (BluetoothLeService.ACTION_DEVICE_FOUND.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra("BLE_DEVICE");
                Log.d(TAG, "Device Found " + device.getAddress());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            } else if (BluetoothLeService.ACTION_DATA_WRITE_COMPLETED.equals(action)) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_device_scan);

        mListView = (ListView)findViewById(R.id.lvScanList);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!mConnecting) {
                    final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                    Log.d(TAG, "Device Selected: " + device.getName() + " " + device.getAddress());
                    mBluetoothLeService.scanLeDevice(false);
                    mConnecting = true;
                    if(mBluetoothLeService.isConnected())
                        mBluetoothLeService.disconnect();
                    mBluetoothLeService.connect(device.getAddress());

                    invalidateOptionsMenu();
                }
            }
        });

        // set up shared preferences
        sharedPreference = new ApplicationPreferences();

        // get paired device
        mDeviceAddress= sharedPreference.getValue(context);
        Log.d(TAG, "Paired Device: " + mDeviceAddress);

        // bind to BLE Service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // create handler object
        mHandler = new Handler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mBluetoothLeService.scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        mLeDeviceListAdapter.clear();
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);

        if (mBluetoothLeService != null && mDeviceAddress != null) {
            if(!mBluetoothLeService.isConnected()){
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }else if(mBluetoothLeService != null && !mBluetoothLeService.isConnected()){
            mBluetoothLeService.scanLeDevice(true);
        }else if(mBluetoothLeService != null && mBluetoothLeService.isConnected()){
            // go to main
            BluetoothDevice dev = mBluetoothLeService.getConnectedDevice();
            if(dev != null) {
                mLeDeviceListAdapter.addDevice(dev);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed(){
        Log.d(TAG, "Back Button Pressed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
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

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}
