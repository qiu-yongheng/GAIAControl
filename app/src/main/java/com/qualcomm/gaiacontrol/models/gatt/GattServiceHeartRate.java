/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

/**
 * <p>To get the HEART RATE GATT Service and its Characteristics.</p>
 */
public class GattServiceHeartRate {
    /**
     * <p>The HEART RATE Service known as {@link GATT.UUIDs#SERVICE_HEART_RATE_UUID SERVICE_HEART_RATE_UUID}.</p>
     */
    private BluetoothGattService mGattService = null;
    /**
     * <p>The HEART RATE MEASUREMENT characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID} which corresponds
     * to the {@link GATT.UUIDs#SERVICE_HEART_RATE_UUID SERVICE_HEART_RATE_UUID}.</p>
     */
    private BluetoothGattCharacteristic mHeartRateMeasurementCharacteristic = null;
    /**
     * <p>To know if the CLIENT CHARACTERISTIC CONFIGURATION descriptor known as
     * {@link GATT.UUIDs#DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID}
     * which corresponds to the
     * {@link GATT.UUIDs#CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID} is
     * present.</p>
     */
    private boolean mHasClientCharacteristicConfigurationDescriptor = false;
    /**
     * <p>The HEART RATE MEASUREMENT characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID} which corresponds
     * to the {@link GATT.UUIDs#SERVICE_HEART_RATE_UUID SERVICE_HEART_RATE_UUID}.</p>
     */
    private BluetoothGattCharacteristic mBodySensorLocationCharacteristic = null;
    /**
     * <p>The HEART RATE MEASUREMENT characteristic known as
     * {@link GATT.UUIDs#CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID} which
     * corresponds to the {@link GATT.UUIDs#SERVICE_HEART_RATE_UUID SERVICE_HEART_RATE_UUID}.</p>
     */
    private BluetoothGattCharacteristic mHeartRateControlPointCharacteristic = null;

    /**
     * <p>To know if the GATT HEART RATE Service is supported by the the remote device.</p>
     * <p>The GATT HEART RATE Service is supported if the following mandatory GATT characteristic and descriptor UUIDs
     * had been provided by the device with the given properties:
     * <ul>
     *     <li>{@link GATT.UUIDs#SERVICE_HEART_RATE_UUID SERVICE_HEART_RATE_UUID}</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID}:
     *     <code>NOTIFY</code>.</li>
     *     <li>{@link GATT.UUIDs#DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID
     *     DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID}.</li>
     * </ul></p>
     * <p>The following GATT characteristics are optional or conditional:
     * <ul>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID} is optional
     *     with properties: <code>READ</code>.</li>
     *     <li>{@link GATT.UUIDs#CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID} is
     *     conditional and should be provided if the Energy Expended field is provided by the HEART RATE MEASUREMENT
     *     characteristic. This characteristic has the following properties: <code>WRITE</code>.</li>
     * </ul></p>
     *
     * @return True if the HEART RATE Service is considered as supported.
     */
    public boolean isSupported() {
        return isServiceAvailable() && isHeartRateMeasurementCharacteristicAvailable()
                && isClientCharacteristicConfigurationDescriptorAvailable();
    }

