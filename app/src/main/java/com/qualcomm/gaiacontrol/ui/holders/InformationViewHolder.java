/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;

/**
 * This class allows definition of the view for an item on a RecyclerView.
 */
public class InformationViewHolder extends RecyclerView.ViewHolder {
    /**
     * The text views to display information from a device.
     */
    private final TextView mTVInformationName, mTVInformationValue;

    /**
     * The constructor of this class to build this view.
     * @param v
     *          The inflated layout for this view.
     */
    public InformationViewHolder(View v) {
        super(v);
        mTVInformationName = (TextView) v.findViewById(R.id.tv_information_name);
        mTVInformationValue = (TextView) v.findViewById(R.id.tv_information_value);
    }

    /**
     * <p>This method is for refreshing all the values displayed in the corresponding view which show all information
     * related to an Information.</p>
     *
     * @param name
     *          The name of the information to display
     * @param value
     *          The value of the information to display
     */
    public void refreshValues(String name, String value) {
        mTVInformationName.setText(name);
        mTVInformationValue.setText(value);
    }
}