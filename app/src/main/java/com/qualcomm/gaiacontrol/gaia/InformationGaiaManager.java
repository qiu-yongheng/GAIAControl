/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.GaiaException;
import com.qualcomm.libraries.gaia.GaiaUtils;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the information Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 * <p>The GAIA Notifications does not include any update about the actual battery level or the RSSI level. In
 * order to allow the display to be updated this manager creates its own notifications by running the corresponding
 * GET request every {@link #DELAY_CUSTOM_NOTIFICATION DELAY_CUSTOM_NOTIFICATION}. These notifications are called
 * "custom notifications" in this class.</p>
 */
public class InformationGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in the debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * This time is used to delay a runnable in order to run the custom notifications.
     */
    private static final int DELAY_CUSTOM_NOTIFICATION = 5000;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * The handler to run some tasks.
     */
    private final Handler mHandler = new Handler();
    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "InformationGaiaManager";
    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;
    /**
     * <p>The GAIA Notifications does not include any update about the actual battery level or the RSSI level. In
     * order to allow the display to be updated this manager creates its own notifications by running the corresponding
     * GET request every {@link #DELAY_CUSTOM_NOTIFICATION DELAY_CUSTOM_NOTIFICATION}. These notifications are called "custom
     * notifications" in this class.</p>
     * <p>The value in this ArrayMap is a flag that indicates when the GET request received an answer if it was
     * expected as a notification.</p>
     */
    private static final ArrayMap<Integer, Boolean> mPendingCustomNotifications = new ArrayMap<>();
    /**
     * To start a task to get the battery level from the device.
     */
    private final Runnable mRunnableBattery = new Runnable() {
        @Override
        public void run() {
            synchronized (mPendingCustomNotifications) {
                if (mPendingCustomNotifications.containsKey(Information.BATTERY)) {
                    mPendingCustomNotifications.put(Information.BATTERY, true);
                    getInformation(Information.BATTERY);
                }
            }
        }
    };
    /**
     * To start a task to get the RSSI value from the device.
     */
    private final Runnable mRunnableRSSI = new Runnable() {
        @Override
        public void run() {
            synchronized (mPendingCustomNotifications) {
                if (mPendingCustomNotifications.containsKey(Information.RSSI)) {
                    mPendingCustomNotifications.put(Information.RSSI, true);
                    getInformation(Information.RSSI);
                }
            }
        }
    };


    // ====== ENUM =================================================================================

    /**
     * <p>This enumeration represents all the information the application can request for the Information Activity in
     * order to display them.</p>
     * <p>The corresponding commands are requested by using {@link #getInformation(int) getInformation}. All the
     * notification events are activated or deactivated using the
     * {@link #getNotifications(int, boolean) getNotifications}.</p>
     */
    @IntDef(flag = true, value = { Information.BATTERY, Information.RSSI, Information.API_VERSION })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface Information {
        /**
         * <p>The BATTERY information represents all information related to the battery.</p>
         * <p>This information is the following:
         * <ul>
         *     <li>The battery level through the command {@link GAIA#COMMAND_GET_CURRENT_BATTERY_LEVEL}.</li>
         *     <li>The charger alerts - charger connected/disconnected and battery charged - through the events
         *     {@link com.qualcomm.libraries.gaia.GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION}.</li>
         * </ul></p>
         */
        int BATTERY = 1;
        /**
         * <p>The RSSI information represents all information related to the RSSI.</p>
         * <p>This information is the following:
         * <ul>
         *     <li>The RSSI level through the command {@link GAIA#COMMAND_GET_CURRENT_RSSI}.</li>
         * </ul></p>
         */
        int RSSI = 2;
        /**
         * <p>The API version information gives the API version numbers.</p>
         * <p>This information is the following:
         * <ul>
         *     <li>The version numbers through the command {@link GAIA#COMMAND_GET_API_VERSION}.</li>
         * </ul></p>
         */
        int API_VERSION = 3;
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
    public InformationGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>This method requests the information from the device to know its state.</p>
     * <p>The information which is retrievable from the device through this manager follows.
     * It uses the given commands.</p>
     * <ul>
     *     <li>{@link Information#API_VERSION API_VERSION
     *     }: using command {@link GAIA#COMMAND_GET_API_VERSION COMMAND_GET_API_VERSION}.</li>
     * <li>{@link Information#BATTERY BATTERY}: using
     * command {@link GAIA#COMMAND_GET_CURRENT_BATTERY_LEVEL COMMAND_GET_CURRENT_BATTERY_LEVEL}.</li>
     * <li>{@link Information#RSSI RSSI}: using command
     * {@link GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI}.</li>
     * </ul>
     * Any other {@link Information Information} has no effect here.</p>
     *
     * @param information
     *          The information to get the state, one of the following:
     *          {@link Information#API_VERSION API_VERSION},
     *          {@link Information#BATTERY BATTERY} or
     *          {@link Information#RSSI RSSI}.
     */
    public void getInformation(@Information int information) {
        switch (information) {
            case Information.API_VERSION:
                createRequest(createPacket(GAIA.COMMAND_GET_API_VERSION));
                break;
            case Information.BATTERY:
                createRequest(createPacket(GAIA.COMMAND_GET_CURRENT_BATTERY_LEVEL));
                break;
            case Information.RSSI:
                createRequest(createPacket(GAIA.COMMAND_GET_CURRENT_RSSI));
                break;
        }
    }

    /**
     * <p>To register or unregister notifications for the given information.</p>
     * <p>Only the following {@link Information} has available notifications: {@link Information#BATTERY BATTERY}
     * and {@link Information#RSSI RSSI}.</p>
     * <p>If the GAIA protocol does not provide notification events through the Notification commands, this method
     * creates custom notifications by delaying a runnable.</p>
     *
     * @param information
     *          The information to get notifications for.
     * @param notify
     *          True to activate the notifications, false to deactivate them.
     */
    @SuppressLint("SwitchIntDef")
    public void getNotifications(@Information int information, boolean notify) {
        switch (information) {
            case Information.BATTERY:
                getBatteryNotifications(notify);
                break;
            case Information.RSSI:
                getRSSINotifications(notify);
                break;
        }
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case GAIA.COMMAND_GET_CURRENT_BATTERY_LEVEL:
                receivePacketGetCurrentBatteryLevelACK(packet);
                break;
            case GAIA.COMMAND_GET_CURRENT_RSSI:
                receivePacketGetCurrentRSSIACK(packet);
                break;
            case GAIA.COMMAND_GET_API_VERSION:
                receivePacketGetAPIVersionACK(packet);
                break;
        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
        if (packet.getStatus() != GAIA.Status.NOT_SUPPORTED) {
            onInformationNotSupported(packet.getCommand()); // we consider that the information is not supported here
        }
        else {
            onInformationNotSupported(packet.getCommand());
        }
    }

    @Override // extends GaiaManager
    protected void hasNotReceivedAcknowledgementPacket(GaiaPacket packet) {
    }

    @Override // extends GaiaManager
    @SuppressWarnings("SimplifiableIfStatement") // more readable without the simplification
    protected boolean manageReceivedPacket(GaiaPacket packet) {
        if (packet.getCommand() == GAIA.COMMAND_EVENT_NOTIFICATION) {
            return receiveEventNotification(packet);
        }

        return false;
    }

    @Override // extends GaiaManager
    protected boolean sendGAIAPacket(byte[] packet) {
        return mListener.sendGAIAPacket(packet);
    }


    // ====== PRIVATE METHODS - SENDING =============================================================

    /**
     * <p>To register for a {@link GAIA.NotificationEvents GAIA event notification}.</p>
     *
     * @param event
     *              The event to register with.
     */
    @SuppressWarnings("SameParameterValue")
    private void registerGAIANotification(@GAIA.NotificationEvents int event) {
        try {
            GaiaPacket packet = GaiaPacket.buildGaiaNotificationPacket(GAIA.VENDOR_QUALCOMM, GAIA
                    .COMMAND_REGISTER_NOTIFICATION, event, null, getTransportType());
            createRequest(packet);
        } catch (GaiaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * <p>To cancel a {@link GAIA.NotificationEvents GAIA event notification}.</p>
     *
     * @param event
     *              The notification event to cancel.
     */
    @SuppressWarnings("SameParameterValue")
    private void cancelGAIANotification(@GAIA.NotificationEvents int event) {
        try {
            GaiaPacket packet = GaiaPacket.buildGaiaNotificationPacket(GAIA.VENDOR_QUALCOMM, GAIA
                    .COMMAND_CANCEL_NOTIFICATION, event, null, getTransportType());
            createRequest(packet);
        } catch (GaiaException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    // ====== PRIVATE METHODS - RECEIVING =============================================================

    /**
     * <p>To manage a received {@link GaiaPacket} which has {@link GAIA#COMMAND_EVENT_NOTIFICATION} for command.</p>
     * <p>This manager is only interested by the following events:
     * <ul>
     *     <li>{@link GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION} event to be informed if the charger
     *     had been connected or disconnected to/from the device.</li>
     *     <li></li>
     * </ul></p>
     *
     * @param packet
     *              The received notification event packet.
     *
     * @return true if an acknowledgement has been sent.
     */
    @SuppressLint("SwitchIntDef")
    private boolean receiveEventNotification(GaiaPacket packet) {
        final int PAYLOAD_MIN_LENGTH = 1;
        byte[] payload = packet.getPayload();

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            @GAIA.NotificationEvents int event = packet.getEvent();
            switch (event) {
                case GAIA.NotificationEvents.CHARGER_CONNECTION:
                    // event has parameters
                    return receiveEventChargerConnection(packet);
            }
            // other events are not supported by this method
            return false;
        }
        else {
            createAcknowledgmentRequest(packet, GAIA.Status.INVALID_PARAMETER, null);
            return true;
        }
    }

    /**
     * <p>This method is called when this manager handles a GAIA packet from the device with the GAIA command
     * {@link GAIA#COMMAND_EVENT_NOTIFICATION COMMAND_EVENT_NOTIFICATION} and the event
     * {@link GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION}.</p>
     * <p>This method checks if the packet has the expected parameters, informs the listener if a value can be
     * retrieved and acknowledges the packet.</p>
     *
     * @param packet
     *          The received packet with the GAIA command
     *          {@link GAIA#COMMAND_EVENT_NOTIFICATION COMMAND_EVENT_NOTIFICATION} and the event
     *          {@link GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION}.
     *
     * @return True if the packet had been acknowledged - this method always acknowledge as when it is reached the
     * command is known as being supported.
     */
    @SuppressWarnings("SameReturnValue") // the method has a return to be consistent with the method which called it
    private boolean receiveEventChargerConnection(GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_OFFSET = 1;
        final int PAYLOAD_VALUE_LENGTH = 1;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // event length is 1 in the payload

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            createAcknowledgmentRequest(packet, GAIA.Status.SUCCESS, null);
            boolean isCharging = packet.getPayload()[PAYLOAD_VALUE_OFFSET] == 0x01;
            mListener.onChargerConnected(isCharging);
            return true;
        }
        else {
            createAcknowledgmentRequest(packet, GAIA.Status.INVALID_PARAMETER, null);
            return true;
        }
    }

    /**
     * <p>Called when this manager handles a packet with the command
     * {@link GAIA#COMMAND_GET_CURRENT_BATTERY_LEVEL COMMAND_GET_CURRENT_BATTERY_LEVEL}.</p>
     * <p>This method checks if the packet contains the expected parameters, retrieves them and send them to
     * the listener.</p>
     * <p>If there was a pending custom notification for this type of packet, this method starts the battery
     * Runnable in order to request later the battery level.</p>
     *
     * @param packet
     *         The received packet with the command COMMAND_GET_CURRENT_BATTERY_LEVEL.
     */
    private void receivePacketGetCurrentBatteryLevelACK(GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_OFFSET = 1;
        final int PAYLOAD_VALUE_LENGTH = 2;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // ACK status length is 1

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            int level = GaiaUtils.extractIntFromByteArray(packet.getPayload(), PAYLOAD_VALUE_OFFSET,
                    PAYLOAD_VALUE_LENGTH, false);
            mListener.onGetBatteryLevel(level);

            // this method is called asynchronously so we have to be sure that the lock won't be changed
            synchronized (mPendingCustomNotifications) {
                if (mPendingCustomNotifications.containsKey(Information.BATTERY)
                        && mPendingCustomNotifications.get(Information.BATTERY)) {
                    // we received the waiting battery information
                    mPendingCustomNotifications.put(Information.BATTERY, false);
                    // we need to retrieve this information constantly
                    mHandler.postDelayed(mRunnableBattery, DELAY_CUSTOM_NOTIFICATION);
                }
            }
        }
    }

    /**
     * <p>Called when this manager handles a packet with the command
     * {@link GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI}.</p>
     * <p>This method checks if the packet contains the expected parameters, retrieves them and send them to
     * the listener.</p>
     * <p>If there was a pending custom notification for this type of packet, this method starts the RSSI
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
            synchronized (mPendingCustomNotifications) {
                if (mPendingCustomNotifications.containsKey(Information.RSSI)
                        && mPendingCustomNotifications.get(Information.RSSI)) {
                    // we received the waiting battery information
                    mPendingCustomNotifications.put(Information.RSSI, false);
                    // we need to retrieve this information constantly
                    mHandler.postDelayed(mRunnableRSSI, DELAY_CUSTOM_NOTIFICATION);
                }
            }
        }
    }

    /**
     * <p>Called when this manager handles a packet with the command
     * {@link GAIA#COMMAND_GET_API_VERSION COMMAND_GET_API_VERSION}.</p>
     * <p>This method checks if the packet contains the expected parameters, retrieves them and send them to
     * the listener.</p>
     *
     * @param packet
     *         The received packet with the command COMMAND_GET_API_VERSION.
     */
    private void receivePacketGetAPIVersionACK(GaiaPacket packet) {
        byte[] payload = packet.getPayload();
        final int PAYLOAD_VALUE_1_OFFSET = 1;
        final int PAYLOAD_VALUE_2_OFFSET = PAYLOAD_VALUE_1_OFFSET + 1;
        final int PAYLOAD_VALUE_3_OFFSET = PAYLOAD_VALUE_2_OFFSET + 1;
        final int PAYLOAD_VALUE_LENGTH = 3;
        final int PAYLOAD_MIN_LENGTH = PAYLOAD_VALUE_LENGTH + 1; // ACK status length is 1

        if (payload.length >= PAYLOAD_MIN_LENGTH) {
            mListener.onGetAPIVersion(payload[PAYLOAD_VALUE_1_OFFSET], payload[PAYLOAD_VALUE_2_OFFSET],
                    payload[PAYLOAD_VALUE_3_OFFSET]);
        }
    }

    /**
     * <p>This method is called when the request for an information had been unsuccessfully acknowledged. In which
     * case we consider that the get request for the information is not supported.</p>
     * <p>This method then dispatches the unsupported information which corresponds to the given command to the
     * listener.</p>
     * <p>If there was a pending custom notification for the unsupported information, this method cancels it.</p>
     *
     * @param command
     *          The command which the support had been determined as not supported.
     */
    private void onInformationNotSupported(int command) {
        switch (command) {
            case GAIA.COMMAND_GET_CURRENT_BATTERY_LEVEL:
                mListener.onInformationNotSupported(Information.BATTERY);
                synchronized (mPendingCustomNotifications) {
                    if (mPendingCustomNotifications.containsKey(Information.BATTERY)) {
                        mPendingCustomNotifications.remove(Information.BATTERY);
                    }
                }
                break;
            case GAIA.COMMAND_GET_CURRENT_RSSI:
                mListener.onInformationNotSupported(Information.RSSI);
                synchronized (mPendingCustomNotifications) {
                    if (mPendingCustomNotifications.containsKey(Information.RSSI)) {
                        mPendingCustomNotifications.remove(Information.RSSI);
                    }
                }
                break;
            case GAIA.COMMAND_GET_API_VERSION:
                mListener.onInformationNotSupported(Information.API_VERSION);
                break;
        }
    }


    // ====== PRIVATE METHODS ====================================================================

    /**
     * <p>This method starts or stops battery related notifications.</p>
     * <p>For the battery notifications, the application is interested on knowing if a charger had been connected and
     * on regularly being informed of the battery level.</p>
     * <p>In order to know if a charged had been connected, the manager registers fir the GAIA Notification
     * event {@link GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION}.</p>
     * <p>Because there is no GAIA notification event available for the battery level, this manager creates its own
     * custom notification as follows:
     * <ul>
     *     <li>The manager requests the current battery level from the device using the corresponding GAIA command.</li>
     *     <li>Once it receives the level value, it requests it again through a delayed Runnable. The delay time is
     *     defined by {@link #DELAY_CUSTOM_NOTIFICATION DELAY_CUSTOM_NOTIFICATION}.</li>
     * </ul>
     *
     * @param notify
     *          True to start the notifications, false to cancel them.
     *
     */
    private void getBatteryNotifications(boolean notify) {
        // there is no existing notification for battery level so we request the battery level through a Runnable
        synchronized (mPendingCustomNotifications) {
            if (notify) {
                registerGAIANotification(GAIA.NotificationEvents.CHARGER_CONNECTION);
                mPendingCustomNotifications.put(Information.BATTERY, true);
                getInformation(Information.BATTERY);
            } else {
                mPendingCustomNotifications.remove(Information.BATTERY);
                mHandler.removeCallbacks(mRunnableBattery);
                cancelGAIANotification(GAIA.NotificationEvents.CHARGER_CONNECTION);
            }
        }
    }

    /**
     * <p>This method starts the notifications related to the RSSI or stops them.</p>
     * <p>For the RSSI notifications, the application is only interested on regularly being informed of the RSSI
     * level.</p>
     * <p>Because there is no GAIA notification event available for the RSSI level, this manager creates its own
     * custom notification as follows:
     * <ul>
     *     <li>The manager requests the current RSSI level from the device using the corresponding GAIA command.</li>
     *     <li>Once it receives the level value, it requests it again through a delayed Runnable. The delay time is
     *     defined by {@link #DELAY_CUSTOM_NOTIFICATION DELAY_CUSTOM_NOTIFICATION}.</li>
     * </ul>
     *
     * @param notify
     *          True to start the notifications, false to cancel them.
     *
     */
    private void getRSSINotifications(boolean notify) {
        // there is no existing notification for battery level so we request the battery level through a Runnable
        synchronized (mPendingCustomNotifications) {
            if (notify) {
                mPendingCustomNotifications.put(Information.RSSI, true);
                getInformation(Information.RSSI);
            } else {
                mPendingCustomNotifications.remove(Information.RSSI);
                mHandler.removeCallbacks(mRunnableRSSI);
            }
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
         * <p>This method informs that a requested information had been considered as not supported by the device.</p>
         * <p>A requested information is considered as not supported by the device if the acknowledgement of the
         * request is not successful.</p>
         *
         * @param information
         *          The information which had been determined as not supported.
         */
        void onInformationNotSupported(@Information int information);

        /**
         * <p>This method is called when the device has informed that a charger had been connected or disconnected
         * through the
         * {@link GAIA.NotificationEvents#CHARGER_CONNECTION CHARGER_CONNECTION} event.</p>
         *
         * @param isConnected
         *          True if the charger had been connected, false otherwise.
         */
        void onChargerConnected(boolean isConnected);

        /**
         * <p>This method is called when the device had sent its battery level through the acknowledgement of the
         * GAIA command {@link GAIA#COMMAND_GET_CURRENT_BATTERY_LEVEL COMMAND_GET_CURRENT_BATTERY_LEVEL}.</p>
         *
         * @param level
         *          The level of the battery given by the device.
         */
        void onGetBatteryLevel(int level);

        /**
         * <p>This method is called when the device had sent its RSSI level through the acknowledgement of the
         * GAIA command {@link GAIA#COMMAND_GET_CURRENT_RSSI COMMAND_GET_CURRENT_RSSI}.</p>
         *
         * @param level
         *          The level of the RSSI given by the device.
         */
        void onGetRSSILevel(int level);

        /**
         * <p>This method is called when the device had sent its API version through the acknowledgement of the
         * GAIA command {@link GAIA#COMMAND_GET_API_VERSION COMMAND_GET_API_VERSION}.</p>
         *
         * @param versionPart1
         *          The first number of the version is represented as 1.x.x
         * @param versionPart2
         *          The second number of the version is represented as x.2.x
         * @param versionPart3
         *          The third number of the version is represented as x.x.3
         */
        void onGetAPIVersion(int versionPart1, int versionPart2, int versionPart3);
    }

}
