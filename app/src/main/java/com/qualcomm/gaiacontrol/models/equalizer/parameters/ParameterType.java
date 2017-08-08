/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

/**
 * This enumeration defines all the different parameters a {@link com.qualcomm.gaiacontrol.models.equalizer.Band Band}
 * has.
 */

public enum ParameterType {
    /**
     * Each band has a filter type which is defined in the Filter enumeration.
     */
    FILTER,
    /**
     * Each band has a frequency parameter depending on the selected filter type.
     */
    FREQUENCY,
    /**
     * Each band has a gain parameter depending on the selected filter type.
     */
    GAIN,
    /**
     * Each band has a quality parameter depending on the selected filter type.
     */
    QUALITY;

    /**
     * To keep this array constantly without calling the values() method which is copying an array when it's called.
     */
    private static final ParameterType[] values = ParameterType.values();

    /**
     * To get the band type matching the corresponding int value in this enumeration.
     *
     * @param value
     *            the int value from which we want the matching band type.
     *
     * @return the matching band type.
     */
    public static ParameterType valueOf(int value) {
        if (value < 0 || value >= values.length) {
            return null;
        }

        return ParameterType.values[value];
    }

    /**
     * To get the number of values for this enumeration.
     *
     * @return the length of this enumeration.
     */
    public static int getSize() {
        return values.length;
    }
}
