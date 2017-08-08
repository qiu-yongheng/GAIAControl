/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia.packets;

import com.qualcomm.libraries.gaia.GaiaUtils;
import com.qualcomm.libraries.gaia.GaiaException;

/**
 * <p>This class encapsulates a Gaia packet sent and received over a BR/EDR connection with a Bluetooth device.</p>
 * <p>The packet encapsulated by this class is represented by the following byte structure over Bluetooth:
 * <blockquote><pre>
 * 0 bytes  1         2        3        4        5        6        7         8         9       len+8
 * +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
 * |   SOF  | VERSION | FLAGS  | LENGTH |    VENDOR ID    |   COMMAND ID    | | PAYLOAD   ...   | | CHECK  |
 * +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
 * </pre></blockquote></p>
 * <ul>
 *     <li>Start of frame (SOF)
 *     <ul>
 *         <li>Length: 1 byte</li>
 *         <li>Value: 0xFF</li>
 *     </ul></li>
 *     <li>Version
 *     <ul>
 *         <li>Length: 1 byte</li>
 *         <li>Value: 0x01 for first version of the protocol</li>
 *     </ul></li>
 *     <li>Flags
 *     <ul>
 *         <li>Length: 1 byte</li>
 *         <li>Bit 0: GAIA_FLAG_CHECK, checksum is in use</li>
 *         <li>Bits 1 - 7: reserved, must be zero</li>
 *     </ul></li>
 *     <li>Payload Length
 *     <ul>
 *         <li>Length: 1 byte</li>
 *         <li>A maximum payload length of 254 bytes reduces the likelihood of spurious SOFs in the packet</li>
 *     </ul></li>
 *     <li>Vendor ID
 *     <ul>
 *         <li>Length: 2 bytes</li>
 *         <li>Bluetooth SIG already have assigned numbers identifying member companies. For instance, CSR is 0x000A.</li>
 *     </ul></li>
 *     <li>Command ID
 *     <ul>
 *         <li>Length: 2 bytes</li>
 *         <li>See Commands for valid Command IDs.</li>
 *     </ul></li>
 *     <li>Payload
 *     <ul>
 *         <li>Length: As defined by 'Payload Length' header field</li>
 *         <li>Format implicit in COMMAND ID</li>
 *     </ul></li>
 *     <li>Checksum
 *     <ul>
 *         <li>Optional -- see Flags above</li>
 *         <li>Length: 1 byte</li>
 *         <li>Simple XOR of all bytes in the packet</li>
 * </ul>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GaiaPacketBREDR extends GaiaPacket {

    /**
     * <p>To know if the checksum is in use for this packet.</p>
     * <p>By default there is no checksum for the packet.</p>
     */
    private boolean mHasChecksum = false;

    /**
     * <p>The maximum length of a complete packet.</p>
     */
    public static final int MAX_PACKET = 270;
    /**
     * <p>The maximum length for the packet payload.</p>
     */
    public static final int MAX_PAYLOAD = 254;
    /**
     * <p>The mask for the flag of this packet if requested.</p>
     */
    public static final int FLAG_CHECK_MASK = 0x01;
    /**
     * <p>The offset for the bytes which represents the SOF - start of frame - in the byte structure.</p>
     */
    public static final int OFFSET_SOF = 0;
    /**
     * <p>The offset for the bytes which represents the protocol version in the byte structure.</p>
     */
    public static final int OFFSET_VERSION = 1;
    /**
     * <p>The offset for the bytes which represents the flag in the byte structure.</p>
     */
    public static final int OFFSET_FLAGS = 2;
    /**
     * <p>The offset for the bytes which represents the payload length in the byte structure.</p>
     */
    public static final int OFFSET_LENGTH = 3;
    /**
     * <p>The offset for the bytes which represents the vendor id in the byte structure.</p>
     */
    public static final int OFFSET_VENDOR_ID = 4;
    /**
     * <p>The number of bytes which represents the vendor id in the byte structure.</p>
     */
    public static final int LENGTH_VENDOR_ID = 2;
    /**
     * <p>The offset for the bytes which represents the command id in the byte structure.</p>
     */
    public static final int OFFSET_COMMAND_ID = 6;
    /**
     * <p>The number of bytes which represents the command id in the byte structure.</p>
     */
    public static final int LENGTH_COMMAND_ID = 2;
    /**
     * <p>The offset for the bytes which represents the payload in the byte structure.</p>
     */
    public static final int OFFSET_PAYLOAD = 8;
    /**
     * <p>The protocol version to use for these packets.</p>
     */
    public static final int PROTOCOL_VERSION = 1;
    /**
     * The number of bytes for the check value.
     */
    public static final int CHECK_LENGTH = 1;
    /**
     * <p>The SOF - Start Of Frame - value to use for these packets.</p>
     */
    public static final byte SOF = (byte) 0xFF;


    /**
     * <p>Constructor that builds a command from a byte sequence.</p>
     *
     * @param source
     *            Array of bytes to build the command from.
     */
    public GaiaPacketBREDR(byte[] source) {
        int flags = source[OFFSET_FLAGS];
        int payloadLength = source.length - OFFSET_PAYLOAD;

        if ((flags & FLAG_CHECK_MASK) != 0) {
            --payloadLength;
        }

        mVendorId = GaiaUtils.extractIntFromByteArray(source, OFFSET_VENDOR_ID, LENGTH_VENDOR_ID, false);
        mCommandId = GaiaUtils.extractIntFromByteArray(source, OFFSET_COMMAND_ID, LENGTH_COMMAND_ID, false);

        if (payloadLength > 0) {
            mPayload = new byte[payloadLength];
            System.arraycopy(source, OFFSET_PAYLOAD, mPayload, 0, payloadLength);
        }

        this.mBytes = source;
    }

    /**
     * <p>Constructor that builds a packet with the information which composed the packet.</p>
     *
     * @param vendorId
     *              The packet vendor ID.
     * @param commandId
     *              The packet command ID.
     * @param hasChecksum
     *              A boolean to know if a flag should be applied to this packet.
     */
    public GaiaPacketBREDR(int vendorId, int commandId, boolean hasChecksum) {
        this.mVendorId = vendorId;
        this.mCommandId = commandId;
        this.mPayload = new byte[0];
        this.mHasChecksum = hasChecksum;
        this.mBytes = null;
    }

    /**
     * <p>Constructor that builds a packet with the information which composed the packet.</p>
     *
     * @param vendorId
     *              The packet vendor ID.
     * @param commandId
     *              The packet command ID.
     * @param payload
     *              The packet payload.
     * @param hasChecksum
     *              A boolean to know if a flag should be applied to this packet.
     */
    public GaiaPacketBREDR(int vendorId, int commandId, byte[] payload, boolean hasChecksum) {
        this.mVendorId = vendorId;
        this.mCommandId = commandId;
        this.mPayload = payload;
        this.mHasChecksum = hasChecksum;
        this.mBytes = null;
    }

    /**
     * <p>Constructor that builds a packet with the information which composed the packet.</p>
     * <p>Using this constructor, the flags will be set to false.</p>
     *
     * @param vendorId
     *              The packet vendor ID.
     * @param commandId
     *              The packet command ID.
     * @param payload
     *              The packet payload.
     */
    public GaiaPacketBREDR(int vendorId, int commandId, byte[] payload) {
        this.mVendorId = vendorId;
        this.mCommandId = commandId;
        this.mPayload = payload;
        this.mHasChecksum = false;
        this.mBytes = null;
    }

    /**
     * <p>To build the byte array which represents this Gaia Packet over BR/EDR.</p>
     * <p>The bytes array is built according to the definition of a GAIA Packet sent over BR/EDR:
     * <blockquote>
     *
     * <pre>
     * 0 bytes  1         2        3        4        5        6        7         8         9       len+8
     * +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
     * |   SOF  | VERSION | FLAGS  | LENGTH |    VENDOR ID    |   COMMAND ID    | | PAYLOAD   ...   | | CHECK  |
     * +--------+---------+--------+--------+--------+--------+--------+--------+ +--------+--------+ +--------+
     * </pre>
     *
     * </blockquote></p>
     *
     * @param commandId
     *              The command ID for the packet bytes to build:
     *              <ul>
     *                  <li>The original command ID of this packet.</li>
     *                  <li>The acknowledgement version of the original command ID.</li>
     *              </ul>
     * @param payload
     *              The payload to include in the packet bytes.
     *
     * @return A new byte array built with the given information.
     *
     * @throws GaiaException If any issue occurs during the building of the bytes, A GaiaException is thrown with types:
     * <ul>
     *     <li>{@link GaiaException.Type#PAYLOAD_LENGTH_TOO_LONG}</li>
     * </ul>
     */
    byte[] buildBytes(int commandId, byte[] payload) throws GaiaException {
        // if the payload is bigger than the maximum size: packet won't be sent.
        if (payload.length > MAX_PAYLOAD) {
            throw new GaiaException(GaiaException.Type.PAYLOAD_LENGTH_TOO_LONG);
        }

        int length = payload.length + OFFSET_PAYLOAD + (mHasChecksum ? CHECK_LENGTH : 0);
        byte[] bytes = new byte[length];

        bytes[OFFSET_SOF] = SOF;
        bytes[OFFSET_VERSION] = PROTOCOL_VERSION;
        bytes[OFFSET_FLAGS] = mHasChecksum ? (byte) 0x01 : 0x00;
        bytes[OFFSET_LENGTH] = (byte) payload.length;

        GaiaUtils.copyIntIntoByteArray(mVendorId, bytes, OFFSET_VENDOR_ID, LENGTH_VENDOR_ID, false);
        GaiaUtils.copyIntIntoByteArray(commandId, bytes, OFFSET_COMMAND_ID, LENGTH_COMMAND_ID, false);

        System.arraycopy(payload, 0, bytes, OFFSET_PAYLOAD, payload.length);

        // if there is a checksum, calculating the checksum value
        if (mHasChecksum) {
            byte check = 0;
            for (int i=0; i < length-1; i++) {
                check ^= bytes[i];
            }
            bytes[length-1] = check;
        }

        return bytes;
    }

    @Override
    int getPayloadMaxLength() {
        return MAX_PAYLOAD;
    }
}
