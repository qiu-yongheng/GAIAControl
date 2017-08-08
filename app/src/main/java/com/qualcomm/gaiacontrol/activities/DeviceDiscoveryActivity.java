/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.receivers.BREDRDiscoveryReceiver;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.ui.adapters.DevicesListAdapter;
import com.qualcomm.gaiacontrol.ui.adapters.DevicesListTabsAdapter;
import com.qualcomm.gaiacontrol.ui.fragments.DevicesListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * This activity controls the scan of available BLE devices to connect with one of them. This activity also shows the
 * bonded devices to let the user choose the exact device he would like to use.
 * This activity is the start activity of the application and will initiate the connection with the device before
 * starting the next activity.
 */
public class DeviceDiscoveryActivity extends BluetoothActivity implements
        DevicesListFragment.DevicesListFragmentListener, BREDRDiscoveryReceiver.BREDRDiscoveryListener {

    /**
     * For debug mode, the tag to display for logs.
     */
    private final static String TAG = "DeviceDiscoveryActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too
     * memory intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private DevicesListTabsAdapter mTabsAdapter;
    /**
     * The button the user uses to connect with a selected device over BLE.
     */
    private Button mBtConnectBLE;
    /**
     * The button the user uses to connect with a selected device over BR/EDR.
     */
    private Button mBtConnectBREDR;
    /**
     * The handler to use to postpone some actions.
     */
    private final Handler mHandler = new Handler();
    /**
     * To know if the scan is running.
     */
    private boolean mIsScanning = false;
    /**
     * The callback called when a device has been scanned by the LE scanner.
     */
    private final LeScanCallback mLeScanCallback = new LeScanCallback();
    /**
     * The adapter which should be informed about scanned devices.
     */
    private DevicesListAdapter mDevicesAdapter;
    /**
     * The runnable to trigger to stop the scan once the scanning time is finished.
     */
    private final Runnable mStopScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    /**
     * The broadcast receiver in order to get devices which had been discovered during scanning using
     * {@link BluetoothAdapter#startDiscovery() startDiscovery()}.
     */
    private final BREDRDiscoveryReceiver mDiscoveryReceiver = new BREDRDiscoveryReceiver(this);
    /**
     * The listener to be informed when the user changes the tab selection.
     */
    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int newPosition) {
            mTabsAdapter.onPageSelected(newPosition);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };


    // ------ OVERRIDE METHODS ----------------------------------------------------------------------------------------

    @SuppressWarnings("EmptyMethod") // does not need to be implemented at the moment
    @Override // ModelActivity
    public void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        // start scan?
    }

    @Override // DevicesListFragmentListener
    public void startScan(DevicesListAdapter adapter) {
        mDevicesAdapter = adapter;
        scanDevices(true);
    }

    @Override // DevicesListFragmentListener
    public void onItemSelected(boolean selected) {
        enableButtons(selected);
    }

    @Override // DevicesListFragmentListener
    public void getBondedDevices(DevicesListAdapter adapter) {
        Set<BluetoothDevice> listDevices;

        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            listDevices = mBtAdapter.getBondedDevices();
        } else {
            listDevices = Collections.emptySet();
        }

        ArrayList<BluetoothDevice> listBLEDevices = new ArrayList<>();

        for (BluetoothDevice device : listDevices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL
                    || device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC
                    || device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                listBLEDevices.add(device);
            }
        }
        adapter.setListDevices(listBLEDevices);
        mTabsAdapter.onScanFinished(DevicesListTabsAdapter.BONDED_LIST_TYPE);
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (mDevicesAdapter != null && device != null
                && device.getName() != null && device.getName().length() > 0) {
            mDevicesAdapter.add(device, 0);
        }
    }


    // ------ ACTIVITY METHODS ----------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        init();
    }

    @Override
    protected void onResumeFragments() {
        registerReceiver();
        super.onResumeFragments();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            scanDevices(false);
        }
        unregisterReceiver();
    }


    // ------ PRIVATE METHODS -----------------------------------------------------------------------------------------

    /**
     *
     */
    private void enableButtons(boolean enabled) {
        if (!enabled || !mTabsAdapter.hasSelection()) {
            mBtConnectBLE.setEnabled(false);
            mBtConnectBREDR.setEnabled(false);
        }
        else {
            int type = mTabsAdapter.getSelectedDevice().getType();
            mBtConnectBREDR.setEnabled(type == BluetoothDevice.DEVICE_TYPE_CLASSIC
                    || type == BluetoothDevice.DEVICE_TYPE_DUAL);
            mBtConnectBLE.setEnabled(type == BluetoothDevice.DEVICE_TYPE_LE
                    || type == BluetoothDevice.DEVICE_TYPE_DUAL);
        }
    }

    /**
     * <p>To start or stop the scan of available devices.</p>
     * <p>Do not use this method directly, prefer the
     * {@link DeviceDiscoveryActivity#startScan startScan} and {@link DeviceDiscoveryActivity#stopScan() stopScan} methods.</p>
     *
     * @param scan
     *          True to start the scan, false to stop it.
     */
    private void scanDevices(boolean scan) {
        assert mBtAdapter != null;

        if (scan && !mIsScanning) {
            mIsScanning = true;
            mHandler.postDelayed(mStopScanRunnable, Consts.SCANNING_TIME);
            //noinspection deprecation,UnusedAssignment
            boolean isScanning = mBtAdapter.startLeScan(mLeScanCallback);
            //noinspection UnusedAssignment
            boolean isDiscovering = mBtAdapter.startDiscovery();
            if (DEBUG) Log.i(TAG, "Start scan of LE devices: " + isScanning + " - start discovery of BR/EDR devices: " +
                    isDiscovering);
        } else if (mIsScanning) {
            mIsScanning = false;
            mHandler.removeCallbacks(mStopScanRunnable);
            //noinspection deprecation
            mBtAdapter.stopLeScan(mLeScanCallback);
            //noinspection UnusedAssignment
            boolean isDiscovering = mBtAdapter.cancelDiscovery();
            if (DEBUG) Log.i(TAG, "Stop scan of LE devices - stop discovery of BR/EDR devices: " + isDiscovering);
        }
    }

    /**
     * <p>The method to call to stop the scan of available devices. This method will then call the
     * {@link DeviceDiscoveryActivity#scanDevices(boolean) scanDevices} method with "false" as the argument.</p>
     */
    private void stopScan() {
        mTabsAdapter.onScanFinished(DevicesListTabsAdapter.SCANNED_LIST_TYPE);
        scanDevices(false);
    }

    /**
     * To start the MainActivity.
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    /**
     * <p>This method is used to initialize all view components which will be used in this activity.</p>
     */
    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mTabsAdapter = new DevicesListTabsAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(pageChangeListener);

        mBtConnectBLE = (Button) findViewById(R.id.bt_connect_ble);
        mBtConnectBLE.setEnabled(false);
        mBtConnectBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectButtonClicked(BluetoothService.Transport.BLE);
            }
        });

        mBtConnectBREDR = (Button) findViewById(R.id.bt_connect_br_edr);
        mBtConnectBREDR.setEnabled(false);
        mBtConnectBREDR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectButtonClicked(BluetoothService.Transport.BR_EDR);
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void onConnectButtonClicked(@BluetoothService.Transport int transport) {
        stopScan();

        BluetoothDevice device = mTabsAdapter.getSelectedDevice();

        // keep information
        SharedPreferences sharedPref = getSharedPreferences(Consts.PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Consts.TRANSPORT_KEY, transport);
        editor.putString(Consts.BLUETOOTH_ADDRESS_KEY, device.getAddress());
        editor.apply();

        startMainActivity();
    }

    /**
     * <p>To register the bond state receiver to be aware of any bond state change.</p>
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mDiscoveryReceiver, filter);
    }

    /**
     * <p>To unregister the bond state receiver when the application is stopped or we don't need it anymore.</p>
     */
    private void unregisterReceiver() {
        unregisterReceiver(mDiscoveryReceiver);
    }


    // ------ INNER CLASS ---------------------------------------------------------------------------------------------

    /**
     * Callback for scan results.
     */
    private class LeScanCallback implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (mDevicesAdapter != null && device != null
                    && device.getName() != null && device.getName().length() > 0) {
                mDevicesAdapter.add(device, rssi);
            }
        }
    }

}
