/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattCharacteristic;

import com.qualcomm.libraries.ble.Characteristics;
import com.qualcomm.libraries.ble.Services;

import java.util.UUID;

/**
 * <p>Represents all GATT constants this application uses.</p>
 */
@SuppressWarnings("unused")
public final class GATT {

    /**
     * <p>Represents the number of bytes for {@link BluetoothGattCharacteristic#FORMAT_UINT8 FORMAT_UINT8}.</p>
     */
    public static final int UINT8_LENGTH_IN_BYTES = 1;
    /**
     * <p>Represents the number of bytes for {@link BluetoothGattCharacteristic#FORMAT_UINT16 FORMAT_UINT16}.</p>
     */
    public static final int UINT16_LENGTH_IN_BYTES = 2;

    /**
     * <p>Regroups all GATT UUIDs this application uses.</p>
     */
    public static class UUIDs {

        /**
         * The UUID of the GAIA GATT service.
         */
        public static final UUID SERVICE_GAIA_UUID = Services.getStringServiceUUID(Services.SERVICE_CSR_GAIA);
        /**
         * The UUID of the GAIA GATT characteristic response endpoint.
         */
        public static final UUID CHARACTERISTIC_GAIA_RESPONSE_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_CSR_GAIA_RESPONSE_ENDPOINT);
        /**
         * The UUID of the GAIA GATT characteristic command endpoint.
         */
        public static final UUID CHARACTERISTIC_GAIA_COMMAND_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT);
        /**
         * The UUID of the GAIA GATT characteristic data endpoint.
         */
        public static final UUID CHARACTERISTIC_GAIA_DATA_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_CSR_GAIA_DATA_ENDPOINT);
        /**
         * The UUID of the link loss GATT service.
         */
        public static final UUID SERVICE_LINK_LOSS_UUID = Services.getStringServiceUUID(Services.SERVICE_LINK_LOSS);
        /**
         * The UUID of the GATT characteristic alert level.
         */
        public static final UUID CHARACTERISTIC_ALERT_LEVEL_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_ALERT_LEVEL);
        /**
         * The UUID of the immediate alert GATT service.
         */
        public static final UUID SERVICE_IMMEDIATE_ALERT_UUID =
                Services.getStringServiceUUID(Services.SERVICE_IMMEDIATE_ALERT);
        /**
         * The UUID of the tx power GATT service.
         */
        public static final UUID SERVICE_TX_POWER_UUID = Services.getStringServiceUUID(Services.SERVICE_TX_POWER);
        /**
         * The UUID of the GATT characteristic tx power level.
         */
        public static final UUID CHARACTERISTIC_TX_POWER_LEVEL_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_TX_POWER_LEVEL);
        /**
         * The UUID of the tx power GATT service.
         */
        public static final UUID SERVICE_BATTERY_UUID = Services.getStringServiceUUID(Services.SERVICE_BATTERY);
        /**
         * The UUID of the GATT characteristic tx power level.
         */
        public static final UUID CHARACTERISTIC_BATTERY_LEVEL_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_BATTERY_LEVEL);
        /**
         * The UUID of the GATT descriptor CHARACTERISTIC PRESENTATION FORMAT.
         */
        public static final UUID DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT =
                Characteristics.CHARACTERISTIC_PRESENTATION_FORMAT;
        /**
         * The UUID of the heart rate GATT service.
         */
        public static final UUID SERVICE_HEART_RATE_UUID = Services.getStringServiceUUID(Services.SERVICE_HEART_RATE);
        /**
         * The UUID of the device information GATT service.
         */
        public static final UUID SERVICE_DEVICE_INFORMATION_UUID =
                Services.getStringServiceUUID(Services.SERVICE_DEVICE_INFORMATION);
        /**
         * The UUID of the GATT characteristic heart rate measurement.
         */
        public static final UUID CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_HEART_RATE_MEASUREMENT);
        /**
         * The UUID of the GATT descriptor client characteristic configuration.
         */
        public static final UUID DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID =
                Characteristics.CLIENT_CHARACTERISTIC_CONFIG;
        /**
         * The UUID of the GATT characteristic body sensor location.
         */
        public static final UUID CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_BODY_SENSOR_LOCATION);
        /**
         * The UUID of the GATT characteristic heart rate control point.
         */
        public static final UUID CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID =
                Characteristics.getCharacteristicUUID(Characteristics.CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
    }

    /**
     * <p>Complementary information for the structure or content of ALERT LEVEL characteristic known as
     * {@link UUIDs#CHARACTERISTIC_ALERT_LEVEL_UUID CHARACTERISTIC_ALERT_LEVEL_UUID}.</p>
     * <p>The Alert Level characteristic defines the level of alert and is one of the values defined in
     * {@link Levels Levels}.</p>
     */
    public static class AlertLevel {
        /**
         * <p>Represents all possible values for the alert level.</p>
         */
        public static class Levels {
            /**
             * <p>The "no alert" level.</p>
             */
            public static final int NONE = 0;
            /**
             * <p>The "mild alert" level.</p>
             */
            public static final int MILD = 1;
            /**
             * <p>The "high alert" level.</p>
             */
            public static final int HIGH = 2;
            /**
             * <p>The total number of levels for the alert level characteristic.</p>
             */
            public static final int NUMBER_OF_LEVELS = 3;
        }

        /**
         * <p>Defines the offset of the level value contained in Alert Level characteristic.</p>
         */
        public static final int LEVEL_BYTE_OFFSET = 0;
        /**
         * <p>Defines the format of the level value contained in Alert Level characteristic.</p>
         */
        public static final int LEVEL_FORMAT = BluetoothGattCharacteristic.FORMAT_UINT8;
        /**
         * <p>Defines the length of the data contained in Alert Level characteristic.</p>
         */
        public static final int DATA_LENGTH_IN_BYTES = 1;
    }

    /**
     * <p>Complementary information for the structure or content of TX POWER LEVEL characteristic known as
     * {@link UUIDs#CHARACTERISTIC_TX_POWER_LEVEL_UUID CHARACTERISTIC_TX_POWER_LEVEL_UUID}.</p>
     * <p>The TX Power Level characteristic represents the current transmit power level in dBm, and the level
     * ranges from -100 dBm to +20 dBm to a resolution of 1 dBm.</p>
     */
    public static class TxPowerLevel {
        /**
         * <p>Defines the offset of the level value contained in TX Power Level characteristic.</p>
         */
        public static final int LEVEL_BYTE_OFFSET = 0;
        /**
         * <p>Defines the format of the level value contained in TX Power Level characteristic.</p>
         */
        public static final int LEVEL_FORMAT = BluetoothGattCharacteristic.FORMAT_SINT8;
    }

    /**
     * <p>Complementary information for the structure or content of PRESENTATION FORMAT descriptor known as
     * {@link UUIDs#DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT}.</p>
     * <p>This descriptor contains data as follows:
     * <blockquote><pre>
     * 0 bytes     1           2           3           4           5           6           7
     * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+
     * |  FORMAT   | EXPONENT  |         UNIT          | NAMESPACE |      DESCRIPTION      |
     * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+
     * </pre>
     *
     * </blockquote></p>
     *
     * <p>This descriptor is used in this application as a descriptor of the BATTERY LEVEL characteristic for the
     * BATTERY Service, in order to describe the battery to which the Battery Service corresponds. This descriptor
     * is present for each BATTERY LEVEL characteristic when a device has more than one instance of the Battery
     * Service as described in the official Battery Service description.</p>
     * <p>To get the Battery Service description this application looked at:
     * <ul>
     *     <li><code>NAMESPACE</code>: should correspond to
     *     {@link PresentationFormat.Namespace#BLUETOOTH_SIG_ASSIGNED_NUMBERS BLUETOOTH_SIG_ASSIGNED_NUMBERS}.</li>
     *     <li><code>DESCRIPTION</code>: should corresponds to one of the
     *     {@link PresentationFormat.Description Description} values.</li>
     * </ul></p>
     */
    public static class PresentationFormat {
        /**
         * The offset for the namespace value.
         */
        public static final int NAMESPACE_BYTE_OFFSET = 4;
        /**
         * The number of bytes which contain the namespace value.
         */
        public static final int NAMESPACE_LENGTH_IN_BYTES = 1;
        /**
         * The offset for the description value.
         */
        public static final int DESCRIPTION_BYTE_OFFSET = NAMESPACE_BYTE_OFFSET + NAMESPACE_LENGTH_IN_BYTES;
        /**
         * The number of bytes which contain the description value.
         */
        public static final int DESCRIPTION_LENGTH_IN_BYTES = 2;
        /**
         * The data length for the Presentation Format descriptor value/data.
         */
        public static final int DATA_LENGTH_IN_BYTES = 7;

        /**
         * This class describes all the values the namespace field can take.
         */
        public static class Namespace {
            /**
             * The Bluetooth SIG Assigned numbers as defined by the Bluetooth specifications.
             */
            public static final int BLUETOOTH_SIG_ASSIGNED_NUMBERS = 0x01;
        }

        /**
         * <p>This class describes all the values the description field can take for this application.</p>
         */
        public static class Description {
            /**
             * <p>The value for the "unknown" name of the Bluetooth SIG assigned numbers namespace.</p>
             * <p>In this application, this value is used as a default one if the description does not match any known
             * value.</p>
             */
            public static final int UNKNOWN = 0x0000;
            /**
             * <p>The value for the "second" name of the Bluetooth SIG assigned numbers namespace.</p>
             * <p>For audio devices this value is used to describe "remote battery".</p>
             */
            public static final int SECOND = 0x0002;
            /**
             * <p>The value for the "third" name of the Bluetooth SIG assigned numbers namespace.</p>
             * <p>For audio devices this value is used to describe "peer battery".</p>
             */
            public static final int THIRD = 0x0003; /* for peer battery */
            /**
             * <p>The value for the "internal" name of the Bluetooth SIG assigned numbers namespace.</p>
             * <p>For audio devices this value is used to describe "local battery".</p>
             */
            public static final int INTERNAL = 0x010F; /* for local battery */
        }
    }

    /**
     * <p>Complementary information for the structure or content of HEART RATE MEASUREMENT characteristic known as
     * {@link UUIDs#CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID}.</p>
     * <p>This characteristic contains data as follows:
     * <blockquote><pre>
     * 0 byte      1 ...
     * +-----------+------ ... -----+-----------+-----------+-----------+
     * |  FLAGS*   |  HEART RATE*   |  ENERGY   |     RR INTERVALS      |
     * |  1 byte   |   1/2 bytes    |  2 bytes  | 2 bytes per interval  |
     * +-----------+------ ... -----+-----------+-----------+-----------+
     * * mandatory information</pre></blockquote></p>
     *
     * <ul>
     *     <li>The <code>FLAGS</code> contains complementary information for all other fields.</li>
     *     <li>The <code>HEART RATE</code> gives the heart rate value in the format describes in the flags.</li>
     *     <li>The <code>ENERGY</code> gives the expended energy since the last reset in KJ. This field is present
     *     depending on the corresponding flag value.</li>
     *     <li>The <code>RR INTERVAL</code> is present depending on the corresponding flag value. If present, it gives
     *     RR intervals values in a UINT16 format (2 bytes) for a resolution of 1/1024 second.</li>
     * </ul>
     */
    public static class HeartRateMeasurement {
        /**
         * The offset for the flags value.
         */
        public static final int FLAGS_BYTE_OFFSET = 0;
        /**
         * The number of bytes which contain the flags value.
         */
        public static final int FLAGS_LENGTH_IN_BYTES = 1;
        /**
         * The number of bytes which contain the expended energy value.
         */
        public static final int ENERGY_LENGTH_IN_BYTES = 2;

        /**
         * <p>This class contains complementary information for the flags field.</p>
         * <p>This field describes the following:
         * <blockquote><pre>
         * 0 bit      1          2          3          4          5
         * +----------+----------+----------+----------+----------+
         * |  FORMAT  |       SENSOR        |  ENERGY  | INTERVAL |
         * +----------+----------+----------+----------+----------+</pre></blockquote>
         * <ul>
         *     <li><code>FORMAT</code>: gives the format of the heart rate value.</li>
         *     <li><code>SENSOR</code>: gives the contact status of the sensor: detected, in contact or not
         *          supported.</li>
         *     <li><code>ENERGY</code>: tells if the energy expended is present in the bytes array.</li>
         *     <li><code>RR INTERVALS</code>: tells if the bytes array contains RR intervals.</li>
         * </ul></p>
         */
        public static class Flags {
            /**
             * The bit offset for the Heart Rate value format.
             */
            public static final int FORMAT_BIT_OFFSET = 0;
            /**
             * The number of bits in the Heart Rate value format.
             */
            public static final int FORMAT_LENGTH_IN_BITS = 1;
            /**
             * The bit offset for the sensor contact status.
             */
            public static final int SENSOR_CONTACT_STATUS_BIT_OFFSET = FORMAT_BIT_OFFSET + FORMAT_LENGTH_IN_BITS;
            /**
             * The number of bits which contain the sensor contact status information.
             */
            public static final int SENSOR_CONTACT_STATUS_LENGTH_IN_BITS = 2;
            /**
             * The bit offset for the energy expended status information.
             */
            public static final int ENERGY_EXPENDED_PRESENCE_BIT_OFFSET =
                    SENSOR_CONTACT_STATUS_BIT_OFFSET + SENSOR_CONTACT_STATUS_LENGTH_IN_BITS;
            /**
             * The number of bits which contain the energy expended presence information.
             */
            public static final int ENERGY_EXPENDED_PRESENCE_LENGTH_IN_BITS = 1;
            /**
             * The bit offset for the RR interval status information.
             */
            public static final int RR_INTERVAL_BIT_OFFSET =
                    ENERGY_EXPENDED_PRESENCE_BIT_OFFSET + ENERGY_EXPENDED_PRESENCE_LENGTH_IN_BITS;
            /**
             * The number of bits which contain the RR intervals presence information.
             */
            public static final int RR_INTERVAL_LENGTH_IN_BITS = 1;

            /**
             * <p>To get the flag value contained in the flags byte array, starting at the given offset and of the
             * given length.</p>
             *
             * @param flags
             *          The byte array containing the flag.
             * @param offset
             *          The bit offset at which to find the flag information.
             * @param length
             *          The number of bits representing the flag.
             *
             * @return The flag value.
             */
            public static int getFlag(byte flags, int offset, int length) {
                int mask = ((1 << length) - 1) << offset;
                return (flags & mask) >>> offset;
            }

            /**
             * <p>This class describes all the possible values of the heart rate value format field.</p>
             */
            public static class Format {
                /**
                 * The heart rate value format is UINT8.
                 */
                public static final int UINT8 = 0x00;
                /**
                 * The heart rate value format is UINT16.
                 */
                public static final int UINT16 = 0x01;
            }

            /**
             * <p>This class describes all the possible values of the sensor contact status.</p>
             */
            public static class SensorStatus {
                /**
                 * This status means the sensor is not supported as well as the value 0x01.
                 */
                public static final int NOT_SUPPORTED = 0x00;
                /**
                 * This status means the sensor is not supported as well as the value 0x00.
                 */
                public static final int NOT_SUPPORTED_2 = 0x01;
                /**
                 * This status means the sensor is supported but no or poor contact had been detected.
                 */
                public static final int SUPPORTED_WITH_NO_CONTACT_DETECTED = 0x02;
                /**
                 * This status means the sensor is supported and contact is detected.
                 */
                public static final int SUPPORTED_WITH_CONTACT_DETECTED = 0x03; /* in contact */
            }

            /**
             * <p>This class describes the different values of an information presence field.</p>
             */
            public static class Presence {
                /**
                 * This presence status indicates that the information is not present.
                 */
                public static final int NOT_PRESENT = 0x00;
                /**
                 * This presence status indicates that the information is present.
                 */
                public static final int PRESENT = 0x01;
            }
        }
    }

    /**
     * <p>Complementary information for the structure or content of BODY SENSOR LOCATION characteristic known as
     * {@link UUIDs#CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID}.</p>
     * <p>The Body sensor location characteristic gives the location of the sensor as one of the
     * {@link BodySensorLocation.Locations Locations}.</p>
     */
    public static class BodySensorLocation {
        /**
         * The byte offset for the location information.
         */
        public static final int LOCATION_BYTE_OFFSET = 0;
        /**
         * The number of bytes which contains the location information.
         */
        public static final int LOCATION_LENGTH_IN_BYTES = 1;

        /**
         * <p>This class describes all the known location as defined by the Bluetooth specifications.</p>
         */
        public static class Locations {
            /**
             * The sensor is worn in another location than the other ones described here.
             */
            public static final int OTHER = 0x00;
            /**
             * The sensor is worn at the chest.
             */
            public static final int CHEST = 0x01;
            /**
             * The sensor is worn at the wrist.
             */
            public static final int WRIST = 0x02;
            /**
             * The sensor is worn at the finger.
             */
            public static final int FINGER = 0x03;
            /**
             * The sensor is worn at the hand.
             */
            public static final int HAND = 0x04;
            /**
             * The sensor is worn at the ear lobe.
             */
            public static final int EAR_LOBE = 0x05;
            /**
             * The sensor is worn at the foot.
             */
            public static final int FOOT = 0x06;
        }
    }

    /**
     * <p>Complementary information for the structure or content of HEART RATE CONTROL POINT characteristic known as
     * {@link UUIDs#CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID CHARACTERISTIC_HEART_RATE_CONTROL_POINT_UUID}.</p>
     * <p>The heart rate control point characteristic allow to write a control value. All possible controls are given by
     * {@link HeartRateControlPoint.Controls Controls}.</p>
     */
    public static class HeartRateControlPoint {
        /**
         * The offset for the control information.
         */
        public static final int CONTROL_BYTE_OFFSET = 0;
        /**
         * The number of bytes which correspond to the control information.
         */
        public static final int CONTROL_LENGTH_IN_BYTES = 1;

        /**
         * <p>This class describes all the know controls which can be written on the characteristic.</p>
         */
        public static class Controls {
            /**
             * This control requests the sensor to reset all the energy expended which had been accumulated since the
             * last reset.
             */
            public static final byte RESET_ENERGY_EXPENDED = 0x01;
        }
    }
}
