/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/
package com.qualcomm.gaiacontrol.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.activities.InformationActivity;
import com.qualcomm.gaiacontrol.ui.holders.InformationViewHolder;

/**
 * This adapter is for displaying some information in a RecyclerView by displaying a title/name and a value for the
 * information.
 */
public class InformationListAdapter extends RecyclerView.Adapter<InformationViewHolder> {

    /**
     * The data list for this adapter.
     */
    private final String[] mList;
    /**
     * The listener for the activity in which this adapter is linked.
     */
    private final IListAdapterListener mListener;

    /**
     * The main constructor of this list to build a new instance for an adapter to a RecyclerView about an information
     * list.
     *
     * @param listener
     *            The listener to give some information to this adapter.
     */
    public InformationListAdapter(IListAdapterListener listener) {
        this.mListener = listener;
        mList = new String[InformationActivity.DisplayedInformation.NUMBER_OF_DISPLAYED_INFORMATION];
    }

    @Override // RecyclerView.Adapter<InformationViewHolder>
    public InformationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_information_item, parent, false);
        return new InformationViewHolder(view);
    }

    @Override // RecyclerView.Adapter<InformationViewHolder>
    public void onBindViewHolder(InformationViewHolder holder, int position) {
        // we define the content for this view.
        holder.refreshValues(mListener.getInformationName(position), mList[position]);
    }

    @Override // RecyclerView.Adapter<InformationViewHolder>
    public int getItemCount() {
        return mList.length;
    }

    /**
     * To set the value to display for the corresponding item.
     * 
     * @param position
     *            the position of the item.
     * @param value
     *            the value for the item.
     */
    public void setValue(@InformationActivity.DisplayedInformation int position, String value) {
        if (position >= 0 && position < mList.length) {
            mList[position] = value;
            notifyItemChanged(position);
        }
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the RecyclerView. Such as a
     * fragment or an activity.
     */
    public interface IListAdapterListener {

        /**
         * To know the name of the information which corresponds to the given number.
         * 
         * @param information
         *            the position of the information for which we want the name.
         * 
         * @return the information name.
         */
        String getInformationName(int information);
    }
}