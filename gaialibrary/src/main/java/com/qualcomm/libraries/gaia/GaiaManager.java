/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.qualcomm.libraries.gaia.packets.GaiaPacket;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBLE;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBREDR;
import com.qualcomm.libraries.gaia.requests.GaiaAcknowledgementRequest;
import com.qualcomm.libraries.gaia.requests.GaiaRequest;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>This class manages the sending and receiving of packets using the GAIA protocol.</p>
 * <p>This protocol is used through two different transports:
 * {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} and
 * {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR_EDR}. Depending on the transport the structure of a GAIA
 * packet is different, see {@link GaiaPacketBLE GaiaPacketBLE} and {@link GaiaPacketBREDR GaiaPacketBREDR} for more
 * information. The transport this manager uses can only be defined when creating this manager, through the class
 * constructor.</p>
 * <p>For any byte array known as a potential GAIA packet, it is passed to this manager using
 * {@link #onReceiveGAIAPacket(byte[]) onReceiveGAIAPacket}. This method will then analyze the array in order to
 * get a {@link GaiaPacket GaiaPacket}. Then depending on the content of this packet one of the following abstract
 * methods is called:
 * <ul>
 *     <li>If the packet is an acknowledgement packet:
 *     {@link #receiveSuccessfulAcknowledgement(GaiaPacket) receiveSuccessfulAcknowledgement} or
 *     {@link #receiveUnsuccessfulAcknowledgement(GaiaPacket) receiveUnsuccessfulAcknowledgement}.</li>
 *     <li>Otherwise: {@link #manageReceivedPacket(GaiaPacket) manageReceivedPacket}.</li>
 * </ul>
 * This way the application only has to provide the process which is implementation dependant through the
 * implementation of these abstract methods.</p>
 * <p>This manager provides a {@link #createRequest(GaiaPacket) createRequest} method in order to send a GAIA Packet
 * to the device and to inform the application if it has not received a corresponding acknowledgement packet, see
 * {@link #hasNotReceivedAcknowledgementPacket(GaiaPacket) hasNotReceivedAcknowledgementPacket}. To define the time
 * before declaring the request as timed out, use {@link #setRequestTimeOut(int) setRequestTimeOut}. The default time is
 * {@link #ACKNOWLEDGEMENT_RUNNABLE_DEFAULT_DELAY_MILLIS ACKNOWLEDGEMENT_RUNNABLE_DEFAULT_DELAY_MILLIS}.</p>
 * <p>This manager should be reset when the device is disconnected.</p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class GaiaManager {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "GaiaManager";
    /**
     * <p>An array map which groups all Runnable for requests which have sent a GAIA packet and are waiting for the
     * corresponding acknowledgement packet.</p>
     */
    private final ArrayMap<Integer, LinkedList<TimeOutRequestRunnable>> mTimeOutRequestRunnableMap = new ArrayMap<>();
    /**
     * <p>The default time to declare a GAIA request is timed out.</p>
     * <p>A request is considered as timed out when the GAIA packet sent by the request has not received a corresponding
     * acknowledgement packet.</p>
     */
    protected static final int ACKNOWLEDGEMENT_RUNNABLE_DEFAULT_DELAY_MILLIS = 30000;
    /**
     * <p>The time in millisecond used to delay a Runnable in order to declare a request had been timed out.</p>
     * <p>A request is considered as time out when the packet sent for the request has not received a corresponding
     * acknowledgement packet.</p>
     */
    private int mTimeOutRequestDelay = ACKNOWLEDGEMENT_RUNNABLE_DEFAULT_DELAY_MILLIS;
    /**
     * <p>The main handler to run tasks.</p>>
     */
    private final Handler mHandler = new Handler();
    /**
     * The type of transport this manager should use for the GAIA packet format:
     * {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     * {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    private final @GAIA.Transport int mTransportType;
    /**
     * <p>To show the debug logs indicating when a method had been reached.</p>
     */
    private boolean mShowDebugLogs = false;


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows initialisation of a listener to send messages to a device or
     * dispatch any GAIA received messages.</p>
     *
     * @param transportType
     *          The type of transport which will be used to send and receive GAIA packets. The transport defines the
     *          structure of the packet.
     */
    protected GaiaManager(@GAIA.Transport int transportType) {
        mTransportType = transportType;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>To reset the manager by deleting all GAIA requests pending for an acknowledgement.</p>
     */
    public void reset() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to reset the manager.");
        }
        resetTimeOutRequestRunnableMap();
    }

    /**
     * <p>To set up the time to wait before declaring that requests are timed out.</p>
     * <p>This method is synchronised in order to be sure that any request created after this call will use the set up
     * time.</p>
     *
     * @param time
     *          The time to use as a waiting time.
     */
    public synchronized void setRequestTimeOut(int time) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Time out set up to " + time + ", previous time out was " + mTimeOutRequestDelay);
        }
        mTimeOutRequestDelay = time;
    }

    /**
     * To get the type of transport used by this manager.
     *
     * @return The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public @GAIA.Transport int getTransportType() {
        return mTransportType;
    }


    // ====== PROTECTED METHODS =======================================================================

    /**
     * <p>To allow the display of the debug logs.</p>
     * <p>They give complementary information on any call of a method.
     * They can indicate that a method is reached but also the action the method does.</p>
     *
     * @param show
     *          True to show the debug logs, false otherwise.
     */
    protected void showDebugLogs(boolean show) {
        mShowDebugLogs = show;
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
    }


    // ====== GAIA METHODS - SENDING ===============================================================

    /**
     * <p>This method builds an acknowledgement packet for a received GAIA packet and dispatches it to the attached
     * listener to be sent over the communication channel.</p>
     *
     * @param packet
     *            The original received packet.
     * @param status
     *            The status for the acknowledgement.
     * @param value
     *            Any complementary information to add to the packet, can be null.
     */
    private void sendGAIAAcknowledgement(GaiaPacket packet, @GAIA.Status int status, @Nullable byte[] value) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request to send acknowledgement for packet with command " + packet.getCommand());
        }

        if (packet.isAcknowledgement()) {
            Log.w(TAG, "Send of GAIA acknowledgement failed: packet is already an acknowledgement packet.");
            return;
        }

        try {
            byte[] bytes = packet.getAcknowledgementPacketBytes(status, value);
            sendGAIAPacket(bytes);
        }
        catch (GaiaException e) {
            Log.w(TAG, "ACK packet not created, exception occurred: " + e.toString());
        }

    }


    // ====== GAIA METHODS - RECEIVING =============================================================

    /**
     * <p>This method should be called by the application when it thinks it has received some bytes which might
     * correspond to a GAIA packet.</p>
     * <p>This method will first attempt to build a {@link GaiaPacket GaiaPacket} object with the given bytes.</p>
     * <p>Then, if this was successful, and depending on the content of the packet, one of the following methods is
     * called:<ul>
     *     <li>If the packet is an acknowledgement packet:
     *     {@link #receiveSuccessfulAcknowledgement(GaiaPacket) receiveSuccessfulAcknowledgement} or
     *     {@link #receiveUnsuccessfulAcknowledgement(GaiaPacket) receiveUnsuccessfulAcknowledgement}.</li>
     *     <li>Otherwise: {@link #manageReceivedPacket(GaiaPacket) manageReceivedPacket}.</li>
     * </ul></p>
     * <p>To finish, if the packet is not an acknowledgement packet, the GAIA Manager - this abstract class or the
     * implementation of the child class - must acknowledge it. If the child class does not acknowledge the packet
     * through the return of {@link #manageReceivedPacket(GaiaPacket) manageReceivedPacket}, this method sends an
     * acknowledgement packet with status
     * {@link com.qualcomm.libraries.gaia.GAIA.Status#NOT_SUPPORTED NOT_SUPPORTED}.</p>
     */
    public void onReceiveGAIAPacket(byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Received potential GAIA packet: " + GaiaUtils.getHexadecimalStringFromBytes(data));
        }

        GaiaPacket packet;

        // Attempt to to get the received packet
        try {
            packet = mTransportType == GAIA.Transport.BLE ? new GaiaPacketBLE(data)
                    : new GaiaPacketBREDR(data);
        }
        catch (GaiaException e) {
            // occurs when the data array length is too short to contain the mandatory fields of a GAIA packet.
            // for BLE, mandatory fields are the vendor ID and the command ID.
            Log.w(TAG, "Impossible to retrieve packet from device: " + GaiaUtils.getHexadecimalStringFromBytes(data));
            // we cannot send an ACK or act as we cannot retrieve any command value.
            return;
        }

        if (mShowDebugLogs) {
            Log.d(TAG, "Manager could retrieve a packet from the given data with command: " +
                    GaiaUtils.getGAIACommandToString(packet.getCommand()));
        }

        // checking if we received any acknowledgement
        if (packet.isAcknowledgement()) {
            if (!cancelTimeOutRequestRunnable(packet.getCommand())) {
                Log.w(TAG, "Received unexpected acknowledgement packet for command "
                        + GaiaUtils.getGAIACommandToString(packet.getCommand()));
                return;
            }

            // acknowledgement was expected: it is dispatched to the child
            @GAIA.Status int status = packet.getStatus();
            if (mShowDebugLogs) {
                Log.d(TAG, "Received GAIA ACK packet for command "
                        + GaiaUtils.getGAIACommandToString(packet.getCommand())
                        + " with status: " + GAIA.getStatusToString(status));
            }

            if (status == GAIA.Status.SUCCESS) {
                receiveSuccessfulAcknowledgement(packet);
            } else {
                receiveUnsuccessfulAcknowledgement(packet);
            }
        }
        // not an ACK packet: we have to ack it
        else {
            if (!manageReceivedPacket(packet)) {
                Log.i(TAG, "Packet has not been managed by application, manager sends NOT_SUPPORTED acknowledgement.");
                createAcknowledgmentRequest(packet, GAIA.Status.NOT_SUPPORTED, null);
            }
        }
    }


    // ====== REQUESTS PROCESS ===============================================================

    /**
     * <p>To create a GAIA request to send a packet over the listener.</p>
     *
     * @param packet
     *            The packet to send over the listener.
     */
    protected void createRequest(GaiaPacket packet) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Received request to send a packet for command: "
                    + GaiaUtils.getGAIACommandToString(packet.getCommand()));
        }
        GaiaRequest request = new GaiaRequest(GaiaRequest.Type.SINGLE_REQUEST);
        request.packet = packet;
        processRequest(request);
    }

    /**
     * <p>To create an acknowledgement GAIA request to send a packet over the listener.</p>
     *
     * @param packet
     *            The packet to acknowledge over the listener.
     */
    protected void createAcknowledgmentRequest(GaiaPacket packet, @GAIA.Status int status, @Nullable byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Received request to send an acknowledgement packet for command: "
                    + GaiaUtils.getGAIACommandToString(packet.getCommand()) + "with status: "
                    + GAIA.getStatusToString(status));
        }
        GaiaAcknowledgementRequest request = new GaiaAcknowledgementRequest(status, data);
        request.packet = packet;
        processRequest(request);
    }

    /**
     * <p>To start a Runnable which will be thrown after the known time out request delay set up with
     * {@link #setRequestTimeOut(int) setRequestTimeOut}. This Runnable deals with GAIA requests that didn't
     * receive any acknowledgement for their sent packet.</p>
     *
     * @param request
     *              The GAIA request which expects a acknowledgement.
     */
    private void startTimeOutRequestRunnable(GaiaRequest request) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Set up TimeOutRequestRunnable for type request: " + request.type + " for command "
                    + GaiaUtils.getGAIACommandToString(request.packet.getCommand()));
        }

        TimeOutRequestRunnable runnable = new TimeOutRequestRunnable(request);
        int key = request.packet.getCommand();
        if (mTimeOutRequestRunnableMap.containsKey(key)) {
            mTimeOutRequestRunnableMap.get(key).add(runnable);
        }
        else {
            LinkedList<TimeOutRequestRunnable> list = new LinkedList<>();
            list.add(runnable);
            mTimeOutRequestRunnableMap.put(request.packet.getCommand(), list);
        }
        mHandler.postDelayed(runnable, mTimeOutRequestDelay);
    }

    /**
     * <p>To cancel the TimeOutRequestRunnable if there is one running.</p>
     * <p>This method will check if the given key corresponds to any running Runnable, if it does it will stop the
     * Runnable and then removed it from the Map.</p>
     * <p>The key corresponds to the GAIA command of the request which corresponds to the Runnable.</p>
     * @param key
     *          The key of the TimeOutRequestRunnable in the Map.
     */
    private boolean cancelTimeOutRequestRunnable(int key) {
        synchronized (mTimeOutRequestRunnableMap) {
            if (mShowDebugLogs) {
                Log.d(TAG, "Request to cancel a TimeOutRequestRunnable for command: "
                        + GaiaUtils.getGAIACommandToString(key));
            }

            if (!mTimeOutRequestRunnableMap.containsKey(key)) {
                // time out request runnable not found
                Log.w(TAG, "No pending TimeOutRequestRunnable matches command: "
                        + GaiaUtils.getGAIACommandToString(key));
                return false;
            }

            // expected command
            List<TimeOutRequestRunnable> list = mTimeOutRequestRunnableMap.get(key);
            // get the first runnable corresponding to the given key - which should be the oldest one
            TimeOutRequestRunnable runnable = list.remove(0);
            // stop the runnable
            mHandler.removeCallbacks(runnable);
            // if there is no other runnable for that key we removed the entry from the Map
            if (list.isEmpty()) {
                mTimeOutRequestRunnableMap.remove(key);
            }
            return true;

        }
    }

    /**
     * <p>To reset the list of time out request runnable to an empty state.</p>
     */
    private synchronized void resetTimeOutRequestRunnableMap() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Received request to reset the TimeOutRequestRunnable Map");
        }
        for (int i = 0; i< mTimeOutRequestRunnableMap.size(); i++) {
            for (TimeOutRequestRunnable runnable : mTimeOutRequestRunnableMap.valueAt(i)) {
                mHandler.removeCallbacks(runnable);
            }
        }
        mTimeOutRequestRunnableMap.clear();
    }

    /**
     * <p>Call to process a request: gets the GAIA packet and sent it to the device.</p>
     * <p>This method will also starts a corresponding TimeOutRequestRunnable if the request requires to wait for
     * an acknowledgement.</p>
     */
    private void processRequest(GaiaRequest request) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Processing request of type " + request.type);
        }

        // process the request depending on its type
        switch (request.type) {
            case GaiaRequest.Type.SINGLE_REQUEST:
                try {
                    byte[] bytes = request.packet.getBytes();
                    // GAIA request which requires an acknowledgement packet
                    startTimeOutRequestRunnable(request);
                    sendGAIAPacket(bytes);
                }
                catch (GaiaException e) {
                    Log.w(TAG, "Exception when attempting to create GAIA packet: " + e.toString());
                }
                return;

            case GaiaRequest.Type.ACKNOWLEDGEMENT:
                // GAIA request in order to acknowledge a packet
                GaiaAcknowledgementRequest ackRequest = (GaiaAcknowledgementRequest) request;
                sendGAIAAcknowledgement(ackRequest.packet, ackRequest.status, ackRequest.data);
                return;
        }

        // if the method reaches this instruction that means it wasn't possible to create the request
        Log.w(TAG, "Not possible to create request with type " + request.type + " for GAIA command: "
                + request.packet.getCommandId());
    }


    // ====== ABSTRACT METHODS =====================================================================

    /**
     * <p>To manage the reception of a successful acknowledgement.</p>
     * <p>This method is called when the manager receives a successful acknowledgement.</p>
     *
     * @param packet
     *          the packet received which is the successful acknowledgement.
     */
    protected abstract void receiveSuccessfulAcknowledgement(GaiaPacket packet);

    /**
     * <p>To manage the reception of an unsuccessful acknowledgement.</p>
     * <p>This method is called when the manager receives an unsuccessful acknowledgement.</p>
     *
     * @param packet
     *          the packet received which is the unsuccessful acknowledgement.
     */
    protected abstract void receiveUnsuccessfulAcknowledgement(GaiaPacket packet);

    /**
     * <p>When this manager receives a packet which is not an acknowledgement this method is called to act depending
     * on the received packet.</p>
     * <p>This method MUST acknowledge the received packet if it fits any command managed by the application which
     * implements this manager.</p>
     *
     * @param packet
     *              The received packet this method has to deal with.
     *
     * @return true if the packet has been acknowledged in this method. If the method returns false the packet will be
     * acknowledged with a {@link GAIA.Status#NOT_SUPPORTED} status.
     */
    protected abstract boolean manageReceivedPacket(GaiaPacket packet);

    /**
     * <p>To manage any packet which did not receive any acknowledgement on the time set up with
     * {@link #setRequestTimeOut(int) setRequestTimeOut}.</p>
     * <p>By default this time is defined by {@link #ACKNOWLEDGEMENT_RUNNABLE_DEFAULT_DELAY_MILLIS}.</p>
     *
     * @param packet
     *          The packet which has not been acknowledged.
     */
    protected abstract void hasNotReceivedAcknowledgementPacket(GaiaPacket packet);

    /**
     * <p>To send over a communication channel the bytes of a GAIA packet using the GAIA protocol.</p>
     *
     * @param packet
     *          The byte array to send to a device.
     * @return
     *          true if the sending could be done.
     */
    @SuppressWarnings("UnusedReturnValue")
    protected abstract boolean sendGAIAPacket(byte[] packet);


    // ====== INNER CLASS ==========================================================================

    /**
     * <p>A Runnable to define what should be done if a request is timed out.</p>
     * <p>A request is considered as being timed out if the packet sent while processing the request has not
     * received a corresponding acknowledgement.</p>
     *
     */
    private class TimeOutRequestRunnable implements Runnable {
        /**
         * <p>The request which is monitored for a time out.</p>
         */
        private final GaiaRequest request;

        /**
         * <p>Constructor for this class.</p>
         *
         * @param request
         *            The corresponding request.
         */
        TimeOutRequestRunnable(GaiaRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            synchronized (mTimeOutRequestRunnableMap) {
                int command = request.packet.getCommand();

                if (mShowDebugLogs) {
                    Log.d(TAG, "A request is timed out for command: " + GaiaUtils.getGAIACommandToString(command));
                }

                if (!mTimeOutRequestRunnableMap.containsKey(command)) {
                    // time out are only for ACK commands
                    Log.w(TAG, "Unexpected runnable is running for command: "
                            + GaiaUtils.getGAIACommandToString(command));
                    return;
                }

                // runnable was expected to run
                LinkedList<TimeOutRequestRunnable> list = mTimeOutRequestRunnableMap.get(command);
                // remove the runnable from the list
                list.remove(this);
                // if there is no other runnable for that key we removed the entry from the Map
                if (list.isEmpty()) {
                    mTimeOutRequestRunnableMap.remove(command);
                }
            }

            Log.w(TAG, "No ACK packet for command: " + GaiaUtils.getGAIACommandToString(request.packet.getCommand()));
            hasNotReceivedAcknowledgementPacket(request.packet);
        }
    }

}
