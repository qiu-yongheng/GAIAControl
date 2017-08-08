/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia.packets;

import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.GaiaException;

/**
 * <p>This class encapsulates information for a GAIA packet. Depending on the type of the communication used, this class
 * will be implemented using the following classes: <ul> <li>{@link GaiaPacketBLE}: for a packet used over BLE
 * connections.</li> <li>{@link GaiaPacketBREDR}: for a packet used over BLE connections.</li> </ul></p>
 */
@SuppressWarnings("unused")
public abstract class GaiaPacket {

    /**
     * <p>The vendor ID of the packet.</p>
     */
    int mVendorId = GAIA.VENDOR_QUALCOMM;
    /**
     * <p>This attribute contains the full command of the packet. If this packet is an acknowledgement packet, this
     * attribute will contain the acknowledgement bit set to 1.</p>
     */
    int mCommandId;
    /**
     * <p>The payload which contains all values for the specified command.</p> <p>If the
     * packet is an acknowledgement packet, the first <code>byte</code> of the packet corresponds to the status of the
     * sent command.</p>
     */
    byte[] mPayload = new byte[0];
    /**
     * <p>The bytes which represent this packet.</p>
     */
    byte[] mBytes;

    /**
     * <p>Gets the entire payload.</p>
     *
     * @return Array of bytes containing the payload.
     */
    public byte[] getPayload() {
        return mPayload;
    }

    /**
     * <p>Gets the vendor identifier for this command.</p>
     *
     * @return The vendor identifier.
     */
    public int getVendorId() {
        return mVendorId;
    }

    /**
     * <p>Gets the raw command ID for this command with the ACK bit stripped out.</p>
     *
     * @return The command ID without the acknowledgment.
     */
    public int getCommand() {
        return mCommandId & GAIA.COMMAND_MASK;
    }

    /**
     * <p>Gets the command ID including the ACK bit.</p>
     *
     * @return The command ID.
     */
    public int getCommandId() {
        return mCommandId;
    }

    /**
     * <p>Gets the status byte from the payload of an acknowledgement packet.</p> <p>By convention in acknowledgement
     * packets the first byte contains the command status or 'result' of the command. Additional data may be present
     * in the acknowledgement packet, as defined by individual commands.</p>
     *
     * @return <p>The status code as defined in {@link GAIA.Status}.</p>
     */
    public @GAIA.Status int getStatus() {
        final int STATUS_OFFSET = 0;
        final int STATUS_LENGTH = 1;

        if (!isAcknowledgement() || mPayload == null || mPayload.length < STATUS_LENGTH) {
            return GAIA.Status.NOT_STATUS;
        }
        else {
            return GAIA.getStatus(mPayload[STATUS_OFFSET]);
        }
    }

    /**
     * <p>A packet is an acknowledgement packet if its command contains the acknowledgement mask.</p>
     *
     * @return <code>true</code> if the command is an acknowledgement.
     */
    public boolean isAcknowledgement() {
        return (mCommandId & GAIA.ACKNOWLEDGMENT_MASK) > 0;
    }

    /**
     * <p>Gets the event found in byte zero of the payload if the packet is a notification event packet.</p>
     *
     * @return The event code according to {@link GAIA.NotificationEvents}
     */
    public @GAIA.NotificationEvents int getEvent() {
        final int EVENT_OFFSET = 0;
        final int EVENT_LENGTH = 1;

        if ((mCommandId & GAIA.COMMANDS_NOTIFICATION_MASK) < 1 || mPayload == null ||
                mPayload.length < EVENT_LENGTH) {
            return GAIA.NotificationEvents.NOT_NOTIFICATION;
        }
        else {
            return GAIA.getNotificationEvent(mPayload[EVENT_OFFSET]);
        }
    }

    /**
     * <p>To get the bytes which correspond to this packet.</p>
     *
     * @return A new byte array if this packet has been created using its characteristics or the source bytes if this
     * packet has been created from a source <code>byte</code> array.
     *
     * @throws GaiaException for types:
     * <ul>
     *     <li>{@link GaiaException.Type#PAYLOAD_LENGTH_TOO_LONG}</li>
     * </ul>
     */
    public byte[] getBytes() throws GaiaException {
        if (mBytes != null) {
            return mBytes;
        }
        else {
            mBytes = buildBytes(mCommandId, mPayload);
            return mBytes;
        }
    }

