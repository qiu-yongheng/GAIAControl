/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;

/**
 * <p>This class extends the LinearLayout class in order to create a Slider UI component which incorporates a
 * SeekBar, TextView to display some bounds labels, the exact value displayed on the SeekBar and a title.</p>
 * <p>This layout is used in the Custom Equalizer Activity for the user to be able to select the values he would
 * like for the master gain, gain, frequency and quality parameters.</p>
 */

public class SliderLayout extends LinearLayout {

    /**
     * The slider for this layout.
     */
    private SeekBar mSeekBar;
    /**
     * The text view to display the current value from the slider.
     */
    private TextView mTVValue;
    /**
     * The title for this view.
     */
    private TextView mTVTitle;
    /**
     * The text view to display the minimum value for the slider.
     */
    private TextView mTVMinValue;
    /**
     * The text view to display the maximum value for the slider.
     */
    private TextView mTVMaxValue;

    /**
     * The listener with which to interact when the user is using the slider.
     */
    private SliderListener mListener;

    // required when extending LinearLayout
    public SliderLayout(Context context) {
        super(context);
    }

    // required when extending LinearLayout
    public SliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // required when extending LinearLayout
    public SliderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override // extends LinearLayout
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSeekBar = (SeekBar) findViewById(R.id.sb_slider);
        mTVValue = (TextView) findViewById(R.id.tv_value);
        mTVTitle = (TextView) findViewById(R.id.tv_title);
        mTVMinValue = (TextView) findViewById(R.id.tv_slider_min_value);
        mTVMaxValue = (TextView) findViewById(R.id.tv_slider_max_value);
    }

    /**
     * To enable or disable the view.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mSeekBar.setEnabled(enabled);
        mTVValue.setEnabled(enabled);
        mTVMinValue.setEnabled(enabled);
        mTVMaxValue.setEnabled(enabled);
        mTVTitle.setEnabled(enabled);
    }

    /**
     * To display the label for the current value.
     */
    public void displayValue(String valueString) {
        mTVValue.setText(valueString);
    }

    /**
     * To hide the title if it does not need to be displayed.
     */
    public void hideTitle() {
        mTVTitle.setVisibility(GONE);
    }

    /**
     * <p>To set up the current position of the slider. If the given value is lower than 0 or bigger than the
     * maximum value for the SeekBar, this method will set up the value to the corresponding bound.</p>
     *
     * @param valueInt
     *          the value to set which has to be between 0 and the maximum value defined by using
     *          {@link #setSliderBounds(int, String, String) setSliderBounds}.
     */
    public void setSliderPosition(int valueInt) {
        if (valueInt < 0) {
            valueInt = 0;
        }
        else if (valueInt > mSeekBar.getMax()) {
            valueInt = mSeekBar.getMax();
        }

        mSeekBar.setProgress(valueInt);
    }

    /**
     * To define the default values to display for the view.
     *
     * @param title
     *          The view title.
     * @param value
     *          The label for the current value.
     * @param listener
     *          The listener to use to interact with.
     */
    public void initialize (String title, String value, SliderListener listener) {
        mTVTitle.setText(title);
        mTVValue.setText(value);
        mListener = listener;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                // if the modification had been made by the user, the event is thrown to the listener
                if (fromUser) {
                    mListener.onProgressChangedByUser(position, getId());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mListener.onStopTrackingTouch(seekBar.getProgress(), getId());
            }
        });

    }

    /**
     * To define the range of the slider as well as its bounds labels.
     *
     * @param maximum
     *              The maximum value which can be set up for the position of the slider on the SeekBar.
     * @param minText
     *              the label for the minimum bound.
     * @param maxText
     *              the label for the maximum bound.
     */
    public void setSliderBounds(int maximum, String minText, String maxText) {
        mTVMinValue.setText(minText);
        mTVMaxValue.setText(maxText);
        mSeekBar.setMax(maximum);
    }

    /**
     *  The listener to throw user events when the user interacts with the slider.
     */
    public interface SliderListener {
        /**
         * This method is called when the user is touching the slider and changing the value by sliding.
         *
         * @param progress
         *          the new value of the progress on the slider.
         * @param id
         *          The ID of the SliderLayout which calls the listener.
         */
        void onProgressChangedByUser(int progress, int id);

        /**
         * This method is called when the user stops to interact with the slider.
         *
         * @param progress
         *          the new value of the progress on the slider.
         * @param id
         *          The ID of the SliderLayout which calls the listener.
         */
        void onStopTrackingTouch(int progress, int id);
    }
}