    /**
     * <p>To get the values of the HEART RATE MEASUREMENT characteristic and store them into a
     * {@link HeartRateMeasurementValues HeartRateMeasurementValues} object.</p>
     * <p>The raw data of the HEART RATE MEASUREMENT characteristic is as follows:<blockquote><pre>
     *      0 byte      1 ...
     *      +-----------+------ ... -----+-----------+-----------+-----------+
     *      |  FLAGS*   |  HEART RATE*   |  ENERGY   |     RR INTERVALS      |
     *      |  1 byte   |   1/2 bytes    |  2 bytes  | 2 bytes per interval  |
     *      +-----------+------ ... -----+-----------+-----------+-----------+
     *      * mandatory information
     * </pre></blockquote></p>
     * <p>The FLAGS describes the following:<blockquote><pre>
     *      0 bit      1          2          3          4          5
     *      +----------+----------+----------+----------+----------+
     *      |  FORMAT  |       SENSOR        |  ENERGY  | INTERVAL |
     *      +----------+----------+----------+----------+----------+
     * </pre></blockquote>
     * <ul>Meaning of the flags:
     *     <li><code>FORMAT</code>: describes the format of the heart rate value.</li>
     *     <li><code>SENSOR</code>: describes the contact status of the sensor: detected, in contact or not
     *     supported.</li>
     *     <li><code>ENERGY</code>: tells if the energy expended is present in the bytes array.</li>
     *     <li><code>RR INTERVALS</code>: tells if the bytes array contains the RR intervals.</li>
     * </ul></p>
     *
     * @return the cached values as known by the characteristic. If a value is unknown the value is set up to the
     * default unknown value {@link HeartRateMeasurementValues#NO_VALUE NO_VALUE}.
     */
    public HeartRateMeasurementValues getHeartRateMeasurementValues() {
        HeartRateMeasurementValues values = new HeartRateMeasurementValues();

        if (isHeartRateMeasurementCharacteristicAvailable()) {
            int offset = GATT.HeartRateMeasurement.FLAGS_BYTE_OFFSET;
            byte[] data = mHeartRateMeasurementCharacteristic.getValue();

            // 1. get the flags values
            byte flags = data[offset];
            values.flags.heartRateFormat = GATT.HeartRateMeasurement.Flags.getFlag(flags,
                    GATT.HeartRateMeasurement.Flags.FORMAT_BIT_OFFSET,
                    GATT.HeartRateMeasurement.Flags.FORMAT_LENGTH_IN_BITS);
            values.flags.sensorContactStatus = GATT.HeartRateMeasurement.Flags.getFlag(flags,
                    GATT.HeartRateMeasurement.Flags.SENSOR_CONTACT_STATUS_BIT_OFFSET,
                    GATT.HeartRateMeasurement.Flags.SENSOR_CONTACT_STATUS_LENGTH_IN_BITS);
            values.flags.energyExpendedPresence = GATT.HeartRateMeasurement.Flags.getFlag(flags,
                    GATT.HeartRateMeasurement.Flags.ENERGY_EXPENDED_PRESENCE_BIT_OFFSET,
                    GATT.HeartRateMeasurement.Flags.ENERGY_EXPENDED_PRESENCE_LENGTH_IN_BITS);
            values.flags.rrIntervalPresence = GATT.HeartRateMeasurement.Flags.getFlag(flags,
                    GATT.HeartRateMeasurement.Flags.RR_INTERVAL_BIT_OFFSET,
                    GATT.HeartRateMeasurement.Flags.RR_INTERVAL_LENGTH_IN_BITS);

            // 2. get the heart rate value
            offset += GATT.HeartRateMeasurement.FLAGS_LENGTH_IN_BYTES; // we move the offset
            int valueLength;
            if ((values.flags.heartRateFormat == GATT.HeartRateMeasurement.Flags.Format.UINT8)) {
                values.heartRateValue = mHeartRateMeasurementCharacteristic
                        .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                valueLength = GATT.UINT8_LENGTH_IN_BYTES;
            }
            else if ((values.flags.heartRateFormat == GATT.HeartRateMeasurement.Flags.Format.UINT16)) {
                values.heartRateValue = mHeartRateMeasurementCharacteristic
                        .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                valueLength = GATT.UINT16_LENGTH_IN_BYTES;
            }
            else {
                // the format is unknown, it is not possible to process
                return values;
            }
            offset += valueLength;

            // 3. get the energy expended if present
            if (values.flags.energyExpendedPresence == GATT.HeartRateMeasurement.Flags.Presence.PRESENT) {
                values.energy = mHeartRateMeasurementCharacteristic
                        .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                offset += GATT.HeartRateMeasurement.ENERGY_LENGTH_IN_BYTES;
            }

            // 4. get the RR interval
            if (values.flags.rrIntervalPresence == GATT.HeartRateMeasurement.Flags.Presence.PRESENT) {
                // There can be multiple RR values, each value corresponds to 2 bytes
                int length = data.length - offset;
                if (length%2 == 0) {
                    int intervals = length / 2;
                    values.rrIntervals = new int[intervals];

                    for (int i=0; i<intervals;i++) {
                        int value = mHeartRateMeasurementCharacteristic
                                .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                        values.rrIntervals[i] = (int) (value / 1024.0 * 1000.0); // resolution is 1/1024 second
                        offset += GATT.UINT16_LENGTH_IN_BYTES;
                    }
                }
            }
        }

        return values;
    }

