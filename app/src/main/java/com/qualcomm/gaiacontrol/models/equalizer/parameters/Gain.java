/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

import java.text.DecimalFormat;

/**
 * <p>The parameter "gain" refers to a band parameter and its configuration and bounds depend on the selected
 * band filter.</p>
 * <p>The factor for this parameter is 60 times the real value.</p>
 */

public class Gain extends Parameter {

    /**
     * To define a human readable format for the decimal numbers.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();
    /**
     * To convert the gain from the packet value to the displayed one and vice-versa,
     * we have to multiply by a certain factor defined in the GAIA protocol.
     */
    private static final int FACTOR = 60;

    /**
     * <p>To build a new {@link Parameter} of the type {@link ParameterType#GAIN GAIN}.</p>
     */
    public Gain () {
        super(ParameterType.GAIN);
    }

    @Override
    String getLabel(double value) {
        if (isConfigurable) {
            mDecimalFormat.setMaximumFractionDigits(1);
            return mDecimalFormat.format(value) + " dB";
        }
        else {
            return "- dB";
        }
    }

    @Override
    public int getFactor() {
        return FACTOR;
    }
}
