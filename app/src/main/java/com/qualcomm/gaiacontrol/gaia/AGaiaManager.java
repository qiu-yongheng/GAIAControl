/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.gaia;

import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.GaiaManager;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBLE;
import com.qualcomm.libraries.gaia.packets.GaiaPacketBREDR;

/**
 * <p>This abstract class defines generic methods to create GAIA packets depending on the transport type.</p>
 * <p>This will avoid each GAIA Manager used in this application to implement these methods.</p>
 * <p>Also this class allows to manage the checksum of BR/EDR packets as per default this application does not use
 * the checksum to build its packets.</p>
 * <p>To finish this class also manages the Vendor value for the creation of packet as per default this application
 * only uses commands define for vendor {@link GAIA#VENDOR_QUALCOMM VENDOR_QUALCOMM}.</p>
 */
/*package*/ abstract class AGaiaManager extends GaiaManager {

    /**
     * To know if the checksum should be present for the building of a BR/EDR packet.
     */
    private final boolean hasChecksum = false;
    /**
     * The vendor to use to create packets
     */
    private final int mVendor = GAIA.VENDOR_QUALCOMM;

    /**
     * <p>Constructor of the class.</p>
     *
     * @param transportType
     *          The type of Bluetooth transport to use with this manager. It should be one of the transports defined in
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport Transport}.
     */
    /*package*/ AGaiaManager(@GAIA.Transport int transportType) {
        super(transportType);
        showDebugLogs(false); // when needed the child managers can activate this to display more logs
    }

    /**
     * <p>To create a simple GAIA packet with no payload.</p>
     * <p>Depending on the transport, this method will create one of the following GaiaPacket:
     * <ul>
     *     <li>{@link GaiaPacketBLE GaiaPacketBLE}</li>
     *     <li>{@link GaiaPacketBREDR GaiaPacketBREDR}</li>
     * </ul></p>
     *
     * @param command
     *          The GAIA command to put in the GAIA packet.
     *
     * @return The GAIA packet created depending on the transport type of this manager.
     */
    /*package*/ GaiaPacket createPacket(int command) {
        return getTransportType() == GAIA.Transport.BLE ?
                new GaiaPacketBLE(mVendor, command)
                : new GaiaPacketBREDR(mVendor, command, hasChecksum);
    }

    /**
     * <p>To create a GAIA packet with a payload.</p>
     * <p>Depending on the transport, this method will create one of the following GaiaPacket:
     * <ul>
     *     <li>{@link GaiaPacketBLE GaiaPacketBLE}</li>
     *     <li>{@link GaiaPacketBREDR GaiaPacketBREDR}</li>
     * </ul></p>
     *
     * @param command
     *          The GAIA command to put in the GAIA packet.
     * @param payload
     *          The parameters payload to include in the GAIA packet.
     *
     * @return The GAIA packet created depending on the transport type of this manager.
     */
    /*package*/ GaiaPacket createPacket(int command, byte[] payload) {
        return getTransportType() == GAIA.Transport.BLE ?
                new GaiaPacketBLE(mVendor, command, payload)
                : new GaiaPacketBREDR(mVendor, command, payload, hasChecksum);
    }
}
