/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

/**
 * <p>To get the LINK LOSS GATT Service and its Characteristics.</p>
 */
public class GattServiceLinkLoss {
    /**
     * <p>The LINK LOSS Service known as {@link GATT.UUIDs#SERVICE_LINK_LOSS_UUID SERVICE_LINK_LOSS_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;
    /**
     * <p>The ALERT LEVEL characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_ALERT_LEVEL_UUID CHARACTERISTIC_ALERT_LEVEL_UUID} which corresponds to the
     * {@link GATT.UUIDs#SERVICE_LINK_LOSS_UUID SERVICE_LINK_LOSS_UUID}.</p>
     */
    private BluetoothGattCharacteristic mAlertLevelCharacteristic = null;

    /**
     * <p>To know if the GATT LINK LOSS Service is supported by the the remote device.</p>
     * <p>The GATT LINK LOSS Service is supported if the following GATT service and characteristic UUIDs have
     * been provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_LINK_LOSS_UUID SERVICE_LINK_LOSS_UUID}</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_ALERT_LEVEL_UUID CHARACTERISTIC_ALERT_LEVEL_UUID}: <code>READ,
     *     WRITE</code>.</li>
     * </ul></p>
     *
     * @return True if the LINK LOSS Service is considered as supported.
     */
    public boolean isSupported() {
        return isServiceAvailable() && isAlertLevelCharacteristicAvailable();
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the LINK LOSS service.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the LINK LOSS service, false otherwise.
     */
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_LINK_LOSS_UUID)) {
            mGattService = gattService;
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID characteristicUUID = gattCharacteristic.getUuid();
                if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_ALERT_LEVEL_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    mAlertLevelCharacteristic = gattCharacteristic;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT LINK LOSS Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To know if the GATT ALERT LEVEL Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isAlertLevelCharacteristicAvailable() {
        return mAlertLevelCharacteristic != null;
    }

    /**
     * <p>To get the GATT ALERT LEVEL characteristic for the LINK LOSS service.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getAlertLevelCharacteristic() {
        return mAlertLevelCharacteristic;
    }

    /**
     * <p>To fully reset this object.</p>
     */
    void reset() {
        mGattService = null;
        mAlertLevelCharacteristic = null;
    }

    @Override // Object
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("LINK LOSS Service ");
        if (isServiceAvailable()) {
            message.append("available with the following characteristics:");
            message.append("\n\t- ALERT LEVEL");
            message.append(isAlertLevelCharacteristicAvailable() ?
                    " available" : " not available or with wrong properties");
        }
        else {
            message.append("not available.");
        }

        return message.toString();
    }
}
