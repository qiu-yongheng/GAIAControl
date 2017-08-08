/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the Remote Control Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 */
public class RemoteGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("unused")
    private final String TAG = "RemoteGaiaManager";
    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;


    // ====== ENUM =================================================================================

    /**
     * <p>This enumeration represents all the controls which can be used with the command
     * {@link GAIA#COMMAND_AV_REMOTE_CONTROL COMMAND_AV_REMOTE_CONTROL}.</p>
     */
    @IntDef(flag = true, value = { Controls.VOLUME_UP, Controls.VOLUME_DOWN, Controls.MUTE, Controls.PLAY,
            Controls.STOP, Controls.PAUSE, Controls.REWIND, Controls.FORWARD })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface Controls {
        /**
         * The AV remote control operation for volume up.
         */
        int VOLUME_UP = 0x41;
        /**
         * The AV remote control operation for volume down.
         */
        int VOLUME_DOWN = 0x42;
        /**
         * The AV remote control operation for mute.
         */
        int MUTE = 0x43;
        /**
         * The AV remote control operation for play.
         */
        int PLAY = 0x44;
        /**
         * The AV remote control operation for stop.
         */
        int STOP = 0x45;
        /**
         * The AV remote control operation for pause.
         */
        int PAUSE = 0x46;
        /**
         * The AV remote control operation for reward.
         */
        int REWIND = 0x4C;
        /**
         * The AV remote control operation for forward.
         */
        int FORWARD = 0x4B;
    }


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows initialisation of a listener to send messages to a device or dispatch
     * any received GAIA messages.</p>
     *
     * @param myListener
     *         An object which implements the {@link GaiaManagerListener MyGaiaManagerListener} interface.
     * @param transport
     *          The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public RemoteGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>To set up the audio sound over the GAIA command
     * {@link GAIA#COMMAND_AV_REMOTE_CONTROL COMMAND_AV_REMOTE_CONTROL} using the available
     * {@link Controls Controls}.</p>
     *
     * @param control
     *          The {@link Controls Control} to send.
     */
    public void sendControlCommand(@Controls int control) {
        final int PAYLOAD_LENGTH = 1;
        final int CONTROL_OFFSET = 0;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[CONTROL_OFFSET] = (byte) control;
        createRequest(createPacket(GAIA.COMMAND_AV_REMOTE_CONTROL, payload));
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case GAIA.COMMAND_AV_REMOTE_CONTROL:
                break;
        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
        mListener.onRemoteControlNotSupported();
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
         * <p>This method informs that the remote control feature has been considered as not supported by this
         * manager: a feature is considered as not supported by the device if the acknowledgement of the
         * request is not successful.</p>
         */
        void onRemoteControlNotSupported();
    }

}
