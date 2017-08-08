/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.services;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.gaiacontrol.Utils;
import com.qualcomm.gaiacontrol.gaia.UpgradeGaiaManager;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBREDR;
import com.qualcomm.libraries.vmupgrade.UpgradeError;
import com.qualcomm.libraries.vmupgrade.UpgradeManager;
import com.qualcomm.libraries.vmupgrade.UploadProgress;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class provides the tools to connect, communicate and disconnect with a BR/EDR device over RFCOMM using the
 * GAIA protocol. This class manages a BR EDR connection by extending {@link BREDRProvider BREDRProvider}.</p>
 * <p>This class analyzes the incoming data from a device in order to detect GAIA packets which are then provided to
 * a registered listener using {@link Messages#GAIA_PACKET}.</p>
 * <p>This class is also in charge of the upgrade process by instancing the
 * {@link UpgradeGaiaManager UpgradeGaiaManager} when an upgrade is requested by the application. This provider will
 * then throw update messages about the upgrade to the register listener using {@link Messages#UPGRADE_MESSAGE}. If
 * there is an upgrade going on, any data corresponding to a potential GAIA packet is sent to the Upgrade GAIA
 * Manager and any registered listener is no longer informed about them.</p>
 */
/*package*/ class GAIABREDRProvider extends BREDRProvider implements UpgradeGaiaManager.GaiaManagerListener {
    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "GAIABREDRProvider";
    /**
     * <p>To show the debug logs.</p>
     */
    private boolean mShowDebugLogs = false;
    /**
     * <p>The listener which is interested in events going on on this provider: connection state, errors, messages
     * received, etc.</p>
     */
    private final Handler mListener;
    /**
     * The handler to use in order to delay some instructions in order to not block a current process.
     */
    private final Handler mHandler = new Handler();
    /**
     * The analyser of data used to build GAIA packets from bytes received from the Provider.
     */
    private final DataAnalyser mAnalyser = new DataAnalyser();
    /**
     * To manage the GAIA packets which had been received from the device during the process of an upgrade. If there
     * is no upgrade processing, this field is null.
     */
    private UpgradeGaiaManager mUpgradeGaiaManager;


    // ====== ENUMS =====================================================================================

    /**
     * <p>All types of messages this provider can throw to a registered listener.</p>
     */
    @IntDef(flag = true, value = {GAIABREDRProvider.Messages.CONNECTION_STATE_HAS_CHANGED,
            GAIABREDRProvider.Messages.GAIA_PACKET, GAIABREDRProvider.Messages.ERROR,
            GAIABREDRProvider.Messages.GAIA_READY, GAIABREDRProvider.Messages.UPGRADE_MESSAGE })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    /*package*/ @interface Messages {

        /**
         * <p>To inform that the connection state with the given device has changed.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The connection state of the device as: {@link State#CONNECTED CONNECTED},
         *     {@link State#CONNECTING CONNECTING},
         *     {@link State#DISCONNECTING DISCONNECTING} or
         *     {@link State#DISCONNECTED DISCONNECTED}. This information is contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         *     </ul>
         */
        int CONNECTION_STATE_HAS_CHANGED = 0;

        /**
         * <p>To inform that this provider has received a potential GAIA packet over an ongoing Bluetooth
         * connection.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>A <code>byte</code> array which contains the data corresponding to a potential GAIA packet. This
         *     information is contained in <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int GAIA_PACKET = 1;

        /**
         * <p>To inform about any unexpected error which occurs during an ongoing connection or the connection
         * process itself.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The type of error as one of the {@link Errors Errors} values. This information is contained in
         *     <code>{@link android.os.Message#arg1 msg.arg1}</code>.</li>
         * </ul>
         */
        int ERROR = 2;

        /**
         * <p>This message is used to let the application know it can now communicate with the device using the GAIA
         * protocol.</p>
         * <p>This message does not contain any other information.</p>
         */
        int GAIA_READY = 3;

        /**
         * <p>This message is used during the process of an upgrade to provide updates about the process to an attached
         * listener.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The type of upgrade message as listed in {@link GATTBLEService.UpgradeMessage UpgradeMessage}.
         *     This information is contained in <code>{@link android.os.Message#arg1 msg.arg1}</code>.</li>
         *     <li>Any complementary information for the upgrade message. This information is contained in
         *     <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_MESSAGE = 4;
    }


    // ====== CONSTRUCTOR ===============================================================================

    /**
     * <p>Constructor of this class to get a provider of BR/EDR connection.</p>
     *
     * @param manager
     *          The BluetoothManager this provider should use to get a BluetoothAdapter. If this is null the provider
     *          will use {@link BluetoothAdapter#getDefaultAdapter() getDefaultAdapter}, known as less efficient than
     *          {@link BluetoothManager#getAdapter() getAdapter}.
     * @param listener
     *         The handler which would like to get events, messages and information form this provider about a connection to
     *         a device.
     */
    GAIABREDRProvider(@NonNull Handler listener, BluetoothManager manager) {
        super(manager);
        mListener = listener;
    }


    // ====== PACKAGE METHODS ===============================================================================

    /**
     * <p>To start the Upgrade process with the given file.</p>
     * <p>This method initialises the {@link UpgradeGaiaManager UpgradeGaiaManager} which is in charge of processing
     * the upgrade.</p>
     *
     * @param file
     *        The file to use to upgrade the Device.
     */
    /*package*/ void startUpgrade(File file) {
        mUpgradeGaiaManager = new UpgradeGaiaManager(this, GAIA.Transport.BR_EDR);
        mUpgradeGaiaManager.startUpgrade(file);
    }

    /**
     * <p>To get the current {@link ResumePoints ResumePoints} of the Upgrade process.</p>
     * <p>If there is no ongoing upgrade this information is useless and not accurate.</p>
     *
     * @return The current known resume point of the upgrade.
     */
    /*package*/ @ResumePoints.Enum int getResumePoint() {
        return (mUpgradeGaiaManager != null) ? mUpgradeGaiaManager.getResumePoint() : ResumePoints.Enum.DATA_TRANSFER;
    }

    /**
     * <p>To abort the upgrade. This method only acts if the Device is connected. If the Device is not connected, there
     * is no ongoing upgrade on the Device side.</p>
     */
    /*package*/ void abortUpgrade() {
        if (getState() == State.CONNECTED && mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.abortUpgrade();
        }
    }

    /**
     * <p>To know if there is an upgrade going on.</p>
     *
     * @return true if an upgrade is already working, false otherwise.
     */
    /*package*/ boolean isUpgrading() {
        return mUpgradeGaiaManager != null && mUpgradeGaiaManager.isUpgrading();
    }

    /**
     * <p>To inform the Upgrade process about a confirmation it is waiting for.</p>
     *
     * @param type
     *        The type of confirmation the Upgrade process is waiting for.
     * @param confirmation
     *        True if the Upgrade process should continue, false to abort it.
     */
    /*package*/ void sendConfirmation(@UpgradeManager.ConfirmationType int type, boolean confirmation) {
        if (mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.sendConfirmation(type, confirmation);
        }
    }

    // ====== UPGRADE GAIA MANAGER LISTENER METHODS =============================================

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onVMUpgradeDisconnected() {
        if (!isUpgrading()) {
            mUpgradeGaiaManager.reset();
            mUpgradeGaiaManager = null;
        }
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onResumePointChanged(@ResumePoints.Enum int point) {
        sendMessageToListener(Messages.UPGRADE_MESSAGE,
                BluetoothService.UpgradeMessage.UPGRADE_STEP_HAS_CHANGED, point);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUpgradeError(UpgradeError error) {
        Log.e(TAG, "ERROR during upgrade: " + error.getString());
        sendMessageToListener(Messages.UPGRADE_MESSAGE, BluetoothService.UpgradeMessage.UPGRADE_ERROR, error);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUploadProgress(UploadProgress progress) {
        sendMessageToListener(Messages.UPGRADE_MESSAGE,
                BluetoothService.UpgradeMessage.UPGRADE_UPLOAD_PROGRESS, progress);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public boolean sendGAIAUpgradePacket(byte[] packet) {
        return sendData(packet);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUpgradeFinish() {
        sendMessageToListener(Messages.UPGRADE_MESSAGE, BluetoothService.UpgradeMessage.UPGRADE_FINISHED, null);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void askConfirmation(@UpgradeManager.ConfirmationType int type) {
        if (mListener != null) {
            sendMessageToListener(Messages.UPGRADE_MESSAGE,
                    BluetoothService.UpgradeMessage.UPGRADE_REQUEST_CONFIRMATION, type);
        }
        else {
            // default behaviour? use a notification?
            sendConfirmation(type, true);
        }
    }


    // ====== OVERRIDE SUPERCLASS METHODS ===========================================================

    @Override // BREDRProvider
    void showDebugLogs(boolean show) {
        mShowDebugLogs = show;
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
        super.showDebugLogs(show);
    }

    @Override // BREDRProvider
    void onConnectionStateChanged(@State int state) {
        sendMessageToListener(Messages.CONNECTION_STATE_HAS_CHANGED, state);

        if (state != State.CONNECTED) {
            mAnalyser.reset();
        }
    }

    @Override // BREDRProvider
    void onConnectionError(@Errors int error) {
        sendMessageToListener(Messages.ERROR, error);
    }

    @Override // BREDRProvider
    void onCommunicationRunning() {
        // this method is called in a running thread which needs to continue as soon as possible

        // sendMessageToListener throws a message so no process over the connection is done
        sendMessageToListener(Messages.GAIA_READY);

        // if an upgrade is processing it should go on but only once this process has released the thread
        if (isUpgrading()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUpgradeGaiaManager.onGaiaReady();
                }
            });
        }
    }

    @Override // BREDRProvider
    void onDataFound(byte[] data) {
        mAnalyser.analyse(data);
    }


    // ====== PRIVATE METHODS =========================================================================

    /**
     * <p>This method is called when the {@link DataAnalyser DataAnalyser} has built a potential GAIA packet from
     * incoming data from the connected device.</p>
     * <p>This method will dispatch the packet to its listener if there is no active upgrade.</p>
     *
     * @param data
     *          The potential packet.
     */
    private void onGAIAPacketFound(byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Receive potential GAIA packet: " + Utils.getStringFromBytes(data));
        }

        if (mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.onReceiveGAIAPacket((data));
        } else {
            sendMessageToListener(Messages.GAIA_PACKET, data);
        }
    }

    /**
     * <p>To inform the listener by sending it a message.</p>
     *
     * @param message
     *         The message type to send.
     */
    @SuppressWarnings("SameParameterValue")
    private void sendMessageToListener(@Messages int message) {
        if (mListener != null) {
            mListener.obtainMessage(message).sendToTarget();
        }
    }

    /**
     * <p>To inform the listener by sending a message to it.</p>
     *
     * @param message
     *         The message type to send.
     * @param object
     *         Any complementary object to the message.
     */
    private void sendMessageToListener(@Messages int message, Object object) {
        if (mListener != null) {
            mListener.obtainMessage(message, object).sendToTarget();
        }
    }

    /**
     * <p>To inform the listener by sending a message to it.</p>
     *
     * @param message
     *         The message type to send.
     * @param subMessage
     *         Any complementary message for the message
     * @param object
     *         Any complementary object to the message.
     */
    private void sendMessageToListener(@Messages int message, int subMessage, Object object) {
        if (mListener != null) {
            mListener.obtainMessage(message, subMessage, 0, object).sendToTarget();
        }
    }


    // ====== PRIVATE INNER CLASS ========================================================================

    /**
     * <p>This class analyses incoming data in order to build a packet corresponding to the GAIA protocol.</p>
     */
    private class DataAnalyser {
        /**
         * <p>This array contains the data received from the device and which might correspond to a GAIA packet.</p>
         */
        final byte[] mmData = new byte[GaiaPacketBREDR.MAX_PACKET];
        /**
         * <p>While building the data of a GAIA packet, this contain the flags information of the packet.</p>
         */
        int mmFlags;
        /**
         * <p>To get how many bytes had been received so far.</p>
         */
        int mmReceivedLength = 0;
        /**
         * <p>The number of bytes which are expected to build a current GAIA packet.</p>
         */
        int mmExpectedLength = GaiaPacketBREDR.MAX_PACKET;

        /**
         * <p>To reset the data of the analyser: no current packet at the moment.</p>
         */
        private void reset() {
            mmReceivedLength = 0;
            mmExpectedLength = GaiaPacketBREDR.MAX_PACKET;
        }

        /**
         * <p>This method will build a GAIA packet as defined in {@link GaiaPacketBREDR GaiaPacketBREDR}.</p>
         * <p>This method uses the data provided at each call to build a GAIA packet following this process:
         * <ol>
         *     <li>Looks for the start of the packet known as "start of frame": <code>{@link GaiaPacketBREDR#SOF SOF} =
         *     0xFF</code>.</li>
         *     <li>Gets the expected length of the GAIA packet using the bytes which follow SOF: flags and length.</li>
         *     <li>For each byte of a packet, copies the byte in the data array until it reaches the
         *     expectedLength.</li>
         *     <li>Calls {@link #onDataFound(byte[]) onDataFound} when the number of accumulated data reaches the
         *     expected length.</li>
         * </ol></p>
         *
         * @param data
         *          The data to analyse in order to build GAIA packet(s).
         */
        private void analyse(byte[] data) {
            int length = data.length;

            // go through the received data
            //noinspection ForLoopReplaceableByForEach // it is more efficient to not use foreach
            for (int i = 0; i < length; ++i) {
                // has started to get data of a GAIA packet
                if ((this.mmReceivedLength > 0) && (this.mmReceivedLength < GaiaPacketBREDR.MAX_PACKET)) {
                    // gets the data
                    mmData[this.mmReceivedLength] = data[i];

                    // gets the flags to know if there is a checksum which has impact on the GAIA packet length
                    if (this.mmReceivedLength == GaiaPacketBREDR.OFFSET_FLAGS)  { // = 2
                        mmFlags = data[i];
                    }
                    // gets the expected length
                    else if (this.mmReceivedLength == GaiaPacketBREDR.OFFSET_LENGTH) { // = 3
                        mmExpectedLength = data[i] // payload length
                                + GaiaPacketBREDR.OFFSET_PAYLOAD // number of bytes before
                                + (((mmFlags & GaiaPacketBREDR.FLAG_CHECK_MASK) != 0) ? 1 : 0);
                    }

                    // number of received bytes can be incremented
                    ++this.mmReceivedLength;

                    // if GAIA packet is complete, it is dispatched
                    if (this.mmReceivedLength == mmExpectedLength) {
                        byte[] packet = new byte[mmReceivedLength];
                        System.arraycopy(mmData, 0, packet, 0, mmReceivedLength);
                        reset();
                        onGAIAPacketFound(packet);
                    }
                }
                // look for the start of frame
                else if (data[i] == GaiaPacketBREDR.SOF) {
                    this.mmReceivedLength = 1;
                }
                // number of received bytes is too big for a GAIA packet
                else if (mmReceivedLength >= GaiaPacketBREDR.MAX_PACKET) {
                    Log.w(TAG, "Packet is too long: received length is bigger than the maximum length of a GAIA " +
                            "packet. Resetting analyser.");
                    reset();
                }
            }
        }

    }
}
