/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * <p>This class allows reception of information from the system about Bluetooth devices bond state information.</p>
 * <p>This receiver should be used with the following intent filter:
 * {@link BluetoothDevice#ACTION_BOND_STATE_CHANGED ACTION_BOND_STATE_CHANGED} and
 * {@link BluetoothDevice#ACTION_PAIRING_REQUEST ACTION_PAIRING_REQUEST}.</p>
 */
public class BondStateReceiver extends BroadcastReceiver {
    /**
     * The listener to dispatch events from this receiver.
     */
    private final BondStateListener mListener;

    /**
     * <p>The constructor of this class.</p>
     *
     * @param listener The listener to inform of broadcast events from this receiver.
     */
    public BondStateReceiver(BondStateListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            if (device != null && state > -1) {
                mListener.onBondStateChange(device, state);
            }
        }
        else if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                mListener.onBondStateChange(device, BluetoothDevice.BOND_BONDING);
            }
        }
    }

    /**
     * <p>The listener for the {@link BondStateReceiver BondStateReceiver} receiver.</p>
     */
    public interface BondStateListener {
        /**
         * <p>The method to dispatch bond state change to a listener of this receiver.</p>
         *
         * @param device
         *          The device for which the bond state has changed.
         * @param state
         *          The new bond state.
         */
        void onBondStateChange(BluetoothDevice device, int state);
    }

}
