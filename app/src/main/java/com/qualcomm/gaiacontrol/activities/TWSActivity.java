/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.TWSGaiaManager;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.ui.fragments.SpeakerFragment;
import com.qualcomm.libraries.gaia.GAIA;

/**
 * <p>his activity is for controlling the True Wireless Speaker of an audio device connected to the application.</p>
 */
public class TWSActivity extends ServiceActivity implements SpeakerFragment.ISpeakerFragmentListener,
        TWSGaiaManager.GaiaManagerListener {

    // ====== CONSTS ===============================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    private static final String TAG = "TWSActivity";
    /**
     * The fragment to define the channel and the volume for the master speaker.
     */
    private SpeakerFragment mMasterSpeakerFragment;
    /**
     * The fragment to define the channel and the volume for the slave speaker.
     */
    private SpeakerFragment mSlaveSpeakerFragment;
    /**
     * To manage the GAIA packets which have been received from the device and which will be sent to the device.
     */
    private TWSGaiaManager mGaiaManager;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity from ServiceActivity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tws);
        this.init();
    }

    @Override // Activity from ServiceActivity
    protected void onResume() {
        super.onResume();
        getInformation();
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
                getInformation();
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
        mGaiaManager = new TWSGaiaManager(this, transport);

        getInformation();
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {

    }


    // ====== SPEAKER FRAGMENT METHODS ===================================================================

    @Override // SpeakerFragment.ISpeakerFragmentListener
    public void setVolume(@TWSGaiaManager.Speaker int speaker, int volume) {
        mGaiaManager.setVolume(speaker, volume);
    }

    @Override // SpeakerFragment.ISpeakerFragmentListener
    public void setChannel(@TWSGaiaManager.Speaker int speaker, @TWSGaiaManager.Channel int channel) {
        mGaiaManager.setChannel(speaker, channel);
        switch (speaker) {
            case TWSGaiaManager.Speaker.MASTER_SPEAKER:
                askForSlaveChannel();
                break;
            case TWSGaiaManager.Speaker.SLAVE_SPEAKER:
                askForMasterChannel();
        }
    }


    // ====== GAIA MANAGER METHODS =======================================================================

    @Override // TWSGaiaManager.GaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return mService!= null && mService.isGaiaReady()
                && mService.sendGAIAPacket(packet);
    }

    @Override // TWSGaiaManager.GaiaManagerListener
    public void onGetChannel(@TWSGaiaManager.Speaker int speaker, @TWSGaiaManager.Channel int channel) {
        switch (speaker) {
        case TWSGaiaManager.Speaker.MASTER_SPEAKER:
            mMasterSpeakerFragment.setChannel(channel);
            break;
        case TWSGaiaManager.Speaker.SLAVE_SPEAKER:
            mSlaveSpeakerFragment.setChannel(channel);
            break;
        }
    }

    @Override // TWSGaiaManager.GaiaManagerListener
    public void onGetVolume(@TWSGaiaManager.Speaker int speaker, int volume) {
        switch (speaker) {
        case TWSGaiaManager.Speaker.MASTER_SPEAKER:
            mMasterSpeakerFragment.setVolume(volume);
            break;
        case TWSGaiaManager.Speaker.SLAVE_SPEAKER:
            mSlaveSpeakerFragment.setVolume(volume);
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
        this.getSupportActionBar().setLogo(R.drawable.ic_speakers_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        mMasterSpeakerFragment = (SpeakerFragment) getSupportFragmentManager().findFragmentById(R.id.f_master_speaker);
        mMasterSpeakerFragment.setSpeakerValue(TWSGaiaManager.Speaker.MASTER_SPEAKER);
        mSlaveSpeakerFragment = (SpeakerFragment) getSupportFragmentManager().findFragmentById(R.id.f_slave_speaker);
        mSlaveSpeakerFragment.setSpeakerValue(TWSGaiaManager.Speaker.SLAVE_SPEAKER);
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method requests all device information related to the TWS feature in order to initialise the displayed
     * values.</p>
     */
    private void getInformation() {
        if (mService != null && mService.isGaiaReady()) {
            askForMasterVolume();
            askForMasterChannel();
            askForSlaveVolume();
            askForSlaveChannel();
        }
    }

    /**
     * To request the state of the master speaker about its volume.
     */
    private void askForMasterVolume() {
        mGaiaManager.getVolume(TWSGaiaManager.Speaker.MASTER_SPEAKER);
    }

    /**
     * To request the state of the master speaker about its channel.
     */
    private void askForMasterChannel() {
        mGaiaManager.getChannel(TWSGaiaManager.Speaker.MASTER_SPEAKER);
    }

    /**
     * To request the state of the slave speaker about its volume.
     */
    private void askForSlaveVolume() {
        mGaiaManager.getVolume(TWSGaiaManager.Speaker.SLAVE_SPEAKER);
    }

    /**
     * To request the state of the slave speaker about its channel.
     */
    private void askForSlaveChannel() {
        mGaiaManager.getChannel(TWSGaiaManager.Speaker.SLAVE_SPEAKER);
    }

}
