/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade.packet;

import android.util.Log;

import com.qualcomm.libraries.vmupgrade.VMUUtils;
import com.qualcomm.libraries.vmupgrade.codes.OpCodes;

/**
 * <p>This class allows building of a packet for the VM upgrade as defined in the VM upgrade documentation.</p>
 * <p>The VMU packet is composed as follows:
 * <blockquote><pre>
 *      0 bytes   1        2         3         4        length+3
 *      +---------+---------+---------+ +---------+---------+
 *      | OPCODE* |      LENGTH*      | |      DATA...      |
 *      +---------+---------+---------+ +---------+---------+
 *      * mandatory information
 * </pre></blockquote></p>
 */

@SuppressWarnings("unused")
public class VMUPacket {

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "VMUPacket";
    /**
     * The number of bytes to define the packet length information.
     */
    private static final int LENGTH_LENGTH = 2;
    /**
     * The number of bytes to define the packet operation code information.
     */
    private static final int OPCODE_LENGTH = 1;
    /**
     * The offset for the operation code information.
     */
    private static final int OPCODE_OFFSET = 0;
    /**
     * The offset for the length information.
     */
    private static final int LENGTH_OFFSET = OPCODE_OFFSET + OPCODE_LENGTH;
    /**
     * The offset for the data information.
     */
    private static final int DATA_OFFSET = LENGTH_OFFSET + LENGTH_LENGTH;
    /**
     * The packet operation code information.
     */
    private final @OpCodes.Enum int mOpCode;
    /**
     * The packet data information.
     */
    private final byte[] mData;

    /**
     * The minimum length a VMU packet should have to be a VMU packet.
     */
    public static final int REQUIRED_INFORMATION_LENGTH = LENGTH_LENGTH + OPCODE_LENGTH;

    /**
     * To create a new instance of VM Upgrade packet.
     * 
     * @param opCode
     *            the operation code for this packet.
     * @param data
     *            the date for this packet.
     */
    public VMUPacket(@OpCodes.Enum int opCode, byte[] data) {
        this.mOpCode = opCode;
        if (data != null) {
            this.mData = data;
        }
        else {
            this.mData = new byte[0];
        }
    }

    /**
     * To create a new instance of VM Upgrade packet.
     *
     * @param opCode
     *            the operation code for this packet.
     */
    public VMUPacket(@OpCodes.Enum int opCode) {
        this.mOpCode = opCode;
        this.mData = new byte[0];
    }

    /**
     * <p>To build a VMU packet object from a byte array sent by a device.</p>
     * <p>The raw data of a VMU packet is as follows:
     * <blockquote><pre>
     *      0 bytes   1        2         3         4        length+3
     *      +---------+---------+---------+ +---------+---------+
     *      | OPCODE* |      LENGTH*      | |      DATA...      |
     *      +---------+---------+---------+ +---------+---------+
     *      * mandatory information
     * </pre></blockquote></p>
     * 
     * @param bytes
     *            The raw data of a VMU packet as sent by the device. To contain all mandatory information this bytes
     *            array has to have a minimum length of
     *            {@link #REQUIRED_INFORMATION_LENGTH REQUIRED_INFORMATION_LENGTH}.
     *
     * @throws VMUException type
     * {@link com.qualcomm.libraries.vmupgrade.packet.VMUException.Type#DATA_TOO_SHORT DATA_TOO_SHORT}.</p>
     */
    public VMUPacket(byte[] bytes) throws VMUException {
        if (bytes.length >= REQUIRED_INFORMATION_LENGTH) {
            this.mOpCode = OpCodes.getOpCode(bytes[OPCODE_OFFSET]);
            int length = VMUUtils.extractIntFromByteArray(bytes, LENGTH_OFFSET, LENGTH_LENGTH, false);
            int dataLength = bytes.length - REQUIRED_INFORMATION_LENGTH;

            if (length > dataLength) {
                Log.w(TAG, "Building packet: the LENGTH (" + length + ") is bigger than the DATA length("
                        + dataLength + ").");
            }
            else if (length < dataLength) {
                Log.w(TAG, "Building packet: the LENGTH (" + length + ") is smaller than the DATA length("
                        + dataLength + ").");
            }

            this.mData = new byte[dataLength];
            if (dataLength > 0) {
                System.arraycopy(bytes, DATA_OFFSET, mData, 0, dataLength);
            }
        }
        else {
            throw new VMUException(VMUException.Type.DATA_TOO_SHORT, bytes);
        }
    }

    /**
     * To get the byte array corresponding to this VMU packet.
     * 
     * @return a byte array built with these packet information.
     * 
     */
    public byte[] getBytes() {
        byte[] packet = new byte[mData.length + REQUIRED_INFORMATION_LENGTH];
        // opcode
        packet[OPCODE_OFFSET] = (byte) mOpCode;
        // length
        VMUUtils.copyIntIntoByteArray(mData.length, packet, LENGTH_OFFSET, LENGTH_LENGTH, false);

        // data if exists
        if (mData.length > 0) {
            System.arraycopy(mData, 0, packet, DATA_OFFSET, mData.length);
        }

        return packet;
    }

    /**
     * To get the operation code.
     * 
     * @return the operation code.
     */
    public @OpCodes.Enum int getOpCode() {
        return mOpCode;
    }

    /**
     * To get the data length.
     * 
     * @return the data length.
     */
    public int getLength() {
        return mData.length;
    }

    /**
     * To get the packet data.
     * 
     * @return the packet data.
     */
    public byte[] getData() {
        return mData;
    }

}
