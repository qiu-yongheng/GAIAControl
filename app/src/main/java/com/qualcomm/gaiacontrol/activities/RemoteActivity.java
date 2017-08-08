/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.RemoteGaiaManager;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.libraries.gaia.GAIA;

/**
 * <p>This activity is for controlling the streamed sound of the audio device connected to the application as using a
 * remote control.</p>
 */
public class RemoteActivity extends ServiceActivity implements View.OnClickListener,
        RemoteGaiaManager.GaiaManagerListener {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    private static final String TAG = "RemoteActivity";
    /**
     * To manage the GAIA packets which have been received from the device and which will be sent to the device.
     */
    private RemoteGaiaManager mGaiaManager;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        this.init();
    }


    // ====== SERVICE METHODS =======================================================================

    @Override // ServiceActivity
    protected void handleMessageFromService(Message msg) {
        //noinspection UnusedAssignment
        String handleMessage = "Handle a message from BLE service: ";

        switch (msg.what) {
            case BluetoothService.Messages.CONNECTION_STATE_HAS_CHANGED:
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
        mGaiaManager = new RemoteGaiaManager(this, transport);
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {
    }


    // ====== GAIA MANAGER METHODS =======================================================================

    @Override // InformationGaiaManager.GaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return mService!= null && mService.isGaiaReady() && mService.sendGAIAPacket(packet);
    }

    @Override  // InformationGaiaManager.GaiaManagerListener
    public void onRemoteControlNotSupported() {
        displayLongToast(R.string.toast_remote_control_not_supported);
    }


    // ====== OVERRIDE METHODS =======================================================================

    @Override // View.OnClickListener
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_volume_up:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.VOLUME_UP);
                break;

            case R.id.bt_volume_down:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.VOLUME_DOWN);
                break;

            case R.id.bt_mute:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.MUTE);
                break;

            case R.id.bt_pause:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.PAUSE);
                break;

            case R.id.bt_play:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.PLAY);
                break;

            case R.id.bt_forward:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.FORWARD);
                break;

            case R.id.bt_rewind:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.REWIND);
                break;

            case R.id.bt_stop:
                mGaiaManager.sendControlCommand(RemoteGaiaManager.Controls.STOP);
                break;
        }

    }


    // ====== UI METHODS ======================================================================

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_remote_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        // adding listener for each button
        findViewById(R.id.bt_volume_down).setOnClickListener(this);
        findViewById(R.id.bt_volume_up).setOnClickListener(this);
        findViewById(R.id.bt_mute).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_forward).setOnClickListener(this);
        findViewById(R.id.bt_rewind).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        findViewById(R.id.bt_play).setOnClickListener(this);
    }

}
