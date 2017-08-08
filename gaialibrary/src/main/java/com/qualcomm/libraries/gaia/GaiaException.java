/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

/**
 * <p>A GaiaException is thrown when a problem occurs with the GAIA protocol.</p>
 */
@SuppressWarnings("unused")
public class GaiaException extends Exception {

    /**
     * <p>The type of the gaia exception.</p>
     */
    private final @Type int mType;
    /**
     * <p>If the exception is linked to a command, the corresponding command.</p>
     */
    private final int mCommand;

    /**
     * <p></p>All types of gaia exceptions.</p>
     */
    @IntDef(flag = true, value = { Type.PAYLOAD_LENGTH_TOO_LONG, Type.PACKET_IS_ALREADY_AN_ACKNOWLEDGEMENT,
            Type.PACKET_NOT_A_NOTIFICATION, Type.PACKET_PAYLOAD_INVALID_PARAMETER, Type.PACKET_NOT_AN_ACKNOWLEDGMENT })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Type {
        /**
         * <p>This exception occurs when trying to build a GAIA packet but the given payload is longer than the
         * maximum length the packet can be.</p>
         */
        int PAYLOAD_LENGTH_TOO_LONG = 0;
        /**
         * <p>This exception occurs when trying to build an acknowledgement packet on an acknowledgement packet.</p>
         */
        int PACKET_IS_ALREADY_AN_ACKNOWLEDGEMENT = 1;
        /**
         * <p>This exception occurs when trying to use a non notification packet as a notification packet.</p>
         */
        int PACKET_NOT_A_NOTIFICATION = 2;
        /**
         * <p>This exception occurs when the payload does not contain the value requested for a command or when building a new GAIA packet and the given argument is incorrect.</p>
         * <p><i>For example, the command EVENT_NOTIFICATION required the event code as the first byte of the payload.</i></p>
         * <p><i>For example, we are trying to build a packet from a byte array which does not contain enough bytes.</i></p>
         */
        int PACKET_PAYLOAD_INVALID_PARAMETER = 3;
        /**
         * <p>This exception occurs when trying to use a non acknowledgement packet as an acknowledgement packet.</p>
         */
        int PACKET_NOT_AN_ACKNOWLEDGMENT = 4;
    }

    /**
     * <p>Class constructor for this exception.</p>
     *
     * @param type
     *            the type of this exception.
     */
    public GaiaException(@Type int type) {
        super();
        this.mType = type;
        this.mCommand = -1;
    }

    /**
     * <p>Class constructor for this exception.</p>
     *
     * @param type
     *            the type of this exception.
     *  @param command
     *            the command linked to the exception if occurred while working with a command.
     */
    public GaiaException(@Type int type, int command) {
        super();
        this.mType = type;
        this.mCommand = command;
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
        case Type.PAYLOAD_LENGTH_TOO_LONG:
            strBuilder.append("Build of a packet failed: the payload length is bigger than the authorized packet " +
                    "length.");
            break;

        case Type.PACKET_IS_ALREADY_AN_ACKNOWLEDGEMENT:
            strBuilder.append("Build of a packet failed: the packet is already an acknowledgement packet: not " +
                    "possible to create an acknowledgement packet from it.");
            break;

        case Type.PACKET_NOT_A_NOTIFICATION:
            strBuilder.append("Packet is not a COMMAND NOTIFICATION");
            if (mCommand >= 0) {
                strBuilder.append(", received command: ");
                strBuilder.append(GaiaUtils.getGAIACommandToString(mCommand));
            }
            break;

        case Type.PACKET_PAYLOAD_INVALID_PARAMETER:
            strBuilder.append("Payload is missing argument");
            if (mCommand >= 0) {
                strBuilder.append(" for command: ");
                strBuilder.append(GaiaUtils.getGAIACommandToString(mCommand));
            }
            break;

            case Type.PACKET_NOT_AN_ACKNOWLEDGMENT:
                strBuilder.append("The packet is not an acknowledgement, ");
                if (mCommand >= 0) {
                    strBuilder.append(" received command: ");
                    strBuilder.append(GaiaUtils.getGAIACommandToString(mCommand));
                }
                break;

        default:
            strBuilder.append("Gaia Exception occurred.");
        }

        return strBuilder.toString();
    }
}
