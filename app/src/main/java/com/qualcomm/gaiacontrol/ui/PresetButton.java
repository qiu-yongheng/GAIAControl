/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.gaia.EqualizerGaiaManager;

/**
 * To personalize buttons depending on their state & their properties to display pre-sets buttons for the equalizer
 * feature.
 */
public class PresetButton extends Button {

    /**
     * The drawable to use when the button is selected.
     */
    private int mSelectedDrawable;
    /**
     * The drawable to use when the button is unselected.
     */
    private int mUnselectedDrawable;
    /**
     * The preset which fits with this button.
     */
    private int mPreset;

    public PresetButton(Context context) {
        super(context);
        init();
    }

    public PresetButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PresetButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * To mark this preset button as selected.
     *
     * @param selected
     *              true if the button is selected, false otherwise.
     */
    public void selectButton (boolean selected) {
        setSelected(selected);

        if (getPreset() != EqualizerGaiaManager.CUSTOMIZABLE_PRESET) {
            setEnabled(!selected);
        }

        if (selected) {
            setCompoundDrawablesWithIntrinsicBounds(0, mSelectedDrawable, 0, 0);
            setTextColor(getResources().getColor(R.color.primary_text, null));
            setBackgroundColor(getResources().getColor(R.color.button_preset_background_selected, null));
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, mUnselectedDrawable, 0, 0);
            setTextColor(getResources().getColor(R.color.secondary_text, null));
            setBackground(getResources().getDrawable(R.drawable.flat_button_preset_background, null));
        }
    }

    /**
     * To return the preset number which corresponds to this button.
     *
     * @return the value for the button preset.
     */
    public int getPreset() {
        return mPreset;
    }

    /**
     * To initialize fields depending on which preset button it is.
     */
    private void init() {
        switch (this.getId()) {
            case R.id.bt_preset_0:
                mSelectedDrawable = R.drawable.ic_preset_1_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_1_light_76dp;
                mPreset = 0;
                break;
            case R.id.bt_preset_1:
                mSelectedDrawable = R.drawable.ic_preset_custom_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_custom_light_76dp;
                mPreset = EqualizerGaiaManager.CUSTOMIZABLE_PRESET;
                break;
            case R.id.bt_preset_2:
                mSelectedDrawable = R.drawable.ic_preset_2_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_2_light_76dp;
                mPreset = 2;
                break;
            case R.id.bt_preset_3:
                mSelectedDrawable = R.drawable.ic_preset_3_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_3_light_76dp;
                mPreset = 3;
                break;
            case R.id.bt_preset_4:
                mSelectedDrawable = R.drawable.ic_preset_4_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_4_light_76dp;
                mPreset = 4;
                break;
            case R.id.bt_preset_5:
                mSelectedDrawable = R.drawable.ic_preset_5_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_5_light_76dp;
                mPreset = 5;
                break;
            case R.id.bt_preset_6:
                mSelectedDrawable = R.drawable.ic_preset_6_76dp;
                mUnselectedDrawable = R.drawable.ic_preset_6_light_76dp;
                mPreset = 6;
                break;
        }
    }
}
