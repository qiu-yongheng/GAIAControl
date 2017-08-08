/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

/**
 * This enumeration encapsulates all possible values for the band filter parameter.
 */
public enum Filter {
    /**
     * The "Bypass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 0</li>
     *     <li>no frequency</li>
     *     <li>no gain</li>
     *     <li>no quality</li>
     * </ul>
     */
    BYPASS,
    /**
     * The "First Order Low Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 1</li>
     *     <li>frequency from 0.3Hz to 20 kHz</li>
     *     <li>no gain</li>
     *     <li>no quality</li>
     * </ul>
     */
    LOW_PASS_1,
    /**
     * The "First Order High Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 2</li>
     *     <li>frequency from 0.3Hz to 20 kHz</li>
     *     <li>no gain</li>
     *     <li>no quality</li>
     * </ul>
     */
    HIGH_PASS_1,
    /**
     * The "First Order All Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 3</li>
     *     <li>frequency from 0.3Hz to 20 kHz</li>
     *     <li>no gain</li>
     *     <li>no quality</li>
     * </ul>
     */
    ALL_PASS_1,
    /**
     * The "First Order Low Shelf" band filter has the following characteristics:
     * <ul>
     *     <li>number: 4</li>
     *     <li>frequency from 20Hz to 20 kHz</li>
     *     <li>gain from -12dB to 12dB</li>
     *     <li>no quality</li>
     * </ul>
     */
    LOW_SHELF_1,
    /**
     * The "First Order High Shelf" band filter has the following characteristics:
     * <ul>
     *     <li>number: 5</li>
     *     <li>frequency from 20Hz to 20 kHz</li>
     *     <li>gain from -12dB to 12dB</li>
     *     <li>no quality</li>
     * </ul>
     */
    HIGH_SHELF_1,
    /**
     * The "First Order Tilt" band filter has the following characteristics:
     * <ul>
     *     <li>number: 6</li>
     *     <li>frequency from 20Hz to 20 kHz</li>
     *     <li>gain from -12dB to 12dB</li>
     *     <li>no quality</li>
     * </ul>
     */
    TILT_1,
    /**
     * The "Second Order Low Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 7</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>no gain</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    LOW_PASS_2,
    /**
     * The "Second Order High Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 8</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>no gain</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    HIGH_PASS_2,
    /**
     * The "Second Order All Pass" band filter has the following characteristics:
     * <ul>
     *     <li>number: 9</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>no gain</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    ALL_PASS_2,
    /**
     * The "Second Order Low Shelf" band filter has the following characteristics:
     * <ul>
     *     <li>number: 10</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>gain from -12 dB to +12 dB</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    LOW_SHELF_2,
    /**
     * The "Second Order High Shelf" band filter has the following characteristics:
     * <ul>
     *     <li>number: 11</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>gain from -12 dB to +12 dB</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    HIGH_SHELF_2,
    /**
     * The "Second Order Tilt" band filter has the following characteristics:
     * <ul>
     *     <li>number: 12</li>
     *     <li>frequency from 40Hz to 20kHz</li>
     *     <li>gain from -12 dB to +12 dB</li>
     *     <li>quality from 0.25 to 2</li>
     * </ul>
     */
    TILT_2,
    /**
     * The "Parametric Equalizer" band filter has the following characteristics:
     * <ul>
     *     <li>number: 13</li>
     *     <li>frequency from 20Hz to 20kHz</li>
     *     <li>gain from -36 dB to +12 dB</li>
     *     <li>quality from 0.25 to 8.0</li>
     * </ul>
     */
    PARAMETRIC_EQUALIZER;

    /**
     * The values() methods copies an array when it's called. In order to be more efficient this enumeration keeps an
     * instance of an array created by the values() method.
     */
    private static final Filter[] values = Filter.values();

    /**
     * To get the band type matching the corresponding int value in this enumeration.
     *
     * @param value
     *            the int value from which we want the matching band type.
     *
     * @return the matching filter type.
     */
    public static Filter valueOf(int value) {
        if (value < 0 || value >= values.length) {
            return null;
        }

        return Filter.values[value];
    }

    /**
     * To get the number of values for this enumeration.
     *
     * @return the length of this enumeration.
     */
    public static int getSize() {
        return values.length;
    }

    /**
     * To get the parameter ranges corresponding to the given filter.
     *
     * @param filter
     *              the filter for which we want the characteristics.
     * @param frequency
     *              the frequency parameter to define its range.
     * @param gain
     *              the gain parameter to define its range.
     * @param quality
     *              the quality parameter to define its range.
     */
    public static void defineParameters(Filter filter, Parameter frequency,
                                        Parameter gain, Parameter quality) {
        switch (filter) {
        case HIGH_PASS_1:
        case ALL_PASS_1:
        case LOW_PASS_1:
            // frequency 0.3Hz to 20 kHz, no gain, no quality
            frequency.setConfigurable(0.333, 20000);
            gain.setNotConfigurable();
            quality.setNotConfigurable();
            break;

        case HIGH_PASS_2:
        case ALL_PASS_2:
        case LOW_PASS_2:
            // frequency from 40Hz to 20kHz, no gain, quality from 0.25 to 2.0
            frequency.setConfigurable(40, 20000);
            gain.setNotConfigurable();
            quality.setConfigurable(0.25, 2); // 2.0 according to the user EQ documentation CS-309844-DC
            break;

        case LOW_SHELF_1:
        case HIGH_SHELF_1:
        case TILT_1:
            // frequency from 20Hz to 20 kHz, gain from -12dB to 12dB, no quality
            frequency.setConfigurable(20, 20000);
            gain.setConfigurable(-12, 12);
            quality.setNotConfigurable();
            break;

        case LOW_SHELF_2:
        case HIGH_SHELF_2:
        case TILT_2:
            // frequency from 40Hz to 20kHz, gain from -12 dB to +12 dB, quality from 0.25 to 2.0
            frequency.setConfigurable(40, 20000);
            gain.setConfigurable(-12, 12);
            quality.setConfigurable(0.25, 2); // 2.0 according to the user EQ documentation CS-309844-DC
            break;

        case BYPASS:
            // no frequency, no gain, no quality
            frequency.setNotConfigurable();
            gain.setNotConfigurable();
            quality.setNotConfigurable();
            break;

        case PARAMETRIC_EQUALIZER:
            // frequency from 20Hz to 20kHz, gain from -36dB to 12dB, quality from 0.25 to 8.0
            frequency.setConfigurable(20, 20000);
            gain.setConfigurable(-36, 12);
            quality.setConfigurable(0.25, 8);
            break;

        }
    }
}
