/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * <p>This class allows reception of information from the system about Bluetooth devices which have been found during a
 * device discovery.</p>
 * <p>This receiver should be used with the following intent filter:
 * {@link BluetoothDevice#ACTION_FOUND ACTION_FOUND}.</p>
 */
public class BREDRDiscoveryReceiver extends BroadcastReceiver {
    /**
     * The listener to dispatch events from this receiver.
     */
    private final BREDRDiscoveryListener mListener;

    /**
     * <p>The constructor of this class.</p>
     *
     * @param listener The listener to inform of broadcast events from this receiver.
     */
    public BREDRDiscoveryReceiver(BREDRDiscoveryListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                mListener.onDeviceFound(device);
            }
        }
    }

    /**
     * <p>The listener for the {@link BREDRDiscoveryReceiver BREDRDiscoveryReceiver} receiver.</p>
     */
    public interface BREDRDiscoveryListener {
        /**
         * <p>The method to dispatch a found device to a listener of this receiver.</p>
         *
         * @param device
         *          The device which had been found.
         */
        void onDeviceFound(BluetoothDevice device);
    }

}