    /**
     * <p>This method checks if the given BluetoothGattService corresponds to the HEART RATE service.</p>
     *
     * @param gattService
     *          The BluetoothGattService to check.
     *
     * @return True if the gattService is the HEART RATE service, false otherwise.
     */
    boolean checkService(BluetoothGattService gattService) {
        if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_HEART_RATE_UUID)) {
            mGattService = gattService;
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : characteristics) {
                UUID characteristicUUID = gattCharacteristic.getUuid();
                if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mHeartRateMeasurementCharacteristic = gattCharacteristic;
                    mHasClientCharacteristicConfigurationDescriptor = gattCharacteristic.getDescriptor(
                            GATT.UUIDs.DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) != null;
                }
                else if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    mBodySensorLocationCharacteristic = gattCharacteristic;
                }
                else if (characteristicUUID.equals(GATT.UUIDs.CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID)
                        && (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    mHeartRateControlPointCharacteristic = gattCharacteristic;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>To know if the GATT HEART RATE Service has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isServiceAvailable() {
        return mGattService != null;
    }

    /**
     * <p>To know if the GATT HEART RATE MEASUREMENT Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    public boolean isHeartRateMeasurementCharacteristicAvailable() {
        return mHeartRateMeasurementCharacteristic != null;
    }

    /**
     * <p>To know if the GATT CLIENT CHARACTERISTIC CONFIGURATION Descriptor had been provided by the remote device
     * for the GATT HEART RATE MEASUREMENT Characteristic.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    public boolean isClientCharacteristicConfigurationDescriptorAvailable() {
        return mHasClientCharacteristicConfigurationDescriptor;
    }

    /**
     * <p>To know if the GATT BODY SENSOR LOCATION Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    public boolean isBodySensorLocationCharacteristicAvailable() {
        return mBodySensorLocationCharacteristic != null;
    }

    /**
     * <p>To know if the GATT HEART RATE CONTROL POINT Characteristic has been provided by the remote device.</p>
     *
     * @return True if the corresponding UUID has been provided by the device.
     */
    public boolean isHeartRateControlPointCharacteristicAvailable() {
        return mHeartRateControlPointCharacteristic != null;
    }

    /**
     * <p>To get the GATT HEART RATE MEASUREMENT characteristic for the HEART RATE service.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getHeartRateMeasurementCharacteristic() {
        return mHeartRateMeasurementCharacteristic;
    }

    /**
     * <p>To get the GATT BODY SENSOR LOCATION characteristic for the HEART RATE service.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getBodySensorLocationCharacteristic() {
        return mBodySensorLocationCharacteristic;
    }

    /**
     * <p>To get the GATT HEART RATE CONTROL POINT characteristic for the HEART RATE service.</p>
     *
     * @return null if the characteristic is not supported.
     */
    public BluetoothGattCharacteristic getHeartRateControlPointCharacteristic() {
        return mHeartRateControlPointCharacteristic;
    }

    /**
     * <p>To fully reset this object.</p>
     */
    void reset() {
        mGattService = null;
        mHeartRateMeasurementCharacteristic = null;
        mHasClientCharacteristicConfigurationDescriptor = false;
        mBodySensorLocationCharacteristic = null;
        mHeartRateControlPointCharacteristic = null;
    }

    @Override // Object
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("HEART RATE Service ");
        if (isServiceAvailable()) {
            message.append("available with the following characteristics:");

            // 1. Heart rate measurement
            message.append("\n\t- HEART RATE MEASUREMENT");
            if (isHeartRateMeasurementCharacteristicAvailable()) {
                message.append(" available with the following descriptors:");
                message.append("\n\t\t- CLIENT CHARACTERISTIC CONFIGURATION");
                message.append(isClientCharacteristicConfigurationDescriptorAvailable() ?
                        " available" : " not available or with wrong permissions");
            }
            else {
                message.append(" not available or with wrong properties");
            }

            // 2. Body sensor location
            message.append("\n\t- BODY SENSOR LOCATION");
            message.append(isBodySensorLocationCharacteristicAvailable() ?
                    " available" : " not available or with wrong properties");

            // 2. Body sensor location
            message.append("\n\t- HEART RATE CONTROL POINT");
            message.append(isHeartRateControlPointCharacteristicAvailable() ?
                    " available" : " not available or with wrong properties");
        }
        else {
            message.append("not available.");
        }

        return message.toString();
    }

    /**
     * <p>To represent all the values which can be retrieved from the HEART RATE MEASUREMENT characteristic as the data
     * has been described in
     * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.HeartRateMeasurement HeartRateMeasurement}.</p>
     */
    public class HeartRateMeasurementValues {
        /**
         * The default value for all the values given by the Heart Rate Measurement characteristic.
         */
        public static final int NO_VALUE = -1;
        /**
         * Represents the heart rate value, in BPM.
         */
        public int heartRateValue = NO_VALUE;
        /**
         * Represents the energy expended value, in KJ.
         */
        public int energy = NO_VALUE;
        /**
         * <p>Represents the RR intervals given by the characteristic.</p>
         * <p>This array can:
         * <ul>
         *     <li>be empty or null: means there is no interval.</li>
         *     <li>contain some values: all the values given by the characteristic, there can be one or more
         *     values.</li>
         * </ul></p>
         * <p>If there is any value contained here, they are in milliseconds.</p>
         */
        public int[] rrIntervals;
        /**
         * Represents all the flag values.
         */
        public final Flags flags = new Flags();

        /**
         * <p>To represent all the values of the FLAGS field.</p>
         */
        public class Flags {
            /**
             * Gives the heart rate format of the heart rate value.
             */
            int heartRateFormat = NO_VALUE;
            /**
             * Gives the sensor contact status, one of the
             * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.HeartRateMeasurement.Flags.SensorStatus SensorStatus}.
             */
            public int sensorContactStatus = NO_VALUE;
            /**
             * Gives the presence of the energy expended, one of the
             * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.HeartRateMeasurement.Flags.Presence Presence}.
             */
            public int energyExpendedPresence = NO_VALUE;
            /**
             * Gives the presence of any RR intervals, one of the
             * {@link com.qualcomm.gaiacontrol.models.gatt.GATT.HeartRateMeasurement.Flags.Presence Presence}.
             */
            int rrIntervalPresence = NO_VALUE;
        }
    }
}
