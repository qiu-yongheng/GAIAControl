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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.EqualizerGaiaManager;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.ui.PresetButton;
import com.qualcomm.libraries.gaia.GAIA;

import static com.qualcomm.gaiacontrol.gaia.EqualizerGaiaManager.CUSTOMIZABLE_PRESET;

/**
 * <p>This activity controls the equalizer for a device connected to the application.</p>
 */

public class EqualizerActivity extends ServiceActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, EqualizerGaiaManager.GaiaManagerListener {

    // ====== CONSTS ===============================================================================

    /**
     * For the debug mode, the tag to display for logs.
     */
    private static final String TAG = "EqualizerActivity";
    /**
     * To manage the GAIA packets which has been received from the device and which will be send to the device.
     */
    private EqualizerGaiaManager mGaiaManager;


    // ====== PRIVATE FIELDS ===============================================================================

    /**
     * The switch to customize the equalizer.
     */
    private Switch mSwitchPresets;
    /**
     * The switch to enable or disable the 3D.
     */
    private Switch mSwitch3D;
    /**
     * The switch to enable or disable the bass boost.
     */
    private Switch mSwitchBass;
    /**
     * All preset buttons.
     */
    private final PresetButton[] mPresets = new PresetButton[EqualizerGaiaManager.NUMBER_OF_PRESETS];
    /**
     * The button to start the configuration of Bank 1.
     */
    private Button mButtonConfigure;
    /**
     * The button selected by the user or by default.
     */
    private int mSelectedPreset = -1;


    // ====== PUBLIC METHODS ======================================================================

    @Override // View.OnClickListener
    public void onClick(View v) {
        int selectedPreset = ((PresetButton) v).getPreset();
        selectPreset(selectedPreset);
        mGaiaManager.setPreset(selectedPreset);
    }

