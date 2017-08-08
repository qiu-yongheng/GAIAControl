/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.MainGaiaManager;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.services.GATTBLEService;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.libraries.gaia.GAIA;

/**
 * <p>This activity is the main activity for this application. It navigates between all other activities depending on
 * the user choice about the feature they want to use.</p>
 */

public class MainActivity extends ServiceActivity implements View.OnClickListener,
        MainGaiaManager.MainGaiaManagerListener {

    // ====== CONSTS ===============================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    private static final String TAG = "MainActivity";


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * To have access to the instance which controls the led.
     */
    private Button mButtonLed;
    /**
     * To know if the led is activated.
     */
    private boolean mIsLedActivated = false;
    /**
     * To have access to the instance which displays the battery level.
     */
    private ImageView mImageViewBatteryLevel;
    /**
     * To have access to the instance which displays the device name.
     */
    private TextView mTextViewDeviceName;
    /**
     * To have access to the instance which displays the device connection status.
     */
    private TextView mTextViewDeviceConnectionState;
    /**
     * To have access to the instance which displays the device bond status.
     */
    private TextView mTextViewDeviceBondState;
    /**
     * To have access to the instance which displays the signal level.
     */
    private ImageView mImageViewSignalLevel;
    /**
     * To have access to the instance which displays the version number.
     */
    private TextView mTextViewVersionNumber;
    /**
     * The layout which contains all tiles.
     */
    private View mLayoutFeatures;
    /**
     * To manage the GAIA packets which has been received from the device and which will be send to the device.
     */
    private MainGaiaManager mGaiaManager;
    /**
     * <p>To know the GAIA features which are supported by the remote device. Each cell corresponds to a
     * {@link com.qualcomm.gaiacontrol.gaia.MainGaiaManager.Features Features}. This array is used as follows to know
     * if, for instance, the TWS feature is supported:<code>mGAIAFeatures[MainGaiaManager.Features.TWS]</code></p>
     */
    private final boolean[] mGAIAFeatures = new boolean[MainGaiaManager.FEATURES_NUMBER];
    /**
     * To know if the device is charging.
     */
    private boolean isCharging = false;
    /**
     * To know the current battery level.
     */
    private int mBatteryLevel = -1;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity from ServiceActivity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.init();
    }

    @Override // Activity from ServiceActivity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override // Activity from ServiceActivity
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mService != null) {
            @BluetoothService.State int state = mService.getConnectionState();
            int label = state == BluetoothService.State.CONNECTED || state == BluetoothService.State.DISCONNECTING ?
                    R.string.button_disconnect : R.string.button_connect;
            boolean enabled = state == BluetoothService.State.CONNECTED || state == BluetoothService.State.DISCONNECTED;

            MenuItem connect = menu.findItem(R.id.menu_item_connect);
            connect.setTitle(label);
            connect.setEnabled(enabled);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override // Activity from ServiceActivity
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // DISCONNECT/CONNECT option menu
            case R.id.menu_item_connect:
                invalidateOptionsMenu();
                if (mService != null && mService.getConnectionState() == BluetoothService.State.CONNECTED) {
                    mService.disconnectDevice();
                }
                else if (mService != null && mService.getConnectionState() == BluetoothService.State.DISCONNECTED) {
                    mService.reconnectToDevice();
                }
                return true;

            case android.R.id.home:
                mService.disconnectDevice();
                // return the default behavior

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override // Activity from ServiceActivity
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        if (mService != null) {
            refreshConnectionState(mService.getConnectionState());
            refreshBondState(mService.getBondState());
            invalidateOptionsMenu();
            getFeatures();
            getInformationFromDevice();
        }
    }

    @Override // Activity from ServiceActivity
    protected void onPause() {
        super.onPause();
        // cancelling all active notifications
        if (mService != null && mService.isGaiaReady()) {
            mGaiaManager.getNotifications(MainGaiaManager.Information.BATTERY, false);
            getRSSINotifications(false);
        }
    }


    // ====== SERVICE METHODS =======================================================================

    @Override // ServiceActivity
    protected void handleMessageFromService(Message msg) {
        //noinspection UnusedAssignment
        String handleMessage = "Handle a message from BLE service: ";

        switch (msg.what) {
            case BluetoothService.Messages.CONNECTION_STATE_HAS_CHANGED:
                @BluetoothService.State int connectionState = (int) msg.obj;
                refreshConnectionState(connectionState);
                if (DEBUG) {
                    String stateLabel = connectionState == BluetoothService.State.CONNECTED ? "CONNECTED"
                            : connectionState == BluetoothService.State.CONNECTING ? "CONNECTING"
                            : connectionState == BluetoothService.State.DISCONNECTING ? "DISCONNECTING"
                            : connectionState == BluetoothService.State.DISCONNECTED ? "DISCONNECTED"
                            : "UNKNOWN";
                    Log.d(TAG, handleMessage + "CONNECTION_STATE_HAS_CHANGED: " + stateLabel);
                }
                break;

            case BluetoothService.Messages.DEVICE_BOND_STATE_HAS_CHANGED:
                int bondState = (int) msg.obj;
                refreshBondState(bondState);
                if (DEBUG) {
                    String bondStateLabel = bondState == BluetoothDevice.BOND_BONDED ? "BONDED"
                            : bondState == BluetoothDevice.BOND_BONDING ? "BONDING"
                            : "BOND NONE";
                    Log.d(TAG, handleMessage + "DEVICE_BOND_STATE_HAS_CHANGED: " + bondStateLabel);
                }
                break;

            case BluetoothService.Messages.GATT_SUPPORT:
                GATTServices gattServices = (GATTServices) msg.obj;
                if (!gattServices.gattServiceGaia.isSupported()) {
                    displayLongToast(R.string.toast_gaia_not_supported);
                }
                if (mService != null && mService.isGattReady()) {
                    enableGattFeatures(gattServices);
                }
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_SUPPORT");
                break;

            case BluetoothService.Messages.GAIA_PACKET:
                byte[] data = (byte[]) msg.obj;
                mGaiaManager.onReceiveGAIAPacket(data);
                // no log as these will be logged by the GAIA manager
                break;

            case BluetoothService.Messages.GAIA_READY:
                getFeatures();
                getInformationFromDevice();
                if (DEBUG) Log.d(TAG, handleMessage + "GAIA_READY");
                break;

            case BluetoothService.Messages.GATT_READY:
                if (mService != null) {
                    enableGattFeatures(mService.getGattSupport());
                }
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_READY");
                break;

            case BluetoothService.Messages.GATT_MESSAGE:
                @GATTBLEService.GattMessage int gattMessage = msg.arg1;
                Object content = msg.obj;
                onReceiveGattMessage(gattMessage, content);
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_MESSAGE");
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
        mGaiaManager = new MainGaiaManager(this, transport);

        getGeneralDeviceInformation();
        refreshConnectionState(mService.getConnectionState());
        refreshBondState(mService.getBondState());
        getFeatures();
        getInformationFromDevice();
        enableGattFeatures(mService.getGattSupport());
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {
    }


    // ====== GAIA MANAGER METHODS =======================================================================

    @Override // MainGaiaManager.MainGaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return
                mService!= null && mService.sendGAIAPacket(packet);
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onFeatureSupported(@MainGaiaManager.Features int feature) {
        switch (feature) {
            case MainGaiaManager.Features.LED:
                mGAIAFeatures[MainGaiaManager.Features.LED] = true;
                mButtonLed.setEnabled(true);
                break;
            case MainGaiaManager.Features.EQUALIZER:
                mGAIAFeatures[MainGaiaManager.Features.EQUALIZER] = true;
                findViewById(R.id.bt_equalizer).setEnabled(true);
                break;
            case MainGaiaManager.Features.TWS:
                mGAIAFeatures[MainGaiaManager.Features.TWS] = true;
                findViewById(R.id.bt_tws).setEnabled(true);
                break;
            case MainGaiaManager.Features.REMOTE_CONTROL:
                mGAIAFeatures[MainGaiaManager.Features.REMOTE_CONTROL] = true;
                findViewById(R.id.bt_remote).setEnabled(true);
                break;
            case MainGaiaManager.Features.UPGRADE:
                mGAIAFeatures[MainGaiaManager.Features.UPGRADE] = true;
                findViewById(R.id.bt_upgrade).setEnabled(true);
                break;
        }
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onGetLedControl(boolean activate) {
        refreshButtonLedImage(activate);
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onGetBatteryLevel(int level) {
        mImageViewBatteryLevel.setVisibility(View.VISIBLE);
        mBatteryLevel = level;
        // we display the received value
        refreshBatteryLevel();
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onGetRSSILevel(int level) {
        mImageViewSignalLevel.setVisibility(View.VISIBLE);
        // we display the received level
        refreshRSSISignal(level);
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onGetAPIVersion(int versionPart1, int versionPart2, int versionPart3) {
        String APIText = "API version " + versionPart1 + "." + versionPart2 + "." + versionPart3;
        mTextViewVersionNumber.setVisibility(View.VISIBLE);
        mTextViewVersionNumber.setText(APIText);
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onInformationNotSupported(@MainGaiaManager.Information int information) {
        switch (information) {

            case MainGaiaManager.Information.API_VERSION:
                mTextViewVersionNumber.setVisibility(View.GONE);
                break;
            case MainGaiaManager.Information.BATTERY:
                mImageViewBatteryLevel.setVisibility(View.GONE);
                break;
            case MainGaiaManager.Information.LED:
                mButtonLed.setEnabled(false);
                break;
            case MainGaiaManager.Information.RSSI:
                mImageViewSignalLevel.setVisibility(View.GONE);
                break;
        }
    }

    @Override // MainGaiaManager.MainGaiaManagerListener
    public void onChargerConnected(boolean isConnected) {
        isCharging = isConnected;
        refreshBatteryLevel();
    }


    // ====== PUBLIC METHODS =======================================================================

    @Override // View.OnClickListener
    public void onClick(View v) {
        if (mService != null && mService.getConnectionState() == BluetoothService.State.CONNECTED) {
            switch (v.getId()) {
                case R.id.bt_led:
                    onClickLedButton();
                    break;
                case R.id.bt_device_information:
                    Intent intentDevice = new Intent(this, InformationActivity.class);
                    startActivity(intentDevice);
                    break;
                case R.id.bt_tws:
                    Intent intentTWS = new Intent(this, TWSActivity.class);
                    startActivity(intentTWS);
                    break;
                case R.id.bt_remote:
                    Intent intentRemote = new Intent(this, RemoteActivity.class);
                    startActivity(intentRemote);
                    break;
                case R.id.bt_equalizer:
                    Intent intentEqualizer = new Intent(this, EqualizerActivity.class);
                    startActivity(intentEqualizer);
                    break;

                case R.id.bt_upgrade:
                    Intent intentUpgrade = new Intent(this, UpgradeActivity.class);
                    startActivity(intentUpgrade);
                    break;

                case R.id.bt_proximity:
                    Intent intentProximity = new Intent(this, ProximityActivity.class);
                    startActivity(intentProximity);
                    break;

                case R.id.bt_find_device:
                    Intent intentFindMe = new Intent(this, FindMeActivity.class);
                    startActivity(intentFindMe);
                    break;

                case R.id.bt_battery_level:
                    Intent intentBattery = new Intent(this, BatteryActivity.class);
                    startActivity(intentBattery);
                    break;

                case R.id.bt_heart_rate:
                    Intent intentHeartRate = new Intent(this, HeartRateActivity.class);
                    startActivity(intentHeartRate);
                    break;

                default:
                    displayLongToast(R.string.toast_not_implemented);
            }
        }
        else {
            displayLongToast(R.string.toast_not_connected);
        }
    }


    // ====== UI METHODS ======================================================================

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_launcher_transparent);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        mButtonLed = (Button) findViewById(R.id.bt_led);
        mButtonLed.setOnClickListener(this);

        mImageViewBatteryLevel = (ImageView) findViewById(R.id.iv_battery);
        mImageViewSignalLevel = (ImageView) findViewById(R.id.iv_signal);
        mTextViewDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mTextViewDeviceConnectionState = (TextView) findViewById(R.id.tv_device_connection_state);
        mTextViewDeviceBondState = (TextView) findViewById(R.id.tv_device_bond_state) ;
        mTextViewVersionNumber = (TextView) findViewById(R.id.tv_device_info);
        mLayoutFeatures = findViewById(R.id.ll_features_buttons);

        findViewById(R.id.bt_equalizer).setOnClickListener(this);
        findViewById(R.id.bt_device_information).setOnClickListener(this);
        findViewById(R.id.bt_tws).setOnClickListener(this);
        findViewById(R.id.bt_upgrade).setOnClickListener(this);
        findViewById(R.id.bt_remote).setOnClickListener(this);
        findViewById(R.id.bt_find_device).setOnClickListener(this);
        findViewById(R.id.bt_heart_rate).setOnClickListener(this);
        findViewById(R.id.bt_proximity).setOnClickListener(this);
        findViewById(R.id.bt_battery_level).setOnClickListener(this);
    }

    /**
     * To display the button LED as activated or deactivated.
     *
     * @param activate
     *         true to display the device led as activated. Should fit with should the led state.
     */
    private void refreshButtonLedImage(boolean activate) {
        mButtonLed.setEnabled(true);

        if (activate) {
            mButtonLed.setBackgroundResource(R.drawable.tile_led_on);
            mButtonLed.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_light_on_56dp, 0, 0);
        } else {
            mButtonLed.setBackgroundResource(R.drawable.tile_led_off);
            mButtonLed.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_light_off_56dp, 0, 0);
        }
    }

    /**
     * <p>This method updates the connection state label and the connection button and enables or disables
     * features depending on the connection state given as a parameter.</p>
     * <p>If the state is connecting or disconnecting an indeterminate progress bar is displayed.</p>
     *
     * @param state
     *          The connection state to display in the UI.
     */
    private void refreshConnectionState(@BluetoothService.State int state) {
        // display the connection state
        int connectionLabel = state == BluetoothService.State.CONNECTING ? R.string.device_state_connecting
                : state == BluetoothService.State.DISCONNECTING ? R.string.device_state_disconnecting
                : state == BluetoothService.State.CONNECTED ? R.string.device_state_connected
                : state == BluetoothService.State.DISCONNECTED ? R.string.device_state_disconnected
                : R.string.device_state_connection_unknown;
        mTextViewDeviceConnectionState.setText(connectionLabel);

        // display "CONNECT" or "DISCONNECT" for the connection button
        invalidateOptionsMenu();

        // activate or deactivate buttons
        enableAllFeatures(state == BluetoothService.State.CONNECTED);
    }

    /**
     * <p>This method updates the bond state label in the UI with the bond state given as a parameter.</p>
     * <p>If the state is bonding, an indeterminate progress bar is displayed.</p>
     *
     * @param bondState
     *          The bond state to display in the UI.
     */
    private void refreshBondState(int bondState) {
        // display the bond state
        String label = bondState == BluetoothDevice.BOND_BONDED ? getString(R.string.device_state_bonded)
                : bondState == BluetoothDevice.BOND_BONDING ? getString(R.string.device_state_bonding)
                : getString(R.string.device_state_not_bonded);
        mTextViewDeviceBondState.setText(label);
    }

    /**
     * To display the corresponding image depending on the value for the RSSI level.
     *
     * @param rssi
     *         the corresponding value to display.
     */
    private void refreshRSSISignal(int rssi) {
        // The RSSI is a negative number, Close to zero, the signal is strong, far and away the signal is low.
        // We consider between -60 and 0 the signal stays strength. Then the strength level decreases by 10 until -90.
        if (-60 <= rssi && rssi <= 0) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_level_4_white_32dp);
        } else if (-70 <= rssi && rssi < -60) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_level_3_white_32dp);
        } else if (-80 <= rssi && rssi < -70) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_level_2_white_32dp);
        } else if (-90 <= rssi && rssi < -80) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_level_1_white_32dp);
        } else if (rssi < -90) {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_level_0_white_32dp);
        } else {
            mImageViewSignalLevel.setImageResource(R.drawable.ic_signal_unknown_white_32dp);
        }
    }

    /**
     * To display the battery level as an image depending on the level value.
     */
    private void refreshBatteryLevel() {
        // The battery level to display depends on a percentage, we calculate the percentage.
        int value = mBatteryLevel * 100 / Consts.BATTERY_LEVEL_MAX;

        // depending on the percentage for the battery level and if the battery is charging we display the corresponding
        // feature.
        // We pick the number depending on images we have.
        if (value >= 0) {
            if (isCharging) {
                if (value > 80) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_4_32dp);
                } else if (value > 60) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_3_32dp);
                } else if (value > 40) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_2_32dp);
                } else if (value > 20) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_1_32dp);
                } else {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_charging_0_32dp);
                }
            } else {
                if (value > 80) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_level_4_32dp);
                } else if (value > 60) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_level_3_32dp);
                } else if (value > 40) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_level_2_32dp);
                } else if (value > 20) {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_level_1_32dp);
                } else {
                    mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_level_0_32dp);
                }
            }
        } else {
            mImageViewBatteryLevel.setImageResource(R.drawable.ic_battery_unknown_32dp);
        }
    }

    /**
     * <p>To enable or disable all the feature buttons.</p>
     *
     * @param enabled
     *              True to enable the buttons, false to disable them.
     */
    private void enableAllFeatures(boolean enabled) {
        if (enabled) {
            enableGaiaFeatures();
            if (mService != null) {
                enableGattFeatures(mService.getGattSupport());
            }
        }
        else {
            enableChildView(mLayoutFeatures, false);
        }
    }

    /**
     * <p>To enable the buttons which correspond to the GAIA
     * {@link com.qualcomm.gaiacontrol.gaia.MainGaiaManager.Features Features}.</p>
     * <p>This method enables the button if the feature is defined as supported in the
     * {@link #mGAIAFeatures mGAIAFeatures} array.</p>
     */
    private void enableGaiaFeatures() {
        mButtonLed.setEnabled(mGAIAFeatures[MainGaiaManager.Features.LED]);
        enableChildView(findViewById(R.id.bt_device_information), true);
        findViewById(R.id.bt_equalizer).setEnabled(mGAIAFeatures[MainGaiaManager.Features.EQUALIZER]);
        findViewById(R.id.bt_tws).setEnabled(mGAIAFeatures[MainGaiaManager.Features.TWS]);
        findViewById(R.id.bt_remote).setEnabled(mGAIAFeatures[MainGaiaManager.Features.REMOTE_CONTROL]);
        findViewById(R.id.bt_upgrade).setEnabled(mGAIAFeatures[MainGaiaManager.Features.UPGRADE]);
    }

    /**
     * <p>To enable the buttons which correspond to the GATT features.</p>
     *
     * @param gattServices
     *          The object which gives the information on which GATT features are supported by the remote device.
     */
    private void enableGattFeatures(GATTServices gattServices) {
        if (gattServices == null) {
            findViewById(R.id.bt_proximity).setEnabled(false);
            findViewById(R.id.bt_find_device).setEnabled(false);
            findViewById(R.id.bt_battery_level).setEnabled(false);
            findViewById(R.id.bt_heart_rate).setEnabled(false);
        }
        else {
            findViewById(R.id.bt_proximity).setEnabled(gattServices.isGattProfileProximitySupported());
            findViewById(R.id.bt_find_device).setEnabled(gattServices.isGattProfileFindMeSupported());
            findViewById(R.id.bt_battery_level).setEnabled(gattServices.isBatteryServiceSupported());
            findViewById(R.id.bt_heart_rate).setEnabled(gattServices.isGattProfileHeartRateSupported());
        }
    }

    /**
     * <p>This method allows all the child components of the given view to be enabled or disabled. The given view is
     * only disabled if it is not a ViewGroup.</p>
     *
     * @param view
     *          The view which all child components should be deactivated.
     * @param enabled
     *          True to enable the view, false to disable it.
     */
    private void enableChildView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                enableChildView(child, enabled);
            }
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * When the user clicks on the led button this method is called.
     */
    private void onClickLedButton() {
        mIsLedActivated = !mIsLedActivated;
        mGaiaManager.setLedState(mIsLedActivated);
        refreshButtonLedImage(mIsLedActivated);
    }

    /**
     * This method requests all general information the GAIA Service knows without doing any request to the device.
     */
    private void getGeneralDeviceInformation() {
        BluetoothDevice device = mService.getDevice();
        String deviceName = device == null ? "" : device.getName();
        mTextViewDeviceName.setText(deviceName);
    }

    /**
     * <p>This method allows request of all features the remote device supports in order to let the user interact
     * with them.</p>
     */
    private void getFeatures() {
        if (mService != null && mService.getConnectionState() == BluetoothService.State.CONNECTED
                && mService.isGaiaReady()) {
            mGaiaManager.checkFeatureSupport(MainGaiaManager.Features.LED);
            mGaiaManager.checkFeatureSupport(MainGaiaManager.Features.EQUALIZER);
            mGaiaManager.checkFeatureSupport(MainGaiaManager.Features.TWS);
            mGaiaManager.checkFeatureSupport(MainGaiaManager.Features.REMOTE_CONTROL);
            mGaiaManager.checkFeatureSupport(MainGaiaManager.Features.UPGRADE);
        }
    }

    /**
     * <p>This method requests all device information which are displayed in this activity such as the RSSI or battery
     * levels, the API version, etc.</p>
     */
    private void getInformationFromDevice() {
        if (mService != null && mService.getConnectionState() == BluetoothService.State.CONNECTED
                && mService.isGaiaReady()) {
            mGaiaManager.getInformation(MainGaiaManager.Information.API_VERSION);
            mGaiaManager.getInformation(MainGaiaManager.Information.RSSI);
            mGaiaManager.getInformation(MainGaiaManager.Information.BATTERY);
            mGaiaManager.getInformation(MainGaiaManager.Information.LED);
            mGaiaManager.getNotifications(MainGaiaManager.Information.BATTERY, true);
            getRSSINotifications(true);
        }
    }

    /**
     * <p>To enable or disable the RSSI notifications:
     * <ul>
     *     <li>Activation: this method will first attempt the activate them through the GATTBLEService to have the
     *     most accurate value. However if this activation fails - see
     *     {@link GATTBLEService#startRssiUpdates(boolean) startRssiUpdates} for more information - this method will
     *     enable the notifications using the GAIA command
     *     {@link com.qualcomm.libraries.gaia.GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI} if available
     *     through the {@link MainGaiaManager MainGaiaManager}.</li>
     *     <li>Deactivation: this method will deactivate the notifications from the Service as well as from the
     *     GAIA manager.</li>
     * </ul></p>
     * <p></p>
     *
     * @param notify True to enable the RSSI notifications, false to disable them.
     */
    private void getRSSINotifications(boolean notify) {
        if (notify && !mService.startRssiUpdates(true)) {
            // it is not possible to use the BLE way so we use GAIA
            mGaiaManager.getNotifications(MainGaiaManager.Information.RSSI, true);
        }
        else if (!notify) {
            mService.startRssiUpdates(false);
            mGaiaManager.getNotifications(MainGaiaManager.Information.RSSI, false);
        }
    }

    /**
     * <p>This method is called when this activity receives a
     * {@link com.qualcomm.gaiacontrol.services.GATTBLEService.GattMessage GattMessage} from the Service.</p>
     * <p>This method will act depending on the type of GATT message which has been broadcast to this activity.</p>
     *
     * @param gattMessage
     *          The GATT Message type.
     * @param content
     *          Any complementary information provided with the GATT Message.
     */
    private void onReceiveGattMessage(@GATTBLEService.GattMessage int gattMessage, Object content) {
        if (gattMessage == GATTBLEService.GattMessage.RSSI_LEVEL) {
            int rssi = (int) content;
            onGetRSSILevel(rssi);
        }
    }

}
