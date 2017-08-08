/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;

import com.qualcomm.libraries.vmupgrade.codes.ReturnCodes;
import com.qualcomm.libraries.vmupgrade.packet.VMUException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class provides information for errors which occur during the Upgrade process.</p>
 */
@SuppressWarnings("unused")
public class UpgradeError {

    /**
     * <p>The type of the error.</p>
     */
    private final @ErrorTypes int mError;
    /**
     * <p>If the error occurs on the board, the ReturnCode it provided.</p>
     */
    private final @ReturnCodes.Enum int mBoardError;
    /**
     * If the error is coming from an exception.
     */
    private final VMUException mException;

    /**
     * <p>To build a simple VMU Error based on its error type.</p>
     *
     * @param type
     *         The type of the error.
     */
    public UpgradeError(@ErrorTypes int type) {
        this.mError = type;
        this.mBoardError = ReturnCodes.Enum.UNKNOWN_ERROR;
        this.mException = null;
    }

    /**
     * <p>To build a {@link UpgradeError.ErrorTypes#RECEIVED_ERROR_FROM_BOARD RECEIVED_ERROR_FROM_BOARD} VMU error
     * object.</p>
     * <p>As it is not possible to have a second constructor with only one int parameter even with a different
     * annotation constraint - the {@link com.qualcomm.libraries.vmupgrade.codes.ReturnCodes.Enum ReturnCodes enumeration}
     * instead of {@link ErrorTypes ErrorTypes enumeration}. This method constraints the choice of the type to
     * {@link ErrorTypes#RECEIVED_ERROR_FROM_BOARD RECEIVED_ERROR_FROM_BOARD} only.</p>
     *
     * @param type
     *         The type of the error, expected type:
     *         {@link ErrorTypes#RECEIVED_ERROR_FROM_BOARD RECEIVED_ERROR_FROM_BOARD}.
     *
     * @param boardError
     *         The error which occurs on the board.
     */
    public UpgradeError(@IntRange(from=ErrorTypes.RECEIVED_ERROR_FROM_BOARD,to=ErrorTypes.RECEIVED_ERROR_FROM_BOARD)
                        @ErrorTypes int type, @ReturnCodes.Enum int boardError) {
        this.mError = type;
        this.mBoardError = boardError;
        this.mException = null;
    }

    /**
     * <p>To build a {@link UpgradeError.ErrorTypes#EXCEPTION EXCEPTION} VMU Error object.</p>
     *
     * @param exception
     *         The VMUException which occurs during the upgrade process.
     */
    public UpgradeError(VMUException exception) {
        this.mError = ErrorTypes.EXCEPTION;
        this.mBoardError = ReturnCodes.Enum.UNKNOWN_ERROR;
        this.mException = exception;
    }

    /**
     * <p>To get the error type for this object.</p>
     *
     * @return the defined error type for this VMU error object.
     */
    public @ErrorTypes int getError() {
        return mError;
    }

    /**
     * <p>To get the return code the board sent.</p> <p>If the error type is not {@link
     * ErrorTypes#RECEIVED_ERROR_FROM_BOARD RECEIVED_ERROR_FROM_BOARD} the returned error is {@link
     * com.qualcomm.libraries.vmupgrade.codes.ReturnCodes.Enum#UNKNOWN_ERROR UNKNOWN_ERROR}.</p>
     *
     * @return the return code associated with this VMU Error.
     */
    public @ReturnCodes.Enum int getReturnCode() {
        return mBoardError;
    }

    /**
     * <p>To get the Exception associated with this VMU Error.</p> <p>If the error type is not {@link
     * ErrorTypes#EXCEPTION EXCEPTION}, this method returns null.</p>
     *
     * @return the Exception associated with this VMU Error.
     */
    public VMUException getException() {
        return mException;
    }

    /**
     * <p>All different errors which can occur when using the vumpgrade library.</p>
     */
    @IntDef(flag = true, value = {ErrorTypes.ERROR_BOARD_NOT_READY, ErrorTypes.WRONG_DATA_PARAMETER, ErrorTypes
            .RECEIVED_ERROR_FROM_BOARD, ErrorTypes.EXCEPTION, ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING,
            ErrorTypes.NO_FILE })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface ErrorTypes {

        /**
         * <p>This error occurs when there is an attempt to start the upgrade but the board is not ready to process.</p>
         */
        int ERROR_BOARD_NOT_READY = 1;
        /**
         * <p>This error occurs when a received VMU packet from the board does not match the expected data: too much
         * information, not enough, etc.</p>
         */
        int WRONG_DATA_PARAMETER = 2;
        /**
         * <p>This error occurs when the board notifies that an error or a warning occurs internally during its upgrade
         * process.</p>
         */
        int RECEIVED_ERROR_FROM_BOARD = 3;
        /**
         * <p>This error is reported when a {@link VMUException VMUException} occurs during the process.</p>
         */
        int EXCEPTION = 4;
        /**
         * <p>This error occurs when there is an attempt to start the upgrade but the VMUManager is already processing an upgrade
         * .</p>
         */
        int AN_UPGRADE_IS_ALREADY_PROCESSING = 5;
        /**
         * <p>This error occurs when the file to upload is empty or does not exist.</p>
         */
        int NO_FILE = 6;
    }

    /**
     * <p>To get a human readable information for this error object.</p>
     *
     * @return A message built on all information contains in the error object.
     */
    public String getString() {
        switch (mError) {
            case ErrorTypes.ERROR_BOARD_NOT_READY:
                return "The board is not ready to process an upgrade.";

            case ErrorTypes.WRONG_DATA_PARAMETER:
                return "The board does not send the expected parameter(s).";

            case ErrorTypes.RECEIVED_ERROR_FROM_BOARD:
                return "An error occurs on the board during the upgrade process."
                        + "\n\t- Received error code: " + VMUUtils.getHexadecimalString(mBoardError)
                        + "\n\t- Received error message: " + ReturnCodes.getReturnCodesMessage(mBoardError);

            case ErrorTypes.EXCEPTION:
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("An Exception has occurred");
                if (mException != null) strBuilder.append(": ").append(mException.toString());
                return strBuilder.toString();

            case ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING:
                return "Attempt to start an upgrade failed: an upgrade is already processing.";

            case ErrorTypes.NO_FILE:
                return "The provided file is empty or does not exist.";

            default:
                return "An error has occurred during the upgrade process.";
        }
    }
}
