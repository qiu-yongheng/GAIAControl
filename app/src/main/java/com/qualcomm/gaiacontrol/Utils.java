/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.text.DecimalFormat;

/**
 * <p>This class contains all useful methods for this module.</p>
 */
@SuppressWarnings("WeakerAccess")
public class Utils {

    /**
     * The number of bits in a byte.
     */
    private static final int BITS_IN_BYTE = 8;
    /**
     * <p>The number of bytes contains in a int.</p>
     */
    public static final int BYTES_IN_INT = 4;
    /**
     * <p>The number of bytes contains in a short.</p>
     */
    private static final int BYTES_IN_SHORT = 2;
    /**
     * The number of bits contains in an hexadecimal value.
     */
    public static final int BITS_IN_HEXADECIMAL = 4;
    /**
     * To display a number in a specific decimal format.
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    /**
     * Convert a byte array to a human readable String.
     *
     * @param value
     *            The byte array.
     * @return String object containing values in byte array formatted as hex.
     */
    public static String getStringFromBytes(byte[] value) {
        if (value == null)
            return "null";
        final StringBuilder stringBuilder = new StringBuilder(value.length*2);
        //noinspection ForLoopReplaceableByForEach // the for loop is more efficient than the foreach one
        for (int i = 0; i < value.length; i++) {
            stringBuilder.append(String.format("0x%02x ", value[i]));
        }
        return stringBuilder.toString();
    }

    /**
     * Get 16-bit hexadecimal string representation of byte.
     *
     * @param i
     *            The value.
     *
     * @return Hex value as a string.
     */
    @SuppressWarnings("unused")
    public static String getIntToHexadecimal(int i) {
        return String.format("0x%04X", i & 0xFFFF);
    }

    /**
     * <p>This method returns the image which corresponds to the given rssi signal. it will return the following:</p>
     * <ul>
     *     <li>For <code>rssi</code> between <code>-60</code> and <code>0</code> the method returns
     *     {@link R.drawable#ic_signal_level_4_24dp ic_signal_level_4_24dp}</li>
     *     <li>For <code>rssi</code> between <code>-70</code> and <code>-60</code> the method returns
     *     {@link R.drawable#ic_signal_level_3_24dp ic_signal_level_3_24dp}</li>
     *     <li>For <code>rssi</code> between <code>-80</code> and <code>-70</code> the method returns
     *     {@link R.drawable#ic_signal_level_2_24dp ic_signal_level_2_24dp}</li>
     *     <li>For <code>rssi</code> between <code>-90</code> and <code>-80</code> the method returns
     *     {@link R.drawable#ic_signal_level_1_24dp ic_signal_level_1_24dp}</li>
     *     <li>For <code>rssi</code> less than <code>-90</code> the method returns
     *     {@link R.drawable#ic_signal_level_0_24dp ic_signal_level_0_24dp}</li>
     *     <li>For all other values the method returns
     *     {@link R.drawable#ic_signal_unknown_24dp ic_signal_unknown_24dp}</li>
     * </ul>
     *
     * @param context
     *          The context of the application for the method to be able to retrieve the image.
     * @param rssi
     *          The value for which we want to retrieve the corresponding image
     *
     * @return A drawable picture of a signal strength depending on the given rssi value.
     */
    @SuppressWarnings("deprecation")
    public static Drawable getSignalIconFromRssi(Context context, int rssi) {
        if (-60 <= rssi && rssi <= 0) {
            //noinspection deprecation
            return context.getResources().getDrawable(R.drawable.ic_signal_level_4_24dp);
        }
        else if (-70 <= rssi && rssi < -60) {
            return context.getResources().getDrawable(R.drawable.ic_signal_level_3_24dp);
        }
        else if (-80 <= rssi && rssi < -70) {
            return context.getResources().getDrawable(R.drawable.ic_signal_level_2_24dp);
        }
        else if (-90 <= rssi && rssi < -80) {
            return context.getResources().getDrawable(R.drawable.ic_signal_level_1_24dp);
        }
        else if (rssi < -90) {
            return context.getResources().getDrawable(R.drawable.ic_signal_level_0_24dp);
        }
        else {
            return context.getResources().getDrawable(R.drawable.ic_signal_unknown_24dp);
        }
    }

