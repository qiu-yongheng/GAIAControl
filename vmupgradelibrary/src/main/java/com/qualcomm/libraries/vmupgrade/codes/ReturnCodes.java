/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade.codes;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.qualcomm.libraries.vmupgrade.VMUUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class gives all codes the board may send - after a command or asynchronously. These codes are encapsulated
 * into an {@link OpCodes.Enum#UPGRADE_ERROR_WARN_IND UPGRADE_ERROR_WARN_IND} message.</p>
 * <ul>
 * <li>Errors are considered as fatal: the Host should abort the process.</li>
 * <li>Warnings are considered as informational: the board will choose to abort or continue the process.</li>
 * </ul>
 */
@SuppressWarnings({"unused", "deprecation"})
public final class ReturnCodes {

    /**
     * <p>The enumeration of all return codes the Device can send.</p>
     */
    @IntDef(flag = true, value = {Enum.ERROR_UNKNOWN_ID, Enum.ERROR_WRONG_VARIANT, Enum.ERROR_INTERNAL_ERROR_DEPRECATED,
            Enum.ERROR_WRONG_PARTITION_NUMBER, Enum.ERROR_PARTITION_SIZE_MISMATCH, Enum.ERROR_PARTITION_TYPE_NOT_FOUND,
            Enum.ERROR_PARTITION_OPEN_FAILED, Enum.ERROR_PARTITION_WRITE_FAILED, Enum.ERROR_PARTITION_CLOSE_FAILED_1,
            Enum.ERROR_SFS_VALIDATION_FAILED, Enum.ERROR_OEM_VALIDATION_FAILED, Enum.ERROR_UPGRADE_FAILED,
            Enum.ERROR_APP_NOT_READY, Enum.ERROR_LOADER_ERROR, Enum.ERROR_UNEXPECTED_LOADER_MSG,
            Enum.ERROR_MISSING_LOADER_MSG, Enum.ERROR_BATTERY_LOW, Enum.ERROR_BAD_LENGTH_PARTITION_PARSE,
            Enum.ERROR_BAD_LENGTH_TOO_SHORT, Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER,
            Enum.ERROR_BAD_LENGTH_PARTITION_HEADER, Enum.ERROR_BAD_LENGTH_SIGNATURE,
            Enum.ERROR_BAD_LENGTH_DATAHDR_RESUME, Enum.ERROR_PARTITION_CLOSE_FAILED_2,
            Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER, Enum.ERROR_PARTITION_TYPE_NOT_MATCHING,
            Enum.ERROR_PARTITION_TYPE_TWO_DFU, Enum.ERROR_PARTITION_WRITE_FAILED_HEADER,
            Enum.ERROR_PARTITION_WRITE_FAILED_DATA, Enum.ERROR_INTERNAL_ERROR_1, Enum.ERROR_INTERNAL_ERROR_2,
            Enum.ERROR_INTERNAL_ERROR_3, Enum.ERROR_INTERNAL_ERROR_4, Enum.ERROR_INTERNAL_ERROR_5,
            Enum.ERROR_INTERNAL_ERROR_6, Enum.ERROR_INTERNAL_ERROR_7, Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE,
            Enum.WARN_SYNC_ID_IS_DIFFERENT, Enum.UNKNOWN_ERROR, Enum.ERROR_INVALID_SYNC_ID, Enum.ERROR_IN_ERROR_STATE,
            Enum.ERROR_NO_MEMORY, Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS, Enum.ERROR_BAD_LENGTH_DEPRECATED,
            Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER, Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1,
            Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2, Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA,
            Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER, Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY,
            Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE, Enum.ERROR_FILE_TOO_SMALL, Enum.ERROR_FILE_TOO_BIG })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Enum {
        /**
         * <p>This error does not exist in the protocol and is only used by this library to have a default error in
         * case a received value does not match any known return code.</p>
         */
        short UNKNOWN_ERROR = 0;

        /**
         * @deprecated
         */
        short ERROR_INTERNAL_ERROR_DEPRECATED = 0x10;
        short ERROR_UNKNOWN_ID = 0x11;
        /**
         * @deprecated
         */
        short ERROR_BAD_LENGTH_DEPRECATED = 0x12;
        short ERROR_WRONG_VARIANT = 0x13;
        short ERROR_WRONG_PARTITION_NUMBER = 0x14;
        short ERROR_PARTITION_SIZE_MISMATCH = 0x15;
        short ERROR_PARTITION_TYPE_NOT_FOUND = 0x16;
        short ERROR_PARTITION_OPEN_FAILED = 0x17;
        short ERROR_PARTITION_WRITE_FAILED = 0x18;
        short ERROR_PARTITION_CLOSE_FAILED_1 = 0x19;
        short ERROR_SFS_VALIDATION_FAILED = 0x1A;
        short ERROR_OEM_VALIDATION_FAILED = 0x1B;
        short ERROR_UPGRADE_FAILED = 0x1C;
        short ERROR_APP_NOT_READY = 0x1D;
        short ERROR_LOADER_ERROR = 0x1E;
        short ERROR_UNEXPECTED_LOADER_MSG = 0x1F;
        short ERROR_MISSING_LOADER_MSG = 0x20;
        short ERROR_BATTERY_LOW = 0x21;
        short ERROR_INVALID_SYNC_ID = 0x22;
        short ERROR_IN_ERROR_STATE = 0x23;
        short ERROR_NO_MEMORY = 0x24;
        short ERROR_BAD_LENGTH_PARTITION_PARSE = 0x30;
        short ERROR_BAD_LENGTH_TOO_SHORT = 0x31;
        short ERROR_BAD_LENGTH_UPGRADE_HEADER = 0x32;
        short ERROR_BAD_LENGTH_PARTITION_HEADER = 0x33;
        short ERROR_BAD_LENGTH_SIGNATURE = 0x34;
        short ERROR_BAD_LENGTH_DATAHDR_RESUME = 0x35;
        short ERROR_OEM_VALIDATION_FAILED_HEADERS = 0x38;
        short ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER = 0x39;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1 = 0x3A;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2 = 0x3B;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA = 0x3C;
        short ERROR_OEM_VALIDATION_FAILED_FOOTER = 0x3D;
        short ERROR_OEM_VALIDATION_FAILED_MEMORY = 0x3E;
        short ERROR_PARTITION_CLOSE_FAILED_2 = 0x40;
        short ERROR_PARTITION_CLOSE_FAILED_HEADER = 0x41;
        short ERROR_PARTITION_CLOSE_FAILED_PS_SPACE = 0x42;
        short ERROR_PARTITION_TYPE_NOT_MATCHING = 0x48;
        short ERROR_PARTITION_TYPE_TWO_DFU = 0x49;
        short ERROR_PARTITION_WRITE_FAILED_HEADER = 0x50;
        short ERROR_PARTITION_WRITE_FAILED_DATA = 0x51;
        short ERROR_FILE_TOO_SMALL = 0x58;
        short ERROR_FILE_TOO_BIG = 0x59;
        short ERROR_INTERNAL_ERROR_1 = 0x65;
        short ERROR_INTERNAL_ERROR_2 = 0x66;
        short ERROR_INTERNAL_ERROR_3 = 0x67;
        short ERROR_INTERNAL_ERROR_4 = 0x68;
        short ERROR_INTERNAL_ERROR_5 = 0x69;
        short ERROR_INTERNAL_ERROR_6 = 0x6A;
        short ERROR_INTERNAL_ERROR_7 = 0x6B;
        short WARN_APP_CONFIG_VERSION_INCOMPATIBLE = 0x80;
        /**
         * This error means the file is already uploaded onto the board.
         */
        short WARN_SYNC_ID_IS_DIFFERENT = 0x81;
    }

    /**
     * <p>To get the ReturnCode corresponding to the given value.</p>
     * @param code
     *              The value to get the corresponding Return Code.
     *
     * @return The corresponding ReturnCode or {@link Enum#UNKNOWN_ERROR UNKNOWN_ERROR} of the value is unknown.
     */
    public static @ReturnCodes.Enum int getReturnCode(short code) {
        switch(code) {
            case Enum.ERROR_INTERNAL_ERROR_DEPRECATED:
                return Enum.ERROR_INTERNAL_ERROR_DEPRECATED;
            case Enum.ERROR_UNKNOWN_ID:
                return Enum.ERROR_UNKNOWN_ID;
            case Enum.ERROR_WRONG_VARIANT:
                return Enum.ERROR_WRONG_VARIANT;
            case Enum.ERROR_WRONG_PARTITION_NUMBER:
                return Enum.ERROR_WRONG_PARTITION_NUMBER;
            case Enum.ERROR_PARTITION_SIZE_MISMATCH:
                return Enum.ERROR_PARTITION_SIZE_MISMATCH;
            case Enum.ERROR_PARTITION_TYPE_NOT_FOUND:
                return Enum.ERROR_PARTITION_TYPE_NOT_FOUND;
            case Enum.ERROR_PARTITION_OPEN_FAILED:
                return Enum.ERROR_PARTITION_OPEN_FAILED;
            case Enum.ERROR_PARTITION_WRITE_FAILED:
                return Enum.ERROR_PARTITION_WRITE_FAILED;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_1:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_1;
            case Enum.ERROR_SFS_VALIDATION_FAILED:
                return Enum.ERROR_SFS_VALIDATION_FAILED;
            case Enum.ERROR_OEM_VALIDATION_FAILED:
                return Enum.ERROR_OEM_VALIDATION_FAILED;
            case Enum.ERROR_UPGRADE_FAILED:
                return Enum.ERROR_UPGRADE_FAILED;
            case Enum.ERROR_APP_NOT_READY:
                return Enum.ERROR_APP_NOT_READY;
            case Enum.ERROR_LOADER_ERROR:
                return Enum.ERROR_LOADER_ERROR;
            case Enum.ERROR_UNEXPECTED_LOADER_MSG:
                return Enum.ERROR_UNEXPECTED_LOADER_MSG;
            case Enum.ERROR_MISSING_LOADER_MSG:
                return Enum.ERROR_MISSING_LOADER_MSG;
            case Enum.ERROR_BATTERY_LOW:
                return Enum.ERROR_BATTERY_LOW;
            case Enum.ERROR_INVALID_SYNC_ID:
                return Enum.ERROR_INVALID_SYNC_ID;
            case Enum.ERROR_IN_ERROR_STATE:
                return Enum.ERROR_IN_ERROR_STATE;
            case Enum.ERROR_NO_MEMORY:
                return Enum.ERROR_NO_MEMORY;
            case Enum.ERROR_BAD_LENGTH_PARTITION_PARSE:
                return Enum.ERROR_BAD_LENGTH_PARTITION_PARSE;
            case Enum.ERROR_BAD_LENGTH_TOO_SHORT:
                return Enum.ERROR_BAD_LENGTH_TOO_SHORT;
            case Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER:
                return Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER;
            case Enum.ERROR_BAD_LENGTH_PARTITION_HEADER:
                return Enum.ERROR_BAD_LENGTH_PARTITION_HEADER;
            case Enum.ERROR_BAD_LENGTH_SIGNATURE:
                return Enum.ERROR_BAD_LENGTH_SIGNATURE;
            case Enum.ERROR_BAD_LENGTH_DATAHDR_RESUME:
                return Enum.ERROR_BAD_LENGTH_DATAHDR_RESUME;
            case Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS:
                return Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS;
            case Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER:
                return Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA;
            case Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER:
                return Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER;
            case Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY:
                return Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_2:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_2;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE;
            case Enum.ERROR_PARTITION_TYPE_NOT_MATCHING:
                return Enum.ERROR_PARTITION_TYPE_NOT_MATCHING;
            case Enum.ERROR_PARTITION_TYPE_TWO_DFU:
                return Enum.ERROR_PARTITION_TYPE_TWO_DFU;
            case Enum.ERROR_PARTITION_WRITE_FAILED_HEADER:
                return Enum.ERROR_PARTITION_WRITE_FAILED_HEADER;
            case Enum.ERROR_PARTITION_WRITE_FAILED_DATA:
                return Enum.ERROR_PARTITION_WRITE_FAILED_DATA;
            case Enum.ERROR_FILE_TOO_SMALL:
                return Enum.ERROR_FILE_TOO_SMALL;
            case Enum.ERROR_FILE_TOO_BIG:
                return Enum.ERROR_FILE_TOO_BIG;
            case Enum.ERROR_INTERNAL_ERROR_1:
                return Enum.ERROR_INTERNAL_ERROR_1;
            case Enum.ERROR_INTERNAL_ERROR_2:
                return Enum.ERROR_INTERNAL_ERROR_2;
            case Enum.ERROR_INTERNAL_ERROR_3:
                return Enum.ERROR_INTERNAL_ERROR_3;
            case Enum.ERROR_INTERNAL_ERROR_4:
                return Enum.ERROR_INTERNAL_ERROR_4;
            case Enum.ERROR_INTERNAL_ERROR_5:
                return Enum.ERROR_INTERNAL_ERROR_5;
            case Enum.ERROR_INTERNAL_ERROR_6:
                return Enum.ERROR_INTERNAL_ERROR_6;
            case Enum.ERROR_INTERNAL_ERROR_7:
                return Enum.ERROR_INTERNAL_ERROR_7;
            case Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                return Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE;
            case Enum.WARN_SYNC_ID_IS_DIFFERENT:
                return Enum.WARN_SYNC_ID_IS_DIFFERENT;
            default:
                return Enum.UNKNOWN_ERROR;
        }
    }

    /**
     * <p>To get a readable message for the given return code.</p>
     * @param code
     *              The code which a readable message is wanted.
     *
     * @return The readable message which corresponds to the given code.
     */
    public static String getReturnCodesMessage(@ReturnCodes.Enum int code) {
        switch (code) {
            case Enum.ERROR_INTERNAL_ERROR_DEPRECATED:
                return "Deprecated error: internal error (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_UNKNOWN_ID:
                return "Error: unknown ID (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_DEPRECATED:
                return "Deprecated error: bad length (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_WRONG_VARIANT:
                return "Error: wrong variant (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_WRONG_PARTITION_NUMBER:
                return "Error: wrong partition number (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_SIZE_MISMATCH:
                return "Error: partition size mismatch (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_TYPE_NOT_FOUND:
                return "Error: partition type not found (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_OPEN_FAILED:
                return "Error: partition open failed (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_WRITE_FAILED:
                return "Error: partition write failed (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_CLOSE_FAILED_1:
                return "Partition close failed type 1 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_SFS_VALIDATION_FAILED:
                return "Error: SFS validation failed (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED:
                return "Error: OEM validation failed (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_UPGRADE_FAILED:
                return "Error: upgrade failed (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_APP_NOT_READY:
                return "Error: application not ready (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_LOADER_ERROR:
                return "Error: loader error (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_UNEXPECTED_LOADER_MSG:
                return "Error: unexpected loader message (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_MISSING_LOADER_MSG:
                return "Error: missing loader message (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BATTERY_LOW:
                return "Error: battery low (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INVALID_SYNC_ID:
                return "Error: invalid sync ID (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_IN_ERROR_STATE:
                return "Error: in error state (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_NO_MEMORY:
                return "Error: no memory (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_PARTITION_PARSE:
                return "Error: bad length partition parse (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_TOO_SHORT:
                return "Error: bad length too short (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER:
                return "Error: bad length upgrade header (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_PARTITION_HEADER:
                return "Error: bad length partition header (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_SIGNATURE:
                return "Error: bad length signature (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_BAD_LENGTH_DATAHDR_RESUME:
                return "Error: bad length data handler resume (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS:
                return "Error: OEM validation failed headers (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER:
                return "Error: OEM validation failed upgrade header (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1:
                return "Error: OEM validation failed partition header 1 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2:
                return "Error: OEM validation failed partition header 2 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA:
                return "Error: OEM validation failed partition data (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER:
                return "Error: OEM validation failed footer (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY:
                return "Error: OEM validation failed memory (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_CLOSE_FAILED_2:
                return "Error: partition close failed type 2 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER:
                return "Error: partition close failed header (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE:
                return "Error: partition close failed ps space (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_TYPE_NOT_MATCHING:
                return "Error: partition type not matching (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_TYPE_TWO_DFU:
                return "Error: partition type two DFU (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_WRITE_FAILED_HEADER:
                return "Error: partition write failed header (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_PARTITION_WRITE_FAILED_DATA:
                return "Error: partition write failed data (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_FILE_TOO_SMALL:
                return "Error: file too small (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_FILE_TOO_BIG:
                return "Error: file too big (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_1:
                return "Error: internal error 1 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_2:
                return "Error: internal error 2 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_3:
                return "Error: internal error 3 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_4:
                return "Error: internal error 4 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_5:
                return "Error: internal error 5 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_6:
                return "Error: internal error 6 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.ERROR_INTERNAL_ERROR_7:
                return "Error: internal error 7 (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                return "Warning: application configuration version incompatible (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.WARN_SYNC_ID_IS_DIFFERENT:
                return "Warning: Sync ID is different (" + VMUUtils.getHexadecimalString(code) + ")";
            case Enum.UNKNOWN_ERROR:
            default:
                return "Unknown return code (" + VMUUtils.getHexadecimalString(code) + ")";
        }
    }
}
