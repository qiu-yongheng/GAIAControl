/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.models.gatt.GATT;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.gaiacontrol.models.gatt.GattServiceBattery;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.services.GATTBLEService;

import java.util.Locale;

/**
 * <p>This activity is the activity to demonstrate the Battery Service over BLE.</p>
 */
public class BatteryActivity extends ServiceActivity {

    // ====== CONSTS FIELDS =======================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "BatteryActivity";


    // ====== UI PRIVATE FIELDS =======================================================================

    /**
     * <p>The whole layout of this view.</p>
     */
    private View mMainLayout;
    /**
     * <p>The snack bar alert to display an error/alert message about the connection state.</p>
     */
    private TextView mBarAlertDisconnection;
    /**
     * The grid which contains all battery layouts - one per battery.
     */
    private GridLayout mBatteryGrid;
    /**
     * The message to display if the battery service is considered as unavailable.
     */
    private View mViewBatteryUnavailable;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
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
                onReceiveGattMessage(gattMessage, msg.obj);
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
            GATTServices support = mService.getGattSupport();
            initInformation(support);
            if (support == null || !support.isGattProfileFindMeSupported()) {
                displayLongToast(R.string.toast_battery_not_supported);
            }
            mService.requestBatteryLevels();
        }
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {

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
        if (support != null && support.isBatteryServiceSupported()) {
            // GATT Battery service is supported and can be used
            initBatteryDisplay(support.gattServiceBatteries);
            mViewBatteryUnavailable.setVisibility(View.GONE);
        }
        else {
            // the Battery service is not supported
            mBatteryGrid.removeAllViews();
            mViewBatteryUnavailable.setVisibility(View.VISIBLE);
        }
    }

    /**
     * <p>This method is called when the Service dispatches a CONNECTION_STATE_HAS_CHANGED message.</p>
     * <p>This method will disable all UI components when the device is known as not connected - disconnecting,
     * connecting or disconnected. It will also disable any ongoing notifications and will display a connection state
     * alert.</p>
     * <p>If the device is disconnected this method will request the GATTBLEService to reconnect to the device.</p>
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
     * <p>This method enables all the UI components to let the user interact with them.</p>
     */
    private void onGattReady() {
        if (mService.getConnectionState() == BluetoothService.State.CONNECTED) {
            enableChildView(mMainLayout, true);
            mService.requestBatteryLevels();
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
     * <p>This method will act depending on the type of GATT message which has been broadcast to this activity.</p>
     *
     * @param gattMessage
     *          The GATT Message type.
     * @param content
     *          Any complementary information provided with the GATT Message.
     */
    private void onReceiveGattMessage(@GATTBLEService.GattMessage int gattMessage, Object content) {
        if (gattMessage == GATTBLEService.GattMessage.BATTERY_LEVEL_UPDATE) {
            int instance = (int) content;
            updateBatteryLevels(instance);
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
        this.getSupportActionBar().setLogo(R.drawable.ic_battery_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        mMainLayout = findViewById(R.id.layout_battery_main);
        mBarAlertDisconnection = (TextView) findViewById(R.id.tv_bar_alert_connection_state);
        mBatteryGrid = (GridLayout) findViewById(R.id.grid_battery_levels);
        mViewBatteryUnavailable = findViewById(R.id.tv_battery_unavailable);
    }

    /**
     * <p>This method allows any of the child components of the given view to be enabled or disabled. The given view is
     * only disabled if it is not a ViewGroup.</p>
     *
     * @param view
     *          The view for which all child components should be deactivated.
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

    /**
     * <p>This method initialises the display of the battery depending on the Battery Service(s) provided - or not -
     * by the remote device.</p>
     * <p>For each Battery Service provided this method will add a battery layout in order to display the
     * information for this Battery Service.</p>
     *
     * @param services The provided Battery services map as known by the application through the GATTBLEService.
     */
    private void initBatteryDisplay(SimpleArrayMap<Integer, GattServiceBattery> services) {
        mBatteryGrid.removeAllViews();

        if (services == null ||services.size() == 0) {
            return;
        }

        // set up a new battery display for each battery service
        for (int i=0; i< services.size(); i++) {

            // add a new battery layout
            View view = addBatteryLayout();

            // set up the battery name to display
            GattServiceBattery service = services.get(services.keyAt(i));
            TextView nameDisplay = (TextView) view.findViewById(R.id.tv_battery_name);
            if (service.isPresentationFormatDescriptorAvailable()) {
                int description = service.getDescription();
                nameDisplay.setText(getBatteryDescription(description));
            }
            else {
                // we add 1 in order to start from 1 on the display
                nameDisplay.setText(getString(R.string.battery_name) + " " + (i+1));
            }
        }
    }

    /**
     * <p>This method updates the display of the battery level for the given instance.</p>
     *
     * @param instance The instance key which corresponds to the Service in the
     * {@link GATTServices#gattServiceBatteries gattServiceBatteries} ArrayMap.
     */
    private void updateBatteryLevels(int instance) {
        // get the corresponding Battery Service information
        GattServiceBattery service = mService.getGattSupport().gattServiceBatteries.get(instance);
        // get the index of the Battery Service to know its place in the grid
        int index = mService.getGattSupport().gattServiceBatteries.indexOfKey(instance);

        // values shouldn't be empty and should match the Grid Layout information
        if (service != null && index >= 0 && index < mBatteryGrid.getChildCount()) {
            // get the corresponding battery layout
            View view = mBatteryGrid.getChildAt(index);
            // get the data to use to update the display
            int level = service.getBatteryLevel();
            if (level >= 0 && level <= 100) {
                ((TextView) view.findViewById(R.id.tv_battery_level)).setText(String.format(Locale.getDefault(),
                        "%d %%", level));
                ((ImageView) view.findViewById(R.id.iv_battery_level)).setImageResource(getBatteryIconFromLevel(level));
            }
        }
    }

    /**
     * To display the battery level as an image depending on the level value.
     */
    private int getBatteryIconFromLevel(int level) {
        if (level <= 100 && level >= 0) {
            if (level > 80) {
                return R.drawable.ic_battery_level_4_128dp;
            } else if (level > 60) {
                return R.drawable.ic_battery_level_3_128dp;
            } else if (level > 40) {
                return R.drawable.ic_battery_level_2_128dp;
            } else if (level > 20) {
                return R.drawable.ic_battery_level_1_128dp;
            } else {
                return R.drawable.ic_battery_level_0_128dp;
            }
        } else {
            return R.drawable.ic_battery_level_unknown_128dp;
        }
    }

    /**
     * <p>To get the battery name depending on the
     * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.PresentationFormat.Description Description} value.</p>
     *
     * @param description The description to get a name for.
     *
     * @return The corresponding string of the description.
     */
    private String getBatteryDescription (int description) {
        switch (description) {
            case GATT.PresentationFormat.Description.INTERNAL:
                return getString(R.string.battery_description_internal);
            case GATT.PresentationFormat.Description.SECOND:
                return getString(R.string.battery_description_second);
            case GATT.PresentationFormat.Description.THIRD:
                return getString(R.string.battery_description_third);
            case GATT.PresentationFormat.Description.UNKNOWN:
            default:
                return getString(R.string.battery_description_unknown);
        }
    }

    /**
     * <p>To add a battery to the grid layout.</p>
     *
     * @return the view which has been added to the grid layout.
     */
    private View addBatteryLayout () {
        float weight = 1; // all layouts in the grid have a weight of 1
        View view = View.inflate(this, R.layout.layout_battery, null);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, weight);
        int margin = (int) getResources().getDimension(R.dimen.padding_settings_horizontal);
        param.setMargins(margin, margin, margin, margin);
        mBatteryGrid.addView(view, param);
        return view;
    }

}
