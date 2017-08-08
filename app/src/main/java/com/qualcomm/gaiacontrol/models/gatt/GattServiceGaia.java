/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

/**
 * <p>To get the GAIA GATT Service and its Characteristics.</p>
 */
public class GattServiceGaia {
    /**
     * <p>The GAIA Service known as {@link GATT.UUIDs#SERVICE_GAIA_UUID SERVICE_GAIA_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;
    /**
     * <p>The GAIA RESPONSE characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_GAIA_RESPONSE_UUID CHARACTERISTIC_GAIA_RESPONSE_UUID}.</p>
     */
    private BluetoothGattCharacteristic mGaiaResponseCharacteristic = null;
    /**
     * <p>The GAIA COMMAND characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_GAIA_COMMAND_UUID CHARACTERISTIC_GAIA_COMMAND_UUID}.</p>
     */
    private BluetoothGattCharacteristic mGaiaCommandCharacteristic = null;
    /**
     * <p>The GAIA DATA characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_GAIA_DATA_UUID CHARACTERISTIC_GAIA_DATA_UUID}.</p>
     */
    private BluetoothGattCharacteristic mGaiaDataCharacteristic = null;

    /**
     * <p>To know if the GATT GAIA Service is supported by the the remote device.</p>
     * <p>The GATT GAIA Service is supported if the following GATT service and characteristic UUIDs have been
     * provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_GAIA_UUID SERVICE_GAIA_UUID}</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_GAIA_RESPONSE_UUID CHARACTERISTIC_GAIA_RESPONSE_UUID}: NOTIFY<br/>
     *     <i>For testing purposes using previous ADK versions, the NOTIFY property is bypassed.</i></li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_GAIA_COMMAND_UUID CHARACTERISTIC_GAIA_COMMAND_UUID}: WRITE</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_GAIA_DATA_UUID CHARACTERISTIC_GAIA_DATA_UUID}: READ</li>
     * </ul></p>
     *
     * @return True if the GAIA Service is considered as supported.
     */
    public boolean isSupported() {
        return isServiceAvailable() && isCharacteristicGaiaCommandAvailable()
                && isCharacteristicGaiaDataAvailable() && isCharacteristicGaiaResponseAvailable();
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the GAIA service.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the GAIA service, false otherwise.
     */
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_GAIA_UUID)) {
            mGattService = gattService;
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            // Loops through available Characteristics to know if GAIA services are available
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID characteristicUUID = gattCharacteristic.getUuid();
                if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_RESPONSE_UUID)
                    /*&& (gattCharacteristic.getProperties() & BluetoothGattCharacteristic
                                .PROPERTY_NOTIFY) > 0*/) { // workaround: property not part of the Service description
                    mGaiaResponseCharacteristic = gattCharacteristic;
                } else if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_COMMAND_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    mGaiaCommandCharacteristic = gattCharacteristic;
                } else if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_DATA_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    mGaiaDataCharacteristic = gattCharacteristic;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT GAIA Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To know if the GATT GAIA COMMAND Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isCharacteristicGaiaCommandAvailable() {
        return mGaiaCommandCharacteristic != null;
    }

    /**
     * <p>To know if the GATT GAIA DATA Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isCharacteristicGaiaDataAvailable() {
        return mGaiaDataCharacteristic != null;
    }

    /**
     * <p>To know if the GATT GAIA RESPONSE Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isCharacteristicGaiaResponseAvailable() {
        return mGaiaResponseCharacteristic != null;
    }

    /**
     * <p>To get the GATT GAIA COMMAND characteristic.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getGaiaCommandCharacteristic() {
        return mGaiaCommandCharacteristic;
    }

    /**
     * <p>To get the GATT GAIA DATA characteristic.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getGaiaDataCharacteristic() {
        return mGaiaDataCharacteristic;
    }

    /**
     * <p>To get the GATT GAIA RESPONSE characteristic.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getGaiaResponseCharacteristic() {
        return mGaiaResponseCharacteristic;
    }

    /**
     * <p>To fully reset this object.</p>
     */
    void reset() {
        mGattService = null;
        mGaiaDataCharacteristic = null;
        mGaiaResponseCharacteristic = null;
        mGaiaCommandCharacteristic = null;
    }

    @Override // Object
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("GAIA Service ");
        if (isServiceAvailable()) {
            message.append("available with the following characteristics:");
            message.append("\n\t- GAIA COMMAND");
            message.append(isCharacteristicGaiaCommandAvailable() ?
                    " available" : " not available or with wrong properties");
            message.append("\n\t- GAIA DATA");
            message.append(isCharacteristicGaiaDataAvailable() ?
                    " available" : " not available or with wrong properties");
            message.append("\n\t- GAIA RESPONSE");
            message.append(isCharacteristicGaiaResponseAvailable() ?
                    " available" : " not available or with wrong properties");
        }
        else {
            message.append("not available.");
        }

        return message.toString();
    }
}
