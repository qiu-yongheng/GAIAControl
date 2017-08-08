/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.ProximityGaiaManager;
import com.qualcomm.gaiacontrol.models.gatt.GATT;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.services.GATTBLEService;
import com.qualcomm.libraries.gaia.GAIA;

import java.util.Locale;

/**
 * <p>This activity for demonstration of the Proximity Profile over BLE.</p>
 */
public class ProximityActivity extends ServiceActivity implements ProximityGaiaManager.ProximityGaiaManagerListener {

    // ====== CONSTS FIELDS =======================================================================

    /**
     * For the debug mode, the tag to display for logs.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ProximityActivity";
    /**
     * All default values used in this activity.
     */
    private static class Default {
        /**
         * <p>The default threshold value for the MILD immediate alert level.</p>
         */
        private static final int THRESHOLD_MILD = 60;
        /**
          <p>The default threshold value for the HIGH immediate alert level.</p>
         */
        private static final int THRESHOLD_HIGH = 90;
        /**
         * <p>The default value for the TX Power level.</p>
         */
        private static final int TX_POWER_LEVEL = 0;
        /**
         * <p>The default value for the RSSI level.</p>
         */
        private static final int RSSI = 0;
        /**
         * <p>The default value for the path loss.</p>
         */
        private static final int PATH_LOSS = TX_POWER_LEVEL - RSSI;
        /**
         * <p>The default value for Alert Levels.</p>
         */
        private static final int ALERT_LEVEL = GATT.AlertLevel.Levels.NONE;
    }


    // ====== UI PRIVATE FIELDS =======================================================================