    @Override // CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_presets:
                mGaiaManager.setActivationState(EqualizerGaiaManager.Controls.PRESETS, isChecked);
                if (isChecked) {
                    mGaiaManager.getPreset();
                }
                else {
                    activatePresets(false);
                }
                break;
            case R.id.sw_bass:
                mGaiaManager.setActivationState(EqualizerGaiaManager.Controls.BASS_BOOST, isChecked);
                break;
            case R.id.sw_3d:
                mGaiaManager.setActivationState(EqualizerGaiaManager.Controls.ENHANCEMENT_3D, isChecked);
                break;
        }
    }


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity from ServiceActivity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
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
        mGaiaManager = new EqualizerGaiaManager(this, transport);
        getInformation();
    }

    @Override // ServiceActivity
    protected void onServiceDisconnected() {

    }


    // ====== GAIA MANAGER METHODS =======================================================================


    @Override // EqualizerGaiaManager.GaiaManagerListener
    public boolean sendGAIAPacket(byte[] packet) {
        return mService!= null && mService.isGaiaReady()
                && mService.sendGAIAPacket(packet);
    }

    @Override// EqualizerGaiaManager.GaiaManagerListener
    public void onGetPreset(int preset) {
        if (mSwitchPresets.isChecked()) {
            activatePresets(true);
        }
        selectPreset(preset);
    }

    @Override// EqualizerGaiaManager.GaiaManagerListener
    public void onGetControlActivationState(@EqualizerGaiaManager.Controls int control, boolean activated) {
        switch (control) {
            case EqualizerGaiaManager.Controls.BASS_BOOST:
                refreshBassBoostDisplay(activated);
                break;
            case EqualizerGaiaManager.Controls.ENHANCEMENT_3D:
                refresh3DEnhancementDisplay(activated);
                break;
            case EqualizerGaiaManager.Controls.PRESETS:
                refreshPresetsDisplay(activated);
                break;
        }
    }

    @Override// EqualizerGaiaManager.GaiaManagerListener
    public void onControlNotSupported(@EqualizerGaiaManager.Controls int control) {
        switch (control) {
            case EqualizerGaiaManager.Controls.BASS_BOOST:
                mSwitchBass.setVisibility(View.GONE);
                findViewById(R.id.tv_info_bass_boost).setVisibility(View.VISIBLE);
                break;
            case EqualizerGaiaManager.Controls.ENHANCEMENT_3D:
                mSwitch3D.setVisibility(View.GONE);
                findViewById(R.id.tv_info_3D).setVisibility(View.VISIBLE);
                break;
            case EqualizerGaiaManager.Controls.PRESETS:
                activatePresets(false);
                mSwitchPresets.setVisibility(View.GONE);
                findViewById(R.id.tv_info_custom).setVisibility(View.VISIBLE);
                break;
        }
    }


    // ====== UI METHODS ======================================================================

    /**
     * To initialise objects used in this activity.
     */
    private void init() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.tb_menu));
        //noinspection ConstantConditions
        this.getSupportActionBar().setLogo(R.drawable.ic_equalizer_32dp);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp);

        PresetButton buttonClassic = (PresetButton) findViewById(R.id.bt_preset_6);
        buttonClassic.setOnClickListener(this);
        mPresets[buttonClassic.getPreset()] = buttonClassic;

        PresetButton buttonPop = (PresetButton) findViewById(R.id.bt_preset_5);
        buttonPop.setOnClickListener(this);
        mPresets[buttonPop.getPreset()] = buttonPop;

        PresetButton buttonRock = (PresetButton) findViewById(R.id.bt_preset_2);
        buttonRock.setOnClickListener(this);
        mPresets[buttonRock.getPreset()] = buttonRock;

        PresetButton buttonJazz = (PresetButton) findViewById(R.id.bt_preset_3);
        buttonJazz.setOnClickListener(this);
        mPresets[buttonJazz.getPreset()] = buttonJazz;

        PresetButton buttonFolk = (PresetButton) findViewById(R.id.bt_preset_4);
        buttonFolk.setOnClickListener(this);
        mPresets[buttonFolk.getPreset()] = buttonFolk;

        PresetButton buttonDefault = (PresetButton) findViewById(R.id.bt_preset_0);
        buttonDefault.setOnClickListener(this);
        mPresets[buttonDefault.getPreset()] = buttonDefault;

        PresetButton buttonCustom = (PresetButton) findViewById(R.id.bt_preset_1);
        buttonCustom.setOnClickListener(this);
        mPresets[CUSTOMIZABLE_PRESET] = buttonCustom;

        mSwitchPresets = (Switch) findViewById(R.id.sw_presets);
        mSwitchPresets.setOnCheckedChangeListener(this);
        mSwitch3D = (Switch) findViewById(R.id.sw_3d);
        mSwitch3D.setOnCheckedChangeListener(this);
        mSwitchBass = (Switch) findViewById(R.id.sw_bass);
        mSwitchBass.setOnCheckedChangeListener(this);

        mButtonConfigure = (Button) findViewById(R.id.bt_configure);
        mButtonConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCustomization();
            }
        });
    }

    /**
     * To select a preset for the UI.
     *
     * @param selected
     *              The preset to show as selected.
     */
    private void selectPreset(int selected) {
        if (mSelectedPreset >= 0 && mSelectedPreset < EqualizerGaiaManager.NUMBER_OF_PRESETS) {
            mPresets[mSelectedPreset].selectButton(false);
        }
        mPresets[selected].selectButton(true);
        mSelectedPreset = selected;
        mButtonConfigure.setEnabled(selected == CUSTOMIZABLE_PRESET);
    }

    /**
     * To hide or display the presets view depending on act on the customization switch.
     */
    private void activatePresets (boolean activate) {
        int visibility = activate ? View.VISIBLE : View.GONE;
        findViewById(R.id.tl_presets).setVisibility(visibility);
        mButtonConfigure.setVisibility(visibility);
        mButtonConfigure.setEnabled(mSelectedPreset == CUSTOMIZABLE_PRESET);
    }

    /**
     * <p>This method refreshes the display which corresponds the 3D enhancement control.</p>
     * <p>The method enables the corresponding switch and sets its check value to the given value.</p>
     *
     * @param activated
     *          True if the control is activated, false otherwise.
     */
    private void refresh3DEnhancementDisplay(boolean activated) {
        mSwitch3D.setEnabled(true);
        mSwitch3D.setChecked(activated);
    }

    /**
     * <p>This method refreshes the display which corresponds the Bass boost control.</p>
     * <p>The method enables the corresponding switch and sets its check value to the given value.</p>
     *
     * @param activated
     *          True if the control is activated, false otherwise.
     */
    private void refreshBassBoostDisplay(boolean activated) {
        mSwitchBass.setEnabled(true);
        mSwitchBass.setChecked(activated);
    }

    /**
     * <p>This method refreshes the display which corresponds the 3D enhancement control.</p>
     * <p>The method enables the corresponding switch and sets its check value to the given value. It will request
     * the pre-set to the manager if the control is activated. If the control is not activated, this method will
     * hide the presets display.</p>
     *
     * @param activated
     *          True if the control is activated, false otherwise.
     */
    private void refreshPresetsDisplay(boolean activated) {
        mSwitchPresets.setEnabled(true);
        mSwitchPresets.setChecked(activated);
        if (activated) {
            mGaiaManager.getPreset();
        }
        else {
            activatePresets(false);
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method requests all device information related to the TWS feature in order to initialise the displayed
     * values.</p>
     */
    private void getInformation() {
        if (mService != null && mService.isGaiaReady()) {
            // disable all actions while we don't know the device state.
            mSwitchPresets.setEnabled(false);
            mSwitch3D.setEnabled(false);
            mSwitchPresets.setEnabled(false);
            activatePresets(false);

            // we ask its state to the device.
            mGaiaManager.getActivationState(EqualizerGaiaManager.Controls.ENHANCEMENT_3D);
            mGaiaManager.getActivationState(EqualizerGaiaManager.Controls.BASS_BOOST);
            mGaiaManager.getActivationState(EqualizerGaiaManager.Controls.PRESETS);
        }
    }

    /**
     * <p>To start the view to allow the user to customize the bands.</p>
     */
    private void startCustomization() {
        Intent intentCustomEqualizer = new Intent(this, CustomEqualizerActivity.class);
        startActivity(intentCustomEqualizer);
    }

}
