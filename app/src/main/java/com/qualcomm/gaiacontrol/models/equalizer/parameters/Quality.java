/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

import java.text.DecimalFormat;

/**
 * <p>The parameter "quality" refers to a band parameter and its configuration and bounds depend on the selected
 * band filter.</p>
 * <p>The factor for this parameter is 4096 times the real value.</p>
 */

public class Quality extends Parameter {

    /**
     * To define a human readable format for the decimal numbers.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();
    /**
     * To convert the quality from the packet value to the displayed one and vice-versa,
     * we have to multiply by a certain factor defined in the GAIA protocol.
     */
    private static final int FACTOR = 4096;

    public Quality () {
        super(ParameterType.QUALITY);
    }

    @Override
    String getLabel(double value) {
        if (isConfigurable) {
            mDecimalFormat.setMaximumFractionDigits(2);
            return mDecimalFormat.format(value);
        }
        else {
            return "-";
        }
    }

    @Override
    public int getFactor() {
        return FACTOR;
    }
}
