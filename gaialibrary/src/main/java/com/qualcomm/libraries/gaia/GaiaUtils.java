/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia;

/**
 * <p>This class contains all generic methods which can be re-used.</p>
 */
@SuppressWarnings("SameParameterValue")
public final class GaiaUtils {

    /**
     * <p>The number of bytes contained in a int.</p>
     */
    private static final int BYTES_IN_INT = 4;
    /**
     * <p>The number of bits contained in a byte.</p>
     */
    private static final int BITS_IN_BYTE = 8;

    /**
     * <p>This method allows retrieval of a human readable representation of an hexadecimal value contained in a
     * <code>int</code>.</p>
     *
     * @param i
     *         The <code>int</code> value.
     *
     * @return The hexadecimal value as a <code>String</code>.
     */
    public static String getHexadecimalStringFromInt(int i) {
        return String.format("%04X", i & 0xFFFF);
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
     * <p>This method allows copy of an int value into a byte array from the specified <code>offset</code> location to
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

    /**
     * <p>To get a String label which corresponds to the given GAIA command.</p>
     * <p>The label is built as follows:
     * <ol>
     *     <Li>The value of the GAIA command as an hexadecimal given by {@link #getHexadecimalStringFromInt(int)
     *     getHexadecimalStringFromInt}.</Li>
     *     <li>The name of the GAIA command as defined in the protocol or <code>UNKNOWN</code> if the value cannot be
     *     matched with the known ones.</li>
     *     <li><i>Optional</i>: "(deprecated)" is the command had been deprecated.</li>
     * </ol></p>
     * <p>For instance, for the given value <code>384</code> the method will return <code>"0x0180
     * COMMAND_GET_CONFIGURATION_VERSION"</code>.</p>
     *
     * @param command
     *          The command to obtain a label for.
     *
     * @return the label corresponding to the given command.
     */
    @SuppressWarnings("deprecation")
    public static String getGAIACommandToString(int command) {
        String name = "UNKNOWN";
        String deprecated = "(deprecated)";
        switch (command) {

            case GAIA.COMMAND_SET_RAW_CONFIGURATION:
                name = "COMMAND_SET_RAW_CONFIGURATION" + deprecated;
                break;
            case GAIA.COMMAND_GET_CONFIGURATION_VERSION:
                name = "COMMAND_GET_CONFIGURATION_VERSION";
                break;
            case GAIA.COMMAND_SET_LED_CONFIGURATION:
                name = "COMMAND_SET_LED_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_LED_CONFIGURATION:
                name = "COMMAND_GET_LED_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_TONE_CONFIGURATION:
                name = "COMMAND_SET_TONE_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_TONE_CONFIGURATION:
                name = "COMMAND_GET_TONE_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_DEFAULT_VOLUME:
                name = "COMMAND_SET_DEFAULT_VOLUME";
                break;
            case GAIA.COMMAND_GET_DEFAULT_VOLUME:
                name = "COMMAND_GET_DEFAULT_VOLUME";
                break;
            case GAIA.COMMAND_FACTORY_DEFAULT_RESET:
                name = "COMMAND_FACTORY_DEFAULT_RESET";
                break;
            case GAIA.COMMAND_GET_CONFIGURATION_ID:
                name = "COMMAND_GET_CONFIGURATION_ID" + deprecated;
                break;
            case GAIA.COMMAND_SET_VIBRATOR_CONFIGURATION:
                name = "COMMAND_SET_VIBRATOR_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_VIBRATOR_CONFIGURATION:
                name = "COMMAND_GET_VIBRATOR_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_VOICE_PROMPT_CONFIGURATION:
                name = "COMMAND_SET_VOICE_PROMPT_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_VOICE_PROMPT_CONFIGURATION:
                name = "COMMAND_GET_VOICE_PROMPT_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_FEATURE_CONFIGURATION:
                name = "COMMAND_SET_FEATURE_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_FEATURE_CONFIGURATION:
                name = "COMMAND_GET_FEATURE_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_USER_EVENT_CONFIGURATION:
                name = "COMMAND_SET_USER_EVENT_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_USER_EVENT_CONFIGURATION:
                name = "COMMAND_GET_USER_EVENT_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_TIMER_CONFIGURATION:
                name = "COMMAND_SET_TIMER_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_TIMER_CONFIGURATION:
                name = "COMMAND_GET_TIMER_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_AUDIO_GAIN_CONFIGURATION:
                name = "COMMAND_SET_AUDIO_GAIN_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_AUDIO_GAIN_CONFIGURATION:
                name = "COMMAND_GET_AUDIO_GAIN_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_VOLUME_CONFIGURATION:
                name = "COMMAND_SET_VOLUME_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_VOLUME_CONFIGURATION:
                name = "COMMAND_GET_VOLUME_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_POWER_CONFIGURATION:
                name = "COMMAND_SET_POWER_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_POWER_CONFIGURATION:
                name = "COMMAND_GET_POWER_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_USER_TONE_CONFIGURATION:
                name = "COMMAND_SET_USER_TONE_CONFIGURATION";
                break;
            case GAIA.COMMAND_GET_USER_TONE_CONFIGURATION:
                name = "COMMAND_GET_USER_TONE_CONFIGURATION";
                break;
            case GAIA.COMMAND_SET_DEVICE_NAME:
                name = "COMMAND_SET_DEVICE_NAME";
                break;
            case GAIA.COMMAND_GET_DEVICE_NAME:
                name = "COMMAND_GET_DEVICE_NAME";
                break;
            case GAIA.COMMAND_SET_WLAN_CREDENTIALS:
                name = "COMMAND_SET_WLAN_CREDENTIALS";
                break;
            case GAIA.COMMAND_GET_WLAN_CREDENTIALS:
                name = "COMMAND_GET_WLAN_CREDENTIALS";
                break;
            case GAIA.COMMAND_SET_PEER_PERMITTED_ROUTING:
                name = "COMMAND_SET_PEER_PERMITTED_ROUTING";
                break;
            case GAIA.COMMAND_GET_PEER_PERMITTED_ROUTING:
                name = "COMMAND_GET_PEER_PERMITTED_ROUTING";
                break;
            case GAIA.COMMAND_SET_PERMITTED_NEXT_AUDIO_SOURCE:
                name = "COMMAND_SET_PERMITTED_NEXT_AUDIO_SOURCE";
                break;
            case GAIA.COMMAND_GET_PERMITTED_NEXT_AUDIO_SOURCE:
                name = "COMMAND_GET_PERMITTED_NEXT_AUDIO_SOURCE";
                break;
            case GAIA.COMMAND_SET_ONE_TOUCH_DIAL_STRING:
                name = "COMMAND_SET_ONE_TOUCH_DIAL_STRING";
                break;
            case GAIA.COMMAND_GET_ONE_TOUCH_DIAL_STRING:
                name = "COMMAND_GET_ONE_TOUCH_DIAL_STRING";
                break;
            case GAIA.COMMAND_GET_MOUNTED_PARTITIONS:
                name = "COMMAND_GET_MOUNTED_PARTITIONS";
                break;
            case GAIA.COMMAND_SET_DFU_PARTITION:
                name = "COMMAND_SET_DFU_PARTITION";
                break;
            case GAIA.COMMAND_GET_DFU_PARTITION:
                name = "COMMAND_GET_DFU_PARTITION";
                break;
            case GAIA.COMMAND_CHANGE_VOLUME:
                name = "COMMAND_CHANGE_VOLUME";
                break;
            case GAIA.COMMAND_DEVICE_RESET:
                name = "COMMAND_DEVICE_RESET";
                break;
            case GAIA.COMMAND_GET_BOOT_MODE:
                name = "COMMAND_GET_BOOT_MODE";
                break;
            case GAIA.COMMAND_SET_PIO_CONTROL:
                name = "COMMAND_SET_PIO_CONTROL";
                break;
            case GAIA.COMMAND_GET_PIO_CONTROL:
                name = "COMMAND_GET_PIO_CONTROL";
                break;
            case GAIA.COMMAND_SET_POWER_STATE:
                name = "COMMAND_SET_POWER_STATE";
                break;
            case GAIA.COMMAND_GET_POWER_STATE:
                name = "COMMAND_GET_POWER_STATE";
                break;
            case GAIA.COMMAND_SET_VOLUME_ORIENTATION:
                name = "COMMAND_SET_VOLUME_ORIENTATION";
                break;
            case GAIA.COMMAND_GET_VOLUME_ORIENTATION:
                name = "COMMAND_GET_VOLUME_ORIENTATION";
                break;
            case GAIA.COMMAND_SET_VIBRATOR_CONTROL:
                name = "COMMAND_SET_VIBRATOR_CONTROL";
                break;
            case GAIA.COMMAND_GET_VIBRATOR_CONTROL:
                name = "COMMAND_GET_VIBRATOR_CONTROL";
                break;
            case GAIA.COMMAND_SET_LED_CONTROL:
                name = "COMMAND_SET_LED_CONTROL";
                break;
            case GAIA.COMMAND_GET_LED_CONTROL:
                name = "COMMAND_GET_LED_CONTROL";
                break;
            case GAIA.COMMAND_FM_CONTROL:
                name = "COMMAND_FM_CONTROL";
                break;
            case GAIA.COMMAND_PLAY_TONE:
                name = "COMMAND_PLAY_TONE";
                break;
            case GAIA.COMMAND_SET_VOICE_PROMPT_CONTROL:
                name = "COMMAND_SET_VOICE_PROMPT_CONTROL";
                break;
            case GAIA.COMMAND_GET_VOICE_PROMPT_CONTROL:
                name = "COMMAND_GET_VOICE_PROMPT_CONTROL";
                break;
            case GAIA.COMMAND_CHANGE_AUDIO_PROMPT_LANGUAGE:
                name = "COMMAND_CHANGE_AUDIO_PROMPT_LANGUAGE";
                break;
            case GAIA.COMMAND_SET_SPEECH_RECOGNITION_CONTROL:
                name = "COMMAND_SET_SPEECH_RECOGNITION_CONTROL";
                break;
            case GAIA.COMMAND_GET_SPEECH_RECOGNITION_CONTROL:
                name = "COMMAND_GET_SPEECH_RECOGNITION_CONTROL";
                break;
            case GAIA.COMMAND_ALERT_LEDS:
                name = "COMMAND_ALERT_LEDS";
                break;
            case GAIA.COMMAND_ALERT_TONE:
                name = "COMMAND_ALERT_TONE";
                break;
            case GAIA.COMMAND_ALERT_EVENT:
                name = "COMMAND_ALERT_EVENT";
                break;
            case GAIA.COMMAND_ALERT_VOICE:
                name = "COMMAND_ALERT_VOICE";
                break;
            case GAIA.COMMAND_SET_AUDIO_PROMPT_LANGUAGE:
                name = "COMMAND_SET_AUDIO_PROMPT_LANGUAGE";
                break;
            case GAIA.COMMAND_GET_AUDIO_PROMPT_LANGUAGE:
                name = "COMMAND_GET_AUDIO_PROMPT_LANGUAGE";
                break;
            case GAIA.COMMAND_START_SPEECH_RECOGNITION:
                name = "COMMAND_START_SPEECH_RECOGNITION";
                break;
            case GAIA.COMMAND_SET_EQ_CONTROL:
                name = "COMMAND_SET_EQ_CONTROL";
                break;
            case GAIA.COMMAND_GET_EQ_CONTROL:
                name = "COMMAND_GET_EQ_CONTROL";
                break;
            case GAIA.COMMAND_SET_BASS_BOOST_CONTROL:
                name = "COMMAND_SET_BASS_BOOST_CONTROL";
                break;
            case GAIA.COMMAND_GET_BASS_BOOST_CONTROL:
                name = "COMMAND_GET_BASS_BOOST_CONTROL";
                break;
            case GAIA.COMMAND_SET_3D_ENHANCEMENT_CONTROL:
                name = "COMMAND_SET_3D_ENHANCEMENT_CONTROL";
                break;
            case GAIA.COMMAND_GET_3D_ENHANCEMENT_CONTROL:
                name = "COMMAND_GET_3D_ENHANCEMENT_CONTROL";
                break;
            case GAIA.COMMAND_SWITCH_EQ_CONTROL:
                name = "COMMAND_SWITCH_EQ_CONTROL";
                break;
            case GAIA.COMMAND_TOGGLE_BASS_BOOST_CONTROL:
                name = "COMMAND_TOGGLE_BASS_BOOST_CONTROL";
                break;
            case GAIA.COMMAND_TOGGLE_3D_ENHANCEMENT_CONTROL:
                name = "COMMAND_TOGGLE_3D_ENHANCEMENT_CONTROL";
                break;
            case GAIA.COMMAND_SET_EQ_PARAMETER:
                name = "COMMAND_SET_EQ_PARAMETER";
                break;
            case GAIA.COMMAND_GET_EQ_PARAMETER:
                name = "COMMAND_GET_EQ_PARAMETER";
                break;
            case GAIA.COMMAND_SET_EQ_GROUP_PARAMETER:
                name = "COMMAND_SET_EQ_GROUP_PARAMETER";
                break;
            case GAIA.COMMAND_GET_EQ_GROUP_PARAMETER:
                name = "COMMAND_GET_EQ_GROUP_PARAMETER";
                break;
            case GAIA.COMMAND_DISPLAY_CONTROL:
                name = "COMMAND_DISPLAY_CONTROL";
                break;
            case GAIA.COMMAND_ENTER_BLUETOOTH_PAIRING_MODE:
                name = "COMMAND_ENTER_BLUETOOTH_PAIRING_MODE";
                break;
            case GAIA.COMMAND_SET_AUDIO_SOURCE:
                name = "COMMAND_SET_AUDIO_SOURCE";
                break;
            case GAIA.COMMAND_GET_AUDIO_SOURCE:
                name = "COMMAND_GET_AUDIO_SOURCE";
                break;
            case GAIA.COMMAND_AV_REMOTE_CONTROL:
                name = "COMMAND_AV_REMOTE_CONTROL";
                break;
            case GAIA.COMMAND_SET_USER_EQ_CONTROL:
                name = "COMMAND_SET_USER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_GET_USER_EQ_CONTROL:
                name = "COMMAND_GET_USER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_TOGGLE_USER_EQ_CONTROL:
                name = "COMMAND_TOGGLE_USER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_SET_SPEAKER_EQ_CONTROL:
                name = "COMMAND_SET_SPEAKER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_GET_SPEAKER_EQ_CONTROL:
                name = "COMMAND_GET_SPEAKER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_TOGGLE_SPEAKER_EQ_CONTROL:
                name = "COMMAND_TOGGLE_SPEAKER_EQ_CONTROL";
                break;
            case GAIA.COMMAND_SET_TWS_AUDIO_ROUTING:
                name = "COMMAND_SET_TWS_AUDIO_ROUTING";
                break;
            case GAIA.COMMAND_GET_TWS_AUDIO_ROUTING:
                name = "COMMAND_GET_TWS_AUDIO_ROUTING";
                break;
            case GAIA.COMMAND_SET_TWS_VOLUME:
                name = "COMMAND_SET_TWS_VOLUME";
                break;
            case GAIA.COMMAND_GET_TWS_VOLUME:
                name = "COMMAND_GET_TWS_VOLUME";
                break;
            case GAIA.COMMAND_TRIM_TWS_VOLUME:
                name = "COMMAND_TRIM_TWS_VOLUME";
                break;
            case GAIA.COMMAND_SET_PEER_LINK_RESERVED:
                name = "COMMAND_SET_PEER_LINK_RESERVED";
                break;
            case GAIA.COMMAND_GET_PEER_LINK_RESERVED:
                name = "COMMAND_GET_PEER_LINK_RESERVED";
                break;
            case GAIA.COMMAND_TWS_PEER_START_ADVERTISING:
                name = "COMMAND_TWS_PEER_START_ADVERTISING";
                break;
            case GAIA.COMMAND_FIND_MY_REMOTE:
                name = "COMMAND_FIND_MY_REMOTE";
                break;
            case GAIA.COMMAND_SET_CODEC:
                name = "COMMAND_SET_CODEC";
                break;
            case GAIA.COMMAND_GET_CODEC:
                name = "COMMAND_GET_CODEC";
                break;
            case GAIA.COMMAND_SET_SUPPORTED_FEATURES:
                name = "COMMAND_SET_SUPPORTED_FEATURES";
                break;
            case GAIA.COMMAND_DISCONNECT:
                name = "COMMAND_DISCONNECT";
                break;
            case GAIA.COMMAND_GET_API_VERSION:
                name = "COMMAND_GET_API_VERSION";
                break;
            case GAIA.COMMAND_GET_CURRENT_RSSI:
                name = "COMMAND_GET_CURRENT_RSSI";
                break;
            case GAIA.COMMAND_GET_CURRENT_BATTERY_LEVEL:
                name = "COMMAND_GET_CURRENT_BATTERY_LEVEL";
                break;
            case GAIA.COMMAND_GET_MODULE_ID:
                name = "COMMAND_GET_MODULE_ID";
                break;
            case GAIA.COMMAND_GET_APPLICATION_VERSION:
                name = "COMMAND_GET_APPLICATION_VERSION";
                break;
            case GAIA.COMMAND_GET_PIO_STATE:
                name = "COMMAND_GET_PIO_STATE";
                break;
            case GAIA.COMMAND_READ_ADC:
                name = "COMMAND_READ_ADC";
                break;
            case GAIA.COMMAND_GET_PEER_ADDRESS:
                name = "COMMAND_GET_PEER_ADDRESS";
                break;
            case GAIA.COMMAND_GET_DFU_STATUS:
                name = "COMMAND_GET_DFU_STATUS" + deprecated;
                break;
            case GAIA.COMMAND_GET_HOST_FEATURE_INFORMATION:
                name = "COMMAND_GET_HOST_FEATURE_INFORMATION";
                break;
            case GAIA.COMMAND_GET_AUTH_BITMAPS:
                name = "COMMAND_GET_AUTH_BITMAPS";
                break;
            case GAIA.COMMAND_AUTHENTICATE_REQUEST:
                name = "COMMAND_AUTHENTICATE_REQUEST";
                break;
            case GAIA.COMMAND_AUTHENTICATE_RESPONSE:
                name = "COMMAND_AUTHENTICATE_RESPONSE";
                break;
            case GAIA.COMMAND_SET_FEATURE:
                name = "COMMAND_SET_FEATURE";
                break;
            case GAIA.COMMAND_GET_FEATURE:
                name = "COMMAND_GET_FEATURE";
                break;
            case GAIA.COMMAND_SET_SESSION_ENABLE:
                name = "COMMAND_SET_SESSION_ENABLE";
                break;
            case GAIA.COMMAND_GET_SESSION_ENABLE:
                name = "COMMAND_GET_SESSION_ENABLE";
                break;
            case GAIA.COMMAND_DATA_TRANSFER_SETUP:
                name = "COMMAND_DATA_TRANSFER_SETUP";
                break;
            case GAIA.COMMAND_DATA_TRANSFER_CLOSE:
                name = "COMMAND_DATA_TRANSFER_CLOSE";
                break;
            case GAIA.COMMAND_HOST_TO_DEVICE_DATA:
                name = "COMMAND_HOST_TO_DEVICE_DATA";
                break;
            case GAIA.COMMAND_DEVICE_TO_HOST_DATA:
                name = "COMMAND_DEVICE_TO_HOST_DATA";
                break;
            case GAIA.COMMAND_I2C_TRANSFER:
                name = "COMMAND_I2C_TRANSFER";
                break;
            case GAIA.COMMAND_GET_STORAGE_PARTITION_STATUS:
                name = "COMMAND_GET_STORAGE_PARTITION_STATUS";
                break;
            case GAIA.COMMAND_OPEN_STORAGE_PARTITION:
                name = "COMMAND_OPEN_STORAGE_PARTITION";
                break;
            case GAIA.COMMAND_OPEN_UART:
                name = "COMMAND_OPEN_UART";
                break;
            case GAIA.COMMAND_WRITE_STORAGE_PARTITION:
                name = "COMMAND_WRITE_STORAGE_PARTITION";
                break;
            case GAIA.COMMAND_WRITE_STREAM:
                name = "COMMAND_WRITE_STREAM";
                break;
            case GAIA.COMMAND_CLOSE_STORAGE_PARTITION:
                name = "COMMAND_CLOSE_STORAGE_PARTITION";
                break;
            case GAIA.COMMAND_MOUNT_STORAGE_PARTITION:
                name = "COMMAND_MOUNT_STORAGE_PARTITION";
                break;
            case GAIA.COMMAND_GET_FILE_STATUS:
                name = "COMMAND_GET_FILE_STATUS";
                break;
            case GAIA.COMMAND_OPEN_FILE:
                name = "COMMAND_OPEN_FILE";
                break;
            case GAIA.COMMAND_READ_FILE:
                name = "COMMAND_READ_FILE";
                break;
            case GAIA.COMMAND_CLOSE_FILE:
                name = "COMMAND_CLOSE_FILE";
                break;
            case GAIA.COMMAND_DFU_REQUEST:
                name = "COMMAND_DFU_REQUEST";
                break;
            case GAIA.COMMAND_DFU_BEGIN:
                name = "COMMAND_DFU_BEGIN";
                break;
            case GAIA.COMMAND_DFU_WRITE:
                name = "COMMAND_DFU_WRITE";
                break;
            case GAIA.COMMAND_DFU_COMMIT:
                name = "COMMAND_DFU_COMMIT";
                break;
            case GAIA.COMMAND_DFU_GET_RESULT:
                name = "COMMAND_DFU_GET_RESULT";
                break;
            case GAIA.COMMAND_VM_UPGRADE_CONNECT:
                name = "COMMAND_VM_UPGRADE_CONNECT";
                break;
            case GAIA.COMMAND_VM_UPGRADE_DISCONNECT:
                name = "COMMAND_VM_UPGRADE_DISCONNECT";
                break;
            case GAIA.COMMAND_VM_UPGRADE_CONTROL:
                name = "COMMAND_VM_UPGRADE_CONTROL";
                break;
            case GAIA.COMMAND_VM_UPGRADE_DATA:
                name = "COMMAND_VM_UPGRADE_DATA";
                break;
            case GAIA.COMMAND_NO_OPERATION:
                name = "COMMAND_NO_OPERATION";
                break;
            case GAIA.COMMAND_GET_DEBUG_FLAGS:
                name = "COMMAND_GET_DEBUG_FLAGS";
                break;
            case GAIA.COMMAND_SET_DEBUG_FLAGS:
                name = "COMMAND_SET_DEBUG_FLAGS";
                break;
            case GAIA.COMMAND_RETRIEVE_PS_KEY:
                name = "COMMAND_RETRIEVE_PS_KEY";
                break;
            case GAIA.COMMAND_RETRIEVE_FULL_PS_KEY:
                name = "COMMAND_RETRIEVE_FULL_PS_KEY";
                break;
            case GAIA.COMMAND_STORE_PS_KEY:
                name = "COMMAND_STORE_PS_KEY";
                break;
            case GAIA.COMMAND_FLOOD_PS:
                name = "COMMAND_FLOOD_PS";
                break;
            case GAIA.COMMAND_STORE_FULL_PS_KEY:
                name = "COMMAND_STORE_FULL_PS_KEY";
                break;
            case GAIA.COMMAND_SEND_DEBUG_MESSAGE:
                name = "COMMAND_SEND_DEBUG_MESSAGE";
                break;
            case GAIA.COMMAND_SEND_APPLICATION_MESSAGE:
                name = "COMMAND_SEND_APPLICATION_MESSAGE";
                break;
            case GAIA.COMMAND_SEND_KALIMBA_MESSAGE:
                name = "COMMAND_SEND_KALIMBA_MESSAGE";
                break;
            case GAIA.COMMAND_GET_MEMORY_SLOTS:
                name = "COMMAND_GET_MEMORY_SLOTS";
                break;
            case GAIA.COMMAND_GET_DEBUG_VARIABLE:
                name = "COMMAND_GET_DEBUG_VARIABLE";
                break;
            case GAIA.COMMAND_SET_DEBUG_VARIABLE:
                name = "COMMAND_SET_DEBUG_VARIABLE";
                break;
            case GAIA.COMMAND_DELETE_PDL:
                name = "COMMAND_DELETE_PDL";
                break;
            case GAIA.COMMAND_SET_BLE_CONNECTION_PARAMETERS:
                name = "COMMAND_SET_BLE_CONNECTION_PARAMETERS";
                break;
            case GAIA.COMMAND_REGISTER_NOTIFICATION:
                name = "COMMAND_REGISTER_NOTIFICATION";
                break;
            case GAIA.COMMAND_GET_NOTIFICATION:
                name = "COMMAND_GET_NOTIFICATION";
                break;
            case GAIA.COMMAND_CANCEL_NOTIFICATION:
                name = "COMMAND_CANCEL_NOTIFICATION";
                break;
            case GAIA.COMMAND_EVENT_NOTIFICATION:
                name = "COMMAND_EVENT_NOTIFICATION";
                break;
        }

        return getHexadecimalStringFromInt(command) + " " + name;
    }
}