    /**
     * <p>This method allows to copy a int value into a byte array from the specified <code>offset</code> location to
     * the <code>offset + length</code> location.</p>
     *
     * @param sourceValue
     *         The <code>int</code> value to copy in the array.
     * @param target
     *         The <code>byte</code> array to copy in the <code>int</code> value.
     * @param targetOffset
     *         The targeted offset in the array to copy the first byte of the <code>int</code> value.
     * @param length
     *         The number of bytes in the array to copy the <code>int</code> value.
     * @param reverse
     *         True if bytes should be interpreted in reverse (little endian) order.
     */
    @SuppressWarnings("SameParameterValue")
    public static void copyIntIntoByteArray(int sourceValue, byte [] target, int targetOffset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_INT) {
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_INT);
        }

        if (reverse) {
            int shift = 0;
            int j = 0;
            for (int i = length-1; i >= 0; i--) {
                int mask = 0xFF << shift;
                target[j+targetOffset] = (byte)((sourceValue & mask) >> shift);
                shift += BITS_IN_BYTE;
                j++;
            }
        }
        else {
            int shift = (length-1) * BITS_IN_BYTE;
            for (int i = 0; i < length; i++) {
                int mask = 0xFF << shift;
                target[i+targetOffset] = (byte)((sourceValue & mask) >> shift);
                shift -= BITS_IN_BYTE;
            }
        }
    }

    /**
     * Extract a <code>short</code> field from an array.
     * @param source The array to extract from.
     * @param offset Offset within source array.
     * @param length Number of bytes to use (maximum 2).
     * @param reverse True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted integer.
     */
    @SuppressWarnings("SameParameterValue")
    public static short extractShortFromByteArray(byte [] source, int offset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_SHORT)
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_SHORT);
        short result = 0;
        int shift = (length-1) * BITS_IN_BYTE;

        if (reverse) {
            for (int i = offset+length-1; i >= offset; i--) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        else {
            for (int i = offset; i < offset+length; i++) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        return result;
    }

    /**
     * <p>Extract an <code>int</code> value from a <code>bytes</code> array.</p>
     *
     * @param source
     *         The array to extract from.
     * @param offset
     *         Offset within source array.
     * @param length
     *         Number of bytes to use (maximum 4).
     * @param reverse
     *         True if bytes should be interpreted in reverse (little endian) order.
     *
     * @return The extracted <code>int</code>.
     */
    @SuppressWarnings("SameParameterValue")
    public static int extractIntFromByteArray(byte[] source, int offset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_INT)
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_INT);
        int result = 0;
        int shift = (length - 1) * BITS_IN_BYTE;

        if (reverse) {
            for (int i = offset + length - 1; i >= offset; i--) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        } else {
            for (int i = offset; i < offset + length; i++) {
                result |= ((source[i] & 0xFF) << shift);
                shift -= BITS_IN_BYTE;
            }
        }
        return result;
    }

    /**
     * <p>To get the percentage as a String, formatted with the % type as follow.</p>
     *
     * @return the percentage as a formatted String value as follow: <code>value %</code>
     */
    public static String getStringForPercentage(double percentage) {
        if (percentage <= 1) {
            DECIMAL_FORMAT.setMaximumFractionDigits(2);
        }
        else {
            DECIMAL_FORMAT.setMaximumFractionDigits(1);
        }

        return DECIMAL_FORMAT.format(percentage) + " " + Consts.PERCENTAGE_CHARACTER;
    }

    /**
     * <p>To get a readable value of the remaining time.</p>
     * <p>This method will only returns:</p>
     * <ul>
     *     <li>the number of hours if the remaining time is more than 1 hour.</li>
     *     <li>the number of minutes if the remaining time is between 1 hour and 1 minute.</li>
     *     <li>the number of seconds if the remaining time is less than 1 minute.</li>
     * </ul>
     *
     * @return A human readable value of the remaining time.
     */
    public static String getStringForTime(long time) {
        long seconds = time / 1000;

        // if less than 1 min we return only the seconds
        if (seconds < 60) {
            seconds = seconds - (seconds % 5);
            return seconds + "s";
        }

        // otherwise we check the minutes
        long minutes = seconds / 60;
        long remainingSeconds = seconds - minutes*60;

        // minutes <5 format 'xminxxs'
        if (minutes < 10) {
            remainingSeconds = remainingSeconds - (remainingSeconds % 10);
            if (remainingSeconds == 0) {
                return minutes + "min";
            }
            return minutes + "min" + remainingSeconds + "s";
        }
        // we consider that remaining seconds >30 ==> min=min+1
        if (remainingSeconds >= 30) {
            minutes++;
        }
        // minutes < 60 format 'xxmin'
        if (minutes < 60) {
            return minutes + "min";
        }

        // otherwise we check the hours
        long hours = minutes / 60;
        long remainingMinutes = minutes - hours*60;

        if (hours < 12) {
            remainingMinutes = remainingMinutes - (remainingMinutes % 5);
            if (remainingMinutes == 0) {
                return hours + "h";
            }
            return hours + "h" + remainingMinutes + "min";
        }

        if (hours < 24) {
            remainingMinutes = remainingMinutes - (remainingMinutes % 30);
            return hours + "h" + remainingMinutes + "min";
        }

        if (remainingMinutes > 30) {
            hours++;
        }

        return hours + "h";
    }
}