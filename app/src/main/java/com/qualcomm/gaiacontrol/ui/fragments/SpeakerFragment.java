/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.ui.fragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.TWSGaiaManager;

/**
 * This fragment allows control of a speaker view for the TWS feature. It contains the possibility for the user to
 * select a channel (stereo, left, etc.) and the speaker volume if these settings are available.
 */
public class SpeakerFragment extends Fragment {

    /**
     * The listener to interact with the activity which implements this fragment.
     */
    private ISpeakerFragmentListener mActivityListener;
    /**
     * The text view which displays the speaker name.
     */
    private TextView mTVTitle;
    /**
     * The speaker name.
     */
    private String mTitle;
    /**
     * The number which corresponds to this speaker.
     */
    private @TWSGaiaManager.Speaker int mSpeakerValue;
    /**
     * The seek bar which allows the user to change the volume.
     */
    private SeekBar mSBVolume;
    /**
     * The channel which is selected and displayed to the user.
     */
    private @TWSGaiaManager.Channel int mChannel = TWSGaiaManager.Channel.MONO;
    /**
     * The text view to display the channel name.
     */
    private TextView mTVChannel;
    /**
     * The left speaker picture.
     */
    private ImageView mIVLeftSpeaker;
    /**
     * The right speaker picture.
     */
    private ImageView mIVRightSpeaker;
    /**
     * To know if the volume is disabled because this feature is not supported.
     */
    private boolean volumeDisabled = true;
    /**
     * To know if the channel is disabled because this feature is not supported.
     */
    private boolean channelDisabled = true;
    /**
     * The view which contains the label speaker channel and buttons.
     */
    private View mVChanelLabel;
    /**
     * The view which contains the speakers images.
     */
    private View mVChanelSpeakers;
    /**
     * To keep an instance of the view which contains all the volume components.
     */
    private View mVVolume;
    /**
     * To keep an instance of the main view this class manages.
     */
    private View mVFragment;


    /**
     * Empty constructor - required.
     */
    public SpeakerFragment() {
    }

