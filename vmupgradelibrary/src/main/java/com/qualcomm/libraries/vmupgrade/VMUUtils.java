/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade;

import android.util.Log;

import com.qualcomm.libraries.vmupgrade.packet.VMUException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * This class contains all useful methods for this library.
 */
@SuppressWarnings("SameParameterValue")
public final class VMUUtils {

    /**
     * <p>The tag to display for logs.</p>
     */
    private static final String TAG = "VMUUtils";
    /**
     * The number of bits in a byte.
     */
    private static final int BITS_IN_BYTE = 8;
    /**
     * The number of bytes to define a long.
     */
    private static final int BYTES_IN_LONG = 8;
    /**
     * <p>The number of bytes contained in a int.</p>
     */
    private static final int BYTES_IN_INT = 4;
    /**
     * <p>The number of bytes contained in a short.</p>
     */
    private static final int BYTES_IN_SHORT = 2;

    /**
     * <p>Extract an <code>int</code> value from a <code>byte</code> array.</p>
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
     * Extract a <code>short</code> field from an array.
     * @param source The array to extract from.
     * @param offset Offset within source array.
     * @param length Number of bytes to use (maximum 2).
     * @param reverse True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted integer.
     */
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
     * Extract a <code>long</code> field from an array.
     * @param source The array to extract from.
     * @param offset Offset within source array.
     * @param length Number of bytes to use (maximum 8).
     * @param reverse True if bytes should be interpreted in reverse (little endian) order.
     * @return The extracted integer.
     */
    public static long extractLongFromByteArray(byte [] source, int offset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_LONG)
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_LONG);
        long result = 0;
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
     * <p>This method allows retrieval of a human readable representation of an hexadecimal value contains in a
     * <code>int</code>.</p>
     *
     * @param i
     *         The <code>int</code> value.
     *
     * @return The hexadecimal value as a <code>String</code>.
     */
    public static String getHexadecimalString(int i) {
        return String.format("0x%04X", i & 0xFFFF);
    }

    /**
     * <p>This method allows retrieval of a human readable representation of an hexadecimal value contains in a
     * <code>int</code>.</p>
     *
     * @param i
     *         The <code>int</code> value.
     *
     * @return The hexadecimal value as a <code>String</code>.
     */
    public static String getHexadecimalStringTwoBytes(int i) {
        return String.format("0x%02X", i & 0xFF);
    }

    /**
     * Convert a byte array to a human readable String.
     *
     * @param value
     *         The byte array.
     *
     * @return String object containing values in byte array formatted as hex.
     */
    public static String getHexadecimalStringFromBytes(byte[] value) {
        if (value == null)
            return "null";
        final StringBuilder stringBuilder = new StringBuilder(value.length * 2);
        //noinspection ForLoopReplaceableByForEach // the for loop used less ressources than the foreach one.
        for (int i = 0; i < value.length; i++) {
            stringBuilder.append(String.format("0x%02x ", value[i]));
        }
        return stringBuilder.toString();
    }

    /**
     * To obtain the MD5 checksum from a file.
     *
     * @param file
     *                  The path to the file which we want the MD5 checksum.
     */
    public static byte[] getMD5FromFile(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            }
            return digest.digest();
        }
        catch (Exception e) {
            Log.e(TAG, "Exception occurs when tried to get MD5 check sum for file: " + file.getName());
            Log.e(TAG, "Exception: " + e.getMessage());
            return new byte[0];
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (Exception e) {
                    Log.w(TAG, "Exception occurs when tried to get MD5 check sum for file: " + file.getName());
                    Log.w(TAG, "Exception: " + e.getMessage());
                }
            }
        }
    }

    /**
     * To retrieve an array of bytes from a file.
     *
     * @param file
     *              The file to obtain the bytes.
     */
    public static byte[] getBytesFromFile(File file) throws VMUException {
        byte[] result;

        try {
            InputStream inputStream = new FileInputStream(file);
            long fileLength = file.length();
            int length = (int) fileLength;
            if (length != fileLength) {
                throw new VMUException(VMUException.Type.FILE_TOO_BIG);
            }

            result = new byte[length];
            int bytesRead = inputStream.read(result);
            inputStream.close();

            // if the number of bytes read does not correspond to the length of the file, the result is not complete
            // if length is the maximum value possible, bytesRead is -1.
            if (bytesRead == length || (bytesRead == -1 && length == Integer.MAX_VALUE)) {
                return result;
            }
            else {
                throw new VMUException(VMUException.Type.GET_BYTES_FILE_FAILED);
            }
        }
        catch (IOException e) {
            throw new VMUException(VMUException.Type.GET_BYTES_FILE_FAILED, e.getMessage());
        }
    }

    /**
     * <p>This method allows copying of a int value into a byte array from the specified <code>offset</code> location to
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
    public static void copyIntIntoByteArray(int sourceValue, byte[] target, int targetOffset, int length, boolean reverse) {
        if (length < 0 | length > BYTES_IN_INT) {
            throw new IndexOutOfBoundsException("Length must be between 0 and " + BYTES_IN_INT);
        } else if (target.length < targetOffset + length) {
            throw new IndexOutOfBoundsException("The targeted location must be contained in the target array.");
        }

        if (reverse) {
            int shift = 0;
            int j = 0;
            for (int i = length - 1; i >= 0; i--) {
                int mask = 0xFF << shift;
                target[j + targetOffset] = (byte) ((sourceValue & mask) >> shift);
                shift += BITS_IN_BYTE;
                j++;
            }
        } else {
            int shift = (length - 1) * BITS_IN_BYTE;
            for (int i = 0; i < length; i++) {
                int mask = 0xFF << shift;
                target[i + targetOffset] = (byte) ((sourceValue & mask) >> shift);
                shift -= BITS_IN_BYTE;
            }
        }
    }
}
