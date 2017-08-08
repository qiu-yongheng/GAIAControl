/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

/**
 * <p>To get the TX POWER GATT Service and its Characteristics.</p>
 */
public class GattServiceTxPower {
    /**
     * <p>The TX POWER Service known as {@link GATT.UUIDs#SERVICE_TX_POWER_UUID SERVICE_TX_POWER_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;
    /**
     * <p>The TX POWER LEVEL characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_TX_POWER_LEVEL_UUID CHARACTERISTIC_TX_POWER_LEVEL_UUID}.</p>
     */
    private BluetoothGattCharacteristic mTxPowerLevelCharacteristic = null;

    /**
     * <p>To know if the GATT TX POWER Service is supported by the the remote device.</p>
     * <p>The GATT TX POWER Service is supported if the following GATT service and characteristic UUIDs had been
     * provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_TX_POWER_UUID SERVICE_TX_POWER_UUID}</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_TX_POWER_LEVEL_UUID CHARACTERISTIC_TX_POWER_LEVEL_UUID}:
     *     <code>READ</code>.</li>
     * </ul></p>
     *
     * @return True if the TX POWER Service is considered as supported.
     */
    public boolean isSupported() {
        return isServiceAvailable() && isTxPowerLevelCharacteristicAvailable();
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the TX POWER service.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the TX POWER service, false otherwise.
     */
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_TX_POWER_UUID)) {
            mGattService = gattService;
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID characteristicUUID = gattCharacteristic.getUuid();
                if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_TX_POWER_LEVEL_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    mTxPowerLevelCharacteristic = gattCharacteristic;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT TX POWER Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To know if the GATT TX POWER LEVEL Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isTxPowerLevelCharacteristicAvailable() {
        return mTxPowerLevelCharacteristic != null;
    }

    /**
     * <p>To get the GATT TX POWER LEVEL characteristic.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getTxPowerLevelCharacteristic() {
        return mTxPowerLevelCharacteristic;
    }

    /**
     * <p>To fully reset this object.</p>
     */
    void reset() {
        mGattService = null;
        mTxPowerLevelCharacteristic = null;
    }

    @Override // Object
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("TX POWER Service ");
        if (isServiceAvailable()) {
            message.append("available with the following characteristics:");
            message.append("\n\t- TX POWER LEVEL");
            message.append(isTxPowerLevelCharacteristicAvailable() ?
                    " available" : " not available or with wrong properties");
        }
        else {
            message.append("not available.");
        }

        return message.toString();
    }
}
