/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.GaiaException;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBLE;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBREDR;
import com.qualcomm.libraries.vmupgrade.UpgradeError;
import com.qualcomm.libraries.vmupgrade.UpgradeManager;
import com.qualcomm.libraries.vmupgrade.UploadProgress;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.io.File;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol in
 * order to process an upgrade using the VM Upgrade protocol.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 */
public class UpgradeGaiaManager extends AGaiaManager implements UpgradeManager.UpgradeManagerListener {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "UpgradeGaiaManager";
    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;
    /**
     * <p>The manager to process the upgrade.</p>
     */
    private final UpgradeManager mUpgradeManager;


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which initialises a listener to send messages to a device or dispatch
     * any GAIA received messages.</p>
     *
     * @param myListener
     *         An object which implements the {@link GaiaManagerListener MyGaiaManagerListener} interface.
     * @param transport
     *          The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public UpgradeGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
        int packetLength = transport == GAIA.Transport.BR_EDR ? GaiaPacketBREDR.MAX_PAYLOAD : GaiaPacketBLE.MAX_PAYLOAD;
        mUpgradeManager = new UpgradeManager(this, packetLength);
        mUpgradeManager.showDebugLogs(Consts.DEBUG);
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>To start the VM Upgrade process. This method sends the {@link GAIA#COMMAND_VM_UPGRADE_CONNECT
     * COMMAND_VM_UPGRADE_CONNECT} command and registers for the {@link GAIA.NotificationEvents#VMU_PACKET VMU_PACKET}
     * event. Once this step has been done and acknowledged, this manager asks the VMU Manager to start its process
     * .</p>
     *
     */
    public void startUpgrade(File file) {
        if (!mUpgradeManager.isUpgrading()) {
            registerNotification(GAIA.NotificationEvents.VMU_PACKET);
            mUpgradeManager.setFile(file);
            sendUpgradeConnect();
        }
    }

    /**
     * <p>To abort an ongoing upgrade.</p>
     */
    public void abortUpgrade() {
        mUpgradeManager.abortUpgrade();
    }

    /**
     * <p>To get the current {@link ResumePoints ResumePoint} of the upgrade process.</p>
     *
     * @return The corresponding ResumePoint. If there is no ongoing upgrade the given ResumePoint is not accurate.
     */
    public @ResumePoints.Enum int getResumePoint() {
        return mUpgradeManager.getResumePoint();
    }

    /**
     * <p>To give an answer to the {@link UpgradeManager UpgradeManager} about a confirmation it is waiting for before
     * continuing the upgrade process.</p>
     *
     * @param type
     *              The type of confirmation the UpgradeManager requested.
     * @param confirmation
     *              To know if the UpgradeManager should continue the process or abort it.
     */
    public void sendConfirmation(@UpgradeManager.ConfirmationType int type, boolean confirmation) {
        if (mUpgradeManager.isUpgrading()) {
            mUpgradeManager.sendConfirmation(type, confirmation);
        }
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void sendUpgradePacket(byte[] bytes) {
        sendUpgradeControl(bytes);
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void onUpgradeProcessError(UpgradeError error) {
        mListener.onUpgradeError(error);
        switch (error.getError()) {
            case UpgradeError.ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING:
            case UpgradeError.ErrorTypes.NO_FILE:
                // no ongoing upgrade to abort
                break;
            case UpgradeError.ErrorTypes.ERROR_BOARD_NOT_READY:
            case UpgradeError.ErrorTypes.EXCEPTION:
            case UpgradeError.ErrorTypes.RECEIVED_ERROR_FROM_BOARD:
            case UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER:
                mUpgradeManager.abortUpgrade();
                break;
        }
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void onResumePointChanged(@ResumePoints.Enum int point) {
        mListener.onResumePointChanged(point);
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void onUpgradeFinished() {
        mListener.onUpgradeFinish();
        disconnectUpgrade();
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void onFileUploadProgress(UploadProgress progress) {
        mListener.onUploadProgress(progress);
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void askConfirmationFor(@UpgradeManager.ConfirmationType int type) {
        mListener.askConfirmation(type);
    }

    @Override // UpgradeManager.UpgradeManagerListener
    public void disconnectUpgrade() {
        cancelNotification(GAIA.NotificationEvents.VMU_PACKET);
        sendUpgradeDisconnect();
    }

    /**
     * <p>To know if there is an upgrade going on.</p>
     *
     * @return true if there is an upgrade working, false otherwise.
     */
    public boolean isUpgrading() {
        return mUpgradeManager.isUpgrading();
    }

    /**
     * <p>Once the Bluetooth connection is suitable to send GAIA messages, this method is called to inform this
     * manager it can start to communicate with a GAIA device.</p>
     * <p>This method will resume any VMU process already started.</p>
     */
    public void onGaiaReady() {
        if (mUpgradeManager.isUpgrading()) {
            registerNotification(GAIA.NotificationEvents.VMU_PACKET);
            sendUpgradeConnect();
        }
    }


    // ====== PRIVATE METHODS - SENDING =============================================================

    /**
     * <p>To send a {@link GAIA#COMMAND_VM_UPGRADE_CONNECT COMMAND_VM_UPGRADE_CONNECT} packet.</p>
     */
    private void sendUpgradeConnect() {
        GaiaPacket packet = createPacket(GAIA.COMMAND_VM_UPGRADE_CONNECT);
        createRequest(packet);
    }

    /**
     * <p>To send a {@link GAIA#COMMAND_VM_UPGRADE_DISCONNECT COMMAND_VM_UPGRADE_DISCONNECT} packet.</p>
     */
    private void sendUpgradeDisconnect() {
        GaiaPacket packet = createPacket(GAIA.COMMAND_VM_UPGRADE_DISCONNECT);
        createRequest(packet);
    }

    /**
     * <p>To send a {@link GAIA#COMMAND_VM_UPGRADE_CONTROL COMMAND_VM_UPGRADE_CONTROL} GAIA packet. That packet contains
     * the bytes of a VM Upgrade packet.</p>
     *
     * @param payload
     *              The bytes which corresponds to a
     *              {@link com.qualcomm.libraries.vmupgrade.packet.VMUPacket VMUPacket}.
     */
    private void sendUpgradeControl(byte[] payload) {
        GaiaPacket packet = createPacket(GAIA.COMMAND_VM_UPGRADE_CONTROL, payload);
        createRequest(packet);
    }

    /**
     * <p>To register for a {@link GAIA.NotificationEvents GAIA event notification}.</p>
     *
     * @param event
     *              The event to register with.
     */
    @SuppressWarnings("SameParameterValue") // the parameter is always VMU_PACKET in this application
    private void registerNotification (@GAIA.NotificationEvents int event) {
        try {
            GaiaPacket packet = GaiaPacketBLE.buildGaiaNotificationPacket(GAIA.VENDOR_QUALCOMM, GAIA
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
    @SuppressWarnings("SameParameterValue") // the parameter is always VMU_PACKET in this application
    private void cancelNotification (@GAIA.NotificationEvents int event) {
        try {
            GaiaPacket packet = GaiaPacketBLE.buildGaiaNotificationPacket(GAIA.VENDOR_QUALCOMM, GAIA
                    .COMMAND_CANCEL_NOTIFICATION, event, null, getTransportType());
            createRequest(packet);
        } catch (GaiaException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    // ====== PRIVATE METHODS - RECEIVING =============================================================

    /**
     * <p>To manage a received {@link GaiaPacket} which has {@link GAIA#COMMAND_EVENT_NOTIFICATION} for command.</p>
     * <p>This manager is only interested by the
     * {@link GAIA.NotificationEvents#VMU_PACKET VMU_PACKET} event to manage a VM Upgrade.</p>
     *
     * @param packet
     *              The receive notification event packet.
     *
     * @return
     *          true if an acknowledgement has been sent.
     */
    private boolean receiveEventNotification(GaiaPacket packet) {
        byte[] payload = packet.getPayload();

        if (payload.length > 0) {
            @GAIA.NotificationEvents int event = packet.getEvent();
            if (event == GAIA.NotificationEvents.VMU_PACKET && mUpgradeManager != null) {
                createAcknowledgmentRequest(packet, GAIA.Status.SUCCESS, null);
                byte[] data = new byte[payload.length - 1];
                System.arraycopy(payload, 1, data, 0, payload.length - 1);
                mUpgradeManager.receiveVMUPacket(data);
                return true;
            }
            else {
                // not supported
                return false;
            }
        }
        else {
            createAcknowledgmentRequest(packet, GAIA.Status.INVALID_PARAMETER, null);
            return true;
        }
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case GAIA.COMMAND_VM_UPGRADE_CONNECT:
                if (mUpgradeManager.isUpgrading()) {
                    mUpgradeManager.resumeUpgrade();
                }
                else {
                    mUpgradeManager.startUpgrade();
                }
                break;
            case GAIA.COMMAND_REGISTER_NOTIFICATION:
            case GAIA.COMMAND_CANCEL_NOTIFICATION:
            case GAIA.COMMAND_EVENT_NOTIFICATION:
                /* we assume that if the VM_UPGRADE commands are supported the NOTIFICATION ones also are as there
                are necessary for the Device to communicate with the Host.*/
                break;
            case GAIA.COMMAND_VM_UPGRADE_DISCONNECT:
                mUpgradeManager.receiveVMDisconnectSucceed();
                mListener.onVMUpgradeDisconnected();
                break;
            case GAIA.COMMAND_VM_UPGRADE_CONTROL:
                mUpgradeManager.receiveVMControlSucceed();
                break;
        }

    }

    @Override // GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
        if (packet.getCommand() == GAIA.COMMAND_VM_UPGRADE_CONNECT
                || packet.getCommand() == GAIA.COMMAND_VM_UPGRADE_CONTROL) {
            sendUpgradeDisconnect();
        }
        else if (packet.getCommand() == GAIA.COMMAND_VM_UPGRADE_DISCONNECT) {
            mListener.onVMUpgradeDisconnected();
        }
    }

    @Override // GaiaManager
    protected void hasNotReceivedAcknowledgementPacket(GaiaPacket packet) {
        if (packet.getCommand() == GAIA.COMMAND_DISCONNECT) {
            mListener.onVMUpgradeDisconnected();
        }
    }

    @Override // GaiaManager
    protected boolean manageReceivedPacket(GaiaPacket packet) {
        switch (packet.getCommand()) {
            case GAIA.COMMAND_EVENT_NOTIFICATION:
                return receiveEventNotification(packet);
        }

        return false;
    }

    @Override // GaiaManager
    protected boolean sendGAIAPacket(byte[] packet) {
        return mListener.sendGAIAUpgradePacket(packet);
    }


    // ====== INTERFACES ===========================================================================

    /**
     * <p>This interface allows this manager to dispatch messages or events to a listener.</p>
     */
    public interface GaiaManagerListener {

        /**
         * <p>This method is called to inform the listener that the VM Upgrade process has been disconnected.</p>
         * <p>This method is called if a {@link GAIA#COMMAND_DISCONNECT COMMAND_DISCONNECT} packet has been sent to
         * the device. Before being called, the manager waits for any ACK to the command or a time out.</p>
         */
        @SuppressWarnings("EmptyMethod") // this method is empty for all its implementation
        void onVMUpgradeDisconnected();

        /**
         * <p>This method is called when there is progress to a new step of the upgrade process.</p>
         *
         * @see ResumePoints
         *
         * @param point
         *              the new step reached by the process.
         */
        void onResumePointChanged(@ResumePoints.Enum int point);

        /**
         * <p>This method informs the listener that an error occurs during the upgrade process.</p>
         * <p>For the following error types, the upgrade is automatically aborted:
         * <ul>
         *     <li>{@link UpgradeError.ErrorTypes#EXCEPTION EXCEPTION}</li>
         *     <li>{@link UpgradeError.ErrorTypes#RECEIVED_ERROR_FROM_BOARD RECEIVED_ERROR_FROM_BOARD}</li>
         *     <li>{@link UpgradeError.ErrorTypes#WRONG_DATA_PARAMETER WRONG_DATA_PARAMETER}</li>
         * </ul></p>
         *
         * @param error
         *          All the information relative to the occurred error.
         */
        void onUpgradeError(UpgradeError error);

        /**
         * <p>This method is called when progress has been made uploading the file to the device.</p>
         * <p>This method is used when the actual step is
         * {@link com.qualcomm.libraries.vmupgrade.codes.ResumePoints.Enum#DATA_TRANSFER DATA_TRANSFER}.</p>
         *
         * @param progress
         *          A progress object which contains the percentage of the bytes of the file that have been sent to
         *          the board and an estimation of how long this manager will need to upload the rest of the file.
         */
        void onUploadProgress(UploadProgress progress);

        /**
         * <p>To send over a communication channel the bytes of a GAIA packet using the GAIA protocol.</p>
         *
         * @param packet
         *          The byte array to send to a device.
         * @return
         *          true if the sending could be done.
         */
        boolean sendGAIAUpgradePacket(byte[] packet);

        /**
         * <p>To inform any listener that the upgrade has ended successfully. This method is called after the last
         * Upgrade packet has been sent by the Device to inform that it has been successfully upgraded.</p>
         */
        void onUpgradeFinish();

        /**
         * <p>This method is called when the manager needs the listener to confirm any action before continuing the
         * process.</p>
         * <p>To inform the manager about its decision, the listener has to call the
         * {@link UpgradeGaiaManager#sendConfirmation(int, boolean) sendConfirmation} method.</p>
         */
        void askConfirmation(@UpgradeManager.ConfirmationType int type);
    }

}