    /**
     * <p>To get the acknowledgement packet bytes which correspond to this packet. Only works if the packet is not
     * already an acknowledgement packet.</p>
     *
     * @param status
     *          The status for the acknowledgement packet.
     * @param value
     *          The parameters to specify for the acknowledgement packet.
     *
     * @return A new byte array created with the acknowledgement command which corresponds to this packet command.
     *
     * @throws GaiaException for types:
     * <ul>
     *     <li>{@link GaiaException.Type#PAYLOAD_LENGTH_TOO_LONG}</li>
     *     <li>{@link GaiaException.Type#PACKET_NOT_AN_ACKNOWLEDGMENT}</li>
     * </ul>
     */
    public byte[] getAcknowledgementPacketBytes(@GAIA.Status int status, byte[] value) throws GaiaException {
        final int STATUS_OFFSET = 0;
        final int STATUS_LENGTH = 1;
        final int DATA_OFFSET = 1;

        if (isAcknowledgement()) {
            throw new GaiaException(GaiaException.Type.PACKET_IS_ALREADY_AN_ACKNOWLEDGEMENT);
        }
        int commandId = mCommandId | GAIA.ACKNOWLEDGMENT_MASK;

        byte[] payload;

        if (value != null) {
            int maxLength = getPayloadMaxLength();
            int length = STATUS_LENGTH + (value.length < maxLength ? value.length : maxLength);
            payload = new byte[length];
            System.arraycopy(value, 0, payload, DATA_OFFSET, length-STATUS_LENGTH);
        }
        else {
            payload = new byte[STATUS_LENGTH];
        }
        payload[STATUS_OFFSET] = (byte) status;

        return buildBytes(commandId, payload);
    }

    /**
     * <p>To build the byte array which represents this Gaia Packet.</p>
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
    abstract byte[] buildBytes(int commandId, byte[] payload) throws GaiaException;

    /**
     * <p>Depending on the type of transport used the packet format is different as well as the maximum number of
     * bytes they can use. This method gives the maximum number of bytes the payload can have. The payload is a
     * common field to any GAIA packet as well as the vendor ID and the command ID.</p>
     *
     * @return the maximum length of a payload.
     */
    abstract int getPayloadMaxLength();

    /**
     * <p>To build a Notification packet.</p>
     * <p>The packet is built according to the definition of a GAIA Notification Packet. The first byte of the
     * payload is the notification event, see
     * {@link com.qualcomm.libraries.gaia.GAIA.NotificationEvents NotificationEvents}. The next bytes represent the
     * data for the given event. The structure of the payload is as follows:
     * <blockquote>
     *
     * <pre>
     *     0 bytes  1                len
     *     +--------+--------+--------+
     * ... | EVENT  | DATA      ...   | ...
     *     +--------+--------+--------+
     * </pre>
     *
     * </blockquote></p>
     *
     * @param vendorID
     *              the vendor ID of the packet.
     * @param commandID
     *              the notification command ID (0x4...)
     * @param event
     *              The notification event
     * @param data
     *              Any additional data to complete with the notification event.
     *
     * @return the built object of the class GaiaPacketBLE
     *
     * @throws GaiaException occurs if the command ID does not fit a notification command ID.
     */
    public static GaiaPacket buildGaiaNotificationPacket(int vendorID, int commandID,
                                                         @GAIA.NotificationEvents int event, byte[] data,
                                                         @GAIA.Transport int type) throws GaiaException {
        if ((commandID & GAIA.COMMANDS_NOTIFICATION_MASK) != GAIA.COMMANDS_NOTIFICATION_MASK) {
            throw new GaiaException(GaiaException.Type.PACKET_NOT_A_NOTIFICATION);
        }
        final int EVENT_OFFSET = 0;
        final int EVENT_LENGTH = 1;
        final int DATA_OFFSET = 1; // EVENT_OFFSET + EVENT_LENGTH
        byte[] payload;

        if (data != null) {
            payload = new byte[EVENT_LENGTH + data.length];
            payload[EVENT_OFFSET] = (byte) event;
            System.arraycopy(data, 0, payload, DATA_OFFSET, data.length);
        }
        else {
            payload = new byte[EVENT_LENGTH];
            payload[EVENT_OFFSET] = (byte) event;
        }

        return type == GAIA.Transport.BLE ? new GaiaPacketBLE(vendorID, commandID, payload)
                : new GaiaPacketBREDR(vendorID, commandID, payload);
    }
}