    /**
     * <p>All buttons to display the different values for the Link Loss level the user can select.</p>
     */
    private final Button[] mLinkLossAlertLevelButtons = new Button[GATT.AlertLevel.Levels.NUMBER_OF_LEVELS];
    /**
     * <p>The edit text to display the mild threshold value and let the user type one.</p>
     */
    private EditText mEditTextThresholdMild;
    /**
     * <p>The edit text to display the high threshold value and let the user type one.</p>
     */
    private EditText mEditTextThresholdHigh;
    /**
     * <p>The text view to display the current RSSI level.</p>
     */
    private TextView mTextViewRssiValue;
    /**
     * <p>The snack bar alert to display an error/alert message.</p>
     */
    private TextView mBarAlertDisconnection;
    /**
     * <p>The layout which groups the Link Loss alert settings.</p>
     */
    private View mLayoutLinkLossAlertLevel;
    /**
     * <p>The layout which groups the proximity threshold settings.</p>
     */
    private View mLayoutProximityThreshold;
    /**
     * <p>The message to display if the Link Loss service is considered as unavailable.</p>
     */
    private View mViewLinkLossAlertLevelUnavailable;
    /**
     * <p>The message to display if the immediate alert service - as much as the tw power one - is considered as
     * unavailable.</p>
     */
    private View mViewProximityThresholdUnavailable;
    /**
     * <p>The whole layout of this view.</p>
     */
    private View mMainLayout;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The current value for the mild threshold.</p>
     */
    private int mThresholdMild = Default.THRESHOLD_MILD;
    /**
     * <p>The current value for the high threshold.</p>
     */
    private int mThresholdHigh = Default.THRESHOLD_HIGH;
    /**
     * <p>The current value of the power level as get from the remote device.</p>
     */
    private int mTXPowerLevel = Default.TX_POWER_LEVEL;
    /**
     * <p>The current path loss value calculated with : tx power - rssi.</p>
     */
    private int mPathLoss = Default.PATH_LOSS;
    /**
     * <p>The current value of the immediate alert level defined by this application.</p>
     */
    private int mImmediateAlert = Default.ALERT_LEVEL;
    /**
     * <p>The current value of the link loss alert level.</p>
     */
    private int mLinkLossAlertLevel = Default.ALERT_LEVEL;
    /**
     * <p>The current value of the RSSI level.</p>
     */
    private int mRssi = Default.RSSI;
    /**
     * <p>To manage the GAIA packets which have been received from the device and which will be sent to the device.</p>
     */
    private ProximityGaiaManager mGaiaManager;
    /**
     * <p>To know if getting the RSSI value form the device is supported.</p>
     * <p>This is supported if the application can get a RSSI value through the GATT connection or the corresponding
     * GAIA command.</p>
     * <p>By default it is considered as supported.</p>
     */
    private boolean isRssiSupported = true;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity);
        this.init();
    }

    @Override // Activity
    protected void onPause() {
        super.onPause();
        // we disable the RSSI notifications
        getRSSINotifications(false);
    }


    // ====== SERVICE METHODS =======================================================================

    @Override // ServiceActivity
    protected void handleMessageFromService(Message msg) {
        //noinspection UnusedAssignment
        String handleMessage = "Handle a message from BLE service: ";

        switch (msg.what) {
            case BluetoothService.Messages.CONNECTION_STATE_HAS_CHANGED:
                @BluetoothService.State int connectionState = (int) msg.obj;
                onConnectionStateChanged(connectionState);
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
                GATTServices services = (GATTServices) msg.obj;
                onGattSupport(services);
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_SUPPORT");
                break;

            case BluetoothService.Messages.GAIA_PACKET:
                byte[] data = (byte[]) msg.obj;
                mGaiaManager.onReceiveGAIAPacket(data);
                // no log as these will be logged by the GAIA manager
                break;

            case BluetoothService.Messages.GAIA_READY:
                if (DEBUG) Log.d(TAG, handleMessage + "GAIA_READY");
                break;

            case BluetoothService.Messages.GATT_READY:
                onGattReady();
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_READY");
                break;

            case BluetoothService.Messages.GATT_MESSAGE:
                @GATTBLEService.GattMessage int gattMessage = msg.arg1;
                Object content = msg.obj;
                onReceiveGattMessage(gattMessage, content);
                if (DEBUG) Log.d(TAG, handleMessage + "GATT_MESSAGE");
                break;

            case BluetoothService.Messages.UPGRADE_MESSAGE:
                if (DEBUG) Log.d(TAG, handleMessage + "UPGRADE_MESSAGE");
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
        mGaiaManager = new ProximityGaiaManager(this, transport);

        if (mService != null) {
            GATTServices support = mService.getGattSupport();
            initInformation(support);
            if (support == null || !support.isGattProfileProximitySupported()) {
                displayLongToast(R.string.toast_proximity_not_supported);
            }
        }
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {

    }


    // ====== GAIA MANAGER METHODS =======================================================================

    @Override // ProximityGaiaManager.ProximityGaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return mService!= null && mService.sendGAIAPacket(packet);
    }

    @Override // ProximityGaiaManager.ProximityGaiaManagerListener
    public void onRSSINotSupported() {
        // RSSI level is required to calculate the immediate alert
        isRssiSupported = false;
        mLayoutProximityThreshold.setVisibility(View.GONE);
        mViewProximityThresholdUnavailable.setVisibility(View.VISIBLE);
    }

    @Override // ProximityGaiaManager.ProximityGaiaManagerListener
    public void onGetRSSILevel(int level) {
        mRssi = level;
        updatePathLoss(level, mTXPowerLevel);
        updateRssiSignalDisplay(level);
    }


    // ====== PRIVATE METHODS =================================================================

    /**
     * <p>To initialise all information which is displayed.</p>
     * <p>This method will show or hide the settings which are known as available and sets them up to their known
     * value.</p>
     *
     * @param support The GATT services which are supported by the device in order to know the available settings for
     * the proximity profile.
     */
    private void initInformation(GATTServices support) {
        if (support != null && support.isGattProfileProximitySupported()) {
            // GATT service Link Loss is supported as it is mandatory for the Proximity profile
            mService.requestLinkLossAlertLevel();
            mLayoutLinkLossAlertLevel.setVisibility(View.VISIBLE);
            mViewLinkLossAlertLevelUnavailable.setVisibility(View.GONE);

            if (support.gattServiceimmediateAlert.isSupported() && support.gattServicetxPower.isSupported()
                    && isRssiSupported) {
                // if both services are supported they can be used
                mService.requestTxPowerLevel();
                mLayoutProximityThreshold.setVisibility(View.VISIBLE);
                mViewProximityThresholdUnavailable.setVisibility(View.GONE);
                getRSSINotifications(true);
            }
            else {
                // the alert threshold is not available as at least one of service is not supported
                mLayoutProximityThreshold.setVisibility(View.GONE);
                mViewProximityThresholdUnavailable.setVisibility(View.VISIBLE);
            }
        }
        else {
            // the Proximity Profile is not supported
            mLayoutLinkLossAlertLevel.setVisibility(View.GONE);
            mViewLinkLossAlertLevelUnavailable.setVisibility(View.VISIBLE);
            mLayoutProximityThreshold.setVisibility(View.GONE);
            mViewProximityThresholdUnavailable.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p>To set up the value of the mild threshold when the user interacts with it - through the minus and plus buttons
     * or the Edit Text directly.</p>
     * <p>This method checks that the value is possible prior to set it up.</p>
     *
     * @param threshold The new threshold value selected by the user.
     */
    private void setThresholdMild(int threshold) {
        if (threshold < mThresholdHigh) {
            mThresholdMild = threshold;
        }
        else {
            displayLongToast(R.string.toast_proximity_threshold_not_in_range);
        }
        mEditTextThresholdMild.setText(String.format(Locale.getDefault(), "%d", mThresholdMild));
        checkThreshold(mPathLoss);
    }

    /**
     * <p>To set up the value of the HIGH threshold when user interacts with it - through the minus and plus buttons
     * or the Edit Text directly.</p>
     * <p>This method checks that the value is possible prior to set it up.</p>
     *
     * @param threshold The new threshold value selected by the user.
     */
    private void setThresholdHigh(int threshold) {
        if (mThresholdMild < threshold) {
            mThresholdHigh = threshold;
        }
        else {
            displayLongToast(R.string.toast_proximity_threshold_not_in_range);
        }
        mEditTextThresholdHigh.setText(String.format(Locale.getDefault(), "%d", mThresholdHigh));
        checkThreshold(mPathLoss);
    }

    /**
     * <p>This method is called when the Service dispatches a CONNECTION_STATE_HAS_CHANGED message.</p>
     * <p>This method will disable all UI components when the device is known as not connected - disconnecting,
     * connecting, disconnected. It will also disable any ongoing notifications and will display a connection state
     * alert.</p>
     * <p>If the device is disconnected this method will request to the GATTBLEService to reconnect to the device.</p>
     *
     * @param connectionState The new connection state
     */
    private void onConnectionStateChanged(int connectionState) {
        // automatic reconnection for when the device will be available
        if (connectionState == BluetoothService.State.DISCONNECTED) {
            mService.reconnectToDevice();
        }

        boolean connected = connectionState == BluetoothService.State.CONNECTED;

        // show or hide the alert message
        int visibility = connected ? View.GONE : View.VISIBLE;
        mBarAlertDisconnection.setVisibility(visibility);

        if (!connected) {
            // disable ongoing notifications
            getRSSINotifications(false);

            // UI
            enableChildView(mMainLayout, false);
            mBarAlertDisconnection.setVisibility(View.VISIBLE);
            int message = connectionState == BluetoothService.State.DISCONNECTED ? R.string.alert_message_disconnected :
                             connectionState == BluetoothService.State.DISCONNECTING ? R.string.alert_message_disconnecting :
                             connectionState == BluetoothService.State.CONNECTING ? R.string.alert_message_connecting :
                                     R.string.alert_message_connection_state_unknown;
            mBarAlertDisconnection.setText(message);
        }

    }

    /**
     * <p>This method is called when the GATT connection can be used to communicate with the device.</p>
     * <p>This method enables all the UI components to let the user interacts with them.</p>
     */
    private void onGattReady() {
        if (mService.getConnectionState() == BluetoothService.State.CONNECTED) {
            enableChildView(mMainLayout, true);
        }
    }

    /**
     * <p>This method is called when the GATTBLEService knows the services the device supports.</p>
     * <p>This method initialises the UI with the known information.</p>
     *
     * @param services the object which describes the supported services.
     */
    private void onGattSupport(GATTServices services) {
        if (mService.getConnectionState() == BluetoothService.State.CONNECTED) {
            initInformation(services);
        }
    }

    /**
     * <p>This method is called when this activity receives a
     * {@link com.qualcomm.gaiacontrol.services.GATTBLEService.GattMessage GattMessage} from the Service.</p>
     * <p>This method will act depending on the type of GATT message which had been broadcast to this activity.</p>
     *
     * @param gattMessage
     *          The GATT Message type.
     * @param content
     *          Any complementary information provided with the GATT Message.
     */
    @SuppressLint("SwitchIntDef")
    private void onReceiveGattMessage(@GATTBLEService.GattMessage int gattMessage, Object content) {
        switch (gattMessage) {
            case GATTBLEService.GattMessage.RSSI_LEVEL:
                mRssi = (int) content;
                updatePathLoss(mRssi, mTXPowerLevel);
                updateRssiSignalDisplay(mRssi);
                break;
            case GATTBLEService.GattMessage.TX_POWER_LEVEL:
                mTXPowerLevel = (int) content;
                updatePathLoss(mRssi, mTXPowerLevel);
                break;
            case GATTBLEService.GattMessage.LINK_LOSS_ALERT_LEVEL:
                mLinkLossAlertLevel = (int) content;
                updateLinkLossAlertLevelButtons(mLinkLossAlertLevel);
                break;
        }
    }

    /**
     * <p>To update the path loss value by recalculating it.</p>
     * <p>This method will then call the {@link #checkThreshold(int) checkThreshold} method.</p>
     *
     * @param rssi
     *          The rssi value to use to update the path loss value.
     * @param txPowerLevel
     *          The TX Power level value to use to update the path loss value.
     */
    private void updatePathLoss(int rssi, int txPowerLevel) {
        mPathLoss = txPowerLevel - rssi;
        checkThreshold(mPathLoss);
    }

    /**
     * <p>To enable or disable the RSSI notifications:
     * <ul>
     *     <li>Activation: this method will first attempt the activate them through the GATTBLEService to have the
     *     most accurate value. However if this activation fails - see
     *     {@link GATTBLEService#startRssiUpdates(boolean) startRssiUpdates} for more information - this method will
     *     enable the notifications using the GAIA command
     *     {@link com.qualcomm.libraries.gaia.GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI} if available
     *     through the {@link ProximityGaiaManager ProximityGaiaManager}.</li>
     *     <li>Deactivation: this method will deactivate the notifications from the Service as well as from the
     *     GAIA manager.</li>
     * </ul></p>
     * <p></p>
     *
     * @param notify True to enable the RSSI notification, false to disable them.
     */
    private void getRSSINotifications(boolean notify) {
        if (notify && !mService.startRssiUpdates(true)) {
            // it is not possible to use the BLE way so we use GAIA
            mGaiaManager.getRSSINotifications(true);
        }
        else if (!notify) {
            mService.startRssiUpdates(false);
            mGaiaManager.getRSSINotifications(false);
        }
    }

    /**
     * <p>To check if the path loss value is higher than the threshold.</p>
     * <p>If the path loss is higher this method will trigger the corresponding value of immediate alert level to
     * the device and will update the display.</p>
     *
     * @param pathLoss the value to check against the mild and high thresholds.
     */
    private void checkThreshold(int pathLoss) {
        if (mThresholdHigh < pathLoss && mImmediateAlert != GATT.AlertLevel.Levels.HIGH) {
            mImmediateAlert = GATT.AlertLevel.Levels.HIGH;
            mService.sendImmediateAlertLevel(mImmediateAlert);
            mTextViewRssiValue.setCompoundDrawableTintList(getColorStateList(R.color.immediate_alert_high));
        }
        else if (mThresholdMild < pathLoss && mImmediateAlert != GATT.AlertLevel.Levels.MILD) {
            mImmediateAlert = GATT.AlertLevel.Levels.MILD;
            mService.sendImmediateAlertLevel(mImmediateAlert);
            mTextViewRssiValue.setCompoundDrawableTintList(getColorStateList(R.color.immediate_alert_mild));
        }
        else if (mImmediateAlert != GATT.AlertLevel.Levels.NONE) {
            mImmediateAlert = GATT.AlertLevel.Levels.NONE;
            mService.sendImmediateAlertLevel(mImmediateAlert);
            mTextViewRssiValue.setCompoundDrawableTintList(getColorStateList(R.color.immediate_alert_none));
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
        this.getSupportActionBar().setLogo(R.drawable.ic_proximity_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        mLayoutLinkLossAlertLevel = findViewById(R.id.ll_link_loss_alert_levels);
        mLayoutProximityThreshold = findViewById(R.id.ll_proximity_threshold);
        mViewLinkLossAlertLevelUnavailable = findViewById(R.id.tv_link_loss_alert_level_unavailable);
        mViewProximityThresholdUnavailable = findViewById(R.id.tv_proximity_threshold_unavailable);
        mTextViewRssiValue = (TextView) findViewById(R.id.tv_signal_values);
        mMainLayout = findViewById(R.id.layout_proximity_main);
        mBarAlertDisconnection = (TextView) findViewById(R.id.tv_bar_alert_connection_state);

        // specific initialisations
        initLinkLossAlertLevel();
        initThresholdMild();
        initThresholdHigh();

        // RSSI display initialisation
        updateRssiSignalDisplay(mRssi);
    }

    /**
     * <p>To init the buttons for the user to select the alert levels for the Link Loss service.</p>
     */
    private void initLinkLossAlertLevel() {
        mLinkLossAlertLevelButtons[GATT.AlertLevel.Levels.NONE] = (Button) findViewById(R.id.bt_level_none);
        mLinkLossAlertLevelButtons[GATT.AlertLevel.Levels.MILD] = (Button) findViewById(R.id.bt_level_mild);
        mLinkLossAlertLevelButtons[GATT.AlertLevel.Levels.HIGH] = (Button) findViewById(R.id.bt_level_high);

        // initialise click listener
        for (int i = 0; i< mLinkLossAlertLevelButtons.length; i++) {
            final int selection = i;
            mLinkLossAlertLevelButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLinkLossAlertLevel = selection;
                    updateLinkLossAlertLevelButtons(selection);
                    mService.sendLinkLossAlertLevel(selection);
                }
            });
        }
    }

    /**
     * <p>To initialise the MILD threshold interaction components: minus and plus buttons and the edit text.</p>
     */
    private void initThresholdMild() {
        mEditTextThresholdMild = (EditText) findViewById(R.id.et_proximity_threshold_mild);
        Button minusThresholdMild = (Button) findViewById(R.id.bt_minus_threshold_mild);
        Button plusThresholdMild = (Button) findViewById(R.id.bt_plus_threshold_mild);

        setThresholdMild(mThresholdMild);
        mEditTextThresholdMild.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    setThresholdMild(Integer.getInteger(mEditTextThresholdMild.getText().toString()));
                    return true;
                }
                return false;
            }
        });

        minusThresholdMild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setThresholdMild(mThresholdMild -1);
            }
        });

        plusThresholdMild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setThresholdMild(mThresholdMild +1);
            }
        });
    }

    /**
     * <p>To initialise the HIGH threshold interaction components: minus and plus buttons and the edit text.</p>
     */
    private void initThresholdHigh() {
        mEditTextThresholdHigh = (EditText) findViewById(R.id.et_proximity_threshold_high);
        Button minusThresholdHigh = (Button) findViewById(R.id.bt_minus_threshold_high);
        Button plusThresholdHigh = (Button) findViewById(R.id.bt_plus_threshold_high);

        setThresholdHigh(mThresholdHigh);
        mEditTextThresholdHigh.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    setThresholdHigh(Integer.getInteger(mEditTextThresholdHigh.getText().toString()));
                    return true;
                }
                return false;
            }
        });

        minusThresholdHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setThresholdHigh(mThresholdHigh-1);
            }
        });

        plusThresholdHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setThresholdHigh(mThresholdHigh+1);
            }
        });
    }

    /**
     * <p>This method returns the image which corresponds to the given rssi signal. it will return the following:</p>
     * <ul>
     *     <li>For <code>rssi</code> between <code>-60</code> and <code>0</code> the method returns
     *     {@link R.drawable#ic_signal_level_4_128dp ic_signal_level_4_128dp}</li>
     *     <li>For <code>rssi</code> between <code>-70</code> and <code>-60</code> the method returns
     *     {@link R.drawable#ic_signal_level_3_128dp ic_signal_level_3_128dp}</li>
     *     <li>For <code>rssi</code> between <code>-80</code> and <code>-70</code> the method returns
     *     {@link R.drawable#ic_signal_level_2_128dp ic_signal_level_2_128dp}</li>
     *     <li>For <code>rssi</code> between <code>-90</code> and <code>-80</code> the method returns
     *     {@link R.drawable# ic_signal_level_1_128dp ic_signal_level_1_128dp}</li>
     *     <li>For <code>rssi</code> less than <code>-90</code> the method returns
     *     {@link R.drawable#ic_signal_level_0_128dp ic_signal_level_0_128dp}</li>
     *     <li>For all other values the method returns
     *     {@link R.drawable#ic_signal_unknown_128dp ic_signal_unknown_128dp}</li>
     * </ul>
     *
     * @param rssi
     *          The value for which we want to retrieve the corresponding image
     *
     * @return A drawable picture of a signal strength depending on the given rssi value.
     */
    private int getSignalIconFromRssi(int rssi) {
        if (-60 <= rssi && rssi < 0) {
            return  R.drawable.ic_signal_level_4_128dp;
        }
        else if (-70 <= rssi && rssi < -60) {
            return R.drawable.ic_signal_level_3_128dp;
        }
        else if (-80 <= rssi && rssi < -70) {
            return R.drawable.ic_signal_level_2_128dp;
        }
        else if (-90 <= rssi && rssi < -80) {
            return R.drawable.ic_signal_level_1_128dp;
        }
        else if (rssi < -90) {
            return R.drawable.ic_signal_level_0_128dp;
        }
        else {
            return R.drawable.ic_signal_unknown_128dp;
        }
    }

    /**
     * <p>To update the RSSI information which is displayed on the screen.</p>
     *
     * @param rssi
     *          The RSSI value to use to update the display.
     */
    private void updateRssiSignalDisplay(int rssi) {
        mTextViewRssiValue.setCompoundDrawablesWithIntrinsicBounds(0, getSignalIconFromRssi(rssi), 0, 0);
        String label = rssi >= 0 ? "RSSI level unknown" : "RSSI: " + rssi + "dbM";
        mTextViewRssiValue.setText(label);
    }

    /**
     * <p>To update the alert level buttons with the selected one.</p>
     *
     * @param selected the button which had been selected by the user.
     */
    private void updateLinkLossAlertLevelButtons(int selected) {
        for (int i = 0; i< mLinkLossAlertLevelButtons.length; i++) {
            mLinkLossAlertLevelButtons[i].setSelected(i == selected);
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

}
