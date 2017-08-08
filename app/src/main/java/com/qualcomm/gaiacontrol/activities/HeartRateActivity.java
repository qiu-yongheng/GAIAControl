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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.models.gatt.GATT;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.services.GATTBLEService;

/**
 * <p>This activity demonstrates the Heart Rate Profile over BLE.</p>
 *
 */
public class HeartRateActivity extends ServiceActivity {

    // ====== CONSTS FIELDS =======================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "HeartRateActivity";


    // ====== UI PRIVATE FIELDS =======================================================================

    /**
     * <p>The snack bar alert to display an error/alert message.</p>
     */
    private TextView mBarAlertDisconnection;

    /**
     * <p>The layout which contains all views related to the display of the heart rate profile.</p>
     */
    private View mMainLayout;

    /**
     * <p>The view which displays the unavailable message of the Heart Rate Measurement characteristic.</p>
     */
    private View mViewHeartRateMeasurementUnavailable;
    /**
     * <p>The view to display the heart rate value.</p>
     */
    private View mViewHeartRateValue;
    /**
     * <p>The view which contains all energy related views.</p>
     */
    private View mViewHeartRateEnergy;
    /**
     * <p>The view which contains all sensor contact related views.</p>
     */
    private View mViewHeartRateSensorContact;
    /**
     * <p>The view which contains all RR intervals related views.</p>
     */
    private View mViewHeartRateRRIntervals;
    /**
     * <p>The view to display the heart rate value.</p>
     */
    private TextView mTextViewHeartRateValue;
    /**
     * The view to display the energy value.
     */
    private TextView mTextViewEnergyValue;
    /**
     * The view to display the sensor contact state.
     */
    private TextView mTextViewSensorContactValue;
    /**
     * The gridlayout which can contain the RR intervals if there is more than one.
     */
    private GridLayout mGridLayoutRRIntervals;
    /**
     * The TextView to display the RR interval if only one had been provided by the device.
     */
    private TextView mTextViewHeartRateRRIntervalsOneValue;
    /**
     * <p>The view which displays the unavailable message of the Body Sensor Location characteristic.</p>
     */
    private View mViewBodySensorLocationUnavailable;
    /**
     * <p>The view which contains all body sensor location related views.</p>
     */
    private View mViewBodySensorLocation;
    /**
     * The TextView to display the location of the body sensor given by the device.
     */
    private TextView mTextViewBodySensorLocation;
    /**
     * * <p>The view which displays the unavailable message of the Heart Rate Control Point characteristic.</p>
     */
    private View mViewHeartRateControlPointUnavailable;
    /**
     * <p>The view which contains all energy expended reset related views.</p>
     */
    private View mViewResetEnergyExpended;
    /**
     * The Button the user can use to reset the expended energy.
     */
    private Button mButtonResetEnergyExpended;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
        this.init();
    }

    @Override // Activity
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            // stop the heart rate notifications from the device
            mService.requestHeartMeasurementNotifications(false);
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
                onConnectionStateChanged(connectionState);
                String stateLabel = connectionState == BluetoothService.State.CONNECTED ? "CONNECTED"
                        : connectionState == BluetoothService.State.CONNECTING ? "CONNECTING"
                        : connectionState == BluetoothService.State.DISCONNECTING ? "DISCONNECTING"
                        : connectionState == BluetoothService.State.DISCONNECTED ? "DISCONNECTED"
                        : "UNKNOWN";
                displayShortToast(getString(R.string.toast_device_information) + stateLabel);
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
                if (DEBUG) Log.d(TAG, handleMessage + "GAIA_PACKET");
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
        if (mService != null) {
            GATTServices services = mService.getGattSupport();
            setVisibleViews(services);
            if (services == null || !services.isGattProfileHeartRateSupported()) {
                displayLongToast(R.string.toast_heart_rate_not_supported);
            }
            else if (mService.getConnectionState() == BluetoothService.State.CONNECTED) {
                // start notifications & request any other information related to the profile
                mService.requestHeartMeasurementNotifications(true);
                mService.requestBodySensorLocation();
            }
        }
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {
        // stops any notification
        mService.requestHeartMeasurementNotifications(false);
    }


    // ====== PRIVATE METHODS =================================================================

    /**
     * <p>To initialise all views which are displayed.</p>
     * <p>This method will show the settings which are known as available.</p>
     *
     * @param services The GATT services which are supported by the device in order to know the available settings for
     * the heart rate profile.
     */
    private void setVisibleViews(GATTServices services) {
        showHeartRateMeasurement(services != null
                && services.gattServiceHeartRate.isHeartRateMeasurementCharacteristicAvailable());
        showBodySensorLocation(services != null
                && services.gattServiceHeartRate.isBodySensorLocationCharacteristicAvailable());
        showHeartRateControlPoint(services != null
                && services.gattServiceHeartRate.isHeartRateControlPointCharacteristicAvailable());
        // all displayed values are outdated
        displayDefaultValues();
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
            // UI
            displayDefaultValues(); // displayed values are outdated
            enableChildView(mMainLayout, false); // the user cannot interact with device
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
     * <p>This method enables all the UI components to let the user interact with them.</p>
     */
    private void onGattReady() {
        if (mService.getConnectionState() == BluetoothService.State.CONNECTED) {
            enableChildView(mMainLayout, true);
            // GATT channel just ready, the app does know yet if the reset button is supported.
            mButtonResetEnergyExpended.setEnabled(false);

            // GATT channel ready, the app can request needed information
            mService.requestHeartMeasurementNotifications(true);
            mService.requestBodySensorLocation();
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
            setVisibleViews(services);
        }
    }

    /**
     * <p>This method is called when this activity receives a
     * {@link GATTBLEService.GattMessage GattMessage} from the Service.</p>
     * <p>This method will act depending on the type of GATT message which has been broadcast to this activity.</p>
     *
     * @param gattMessage
     *          The GATT Message type.
     * @param content
     *          Any complementary information provided with the GATT Message.
     */
    @SuppressLint("SwitchIntDef")
    private void onReceiveGattMessage(@GATTBLEService.GattMessage int gattMessage, Object content) {
        switch (gattMessage) {
            case GATTBLEService.GattMessage.HEART_RATE_MEASUREMENT:
                GattServiceHeartRate.HeartRateMeasurementValues values =
                        (GattServiceHeartRate.HeartRateMeasurementValues) content;
                updateHeartRateDisplay(values);
                break;

            case GATTBLEService.GattMessage.BODY_SENSOR_LOCATION:
                int location = (int) content;
                updateBodySensorLocationDisplay(location);
                break;
        }
    }

    /**
     * <p>This method gets the label which corresponds to the given location as per Bluetooth specifications.</p>
     *
     * @param location
     *          the int value to get a label for.
     *
     * @return A String value which corresponds to the location name of the given parameter.
     */
    private String getBodySensorLocationLabel(int location) {
        switch (location) {
            case GATT.BodySensorLocation.Locations.CHEST:
                return getString(R.string.location_chest);
            case GATT.BodySensorLocation.Locations.EAR_LOBE:
                return getString(R.string.location_ear_lobe);
            case GATT.BodySensorLocation.Locations.FINGER:
                return getString(R.string.location_finger);
            case GATT.BodySensorLocation.Locations.FOOT:
                return getString(R.string.location_foot);
            case GATT.BodySensorLocation.Locations.HAND:
                return getString(R.string.location_hand);
            case GATT.BodySensorLocation.Locations.WRIST:
                return getString(R.string.location_wrist);
            default:
                return getString(R.string.location_other);
        }
    }

    /**
     * <p>To get a label which corresponds to the given status.</p>
     *
     * @param status
     *          the status to get a label for.
     *
     * @return A String label which corresponds to the given parameter. If the value does not correspond to a known
     * status, this return the label for status "not supported".
     */
    private String getSensorContactStatusLabel(int status) {
        if (status == GATT.HeartRateMeasurement.Flags.SensorStatus.SUPPORTED_WITH_CONTACT_DETECTED) {
            return getString(R.string.heart_rate_sensor_contact_status_in_contact);
        }
        else if (status == GATT.HeartRateMeasurement.Flags.SensorStatus.SUPPORTED_WITH_NO_CONTACT_DETECTED) {
            return getString(R.string.heart_rate_sensor_contact_status_no_contact);
        }
        else {
            return getString(R.string.heart_rate_sensor_contact_status_not_supported);
        }
    }


    // ====== UI METHODS ======================================================================

    /**
     * To initialise view objects used in this activity.
     */
    private void init() {
        // manage the action bar
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_heart_rate_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        mMainLayout = findViewById(R.id.layout_heart_rate_main);
        mBarAlertDisconnection = (TextView) findViewById(R.id.tv_bar_alert_connection_state);

        mViewHeartRateMeasurementUnavailable = findViewById(R.id.tv_heart_rate_measurement_unavailable);
        mViewHeartRateValue = findViewById(R.id.ll_heart_rate_measurement_value);
        mTextViewHeartRateValue = (TextView) findViewById(R.id.tv_heart_rate_value);
        mViewHeartRateEnergy = findViewById(R.id.ll_heart_rate_energy);
        mTextViewEnergyValue = (TextView) findViewById(R.id.tv_heart_rate_energy_value);
        mViewHeartRateSensorContact = findViewById(R.id.ll_heart_rate_sensor_contact);
        mTextViewSensorContactValue = (TextView) findViewById(R.id.tv_heart_rate_sensor_contact_status);
        mViewHeartRateRRIntervals = findViewById(R.id.ll_rr_intervals);
        mTextViewHeartRateRRIntervalsOneValue = (TextView) findViewById(R.id.tv_rr_interval_one_value);
        mGridLayoutRRIntervals = (GridLayout) findViewById(R.id.grid_rr_intervals);
        mViewBodySensorLocationUnavailable = findViewById(R.id.tv_body_sensor_location_unavailable);
        mViewBodySensorLocation = findViewById(R.id.ll_body_sensor_location);
        mTextViewBodySensorLocation = (TextView) findViewById(R.id.tv_body_sensor_location);
        mViewHeartRateControlPointUnavailable = findViewById(R.id.tv_heart_rate_control_point_unavailable);
        mViewResetEnergyExpended = findViewById(R.id.ll_reset_energy_expended);
        mButtonResetEnergyExpended = (Button) findViewById(R.id.bt_reset_energy_expended);

        // to init behaviour when user taps on button
        mButtonResetEnergyExpended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.sendHeartRateControlPoint(GATT.HeartRateControlPoint.Controls.RESET_ENERGY_EXPENDED);
                updateEnergyValueDisplay(GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE);
            }
        });
    }

    /**
     * <p>This method modifies all displayed values for the unknown default one. Any display of a value such as the
     * energy, interval, heart rate, etc. are displayed as "-". Any button is disabled.</p>
     */
    private void displayDefaultValues() {
        updateHeartRateValueDisplay(GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE);
        updateEnergyValueDisplay(GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE);
        updateSensorContactDisplay(GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE);
        updateRRIntervalsDisplay(null);
        mButtonResetEnergyExpended.setEnabled(false);
    }

    /**
     * <p>To update all displayed values corresponding to the Heart Rate characteristic with the given ones.</p>
     *
     * @param values the values to display
     */
    private void updateHeartRateDisplay(GattServiceHeartRate.HeartRateMeasurementValues values) {
        if (values != null) {
            // we update the views only if the corresponding value exists
            if (values.heartRateValue != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE) {
                updateHeartRateValueDisplay(values.heartRateValue);
            }
            if (values.energy != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE) {
                updateEnergyValueDisplay(values.energy);
            }
            if (values.flags.sensorContactStatus != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE) {
                updateSensorContactDisplay(values.flags.sensorContactStatus);
            }
            if (values.rrIntervals != null && values.rrIntervals.length > 0) {
                updateRRIntervalsDisplay(values.rrIntervals);
            }
            // the energy expended reset can only be used of the heart measurement provides the energy expended flag
            if (!mButtonResetEnergyExpended.isEnabled()) {
                mButtonResetEnergyExpended.setEnabled(values.flags.energyExpendedPresence
                        == GATT.HeartRateMeasurement.Flags.Presence.PRESENT);
            }
        }
        else {
            displayDefaultValues();
        }
    }

    /**
     * <p>To update the displayed heart rate value with the given one.</p>
     * <p>If the value corresponds to
     * {@link com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate.HeartRateMeasurementValues#NO_VALUE NO_VALUE}
     * the default unknown value is displayed:
     * {@link com.qualcomm.gaiacontrol.R.string#heart_rate_no_value heart_rate_no_value}.</p>
     *
     * @param value
     *          The value to display for the heart rate.
     */
    private void updateHeartRateValueDisplay(int value) {
        String message = value != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE ?
                value + "" : getString(R.string.heart_rate_no_value);
        mTextViewHeartRateValue.setText(message);
    }

    /**
     * <p>To update the displayed value for the energy expended with the given value.</p>
     * <p>If the value corresponds to
     * {@link com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate.HeartRateMeasurementValues#NO_VALUE NO_VALUE}
     * the default unknown value is displayed:
     * {@link com.qualcomm.gaiacontrol.R.string#heart_rate_no_value heart_rate_no_value}.</p>
     *
     * @param value
     *          The value to display for the expended energy.
     */
    private void updateEnergyValueDisplay(int value) {
        String message = value != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE ?
                value + "" : getString(R.string.heart_rate_no_value);
        mTextViewEnergyValue.setText(message);
    }

    /**
     * <p>To update the displayed value for the sensor contact status with the given value. This method gets the
     * corresponding label using {@link #getSensorContactStatusLabel(int) getSensorContactStatusLabel}.</p>
     * <p>If the value corresponds to
     * {@link com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate.HeartRateMeasurementValues#NO_VALUE NO_VALUE}
     * the default unknown value is displayed:
     * {@link com.qualcomm.gaiacontrol.R.string#heart_rate_no_value heart_rate_no_value}.</p>
     *
     * @param value
     *          The value to display for the sensor contact status.
     */
    private void updateSensorContactDisplay(int value) {
        String message = value != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE ?
                getSensorContactStatusLabel(value) : getString(R.string.heart_rate_no_value);
        mTextViewSensorContactValue.setText(message);

    }

    /**
     * <p>To update the displayed value(s) for the RR interval(s) with the given value(s).</p>
     * <p>If there is only one value in the array, this method displays a text view to display the value. If there
     * is more than one value, this method displays a grid layout to display all values.</p>
     * <p>If there is no value the default unknown value is displayed:
     * {@link com.qualcomm.gaiacontrol.R.string#heart_rate_no_value heart_rate_no_value}.</p>
     * <p></p>
     *
     * @param intervals
     *          The value to display for the sensor contact status.
     */
    private void updateRRIntervalsDisplay(int[] intervals) {
        // if there is one more values, the method displays the values in a grid layout
        if (intervals != null && intervals.length > 1) {
            mGridLayoutRRIntervals.removeAllViews(); // remove all previous displayed values
            // show the grid layout and hide the text view
            mGridLayoutRRIntervals.setVisibility(View.VISIBLE);
            mTextViewHeartRateRRIntervalsOneValue.setVisibility(View.GONE);
            // add an interval view for each value
            //noinspection ForLoopReplaceableByForEach // more understandable this way
            for (int i=0; i<intervals.length; i++) {
                addInterval(intervals[i] + "");
            }
        }
        else {
            // hide the grid layout and show the text view
            mGridLayoutRRIntervals.setVisibility(View.GONE);
            mTextViewHeartRateRRIntervalsOneValue.setVisibility(View.VISIBLE);
            String text;
            // if there is one value, gets the value, otherwise gets the default unknown value
            if (intervals != null && intervals.length == 1) {
                text = intervals[0] + "";
            }
            else {
                text = getString(R.string.heart_rate_no_value);
            }

            // displays the value
            mTextViewHeartRateRRIntervalsOneValue.setText(text);
        }
    }

    /**
     * <p>This methods adds a new child view containing the interval value in the grid layout which displays the
     * interval values.</p>
     *
     * @param value
     *          The interval value to display.
     */
    private void addInterval(String value) {
        float weight = 1; // all layouts in the grid have a weight of 1
        View view = View.inflate(this, R.layout.layout_rr_interval, null);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, weight);
        int margin = (int) getResources().getDimension(R.dimen.padding_settings_horizontal);
        param.setMargins(margin, margin, margin, margin);
        mGridLayoutRRIntervals.addView(view, param);
        ((TextView) (view.findViewById(R.id.tv_rr_interval_value))).setText(value);
    }

    /**
     * <p>To show or hide the information views about the Heart Rate Measurement characteristic.</p>
     *
     * @param show
     *          true to show the information, false to hide the views.
     *
     */
    private void showHeartRateMeasurement(boolean show) {
        if (show) {
            mViewHeartRateValue.setVisibility(View.VISIBLE);
            mViewHeartRateEnergy.setVisibility(View.VISIBLE);
            mViewHeartRateSensorContact.setVisibility(View.VISIBLE);
            mViewHeartRateRRIntervals.setVisibility(View.VISIBLE);
            mViewHeartRateMeasurementUnavailable.setVisibility(View.GONE);
        }
        else {
            mViewHeartRateValue.setVisibility(View.GONE);
            mViewHeartRateEnergy.setVisibility(View.GONE);
            mViewHeartRateSensorContact.setVisibility(View.GONE);
            mViewHeartRateRRIntervals.setVisibility(View.GONE);
            mViewHeartRateMeasurementUnavailable.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p>To show or hide the information views about the Body Sensor Location characteristic.</p>
     *
     * @param show
     *          true to show the information, false to hide the views.
     */
    private void showBodySensorLocation(boolean show) {
        if (show) {
            mViewBodySensorLocation.setVisibility(View.VISIBLE);
            mViewBodySensorLocationUnavailable.setVisibility(View.GONE);
        }
        else {
            mViewBodySensorLocation.setVisibility(View.GONE);
            mViewBodySensorLocationUnavailable.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p>To update the displayed value for the Body Sensor Location.</p>
     *
     * @param location
     *          The location to display.
     */
    private void updateBodySensorLocationDisplay(int location) {
        String text = location != GattServiceHeartRate.HeartRateMeasurementValues.NO_VALUE ?
                getBodySensorLocationLabel(location) : getString(R.string.heart_rate_no_value);
        mTextViewBodySensorLocation.setText(text);
    }

    /**
     * <p>To show or hide the information views about the Heart Rate Control Point characteristic.</p>
     *
     * @param show
     *          true to show the information, false to hide the views.
     */
    private void showHeartRateControlPoint(boolean show) {
        if (show) {
            mViewResetEnergyExpended.setVisibility(View.VISIBLE);
            mViewHeartRateControlPointUnavailable.setVisibility(View.GONE);
        }
        else {
            mViewResetEnergyExpended.setVisibility(View.GONE);
            mViewHeartRateControlPointUnavailable.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p>This method allows the given view and all its child components to enabled or disabled. The given view is
     * only disabled if it is not a ViewGroup.</p>
     *
     * @param view
     *          The view which all child components should be deactivated.
     * @param enabled
     *          True to enable the views, false to disable it.
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