/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import android.support.v4.util.ArrayMap;
import java.util.UUID;

/**
 * This class encapsulates all the different GATT characteristics: names and UUIDs.
 * This class might be outdated: feel free to add more when they are published.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Characteristics {

    private static final ArrayMap<String, String> mCharacteristics = new ArrayMap<>();

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_PRESENTATION_FORMAT = UUID.fromString
            ("00002904-0000-1000-8000-00805f9b34fb");

    /**
     * <p></p>List of available Characteristics names</p>
     * <p>Might be outdated: feel free to add more as they are published (They are alphabetically ordered)</p>
     */
    public static final String CHARACTERISTIC_ALERT_CATEGORY_ID = "Alert Category ID";
    public static final String CHARACTERISTIC_ALERT_CATEGORY_ID_BIT_MASK = "Alert Category ID Bit Mask";
    public static final String CHARACTERISTIC_ALERT_LEVEL = "Alert Level";
    public static final String CHARACTERISTIC_ALERT_NOTIFICATION_CONTROL_POINT = "Alert Notification Control Point";
    public static final String CHARACTERISTIC_ALERT_STATUS = "Alert Status";
    public static final String CHARACTERISTIC_APPEARANCE = "Appearance";
    public static final String CHARACTERISTIC_BATTERY_LEVEL = "Battery Level";
    public static final String CHARACTERISTIC_BLOOD_PRESSURE_FEATURE = "Blood Pressure Feature";
    public static final String CHARACTERISTIC_BLOOD_PRESSURE_MEASUREMENT = "Blood Pressure Measurement";
    public static final String CHARACTERISTIC_BODY_SENSOR_LOCATION = "Body Sensor Location";
    public static final String CHARACTERISTIC_BOOT_KEYBOARD_INPUT_REPORT = "Boot Keyboard Input Report";
    public static final String CHARACTERISTIC_BOOT_KEYBOARD_OUTPUT_REPORT = "Boot Keyboard Output Report";
    public static final String CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT = "Boot Mouse Input Report";
    public static final String CHARACTERISTIC_CSC_FEATURE = "CSC Feature";
    public static final String CHARACTERISTIC_CSC_MEASUREMENT = "CSC Measurement";
    public static final String CHARACTERISTIC_CURRENT_TIME = "Current Time";
    public static final String CHARACTERISTIC_CYCLING_POWER_CONTROL_POINT = "Cycling Power Control Point";
    public static final String CHARACTERISTIC_CYCLING_POWER_FEATURE = "Cycling Power Feature";
    public static final String CHARACTERISTIC_CYCLING_POWER_MEASUREMENT = "Cycling Power Measurement";
    public static final String CHARACTERISTIC_CYCLING_POWER_VECTOR = "Cycling Power Vector";
    public static final String CHARACTERISTIC_DATE_TIME = "Date Time";
    public static final String CHARACTERISTIC_DAY_DATE_TIME = "Day Date Time";
    public static final String CHARACTERISTIC_DAY_OF_WEEK = "Day of Week";
    public static final String CHARACTERISTIC_DEVICE_NAME = "Device Name";
    public static final String CHARACTERISTIC_DST_OFFSET = "DST Offset";
    public static final String CHARACTERISTIC_EXACT_TIME_256 = "Exact Time 256";
    public static final String CHARACTERISTIC_FIRMWARE_REVISION_STRING = "Firmware Revision String";
    public static final String CHARACTERISTIC_GLUCOSE_FEATURE = "Glucose Feature";
    public static final String CHARACTERISTIC_GLUCOSE_MEASUREMENT = "Glucose Measurement";
    public static final String CHARACTERISTIC_GLUCOSE_MEASUREMENT_CONTEXT = "Glucose Measurement Context";
    public static final String CHARACTERISTIC_HARDWARE_REVISION = "Hardware Revision String";
    public static final String CHARACTERISTIC_HEART_RATE_CONTROL_POINT = "Heart Rate Control Point";
    public static final String CHARACTERISTIC_HEART_RATE_MEASUREMENT = "Heart Rate Measurement";
    public static final String CHARACTERISTIC_HID_CONTROL_POINT = "HID Control Point";
    public static final String CHARACTERISTIC_HID_INFORMATION = "HID Information";
    public static final String CHARACTERISTIC_IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST = "IEEE 11073-20601 Regulatory Certification Data List";
    public static final String CHARACTERISTIC_INTERMEDIATE_CUFF_PRESSURE = "Intermediate Cuff Pressure";
    public static final String CHARACTERISTIC_INTERMEDIATE_TEMPERATURE = "Intermediate Temperature";
    public static final String CHARACTERISTIC_LN_CONTROL_POINT = "LN Control Point";
    public static final String CHARACTERISTIC_LN_FEATURE = "LN Feature";
    public static final String CHARACTERISTIC_LOCAL_TIME_INFORMATION = "Local Time Information";
    public static final String CHARACTERISTIC_LOCATION_AND_SPEED = "Location and Speed";
    public static final String CHARACTERISTIC_MANUFACTURER_NAME_STRING = "Manufacturer Name String";
    public static final String CHARACTERISTIC_MEASUREMENT_INTERVAL = "Measurement Interval";
    public static final String CHARACTERISTIC_MODEL_NUMBER_STRING = "Model Number String";
    public static final String CHARACTERISTIC_NAVIGATION = "Navigation";
    public static final String CHARACTERISTIC_NEW_ALERT = "New Alert";
    public static final String CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "Peripheral Preferred Connection Parameters";
    public static final String CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG = "Peripheral Privacy Flag";
    public static final String CHARACTERISTIC_PNP_ID = "PnP ID";
    public static final String CHARACTERISTIC_POSITION_QUALITY = "Position Quality";
    public static final String CHARACTERISTIC_PROTOCOL_MODE = "Protocol Mode";
    public static final String CHARACTERISTIC_RECONNECTION_ADDRESS = "Reconnection Address";
    public static final String CHARACTERISTIC_RECORD_ACCESS_CONTROL_POINT = "Record Access Control Point";
    public static final String CHARACTERISTIC_REFERENCE_TIME_INFORMATION = "Reference Time Information";
    public static final String CHARACTERISTIC_REPORT = "Report";
    public static final String CHARACTERISTIC_REPORT_MAP = "Report Map";
    public static final String CHARACTERISTIC_RINGER_CONTROL_POINT = "Ringer Control Point";
    public static final String CHARACTERISTIC_RINGER_SETTING = "Ringer Setting";
    public static final String CHARACTERISTIC_RSC_FEATURE = "RSC Feature";
    public static final String CHARACTERISTIC_RSC_MEASUREMENT = "RSC Measurement";
    public static final String CHARACTERISTIC_SC_CONTROL_POINT = "SC Control Point";
    public static final String CHARACTERISTIC_SCAN_INTERVAL_WINDOW = "Scan Interval Window";
    public static final String CHARACTERISTIC_SCAN_REFRESH = "Scan Refresh";
    public static final String CHARACTERISTIC_SENSOR_LOCATION = "Sensor Location";
    public static final String CHARACTERISTIC_SERIAL_NUMBER_STRING = "Serial Number String";
    public static final String CHARACTERISTIC_SERVICE_CHANGED = "Service Changed";
    public static final String CHARACTERISTIC_SOFTWARE_REVISION_STRING = "Software Revision String";
    public static final String CHARACTERISTIC_SUPPORTED_NEW_ALERT_CATEGORY = "Supported New Alert Category";
    public static final String CHARACTERISTIC_SUPPORTED_UNREAD_ALERT_CATEGORY = "Supported Unread Alert Category";
    public static final String CHARACTERISTIC_SYSTEM_ID = "System ID";
    public static final String CHARACTERISTIC_TEMPERATURE_MEASUREMENT = "Temperature Measurement";
    public static final String CHARACTERISTIC_TEMPERATURE_TYPE = "Temperature Type";
    public static final String CHARACTERISTIC_TIME_ACCURACY = "Time Accuracy";
    public static final String CHARACTERISTIC_TIME_SOURCE = "Time Source";
    public static final String CHARACTERISTIC_TIME_UPDATE_CONTROL_POINT = "Time Update Control Point";
    public static final String CHARACTERISTIC_TIME_UPDATE_STATE = "Time Update State";
    public static final String CHARACTERISTIC_TIME_WITH_DST = "Time with DST";
    public static final String CHARACTERISTIC_TIME_ZONE = "Time Zone";
    public static final String CHARACTERISTIC_TX_POWER_LEVEL = "Tx Power Level";
    public static final String CHARACTERISTIC_UNREAD_ALERT_STATUS = "Unread Alert Status";


    /**
     * <p></p>List of available Characteristics names for the Environmental Sensing service</p>
     * <p>Might be outdated: feel free to add more as they are published (They are alphabetically ordered)</p>
     */
    public static final String CHARACTERISTIC_DESCRIPTOR_VALUE_CHANGED = "Descriptor Value Changed";
    public static final String CHARACTERISTIC_APPARENT_WIND_DIRECTION = "Apparent Wind Direction";
    public static final String CHARACTERISTIC_APPARENT_WIND_SPEED = "Apparent Wind Speed";
    public static final String CHARACTERISTIC_ELEVATION = "Elevation";
    public static final String CHARACTERISTIC_GUST_FACTOR = "Gust Factor";
    public static final String CHARACTERISTIC_HEAT_INDEX = "Heat Index";
    public static final String CHARACTERISTIC_HUMIDITY = "Humidity";
    public static final String CHARACTERISTIC_IRRADIANCE = "Irradiance";
    public static final String CHARACTERISTIC_MAGNETIC_DECLINATION = "Magnetic Declination";
    public static final String CHARACTERISTIC_MAGNETIC_FLUX_DENSITY_2D = "Magnetic Flux Density - 2D";
    public static final String CHARACTERISTIC_MAGNETIC_FLUX_DENSITY_3D = "Magnetic Flux Density - 3D";
    public static final String CHARACTERISTIC_POLLEN_CONCENTRATION = "Pollen concentration";
    public static final String CHARACTERISTIC_PRESSURE = "Pressure";
    public static final String CHARACTERISTIC_RAINFALL = "Rainfall";
    public static final String CHARACTERISTIC_TEMPERATURE = "Temperature";
    public static final String CHARACTERISTIC_TRUE_WIND_DIRECTION = "True Wind Direction";
    public static final String CHARACTERISTIC_TRUE_WIND_SPEED = "True Wind Speed";
    public static final String CHARACTERISTIC_UV_INDEX = "UV Index";
    public static final String CHARACTERISTIC_WIND_CHILL = "Wind Chill";
    public static final String CHARACTERISTIC_CSR_ACCELERATION = "CSR Acceleration";
    public static final String CHARACTERISTIC_CSR_ANGULAR_RATE = "CSR Angular Rate";
    public static final String CHARACTERISTIC_CSR_MAGNETOMETER_CALIBRATION = "CSR Magnetometer Calibration";

    public static final String CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT = "CSR GAIA Command Endpoint";
    public static final String CHARACTERISTIC_CSR_GAIA_RESPONSE_ENDPOINT = "CSR GAIA Response Endpoint";
    public static final String CHARACTERISTIC_CSR_GAIA_DATA_ENDPOINT = "CSR GAIA Data Endpoint";

    /**
     * The UUID completion.
     */
    private final static String GATT_UUID = "-0000-1000-8000-00805f9b34fb";
    private final static String CSR_UUID = "-d102-11e1-9b23-00025b00a5a5";

    static {
        mCharacteristics.put("00002a05" + GATT_UUID, CHARACTERISTIC_SERVICE_CHANGED);
        mCharacteristics.put("00002a00" + GATT_UUID, CHARACTERISTIC_DEVICE_NAME);
        mCharacteristics.put("00002a01" + GATT_UUID, CHARACTERISTIC_APPEARANCE);
        mCharacteristics.put("00002a04" + GATT_UUID, CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS);
        mCharacteristics.put("00002a19" + GATT_UUID, CHARACTERISTIC_BATTERY_LEVEL);
        mCharacteristics.put("00002a25" + GATT_UUID, CHARACTERISTIC_SERIAL_NUMBER_STRING);
        mCharacteristics.put("00002a27" + GATT_UUID, CHARACTERISTIC_HARDWARE_REVISION);
        mCharacteristics.put("00002a26" + GATT_UUID, CHARACTERISTIC_FIRMWARE_REVISION_STRING);
        mCharacteristics.put("00002a28" + GATT_UUID, CHARACTERISTIC_SOFTWARE_REVISION_STRING);
        mCharacteristics.put("00002a29" + GATT_UUID, CHARACTERISTIC_MANUFACTURER_NAME_STRING);
        mCharacteristics.put("00002a50" + GATT_UUID, CHARACTERISTIC_PNP_ID);
        mCharacteristics.put("00002a4a" + GATT_UUID, CHARACTERISTIC_HID_INFORMATION);
        mCharacteristics.put("00002a4b" + GATT_UUID, CHARACTERISTIC_REPORT_MAP);
        mCharacteristics.put("00002a4d" + GATT_UUID, CHARACTERISTIC_REPORT);
        mCharacteristics.put("00002a4c" + GATT_UUID, CHARACTERISTIC_HID_CONTROL_POINT);
        mCharacteristics.put("00001101" + CSR_UUID, CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT);
        mCharacteristics.put("00001102" + CSR_UUID, CHARACTERISTIC_CSR_GAIA_RESPONSE_ENDPOINT);
        mCharacteristics.put("00001103" + CSR_UUID, CHARACTERISTIC_CSR_GAIA_DATA_ENDPOINT);

        /**
         * List of available Characteristics (might be outdated), feel free to add more as they are
         * published (They are alphabetically ordered)
         */
        mCharacteristics.put("00002a43" + GATT_UUID, CHARACTERISTIC_ALERT_CATEGORY_ID);
        mCharacteristics.put("00002a42" + GATT_UUID, CHARACTERISTIC_ALERT_CATEGORY_ID_BIT_MASK);
        mCharacteristics.put("00002a06" + GATT_UUID, CHARACTERISTIC_ALERT_LEVEL);
        mCharacteristics.put("00002a44" + GATT_UUID, CHARACTERISTIC_ALERT_NOTIFICATION_CONTROL_POINT);
        mCharacteristics.put("00002a3f" + GATT_UUID, CHARACTERISTIC_ALERT_STATUS);
        mCharacteristics.put("00002a49" + GATT_UUID, CHARACTERISTIC_BLOOD_PRESSURE_FEATURE);
        mCharacteristics.put("00002a35" + GATT_UUID, CHARACTERISTIC_BLOOD_PRESSURE_MEASUREMENT);
        mCharacteristics.put("00002a38" + GATT_UUID, CHARACTERISTIC_BODY_SENSOR_LOCATION);
        mCharacteristics.put("00002a22" + GATT_UUID, CHARACTERISTIC_BOOT_KEYBOARD_INPUT_REPORT);
        mCharacteristics.put("00002a32" + GATT_UUID, CHARACTERISTIC_BOOT_KEYBOARD_OUTPUT_REPORT);
        mCharacteristics.put("00002a33" + GATT_UUID, CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT);
        mCharacteristics.put("00002a5c" + GATT_UUID, CHARACTERISTIC_CSC_FEATURE);
        mCharacteristics.put("00002a5b" + GATT_UUID, CHARACTERISTIC_CSC_MEASUREMENT);
        mCharacteristics.put("00002a2b" + GATT_UUID, CHARACTERISTIC_CURRENT_TIME);
        mCharacteristics.put("00002a66" + GATT_UUID, CHARACTERISTIC_CYCLING_POWER_CONTROL_POINT);
        mCharacteristics.put("00002a65" + GATT_UUID, CHARACTERISTIC_CYCLING_POWER_FEATURE);
        mCharacteristics.put("00002a63" + GATT_UUID, CHARACTERISTIC_CYCLING_POWER_MEASUREMENT);
        mCharacteristics.put("00002a64" + GATT_UUID, CHARACTERISTIC_CYCLING_POWER_VECTOR);
        mCharacteristics.put("00002a08" + GATT_UUID, CHARACTERISTIC_DATE_TIME);
        mCharacteristics.put("00002a0a" + GATT_UUID, CHARACTERISTIC_DAY_DATE_TIME);
        mCharacteristics.put("00002a09" + GATT_UUID, CHARACTERISTIC_DAY_OF_WEEK);
        mCharacteristics.put("00002a0d" + GATT_UUID, CHARACTERISTIC_DST_OFFSET);
        mCharacteristics.put("00002a0c" + GATT_UUID, CHARACTERISTIC_EXACT_TIME_256);
        mCharacteristics.put("00002a51" + GATT_UUID, CHARACTERISTIC_GLUCOSE_FEATURE);
        mCharacteristics.put("00002a18" + GATT_UUID, CHARACTERISTIC_GLUCOSE_MEASUREMENT);
        mCharacteristics.put("00002a34" + GATT_UUID, CHARACTERISTIC_GLUCOSE_MEASUREMENT_CONTEXT);
        mCharacteristics.put("00002a39" + GATT_UUID, CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
        mCharacteristics.put("00002a37" + GATT_UUID, CHARACTERISTIC_HEART_RATE_MEASUREMENT);
        mCharacteristics.put("00002a2a" + GATT_UUID, CHARACTERISTIC_IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST);
        mCharacteristics.put("00002a36" + GATT_UUID, CHARACTERISTIC_INTERMEDIATE_CUFF_PRESSURE);
        mCharacteristics.put("00002a1e" + GATT_UUID, CHARACTERISTIC_INTERMEDIATE_TEMPERATURE);
        mCharacteristics.put("00002a6b" + GATT_UUID, CHARACTERISTIC_LN_CONTROL_POINT);
        mCharacteristics.put("00002a6a" + GATT_UUID, CHARACTERISTIC_LN_FEATURE);
        mCharacteristics.put("00002a0f" + GATT_UUID, CHARACTERISTIC_LOCAL_TIME_INFORMATION);
        mCharacteristics.put("00002a67" + GATT_UUID, CHARACTERISTIC_LOCATION_AND_SPEED);
        mCharacteristics.put("00002a21" + GATT_UUID, CHARACTERISTIC_MEASUREMENT_INTERVAL);
        mCharacteristics.put("00002a24" + GATT_UUID, CHARACTERISTIC_MODEL_NUMBER_STRING);
        mCharacteristics.put("00002a68" + GATT_UUID, CHARACTERISTIC_NAVIGATION);
        mCharacteristics.put("00002a46" + GATT_UUID, CHARACTERISTIC_NEW_ALERT);
        mCharacteristics.put("00002a04" + GATT_UUID, CHARACTERISTIC_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS);
        mCharacteristics.put("00002a02" + GATT_UUID, CHARACTERISTIC_PERIPHERAL_PRIVACY_FLAG);
        mCharacteristics.put("00002a69" + GATT_UUID, CHARACTERISTIC_POSITION_QUALITY);
        mCharacteristics.put("00002a4e" + GATT_UUID, CHARACTERISTIC_PROTOCOL_MODE);
        mCharacteristics.put("00002a03" + GATT_UUID, CHARACTERISTIC_RECONNECTION_ADDRESS);
        mCharacteristics.put("00002a52" + GATT_UUID, CHARACTERISTIC_RECORD_ACCESS_CONTROL_POINT);
        mCharacteristics.put("00002a14" + GATT_UUID, CHARACTERISTIC_REFERENCE_TIME_INFORMATION);
        mCharacteristics.put("00002a40" + GATT_UUID, CHARACTERISTIC_RINGER_CONTROL_POINT);
        mCharacteristics.put("00002a41" + GATT_UUID, CHARACTERISTIC_RINGER_SETTING);
        mCharacteristics.put("00002a54" + GATT_UUID, CHARACTERISTIC_RSC_FEATURE);
        mCharacteristics.put("00002a53" + GATT_UUID, CHARACTERISTIC_RSC_MEASUREMENT);
        mCharacteristics.put("00002a55" + GATT_UUID, CHARACTERISTIC_SC_CONTROL_POINT);
        mCharacteristics.put("00002a4f" + GATT_UUID, CHARACTERISTIC_SCAN_INTERVAL_WINDOW);
        mCharacteristics.put("00002a31" + GATT_UUID, CHARACTERISTIC_SCAN_REFRESH);
        mCharacteristics.put("00002a5d" + GATT_UUID, CHARACTERISTIC_SENSOR_LOCATION);
        mCharacteristics.put("00002a47" + GATT_UUID, CHARACTERISTIC_SUPPORTED_NEW_ALERT_CATEGORY);
        mCharacteristics.put("00002a48" + GATT_UUID, CHARACTERISTIC_SUPPORTED_UNREAD_ALERT_CATEGORY);
        mCharacteristics.put("00002a23" + GATT_UUID, CHARACTERISTIC_SYSTEM_ID);
        mCharacteristics.put("00002a1c" + GATT_UUID, CHARACTERISTIC_TEMPERATURE_MEASUREMENT);
        mCharacteristics.put("00002a1d" + GATT_UUID, CHARACTERISTIC_TEMPERATURE_TYPE);
        mCharacteristics.put("00002a12" + GATT_UUID, CHARACTERISTIC_TIME_ACCURACY);
        mCharacteristics.put("00002a13" + GATT_UUID, CHARACTERISTIC_TIME_SOURCE);
        mCharacteristics.put("00002a16" + GATT_UUID, CHARACTERISTIC_TIME_UPDATE_CONTROL_POINT);
        mCharacteristics.put("00002a17" + GATT_UUID, CHARACTERISTIC_TIME_UPDATE_STATE);
        mCharacteristics.put("00002a11" + GATT_UUID, CHARACTERISTIC_TIME_WITH_DST);
        mCharacteristics.put("00002a0e" + GATT_UUID, CHARACTERISTIC_TIME_ZONE);
        mCharacteristics.put("00002a07" + GATT_UUID, CHARACTERISTIC_TX_POWER_LEVEL);
        mCharacteristics.put("00002a45" + GATT_UUID, CHARACTERISTIC_UNREAD_ALERT_STATUS);

        /**
         * List of characteristics for the service Environmental Sensing
         */
        mCharacteristics.put("00002a7d" + GATT_UUID, CHARACTERISTIC_DESCRIPTOR_VALUE_CHANGED);
        mCharacteristics.put("00002a73" + GATT_UUID, CHARACTERISTIC_APPARENT_WIND_DIRECTION);
        mCharacteristics.put("00002a72" + GATT_UUID, CHARACTERISTIC_APPARENT_WIND_SPEED);
        mCharacteristics.put("0000aaa1" + CSR_UUID, CHARACTERISTIC_CSR_ACCELERATION);
        mCharacteristics.put("0000aaa2" + CSR_UUID, CHARACTERISTIC_CSR_ANGULAR_RATE);
        mCharacteristics.put("0000aaa4" + CSR_UUID, CHARACTERISTIC_CSR_MAGNETOMETER_CALIBRATION);
        mCharacteristics.put("00002a6c" + GATT_UUID, CHARACTERISTIC_ELEVATION);
        mCharacteristics.put("00002a7a" + GATT_UUID, CHARACTERISTIC_GUST_FACTOR);
        mCharacteristics.put("00002a7a" + GATT_UUID, CHARACTERISTIC_HEAT_INDEX);
        mCharacteristics.put("00002a6f" + GATT_UUID, CHARACTERISTIC_HUMIDITY);
        mCharacteristics.put("00002a77" + GATT_UUID, CHARACTERISTIC_IRRADIANCE);
        mCharacteristics.put("00002a2c" + GATT_UUID, CHARACTERISTIC_MAGNETIC_DECLINATION);
        mCharacteristics.put("00002aa0" + GATT_UUID, CHARACTERISTIC_MAGNETIC_FLUX_DENSITY_2D);
        mCharacteristics.put("00002aa1" + GATT_UUID, CHARACTERISTIC_MAGNETIC_FLUX_DENSITY_3D);
        mCharacteristics.put("00002a75" + GATT_UUID, CHARACTERISTIC_POLLEN_CONCENTRATION);
        mCharacteristics.put("00002a6d" + GATT_UUID, CHARACTERISTIC_PRESSURE);
        mCharacteristics.put("00002a78" + GATT_UUID, CHARACTERISTIC_RAINFALL);
        mCharacteristics.put("00002a6e" + GATT_UUID, CHARACTERISTIC_TEMPERATURE);
        mCharacteristics.put("00002a71" + GATT_UUID, CHARACTERISTIC_TRUE_WIND_DIRECTION);
        mCharacteristics.put("00002a70" + GATT_UUID, CHARACTERISTIC_TRUE_WIND_SPEED);
        mCharacteristics.put("00002a76" + GATT_UUID, CHARACTERISTIC_UV_INDEX);
        mCharacteristics.put("00002a79" + GATT_UUID, CHARACTERISTIC_WIND_CHILL);

    }

    /**
     * To retrieve a Characteristic UUID from its name.
     *
     * @param name
     *          The Characteristic name
     *
     * @return The Characteristic UUID.
     */
    public static UUID getCharacteristicUUID(String name) {
        if (mCharacteristics.containsValue(name)) {
            for (int i=0; i< mCharacteristics.size(); i++) {
                if (mCharacteristics.valueAt(i).equals(name)) {
                    return UUID.fromString(mCharacteristics.keyAt(i));
                }
            }
        }

        return null;
    }

    /**
     * To get a Characteristic name from its uuid.
     *
     * @param uuid
     *          the Characteristic uuid value
     *
     * @return The Characteristic name
     */
    public static String getCharacteristicName(final String uuid)
    {
        String result = mCharacteristics.get(uuid);
        if(result == null) return "Unknown Characteristic";
        return result;
    }

    /**
     * To know if a uuid belongs to the known characteristics list.
     *
     * @param uuid
     *          the uuid to look for.
     *
     * @return true if the characteristic belongs to the known services list.
     */
    public static boolean isCharacteristic(final String uuid) {
        return mCharacteristics.containsKey(uuid);
    }


}
