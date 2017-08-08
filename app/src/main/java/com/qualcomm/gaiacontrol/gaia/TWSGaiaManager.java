/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.gaiacontrol.gaia;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.gaia.packets.GaiaPacket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class follows the GAIA protocol. It manages all messages which are sent and received over the protocol for
 * the TWS Activity.</p>
 * <p>For all GAIA commands used in this class, the Vendor ID is always {@link GAIA#VENDOR_QUALCOMM}.</p>
 */
public class TWSGaiaManager extends AGaiaManager {

    // ====== STATIC FIELDS =======================================================================

    /**
     * To know if we are using the application in debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * The maximum volume value for a speaker.
     */
    private static final int MAX_VOLUME = 0x7F;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("unused")
    private final String TAG = "TWSGaiaManager";
    /**
     * <p>The listener which implements the GaiaManagerListener interface to allow this manager to communicate with a
     * device.</p>
     */
    private final GaiaManagerListener mListener;


    // ====== ENUM =================================================================================

    /**
     * <p>This enumeration represents the master and slave values a speaker can be.</p>
     */
    @IntDef(flag = true, value = { Speaker.MASTER_SPEAKER, Speaker.SLAVE_SPEAKER })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface Speaker {
        /**
         * The value to send to the speaker when a message concerns the master speaker.
         */
        int MASTER_SPEAKER = 0x00;
        /**
         * The value to send to the speaker when a message concerns the slave speaker.
         */
        int SLAVE_SPEAKER = 0x01;
    }

    /**
     * <p>This enumeration represents the different types of channel routes speakers can act: stereo, mono, left or
     * right.</p>
     */
    @IntDef(flag = true, value = { Channel.STEREO, Channel.LEFT, Channel.RIGHT, Channel.MONO })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface Channel {
        /**
         * A speaker acts on a STEREO route.
         */
        int STEREO = 0x00;
        /**
         * A speaker acts on a LEFT route.
         */
        int LEFT = 0x01;
        /**
         * A speaker acts on a RIGHT route.
         */
        int RIGHT = 0x02;
        /**
         * A speaker acts on a MONO route.
         */
        int MONO = 0x03;
    }


    // ====== CONSTRUCTOR ==========================================================================

    /**
     * <p>Main constructor of this class which allows initialisation of a listener to send messages to a device or dispatch
     * any received GAIA messages.</p>
     *
     * @param myListener
     *         An object which implements the {@link GaiaManagerListener MyGaiaManagerListener} interface.
     * @param transport
     *          The type of transport this manager should use for the GAIA packet format:
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BLE BLE} or
     *          {@link com.qualcomm.libraries.gaia.GAIA.Transport#BR_EDR BR/EDR}.
     */
    public TWSGaiaManager(GaiaManagerListener myListener, @GAIA.Transport int transport) {
        super(transport);
        this.mListener = myListener;
    }


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>This method requests the route channel the given speaker has.</p>
     *
     * @param speaker
     *          The speaker to know its route channel.
     */
    public void getChannel(@Speaker int speaker) {
        final int PAYLOAD_LENGTH = 1;
        final int SPEAKER_OFFSET = 0;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[SPEAKER_OFFSET] = (byte) speaker;
        createRequest(createPacket(GAIA.COMMAND_GET_TWS_AUDIO_ROUTING, payload));
    }

    /**
     * <p>This method requests the volume of the given speaker.</p>
     *
     * @param speaker
     *          The speaker to know its volume.
     */
    public void getVolume(@Speaker int speaker) {
        final int PAYLOAD_LENGTH = 1;
        final int SPEAKER_OFFSET = 0;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[SPEAKER_OFFSET] = (byte) speaker;
        createRequest(createPacket(GAIA.COMMAND_GET_TWS_VOLUME, payload));
    }

    /**
     * <p>This method sets up the state of the channel for the given speaker through the connected device using the
     * {@link GAIA#COMMAND_SET_TWS_AUDIO_ROUTING COMMAND_SET_TWS_AUDIO_ROUTING} command.</p>
     *
     * @param speaker
     *          The speaker to set up its channel.
     * @param channel
     *          The channel to define for the given speaker.
     */
    public void setChannel(@Speaker int speaker, @Channel int channel) {
        final int PAYLOAD_LENGTH = 2;
        final int SPEAKER_OFFSET = 0;
        final int CHANNEL_OFFSET = 1;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[SPEAKER_OFFSET] = (byte) speaker;
        payload[CHANNEL_OFFSET] = (byte) channel;
        createRequest(createPacket(GAIA.COMMAND_SET_TWS_AUDIO_ROUTING, payload));
    }

    /**
     * <p>This method sets up the volume of the given speaker through the connected device using the
     * {@link GAIA#COMMAND_SET_TWS_VOLUME COMMAND_SET_TWS_VOLUME} command.</p>
     *
     * @param speaker
     *          The speaker to set up its volume.
     * @param volume
     *          The volume to set up for the given speaker.
     */
    public void setVolume(@Speaker int speaker, int volume) {
        volume = volume < 0 ? 0 : volume > MAX_VOLUME ? MAX_VOLUME : volume;
        final int PAYLOAD_LENGTH = 2;
        final int SPEAKER_OFFSET = 0;
        final int VOLUME_OFFSET = 1;
        byte[] payload = new byte[PAYLOAD_LENGTH];
        payload[SPEAKER_OFFSET] = (byte) speaker;
        payload[VOLUME_OFFSET] = (byte) volume;
        createRequest(createPacket(GAIA.COMMAND_SET_TWS_VOLUME, payload));
    }


    // ====== PROTECTED METHODS ====================================================================

    @Override // extends GaiaManager
    protected void receiveSuccessfulAcknowledgement(GaiaPacket packet) {
        switch (packet.getCommand()) {

            case GAIA.COMMAND_GET_TWS_AUDIO_ROUTING:
                receiveGetChannelACK(packet);
                break;

            case GAIA.COMMAND_GET_TWS_VOLUME:
                receiveGetVolumeACK(packet);
                break;

        }
    }

    @Override // extends GaiaManager
    protected void receiveUnsuccessfulAcknowledgement(GaiaPacket packet) {
    }

    @Override // extends GaiaManager
    protected void hasNotReceivedAcknowledgementPacket(GaiaPacket packet) {
    }

    @Override // extends GaiaManager
    protected boolean manageReceivedPacket(GaiaPacket packet) {
        return false;
    }

    @Override // extends GaiaManager
    protected boolean sendGAIAPacket(byte[] packet) {
        return mListener.sendGAIAPacket(packet);
    }


    // ====== PRIVATE METHODS - RECEIVING =============================================================

    /**
     * <p>This method is called when this manager receives a
     * {@link GAIA#COMMAND_GET_TWS_AUDIO_ROUTING COMMAND_GET_TWS_AUDIO_ROUTING} acknowledgement packet.</p>
     * <p>It will also get the information from the packet in order to transfer them to the listener.</p>
     *
     * @param packet
     *          The received acknowledgement packet.
     */
    private void receiveGetChannelACK(GaiaPacket packet) {
        final int PAYLOAD_LENGTH = 3;
        final int SPEAKER_OFFSET = 1;
        final int CHANNEL_OFFSET = 2;

        byte[] payload = packet.getPayload();

        if (payload.length >= PAYLOAD_LENGTH) {
            int speaker = payload[SPEAKER_OFFSET];
            int channel = payload[CHANNEL_OFFSET];

            mListener.onGetChannel(getSpeakerType(speaker), getChannelType(channel));
        }
    }

    /**
     * <p>This method is called when this manager receives a
     * {@link GAIA#COMMAND_GET_TWS_VOLUME COMMAND_GET_TWS_VOLUME} acknowledgement packet.</p>
     * <p>It will also get all the information from the packet in order to transfer it to the listener.</p>
     *
     * @param packet
     *          The received acknowledgement packet.
     */
    private void receiveGetVolumeACK(GaiaPacket packet) {
        final int PAYLOAD_LENGTH = 3;
        final int SPEAKER_OFFSET = 1;
        final int VOLUME_OFFSET = 2;

        byte[] payload = packet.getPayload();

        if (payload.length >= PAYLOAD_LENGTH) {
            int speaker = payload[SPEAKER_OFFSET];
            int volume = payload[VOLUME_OFFSET];
            volume = volume > MAX_VOLUME ? MAX_VOLUME : volume < 0 ? 0 : volume;

            mListener.onGetVolume(getSpeakerType(speaker), volume);
        }
    }


    // ====== PRIVATE METHODS ====================================================================

    /**
     * <p>To get the Speaker enum value for the given value.</p>
     *
     * @param value
     *          The value to get the corresponding enum value.
     *
     * @return A value from the {@link Speaker Speaker} enumeration or -1 if it cannot be found.
     */
    private @Speaker int getSpeakerType(int value) {
        switch (value) {
            case Speaker.MASTER_SPEAKER:
                return Speaker.MASTER_SPEAKER;
            case Speaker.SLAVE_SPEAKER:
                return Speaker.SLAVE_SPEAKER;
            case Channel.RIGHT:
            default:
                return -1;
        }
    }

    /**
     * <p>To get the Channel enum value for the given value.</p>
     *
     * @param value
     *          The value to get the corresponding enum value.
     *
     * @return A value from the {@link Channel Channel} enumeration or -1 if it cannot be found.
     */
    private @Channel int getChannelType(int value) {
        switch (value) {
            case Channel.LEFT:
                return Channel.LEFT;
            case Channel.MONO:
                return Channel.MONO;
            case Channel.RIGHT:
                return Channel.RIGHT;
            case Channel.STEREO:
                return Channel.STEREO;
            default:
                return -1;
        }
    }


    // ====== INTERFACES ===========================================================================

    /**
     * <p>This interface allows this manager to dispatch messages or events to a listener.</p>
     */
    public interface GaiaManagerListener {

        /**
         * <p>To send over a communication channel the bytes of a GAIA packet using the GAIA protocol.</p>
         *
         * @param packet
         *          The byte array to send to a device.
         * @return
         *          true if the sending could be done.
         */
        boolean sendGAIAPacket(byte[] packet);

        /**
         * <p>This method is called when this manager successfully reads a packet which gives the current state of
         * the channel route for the given speaker.</p>
         *
         * @param speaker
         *              The corresponding speaker
         * @param channel
         *              The state for the given speaker.
         */
        void onGetChannel(@Speaker int speaker, @Channel int channel);

        /**
         * <p>This method is called when this manager successfully reads a packet which gives the current value of
         * the volume for the given speaker.</p>
         *
         * @param speaker
         *              The corresponding speaker
         * @param volume
         *              The volume value for the given speaker.
         */
        void onGetVolume(@Speaker int speaker, int volume);
    }

}
