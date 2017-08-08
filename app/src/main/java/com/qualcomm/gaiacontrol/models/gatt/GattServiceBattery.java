/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.qualcomm.gaiacontrol.Utils;

import java.util.List;
import java.util.UUID;

/**
 * <p>To get a Battery Service and related information:
 * <ul>
 *     <li>The BATTERY LEVEL characteristic.</li>
 *     <li>The PRESENTATION FORMAT descriptor corresponding to the BATTERY LEVEL characteristic if exists.</li>
 * </ul></p>
 */
public class GattServiceBattery {
    /**
     * <p>The BATTERY Service known as {@link GATT.UUIDs#SERVICE_BATTERY_UUID SERVICE_BATTERY_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;
    /**
     * <p>The BATTERY LEVEL characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_BATTERY_LEVEL_UUID CHARACTERISTIC_BATTERY_LEVEL_UUID}.</p>
     */
    private BluetoothGattCharacteristic mBatteryLevelCharacteristic = null;
    /**
     * <p>The PRESENTATION FORMAT descriptor known as
     * {@link GATT.UUIDs#DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT}.
     * </p>
     */
    private BluetoothGattDescriptor mPresentationFormatDescriptor = null;
    /**
     * <p>The description value for this Battery Service given by the PRESENTATION FORMAT Descriptor.</p>
     */
    private int mDescription = GATT.PresentationFormat.Description.UNKNOWN;

    /**
     * <p>To know if the GATT BATTERY Service is supported by the the remote device.</p>
     * <p>The GATT BATTERY Service is supported if the following GATT service and characteristic UUIDs have been
     * provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_BATTERY_UUID SERVICE_BATTERY_UUID}</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_BATTERY_LEVEL_UUID CHARACTERISTIC_BATTERY_LEVEL_UUID}:
     *     <code>READ</code>.</li>
     * </ul></p>
     *
     * @return True if the BATTERY Service is considered as supported.
     */
    @SuppressWarnings("unused") // is kept for consistency
    public boolean isSupported() {
        return isServiceAvailable() && isBatteryLevelCharacteristicAvailable();
    }

    /**
     * <p>To get the battery level from the BATTERY LEVEL characteristic.</p>
     *
     * @return the cached value as known by the characteristic or -1 if the value couldn't be retrieved
     * or the characteristic is null.
     */
    public int getBatteryLevel() {
        if (mBatteryLevelCharacteristic != null) {
            return mBatteryLevelCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }
        return -1;
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the BATTERY service.</p>
     * <p>If it is, this method saves the needed information into this GattServiceBattery object.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the BATTERY service, false otherwise.
     */
    @SuppressWarnings("UnusedReturnValue") // we keep the return for consistency
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_BATTERY_UUID)) {
            mGattService = gattService;
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID characteristicUUID = gattCharacteristic.getUuid();
                if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_BATTERY_LEVEL_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    mBatteryLevelCharacteristic = gattCharacteristic;
                    mPresentationFormatDescriptor = gattCharacteristic.getDescriptor(
                            GATT.UUIDs.DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT BATTERY Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess") // is public for consistency
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To know if the GATT BATTERY LEVEL Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess") // is public for consistency
    public boolean isBatteryLevelCharacteristicAvailable() {
        return mBatteryLevelCharacteristic != null;
    }

    /**
     * <p>To know if the GATT PRESENTATION FORMAT descriptor had been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID had been provided by the device.
     */
    public boolean isPresentationFormatDescriptorAvailable() {
        return mPresentationFormatDescriptor != null;
    }

    /**
     * <p>To get the GATT BATTERY LEVEL characteristic.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getBatteryLevelCharacteristic() {
        return mBatteryLevelCharacteristic;
    }

    /**
     * <p>To get the GATT PRESENTATION FORMAT descriptor.</p>
     *
     * @return null if the descriptor is not supported.
     */
    public BluetoothGattDescriptor getPresentationFormatDescriptor() {
        return mPresentationFormatDescriptor;
    }

    /**
     * <p>To get the description value of this BATTERY Service.</p>
     * <p>The description value should match one the
     * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.PresentationFormat.Description Description} values.</p>
     *
     * @return the value given by the PRESENTATION FORMAT descriptor if provided,
     * {@link GATT.PresentationFormat.Description#UNKNOWN UNKNOWN} otherwise.
     */
    public int getDescription() {
        return mDescription;
    }

    /**
     * <p>This method should be called when this application receives a value for this service through a read
     * descriptor request.</p>
     */
    public void updateDescription() {
        // we can only use this if there is a PRESENTATION FORMAT DESCRIPTOR
        if (isPresentationFormatDescriptorAvailable()) {
            byte[] data = mPresentationFormatDescriptor.getValue();
            // if there is no data or not enough information the namespace and the description cannot be retrieved.
            if (data != null && data.length >= GATT.PresentationFormat.DATA_LENGTH_IN_BYTES) {
                int namespace = data[GATT.PresentationFormat.NAMESPACE_BYTE_OFFSET];
                // the namespace should be BLUETOOTH SIG ASSIGNED NUMBERS (0x01) to get a description
                if (namespace == GATT.PresentationFormat.Namespace.BLUETOOTH_SIG_ASSIGNED_NUMBERS) {
                    mDescription = Utils.extractIntFromByteArray(data, GATT.PresentationFormat.DESCRIPTION_BYTE_OFFSET,
                            GATT.PresentationFormat.DESCRIPTION_LENGTH_IN_BYTES, true);
                }
            }
        }
    }

    @Override // Object
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("BATTERY Service ");
        if (isServiceAvailable()) {
            message.append("available with the following characteristics:");
            message.append("\n\t- BATTERY LEVEL");
            if (isBatteryLevelCharacteristicAvailable()) {
                message.append(" available with the following descriptors:");
                message.append("\n\t\t- PRESENTATION FORMAT");
                message.append(isPresentationFormatDescriptorAvailable() ?
                        " available" : " not available or with wrong permissions");
            }
            else {
                message.append(" not available or with wrong properties");
            }
        }
        else {
            message.append("not available.");
        }

        return message.toString();
    }
}
