/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.services.GAIABREDRService;
import com.qualcomm.gaiacontrol.services.GATTBLEService;

import java.lang.ref.WeakReference;

/**
 * <p>This class is the abstract activity to extend for each activity on this application which needs a Bluetooth
 * connection with a device. This class extends the {@link BluetoothActivity BluetoothActivity} in order to manage
 * the Bluetooth on/off events. This class binds to an Android service which implements
 * {@link BluetoothService BluetoothService} for the application depending on the transport chosen in the
 * SharedPreferences of the application.</p>
 */

public abstract class ServiceActivity extends BluetoothActivity {

    /**
     * The tag to use for the logs.
     */
    private static final String TAG = "ServiceActivity";
    /**
     * The BLE service to communicate with any device.
     */
    BluetoothService mService;
    /**
     * The service connection object to manage the service bind and unbind.
     */
    private final ServiceConnection mServiceConnection = new ActivityServiceConnection(this);
    /**
     * The handler used by the service to be linked to this activity.
     */
    private ActivityHandler mHandler;
    /**
     * To know if this activity is in the pause state.
     */
    private boolean mIsPaused;
    /**
     * The type of Bluetooth transport use to communicate with a Bluetooth device.
     */
    private @BluetoothService.Transport int mTransport = BluetoothService.Transport.UNKNOWN;


    // ====== ACTIVITY METHODS =====================================================================

    // When the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init();
    }

    // When the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();

        mIsPaused = false;

        if (mService != null) {
            initService();
        }
        else {
            Log.d(TAG, "BluetoothLEService not bound yet.");
        }
    }

    // When the activity is paused.
    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            mService.removeHandler(mHandler);
            mService = null;
            unbindService(mServiceConnection);
        }

    }


    // ====== PROTECTED METHODS ====================================================================

    /**
     * <p>To get the type of Bluetooth transport uses to communicate with a BluetoothDevice.</p>
     *
     * @return The transport, one of the followings:
     * {@link com.qualcomm.gaiacontrol.services.BluetoothService.Transport#BR_EDR BR_EDR},
     * {@link com.qualcomm.gaiacontrol.services.BluetoothService.Transport#BLE BLE} or
     * {@link com.qualcomm.gaiacontrol.services.BluetoothService.Transport#UNKNOWN UNKNOWN}.
     */
    /*package*/ @BluetoothService.Transport int getTransport() {
        return mTransport;
    }

    // ====== PUBLIC METHODS =======================================================================

    @Override
    public void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        if (mService == null) {
            startService();
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method allows to init the bound service by defining this activity as a handler listening its messages.</p>
     */
    private void initService() {
        mService.addHandler(mHandler);
        if (mService.getDevice() == null) {
            // get the bluetooth information
            SharedPreferences sharedPref = getSharedPreferences(Consts.PREFERENCES_FILE, Context.MODE_PRIVATE);

            // get the device Bluetooth address
            String address = sharedPref.getString(Consts.BLUETOOTH_ADDRESS_KEY, "");
            boolean done = mService.connectToDevice(address);
            if (!done) Log.w(TAG, "connection failed");
        }
    }

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // the Handler to receive messages from the GATTBLEService once attached
        mHandler = new ActivityHandler(this);
    }

    /**
     * <p>To start the Android Service which will allow this application to communicate with a BluetoothDevice.</p>
     * <p>This method will start the {@link GAIABREDRService GAIABREDRService} or the
     * {@link GATTBLEService GATTBLEService} depending on the content of the SharedPreferences file
     * {@link Consts#PREFERENCES_FILE}.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean startService() {
        // get the bluetooth information
        SharedPreferences sharedPref = getSharedPreferences(Consts.PREFERENCES_FILE, Context.MODE_PRIVATE);

        // get the device Bluetooth address
        String address = sharedPref.getString(Consts.BLUETOOTH_ADDRESS_KEY, "");
        if (address.length() == 0 || !BluetoothAdapter.checkBluetoothAddress(address)) {
            // no address, not possible to establish a connection
            return false;
        }

        // get the transport type
        int transport = sharedPref.getInt(Consts.TRANSPORT_KEY, BluetoothService.Transport.UNKNOWN);
        mTransport = transport == BluetoothService.Transport.BLE ? BluetoothService.Transport.BLE :
                        transport == BluetoothService.Transport.BR_EDR ? BluetoothService.Transport.BR_EDR :
                             BluetoothService.Transport.UNKNOWN;
        if (mTransport == BluetoothService.Transport.UNKNOWN) {
            // transport unknown, not possible to establish a connection
            return false;
        }

        // get the service class to bind
        Class<?> serviceClass = mTransport == BluetoothService.Transport.BLE ? GATTBLEService.class :
                GAIABREDRService.class; // mTransport can only be BLE or BR EDR

        // bind the service
        Intent gattServiceIntent = new Intent(this, serviceClass);
        gattServiceIntent.putExtra(Consts.BLUETOOTH_ADDRESS_KEY, address); // give address to the service
        return bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    // ====== ABSTRACT METHODS =====================================================================

    /**
     * <p>This method is called when the connected service is sending a message to the activity.</p>
     *
     * @param msg
     *          The message received from the service.
     */
    protected abstract void handleMessageFromService(Message msg);

    /**
     * <p>This method is called when the service has been bound to this activity.</p>
     */
    protected abstract void onServiceConnected();

    /**
     * <p>This method is called when the service has been unbound from this activity.</p>
     */
    @SuppressWarnings("EmptyMethod")
    protected abstract void onServiceDisconnected();


    // ====== INNER CLASS ==========================================================================

    /**
     * <p>This class is used to be informed of the connection state of the BLE service.</p>
     */
    private static class ActivityServiceConnection implements ServiceConnection {

        /**
         * The reference to this activity.
         */
        final WeakReference<ServiceActivity> mActivity;

        /**
         * The constructor for this activity service connection.
         *
         * @param activity
         *            this activity.
         */
        ActivityServiceConnection(ServiceActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ServiceActivity parentActivity = mActivity.get();

            if (componentName.getClassName().equals(GATTBLEService.class.getName())) {
                parentActivity.mService = ((GATTBLEService.LocalBinder) service).getService();
            }
            else if (componentName.getClassName().equals(GAIABREDRService.class.getName())) {
                parentActivity.mService = ((GAIABREDRService.LocalBinder) service).getService();
            }

            if (parentActivity.mService != null) {
                parentActivity.initService();
                parentActivity.onServiceConnected(); // to inform subclass
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (componentName.getClassName().equals(GATTBLEService.class.getName())) {
                ServiceActivity parentActivity = mActivity.get();
                parentActivity.mService = null;
                parentActivity.onServiceDisconnected(); // to inform subclass
            }
        }
    }

    /**
     * <p>This class is for receiving and managing messages from a {@link GATTBLEService}.</p>
     */
    private static class ActivityHandler extends Handler {

        /**
         * The reference to this activity.
         */
        final WeakReference<ServiceActivity> mReference;

        /**
         * The constructor for this activity handler.
         *
         * @param activity
         *            this activity.
         */
        ActivityHandler(ServiceActivity activity) {
            super();
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ServiceActivity activity = mReference.get();
            if (!activity.mIsPaused) {
                activity.handleMessageFromService(msg);
            }
        }
    }
}
