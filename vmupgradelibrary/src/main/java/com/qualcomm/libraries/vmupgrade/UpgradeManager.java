/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.libraries.vmupgrade;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.libraries.vmupgrade.codes.OpCodes;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;
import com.qualcomm.libraries.vmupgrade.codes.ReturnCodes;
import com.qualcomm.libraries.vmupgrade.packet.VMUException;
import com.qualcomm.libraries.vmupgrade.packet.VMUPacket;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class manages all the default processes of the VM Upgrade protocol. It defines and builds the VMU messages
 * which have to be sent and manages the ones which have been received.</p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UpgradeManager {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "UpgradeManager";
    /**
     * The listener which implements this object to communicate with.
     */
    private final UpgradeManagerListener mListener;
    /**
     * To know if the upgrade process is currently running.
     */
    private boolean isUpgrading = false;
    /**
     * To know how many times we try to start the upgrade.
     */
    private int mStartAttempts = 0;
    /**
     * The offset to use to upload data on the device.
     */
    private int mStartOffset = 0;
    /**
     * The file to upload on the device.
     */
    private byte[] mBytesFile;
    /**
     * The maximum value for the data length of a VM upgrade packet.
     */
    private final int MAX_DATA_LENGTH;
    /**
     * To know if the packet with the operation code "UPGRADE_DATA" which was sent was the last packet to send.
     */
    private boolean wasLastPacket = false;
    /**
     * The value of the actual resume point to display to the user.
     */
    private @ResumePoints.Enum int mResumePoint;
    /**
     * The file to upload on the board.
     */
    private File mFile;
    /**
     * To know when the transfer starts
     */
    private long mTimeStartTransfer = 0;
    /**
     * To know if we have to disconnect after any event which occurs as a fatal error from the board.
     */
    private boolean hasToAbort = false;
    /**
     * The handler to run some tasks.
     */
    private final Handler mHandler = new Handler();
    /**
     * The number of bytes this manager has still to send after the device has sent a
     * {@link com.qualcomm.libraries.vmupgrade.codes.OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} request.
     */
    private int mBytesToSend = 0;
    /**
     * <p>To show the debug logs indicating when a method had been reached.</p>
     */
    private boolean mShowDebugLogs = false;

    private boolean hasToRestartUpgrade = false;


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows initialisation of a listener to send messages to a device or
     * dispatch any GAIA received messages.</p>
     *
     * @param listener
     *            An object which implements the
     *            {@link UpgradeManagerListener} interface.
     * @param maxLength
     *            The maximum length the VMU messages can have - depends in general on the communication protocols.
     *            <i>For example for BLE and GAIA, the maximum could be 16.</i>
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    // maxLength is always set for BLE packets for this application
    public UpgradeManager(@NonNull UpgradeManagerListener listener, int maxLength) {
        this.mListener = listener;
        this.MAX_DATA_LENGTH =  maxLength - VMUPacket.REQUIRED_INFORMATION_LENGTH;
    }


    // ====== PUBLIC METHODS ==========================================================================

    public void setFile (File file) {
        mFile = file;
    }

    /**
     * <p>To allow the display of the debug logs.</p>
     * <p>They give complementary information on any call of a method.
     * They can indicate that a method is reached but also the action the method does.</p>
     *
     * @param show
     *          True to show the debug logs, false otherwise.
     */
    public void showDebugLogs(boolean show) {
        mShowDebugLogs = show;
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
    }

    /**
     * <p>This method is the entry point to start the upgrade process. Prior to calling this method the file to use to
     * upgrade the Device has to be defined through the {@link UpgradeManager#setFile(File) setFile} method.</p>
     * <p>This method can dispatch a VMUError object if the manager has not been able to start the upgrade process.</p>
     * <p>The possible {@link UpgradeError.ErrorTypes ErrorTypes} are the following:
     * <ul>
     *     <li>{@link UpgradeError.ErrorTypes#AN_UPGRADE_IS_ALREADY_PROCESSING
     *     AN_UPGRADE_IS_ALREADY_PROCESSING}</li>
     *     <li>{@link UpgradeError.ErrorTypes#EXCEPTION EXCEPTION} with the following
     *     possible exceptions:
     *     <ul>
     *         <li>{@link VMUException.Type#GET_BYTES_FILE_FAILED
     *         GET_BYTES_FILE_FAILED}</li>
     *         <li>{@link VMUException.Type#FILE_TOO_BIG FILE_TOO_BIG}</li>
     *     </ul></li>
     * </ul></p>
     */
    @SuppressWarnings("WeakerAccess")
    public void startUpgrade() {
        if (!isUpgrading && mFile != null) {
            isUpgrading = true;
            resetUpload();

            try {
                mBytesFile = VMUUtils.getBytesFromFile(mFile);
            } catch (VMUException exception) {
                UpgradeError error = new UpgradeError(exception);
                Log.e(TAG, "Error occurs when attempt to start the process: " + error.getString());
                mListener.onUpgradeProcessError(error);
                return;
            }

            sendSyncReq();
        }
        else if (isUpgrading) {
            mListener.onUpgradeProcessError(new UpgradeError(UpgradeError.ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING));
        }
        else {
            // mFile == null
            mListener.onUpgradeProcessError(new UpgradeError(UpgradeError.ErrorTypes.NO_FILE));
        }
    }

    /**
     * <p>If an upgrade is processing, to resume it after a disconnection of the process, this method should be
     * called to restart the existing running process.</p>
     *
     * @return true if the manager has been able to resume an upgrade.
     */
    @SuppressWarnings("UnusedReturnValue") // the return value is used for some implementations
    public boolean resumeUpgrade() {
        if (isUpgrading) {
            resetUpload();
            sendSyncReq();
        }

        return isUpgrading;
    }

    /**
     * <p>To know if there is an existing upgrade process running.</p>
     *
     * @return true if an upgrade has been started and not ended - successfully or not.
     */
    public boolean isUpgrading() {
        return isUpgrading;
    }

    /**
     * <p>This method allows to manage a VM message which has been received.</p>
     * <p>If a received message does not correspond to a VMU packet the upgrade process is aborted.</p>
     *
     * @param bytes
     *            The received byte array.
     */
    public void receiveVMUPacket(byte[] bytes) {
        try {
            VMUPacket packet = new VMUPacket(bytes);
            if (isUpgrading || packet.getOpCode() == OpCodes.Enum.UPGRADE_ABORT_CFM) {
                if (mShowDebugLogs) {
                    Log.d(TAG, "Received " + OpCodes.getString(packet.getOpCode()) + ": " +
                            VMUUtils.getHexadecimalStringFromBytes(packet.getData()));
                }
                handleVMUPacket(packet);
            }
            else {
                Log.w(TAG, "Received VMU packet while application is not upgrading anymore, opcode received: " +
                        OpCodes.getString(packet.getOpCode()));
            }
        } catch (VMUException exception) {
            UpgradeError error = new UpgradeError(exception);
            startAbortion(error);
        }
    }

    /**
     * This method is called when we received a successful acknowledgment from the board for a VMU packet which has
     * been sent.
     Some of the VMU packets don't not have an answer from the board. To continue the process the manager has to know
     they have been successfully received by the board.
     */
    public void receiveVMControlSucceed() {
        if (wasLastPacket) {
            if (mResumePoint == ResumePoints.Enum.DATA_TRANSFER) {
                wasLastPacket = false;
                setResumePoint(ResumePoints.Enum.VALIDATION);
                sendValidationDoneReq();
            }
        }
        else if (hasToAbort) {
            hasToAbort = false;
            abortUpgrade();
        }
        else if (mBytesToSend > 0 && mResumePoint == ResumePoints.Enum.DATA_TRANSFER) {
            sendNextDataPacket();
        }
    }

    /**
     * <p>To abort the upgrade.</p>
     */
    @SuppressWarnings("WeakerAccess")
    public void abortUpgrade() {
        if (isUpgrading) {
            sendAbortReq();
            isUpgrading = false;
        }
    }

    /**
     * <p>This method is called by the listener after
     * {@link UpgradeManagerListener#askConfirmationFor(int) askConfirmationFor} has been called. It allows
     * continuation of the upgrade process depending on the confirmation choice.</p>
     *
     *
     * @param type
     *              The type of confirmation which has been requested.
     * @param confirmation
     *              To confirm the request should happen.
     */
    public void sendConfirmation(@ConfirmationType int type, boolean confirmation) {
        switch (type) {
            case ConfirmationType.TRANSFER_COMPLETE:
                sendTransferCompleteReq(confirmation);
                if (!confirmation) hasToAbort = true;
                break;

            case ConfirmationType.COMMIT:
                sendCommitCFM(confirmation);
                if (!confirmation) hasToAbort = true;
                break;

            case ConfirmationType.IN_PROGRESS:
                sendInProgressRes(confirmation);
                if (!confirmation) {
                    abortUpgrade();
                }
                break;

            case ConfirmationType.BATTERY_LOW_ON_DEVICE:
                if (confirmation) {
                    sendSyncReq();
                }
                else {
                    abortUpgrade();
                }
                break;

            case ConfirmationType.WARNING_FILE_IS_DIFFERENT:
                hasToRestartUpgrade = confirmation;
                sendAbortReq();
                break;
        }
    }

    /**
     * <p>To get the current resume point.</p>
     *
     * @return The current resume point.
     */
    public @ResumePoints.Enum int getResumePoint() {
        return mResumePoint;
    }

    /**
     * This method is called when we received a successful acknowledgment from the board for disconnecting from the
     * upgrade process.
     */
    public void receiveVMDisconnectSucceed() {
        if (hasToRestartUpgrade) {
            hasToRestartUpgrade = false;
            startUpgrade();
        }
    }


    // ====== PRIVATE METHODS ==========================================================================

    /**
     * <p>To send a VMUPacket through the listener.</p>
     *
     * @param packet
     *          The packet to send.
     */
    private void sendVMUPacket(VMUPacket packet) {
        byte[] bytes = packet.getBytes();
        if (isUpgrading) {
            if (mShowDebugLogs)
                Log.d(TAG, "send " + OpCodes.getString(packet.getOpCode()) + ": " +
                        VMUUtils.getHexadecimalStringFromBytes(bytes));
            mListener.sendUpgradePacket(bytes);
        }
        else {
            Log.w(TAG, "Sending failed as application is no longer upgrading for opcode: " + OpCodes.getString
                    (packet.getOpCode()));
        }
    }

    /**
     * <p>When an error occurs during the process on the Android application side, this method is called to initiate
     * the abortion by informing the board the application would like to abort.</p>
     *
     * @param error
     *          The error which occurs.
     */
    private void startAbortion(UpgradeError error) {
        String strBuilder = "Error occurs during upgrade process: " + error.getString() +
                "\nStart abortion...";
        Log.e(TAG, strBuilder);
        mListener.onUpgradeProcessError(error);
        abortUpgrade();
    }

    /**
     * To define the actual resume point and to display the dialog which provides the information to the user.
     *
     * @param point
     *              The resume point ot define as the actual one.
     */
    private void setResumePoint(@ResumePoints.Enum int point) {
        mResumePoint = point;
        mListener.onResumePointChanged(point);
    }

    /**
     * <p>To continue the process this manager needs the listener to confirm it.</p>
     *
     * @param type
     *          <p>The type of confirmation to request to the listener.</p>
     */
    private void askForConfirmation(@ConfirmationType int type) {
        mListener.askConfirmationFor(type);
    }

    /**
     * <p>To stop the upgrade process.</p>
     */
    private void stopUpgrade() {
        isUpgrading = false;
        mListener.disconnectUpgrade();
    }

    /**
     * <p>To reset the file transfer.</p>
     */
    private void resetUpload() {
        mStartAttempts = 0;
        mBytesToSend = 0;
        mStartOffset = 0;
        mTimeStartTransfer = 0;
    }

    /**
     * To calculate the remaining time and percentage of upload done, and inform the listener about them.
     */
    private void onFileUploadProgress () {
        double percentage = mStartOffset * 100.0 / mBytesFile.length;

        if (mStartOffset > 0) {
            if (mTimeStartTransfer == 0) {
                mTimeStartTransfer = System.currentTimeMillis();
            }

            long remainingTime = (System.currentTimeMillis() - mTimeStartTransfer)
                    * (mBytesFile.length - mStartOffset)
                    / mStartOffset;
            UploadProgress progress = new UploadProgress(percentage, remainingTime);
            mListener.onFileUploadProgress(progress);
        }
    }

    /**
     * To send the next data packet depending on the number of bytes requested by the Device through its last
     * {@link com.qualcomm.libraries.vmupgrade.codes.OpCodes.Enum#UPGRADE_DATA_BYTES_REQ} request.
     */
    private void sendNextDataPacket() {
        // inform listeners about evolution
        onFileUploadProgress();

        int bytesToSend = mBytesToSend < MAX_DATA_LENGTH-1 ? mBytesToSend : MAX_DATA_LENGTH-1;

        // to know if we are sending the last data packet.
        boolean lastPacket = mBytesFile.length-mStartOffset <= bytesToSend;

        // we send the data
        byte[] dataToSend = new byte[bytesToSend];
        System.arraycopy(mBytesFile, mStartOffset, dataToSend, 0, dataToSend.length);

        // to reinitialize variables or increment variables
        if (lastPacket) {
            wasLastPacket = true;
            mBytesToSend = 0;
        }
        else {
            mStartOffset += bytesToSend;
            mBytesToSend -= bytesToSend;
        }

        sendData(lastPacket, dataToSend);
    }


    // ====== PROTECTED METHODS FOR UPGRADE PROCESS WHICH CAN BE OVERRIDE =========================================

    /**
     * To send a {@link OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} message.
     */
    private void sendSyncReq () {
        // send the MD5 information here
        byte[] md5Checksum = VMUUtils.getMD5FromFile(mFile);
        int identifierLength = OpCodes.UpgradeSyncREQ.IDENTIFIER_LENGTH;
        byte[] data = new byte[OpCodes.UpgradeSyncREQ.DATA_LENGTH];

        if (md5Checksum.length >= identifierLength) {
            // the checksum contains more bytes than we need: the request only needs to send the checksum last bytes
            System.arraycopy(md5Checksum, md5Checksum.length-identifierLength, data, OpCodes.UpgradeSyncREQ
                    .IDENTIFIER_OFFSET, identifierLength);
        }
        else if (md5Checksum.length > 0) {
            // all bytes of the checksum should be sent
            System.arraycopy(md5Checksum, 0, data, OpCodes.UpgradeSyncREQ.IDENTIFIER_OFFSET, md5Checksum.length);
        }
        // otherwise the checksum is empty, so the id to send is 0
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_SYNC_REQ, data);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_START_REQ message.
     */
    private void sendStartReq () {
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_START_REQ);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_START_DATA_REQ message.
     */
    private void sendStartDataReq () {
        setResumePoint(ResumePoints.Enum.DATA_TRANSFER);
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_START_DATA_REQ);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_DATA packet.
     *
     * @param lastPacket
     *                  to know if we are sending the last packet for the data.
     * @param data
     *                  the data to send inside this packet.
     */
    private void sendData (boolean lastPacket, byte[] data) {
        byte[] dataToSend = new byte[data.length+1];
        dataToSend[OpCodes.UpgradeData.LAST_PACKET_OFFSET] = lastPacket ? OpCodes.UpgradeData.LastPacket.IS_LAST_PACKET :
                OpCodes.UpgradeData.LastPacket.IS_NOT_LAST_PACKET;
        System.arraycopy(data, 0, dataToSend, OpCodes.UpgradeData.FILE_BYTES_OFFSET, data.length);
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_DATA, dataToSend);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_IS_VALIDATION_DONE_REQ message.
     */
    private void sendValidationDoneReq () {
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_IS_VALIDATION_DONE_REQ);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_TRANSFER_COMPLETE_RES packet.
     *
     * @param process
     *              To confirm the process should continue, false to abort the process.
     */
    private void sendTransferCompleteReq(boolean process) {
        byte[] data = new byte[OpCodes.UpgradeTransferCompleteRES.DATA_LENGTH];
        data[OpCodes.UpgradeTransferCompleteRES.ACTION_OFFSET] = process ? OpCodes.UpgradeTransferCompleteRES.Action.CONTINUE :
                OpCodes.UpgradeTransferCompleteRES.Action.ABORT;
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_TRANSFER_COMPLETE_RES, data);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_IN_PROGRESS_RES packet.
     */
    private void sendInProgressRes(boolean process) {
        byte[] data = new byte[OpCodes.UpgradeInProgressRES.DATA_LENGTH];
        data[OpCodes.UpgradeInProgressRES.ACTION_OFFSET] = process ? OpCodes.UpgradeInProgressRES.Action.CONTINUE :
                OpCodes.UpgradeInProgressRES.Action.ABORT;
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_IN_PROGRESS_RES, data);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_COMMIT_CFM packet.
     *
     * @param process
     *              To confirm the process should continue, false to abort the process.
     */
    private void sendCommitCFM(boolean process) {
        byte[] data = new byte[OpCodes.UpgradeCommitCFM.DATA_LENGTH];
        data[OpCodes.UpgradeCommitCFM.ACTION_OFFSET] = process ? OpCodes.UpgradeCommitCFM.Action.CONTINUE :
                OpCodes.UpgradeCommitCFM.Action.ABORT;
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_COMMIT_CFM, data);
        sendVMUPacket(packet);
    }

    /**
     * To send a message to abort the upgrade.
     */
    private void sendAbortReq() {
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_ABORT_REQ);
        sendVMUPacket(packet);
    }

    /**
     * To send an UPGRADE_ERROR_WARN_RES packet.
     *
     * @param data
     *              the error code we received and we want to return to the device as an acknowledgment.
     */
    private void sendErrorConfirmation(byte[] data) {
        VMUPacket packet = new VMUPacket(OpCodes.Enum.UPGRADE_ERROR_WARN_RES, data);
        sendVMUPacket(packet);
    }

    /**
     * To manage the reception of a message about the VM upgrade.
     *
     * @param packet
     *            the received packet.
     */
    @SuppressLint("SwitchIntDef") // not all operation codes can be found in the VMU packets received from the Device
    private void handleVMUPacket(VMUPacket packet) {

        switch (packet.getOpCode()) {
            case OpCodes.Enum.UPGRADE_SYNC_CFM:
                receiveSyncCFM(packet);
                break;

            case OpCodes.Enum.UPGRADE_START_CFM:
                receiveStartCFM(packet);
                break;

            case OpCodes.Enum.UPGRADE_DATA_BYTES_REQ:
                receiveDataBytesREQ(packet);
                break;

            case OpCodes.Enum.UPGRADE_ABORT_CFM:
                receiveAbortCFM();
                break;

            case OpCodes.Enum.UPGRADE_ERROR_WARN_IND:
                receiveErrorWarnIND(packet);
                break;

            case OpCodes.Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                receiveValidationDoneCFM(packet);
                break;

            case OpCodes.Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                receiveTransferCompleteIND();
                break;

            case OpCodes.Enum.UPGRADE_COMMIT_REQ:
                receiveCommitREQ();
                break;

            case OpCodes.Enum.UPGRADE_COMPLETE_IND:
                receiveCompleteIND();
                break;
        }
    }

    /**
     * To manage errors received during VM upgrade.
     *
     * @param packet
     *            The received packet.
     */
    private void receiveErrorWarnIND(VMUPacket packet) {
        byte[] data = packet.getData();
        sendErrorConfirmation(data); // immediate answer, data is the same as the received one.

        short code = VMUUtils.extractShortFromByteArray(data, OpCodes.UpgradeErrorWarnIND.RETURN_CODE_OFFSET,
                OpCodes.UpgradeErrorWarnIND.RETURN_CODE_LENGTH, false);
        @ReturnCodes.Enum int returnCode = ReturnCodes.getReturnCode(code);

        if (returnCode == ReturnCodes.Enum.WARN_SYNC_ID_IS_DIFFERENT) {
            askForConfirmation(ConfirmationType.WARNING_FILE_IS_DIFFERENT);
        }
        else if (returnCode == ReturnCodes.Enum.ERROR_BATTERY_LOW) {
            askForConfirmation(ConfirmationType.WARNING_FILE_IS_DIFFERENT);
        }
        else {
            UpgradeError vmuError = new UpgradeError(UpgradeError.ErrorTypes.RECEIVED_ERROR_FROM_BOARD, returnCode);
            startAbortion(vmuError);
        }

    }

    /**
     * This method is called when we received an UPGRADE_SYNC_CFM message. This method starts the next step which is
     * sending an UPGRADE_START_REQ message.
     */
    private void receiveSyncCFM(VMUPacket packet) {
        byte[] data = packet.getData();
        if (data.length >= OpCodes.UpgradeSyncCFM.DATA_LENGTH) {
            @ResumePoints.Enum int step = ResumePoints.getResumePoint(data[OpCodes.UpgradeSyncCFM.RESUME_POINT_OFFSET]);
            //noinspection UnusedAssignment
            int identifier = VMUUtils.extractIntFromByteArray(data, OpCodes.UpgradeSyncCFM.IDENTIFIER_OFFSET, OpCodes.UpgradeSyncCFM.IDENTIFIER_LENGTH, false);
            //noinspection UnusedAssignment
            byte protocolVersion = data[OpCodes.UpgradeSyncCFM.PROTOCOL_VERSION_OFFSET];
            setResumePoint(step);
        }
        else {
            setResumePoint(ResumePoints.Enum.DATA_TRANSFER);
        }
        sendStartReq();
    }

    /**
     * This method is called when we received an UPGRADE_START_CFM message. This method reads the message and starts the
     * next step which is sending an UPGRADE_START_DATA_REQ message, or aborts the upgrade depending on the received
     * message.
     *
     * @param packet
     *            The received packet.
     */
    private void receiveStartCFM(VMUPacket packet) {
        byte[] data = packet.getData();

        // the packet has to have a content.
        if (data.length >= OpCodes.UpgradeStartCFM.DATA_LENGTH) {
            // to get the battery level
            //noinspection UnusedAssignment
            short batteryLevel = VMUUtils.extractShortFromByteArray(data, OpCodes.UpgradeStartCFM
                    .BATTERY_LEVEL_OFFSET, OpCodes.UpgradeStartCFM.BATTERY_LEVEL_LENGTH, false);

            if (data[OpCodes.UpgradeStartCFM.STATUS_OFFSET] == OpCodes.UpgradeStartCFM.Status.SUCCESS) {
                mStartAttempts = 0;
                // the device is ready for the upgrade, we can go to the resume point or to the upgrade beginning.
                switch (mResumePoint) {
                    case ResumePoints.Enum.COMMIT:
                        askForConfirmation(ConfirmationType.COMMIT);
                        break;
                    case ResumePoints.Enum.TRANSFER_COMPLETE:
                        askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
                        break;
                    case ResumePoints.Enum.IN_PROGRESS:
                        askForConfirmation(ConfirmationType.IN_PROGRESS);
                        break;
                    case ResumePoints.Enum.VALIDATION:
                        sendValidationDoneReq();
                        break;
                    case ResumePoints.Enum.DATA_TRANSFER:
                    default:
                        sendStartDataReq();
                        break;
                }
            }
            else if (data[OpCodes.UpgradeStartCFM.STATUS_OFFSET] == OpCodes.UpgradeStartCFM.Status.ERROR_APP_NOT_READY) {
                int START_ATTEMPTS_MAX = 5;
                int START_ATTEMPTS_TIME = 2000;

                if (mStartAttempts < START_ATTEMPTS_MAX) {
                    // device not ready we will ask it again.
                    mStartAttempts++;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendStartReq();
                        }
                    }, START_ATTEMPTS_TIME);
                }
                else {
                    mStartAttempts = 0;
                    UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.ERROR_BOARD_NOT_READY);
                    startAbortion(error);
                }
            }
            else {
                UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
                startAbortion(error);
            }
        }
        else {
            UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
            startAbortion(error);
        }
    }

    /**
     * This method is called when we received an UPGRADE_DATA_BYTES_REQ message. We manage this packet and use it for the
     * next step which is to upload the file on the device using UPGRADE_DATA messages.
     *
     * @param packet
     *            The received packet.
     */
    private void receiveDataBytesREQ(VMUPacket packet) {
        byte[] data = packet.getData();

        // Checking the data has the good length
        if (data.length == OpCodes.UpgradeDataBytesREQ.DATA_LENGTH) {

            // retrieving information from the received packet
            mBytesToSend = VMUUtils.extractIntFromByteArray(data, OpCodes.UpgradeDataBytesREQ.NB_BYTES_OFFSET,
                    OpCodes.UpgradeDataBytesREQ.NB_BYTES_LENGTH, false);
            int fileOffset = VMUUtils.extractIntFromByteArray(data, OpCodes.UpgradeDataBytesREQ.FILE_OFFSET_OFFSET,
                    OpCodes.UpgradeDataBytesREQ.FILE_OFFSET_LENGTH, false);

            // we check the value for the offset
            mStartOffset += (fileOffset > 0 && fileOffset+mStartOffset < mBytesFile.length) ? fileOffset : 0;

            // if the asked length doesn't fit with possibilities we use the maximum length we can use.
            mBytesToSend = (mBytesToSend > 0) ? mBytesToSend : 0;
            // if the requested length will look for bytes out of the array we reduce it to the remaining length.
            int remainingLength = mBytesFile.length - mStartOffset;
            mBytesToSend = (mBytesToSend < remainingLength) ? mBytesToSend : remainingLength;

            sendNextDataPacket();
        }
        else {
            UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
            startAbortion(error);
        }
    }

    /**
     * This method is called when we received an UPGRADE_IS_VALIDATION_DONE_CFM message. We manage this packet and use
     * it for the next step which is to send an UPGRADE_IS_VALIDATION_DONE_REQ.
     */
    private void receiveValidationDoneCFM(VMUPacket packet) {
        byte[] data = packet.getBytes();
        if (data.length == OpCodes.UpgradeIsValidationDoneCFM.DATA_LENGTH) {
            long time = VMUUtils.extractLongFromByteArray(data, OpCodes.UpgradeIsValidationDoneCFM
                    .WAITING_TIME_OFFSET, OpCodes.UpgradeIsValidationDoneCFM.WAITING_TIME_LENGTH, false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendValidationDoneReq();
                }
            }, time);
        }
        else {
            sendValidationDoneReq();
        }
    }

    /**
     * This method is called when we received an UPGRADE_TRANSFER_COMPLETE_IND message. We manage this packet and use it for the
     * next step which is to send a validation to continue the process or to abort it temporally - it will be done later.
     */
    private void receiveTransferCompleteIND() {
        setResumePoint(ResumePoints.Enum.TRANSFER_COMPLETE);
        askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
    }

    /**
     * This method is called when we received an UPGRADE_COMMIT_RES message. We manage this packet and use it for the
     * next step which is to send a validation to continue the process or to abort it temporally - it will be done later.
     */
    private void receiveCommitREQ() {
        setResumePoint(ResumePoints.Enum.COMMIT);
        askForConfirmation(ConfirmationType.COMMIT);
    }

    /**
     * This method is called when we received an UPGRADE_COMPLETE_IND message.
     */
    private void receiveCompleteIND() {
        isUpgrading = false;
        mListener.onUpgradeFinished();
    }

    /**
     * This method is called when we received an UPGRADE_ABORT_CFM message after we asked for an abort to the upgrade process.
     */
    private void receiveAbortCFM() {
        stopUpgrade();
    }


    // ====== INTERFACES ===========================================================================

    /**
     * <p>This interface allows this manager to dispatch messages or event to a listener.</p>
     */
    @SuppressWarnings("unused")
    public interface UpgradeManagerListener {
        /**
         * <p>To send a VMUPacket over the defined protocol communication.</p>
         *
         * @param bytes
         *              The packet to send.
         */
        void sendUpgradePacket(byte[] bytes);

        /**
         * <p>Called when an error occurs during the upgrade process on the application. When this method is called
         * the abortion of the process has already started.</p>
         *
         * @param error
         *              The error which occurs during the upgrade process.
         */
        void onUpgradeProcessError(UpgradeError error);

        /**
         * <p>To inform the listener about any progress on the steps of the upgrade process.</p>
         *
         * @param point
         *              The step which is now in process with this manager.
         */
        void onResumePointChanged(@ResumePoints.Enum int point);

        /**
         * <p>This method is called when the upgrade process has successfully ended.</p>
         * <p>This manager has already initiated the stopping of the upgrade by requesting to disconnect the upgrade.</p>
         */
        void onUpgradeFinished();

        /**
         * <p>This method is called during the upload of the file on the board to give information on that upload as
         * the remaining time and the percentage accomplished.</p>
         *
         * @param progress
         *          A progress object which contains the percentage of how many bytes of the file have been sent to
         *          the Board and an estimation of how long this manager will need to upload the rest of the file.
         */
        void onFileUploadProgress(UploadProgress progress);

        /**
         * <p>This method is called when the manager needs the listener to confirm any action before continuing the
         * process.</p>
         * <p>To inform the manager about its decision, the listener has to call the
         * {@link UpgradeManager#sendConfirmation(int, boolean) sendConfirmation} method.</p>
         */
        void askConfirmationFor(@ConfirmationType int type);

        /**
         * <p>When the upgrade has aborted or successfully ended, this method is called to request the listener to
         * close the connection.</p>
         * <p><i>For instance, if the GAIA protocol is used, this method should call the "VM_UPGRADE_DISCONNECT"
         * command and unregister the VMU_PACKET event notification.</i></p>
         */
        void disconnectUpgrade();
    }

    /**
     * <p>All the types of confirmation this manager could request from the listener depending on the messages
     * received from the board.</p>
     */
    @IntDef(flag = true, value = { ConfirmationType.TRANSFER_COMPLETE, ConfirmationType.COMMIT,  ConfirmationType
            .IN_PROGRESS, ConfirmationType.BATTERY_LOW_ON_DEVICE, ConfirmationType.WARNING_FILE_IS_DIFFERENT })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface ConfirmationType {
        /**
         * <p>When the manager receives the
         * {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND} message, the board is asking for a confirmation to
         * {@link OpCodes.UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE}
         * or {@link OpCodes.UpgradeTransferCompleteRES.Action#ABORT ABORT}  the
         * process.</p>
         */
        int TRANSFER_COMPLETE = 1;
        /**
         * <p>When the manager receives the
         * {@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ} message, the
         * board is asking for a confirmation to
         * {@link OpCodes.UpgradeCommitCFM.Action#CONTINUE CONTINUE}
         * or {@link OpCodes.UpgradeCommitCFM.Action#ABORT ABORT}  the process.</p>
         */
        int COMMIT = 2;
        /**
         * <p>When the resume point
         * {@link ResumePoints.Enum#IN_PROGRESS IN_PROGRESS} is reached, the board
         * is expecting to receive a confirmation to
         * {@link OpCodes.UpgradeInProgressRES.Action#CONTINUE CONTINUE}
         * or {@link OpCodes.UpgradeInProgressRES.Action#ABORT ABORT} the process.</p>
         */
        int IN_PROGRESS = 3;
        /**
         * <p>When the Host receives
         * {@link com.qualcomm.libraries.vmupgrade.codes.ReturnCodes.Enum#WARN_SYNC_ID_IS_DIFFERENT WARN_SYNC_ID_IS_DIFFERENT},
         * the listener has to ask if the upgrade should continue or not.</p>
         */
        int WARNING_FILE_IS_DIFFERENT = 4;
        /**
         * <p>>When the Host receives
         * {@link com.qualcomm.libraries.vmupgrade.codes.ReturnCodes.Enum#ERROR_BATTERY_LOW ERROR_BATTERY_LOW},the
         * listener has to ask if the upgrade should continue or not.</p>
         */
        int BATTERY_LOW_ON_DEVICE = 5;
    }

}

