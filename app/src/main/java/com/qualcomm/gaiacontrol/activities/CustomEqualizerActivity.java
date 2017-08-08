/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.CustomEqualizerGaiaManager;
import com.qualcomm.gaiacontrol.models.equalizer.Band;
import com.qualcomm.gaiacontrol.models.equalizer.Bank;
import com.qualcomm.gaiacontrol.models.equalizer.parameters.Filter;
import com.qualcomm.gaiacontrol.models.equalizer.parameters.Parameter;
import com.qualcomm.gaiacontrol.models.equalizer.parameters.ParameterType;
import com.qualcomm.gaiacontrol.services.BluetoothService;
import com.qualcomm.gaiacontrol.ui.SliderLayout;
import com.qualcomm.libraries.gaia.GAIA;

/**
 * <p>This activity controls the custom equalizer for a device connected to the application.</p>
 */
public class CustomEqualizerActivity extends ServiceActivity implements SliderLayout.SliderListener,
        CustomEqualizerGaiaManager.GaiaManagerListener {

    // ====== CONSTS ===============================================================================

    /**
     * For debug mode, the tag to display for logs.
     */
    private static final String TAG = "CustomEqualizerActivity";


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * To keep the instance of the slider about the frequency to get and set the value.
     */
    private SliderLayout mSLFrequency;
    /**
     * To keep the instance of the slider about the gain to get and set the value.
     */
    private SliderLayout mSLGain;
    /**
     * To keep the instance of the slider about the quality to get and set the value.
     */
    private SliderLayout mSLQuality;
    /**
     * To keep the instance of the slider about the master gain to get and set the value.
     */
    private SliderLayout mSLMasterGain;
    /**
     * To keep instances of the band buttons to select and deselect them.
     */
    private final Button[] mBandButtons = new Button[6];
    /**
     * To keep instances of the filter buttons to select and deselect them.
     */
    private final Button[] mFilters = new Button[Filter.getSize()];
    /**
     * The layout to display while retrieving any information.
     */
    private View mProgressLayout;
    /**
     * The dialog to show a message to the user.
     */
    private AlertDialog mIncorrectStateDialog;
    /**
     * To know if the dialog to show a message to the user is already on screen.
     */
    private boolean mIsIncorrectStateDialogDisplayed = false;
    /**
     * All the values displayed to the user for the selected band.
     */
    private final Bank mBank = new Bank(5);
    /**
     * To manage the GAIA packets which had been received from the device and which will be sent to the device.
     */
    private CustomEqualizerGaiaManager mGaiaManager;


    // ====== ACTIVITY METHODS =======================================================================

    @Override // Activity from ServiceActivity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_equalizer);
        this.init();
    }

    @Override // Activity from ServiceActivity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_equaliser_menu, menu);
        return true;
    }

    @Override // Activity from ServiceActivity
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_equaliser:
                getInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mGaiaManager = new CustomEqualizerGaiaManager(this, transport);
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

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onControlNotSupported() {
        displayLongToast(R.string.customization_not_supported);
        finish();
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onGetMasterGain(int value) {
        mBank.getMasterGain().setValue(value);
        mSLMasterGain.setSliderPosition(mBank.getMasterGain().getPositionValue());
        mSLMasterGain.displayValue(mBank.getMasterGain().getLabelValue());

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onGetFilter(int band, Filter filter) {
        setFilter(band, filter, false);

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onGetFrequency(int band, int value) {
        refreshParameterValue(band, value, mBank.getBand(band).getFrequency(), mSLFrequency);

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onGetGain(int band, int value) {
        refreshParameterValue(band, value, mBank.getBand(band).getGain(), mSLGain);

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onGetQuality(int band, int value) {
        refreshParameterValue(band, value, mBank.getBand(band).getQuality(), mSLQuality);

        // in the case where all updates have been done
        if (mBank.getCurrentBand().isUpToDate()) {
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override // EqualizerGaiaManager.GaiaManagerListener
    public void onIncorrectState() {
        // dialog to ask for streaming
        // boolean to know if we should display the dialog
        if (!mIsIncorrectStateDialogDisplayed) {
            mIsIncorrectStateDialogDisplayed = true;
            mIncorrectStateDialog.show();
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

        mProgressLayout = findViewById(R.id.l_progress_bar);

        initSettingsComponents();
        initBandsComponents();
        initFiltersComponents();

        buildDialogs();
    }

    @Override // SliderLayout.SliderListener
    public void onProgressChangedByUser(int progress, int id) {
        Parameter parameter = null;
        SliderLayout sliderLayout = null;

        switch (id) {
        case R.id.sl_frequency:
            parameter = mBank.getCurrentBand().getFrequency();
            sliderLayout = mSLFrequency;
            break;
        case R.id.sl_gain:
            parameter = mBank.getCurrentBand().getGain();
            sliderLayout = mSLGain;
            break;
        case R.id.sl_master_gain:
            parameter = mBank.getMasterGain();
            sliderLayout = mSLMasterGain;
            break;
        case R.id.sl_quality:
            parameter = mBank.getCurrentBand().getQuality();
            sliderLayout = mSLQuality;
            break;
        }

        if (parameter != null && sliderLayout != null) {
            parameter.setValueFromProportion(progress);
            updateDisplayParameterValue(sliderLayout, parameter);
        }
    }

    @Override // SliderLayout.SliderListener
    public void onStopTrackingTouch(int progress, int id) {
        Parameter parameter = null;
        SliderLayout sliderLayout = null;

        switch (id) {
        case R.id.sl_frequency:
            parameter = mBank.getCurrentBand().getFrequency();
            sliderLayout = mSLFrequency;
            break;
        case R.id.sl_gain:
            parameter = mBank.getCurrentBand().getGain();
            sliderLayout = mSLGain;
            break;
        case R.id.sl_master_gain:
            parameter = mBank.getMasterGain();
            sliderLayout = mSLMasterGain;
            break;
        case R.id.sl_quality:
            parameter = mBank.getCurrentBand().getQuality();
            sliderLayout = mSLQuality;
            break;
        }

        if (parameter != null && sliderLayout != null) {
            parameter.setValueFromProportion(progress);
            updateDisplayParameterValue(sliderLayout, parameter);
            ParameterType parameterType = parameter.getParameterType();
            int parameterValue = (parameterType != null) ? parameterType.ordinal()
                    : CustomEqualizerGaiaManager.PARAMETER_MASTER_GAIN;
            int band = (parameterType != null) ? mBank.getNumberCurrentBand()
                    : CustomEqualizerGaiaManager.GENERAL_BAND;
            if (DEBUG) {
                Log.d(TAG, "Request SET eq parameter for band " + band + " and parameter " +
                        ((parameterType != null) ? parameterType.toString() : "MASTER GAIN"));
            }
            mGaiaManager.setEQParameter(band, parameterValue, parameter.getValue());
        }
    }

    /**
     * <p>This method is called when the user selects a band to parametrize.</p>
     * <p>This method will update the UI objects and will request the band parameters to the device through the GAIA
     * Manager.</p>
     *
     * @param band
     *            The band selected by the user.
     */
    private void selectBand(int band) {
        // deselect previous values on the UI
        mBandButtons[mBank.getNumberCurrentBand()].setSelected(false);
        mFilters[mBank.getCurrentBand().getFilter().ordinal()].setSelected(false);
        // select new values on the UI
        mBandButtons[band].setSelected(true);

        // define the new band
        mBank.setCurrentBand(band);

        // update the displayed values
        updateDisplayParameters();
        mBank.getBand(band).hasToBeUpdated();

        if (DEBUG) {
            Log.d(TAG, "Request GET eq parameter for band " + band + " and parameter "
                    + ParameterType.FILTER.toString());
        }
        mGaiaManager.getEQParameter(band, ParameterType.FILTER.ordinal());
    }

    /**
     * To initialise the setting UI components - mainly sliders.
     */
    private void initSettingsComponents() {
        mSLFrequency = (SliderLayout) findViewById(R.id.sl_frequency);
        mSLFrequency.initialize(getString(R.string.frequency_title),
                mBank.getCurrentBand().getFrequency().getLabelValue(), this);
        mSLGain = (SliderLayout) findViewById(R.id.sl_gain);
        mSLGain.initialize(getString(R.string.gain_title), mBank.getCurrentBand().getGain().getLabelValue(), this);
        mSLQuality = (SliderLayout) findViewById(R.id.sl_quality);
        mSLQuality.initialize(getString(R.string.quality_title), mBank.getCurrentBand().getQuality().getLabelValue(),
                this);
        mSLMasterGain = (SliderLayout) findViewById(R.id.sl_master_gain);
        mSLMasterGain.initialize("", mBank.getMasterGain().getLabelValue(), this);
        mSLMasterGain.setSliderBounds(mBank.getMasterGain().getBoundsLength(),
                mBank.getMasterGain().getLabelMinBound(),
                mBank.getMasterGain().getLabelMaxBound());
        mSLMasterGain.hideTitle();
    }

    /**
     * To initialise the filter UI components.
     */
    private void initFiltersComponents() {
        mFilters[Filter.BYPASS.ordinal()] = (Button) findViewById(R.id.bt_BYPASS);
        mFilters[Filter.BYPASS.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.BYPASS);
            }
        });

        mFilters[Filter.LOW_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_LPF1);
        mFilters[Filter.LOW_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_PASS_1);
            }
        });

        mFilters[Filter.HIGH_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_HPF1);
        mFilters[Filter.HIGH_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_PASS_1);
            }
        });

        mFilters[Filter.ALL_PASS_1.ordinal()] = (Button) findViewById(R.id.bt_APF1);
        mFilters[Filter.ALL_PASS_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.ALL_PASS_1);
            }
        });
        mFilters[Filter.LOW_SHELF_1.ordinal()] = (Button) findViewById(R.id.bt_LS1);
        mFilters[Filter.LOW_SHELF_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_SHELF_1);
            }
        });
        mFilters[Filter.HIGH_SHELF_1.ordinal()] = (Button) findViewById(R.id.bt_HS1);
        mFilters[Filter.HIGH_SHELF_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_SHELF_1);
            }
        });
        mFilters[Filter.TILT_1.ordinal()] = (Button) findViewById(R.id.bt_Tilt1);
        mFilters[Filter.TILT_1.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.TILT_1);
            }
        });
        mFilters[Filter.LOW_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_LPF2);
        mFilters[Filter.LOW_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_PASS_2);
            }
        });
        mFilters[Filter.HIGH_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_HPF2);
        mFilters[Filter.HIGH_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_PASS_2);
            }
        });
        mFilters[Filter.ALL_PASS_2.ordinal()] = (Button) findViewById(R.id.bt_APF2);
        mFilters[Filter.ALL_PASS_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.ALL_PASS_2);
            }
        });
        mFilters[Filter.LOW_SHELF_2.ordinal()] = (Button) findViewById(R.id.bt_LS2);
        mFilters[Filter.LOW_SHELF_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.LOW_SHELF_2);
            }
        });
        mFilters[Filter.HIGH_SHELF_2.ordinal()] = (Button) findViewById(R.id.bt_HS2);
        mFilters[Filter.HIGH_SHELF_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.HIGH_SHELF_2);
            }
        });
        mFilters[Filter.TILT_2.ordinal()] = (Button) findViewById(R.id.bt_Tilt2);
        mFilters[Filter.TILT_2.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.TILT_2);
            }
        });
        mFilters[Filter.PARAMETRIC_EQUALIZER.ordinal()] = (Button) findViewById(R.id.bt_PEQ);
        mFilters[Filter.PARAMETRIC_EQUALIZER.ordinal()].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFilterButtonClick(Filter.PARAMETRIC_EQUALIZER);
            }
        });
    }

    /**
     * To initialise the band UI components.
     */
    private void initBandsComponents() {
        mBandButtons[1] = (Button) findViewById(R.id.bt_band_1);
        mBandButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(1);
            }
        });
        mBandButtons[2] = (Button) findViewById(R.id.bt_band_2);
        mBandButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(2);
            }
        });
        mBandButtons[3] = (Button) findViewById(R.id.bt_band_3);
        mBandButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(3);
            }
        });
        mBandButtons[4] = (Button) findViewById(R.id.bt_band_4);
        mBandButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(4);
            }
        });
        mBandButtons[5] = (Button) findViewById(R.id.bt_band_5);
        mBandButtons[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBand(5);
            }
        });

        mBandButtons[mBank.getNumberCurrentBand()].setSelected(true);
    }

    /**
     * <p>This method is called when the user selects a new filter type for the current band.</p>
     *
     * @param filter
     *            The selected filter.
     */
    private void onFilterButtonClick(Filter filter) {
        // with the true parameter the setFilter method will request the values from the device in order to update
        // the other parameters
        setFilter(mBank.getNumberCurrentBand(), filter, true);
    }

    /**
     * <p>This method is called when the user selects a filter for the current band or when the device gets the value
     * of the filter for the corresponding band.</p>
     * <p>This method will first update all the UI components and objects with the given filter for the given band.</p>
     * <p>Then the method will request the parameter values which correspond to the new filter in order to update
     * the UI components and objects.</p>
     * <p>If this method is called when the user sets up a filter, this method also sends the new filter to the
     * device.</p>
     *
     * @param bandNumber
     *            the band for which a new filter value has been set up.
     * @param filter
     *            the new filter value.
     * @param fromUser
     *            to know if this setting is coming from a user action (true) or a value received from the board
     *            (false).
     */
    private void setFilter(int bandNumber, Filter filter, boolean fromUser) {
        boolean isCurrentBand = bandNumber == mBank.getNumberCurrentBand();

        // updating the UI if the band is the current one.
        if (isCurrentBand) {
            mFilters[mBank.getCurrentBand().getFilter().ordinal()].setSelected(false);
            mFilters[filter.ordinal()].setSelected(true);
        }

        // defining the filter for the specific band.
        Band band = mBank.getBand(bandNumber);
        band.setFilter(filter, fromUser);

        // if the information is coming from the user the values are not up to date anymore and we have to update the
        // board with the new value.
        if (fromUser) {
            band.hasToBeUpdated();
            if (DEBUG) {
                Log.d(TAG, "Request SET eq parameter for band " + bandNumber + " and parameter " +
                        ParameterType.FILTER.toString());
            }
            mGaiaManager.setEQParameter(bandNumber, ParameterType.FILTER.ordinal(), filter.ordinal());
        }

        // request the values for the different parameters.
        if (band.getFrequency().isConfigurable()) {
            if (DEBUG) {
                Log.d(TAG, "Request GET eq parameter for band " + bandNumber + " and parameter "
                        + ParameterType.FREQUENCY.toString());
            }
            mGaiaManager.getEQParameter(bandNumber, ParameterType.FREQUENCY.ordinal());
        }
        if (band.getGain().isConfigurable()) {
            if (DEBUG) {
                Log.d(TAG, "Request GET eq parameter for band " + bandNumber + " and parameter "
                        + ParameterType.GAIN.toString());
            }
            mGaiaManager.getEQParameter(bandNumber, ParameterType.GAIN.ordinal());
        }
        if (band.getQuality().isConfigurable()) {
            if (DEBUG) {
                Log.d(TAG, "Request GET eq parameter for band " + bandNumber + " and parameter "
                        + ParameterType.QUALITY.toString());
            }
            mGaiaManager.getEQParameter(bandNumber, ParameterType.QUALITY.ordinal());
        }

        if (isCurrentBand) {
            updateDisplayParameters();
            updateAllParametersBounds();
        }
    }

    /**
     * <p>This method is called when a filter has been selected in order to update all the bounds of the different
     * sliders: GAIN, FREQUENCY and QUALITY parameters.</p>
     */
    private void updateAllParametersBounds() {
        // update frequency
        updateParameterSliderBounds(mSLFrequency, mBank.getCurrentBand().getFrequency());
        // update gain
        updateParameterSliderBounds(mSLGain, mBank.getCurrentBand().getGain());
        // update quality
        updateParameterSliderBounds(mSLQuality, mBank.getCurrentBand().getQuality());
    }

    /**
     * To refresh the bounds values of the given slider with the corresponding parameter value.
     *
     * @param sliderLayout
     *            The slider layout which corresponds to the given parameter.
     * @param parameter
     *            The parameter for which the UI has to be refreshed.
     */
    private void updateParameterSliderBounds(SliderLayout sliderLayout, Parameter parameter) {
        sliderLayout.setEnabled(parameter.isConfigurable());
        sliderLayout.setSliderBounds(parameter.getBoundsLength(), parameter.getLabelMinBound(),
                parameter.getLabelMaxBound());
        sliderLayout.setSliderPosition(parameter.getValue());
    }

    /**
     * <p>This method updates the value of a parameter and also refreshes the UI if the given band is the one for which
     * the values are displayed.</p>
     *
     * @param band
     *            The band for which we received a new parameter value.
     * @param value
     *            The new parameter value for the given band.
     * @param parameter
     *            The parameter for which we received a new value.
     */
    private void refreshParameterValue(int band, int value, Parameter parameter, SliderLayout mSliderLayout) {
        parameter.setValue(value);

        if (band == mBank.getNumberCurrentBand()) {
            updateDisplayParameterValue(mSliderLayout, parameter);
        }
    }

    /**
     * <p>This method requests the update of the display of the gain, frequency and quality sliders UI.</p>
     */
    private void updateDisplayParameters() {
        updateDisplayParameterValue(mSLFrequency, mBank.getCurrentBand().getFrequency());
        updateDisplayParameterValue(mSLGain, mBank.getCurrentBand().getGain());
        updateDisplayParameterValue(mSLQuality, mBank.getCurrentBand().getQuality());
    }

    /**
     * <p>This method will update the slider value with the parameter value if the parameter is configurable. If the
     * parameter is not configurable, the slider is disabled.</p>
     *
     * @param sliderLayout
     *            The slider UI which has to be updated.
     * @param parameter
     *            The parameter values for the slider.
     */
    private void updateDisplayParameterValue(SliderLayout sliderLayout, Parameter parameter) {
        if (parameter.isConfigurable()) {
            sliderLayout.setEnabled(true);
            sliderLayout.setSliderPosition(parameter.getPositionValue());
            sliderLayout.displayValue(parameter.getLabelValue());
        } else {
            sliderLayout.setEnabled(false);
            sliderLayout.displayValue(parameter.getLabelValue());
        }
    }

    /**
     * To initialise alert dialogs to display with this activity.
     */
    private void buildDialogs() {
        // build the dialog to show a progress bar when we try to reconnect.
        AlertDialog.Builder incorrectStateDialogBuilder = new AlertDialog.Builder(CustomEqualizerActivity.this);
        incorrectStateDialogBuilder.setTitle(getString(R.string.dialog_incorrect_state_title));

        incorrectStateDialogBuilder.setMessage(getString(R.string.dialog_incorrect_state_message));
        incorrectStateDialogBuilder.setPositiveButton(getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIsIncorrectStateDialogDisplayed = false;
                        finish();
                    }
                });

        incorrectStateDialogBuilder.setCancelable(false);
        mIncorrectStateDialog = incorrectStateDialogBuilder.create();
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method requests all device information related to the custom equalizer feature in order to initialise
     * the displayed values with the device values.</p>
     */
    private void getInformation() {
        if (mService != null && mService.isGaiaReady()) {
            // disable all actions while we don't know the device state.
            mProgressLayout.setVisibility(View.VISIBLE);
            mBank.hasToBeUpdated();

            // we ask its state to the device.
            // master gain
            if (DEBUG) {
                Log.d(TAG, "Request GET eq parameter for MASTER GAIN");
            }
            mGaiaManager.getMasterGain();
            // filter
            int band = mBank.getNumberCurrentBand();
            if (DEBUG) {
                Log.d(TAG, "Request GET eq parameter for band " + band + " and parameter "
                        + ParameterType.FILTER.toString());
            }
            mGaiaManager.getEQParameter(band, ParameterType.FILTER.ordinal());
            // request the active pre-set for the manager to determine some GAIA packet parameters
            mGaiaManager.getPreset();
        }
    }

}