    // When the view is created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speaker, container, false);
        this.init(view);
        return view;
    }

    // When the view has been created.
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateChannelDisplay();
    }

    // When the fragment is attached to an activity.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivityListener = (ISpeakerFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ISpeakerFragmentListener");
        }
    }

    // When the view is inflated inside the activity.
    @SuppressWarnings("deprecation")
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        if (attrs != null) {
            final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SpeakerFragment);
            mTitle = a.getString(R.styleable.SpeakerFragment_speakerTitle);
            if (mTVTitle != null)
                mTVTitle.setText(mTitle);
            a.recycle();
        }
    }

    // When the fragment his detached from its activity.
    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }

    /**
     * To define the speaker value as master or slave.
     *
     * @param value
     *         the value for this speaker.
     */
    public void setSpeakerValue(int value) {
        mSpeakerValue = value;
    }

    /**
     * To define the volume to display.
     *
     * @param value
     *         the volume value to display.
     */
    public void setVolume(int value) {
        if (volumeDisabled) {
            volumeDisabled = false;
            enableVolume();
        }
        mSBVolume.setProgress(value);
    }

    /**
     * To define the channel to display.
     *
     * @param channel
     *         the channel value to display.
     */
    public void setChannel(@TWSGaiaManager.Channel int channel) {
        if (channelDisabled) {
            channelDisabled = false;
            enableChannel();
        }
        mChannel = channel;
        updateChannelDisplay();
    }

    /**
     * To enable the channel feature.
     */
    private void enableChannel() {
        mVFragment.setVisibility(View.VISIBLE);
        mVChanelLabel.setVisibility(View.VISIBLE);
        mVChanelSpeakers.setVisibility(View.VISIBLE);
    }

    /**
     * To enable the volume feature.
     */
    private void enableVolume() {
        mVFragment.setVisibility(View.VISIBLE);
        mVVolume.setVisibility(View.VISIBLE);
    }

    /**
     * This method allows initialisation of components.
     *
     * @param view
     *         The inflated view for this fragment.
     */
    private void init(View view) {
        mTVTitle = (TextView) view.findViewById(R.id.tv_speaker_name);
        if (mTitle != null)
            mTVTitle.setText(mTitle);

        mSBVolume = (SeekBar) view.findViewById(R.id.sb_volume);
        mSBVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // nothing to do
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nothing to do
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // on stop: send the new value to the speaker
                mActivityListener.setVolume(mSpeakerValue, mSBVolume.getProgress());
            }
        });

        View.OnClickListener leftListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousChannel();
            }
        };
        View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextChannel();
            }
        };

        mIVLeftSpeaker = (ImageView) view.findViewById(R.id.iv_speaker_left);
        mIVLeftSpeaker.setOnClickListener(leftListener);
        mIVRightSpeaker = (ImageView) view.findViewById(R.id.iv_speaker_right);
        mIVRightSpeaker.setOnClickListener(rightListener);
        mTVChannel = (TextView) view.findViewById(R.id.tv_channel);

        mVChanelLabel = view.findViewById(R.id.ll_channel_label);
        mVVolume = view.findViewById(R.id.ll_volume);
        mVChanelSpeakers = view.findViewById(R.id.ll_channel_speakers);
        mVFragment = view;

        ImageButton btLeft = (ImageButton) view.findViewById(R.id.ib_arrow_left);
        btLeft.setOnClickListener(leftListener);
        ImageButton btRight = (ImageButton) view.findViewById(R.id.ib_arrow_right);
        btRight.setOnClickListener(rightListener);
    }

    /**
     * To define the channel as the previous one on the enumeration.
     */
    private void setPreviousChannel() {
        mChannel = getPreviousChannel(mChannel);
        updateChannelDisplay();
        mActivityListener.setChannel(mSpeakerValue, mChannel);
    }

    /**
     * To define the channel as the next one on the enumeration.
     */
    private void setNextChannel() {
        mChannel = getNextChannel(mChannel);
        updateChannelDisplay();
        mActivityListener.setChannel(mSpeakerValue, mChannel);
    }

    /**
     * To update the channel display depending on the actual selected channel.
     */
    private void updateChannelDisplay() {
        switch (mChannel) {
            case TWSGaiaManager.Channel.STEREO:
                mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_left_blue_100dp);
                mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_right_blue_100dp);
                mTVChannel.setText(getString(R.string.tws_stereo));
                break;
            case TWSGaiaManager.Channel.LEFT:
                mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_left_blue_100dp);
                mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_right_grey_100dp);
                mTVChannel.setText(getString(R.string.tws_left_channel));
                break;
            case TWSGaiaManager.Channel.RIGHT:
                mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_left_grey_100dp);
                mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_right_blue_100dp);
                mTVChannel.setText(getString(R.string.tws_right_channel));
                break;
            case TWSGaiaManager.Channel.MONO:
                mIVLeftSpeaker.setImageResource(R.drawable.ic_speaker_left_blue_100dp);
                mIVRightSpeaker.setImageResource(R.drawable.ic_speaker_right_blue_100dp);
                mTVChannel.setText(getString(R.string.tws_mono));
                break;
        }
    }

    /**
     * To retrieve the channel which follows the one specified.
     *
     * @return the next channel
     */
    private @TWSGaiaManager.Channel int getNextChannel(@TWSGaiaManager.Channel int channel) {
        switch (channel) {
            case TWSGaiaManager.Channel.STEREO:
                return TWSGaiaManager.Channel.LEFT;
            case TWSGaiaManager.Channel.LEFT:
                return TWSGaiaManager.Channel.RIGHT;
            case TWSGaiaManager.Channel.RIGHT:
                return TWSGaiaManager.Channel.MONO;
            case TWSGaiaManager.Channel.MONO:
                return TWSGaiaManager.Channel.STEREO;
            default:
                return -1;
        }
    }

    /**
     * To know the channel which precedes the one specified.
     *
     * @return the previous channel
     */
    private @TWSGaiaManager.Channel int getPreviousChannel(@TWSGaiaManager.Channel int channel) {
        // STEREO, LEFT, RIGHT, MONO;
        switch (channel) {
            case TWSGaiaManager.Channel.STEREO:
                return TWSGaiaManager.Channel.MONO;
            case TWSGaiaManager.Channel.LEFT:
                return TWSGaiaManager.Channel.STEREO;
            case TWSGaiaManager.Channel.RIGHT:
                return TWSGaiaManager.Channel.LEFT;
            case TWSGaiaManager.Channel.MONO:
                return TWSGaiaManager.Channel.RIGHT;
            default:
                return -1;
        }
    }


    /**
     * <p>This interface allows this fragment to communicate with its attached activity (which has to implement it).</p>
     */
    public interface ISpeakerFragmentListener {

        /**
         * To send the new volume to the speaker.
         *
         * @param speaker
         *         The number to specify the speaker: 0x00 for the master, 0x01 for the slave, etc.
         * @param volume
         *         The new volume to send to the speaker.
         */
        void setVolume(@TWSGaiaManager.Speaker int speaker, int volume);

        /**
         * To send the selected to the speaker.
         *
         * @param speaker
         *         The to specify the speaker: 0x00 for the master, 0x01 for the slave, etc.
         * @param channel
         *         The new channel for the speaker.
         */
        void setChannel(@TWSGaiaManager.Speaker int speaker, @TWSGaiaManager.Channel int channel);
    }

}
