/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import android.support.v4.util.ArrayMap;

import java.util.UUID;

/**
 * This class encapsulates all the different GATT services: names and UUIDs.
 * This class might be outdated: feel free to add more when they are published.
 */
@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused"})
public class Services {

    private static final ArrayMap<String, String> mServices = new ArrayMap<>();

    /*
     * <p>List of available Services names</p>
     * <p>The services are alphabetically ordered)</p>
     */
    public static final String SERVICE_ALERT_NOTIFICATION =  "Alert Notification";
    public static final String SERVICE_BATTERY = "Battery";
    public static final String SERVICE_BLOOD_PRESSURE = "Blood Pressure";
    public static final String SERVICE_CURRENT_TIME = "Current Time Service";
    public static final String SERVICE_CYCLING_POWER = "Cycling Power";
    public static final String SERVICE_CYCLING_SPEED_AND_CADENCE = "Cycling Speed and Cadence";
    public static final String SERVICE_DEVICE_INFORMATION = "Device Information";
    public static final String SERVICE_ENVIRONMENTAL_SENSING = "Environmental Sensing";
    public static final String SERVICE_GENERIC_ATTRIBUTE = "Generic Attribute";
    public static final String SERVICE_GENERIC_ACCESS = "Generic Access";
    public static final String SERVICE_GLUCOSE = "Glucose";
    public static final String SERVICE_HEALTH_THERMOMETER = "Health Thermometer";
    public static final String SERVICE_HEART_RATE = "Heart Rate";
    public static final String SERVICE_HUMAN_INTERFACE_DEVICE = "Human Interface Device";
    public static final String SERVICE_IMMEDIATE_ALERT = "Immediate Alert";
    public static final String SERVICE_LINK_LOSS = "Link Loss";
    public static final String SERVICE_LOCATION_AND_NAVIGATION = "Location and Navigation";
    public static final String SERVICE_NEXT_DST_CHANGE = "Next DST Change Service";
    public static final String SERVICE_PHONE_ALERT_STATUS = "Phone Alert Status Service";
    public static final String SERVICE_REFERENCE_TIME_UPDATE = "Reference Time Update Service";
    public static final String SERVICE_RUNNING_SPEED_AND_CADENCE = "Running Speed and Cadence";
    public static final String SERVICE_SCAN_PARAMETERS = "Scan Parameters";
    public static final String SERVICE_TX_POWER = "Tx Power";
    public static final String SERVICE_CSR_GAIA = "CSR GAIA";

    /*
     * The UUID completion
     */
    private final static String GATT_UUID = "-0000-1000-8000-00805f9b34fb";
    private final static String CSR_UUID = "-d102-11e1-9b23-00025b00a5a5";

    static {
        /*
         * List of available Services (might be outdated), feel free to add more as they are published.
         */
        mServices.put("00001811" + GATT_UUID, SERVICE_ALERT_NOTIFICATION);
        mServices.put("0000180f" + GATT_UUID, SERVICE_BATTERY);
        mServices.put("00001810" + GATT_UUID, SERVICE_BLOOD_PRESSURE);
        mServices.put("00001805" + GATT_UUID, SERVICE_CURRENT_TIME);
        mServices.put("00001818" + GATT_UUID, SERVICE_CYCLING_POWER);
        mServices.put("00001816" + GATT_UUID, SERVICE_CYCLING_SPEED_AND_CADENCE);
        mServices.put("0000180a" + GATT_UUID, SERVICE_DEVICE_INFORMATION);
        mServices.put("0000181a" + GATT_UUID, SERVICE_ENVIRONMENTAL_SENSING);
        mServices.put("00001800" + GATT_UUID, SERVICE_GENERIC_ACCESS);
        mServices.put("00001801" + GATT_UUID, SERVICE_GENERIC_ATTRIBUTE);
        mServices.put("00001808" + GATT_UUID, SERVICE_GLUCOSE);
        mServices.put("00001809" + GATT_UUID, SERVICE_HEALTH_THERMOMETER);
        mServices.put("0000180d" + GATT_UUID, SERVICE_HEART_RATE);
        mServices.put("00001812" + GATT_UUID, SERVICE_HUMAN_INTERFACE_DEVICE);
        mServices.put("00001802" + GATT_UUID, SERVICE_IMMEDIATE_ALERT);
        mServices.put("00001803" + GATT_UUID, SERVICE_LINK_LOSS);
        mServices.put("00001819" + GATT_UUID, SERVICE_LOCATION_AND_NAVIGATION);
        mServices.put("00001807" + GATT_UUID, SERVICE_NEXT_DST_CHANGE);
        mServices.put("0000180e" + GATT_UUID, SERVICE_PHONE_ALERT_STATUS);
        mServices.put("00001806" + GATT_UUID, SERVICE_REFERENCE_TIME_UPDATE);
        mServices.put("00001814" + GATT_UUID, SERVICE_RUNNING_SPEED_AND_CADENCE);
        mServices.put("00001813" + GATT_UUID, SERVICE_SCAN_PARAMETERS);
        mServices.put("00001804" + GATT_UUID, SERVICE_TX_POWER);
        mServices.put("00001100" + CSR_UUID, SERVICE_CSR_GAIA);
    }

    /**
     * To retrieve a Service UUID from its name.
     *
     * @param name
     *          The Service name
     *
     * @return The Service UUID.
     */
    public static UUID getStringServiceUUID(String name) {
        if (mServices.containsValue(name)) {
            for (int i=0; i<mServices.size(); i++) {
                if (mServices.valueAt(i).equals(name)) {
                    return UUID.fromString(mServices.keyAt(i));
                }
            }
        }

        return null;
    }

    /**
     * To get a Service name from its uuid.
     *
     * @param uuid
     *          the Service uuid value
     *
     * @return The Service name
     */
    public static String getServiceName(final String uuid)
    {
        String result = mServices.get(uuid);
        if(result == null) return "Unknown Service";
        return result;
    }

    /**
     * To know if a uuid belongs to the known services list.
     *
     * @param uuid
     *          the uuid to look for.
     *
     * @return true if the service belongs to the known services list.
     */
    public static boolean isService(final String uuid) {
        return mServices.containsKey(uuid);
    }

}
