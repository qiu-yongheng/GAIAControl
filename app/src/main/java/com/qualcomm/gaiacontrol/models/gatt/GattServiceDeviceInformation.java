/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattService;

/**
 * <p>To get the DEVICE INFORMATION GATT Service.</p>
 */
class GattServiceDeviceInformation {
    /**
     * <p>The DEVICE INFORMATION Service known as {@link GATT.UUIDs#SERVICE_DEVICE_INFORMATION_UUID
     * SERVICE_DEVICE_INFORMATION_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;

    /**
     * <p>To know if the GATT DEVICE INFORMATION Service is supported by the the remote device.</p>
     * <p>The GATT LINK LOSS Service is supported if the following GATT service UUID has
     * been provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_DEVICE_INFORMATION_UUID SERVICE_DEVICE_INFORMATION_UUID}</li>
     * </ul></p>
     * <p>All GATT characteristics of this service are optional.</p>
     *
     * @return True if the DEVICE INFORMATION Service is considered as supported.
     */
    public boolean isSupported() {
        return isServiceAvailable();
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the DEVICE INFORMATION service.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the DEVICE INFORMATION service, false otherwise.
     */
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_DEVICE_INFORMATION_UUID)) {
            mGattService = gattService;
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT DEVICE INFORMATION Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To fully reset this object.</p>
     */
    void reset() {
        mGattService = null;
    }

    @Override // Object
    public String toString() {
        return "DEVICE INFORMATION Service " +
                (isServiceAvailable() ? "available." : "not available.");
    }
}
