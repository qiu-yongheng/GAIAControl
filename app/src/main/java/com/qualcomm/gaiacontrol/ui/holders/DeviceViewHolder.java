/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.holders;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.Utils;

import java.util.Locale;

/**
 * <p>This view holder represents a device item display. It is used in a Devices list to display and update the
 * information of a device for the layout {@link R.layout#list_devices_item list_devices_item}.</p>
 */
public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    /**
     * The text view to display the device name.
     */
    private final TextView textViewDeviceName;
    /**
     * The text view to display the device address.
     */
    private final TextView textViewDeviceAddress;
    /**
     * The text view to display the RSSI for the device.
     */
    private final TextView textViewDeviceRssi;
    /**
     * The text view to display the type of the device.
     */
    private final TextView textViewDeviceType;
    /**
     * The checkbox to select the item.
     */
    private final CheckBox checkbox;
    /**
     * The instance of the parent to interact with it as a listener.
     */
    private final IDeviceViewHolder mListener;

    /**
     * <p>The constructor which will instantiate the views to use for this holder.</p>
     *
     * @param rowView
     *              The main view which contains all the views this holder should use.
     * @param listener
     *          The instance of the parent to interact with it as a listener.
     */
    public DeviceViewHolder(View rowView, IDeviceViewHolder listener) {
        super(rowView);
        textViewDeviceName = (TextView) rowView.findViewById(R.id.tv_device_name);
        textViewDeviceAddress = (TextView) rowView.findViewById(R.id.tv_device_address);
        textViewDeviceRssi = (TextView) rowView.findViewById(R.id.tv_device_rssi);
        textViewDeviceType = (TextView) rowView.findViewById(R.id.tv_device_type);
        checkbox = (CheckBox)rowView.findViewById(R.id.cb_item_selected);
        checkbox.setOnClickListener(this);
        mListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override // View.OnClickListener
    public void onClick(View v) {
        mListener.onClickItem(this.getAdapterPosition());
    }

    /**
     * <p>This method is for refreshing all the values displayed in the corresponding view which show all information
     * related to a Device.</p>
     *
     * @param name
     *          The name which has to be displayed.
     * @param address
     *          The Bluetooth address which has to be displayed.
     * @param type
     *          The type of the device, one of the following:
     *          {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_LE DEVICE_TYPE_LE},
     *          {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_CLASSIC DEVICE_TYPE_CLASSIC},
     *          {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_DUAL DEVICE_TYPE_DUAL} or
     *          {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_UNKNOWN DEVICE_TYPE_UNKNOWN},
     * @param hasRssi
     *          To know if the rssi should be displayed.
     * @param rssi
     *          The rssi to display if one has to be displayed.
     * @param isSelected
     *          To know if the device should be displayed as selected.
     * @param context
     *          The context to use to load images for the view.
     */
    public void refreshValues(String name, String address, int type, boolean hasRssi, int rssi, boolean isSelected,
                              Context context) {
        // display name
        textViewDeviceName.setText(name);
        // display bluetooth address
        textViewDeviceAddress.setText(address);

        // display RSSI level if known
        int rssiVisibility = hasRssi ? View.VISIBLE : View.GONE;
        textViewDeviceRssi.setVisibility(rssiVisibility);
        if (hasRssi) {
            textViewDeviceRssi.setText(String.format(Locale.getDefault(), "%d dBm", rssi));
            textViewDeviceRssi.setCompoundDrawablesWithIntrinsicBounds(null,
                    Utils.getSignalIconFromRssi(context, rssi), null, null);
        }
        // display bluetooth device type
        int typeLabel = type == BluetoothDevice.DEVICE_TYPE_CLASSIC ? R.string.device_type_classic :
                type == BluetoothDevice.DEVICE_TYPE_LE ? R.string.device_type_le :
                        type == BluetoothDevice.DEVICE_TYPE_DUAL ? R.string.device_type_dual :
                                R.string.device_type_unknown;
        textViewDeviceType.setText(typeLabel);

        // display check if device is selected
        checkbox.setChecked(isSelected);
    }

    /**
     * The interface to allow this class to interact with its parent.
     */
    public interface IDeviceViewHolder {
        /**
         * This method is called when the user clicks on the main view of an item.
         *
         * @param position
         *              The position of the item in the list.
         */
        void onClickItem(int position);
    }
}
