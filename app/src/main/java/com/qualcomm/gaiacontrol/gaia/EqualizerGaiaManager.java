/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the Equalizer Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 */
public class EqualizerGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in the debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * <p>To represent the boolean value <code>true</code> as a payload of one parameter for GAIA commands.</p>
     */
    private static final byte[] PAYLOAD_BOOLEAN_TRUE = { 0x01 };
    /**
     * <p>To represent the boolean value <code>false</code> as a payload of one parameter for GAIA commands.</p>
     */
    private static final byte[] PAYLOAD_BOOLEAN_FALSE = { 0x00 };
    /**
     * <p>The total number of presets for the equalizer.</p>
     * <p>According to the GAIA protocol and the specifications of the compatible devices, the presets are going from
     * <code>0</code> to <code>PRESETS_NUMBER-1</code>.</p>
     */
    public static final int NUMBER_OF_PRESETS = 7;
    /**
     * <p>The preset number which is customizable. For the chip implementation there is only one customizable
     * pre-set, the bank 1.</p>
     */
    public static final int CUSTOMIZABLE_PRESET = 1;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("unused")
    private final String TAG = "EqualizerGaiaManager";
    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;


    // ====== ENUM =================================================================================

    /**
     * <p>This enumeration regroups all the different controls which corresponds to the Equalizer feature.</p>
     */
    @IntDef(flag = true, value = { Controls.ENHANCEMENT_3D, Controls.BASS_BOOST, Controls.PRESETS })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface Controls {
        /**
         * <p>This application can control the 3D enhancement using the following commands:
         * <ul>
         *     <li>{@link GAIA#COMMAND_GET_3D_ENHANCEMENT_CONTROL}: to get the current activation state
         *     (enabled/disabled).</li>
         *     <li>{@link GAIA#COMMAND_SET_3D_ENHANCEMENT_CONTROL}: to set up the activation state.</li>
         * </ul></p>
         */
        int ENHANCEMENT_3D = 1;
        /**
         * <p>This application can control the Boost bass using the following commands:
         * <ul>
         *     <li>{@link GAIA#COMMAND_GET_BASS_BOOST_CONTROL}: to get the current activation state (enabled/disabled)
         *     .</li>
         *     <li>{@link GAIA#COMMAND_SET_BASS_BOOST_CONTROL}: to set up the activation state.</li>
         * </ul></p>
         */
        int BASS_BOOST = 2;
        /**
         * <p>This application can control the pre-set banks using the following commands:
         * <ul>
         *     <li>{@link GAIA#COMMAND_GET_USER_EQ_CONTROL}: to get the current activation state of the pre-sets
         *     (enabled/disabled)</li>
         *     <li>{@link GAIA#COMMAND_SET_USER_EQ_CONTROL}: to set up the activation state.</li>
         *     <li>{@link GAIA#COMMAND_GET_EQ_CONTROL}: to get the current pre-set.</li>
         *     <li>{@link GAIA#COMMAND_SET_EQ_CONTROL}: to set up the selected pre-set.</li>
         * </ul></p>
         */
        int PRESETS = 3;
    }


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
    public EqualizerGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>This method sets up the pre-set of the connected device using the
     * {@link GAIA#COMMAND_SET_EQ_CONTROL COMMAND_SET_EQ_CONTROL} command.</p>
     *
     * @param preset
     *          The preset to set up the device.
     */
    public void setPreset(int preset) {
        if (preset >= 0 && preset < NUMBER_OF_PRESETS) {
            final int PAYLOAD_LENGTH = 1;
            final int PRESET_OFFSET = 0;
            byte[] payload = new byte[PAYLOAD_LENGTH];
            payload[PRESET_OFFSET] = (byte) preset;
            createRequest(createPacket(GAIA.COMMAND_SET_EQ_CONTROL, payload));
        }
        else {
            Log.w(TAG, "setPreset used with parameter not between 0 and " + (NUMBER_OF_PRESETS-1) + ", value: " +
                    preset);
        }
    }

    /**
     * <p>This method requests the current pre-set of the connected device using the
     * {@link GAIA#COMMAND_GET_EQ_CONTROL COMMAND_GET_EQ_CONTROL} command.</p>
     */
    public void getPreset() {
        createRequest(createPacket(GAIA.COMMAND_GET_EQ_CONTROL));
    }

    /**
     * <p>This method requests the current activation state for the given control.</p>
     * <p>This method uses the following commands to perform this action:
     * <ul>
     *     <li>Command {@link GAIA#COMMAND_GET_BASS_BOOST_CONTROL COMMAND_GET_BASS_BOOST_CONTROL} for control
     *     {@link Controls#BASS_BOOST BASS_BOOST}.</li>
     *     <li>Command {@link GAIA#COMMAND_GET_3D_ENHANCEMENT_CONTROL COMMAND_GET_3D_ENHANCEMENT_CONTROL} for control
     *     {@link Controls#ENHANCEMENT_3D ENHANCEMENT_3D}.</li>
     *     <li>Command {@link GAIA#COMMAND_GET_USER_EQ_CONTROL COMMAND_GET_USER_EQ_CONTROL} for control
     *     {@link Controls#PRESETS PRESETS}.</li>
     * </ul></p>
     *
     * @param control
     *          The control for which to get the activation state from the remote device.
     */
    public void getActivationState(@Controls int control) {
        switch (control) {
            case Controls.BASS_BOOST:
                createRequest(createPacket(GAIA.COMMAND_GET_BASS_BOOST_CONTROL));
                break;
            case Controls.ENHANCEMENT_3D:
                createRequest(createPacket(GAIA.COMMAND_GET_3D_ENHANCEMENT_CONTROL));
                break;
            case Controls.PRESETS:
                createRequest(createPacket(GAIA.COMMAND_GET_USER_EQ_CONTROL));
                break;
        }
    }

    /**
     * <p>This method sets up the given activation state for the given control.</p>
     * <p>This method uses the following commands to perform this action:
     * <ul>
     *     <li>Command {@link GAIA#COMMAND_SET_BASS_BOOST_CONTROL COMMAND_SET_BASS_BOOST_CONTROL} for control
     *     {@link Controls#BASS_BOOST BASS_BOOST}.</li>
     *     <li>Command {@link GAIA#COMMAND_SET_3D_ENHANCEMENT_CONTROL COMMAND_SET_3D_ENHANCEMENT_CONTROL} for control
     *     {@link Controls#ENHANCEMENT_3D ENHANCEMENT_3D}.</li>
     *     <li>Command {@link GAIA#COMMAND_SET_USER_EQ_CONTROL COMMAND_SET_USER_EQ_CONTROL} for control
     *     {@link Controls#PRESETS PRESETS}.</li>
     * </ul></p>
     *
     * @param control
     *          The control to get the activation state from the remote device.
     * @param activate
     *          True to enable the control, false to disable it.
     */
    public void setActivationState(@Controls int control, boolean activate) {
        // we build the payload
        byte[] payload = activate ? PAYLOAD_BOOLEAN_TRUE : PAYLOAD_BOOLEAN_FALSE;

        // we do the request
        switch (control) {
            case Controls.BASS_BOOST:
                createRequest(createPacket(GAIA.COMMAND_SET_BASS_BOOST_CONTROL, payload));
                break;
            case Controls.ENHANCEMENT_3D:
                createRequest(createPacket(GAIA.COMMAND_SET_3D_ENHANCEMENT_CONTROL, payload));
                break;
            case Controls.PRESETS:
                createRequest(createPacket(GAIA.COMMAND_SET_USER_EQ_CONTROL, payload));
                break;
        }
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {

            case GAIA.COMMAND_GET_USER_EQ_CONTROL:
                receiveGetControlACK(Controls.PRESETS, packet);
                break;

            case GAIA.COMMAND_GET_EQ_CONTROL:
                receiveGetEQControlACK(packet);
                break;

            case GAIA.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
                receiveGetControlACK(Controls.ENHANCEMENT_3D, packet);
                break;

            case GAIA.COMMAND_GET_BASS_BOOST_CONTROL:
                receiveGetControlACK(Controls.BASS_BOOST, packet);
                break;

        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {

        switch (packet.getCommand()) {
            case GAIA.COMMAND_GET_USER_EQ_CONTROL:
            case GAIA.COMMAND_SET_USER_EQ_CONTROL:
            case GAIA.COMMAND_GET_EQ_CONTROL:
            case GAIA.COMMAND_SET_EQ_CONTROL:
                mListener.onControlNotSupported(Controls.PRESETS);
                break;
            case GAIA.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
            case GAIA.COMMAND_SET_3D_ENHANCEMENT_CONTROL:
                mListener.onControlNotSupported(Controls.ENHANCEMENT_3D);
                break;
            case GAIA.COMMAND_GET_BASS_BOOST_CONTROL:
            case GAIA.COMMAND_SET_BASS_BOOST_CONTROL:
                mListener.onControlNotSupported(Controls.BASS_BOOST);
                break;
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
     * <p>Called when this manager handles a packet with one of the following commands:
     * <ul>
     *     <li>{@link GAIA#COMMAND_GET_BASS_BOOST_CONTROL COMMAND_GET_BASS_BOOST_CONTROL}</li>
     *     <li>{@link GAIA#COMMAND_GET_3D_ENHANCEMENT_CONTROL COMMAND_GET_3D_ENHANCEMENT_CONTROL}</li>
     *     <li>{@link GAIA#COMMAND_GET_USER_EQ_CONTROL COMMAND_GET_USER_EQ_CONTROL}</li>
     * </ul></p>
     * <p>This method will check if the packet contains the expected parameter, will retrieve it and will dispatch
     * the information to the listener for the given control using
     * {@link GaiaManagerListener#onGetControlActivationState(int, boolean) onGetControlActivationState}.</p>
     *
     * @param packet
     *         The received packet.
     */
    private void receiveGetControlACK (@Controls int control, GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_OFFSET = 1;
        final int PAYLOAD_VALUE_LENGTH = 1;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // ACK status length is 1

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            boolean activate = payload[PAYLOAD_VALUE_OFFSET] == 0x01;
            mListener.onGetControlActivationState(control, activate);
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
            mListener.onGetPreset(preset);
        }
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
         * <p>This method is called when the device has sent its current pre-set through the acknowledgement of the
         * GAIA command {@link GAIA#COMMAND_GET_EQ_CONTROL COMMAND_GET_EQ_CONTROL}.</p>
         *
         * @param preset
         *              The current pre-set given by the device.
         */
        void onGetPreset(int preset);

        /**
         * <p>This method is called when the device has sent its current activation status - enabled or disabled -
         * through the acknowledgement of the GAIA command which corresponds to the given control:
         * <ul>
         *     <li>Command {@link GAIA#COMMAND_SET_BASS_BOOST_CONTROL COMMAND_SET_BASS_BOOST_CONTROL} for control
         *     {@link Controls#BASS_BOOST BASS_BOOST}.</li>
         *     <li>Command {@link GAIA#COMMAND_SET_3D_ENHANCEMENT_CONTROL COMMAND_SET_3D_ENHANCEMENT_CONTROL} for control
         *     {@link Controls#ENHANCEMENT_3D ENHANCEMENT_3D}.</li>
         *     <li>Command {@link GAIA#COMMAND_SET_USER_EQ_CONTROL COMMAND_SET_USER_EQ_CONTROL} for control
         *     {@link Controls#PRESETS PRESETS}.</li>
         * </ul></p>
         *
         * @param control
         *              The control for which the {@link EqualizerGaiaManager EqualizerGaiaManager} has received the
         *              current activation state.
         * @param activated
         *              True if the control is enabled, false otherwise.
         */
        void onGetControlActivationState(@Controls int control, boolean activated);

        /**
         * <p>This method informs that a command used by the given control had not received a successful
         * acknowledgement and is considered as not supported.</p>
         *
         * @param control
         *              The control which is considered as not supported.
         */
        void onControlNotSupported(@Controls int control);
    }

}
