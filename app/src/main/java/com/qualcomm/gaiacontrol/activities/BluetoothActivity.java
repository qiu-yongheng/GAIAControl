/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.receivers.BluetoothStateReceiver;

/**
 * <p>This class is the abstract activity to extend for each activity in this application which needs to use
 * Bluetooth. This class will initialise the {@link BluetoothAdapter BluetoothAdapter} and will check that Bluetooth
 * is enabled.</p>
 * <p>This class extends the {@link PermissionsActivity PermissionsActivity} and manages.</p>
 */

public abstract class BluetoothActivity extends PermissionsActivity
        implements BluetoothStateReceiver.BroadcastReceiverListener {

    /**
     * The Broadcast receiver we used to have information about the Bluetooth state on the device.
     */
    private BroadcastReceiver mBluetoothStateReceiver;

    /**
     * To know if we are using the application in debug mode.
     */
    static final boolean DEBUG = Consts.DEBUG;
    /**
     * The instance of the Bluetooth adapter used to retrieve paired Bluetooth devices.
     */
    BluetoothAdapter mBtAdapter;


    // ====== ACTIVITY METHODS =====================================================================

    // When the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init();
    }

    // Callback activated after the user responds to the enable Bluetooth dialogue.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Consts.ACTION_REQUEST_ENABLE_BLUETOOTH: {
                if (resultCode == RESULT_OK) {
                    onBluetoothEnabled();
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // When the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBluetoothStateReceiver, filter);

        checkEnableBt();
    }

    // When the activity is paused.
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBluetoothStateReceiver);
    }


    // ====== PROTECTED METHODS ====================================================================

    /**
     * To display a long toast inside this activity
     *
     * @param textID
     *              The ID of the text to display from the strings file.
     */
    void displayLongToast(int textID) {
        Toast.makeText(this, textID, Toast.LENGTH_LONG).show();
    }

    /**
     * To display a long toast inside this activity
     *
     * @param text
     *              The text to display.
     */
    void displayLongToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * To display a short toast inside this activity
     *
     * @param text
     *              The text to display.
     */
    void displayShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * <p>This method checks if Bluetooth is enabled on the phone.</p>
     *
     * @return true is the Bluetooth is enabled, false otherwise.
     */
    private boolean isBluetoothEnabled() {
        return mBtAdapter == null || !mBtAdapter.isEnabled();
    }


    // ====== PUBLIC METHODS =======================================================================

    @Override
    public void onBluetoothDisabled() {
        checkEnableBt();
    }

    @Override
    public void onBluetoothEnabled() {
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // Bluetooth adapter
        mBtAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        // Register for broadcasts on BluetoothAdapter state change so that we can tell if it has been turned off.
        mBluetoothStateReceiver = new BluetoothStateReceiver(this);
    }

    /**
     * Display a dialog requesting Bluetooth to be enabled if it isn't already. Otherwise this method updates the
     * list to the list view. The list view needs to be ready when this method is called.
     */
    private void checkEnableBt() {
        if (isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Consts.ACTION_REQUEST_ENABLE_BLUETOOTH);
        }
        else {
            onBluetoothEnabled();
        }
    }

}
