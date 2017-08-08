/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.gaia;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class contains all characteristics constants for the GAIA wire protocol.
 * GAIA is the Generic Application Interface Architecture defined by Qualcomm for devices to communicate over a
 * Bluetooth connection.</p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class GAIA {

    /**
     * <p>The mask which represents a command.</p>
     * <p>Mask used to retrieve the command from the packet.</p>
     *
     * @see #ACKNOWLEDGMENT_MASK <code>ACKNOWLEDGMENT_MASK</code> to know if a command is an acknowledgement
     */
    public static final int COMMAND_MASK = 0x7FFF;
    /**
     * <p>The mask which represents an acknowledgement.</p>
     * <ul>
     *     <li><code>COMMAND & ACKNOWLEDGMENT_MASK > 0</code> to know if the command is an acknowledgement.</li>
     *     <li><code>COMMAND | ACKNOWLEDGMENT_MASK</code> to build the acknowledgement command of a command.</li>
     * </ul>
     *
     * @see #COMMAND_MASK <code>COMMAND_MASK</code> to know how to retrieve the command number
     */
    public static final int ACKNOWLEDGMENT_MASK = 0x8000;

    /**
     * <p>The default value defined by the protocol for a "none" vendor.</p>
     */
    public static final int VENDOR_NONE = 0x7FFE;
    /**
     * <p>The vendor default value defined by the protocol for Qualcomm vendor.</p>
     */
    public static final int VENDOR_QUALCOMM = 0x000A;
    
    
    // -------------------------------------------------------------------
    // |                  CONFIGURATION COMMANDS 0x01nn                  |
    // -------------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a configuration command.</p>
     */
    public static final int COMMANDS_CONFIGURATION_MASK = 0x0100;
    /**
     * @deprecated
     */
    public static final int COMMAND_SET_RAW_CONFIGURATION = 0x0100;
    /**
     * <p>Retrieves the version of the configuration set.</p>
     */
    public static final int COMMAND_GET_CONFIGURATION_VERSION = 0x0180;
    /**
     * <p>Configures the LED indicators. Determines patterns to be displayed in given states and on events and
     * configures filters to be applied as events occur.</p>
     */
    public static final int COMMAND_SET_LED_CONFIGURATION = 0x0101;
    /**
     * <p>Retrieves the current LED configuration.</p>
     */
    public static final int COMMAND_GET_LED_CONFIGURATION = 0x0181;
    /**
     * <p>Configures informational tones on the device.</p>
     */
    public static final int COMMAND_SET_TONE_CONFIGURATION = 0x0102;
    /**
     * <p>Retrieves the currently configured tone configuration.</p>
     */
    public static final int COMMAND_GET_TONE_CONFIGURATION = 0x0182;
    /**
     * <p>Sets the default volume for tones and audio.</p>
     */
    public static final int COMMAND_SET_DEFAULT_VOLUME = 0x0103;
    /**
     * <p>Requests the default volume settings for tones and audio.</p>
     */
    public static final int COMMAND_GET_DEFAULT_VOLUME = 0x0183;
    /**
     * <p>Resets all settings (deletes PS keys) which override factory defaults.</p>
     */
    public static final int COMMAND_FACTORY_DEFAULT_RESET = 0x0104;
    /**
     * @deprecated
     */
    public static final int COMMAND_GET_CONFIGURATION_ID = 0x0184;
    /**
     * <p>Configures per-event vibrator patterns.</p>
     */
    public static final int COMMAND_SET_VIBRATOR_CONFIGURATION = 0x0105;
    /**
     * <p>Retrieves the currently configured vibrator configuration.</p>
     */
    public static final int COMMAND_GET_VIBRATOR_CONFIGURATION = 0x0185;
    /**
     * <p>Configures voice prompts to select a different language, voice etc.</p>
     */
    public static final int COMMAND_SET_VOICE_PROMPT_CONFIGURATION = 0x0106;
    /**
     * <p>Retrieves the currently configured voice prompt configuration.</p>
     */
    public static final int COMMAND_GET_VOICE_PROMPT_CONFIGURATION = 0x0186;
    /**
     * <p>Configures device features. The feature identifiers are application dependent and would be documented with the
     * application.</p>
     */
    public static final int COMMAND_SET_FEATURE_CONFIGURATION = 0x0107;
    /**
     * <p>Retrieves settings of device features.</p>
     */
    public static final int COMMAND_GET_FEATURE_CONFIGURATION = 0x0187;
    /**
     * <p>Set User Event Configuration.</p>
     */
    public static final int COMMAND_SET_USER_EVENT_CONFIGURATION = 0x0108;
    /**
     * <p>Get User Event Configuration.</p>
     */
    public static final int COMMAND_GET_USER_EVENT_CONFIGURATION = 0x0188;
    /**
     * <p>Configures the various timers on the device. This command has a long form (where the payload holds the value
     * of every timer) and a short form (where the payload holds a timer number and the value of that timer).</p>
     */
    public static final int COMMAND_SET_TIMER_CONFIGURATION = 0x0109;
    /**
     * <p>Retrieves the configuration of the various timers on the device. This command has a long form (where the
     * response holds the value of every timer) and a short form (where the command payload holds a timer number and
     * the response holds the number and value of that timer).</p>
     */
    public static final int COMMAND_GET_TIMER_CONFIGURATION = 0x0189;
    /**
     * <p>Configures the device volume control for each of the 16 volume levels.</p>
     */
    public static final int COMMAND_SET_AUDIO_GAIN_CONFIGURATION = 0x010A;
    /**
     * <p>Requests the device volume control configuration for each of the 16 volume levels.</p>
     */
    public static final int COMMAND_GET_AUDIO_GAIN_CONFIGURATION = 0x018A;
    /**
     * <p>Set Volume Configuration.</p>
     */
    public static final int COMMAND_SET_VOLUME_CONFIGURATION = 0x010B;
    /**
     * <p>Get Volume Configuration.</p>
     */
    public static final int COMMAND_GET_VOLUME_CONFIGURATION = 0x018B;
    /**
     * <p>Set Power Configuration.</p>
     */
    public static final int COMMAND_SET_POWER_CONFIGURATION = 0x010C;
    /**
     * <p>Get Power Configuration.</p>
     */
    public static final int COMMAND_GET_POWER_CONFIGURATION = 0x018C;
    /**
     * <p>Set User Tone Configuration.</p>
     */
    public static final int COMMAND_SET_USER_TONE_CONFIGURATION = 0x010E;
    /**
     * <p>Get User Tone Configuration.</p>
     */
    public static final int COMMAND_GET_USER_TONE_CONFIGURATION = 0x018E;
    /**
     * <p>Set device name.</p>
     */
    public static final int COMMAND_SET_DEVICE_NAME = 0x010F;
    /**
     * <p>Get device name.</p>
     */
    public static final int COMMAND_GET_DEVICE_NAME = 0x018F;
    /**
     * <p>Sets the credentials to access the Wi-Fi access point.</p>
     */
    public static final int COMMAND_SET_WLAN_CREDENTIALS = 0x0110;
    /**
     * <p>Retrieves the credentials to access the Wi-Fi access point.</p>
     */
    public static final int COMMAND_GET_WLAN_CREDENTIALS = 0x0190;
    /**
     * <p>Sets peer permitted routing.</p>
     */
    public static final int COMMAND_SET_PEER_PERMITTED_ROUTING = 0x0111;
    /**
     * <p>Gets peer permitted routing.</p>
     */
    public static final int COMMAND_GET_PEER_PERMITTED_ROUTING = 0x0191;
    /**
     * <p>Sets permitted next audio source.</p>
     */
    public static final int COMMAND_SET_PERMITTED_NEXT_AUDIO_SOURCE = 0x0112;
    /**
     * <p>Gets permitted next audio source.</p>
     */
    public static final int COMMAND_GET_PERMITTED_NEXT_AUDIO_SOURCE = 0x0192;
    /**
     * <p>Sets the string to be sent to an AG to be dialled when the one-touch dialling feature is used.</p>
     */
    public static final int COMMAND_SET_ONE_TOUCH_DIAL_STRING = 0x0116;
    /**
     * <p>Returns the string to be sent to an AG to be dialled when the one-touch dialling feature is used.</p>
     */
    public static final int COMMAND_GET_ONE_TOUCH_DIAL_STRING = 0x0196;
    /**
     * <p>Gets Mounted partitions.</p>
     */
    public static final int COMMAND_GET_MOUNTED_PARTITIONS = 0x01A0;
    /**
     * <p>Configures which SQIF partition is to be used for DFU operations.</p>
     */
    public static final int COMMAND_SET_DFU_PARTITION = 0x0121;
    /**
     * <p>Retrieves the index and size of the configured DFU partition.</p>
     */
    public static final int COMMAND_GET_DFU_PARTITION = 0x01A1;


    // --------------------------------------------------------------
    // |                  CONTROLS COMMANDS 0x02nn                  |
    // --------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a configuration command.</p>
     */
    public static final int COMMANDS_CONTROLS_MASK = 0x0200;
    /**
     * <p>The host can raise/lower the current volume or mute/unmute audio using this command.</p>
     */
    public static final int COMMAND_CHANGE_VOLUME = 0x0201;
    /**
     * <p>A host can cause a device to warm reset using this command. The device will transmit an acknowledgement and
     * then do a warm reset.</p>
     */
    public static final int COMMAND_DEVICE_RESET = 0x0202;
    /**
     * <p>Requests the device's current boot mode.</p>
     */
    public static final int COMMAND_GET_BOOT_MODE = 0x0282;
    /**
     * <p>Sets the state of device PIO pins.</p>
     */
    public static final int COMMAND_SET_PIO_CONTROL = 0x0203;
    /**
     * <p>Gets the state of device PIOs.</p>
     */
    public static final int COMMAND_GET_PIO_CONTROL = 0x0283;
    /**
     * <p>The host can request the device to physically power on or off by sending a <code>SET_POWER_STATE</code>
     * command. The device will transmit an acknowledgement in response to the hosts request, if accepted the device
     * shall also physically power on / off.</p>
     */
    public static final int COMMAND_SET_POWER_STATE = 0x0204;
    /**
     * <p>The host can request to retrieve the devices current power state. The device will transmit an acknowledgement
     * and if successful, shall also indicate its current power state.</p>
     */
    public static final int COMMAND_GET_POWER_STATE = 0x0284;
    /**
     * <p>Sets the orientation of the volume control buttons on the device.</p>
     */
    public static final int COMMAND_SET_VOLUME_ORIENTATION = 0x0205;
    /**
     * <p>Requests the current orientation of the volume control buttons on the device.</p>
     */
    public static final int COMMAND_GET_VOLUME_ORIENTATION = 0x0285;
    /**
     * <p>Enables or disables use of the vibrator in the headset, if one is present.</p>
     */
    public static final int COMMAND_SET_VIBRATOR_CONTROL = 0x0206;
    /**
     * <p>Requests the current setting of the vibrator.</p>
     */
    public static final int COMMAND_GET_VIBRATOR_CONTROL = 0x0286;
    /**
     * <p>Enables or disables LEDs (or equivalent indicators) on the headset.</p>
     */
    public static final int COMMAND_SET_LED_CONTROL = 0x0207;
    /**
     * <p>Establishes whether LED indicators are enabled.</p>
     */
    public static final int COMMAND_GET_LED_CONTROL = 0x0287;
    /**
     * <p>Sent from a headset to control an FM receiver on the phone, or from a handset to control a receiver in a
     * headset.</p>
     */
    public static final int COMMAND_FM_CONTROL = 0x0208;
    /**
     * <p>Play tone.</p>
     */
    public static final int COMMAND_PLAY_TONE = 0x0209;
    /**
     * <p>Enables or disables voice prompts on the headset.</p>
     */
    public static final int COMMAND_SET_VOICE_PROMPT_CONTROL = 0x020A;
    /**
     * <p>Establishes whether voice prompts are enabled.</p>
     */
    public static final int COMMAND_GET_VOICE_PROMPT_CONTROL = 0x028A;
    /**
     * <p>Selects the next available language for Text-to-Speech functions.</p>
     */
    public static final int COMMAND_CHANGE_AUDIO_PROMPT_LANGUAGE = 0x020B;
    /**
     * <p>Enables or disables simple speech recognition on the headset.</p>
     */
    public static final int COMMAND_SET_SPEECH_RECOGNITION_CONTROL = 0x020C;
    /**
     * <p>Establishes whether speech recognition is enabled.</p>
     */
    public static final int COMMAND_GET_SPEECH_RECOGNITION_CONTROL = 0x028C;
    /**
     * <p>Alert LEDs.</p>
     */
    public static final int COMMAND_ALERT_LEDS = 0x020D;
    /**
     * <p>Alert tone.</p>
     */
    public static final int COMMAND_ALERT_TONE = 0x020E;
    /**
     * <p>Alert the device user with LED patterns, tones or vibration. The method and meaning of each alert is
     * application-dependent and is configured using the appropriate LED, tone or vibrator event configuration.</p>
     */
    public static final int COMMAND_ALERT_EVENT = 0x0210;
    /**
     * <p>Alert voice.</p>
     */
    public static final int COMMAND_ALERT_VOICE = 0x0211;
    /**
     * <p>Sets audio prompt language.</p>
     */
    public static final int COMMAND_SET_AUDIO_PROMPT_LANGUAGE = 0x0212;
    /**
     * <p>Gets audio prompt language.</p>
     */
    public static final int COMMAND_GET_AUDIO_PROMPT_LANGUAGE = 0x0292;
    /**
     * <p>Starts the Simple Speech Recognition engine on the device. A successful acknowledgement indicates that speech
     * recognition has started; the actual speech recognition result will be relayed later via a
     * <code>GAIA_EVENT_SPEECH_RECOGNITION</code> notification.</p>
     */
    public static final int COMMAND_START_SPEECH_RECOGNITION = 0x0213;
    /**
     * <p>Selects an audio equaliser preset.</p>
     */
    public static final int COMMAND_SET_EQ_CONTROL = 0x0214;
    /**
     * <p>Gets the currently selected audio equaliser preset.</p>
     */
    public static final int COMMAND_GET_EQ_CONTROL = 0x0294;
    /**
     * <p>Enables or disables bass boost on the headset.</p>
     */
    public static final int COMMAND_SET_BASS_BOOST_CONTROL = 0x0215;
    /**
     * <p>Establishes whether bass boost is enabled.</p>
     */
    public static final int COMMAND_GET_BASS_BOOST_CONTROL = 0x0295;
    /**
     * <p>Enables or disables 3D sound enhancement on the headset.</p>
     */
    public static final int COMMAND_SET_3D_ENHANCEMENT_CONTROL = 0x0216;
    /**
     * <p>Establishes whether 3D Enhancement is enabled.</p>
     */
    public static final int COMMAND_GET_3D_ENHANCEMENT_CONTROL = 0x0296;
    /**
     * Switches to the next available equaliser preset. If issued while the last available preset is selected, switches
     * to the first.</p>
     */
    public static final int COMMAND_SWITCH_EQ_CONTROL = 0x0217;
    /**
     * <p>Turns on the Bass Boost effect if it was turned off; turns Bass Boost off if it was on.</p>
     */
    public static final int COMMAND_TOGGLE_BASS_BOOST_CONTROL = 0x0218;
    /**
     * <p>Turns on the 3D Enhancement effect if it was turned off; turns 3D Enhancement off if it was on.</p>
     */
    public static final int COMMAND_TOGGLE_3D_ENHANCEMENT_CONTROL = 0x0219;
    /**
     * <p>Sets a parameter of the parametric equaliser and optionally recalculates the filter coefficients.</p>
     */
    public static final int COMMAND_SET_EQ_PARAMETER = 0x021A;
    /**
     * <p>Gets a parameter of the parametric equaliser.</p>
     */
    public static final int COMMAND_GET_EQ_PARAMETER = 0x029A;
    /**
     * <p>Sets a group of parameters of the parametric equaliser.</p>
     */
    public static final int COMMAND_SET_EQ_GROUP_PARAMETER = 0x021B;
    /**
     * <p>Gets a group of parameters of the parametric equaliser.</p>
     */
    public static final int COMMAND_GET_EQ_GROUP_PARAMETER = 0x029B;
    /**
     * <p>Display control.</p>
     */
    public static final int COMMAND_DISPLAY_CONTROL = 0x021C;
    /**
     * <p>Puts a Bluetooth device into pairing mode, making it discoverable and connectable.</p>
     */
    public static final int COMMAND_ENTER_BLUETOOTH_PAIRING_MODE = 0x021D;
    /**
     * <p>Sets the device audio source.</p>
     */
    public static final int COMMAND_SET_AUDIO_SOURCE = 0x021E;
    /**
     * <p>Gets the currently selected audio source.</p>
     */
    public static final int COMMAND_GET_AUDIO_SOURCE = 0x029E;
    /**
     * <p>Sends an AVRC command to the device.</p>
     */
    public static final int COMMAND_AV_REMOTE_CONTROL = 0x021F;
    /**
     * <p>Enables or disables the User-configured parametric equaliser on the device (compare Set EQ Control).</p>
     */
    public static final int COMMAND_SET_USER_EQ_CONTROL = 0x0220;
    /**
     * <p>Establishes whether User EQ is enabled.</p>
     */
    public static final int COMMAND_GET_USER_EQ_CONTROL = 0x02A0;
    /**
     * <p>Turns on the User EQ if it was turned off; turns User EQ off if it was on.</p>
     */
    public static final int COMMAND_TOGGLE_USER_EQ_CONTROL = 0x0221;
    /**
     * <p>Enables or disables the speaker equaliser on the device.</p>
     */
    public static final int COMMAND_SET_SPEAKER_EQ_CONTROL = 0x0222;
    /**
     * <p>Establishes whether Speaker EQ is enabled.</p>
     */
    public static final int COMMAND_GET_SPEAKER_EQ_CONTROL = 0x02A2;
    /**
     * <p>Turns on the Speaker EQ if it was turned off; turns Speaker EQ off if it was on.</p>
     */
    public static final int COMMAND_TOGGLE_SPEAKER_EQ_CONTROL = 0x0223;
    /**
     * <p>Controls the routing of True Wireless Stereo channels.</p>
     */
    public static final int COMMAND_SET_TWS_AUDIO_ROUTING = 0x0224;
    /**
     * <p>Returns the current routing of True Wireless Stereo channels.</p>
     */
    public static final int COMMAND_GET_TWS_AUDIO_ROUTING = 0x02A4;
    /**
     * <p>Controls the volume of True Wireless Stereo output.</p>
     */
    public static final int COMMAND_SET_TWS_VOLUME = 0x0225;
    /**
     * <p>Returns the current volume setting of True Wireless Stereo.</p>
     */
    public static final int COMMAND_GET_TWS_VOLUME = 0x02A5;
    /**
     * <p>Trims the volume of True Wireless Stereo output.</p>
     */
    public static final int COMMAND_TRIM_TWS_VOLUME = 0x0226;
    /**
     * <p>Enables or disables reservation of one link for a peer device.</p>
     */
    public static final int COMMAND_SET_PEER_LINK_RESERVED = 0x0227;
    /**
     * <p>Establishes whether one link is reserved for a peer device.</p>
     */
    public static final int COMMAND_GET_PEER_LINK_RESERVED = 0x02A7;
    /**
     * <p>Requests the peer in a True Wireless Stereo session to begin Advertising. The command payload length will
     * be 1 if no target address is specified or 8 if a Typed Bluetooth Device Address is specified.</p>
     */
    public static final int COMMAND_TWS_PEER_START_ADVERTISING = 0x022A;
    /**
     * <p>Requests the device send a "Find Me" request to the HID remote connected to it.</p>
     */
    public static final int COMMAND_FIND_MY_REMOTE = 0x022B;

    /**
     * <p>Sets Codec.</p>
     */
    public static final int COMMAND_SET_CODEC = 0x0240;
    /**
     * <p>Gets Codec.</p>
     */
    public static final int COMMAND_GET_CODEC = 0x02C0;

    /**
     * <p>The command to set the supported features by Host for notification events. Each feature corresponds to a
     * mask as follow:</p>
     * <ul>
     *     <li>0x0001: time</li>
     *     <li>0x0002: missed calls</li>
     *     <li>0x0004: SMS</li>
     *     <li>0x0008: incoming call</li>
     * </ul>
     */
    public static final int COMMAND_SET_SUPPORTED_FEATURES = 0x022C;
    /**
     * <p>The command to inform the Device that the Host will disconnect the GAIA connection from the Device.</p>
     */
    public static final int COMMAND_DISCONNECT = 0x022D;



    // -------------------------------------------------------------------
    // |                  POLLED STATUS COMMANDS 0x01nn                  |
    // -------------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a polled status command.</p>
     */
    public static final int COMMANDS_POLLED_STATUS_MASK = 0x0300;
    /**
     * <p>Gets the Gaia Protocol and API version numbers from the device.</p>
     */
    public static final int COMMAND_GET_API_VERSION = 0x0300;
    /**
     * <p>Gets the current RSSI value for the Bluetooth link from the device. The RSSI is specified in dBm using 2's
     * compliment representation, e.g. <code>-20 = 0xEC</code>.</p>
     */
    public static final int COMMAND_GET_CURRENT_RSSI = 0x0301;
    /**
     * <p>Gets the current battery level from the device. Battery level is specified in mV stored as a
     * <code>uint16</code>, e.g. <code>3,300mV = 0x0CE4</code>.</p>
     */
    public static final int COMMAND_GET_CURRENT_BATTERY_LEVEL = 0x0302;
    /**
     * <p>Requests the BlueCore hardware, design and module identification.</p>
     */
    public static final int COMMAND_GET_MODULE_ID = 0x0303;
    /**
     * <p>Requests the application software to identify itself. The acknowledgement payload contains eight octets of
     * application identification optionally followed by nul-terminated human-readable text. The identification
     * information is application dependent; the headset copies fields from the Bluetooth Device ID.</p>
     */
    public static final int COMMAND_GET_APPLICATION_VERSION = 0x0304;
    /**
     * <p>Requests the logic state of the chip PIOs.</p>
     */
    public static final int COMMAND_GET_PIO_STATE = 0x0306;
    /**
     * <p>Requests the value read by a given analogue-to-digital converter.</p>
     */
    public static final int COMMAND_READ_ADC = 0x0307;
    /**
     * <p>Requests the Bluetooth device address of the peer.</p>
     */
    public static final int COMMAND_GET_PEER_ADDRESS = 0x030A;
    /**
     * @deprecated
     *          Use COMMAND_DFU_GET_RESULT.
     *
     * @see #COMMAND_DFU_GET_RESULT
     */
    public static final int COMMAND_GET_DFU_STATUS = 0x0310;
    /**
     * <p>To request status of certain information from the host. Here we are talking about information as
     * system notifications such an incoming SMS, a missed call information, etc.</p>
     */
    public static final int COMMAND_GET_HOST_FEATURE_INFORMATION = 0x0320;


    // ---------------------------------------------------------------------
    // |                  FEATURE CONTROL COMMANDS 0x05nn                  |
    // ---------------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a polled status command.</p>
     */
    public static final int COMMANDS_FEATURE_CONTROL_MASK = 0x0500;
    /**
     * <p>Gets Authentication bitmaps.</p>
     */
    public static final int COMMAND_GET_AUTH_BITMAPS = 0x0580;
    /**
     * <p>Initiates a Gaia Authentication exchange.</p>
     */
    public static final int COMMAND_AUTHENTICATE_REQUEST = 0x0501;
    /**
     * <p>Provides authentication credentials.</p>
     */
    public static final int COMMAND_AUTHENTICATE_RESPONSE = 0x0502;
    /**
     * <p>The host can use this command to enable or disable a feature which it is authenticated to use.</p>
     */
    public static final int COMMAND_SET_FEATURE = 0x0503;
    /**
     * <p>The host can use this command to request the status of a feature.</p>
     */
    public static final int COMMAND_GET_FEATURE = 0x0583;
    /**
     * <p>The host uses this command to enable a GAIA session with a device which does not have the session enabled by
     * default.</p>
     */
    public static final int COMMAND_SET_SESSION_ENABLE = 0x0504;
    /**
     * <p>Retrieves the session enabled state.</p>
     */
    public static final int COMMAND_GET_SESSION_ENABLE = 0x0584;

    
    // -------------------------------------------------------------------
    // |                  DATA TRANSFER COMMANDS 0x01nn                  |
    // -------------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a data transfer command.</p>
     */
    public static final int COMMANDS_DATA_TRANSFER_MASK = 0x0600;
    /**
     * <p>Initialises a data transfer session.</p>
     */
    public static final int COMMAND_DATA_TRANSFER_SETUP = 0x0601;
    /**
     * <p>The host uses this command to indicate closure of a data transfer session, providing the Session ID in the
     * packet payload. The device can release any resources required to maintain a data transfer session at this
     * point, as the host must perform another Data Transfer Setup before sending any more data.</p>
     */
    public static final int COMMAND_DATA_TRANSFER_CLOSE = 0x0602;
    /**
     * <p>A host can use this command to transfer data to a device.</p>
     */
    public static final int COMMAND_HOST_TO_DEVICE_DATA = 0x0603;
    /**
     * <p>A device can use this command to transfer data to the host.</p>
     */
    public static final int COMMAND_DEVICE_TO_HOST_DATA = 0x0604;
    /**
     * <p>Initiates an I2C Transfer (write and/or read).</p>
     */
    public static final int COMMAND_I2C_TRANSFER = 0x0608;
    /**
     * <p>Retrieves information on a storage partition.</p>
     */
    public static final int COMMAND_GET_STORAGE_PARTITION_STATUS = 0x0610;
    /**
     * <p>Prepares a device storage partition for access from the host.</p>
     */
    public static final int COMMAND_OPEN_STORAGE_PARTITION = 0x0611;
    /**
     * <p>Prepares a UART for access from the host.</p>
     */
    public static final int COMMAND_OPEN_UART = 0x0612;
    /**
     * <p>Writes raw data to an open storage partition.</p>
     */
    public static final int COMMAND_WRITE_STORAGE_PARTITION = 0x0615;
    /**
     * <p>Writes data to an open stream.</p>
     */
    public static final int COMMAND_WRITE_STREAM = 0x0617;
    /**
     * <p>Closes a storage partition.</p>
     */
    public static final int COMMAND_CLOSE_STORAGE_PARTITION = 0x0618;
    /**
     * <p>Mounts a device storage partition for access from the device.</p>
     */
    public static final int COMMAND_MOUNT_STORAGE_PARTITION = 0x061A;
    /**
     * <p>Gets file status.</p>
     */
    public static final int COMMAND_GET_FILE_STATUS = 0x0620;
    /**
     * <p>Prepares a file for access from the host.</p>
     */
    public static final int COMMAND_OPEN_FILE = 0x0621;
    /**
     * <p>Reads data from an open file.</p>
     */
    public static final int COMMAND_READ_FILE = 0x0624;
    /**
     * <p>Closes a file.</p>
     */
    public static final int COMMAND_CLOSE_FILE = 0x0628;
    /**
     * <p>Indicates to the host that the device wishes to receive a Device Firmware Upgrade image.</p>
     */
    public static final int COMMAND_DFU_REQUEST = 0x0630;
    /**
     * <p>Readies the device to receive a Device Firmware Upgrade image. The payload will be 8 or 136 octets depending
     * on the message digest type.</p>
     */
    public static final int COMMAND_DFU_BEGIN = 0x0631;
    /**
     * <p>DFU write.</p>
     */
    public static final int COMMAND_DFU_WRITE = 0x0632;
    /**
     * <p>Commands the device to install the DFU image and restart.</p>
     */
    public static final int COMMAND_DFU_COMMIT = 0x0633;
    /**
     * <p>Requests the status of the last completed DFU operation.</p>
     */
    public static final int COMMAND_DFU_GET_RESULT = 0x0634;
    /**
     * <p>Begins a VM Upgrade session over GAIA, allowing VM Upgrade Protocol packets to be sent using the VM Upgrade
     * Control and VM Upgrade Data commands.</p>
     */
    public static final int COMMAND_VM_UPGRADE_CONNECT = 0x0640;
    /**
     * <p>Ends a VM Upgrade session over GAIA.</p>
     */
    public static final int COMMAND_VM_UPGRADE_DISCONNECT = 0x0641;
    /**
     * <p>Tunnels a VM Upgrade Protocol packet.</p>
     */
    public static final int COMMAND_VM_UPGRADE_CONTROL = 0x0642;
    /**
     * <p>Introduces VM Upgrade Protocol data.</p>
     */
    public static final int COMMAND_VM_UPGRADE_DATA = 0x0643;


    // ---------------------------------------------------------------
    // |                  DEBUGGING COMMANDS 0x01nn                  |
    // ---------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a polled status command.</p>
     */
    public static final int COMMANDS_DEBUGGING_MASK = 0x0700;
    /**
     * <p>Requests the device to perform no operation; serves to establish that the Gaia protocol handler is alive.</p>
     */
    public static final int COMMAND_NO_OPERATION = 0x0700;
    /**
     * <p>Requests the values of the device debugging flags.</p>
     */
    public static final int COMMAND_GET_DEBUG_FLAGS = 0x0701;
    /**
     * <p>Sets the values of the device debugging flags.</p>
     */
    public static final int COMMAND_SET_DEBUG_FLAGS = 0x0702;
    /**
     * <p>Retrieves the value of the indicated PS key.</p>
     */
    public static final int COMMAND_RETRIEVE_PS_KEY = 0x0710;
    /**
     * <p>Retrieves the value of the indicated PS key.</p>
     */
    public static final int COMMAND_RETRIEVE_FULL_PS_KEY = 0x0711;
    /**
     * <p>Sets the value of the indicated PS key.</p>
     */
    public static final int COMMAND_STORE_PS_KEY = 0x0712;
    /**
     * <p>Flood fill the store to force a defragment at next boot.</p>
     */
    public static final int COMMAND_FLOOD_PS = 0x0713;
    /**
     * <p>Sets the value of the indicated PS key.</p>
     */
    public static final int COMMAND_STORE_FULL_PS_KEY = 0x0714;
    /**
     * <p>Results in a <code>GAIA_DEBUG_MESSAGE</code> being sent up from the Gaia library to the application task. Its
     * interpretation is entirely user defined.</p>
     */
    public static final int COMMAND_SEND_DEBUG_MESSAGE = 0x0720;
    /**
     * <p>Sends an arbitrary message to the on-chip application.</p>
     */
    public static final int COMMAND_SEND_APPLICATION_MESSAGE = 0x0721;
    /**
     * <p>Sends an arbitrary message to the Kalimba DSP.</p>
     */
    public static final int COMMAND_SEND_KALIMBA_MESSAGE = 0x0722;
    /**
     * <p>Retrieves the number of available malloc() slots and the space available for PS keys.</p>
     */
    public static final int COMMAND_GET_MEMORY_SLOTS = 0x0730;
    /**
     * <p>Retrieves the value of the specified 16-bit debug variable.</p>
     */
    public static final int COMMAND_GET_DEBUG_VARIABLE = 0x0740;
    /**
     * <p>Sets the value of the specified 16-bit debug variable.</p>
     */
    public static final int COMMAND_SET_DEBUG_VARIABLE = 0x0741;
    /**
     * <p>Removes all authenticated devices from the paired device list and any associated attribute data.</p>
     */
    public static final int COMMAND_DELETE_PDL = 0x0750;
    /**
     * <p>Sent to a BLE slave device, causing it to request a new set of connection parameters.</p>
     */
    public static final int COMMAND_SET_BLE_CONNECTION_PARAMETERS = 0x0752;


    // ------------------------------------------------------------------
    // |                  NOTIFICATION COMMANDS 0x01nn                  |
    // ------------------------------------------------------------------
    /**
     * <p>The mask to know if the command is a notification command.</p>
     */
    public static final int COMMANDS_NOTIFICATION_MASK = 0x4000;
    /**
     * <p>Hosts register for notifications using the <code>REGISTER_NOTIFICATION</code> command, specifying an Event
     * Type from table below as the first byte of payload, with optional parameters as defined per event in
     * successive payload bytes.</p>
     */
    public static final int COMMAND_REGISTER_NOTIFICATION = 0x4001;
    /**
     * <p>Requests the current status of an event type. For threshold type events where multiple levels may be
     * registered, the response indicates how many notifications are registered. Where an event may be simply
     * registered or not the number will be <code>1</code> or <code>0</code>.</p>
     */
    public static final int COMMAND_GET_NOTIFICATION = 0x4081;
    /**
     * <p>A host can cancel event notification by sending a <code>CANCEL_NOTIFICATION</code> command, the first byte of
     * payload will be the Event Type being cancelled.</p>
     */
    public static final int COMMAND_CANCEL_NOTIFICATION = 0x4002;
    /**
     * <p>Assuming successful registration, the host will asynchronously receive one or more
     * <code>EVENT_NOTIFICATION</code> command(s) (Command ID <code>0x4003</code>). The first byte of the Event
     * Notification command payload will be the Event Type code, indicating the notification type. For example,
     * <code>0x03</code> indicating a battery level low threshold event notification. Further data in the Event
     * Notification payload is dependent on the notification type and defined on a per-notification basis below.</p>
     */
    public static final int COMMAND_EVENT_NOTIFICATION = 0x4003;
    

    /**
     * <p>The different status for an acknowledgment packet.</p> <p>By convention, the first octet in an acknowledgement
     * (ACK) packet is a status code indicating the success or the reason for the failure of a request.</p>
     */
    @IntDef(flag = true, value = { Status.NOT_STATUS, Status.SUCCESS, Status.NOT_SUPPORTED, Status.NOT_AUTHENTICATED,
            Status.INSUFFICIENT_RESOURCES, Status.AUTHENTICATING, Status.INVALID_PARAMETER, Status.INCORRECT_STATE,
            Status.IN_PROGRESS })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Status {

        int NOT_STATUS = -1;
        /**
         * <p>The request completed successfully.</p>
         */
        int SUCCESS = 0;
        /**
         * <p>An invalid COMMAND ID has been sent or is not supported by the device.</p>
         */
        int NOT_SUPPORTED = 1;
        /**
         * <p>The host is not authenticated to use a Command ID or to control a feature type.</p>
         */
        int NOT_AUTHENTICATED = 2;
        /**
         * <p>The COMMAND ID used is valid but the GAIA device could not complete it successfully.</p>
         */
        int INSUFFICIENT_RESOURCES = 3;
        /**
         * <p>The GAIA device is in the process of authenticating the host.</p>
         */
        int AUTHENTICATING = 4;
        /**
         * <p>The parameters sent were invalid: missing parameters, too much parameters, range, etc.</p>
         */
        int INVALID_PARAMETER = 5;
        /**
         * <p>The GAIA device is not in the correct state to process the command: needs to stream music, use a certain
         * source, etc.</p>
         */
        int INCORRECT_STATE = 6;
        /**
         * <p>The command is in progress.</p> <p>Acknowledgements with <code>IN_PROGRESS</code> status may be sent once
         * or periodically during the processing of a time-consuming operation to indicate that the operation has not
         * stalled.</p>
         */
        int IN_PROGRESS = 7;
    }

    /**
     * <p>All notification event types which can be sent by the device.</p>
     */
    @IntDef(flag = true, value = {NotificationEvents.NOT_NOTIFICATION, NotificationEvents.RSSI_HIGH_THRESHOLD,
            NotificationEvents.RSSI_LOW_THRESHOLD, NotificationEvents.BATTERY_HIGH_THRESHOLD,
            NotificationEvents.BATTERY_LOW_THRESHOLD, NotificationEvents.DEVICE_STATE_CHANGED,
            NotificationEvents.PIO_CHANGED, NotificationEvents.DEBUG_MESSAGE,NotificationEvents.BATTERY_CHARGED,
            NotificationEvents.CHARGER_CONNECTION, NotificationEvents.CAPSENSE_UPDATE, NotificationEvents.USER_ACTION,
            NotificationEvents.SPEECH_RECOGNITION, NotificationEvents.AV_COMMAND,
            NotificationEvents.REMOTE_BATTERY_LEVEL, NotificationEvents.KEY, NotificationEvents.DFU_STATE,
            NotificationEvents.UART_RECEIVED_DATA, NotificationEvents.VMU_PACKET, NotificationEvents.HOST_NOTIFICATION})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface NotificationEvents {
        /**
         * <p>This is not a notification - <code>0x00</code></p>
         */
        int NOT_NOTIFICATION = 0;
        /**
         * <p>This event provides a way for hosts to receive notification of changes in the RSSI of a device's Bluetooth
         * link with the host - <code>0x01</code></p>
         */
        int RSSI_LOW_THRESHOLD = 0x01;
        /**
         * <p>This command provides a way for hosts to receive notification of changes in the RSSI of a device's Bluetooth
         * link with the host - <code>0x02</code></p>
         */
        int RSSI_HIGH_THRESHOLD = 0x02;
        /**
         * <p>This command provides a way for hosts to receive notification of changes in the battery level of a device - <code>0x03</code></p>
         */
        int BATTERY_LOW_THRESHOLD = 0x03;
        /**
         * <p>This command provides a way for hosts to receive notification of changes in the battery level of a device - <code>0x04</code></p>
         */
        int BATTERY_HIGH_THRESHOLD = 0x04;
        /**
         * <p>A host can register to receive notifications of the device changes in state - <code>0x05</code></p>
         */
        int DEVICE_STATE_CHANGED = 0x05;
        /**
         * <p>A host can register to receive notification of a change in PIO state. The host provides a uint32 bitmap of
         * PIO pins about which it wishes to receive state change notifications - <code>0x06</code></p>
         */
        int PIO_CHANGED = 0x06;
        /**
         * <p>A host can register to receive debug messages from a device - <code>0x07</code></p>
         */
        int DEBUG_MESSAGE = 0x07;
        /**
         * <p>A host can register to receive a notification when the device battery has been fully charged - <code>0x08</code></p>
         */
        int BATTERY_CHARGED = 0x08;
        /**
         * <p>A host can register to receive a notification when the battery charger is connected or disconnected - <code>0x09</code></p>
         */
        int CHARGER_CONNECTION = 0x09;
        /**
         * <p>A host can register to receive a notification when the capacitive touch sensors' state changes. Removed from
         * V1.0 of the API but sounds useful - <code>0x0A</code></p>
         */
        int CAPSENSE_UPDATE = 0x0A;
        /**
         * <p>A host can register to receive a notification when an application-specific user action takes place, for
         * instance a long button press. Not the same as PIO Changed. Removed from V1.0 of the API but sounds useful - <code>0x0B</code></p>
         */
        int USER_ACTION = 0x0B;
        /**
         * <p>A host can register to receive a notification when the Speech Recognition system thinks it heard something - <code>0x0C</code></p>
         */
        int SPEECH_RECOGNITION = 0x0C;
        /**
         * <p>? - <code>0x0D</code></p>
         */
        int AV_COMMAND = 0x0D;
        /**
         * <p>? - <code>0x0E</code></p>
         */
        int REMOTE_BATTERY_LEVEL = 0x0E;
        /**
         * <p>? - <code>0x0F</code></p>
         */
        int KEY = 0x0F;
        /**
         * <p>This notification event indicates the progress of a Device Firmware Upgrade operation -
         * <code>0x10</code>.</p>
         */
        int DFU_STATE = 0x10;
        /**
         * <p>This notification event indicates that data has been received by a UART - <code>0x11</code></p>
         */
        int UART_RECEIVED_DATA = 0x11;
        /**
         * <p>This notification event encapsulates a VM Upgrade Protocol packet - <code>0x12</code></p>
         */
        int VMU_PACKET = 0x12;
        /**
         * <p>This notification event encapsulates all system notification from the Host, such has an incoming call.</p>
         */
        int HOST_NOTIFICATION =0x13;
    }

    /**
     * <p>To identify a {@link GAIA.Status} by its value.</p>
     *
     * @param status
     *              The status to identify.
     *
     * @return the {@link GAIA.Status} corresponding to the value or {@link com.qualcomm.libraries
     * .gaia.GAIA.Status#NOT_STATUS} if neither {@link GAIA.Status} fits the value.
     */
    public static @GAIA.Status int getStatus(byte status) {
        switch (status) {

            case Status.SUCCESS:
                return GAIA.Status.SUCCESS;
            case Status.NOT_SUPPORTED:
                return GAIA.Status.NOT_SUPPORTED;
            case Status.NOT_AUTHENTICATED:
                return GAIA.Status.NOT_AUTHENTICATED;
            case Status.INSUFFICIENT_RESOURCES:
                return GAIA.Status.INSUFFICIENT_RESOURCES;
            case Status.AUTHENTICATING:
                return GAIA.Status.AUTHENTICATING;
            case Status.INVALID_PARAMETER:
                return GAIA.Status.INVALID_PARAMETER;
            case Status.INCORRECT_STATE:
                return GAIA.Status.INCORRECT_STATE;
            case Status.IN_PROGRESS:
                return GAIA.Status.IN_PROGRESS;

            default:
                return GAIA.Status.NOT_STATUS;
        }
    }

    /**
     * <p>To obtain a readable version of a {@link GAIA.Status}.</p>
     *
     * @param status
     *              the status value.
     *
     * @return A string corresponding to the {@link GAIA.Status} value.
     */
    public static String getStatusToString(int status) {
        switch (status) {

            case Status.SUCCESS:
                return "SUCCESS";
            case Status.NOT_SUPPORTED:
                return "NOT SUPPORTED";
            case Status.NOT_AUTHENTICATED:
                return "NOT AUTHENTICATED";
            case Status.INSUFFICIENT_RESOURCES:
                return "INSUFFICIENT RESOURCES";
            case Status.AUTHENTICATING:
                return "AUTHENTICATING";
            case Status.INVALID_PARAMETER:
                return "INVALID PARAMETER";
            case Status.INCORRECT_STATE:
                return "INCORRECT STATE";
            case Status.IN_PROGRESS:
                return "IN PROGRESS";
            case Status.NOT_STATUS:
                return "NOT STATUS";

            default:
                return "UNKNOWN STATUS";
        }
    }

    /**
     * <p>To identify a {@link GAIA.NotificationEvents} by its value.</p>
     *
     * @param event
     *              The event to identify.
     *
     * @return the {@link GAIA.NotificationEvents} corresponding to the value or {@link com.qualcomm.libraries
     * .gaia.GAIA.NotificationEvents#NOT_NOTIFICATION} if neither {@link GAIA.NotificationEvents} fits the
     * value.
     */
    public static @GAIA.NotificationEvents int getNotificationEvent(byte event) {
        switch (event) {
            case NotificationEvents.RSSI_LOW_THRESHOLD:
                return NotificationEvents.RSSI_LOW_THRESHOLD;

            case NotificationEvents.RSSI_HIGH_THRESHOLD:
                return NotificationEvents.RSSI_HIGH_THRESHOLD;

            case NotificationEvents.BATTERY_LOW_THRESHOLD:
                return NotificationEvents.BATTERY_LOW_THRESHOLD;

            case NotificationEvents.BATTERY_HIGH_THRESHOLD:
                return NotificationEvents.BATTERY_HIGH_THRESHOLD;

            case NotificationEvents.DEVICE_STATE_CHANGED:
                return NotificationEvents.DEVICE_STATE_CHANGED;

            case NotificationEvents.PIO_CHANGED:
                return NotificationEvents.PIO_CHANGED;

            case NotificationEvents.DEBUG_MESSAGE:
                return NotificationEvents.DEBUG_MESSAGE;

            case NotificationEvents.BATTERY_CHARGED:
                return NotificationEvents.BATTERY_CHARGED;

            case NotificationEvents.CHARGER_CONNECTION:
                return NotificationEvents.CHARGER_CONNECTION;

            case NotificationEvents.CAPSENSE_UPDATE:
                return NotificationEvents.CAPSENSE_UPDATE;

            case NotificationEvents.USER_ACTION:
                return NotificationEvents.USER_ACTION;

            case NotificationEvents.SPEECH_RECOGNITION:
                return NotificationEvents.SPEECH_RECOGNITION;

            case NotificationEvents.AV_COMMAND:
                return NotificationEvents.AV_COMMAND;

            case NotificationEvents.REMOTE_BATTERY_LEVEL:
                return NotificationEvents.REMOTE_BATTERY_LEVEL;

            case NotificationEvents.KEY:
                return NotificationEvents.KEY;

            case NotificationEvents.DFU_STATE:
                return NotificationEvents.DFU_STATE;

            case NotificationEvents.UART_RECEIVED_DATA:
                return NotificationEvents.UART_RECEIVED_DATA;

            case NotificationEvents.VMU_PACKET:
                return NotificationEvents.VMU_PACKET;

            case NotificationEvents.HOST_NOTIFICATION:
                return NotificationEvents.HOST_NOTIFICATION;

            default:
                return NotificationEvents.NOT_NOTIFICATION;
        }
    }


    /**
     * <p>All known transports which can be used to send and transfer GAIA packets as their format changes depending
     * on their transport.</p>
     */
    @IntDef(flag = true, value = { Transport.BLE, Transport.BR_EDR })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Transport {
        /**
         * Bluetooth Low Energy.
         */
        int BLE = 0;
        /**
         * Bluetooth Classic.
         */
        int BR_EDR = 1;
    }

}
