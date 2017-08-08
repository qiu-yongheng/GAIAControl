/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.gatt;

import android.bluetooth.BluetoothGattService;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import java.util.List;

/**
 * <p>This class represents all the GATT services, GATT characteristics and GATT profiles which a remote device
 * might support in order to be used with this application. This class keeps the information for each entity if it
 * is known as supported by the device.</p>
 */
public class GATTServices {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * This is true if GATT is supported. GATT is known as supported if a list of
     * {@link BluetoothGattService BluetoothGattService} are passed to this object.
     */
    private boolean isSupported = false;
    /**
     * <p>To get the GAIA GATT Service and its characteristics.</p>
     */
    public final GattServiceGaia gattServiceGaia = new GattServiceGaia();
    /**
     * <p>To get the LINK LOSS GATT Service and its characteristics.</p>
     */
    public final GattServiceLinkLoss gattServiceLinkLoss = new GattServiceLinkLoss();
    /**
     * <p>To get the IMMEDIATE ALERT GATT Service and its characteristics.</p>
     */
    public final GattServiceImmediateAlert gattServiceimmediateAlert = new GattServiceImmediateAlert();
    /**
     * <p>To get the TX POWER GATT Service and its characteristics.</p>
     */
    public final GattServiceTxPower gattServicetxPower = new GattServiceTxPower();
    /**
     * <p>To get the HEART RATE GATT Service and its characteristics.</p>
     */
    public final GattServiceHeartRate gattServiceHeartRate = new GattServiceHeartRate();
    /**
     * <p>To get the DEVICE INFORMATION GATT Service and its characteristics.</p>
     */
    @SuppressWarnings("WeakerAccess")
    public final GattServiceDeviceInformation gattServiceDeviceInformation = new GattServiceDeviceInformation();
    /**
     * <p>The list of Battery services the remote device supplies. Audio devices can have more than one battery
     * services if they have peers or remote devices.</p>
     * <p>The key corresponds to the instance ID of the Battery Service as given by the
     * {@link BluetoothGattService BluetoothGattService} object when added to the GattServiceBattery.</p>
     */
    public final SimpleArrayMap<Integer, GattServiceBattery> gattServiceBatteries = new SimpleArrayMap<>();


    // ====== PUBLIC METHODS =======================================================================

    /**
     * <p>To set up the supported GATT Services and Characteristics through a list of BluetoothGattService.</p>
     *
     * @param services
     *          The list of provided GATT Services and Characteristics a device might have provided to the Android
     *          device.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public void setSupportedGattServices(List<BluetoothGattService> services) {
        assert services != null; // if services is null,
        isSupported = true;
        reset();

        // Loops through available GATT Services to know the available services
        for (BluetoothGattService gattService : services) {
            // are GAIA Service and its characteristics available?
            if (this.gattServiceGaia.checkService(gattService)) {}
            // are LINK LOSS Service and its characteristics available?
            else if (this.gattServiceLinkLoss.checkService(gattService)) {}
            // are IMMEDIATE ALERT Service and its characteristics available?
            else if (this.gattServiceimmediateAlert.checkService(gattService)) {
                Log.e("AURELIE", "passed here");
            }
            // are TX POWER Service and its characteristics available?
            else if (this.gattServicetxPower.checkService(gattService)) {}
            // are Battery Services and their characteristics available?
            else if (gattService.getUuid().equals(GATT.UUIDs.SERVICE_BATTERY_UUID)) {
                GattServiceBattery service = new GattServiceBattery();
                service.checkService(gattService);
                gattServiceBatteries.put(gattService.getInstanceId(), service);
            }
            // are HEART RATE Service and its characteristics available?
            else if (this.gattServiceHeartRate.checkService(gattService)) {}
            // are DEVICE INFORMATION Service and its characteristics available?
            else if (this.gattServiceDeviceInformation.checkService(gattService)) {}
        }
    }

    /**
     * <p>To fully reset this object.</p>
     */
    @SuppressWarnings("WeakerAccess")
    public void reset() {
        isSupported = false;
        gattServiceLinkLoss.reset();
        gattServiceGaia.reset();
        gattServiceimmediateAlert.reset();
        gattServicetxPower.reset();
        gattServiceBatteries.clear();
        gattServiceHeartRate.reset();
        gattServiceDeviceInformation.reset();
    }

    /**
     * <p>This method is used to find out if the PROXIMITY GATT Profile is supported by the device.</p>
     * <p>Only the LINK LOSS service is mandatory for this profile.</p>
     *
     * @return True if the Link Loss service is supported.
     */
    public boolean isGattProfileProximitySupported() {
        return gattServiceLinkLoss.isSupported();
    }

    /**
     * <p>This method is used to find out if the HEART RATE GATT Profile is supported by the device.</p>
     * <p>The HEART RATE service and the DEVICE INFORMATION service are mandatory for this profile.</p>
     *
     * @return True if both HEART RATE and DEVICE INFORMATION services are supported.
     */
    public boolean isGattProfileHeartRateSupported() {
        return gattServiceHeartRate.isSupported() && gattServiceDeviceInformation.isSupported();
    }

    /**
     * <p>This method is used to find out if the GATT FIND ME Profile is supported by the device.</p>
     * <p>Only the IMMEDIATE ALERT service is mandatory for this profile.</p>
     *
     * @return True if the immediate alert service is supported.
     */
    public boolean isGattProfileFindMeSupported() {
        return gattServiceimmediateAlert.isSupported();
    }

    /**
     * <p>This method is used to find out if the GATT BATTERY Service is supported by the device.</p>
     *
     * @return True if the remote device provides at least one Battery service.
     */
    public boolean isBatteryServiceSupported() {
        return !gattServiceBatteries.isEmpty();
    }

    /**
     * <p>To know if GATT is supported. GATT is known as supported if a list of services and characteristics had
     * been provided to this object through the {@link #setSupportedGattServices(List) setSupportedGattServices}.</p>
     *
     * @return False if this object had not been set up with a list of BluetoothGattService or had been reset.
     */
    @SuppressWarnings("unused")
    public boolean isSupported() {
        return isSupported;
    }

    @Override // Object
    public String toString() {
        String result = gattServiceGaia.toString() + "\n\n"
                        + gattServiceLinkLoss.toString() + "\n\n"
                        + gattServiceimmediateAlert.toString() + "\n\n"
                        + gattServicetxPower.toString() + "\n\n"
                        + gattServiceHeartRate.toString() + "\n\n"
                        + gattServiceDeviceInformation.toString();

        result += "\n\n" + gattServiceBatteries.size() + " BATTERY Service(s) available:";
        for (int i=0;i<gattServiceBatteries.size();i++) {
            int key = gattServiceBatteries.keyAt(i);
            GattServiceBattery service = gattServiceBatteries.get(key);
            result += "\ninstance " + key + ": " + service.toString();
        }

        return result;
    }

}
