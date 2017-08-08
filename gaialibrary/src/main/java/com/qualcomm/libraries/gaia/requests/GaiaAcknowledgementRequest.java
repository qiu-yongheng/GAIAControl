/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia.requests;

import com.qualcomm.libraries.gaia.GAIA;

/**
 * The data structure to define an acknowledgement request.
 */
public class GaiaAcknowledgementRequest extends GaiaRequest {

    /**
     * The status for the acknowledgement.
     */
    public @GAIA.Status final int status;
    /**
     * Any data to add to the ACK.
     */
    public final byte[] data;

    /**
     * To build a new request of type acknowledgement.
     */
    public GaiaAcknowledgementRequest(@GAIA.Status int status, byte[] data) {
        super(GaiaRequest.Type.ACKNOWLEDGEMENT);
        this.status = status;
        this.data = data;
    }
}
