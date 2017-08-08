/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * <p>This class contains all useful methods for this library.</p>
 */
@SuppressWarnings("WeakerAccess")
public class BLEUtils {

    /**
     * Get 16-bit hexadecimal string representation of byte.
     *
     * @param i
     *         The value.
     *
     * @return Hex value as a string.
     */
    @SuppressWarnings("WeakerAccess") // this static method can also be used by another class
    public static String getIntToHexadecimal(int i) {
        return String.format("0x%04X", i & 0xFFFF);
    }

    /**
     * <p>To get a label for the different known bond states.</p>
     * <p>If the given state is not one of the states known as {@link BluetoothDevice#BOND_NONE BOND_NONE},
     * {@link BluetoothDevice#BOND_BONDING BOND_BONDING} and {@link BluetoothDevice#BOND_BONDED BOND_BONDED}, this
     * method returns "UNKNOWN" as the label.</p>
     *
     * @param state
     *          The state to get a corresponding label for.
     *
     * @return The label which corresponds to the given state.
     */
    public static String getBondStateName(int state) {
        return state == BluetoothDevice.BOND_NONE ? "BOND_NONE" :
                state == BluetoothDevice.BOND_BONDED ? "BOND_BONDED" :
                        state == BluetoothDevice.BOND_BONDING ? "BOND_BONDING" : "UNKNOWN";
    }

    /**
     * <p>To get a label for the different known connection states.</p>
     * <p>If the given state is not one of the states known as
     * {@link com.qualcomm.libraries.ble.BLEService.State#CONNECTED CONNECTED},
     * {@link com.qualcomm.libraries.ble.BLEService.State#CONNECTING CONNECTING},
     * {@link com.qualcomm.libraries.ble.BLEService.State#DISCONNECTING DISCONNECTING} and
     * {@link com.qualcomm.libraries.ble.BLEService.State#DISCONNECTED DISCONNECTED}, this
     * method returns "UNKNOWN" as the label.</p>
     *
     * @param state
     *          The state to get a corresponding label for.
     *
     * @return The label which corresponds to the given state.
     */
    public static String getConnectionStateName(@BLEService.State int state) {
        return state == BLEService.State.CONNECTED ? "CONNECTION" :
                state == BLEService.State.CONNECTING ? "CONNECTING" :
                        state == BLEService.State.DISCONNECTING ? "DISCONNECTING" :
                                state == BLEService.State.DISCONNECTED ? "DISCONNECTED" : "UNKNOWN";
    }

    /**
     * <p>This method builds a label which displays the different status errors which can be thrown in the callbacks
     * of {@link android.bluetooth.BluetoothGattCallback BluetoothGattCallback}.</p>
     * <p>These different statuses can be thrown by any layer of the BLE connection established with the device. These
     * layers can also be Android framework related.
     * These status can be:
     * <ul>
     *     <li>{@link com.qualcomm.libraries.ble.ErrorStatus.HCI HCI} or
     *     {@link com.qualcomm.libraries.ble.ErrorStatus.ATT ATT} layers related.</li>
     *     <li>Android {@link com.qualcomm.libraries.ble.ErrorStatus.GattApi GattApi} implementation related as
     *     found in the Android source code.</li>
     *     <li>Android java framework through the {@link BluetoothGatt BluetoothGatt} class.</li>
     * </ul></p>
     * <p>This method builds the label using these methods, respectively:
     * <ul>
     *     <li>{@link com.qualcomm.libraries.ble.ErrorStatus.HCI#getLabel(int, boolean) HCI.getLabel()} and
     *     {@link com.qualcomm.libraries.ble.ErrorStatus.ATT#getLabel(int, boolean) ATT.getLabel()}</li>
     *     <li>{@link com.qualcomm.libraries.ble.ErrorStatus.GattApi#getLabel(int, boolean) GattApi.getLabel()}</li>
     *     <li>{@link com.qualcomm.libraries.ble.ErrorStatus#getBluetoothGattStatusLabel(int, boolean)
     *     ErrorStatus.getBluetoothGattStatusLabel()}</li>
     * </ul></p>
     * <p>If the <code>detailed</code> parameter is true, the built label is as follows:
     * <blockquote><pre>
     *     Error status 0x....:
     *          > BluetoothGatt - FIELD_NAME: corresponding known information.
     *          > ATT - ERROR_NAME: corresponding known information.
     *          > HCI - ERROR_NAME: corresponding known information.
     *          > gatt_api.h - FIELD_NAME: corresponding known information.
     * </pre></blockquote>
     * Otherwise it does not display <code>: corresponding known information.</code>.</p>
     * <p>If a layer does not have a corresponding error status label, the corresponding line is not displayed.</p>
     * <p>If there is no field matching the given status, this method returns:
     * <blockquote><pre>Error status 0x....: UNDEFINED</pre></blockquote>.</p>
     * <p>If the status is <code>0x00</code>, it means "success" and is not an error. For this special case the
     * method returns:
     * <blockquote><pre>Status 0x00: SUCCESS</pre></blockquote>.</p>
     *
     * @param status
     *          The status to get a label for.
     * @param detailed
     *          To get complementary information about the status if found.
     *
     * @return A readable label which shows all the possible known error status.
     */
    public static String getGattStatusName(int status, boolean detailed) {
        String number = BLEUtils.getIntToHexadecimal(status);
        StringBuilder builder = new StringBuilder();

        if (status == 0x00) {
            builder.append("Status ").append(number).append(": SUCCESS");
        }
        else {
            boolean statusFound = false;

            builder.append("Error status ").append(number).append(": ");

            // Android framework: BluetoothGatt Java class
            String bluetoothGattStatus = ErrorStatus.getBluetoothGattStatusLabel(status, detailed);
            if (bluetoothGattStatus.length() > 0) {
                statusFound = true;
                builder.append("\n\t> BluetoothGatt - ").append(bluetoothGattStatus);
            }

            // ATT layer as defined in the Bluetooth Core Specification
            String attStatus = ErrorStatus.ATT.getLabel(status, detailed);
            if (attStatus.length() > 0) {
                statusFound = true;
                builder.append("\n\t> ATT - ").append(attStatus);
            }

            // HCI layer as defined in the Bluetooth Core Specification
            String hciStatus = ErrorStatus.HCI.getLabel(status, detailed);
            if (hciStatus.length() > 0) {
                statusFound = true;
                builder.append("\n\t> HCI - ").append(hciStatus);
            }

            // error status defined in Android framework
            String frameworkStatus = ErrorStatus.GattApi.getLabel(status, detailed);
            if (frameworkStatus.length() > 0) {
                statusFound = true;
                builder.append("\n\t> gatt_api.h - ").append(frameworkStatus);
            }

            // if no status had been found, the status is undefined
            if (!statusFound) {
                builder.append("UNDEFINED");
            }
        }

        return builder.toString();
    }

}