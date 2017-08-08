/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.ui.holders.DeviceViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class allows management of the data set for a devices list.</p>
 */
public class DevicesListAdapter extends RecyclerView.Adapter<DeviceViewHolder>
        implements DeviceViewHolder.IDeviceViewHolder {

    /**
     * The data managed by this adapter.
     */
    private final List<BluetoothDevice> mDevices = new ArrayList<>();
    /**
     * The list of RSSI values which correspond to the devices.
     */
    private final List<Integer> mRssi = new ArrayList<>();
    /**
     * When the list has no item selected it is identified by this value.
     */
    private static final int ITEM_NULL = -1;
    /**
     * To know which device is selected.
     */
    private int mSelectedItem = ITEM_NULL;
    /**
     * The listener for all user interactions.
     */
    private final IDevicesListAdapterListener mListener;

    /**
     * Default constructor to build a new instance of this adapter.
     */
    public DevicesListAdapter(IDevicesListAdapterListener listener) {
        mListener = listener;
    }

    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_devices_item, parent, false);
        return new DeviceViewHolder(view, this);
    }

    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        // we define the content of this view depending on the data set of this adapter.
        BluetoothDevice device = mDevices.get(position);
        String deviceName = device.getName();
        deviceName = (deviceName == null || deviceName.length() < 1) ? "Unknown" : deviceName;
        boolean hasRssi = mRssi.size() > position;
        boolean isSelected = mSelectedItem == position;
        int rssi = hasRssi ? mRssi.get(position) : 0;
        hasRssi = hasRssi && rssi < 0;
        int type = device.getType();

        // fill data
        holder.refreshValues(deviceName, device.getAddress(), type, hasRssi, rssi, isSelected, mListener.getContext());
    }

    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public int getItemCount() {
        return mDevices.size();
    }

    @Override // DeviceViewHolder.IDeviceViewHolder
    public void onClickItem(int position) {
        if (mSelectedItem == position) {
            mSelectedItem = ITEM_NULL;
        } else {
            int previousItem = mSelectedItem;
            mSelectedItem = position;
            notifyItemChanged(previousItem);
        }
        notifyItemChanged(position);
        mListener.onItemSelected(hasSelection());
    }

    /**
     * <p>To update the list with a new device or update the information of an existing one.</p>
     *
     * @param device
     *          The device to add or to update.
     * @param rssi
     *          The rssi which corresponds to the device.
     */
    public void add(BluetoothDevice device, int rssi) {
        synchronized (mDevices) {
            boolean contained = mDevices.contains(device);
            if (!contained) {
                mDevices.add(device);
                mRssi.add(rssi);
                notifyItemInserted(mDevices.size()-1);
            } else {
                int position = mDevices.indexOf(device);
                if (position < mRssi.size()) {
                    mRssi.add(position, rssi);
                }
                notifyItemChanged(position);
            }
        }
    }

    /**
     * To completely reset the data set list and clear it completely.
     */
    public void reset() {
        mRssi.clear();
        mDevices.clear();
        mSelectedItem = ITEM_NULL;
        notifyDataSetChanged();
    }

    /**
     * This method allows to know if the view has a selected item.
     *
     * @return true if the view has a selected item and false if none of the items is selected.
     */
    public boolean hasSelection() {
        return mSelectedItem >= 0 && mSelectedItem < mDevices.size();
    }

    /**
     * <p>To get the Device which is actually selected.</p>
     *
     * @return the selected device or null if there is no device selected.
     */
    public BluetoothDevice getSelectedItem() {
        if (hasSelection()) {
            return mDevices.get(mSelectedItem);
        }
        else {
            return null;
        }
    }

    /**
     * This method allows to define the data for the adapter.
     *
     * @param listDevices
     *            The list of devices to put on the RecyclerView.
     */
    public void setListDevices(ArrayList<BluetoothDevice> listDevices) {
        this.mDevices.clear();
        this.mDevices.addAll(listDevices);
        this.mRssi.clear();
        notifyDataSetChanged();
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the RecyclerView. Such as a
     * fragment or an activity.
     */
    public interface IDevicesListAdapterListener {
        /**
         * This method is called by the adapter when the user selects or deselects an item of the list.
         *
         * @param itemSelected
         *                  true if an item is selected, false otherwise.
         */
        void onItemSelected(boolean itemSelected);

        /**
         * <p>This method gets the current context. It is called to be able to change the drawable source for the
         * displayed RSSI signal strength of a Device. To do so, the current Context is required.</p>
         *
         * @return the Context of the IDevicesListAdapterListener.
         */
        Context getContext();
    }
}