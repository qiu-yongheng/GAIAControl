/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia.packets;

import com.qualcomm.libraries.gaia.GaiaException;
import com.qualcomm.libraries.gaia.GaiaUtils;

/**
 * <p>This class encapsulates a Gaia packet sent and received over a Bluetooth Low Energy connection with a BLE
 * device.</p>
 * <p>The packet encapsulated by this class is represented by the following byte structure:
 * <blockquote>
 * 
 * <pre>
 * 0 bytes  1         2        3         4         5       len+4
 * +--------+---------+--------+--------+ +--------+--------+
 * |    VENDOR ID     |   COMMAND ID    | | PAYLOAD   ...   |
 * +--------+---------+--------+--------+ +--------+--------+
 * </pre>
 * 
 * </blockquote></p>
 */
@SuppressWarnings({"SameParameterValue", "unused", "WeakerAccess"})
public class GaiaPacketBLE extends GaiaPacket {

    /**
     * <p>The maximum length for the packet payload.</p>
     * <p>The BLE data length maximum for a packet is 20.</p>
     */
    public static final int MAX_PAYLOAD = 16;
    /**
     * <p>The offset for the bytes which represents the vendor id in the byte structure.</p>
     */
    private static final int OFFSET_VENDOR_ID = 0;
    /**
     * <p>The number of bytes which represents the vendor id in the byte structure.</p>
     */
    private static final int LENGTH_VENDOR_ID = 2;
    /**
     * <p>The offset for the bytes which represents the command id in the byte structure.</p>
     */
    private static final int OFFSET_COMMAND_ID = 2;
    /**
     * <p>The number of bytes which represents the command id in the byte structure.</p>
     */
    private static final int LENGTH_COMMAND_ID = 2;
    /**
     * <p>The offset for the bytes which represents the payload in the byte structure.</p>
     */
    private static final int OFFSET_PAYLOAD = 4;

    /**
     * <p>Constructor that builds a packet from a byte sequence.</p>
     *
     * @param source
     *            Array of bytes to build the command from.
     */
    public GaiaPacketBLE(byte[] source) throws GaiaException {
        int payloadLength = source.length - OFFSET_PAYLOAD;

        if (payloadLength < 0) {
            throw new GaiaException(GaiaException.Type.PACKET_PAYLOAD_INVALID_PARAMETER);
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
     * <p>Constructor that builds a packet with the information which are parts of a packet.</p>
     *
     * @param vendorId
     *            the vendor ID of the packet.
     * @param commandId
     *            the command ID of the packet.
     */
    public GaiaPacketBLE(int vendorId, int commandId) {
        this.mVendorId = vendorId;
        this.mCommandId = commandId;
        this.mPayload = new byte[0];
        this.mBytes = null;
    }

    /**
     * <p>Constructor that builds a packet with the information which are parts of a packet.</p>
     *
     * @param vendorId
     *            the vendor ID of the packet.
     * @param commandId
     *            the command ID of the packet.
     * @param payload
     *            the payload of the packet.
     */
    public GaiaPacketBLE(int vendorId, int commandId, byte[] payload) {
        this.mVendorId = vendorId;
        this.mCommandId = commandId;
        this.mPayload = payload;
        this.mBytes = null;
    }

    /**
     * <p>To build the byte array which represents this Gaia Packet over BLE.</p>
     * <p>The bytes array is built according to the definition of a GAIA Packet sent over BLE:
     * <blockquote>
     *
     * <pre>
     * 0 bytes  1         2        3         4                 len+4
     * +--------+---------+--------+--------+ +--------+--------+
     * |    VENDOR ID     |   COMMAND ID    | | PAYLOAD   ...   |
     * +--------+---------+--------+--------+ +--------+--------+
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
     * @throws GaiaException If any issue occurs during the built of the bytes, A GaiaException is thrown with types:
     * <ul>
     *     <li>{@link GaiaException.Type#PAYLOAD_LENGTH_TOO_LONG}</li>
     * </ul>
     */
    @Override
    byte[] buildBytes(int commandId, byte[] payload) throws GaiaException {
        if (payload.length > MAX_PAYLOAD) {
            throw new GaiaException(GaiaException.Type.PAYLOAD_LENGTH_TOO_LONG);
        }

        int length = payload.length + OFFSET_PAYLOAD;
        byte[] bytes = new byte[length];

        GaiaUtils.copyIntIntoByteArray(mVendorId, bytes, OFFSET_VENDOR_ID, LENGTH_VENDOR_ID, false);
        GaiaUtils.copyIntIntoByteArray(commandId, bytes, OFFSET_COMMAND_ID, LENGTH_COMMAND_ID, false);
        System.arraycopy(payload, 0, bytes, OFFSET_PAYLOAD, payload.length);

        return bytes;
    }

    @Override
    int getPayloadMaxLength() {
        return MAX_PAYLOAD;
    }
}
