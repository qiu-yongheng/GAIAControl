/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade.packet;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.qualcomm.libraries.vmupgrade.VMUUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>A VMUException is thrown when a problem occurs with the VMU protocol.</p>
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class VMUException extends Exception {

    /**
     * <p>The type of the VMU exception.</p>
     */
    private final @Type int mType;
    /**
     * The readable message corresponding to this VMUException.
     */
    private final String mMessage;
    /**
     * If the VMUException occurs during the treatment of the bytes, the bytes are provided.
     */
    private final byte[] mBytes;

    /**
     * <p></p>All types of VMU exceptions.</p>
     */
    @IntDef(flag = true, value = { Type.DATA_TOO_SHORT, Type.FILE_TOO_BIG, Type.GET_BYTES_FILE_FAILED })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Type {
        /**
         * <p>The exception occurs when trying to build a VMU packet from a byte array which does not contain the
         * minimum information it should: the operation code and the data length, 3 bytes.</p>
         */
        int DATA_TOO_SHORT = 0;

        /**
         * <p>This exception occurs when trying to build a VMU packet from a byte array for which the given data
         * length is shorter or longer than the actual data.</p>
         * @deprecated when getting the bytes this library now takes all bytes and only displays a warning log about the
         * given length not matching the data length.
         */
        int DATA_LENGTH_ERROR = 1;

        /**
         * <p>This exception occurs when the given file is bigger than 2^32 bytes - it means that it cannot be
         * contained in a bytes array.</p>
         */
        int FILE_TOO_BIG = 2;

        /**
         * <p>This exception occurs when it has not been possible to get the bytes of a file.</p>
         */
        int GET_BYTES_FILE_FAILED = 3;
    }

    /**
     * <p>Constructor for this exception.</p>
     *
     * @param type
     *            the type of this exception.
     */
    public VMUException(@Type int type) {
        super();
        this.mType = type;
        this.mMessage = "";
        this.mBytes = new byte[0];
    }

    /**
     * <p>Constructor for this exception.</p>
     *
     * @param type
     *            the type of this exception.
     *
     * @param message
     *            Any complementary message to the exception type.
     */
    public VMUException(@Type int type, String message) {
        super();
        this.mType = type;
        this.mMessage = message;
        this.mBytes = new byte[0];
    }

    /**
     * <p>Class constructor for this exception.</p>
     *
     * @param type
     *            the type of this exception.
     *
     * @param bytes
     *            complementary information to the type for this exception.
     */
    public VMUException(@Type int type, byte[] bytes) {
        super();
        this.mType = type;
        this.mMessage = "";
        this.mBytes = bytes;
    }

    /**
     * <p>To know the type of this exception.</p>
     *
     * @return the exception type.
     */
    public @Type int getType() {
        return this.mType;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();

        switch (mType) {
        case Type.DATA_TOO_SHORT:
            strBuilder.append("Build of a VMUPacket failed: the byte array does not contain the minimum required " +
                    "information");
            strBuilder.append("\nReceived bytes: ").append(VMUUtils.getHexadecimalStringFromBytes(mBytes));
            break;

        case Type.FILE_TOO_BIG:
            strBuilder.append("Get file failed: The given file size is >= 2GB");
            break;

        case Type.GET_BYTES_FILE_FAILED:
            strBuilder.append("Get file failed");
            if (mMessage.length() > 0) {
                strBuilder.append(": ").append(mMessage);
            }
            break;

        default:
            strBuilder.append("VMU Exception occurs");
        }

        return strBuilder.toString();
    }
}
