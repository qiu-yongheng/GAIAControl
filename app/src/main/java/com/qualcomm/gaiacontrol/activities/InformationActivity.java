/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.InformationGaiaManager;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.ui.DividerItemDecoration;
import com.qualcomm.gaiacontrol.ui.adapters.InformationListAdapter;
import com.qualcomm.libraries.gaia.GAIA;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This activity displays available information about the device: battery level, RSSI, Api
 * version, etc.</p>
 */
public class InformationActivity extends ServiceActivity implements InformationListAdapter.IListAdapterListener,
        InformationGaiaManager.GaiaManagerListener {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    private static final String TAG = "InformationActivity";
    /**
     * The adapter for the information to display as items in the recycler view.
     */
    private InformationListAdapter mListAdapter;
    /**
     * To manage the GAIA packets which has been received from the device and which will be send to the device.
     */
    private InformationGaiaManager mGaiaManager;


    // ====== ENUM =======================================================================

    /**
     * <p>This enumeration groups all information which is displayed in the activity layout.</p>
     */
    @IntDef(flag = true, value = { DisplayedInformation.BATTERY, DisplayedInformation.RSSI,
            DisplayedInformation.API_VERSION, DisplayedInformation.NAME, DisplayedInformation.BLUETOOTH_ADDRESS,
            DisplayedInformation.BATTERY_STATUS })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface DisplayedInformation {
        /**
         * <p>This represents the information "name of the device".</p>
         */
        int NAME = 0;
        /**
         * <p>This represents the information "bluetooth address of the device".</p>
         */
        int BLUETOOTH_ADDRESS = 1;
        /**
         * <p>This represents the information "battery level of the device".</p>
         */
        int BATTERY = 2;
        /**
         * <p>This represents the information "battery status - on charge/not on charge - of the device".</p>
         */
        int BATTERY_STATUS = 3;
        /**
         * <p>This represents the information "RSSI level of the device".</p>
         */
        int RSSI = 4;
        /**
         * <p>This represents the information "API version of the device".</p>
         */
        int API_VERSION = 5;

        /**
         * This represents the total number of information which are displayed through this activity.
         */
        int NUMBER_OF_DISPLAYED_INFORMATION = 6;
    }


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        this.init();
    }

    @Override // Activity
    protected void onResume() {
        super.onResume();
        getInformationFromDevice();
    }

    @Override // Activity
    protected void onPause() {
        super.onPause();
        // cancel notifications
        mGaiaManager.getNotifications(InformationGaiaManager.Information.BATTERY, false);
        mGaiaManager.getNotifications(InformationGaiaManager.Information.RSSI, false);
    }


    // ====== SERVICE METHODS =======================================================================

    @Override // ServiceActivity
    protected void handleMessageFromService(Message msg) {
        //noinspection UnusedAssignment
        String handleMessage = "Handle a message from BLE service: ";

        switch (msg.what) {
            case BluetoothService.Messages.CONNECTION_STATE_HAS_CHANGED:
                getInformationFromDevice();
                @BluetoothService.State int connectionState = (int) msg.obj;
                String stateLabel = connectionState == BluetoothService.State.CONNECTED ? "CONNECTED"
                        : connectionState == BluetoothService.State.CONNECTING ? "CONNECTING"
                        : connectionState == BluetoothService.State.DISCONNECTING ? "DISCONNECTING"
                        : connectionState == BluetoothService.State.DISCONNECTED ? "DISCONNECTED"
                        : "UNKNOWN";
                displayLongToast(getString(R.string.toast_device_information) + stateLabel);
                if (DEBUG) Log.d(TAG, handleMessage + "CONNECTION_STATE_HAS_CHANGED: " + stateLabel);
                break;

            case BluetoothService.Messages.DEVICE_BOND_STATE_HAS_CHANGED:
                getInformationFromDevice();
                int bondState = (int) msg.obj;
                String bondStateLabel = bondState == BluetoothDevice.BOND_BONDED ? "BONDED"
                        : bondState == BluetoothDevice.BOND_BONDING ? "BONDING"
                        : "BOND NONE";
                displayLongToast(getString(R.string.toast_device_information) + bondStateLabel);
                if (DEBUG) Log.d(TAG, handleMessage + "DEVICE_BOND_STATE_HAS_CHANGED: " + bondStateLabel);
                break;

            case BluetoothService.Messages.GATT_SUPPORT:
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_SUPPORT");
                break;

            case BluetoothService.Messages.GAIA_PACKET:
                byte[] data = (byte[]) msg.obj;
                mGaiaManager.onReceiveGAIAPacket(data);
                break;

            case BluetoothService.Messages.GAIA_READY:
                getInformationFromDevice();
                if (DEBUG) Log.d(TAG, handleMessage + "GAIA_READY");
                break;

            case BluetoothService.Messages.GATT_READY:
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_READY");
                break;

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "UNKNOWN MESSAGE: " + msg.what);
                break;
        }
    }

    @Override // ServiceActivity
    protected void onServiceConnected() {
        @GAIA.Transport int transport = getTransport() == BluetoothService.Transport.BR_EDR ?
                GAIA.Transport.BR_EDR : GAIA.Transport.BLE;
        mGaiaManager = new InformationGaiaManager(this, transport);
        getInformationFromDevice();
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {
    }


    // ====== GAIA MANAGER METHODS =======================================================================

    @Override // InformationGaiaManager.GaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return mService!= null && mService.sendGAIAPacket(packet);
    }

    @Override // InformationGaiaManager.GaiaManagerListener
    public void onGetBatteryLevel(int level) {
            // we display the received value
            mListAdapter.setValue(DisplayedInformation.BATTERY, level + " mV");
    }

    @Override // InformationGaiaManager.GaiaManagerListener
    public void onGetRSSILevel(int level) {
            // we display the received value
            mListAdapter.setValue(DisplayedInformation.RSSI, level + " dBm");
    }

    @Override // InformationGaiaManager.GaiaManagerListener
    public void onGetAPIVersion(int versionPart1, int versionPart2, int versionPart3) {
        String APIText = versionPart1 + "." + versionPart2 + "." + versionPart3;
        mListAdapter.setValue(DisplayedInformation.API_VERSION, APIText);
    }

    @Override // InformationGaiaManager.GaiaManagerListener
    public void onInformationNotSupported(@InformationGaiaManager.Information int information) {
        switch (information) {
            case InformationGaiaManager.Information.API_VERSION:
                mListAdapter.setValue(DisplayedInformation.API_VERSION, getString(R.string.info_not_supported));
                break;
            case InformationGaiaManager.Information.BATTERY:
                mListAdapter.setValue(DisplayedInformation.BATTERY, getString(R.string.info_not_supported));
                mListAdapter.setValue(DisplayedInformation.BATTERY_STATUS, getString(R.string.info_not_supported));
                break;
            case InformationGaiaManager.Information.RSSI:
                mListAdapter.setValue(DisplayedInformation.RSSI, getString(R.string.info_not_supported));
                break;
        }
    }

    @Override // InformationGaiaManager.GaiaManagerListener
    public void onChargerConnected(boolean isConnected) {
        String text;
        if (isConnected) {
            text = getString(R.string.info_battery_status_in_charge);
        }
        else {
            text = getString(R.string.info_battery_status_no_charge);
        }
        mListAdapter.setValue(DisplayedInformation.BATTERY_STATUS, text);
    }


    // ====== OVERRIDE METHODS =======================================================================

    @Override // InformationListAdapter.IListAdapterListener
    public String getInformationName(int position) {
            switch (position) {
                case DisplayedInformation.NAME:
                    return getString(R.string.info_name);
                case DisplayedInformation.BLUETOOTH_ADDRESS:
                    return getString(R.string.info_bluetooth_address);
                case DisplayedInformation.RSSI:
                    return getString(R.string.info_rssi_signal);
                case DisplayedInformation.BATTERY:
                    return getString(R.string.info_battery_level);
                case DisplayedInformation.BATTERY_STATUS:
                    return getString(R.string.info_battery_status);
                case DisplayedInformation.API_VERSION:
                    return getString(R.string.info_api_version);
                default:
                    return getString(R.string.info_no_title);
            }

    }


    // ====== PUBLIC METHODS =======================================================================


    // ====== UI METHODS ======================================================================

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_info_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_information_list);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // add a divider to the list
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        // specify an adapter for the recycler view
        mListAdapter = new InformationListAdapter(this);
        recyclerView.setAdapter(mListAdapter);
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method requests all device information which is displayed in this activity such as the RSSI or battery
     * levels, the API version, etc.</p>
     */
    private void getInformationFromDevice() {
        if (mService!= null && mService.getConnectionState() == BluetoothService.State.CONNECTED
                && mService.isGaiaReady()) {
            BluetoothDevice device = mService.getDevice();
            mListAdapter.setValue(DisplayedInformation.NAME, device.getName());
            mListAdapter.setValue(DisplayedInformation.BLUETOOTH_ADDRESS, device.getAddress());
            mGaiaManager.getInformation(InformationGaiaManager.Information.API_VERSION);
            mGaiaManager.getNotifications(InformationGaiaManager.Information.BATTERY, true);
            mGaiaManager.getNotifications(InformationGaiaManager.Information.RSSI, true);
        }
    }

}
