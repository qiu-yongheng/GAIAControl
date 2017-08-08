/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.Utils;
import com.qualcomm.gaiacontrol.models.equalizer.parameters.Filter;
import com.qualcomm.gaiacontrol.models.equalizer.parameters.ParameterType;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the Custom Equalizer Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 */
public class CustomEqualizerGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in the debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("unused")
    private final String TAG = "CustomEQGaiaManager";
    /**
     * The value for the master gain parameter in the parameter ID from the payload in a GAIA packet for the
     * EQ_PARAMETER commands.
     */
    public static final int GENERAL_BAND = 0x00;
    /**
     * The value for the master gain parameter in the parameter ID from the payload in a GAIA packet for the
     * EQ_PARAMETER commands.
     */
    public static final int PARAMETER_MASTER_GAIN = 0x01;
    /**
     * The first byte of the EQ PARAMETER command is always 0x01 as the only bank which is customizable is 1.
     */
    private static final int EQ_PARAMETER_FIRST_BYTE = 0x01 ;
    /**
     * The length of the payload from the received GAIA packet when the EQ PARAMETER configuration is requested.
     */
    private static final int GET_EQ_PARAMETER_PAYLOAD_LENGTH = 5;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;
    /**
     * <p>To know if the board has to recalculate the bank: this has to be done if the current bank is the custom one
     * which is always the bank 1 for this application.</p>
     * <p>This boolean will determine the value of the last byte for the
     * {@link GAIA#COMMAND_SET_EQ_PARAMETER COMMAND_SET_EQ_PARAMETER} command: <code>0x00</code> for no recalculation -
     * bank 1 not selected - <code>0x01</code> otherwise.</p>
     */
    private boolean isBank1Selected = false;


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows to initialise a listener to send messages to a device or dispatch
     * any received GAIA messages.</p>
     *
     * @param myListener
     *         An object which implements the {@link GaiaManagerListener MyGaiaManagerListener} interface.
     * @param transport
     *          The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public CustomEqualizerGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * T<p>This method requests the current value of the master gain for the custom bank, the bank 1.</p>
     */
    public void getMasterGain() {
        getEQParameter(GENERAL_BAND, PARAMETER_MASTER_GAIN);
    }

    /**
     * <p>This method requests the current value of the given parameter for the given band using the following
     * command: {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER}.</p>
     *
     * @param band
     *            The band for which the current value is requested: <code>0</code> for a general parameter of the
     *            bank, <code>1</code> to <code>5</code> for a specific band.
     * @param parameter
     *            The parameter to get the current value for.
     */
    public void getEQParameter(int band, int parameter) {
        final int PAYLOAD_LENGTH = 2;
        final int ID_PARAMETER_HIGH_OFFSET = 0;
        final int ID_PARAMETER_LOW_OFFSET = 1;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[ID_PARAMETER_HIGH_OFFSET] = EQ_PARAMETER_FIRST_BYTE;
        payload[ID_PARAMETER_LOW_OFFSET] = (byte) buildParameterIDLowByte(band, parameter);
        createRequest(createPacket(GAIA.COMMAND_GET_EQ_PARAMETER, payload));
//        sendGaiaPacket(Gaia.COMMAND_GET_EQ_PARAMETER, EQ_PARAMETER_FIRST_BYTE, value);
    }

    /**
     * <p>This method sets up a new value for the given parameter and given band into the connected device. This
     * method uses the command {@link GAIA#COMMAND_SET_EQ_PARAMETER COMMAND_SET_EQ_PARAMETER} to perform this
     * action.</p>
     *
     * @param band
     *            The band for which to set up one of the parameter values: <code>0</code> for a general parameter of the
     *            bank, <code>1</code> to <code>5</code> for a specific band.
     * @param parameter
     *            the parameter to set up the given value for.
     * @param value
     *            the new value for the band parameter.
     */
    public void setEQParameter(int band, int parameter, int value) {
        final int PAYLOAD_LENGTH = 5;
        final int ID_PARAMETER_HIGH_OFFSET = 0;
        final int ID_PARAMETER_LOW_OFFSET = 1;
        final int VALUE_OFFSET = 2;
        final int VALUE_LENGTH = 2;
        final int RECALCULATION_OFFSET = 4;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[ID_PARAMETER_HIGH_OFFSET] = EQ_PARAMETER_FIRST_BYTE;
        payload[ID_PARAMETER_LOW_OFFSET] = (byte) buildParameterIDLowByte(band, parameter);
        Utils.copyIntIntoByteArray(value, payload, VALUE_OFFSET, VALUE_LENGTH, false);
        payload[RECALCULATION_OFFSET] = isBank1Selected ? (byte) 0x01 : (byte) 0x00; // recalculating in live - only
        // if the custom pre-set is used.
        createRequest(createPacket(GAIA.COMMAND_SET_EQ_PARAMETER,payload));
    }

    /**
     * <p>This method requests the current pre-set of the connected device using the
     * {@link GAIA#COMMAND_GET_EQ_CONTROL COMMAND_GET_EQ_CONTROL} command.</p>
     */
    public void getPreset() {
        createRequest(createPacket(GAIA.COMMAND_GET_EQ_CONTROL));
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case GAIA.COMMAND_GET_EQ_PARAMETER:
                receiveGetEQParameterACK(packet);
                break;
            case GAIA.COMMAND_GET_EQ_CONTROL:
                receiveGetEQControlACK(packet);
                break;
        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
        if (packet.getStatus() == GAIA.Status.INCORRECT_STATE) {
            mListener.onIncorrectState();
        }
        else {
            mListener.onControlNotSupported();
        }
    }

    @Override // extends GaiaManager
    protected void hasNotReceivedAcknowledgementPacket(GaiaPacket packet) {
    }

    @Override // extends GaiaManager
    protected boolean manageReceivedPacket(GaiaPacket packet) {
        return false;
    }

    @Override // extends GaiaManager
    protected boolean sendGAIAPacket(byte[] packet) {
        return mListener.sendGAIAPacket(packet);
    }


    // ====== PRIVATE METHODS - RECEIVING =============================================================

    /**
     * <p>Called when this manager handles a packet with the command
     * {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER}</p>
     * <p>This method will check if the packet contains the expected parameters, will get them and will dispatch
     * the information to the listener for the given parameters using one of the corresponding methods:
     * {@link GaiaManagerListener#onGetGain(int, int) onGetGain},
     * {@link GaiaManagerListener#onGetQuality(int, int) onGetQuality},
     * {@link GaiaManagerListener#onGetFilter(int, Filter) onGetFilter},
     * {@link GaiaManagerListener#onGetFrequency(int, int) onGetFrequency} and
     * {@link GaiaManagerListener#onGetMasterGain(int) onGetMasterGain}.</p>
     * <p>This method expects to have the following information from the received packet:
     * <ul>
     *     <li><code>payload[0]</code>: packet status</li>
     *     <li><code>payload[1]</code>: parameter ID, high byte</li>
     *     <li><code>payload[2]</code>: parameter ID, low byte</li>
     *     <li><code>payload[3]</code>: value, high byte</li>
     *     <li><code>payload[4]</code>: value, low byte</li>
     * </ul></p>
     *
     * @param packet
     *            The received packet.
     */
    private void receiveGetEQParameterACK(GaiaPacket packet) {

        byte[] payload = packet.getPayload();
        if (DEBUG) {
            Log.d(TAG, "EQ PARAM, payload: " + Utils.getStringFromBytes(payload));
        }

        final int OFFSET_PARAMETER_ID_LOW_BYTE = 2;
        final int VALUE_OFFSET = 3;
        final int VALUE_LENGTH = 2;

        // checking if there are enough arguments in the payload
        if (payload.length < GET_EQ_PARAMETER_PAYLOAD_LENGTH) {
            Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with missing arguments.");
            return;
        }

        // getting the different arguments from the received packet.
        // payload[OFFSET_PARAMETER_ID_LOW_BYTE] = OxXXXXYYYY with XXXX represents the band and YYYY the param
        int band = (payload[OFFSET_PARAMETER_ID_LOW_BYTE] & 0xF0) >>> Utils.BITS_IN_HEXADECIMAL;
        int param = payload[OFFSET_PARAMETER_ID_LOW_BYTE] & 0xF;

        // master gain for the bank
        if (band == GENERAL_BAND && param == PARAMETER_MASTER_GAIN) {
            int masterGainValue = Utils.extractShortFromByteArray(payload, VALUE_OFFSET, VALUE_LENGTH, false);
            mListener.onGetMasterGain(masterGainValue);
            if (DEBUG)
                Log.d(TAG, "MASTER GAIN - value: " + masterGainValue);
        }
        else {
            ParameterType parameterType = ParameterType.valueOf(param);

            // checking of the parameter type is defined for this application.
            //noinspection ConstantConditions > parameterType can be null
            if (parameterType == null) {
                Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with an unknown parameter type: " + param);
                return;
            }

            // acting depending on the parameter type for the received band
            switch (parameterType) {
            case FILTER:
                int filterValue = Utils.extractIntFromByteArray(payload, VALUE_OFFSET, VALUE_LENGTH, false);
                Filter filter = Filter.valueOf(filterValue);
                //noinspection ConstantConditions > filter can be null
                if (filter == null) {
                    Log.w(TAG, "Received \"COMMAND_GET_EQ_PARAMETER\" packet with an unknown filter type: "
                                + filterValue);
                    return;
                }
                mListener.onGetFilter(band, filter);
                if (DEBUG) {
                    Log.d(TAG, "BAND: " + band
                            + " - PARAM: " + parameterType.toString()
                            + " - FILTER: " + filter.toString());
                }
                break;

            case FREQUENCY:
                int frequencyValue = Utils.extractIntFromByteArray(payload, VALUE_OFFSET, VALUE_LENGTH, false);
                mListener.onGetFrequency(band, frequencyValue);
                if (DEBUG) {
                    Log.d(TAG, "BAND: " + band
                                    + " - PARAM: " + parameterType.toString()
                                    + " - FREQUENCY: " + frequencyValue);
                }
                break;

            case GAIN:
                int gainValue = Utils.extractIntFromByteArray(payload, VALUE_OFFSET, VALUE_LENGTH, false);
                mListener.onGetGain(band, gainValue);
                if (DEBUG) {
                    Log.d(TAG, "BAND: " + band
                            + " - PARAM: " + parameterType.toString()
                            + " - GAIN: " + gainValue);
                }
                break;

            case QUALITY:
                int qualityValue = Utils.extractIntFromByteArray(payload, VALUE_OFFSET, VALUE_LENGTH, false);
                mListener.onGetQuality(band, qualityValue);
                if (DEBUG) {
                    Log.d(TAG,
                            "BAND: " + band
                                    + " - PARAM: " + parameterType.toString()
                                    + " - QUALITY: " + qualityValue);
                }
                break;
            }
        }
    }

    /**
     * <p>Called when this manager handles a packet with the command
     * {@link GAIA#COMMAND_GET_EQ_CONTROL COMMAND_GET_EQ_CONTROL}.</p>
     * <p>This method will check if the packet contains the expected parameters, will retrieve them and will dispatch
     * the information to the listener.</p>
     *
     * @param packet
     *         The received packet with the command COMMAND_GET_EQ_CONTROL.
     */
    private void receiveGetEQControlACK (GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_OFFSET = 1;
        final int PAYLOAD_VALUE_LENGTH = 1;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // ACK status length is 1

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            int preset = payload[PAYLOAD_VALUE_OFFSET];
            isBank1Selected = preset == EqualizerGaiaManager.CUSTOMIZABLE_PRESET;
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>To build the low byte of the Parameter ID for the GET and SET EQ_PARAMETER commands.</p> <p>The different
     * values for this parameter ID are:
     * <ul>
     *     <li>band 0 & parameter 0: requests the number of bands.</li>
     *     <li>band 0 & parameter 1: requests get the master gain for the bank.</li>
     *     <li>band 1-5 & parameter 0: requests the filter type for the specified band.</li>
     *     <li>band 1-5 & parameter 1: requests the frequency for the specified band.</li>
     *     <li>band 1-5 & parameter 2: requests the gain for the specified band.</li>
     *     <li>band 1-5 & parameter 3: requests the quality type for the specified band.</li>
     * </ul></p>
     *
     * @param band
     *            The band for which we want a value.
     * @param parameter
     *            The parameter for which we want the value.
     *
     * @return the parameter ID.
     */
    private int buildParameterIDLowByte(int band, int parameter) {
        return (band << 4) | parameter;
    }


    // ====== INTERFACES ===========================================================================

    /**
     * <p>This interface allows this manager to dispatch messages or events to a listener.</p>
     */
    public interface GaiaManagerListener {

        /**
         * <p>To send over a communication channel the bytes of a GAIA packet using the GAIA protocol.</p>
         *
         * @param packet
         *          The byte array to send to a device.
         * @return
         *          true if the sending could be done.
         */
        boolean sendGAIAPacket(byte[] packet);

        /**
         * <p>This method informs that a required command for the custom equalizer feature had not received a successful
         * acknowledgement and is considered as not supported.</p>
         */
        void onControlNotSupported();

        /**
         * <p>This method is called when this manager has successfully handled an acknowledgement GAIA packet for the
         * command {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} which gives the value for the master
         * gain of the configurable bank - the bank 1.</p>
         *
         * @param value
         *            The current master gain value.
         */
        void onGetMasterGain(int value);

        /**
         * <p>This method is called when this manager has successfully handled an acknowledgement GAIA packet for the
         * command {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} which gives the filter used for the
         * given band of the configurable bank - the bank 1.</p>
         *
         * @param band
         *            The band for which the filter value had been sent.
         * @param filter
         *            The current filter for the given band.
         */
        void onGetFilter(int band, Filter filter);

        /**
         * <p>This method is called when this manager has successfully handled an acknowledgement GAIA packet for the
         * command {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} which gives the frequency value for
         * the given band of the configurable bank - the bank 1.</p>
         *
         * @param band
         *            The band for which the frequency value had been sent.
         * @param value
         *            The current frequency value for the given band.
         */
        void onGetFrequency(int band, int value);

        /**
         * <p>This method is called when this manager has successfully handled an acknowledgement GAIA packet for the
         * command {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} which gives the gain value for
         * the given band of the configurable bank - the bank 1.</p>
         *
         * @param band
         *            The band for which the gain value had been sent.
         * @param value
         *            The current gain value for the given band.
         */
        void onGetGain(int band, int value);

        /**
         * <p>This method is called when this manager has successfully handled an acknowledgement GAIA packet for the
         * command {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} which gives the quality value for
         * the given band of the configurable bank - the bank 1.</p>
         *
         * @param band
         *            The band for which the quality value had been sent.
         * @param value
         *            The current quality value for the given band.
         */
        void onGetQuality(int band, int value);

        /**
         * <p>This method is called when this manager has handled an acknowledgement packet with the status
         * {@link com.qualcomm.libraries.gaia.GAIA.Status#INCORRECT_STATE INCORRECT_STATE} which, for the commands
         * {@link GAIA#COMMAND_GET_EQ_PARAMETER COMMAND_GET_EQ_PARAMETER} and
         * {@link GAIA#COMMAND_SET_EQ_PARAMETER COMMAND_SET_EQ_PARAMETER}, means the device has to play some audio to
         * let these commands be used.</p>
         */
        void onIncorrectState();
    }

}
