/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

import java.text.DecimalFormat;

/**
 * <p>The parameter "master gain" refers to a band parameter and its configuration and bounds depend on the selected
 * band filter.</p>
 * <p>The master gain is in a range from -36dB to 12 dB.</p>
 * <p>The factor for this parameter is 60 times the real value.</p>
 */
public class MasterGain extends Parameter {

    /**
     * To define a human readable format for the decimal numbers.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();
    /**
     * To convert the master gain from the packet value to the displayed one and vice-versa,
     * we have to multiply by a certain factor defined in the GAIA protocol.
     */
    private static final int FACTOR = 60;

    /**
     * <p>To build a new {@link Parameter} without any type which corresponds to the Master Gain of the Bank.</p>
     */
    public MasterGain () {
        super(null);
    }

    @Override
    String getLabel(double masterGain) {
        if (isConfigurable) {
            mDecimalFormat.setMaximumFractionDigits(1);
            return mDecimalFormat.format(masterGain) + " dB";
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
