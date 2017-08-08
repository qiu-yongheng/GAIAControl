/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.os.Handler;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the Proximity Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 * <p>The GAIA Notifications does not include any update about the RSSI level. In order to allow the display to be
 * updated this manager creates its own notifications by running the corresponding GET request every
 * {@link #RSSI_DELAY_TIME RSSI_DELAY_TIME}. These notifications are called "custom notifications" in this class.</p>
 */
public class ProximityGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * The time to wait before checking the state for RSSI level.
     */
    private static final int RSSI_DELAY_TIME = Consts.DELAY_TIME_FOR_RSSI;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * The handler to run some tasks.
     */
    private final Handler mHandler = new Handler();
    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("unused")
    private final String TAG = "ProximityGaiaManager";
    /**
     * <p>The listener which implements the ProximityGaiaManagerListener interface to allow this manager to communicate
     * with a device.</p>
     */
    private final ProximityGaiaManagerListener mListener;
    /**
     * To start a task to get the RSSI value from the device.
     */
    private final Runnable mRunnableRSSI = new Runnable() {
        @Override
        public void run() {
            // we check that the RSSI notification is still activated
            if (mUpdateRssi) {
                getRSSIInformation();
            }
        }
    };
    /**
     * <p>To know if updates of the RSSI level have been requested.</p>
     * <p>While this is true the process of updating the RSSI level is running.</p>
     */
    private boolean mUpdateRssi = false;
    /**
     * <p>To know if the RSSI GAIA command request had been sent to the device and this manager is waiting for the
     * answer.</p>
     */
    private boolean mPendingRSSICustomNotification = false;


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows initialisation of a listener to send messages to a device or dispatch
     * any received GAIA messages.</p>
     *
     * @param myListener
     *         An object which implements the {@link ProximityGaiaManagerListener MyGaiaManagerListener} interface.
     * @param transport
     *          The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public ProximityGaiaManager(ProximityGaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>This method starts the notifications related to the RSSI or stops them.</p>
     * <p>The application is only interested on regularly being informed of the RSSI level.</p>
     * <p>Because there is no GAIA notification event available for the RSSI level, this manager creates its own
     * custom notification as follows:
     * <ul>
     *     <li>The manager requests the current RSSI level from the device using the corresponding GAIA command.</li>
     *     <li>Once it receives the level value, it requests it again through a delayed Runnable. The delay time is
     *     defined by {@link #RSSI_DELAY_TIME RSSI_DELAY_TIME}.</li>
     * </ul>
     *
     * @param notify
     *          True to start the notifications, false to cancel them.
     *
     */
    public void getRSSINotifications(boolean notify) {
        // there is no existing notification for battery level so we request the battery level through a Runnable
            if (notify && !mUpdateRssi) {
                mUpdateRssi = true;
                getRSSIInformation();
            } else if (!notify && mUpdateRssi) {
                mUpdateRssi = false;
                mPendingRSSICustomNotification = false;
                mHandler.removeCallbacks(mRunnableRSSI);
            }
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        if (packet.getCommand() == GAIA.COMMAND_GET_CURRENT_RSSI) {
            receivePacketGetCurrentRSSIACK(packet);
        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
        if (packet.getCommand() == GAIA.COMMAND_GET_CURRENT_RSSI) {
            mListener.onRSSINotSupported();
            mUpdateRssi = false;
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
     * {@link GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI}.</p>
     * <p>This method checks if the packet contains the expected parameters, retrieves them and send them to
     * the listener.</p>
     * <p>If the received packet was expected as a "waited notification", this method starts the RSSI
     * Runnable in order to request later the RSSI level.</p>
     *
     * @param packet
     *         The received packet with the command COMMAND_GET_CURRENT_RSSI.
     */
    private void receivePacketGetCurrentRSSIACK(GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_OFFSET = 1;
        final int PAYLOAD_VALUE_LENGTH = 1;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // ACK status length is 1

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            int level = payload[PAYLOAD_VALUE_OFFSET];
            mListener.onGetRSSILevel(level);

            // this method is called asynchronously so we have to be sure that the lock won't be changed
            if (mUpdateRssi && mPendingRSSICustomNotification) {
                // we received the waiting battery information
                mPendingRSSICustomNotification = false;
                // we need to retrieve this information constantly
                mHandler.postDelayed(mRunnableRSSI, RSSI_DELAY_TIME);
            }
        }
    }


    // ====== PRIVATE METHODS =============================================================

    /**
     * <p>This method creates the request to get the RSSI level from the device and dispatches it.</p>
     */
    private void getRSSIInformation() {
        if (!mPendingRSSICustomNotification) {
            mPendingRSSICustomNotification = true;
            createRequest(createPacket(GAIA.COMMAND_GET_CURRENT_RSSI));
        }
    }


    // ====== INTERFACES ===========================================================================

    /**
     * <p>This interface allows this manager to dispatch messages or events to a listener.</p>
     */
    public interface ProximityGaiaManagerListener {

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
         * <p>This method informs that the RSSI command is not supported by the device.</p>
         * <p>A requested item of information is considered as not supported by the device if the acknowledgement of the
         * request is not successful.</p>
         */
        void onRSSINotSupported();

        /**
         * <p>This method is called when the device had sent its RSSI level through the acknowledgement of the
         * GAIA command {@link GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI}.</p>
         *
         * @param level
         *          The level of the RSSI given by the device.
         */
        void onGetRSSILevel(int level);

    }

}
