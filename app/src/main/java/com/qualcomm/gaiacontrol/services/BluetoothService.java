/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.services;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;

import com.qualcomm.gaiacontrol.models.gatt.GATT;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.libraries.vmupgrade.UpgradeError;
import com.qualcomm.libraries.vmupgrade.UpgradeManager;
import com.qualcomm.libraries.vmupgrade.UploadProgress;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Defines a generic interface for all Bluetooth Service in order for the activities to only have to interact
 * with this interface and not any specific methods from the different types of BluetoothServices.</p>
 * <p>This interface provides useful methods signatures for any element of the application to be able to communicate
 * with the Bluetooth Service.</p>
 */
@SuppressWarnings("UnusedReturnValue")
public interface BluetoothService {

    // ====== ENUM =================================================================================

    /**
     * <p>All transports implemented in this application which can be used to communicate with a Bluetooth device.</p>
     */
    @IntDef(flag = true, value = { Transport.BLE, Transport.BR_EDR, Transport.UNKNOWN })
    @Retention(RetentionPolicy.SOURCE)
//    @SuppressLint("ShiftFlags") // values are more readable this way
    @interface Transport {
        /**
         * <p>The transport is unknown.</p>
         * <p><i>This one is usually used when the transport is not known yet.</i></p>
         */
        int UNKNOWN = -1;
        /**
         * <p>The Bluetooth Low Energy transport implemented in {@link GATTBLEService GATTBLEService}.</p>
         */
        int BLE = 0;
        /**
         * <p>The BR/EDR transport implemented in {@link GAIABREDRService GAIABREDRService}.</p>
         */
        int BR_EDR = 1;
    }

    /**
     * <p>The possible values for the Bluetooth connection state of this service.</p>
     */
    @IntDef({ State.CONNECTED, State.DISCONNECTED, State.CONNECTING, State.DISCONNECTING })
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
        int DISCONNECTED = 0;
        int CONNECTING = 1;
        int CONNECTED = 2;
        int DISCONNECTING = 3;
    }

    /**
     * <p>All types of messages which can be sent to a handler attached to a BluetoothService.</p>
     */
    @IntDef(flag = true, value = {Messages.CONNECTION_STATE_HAS_CHANGED,
            Messages.DEVICE_BOND_STATE_HAS_CHANGED, Messages.GATT_SUPPORT,
            Messages.GAIA_PACKET, Messages.GAIA_READY,
            Messages.GATT_READY, Messages.GATT_MESSAGE,
            Messages.UPGRADE_MESSAGE })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    @interface Messages {
        /**
         * <p>To inform that the connection state with the selected device has changed.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The connection state of the device as: {@link State#CONNECTED
         *     CONNECTED}, {@link State#CONNECTING CONNECTING},
         *     {@link State#DISCONNECTING DISCONNECTING} or
         *     {@link State#DISCONNECTED DISCONNECTED}. This information is contained
         *     in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int CONNECTION_STATE_HAS_CHANGED = 0;
        /**
         * <p>To inform that the device bond state has changed.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>the bond state value which can be one of the following:
         *     {@link BluetoothDevice#BOND_BONDED BOND_BONDED}, {@link BluetoothDevice#BOND_BONDING BOND_BONDING} and
         *     {@link BluetoothDevice#BOND_NONE BOND_NONE}.<br/>This information is contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int DEVICE_BOND_STATE_HAS_CHANGED = 1;

        /**
         * <p>To inform the listener about GATT Services and Characteristics the remote device supports.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>A {@link GATTServices GATTServices} object.
         *     This information is contained in <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int GATT_SUPPORT = 2;

        /**
         * <p>To inform that the GAIA Service received a potential GAIA packet from the remote device.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>A <code>byte</code> array which contains the potential GAIA packet received from the device.
         *     This information is contained in <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int GAIA_PACKET = 3;

        /**
         * <p>This message is used to let the application know it can now communicate with the device using the GAIA
         * protocol.</p>
         * <p>This message does not contain any other information.</p>
         */
        int GAIA_READY = 4;

        /**
         * <p>This message is used to let the application know it can now communicate with the device over GATT.</p>
         * <p>This message does not contain any other information.</p>
         */
        int GATT_READY = 5;

        /**
         * <p>This message is used to inform the application about GATT service and characteristic operations which
         * are not GAIA related.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The type of GATT message as listed in {@link GATTBLEService.GattMessage GattMessage}.
         *     This information is contained in <code>{@link android.os.Message#arg1 msg.arg1}</code>.</li>
         *     <li>Any complementary information for the GATT message. This information is contained in
         *     <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int GATT_MESSAGE = 6;

        /**
         * <p>This message is used during the process of an upgrade to give updates about the process to an attached
         * handler.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The type of upgrade message as listed in {@link GATTBLEService.UpgradeMessage UpgradeMessage}.
         *     This information is contained in <code>{@link android.os.Message#arg1 msg.arg1}</code>.</li>
         *     <li>Any complementary information for the upgrade message. This information is contained in
         *     <code>{@link android.os.Message#obj msg.obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_MESSAGE = 7;
    }

    /**
     * <p>All types of messages which can be thrown to any attached handler about a processing upgrade through the
     * {@link Messages#UPGRADE_MESSAGE UPGRADE_MESSAGE}.</p>
     * <p>If these messages contain complementary information it is contained in
     * <code>{@link android.os.Message#obj msg.obj}</code>.</li></p>
     */
    @IntDef(flag = true, value = { BluetoothService.UpgradeMessage.UPGRADE_FINISHED,
            BluetoothService.UpgradeMessage.UPGRADE_REQUEST_CONFIRMATION,
            BluetoothService.UpgradeMessage.UPGRADE_STEP_HAS_CHANGED, BluetoothService.UpgradeMessage.UPGRADE_ERROR,
            BluetoothService.UpgradeMessage.UPGRADE_UPLOAD_PROGRESS })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    @interface UpgradeMessage {
        /**
         * <p>To inform that the upgrade process has successfully ended.</p>
         * <p>A message with this type does not contain any complementary information.</p>
         */
        int UPGRADE_FINISHED = 0;
        /**
         * <p>To inform that the upgrade process needs a confirmation from the user to continue the upgrade process.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>{@link UpgradeManager.ConfirmationType ConfirmationType}
         *     information contained in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_REQUEST_CONFIRMATION = 1;
        /**
         * <p>To inform that a new step has been reached during the upgrade process.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>{@link ResumePoints ResumePoints}
         *     information contained in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_STEP_HAS_CHANGED = 2;
        /**
         * <p>To inform that an error occurs during the upgrade process.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>{@link UpgradeError UpgradeError}
         *     information contained in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_ERROR = 3;
        /**
         * <p>To inform on the progress of the file upload.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>{@link UploadProgress UploadProgress}
         *     information contained in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int UPGRADE_UPLOAD_PROGRESS = 4;
    }


    // ====== SERVICE METHODS =======================================================================

    /**
     * <p>Adds the given handler to the targets list for messages from this service.</p>
     * <p>This method must be synchronized to avoid access to the list of listeners while modifying it.</p>
     *
     * @param handler
     *         The Handler for messages.
     */
    void addHandler(Handler handler);

    /**
     * <p>Removes the given handler from the targets list for messages from this service.</p>
     * <p>This method must be synchronized to avoid access to the list of listeners while modifying it.</p>
     *
     * @param handler
     *         The Handler to remove.
     */
    void removeHandler(Handler handler);

    /**
     * To get the type of transport used by the service which implements this interface.
     *
     * @return The corresponding type of transport as one of the following:
     *          {@link com.qualcomm.gaiacontrol.services.BluetoothService.Transport#BLE BLE} or
     *          {@link com.qualcomm.gaiacontrol.services.BluetoothService.Transport#BR_EDR BR/EDR}.
     */
    @SuppressWarnings("unused")
    @Transport int getTransport();


    // ====== CONNECTION METHODS =======================================================================

    /**
     * To disconnect a connected device.
     */
    void disconnectDevice();

    /**
     * <p>To get the bond state of the selected device.</p>
     *
     * @return Any of the bond states used by the {@link BluetoothDevice BluetoothDevice} class:
     *         {@link BluetoothDevice#BOND_BONDED BOND_BONDED}, {@link BluetoothDevice#BOND_BONDING BOND_BONDING} or
     *         {@link BluetoothDevice#BOND_NONE BOND_NONE}. If there is no device defined for this service, this method
     *         returns {@link BluetoothDevice#BOND_NONE BOND_NONE}.
     */
    int getBondState();

    /**
     * <p>To connect to a BluetoothDevice using its Bluetooth address.</p>
     * <p>The connection result is reported asynchronously through the sending of a 
     * {@link Messages#CONNECTION_STATE_HAS_CHANGED CONNECTION_STATE_HAS_CHANGED} message to any handler which
     * has registered through the {@link #addHandler(Handler) addHandler} method.</p>
     *
     * @param address
     *            The Bluetooth address of the device to connect with.
     *
     * @return Return <code>true</code> if the connection is initiated successfully.
     */
    boolean connectToDevice(String address);

    /**
     * <p>To connect to a previously connected BluetoothDevice.</p>
     * <p>The connection result is reported asynchronously through the sending of a 
     * {@link Messages#CONNECTION_STATE_HAS_CHANGED CONNECTION_STATE_HAS_CHANGED} message to any handler which
     * have registered through the {@link #addHandler(Handler) addHandler} method.</p>
     *
     * @return Return <code>true</code> if the connection is initiated successfully.
     */
    boolean reconnectToDevice();

    /**
     * <p>Gets the BluetoothDevice with which this service is connected or has been connected.</p>
     *
     * @return the BluetoothDevice.
     */
    BluetoothDevice getDevice();

    /**
     * <p>Gets the current connection state between this service and a Bluetooth device.</p>
     *
     * @return the connection state.
     */
    @State int getConnectionState();


    // ====== GAIA METHODS ======================================================================

    /**
     * <p>To know if the service is ready to communicate with the remote device using the GAIA
     * protocol.</p>
     *
     * @return True if the service is ready to let the application communicate with the device using the GAIA protocol.
     */
    boolean isGaiaReady();

    /**
     * <p>To send a byte array corresponding to a GAIA packet to a connected BluetoothDevice.</p>
     *
     * @param packet
     *          The bytes to send to a device.
     * @return
     *          true if the sending could be done.
     */
    boolean sendGAIAPacket(byte[] packet);


    // ====== UPGRADE METHODS ======================================================================

    /**
     * <p>To start the Upgrade process with the given file.</p>
     *
     * @param file
     *        The file to use to upgrade the Device.
     */
    void startUpgrade(File file);

    /**
     * <p>To get the current {@link ResumePoints ResumePoints} of the Upgrade process.</p>
     * <p>If there is no ongoing upgrade this information is useless and not accurate.</p>
     *
     * @return The current known resume point of the upgrade.
     */
    @ResumePoints.Enum int getResumePoint();

    /**
     * <p>To abort the upgrade. This method only acts if the Device is connected. If the Device is not connected, there
     * is no ongoing upgrade on the Device side.</p>
     */
    void abortUpgrade();

    /**
     * <p>To know if there is an upgrade going on.</p>
     *
     * @return true if an upgrade is already working, false otherwise.
     */
    boolean isUpgrading();

    /**
     * <p>To inform the Upgrade process about a confirmation it is waiting for.</p>
     *
     * @param type
     *        The type of confirmation the Upgrade process is waiting for.
     * @param confirmation
     *        True if the Upgrade process should continue, false to abort it.
     */
    void sendConfirmation(@UpgradeManager.ConfirmationType int type, boolean confirmation);


    // ====== GATT METHODS ======================================================================

    /**
     * <p>To know if this service is ready to communicate with a remote device.</p>
     * <p>This service is GATT ready if it has initiated a GATT connection with the device.</p>
     *
     * @return True if the service is ready to let the application communicates over GATT.
     */
    boolean isGattReady();

    /**
     * <p>To get the GATT services and characteristics the remote device supports.</p>
     * <p>If the current connection has not be done over GATT, this method returns null.</p>
     *
     * @return the GATTServices object which contains all the support information.
     */
    GATTServices getGattSupport();

    /**
     * <p>To request the LINK LOSS alert level. This can be requested if the current connection is a GATT connection 
     * and if the LINK LOSS Service and its ALERT LEVEL characteristic are supported by the remote device.</p>
     * <p>The value will be sent asynchronously to the listeners attached to this Service using the
     * {@link Messages#GATT_MESSAGE GATT_MESSAGE} with
     * {@link GATTBLEService.GattMessage#LINK_LOSS_ALERT_LEVEL LINK_LOSS_ALERT_LEVEL}.</p>
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean requestLinkLossAlertLevel();

    /**
     * <p>To request the TX POWER level. This can be requested if the current connection is a GATT connection and if the
     * TX POWER Service and its TX POWER LEVEL characteristic are supported by the remote device.</p>
     * <p>The value will be sent asynchronously to the listeners attached to this Service using the
     * {@link Messages#GATT_MESSAGE GATT_MESSAGE} with
     * {@link GATTBLEService.GattMessage#TX_POWER_LEVEL TX_POWER_LEVEL}.</p>
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean requestTxPowerLevel();

    /**
     * <p>To request all the battery levels. This can be requested if the current connection is a GATT connection and
     * if the remote device provides at least one Battery Service with the battery Level characteristic.</p>
     * <p>The value will be sent asynchronously to the listeners attached to this Service using the
     * {@link Messages#GATT_MESSAGE GATT_MESSAGE} with
     * {@link GATTBLEService.GattMessage#BATTERY_LEVEL_UPDATE BATTERY_LEVEL_UPDATE}.
     * </p>
     *
     * @return True if the requests has successfully been added to the queue of requests.
     */
    boolean requestBatteryLevels();

    /**
     * <p>To request the Body Sensor location. This can be requested if the current connection is a GATT connection and
     * if the HEART RATE Service and its TX BODY SENSOR LOCATION characteristic are supported by the remote device.</p>
     * <p>The value will be sent asynchronously to the listeners attached to this Service using the
     * {@link Messages#GATT_MESSAGE GATT_MESSAGE} with
     * {@link GATTBLEService.GattMessage#BODY_SENSOR_LOCATION BODY_SENSOR_LOCATION}.</p>
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean requestBodySensorLocation();

    /**
     * <p>To write the value of the LINK LOSS Alert level. This can be done if the current connection is a GATT
     * connection and if the LINK LOSS Service and its ALERT LEVEL characteristic are supported by the remote
     * device.</p>
     *
     * @param level The level to send to the remote device.
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean sendLinkLossAlertLevel(@IntRange(from= GATT.AlertLevel.Levels.NONE,
            to= GATT.AlertLevel.Levels.HIGH) int level);

    /**
     * <p>To write the value of the IMMEDIATE ALERT level. This can be done if the current connection is a GATT
     * connection and if the IMMEDIATE ALERT Service and its ALERT LEVEL characteristic are supported by the remote
     * device.</p>
     *
     * @param level The level to send to the remote device.
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean sendImmediateAlertLevel(@IntRange(from= GATT.AlertLevel.Levels.NONE,
            to= GATT.AlertLevel.Levels.HIGH) int level);

    /**
     * <p>To write the value of the HEART RATE CONTROL POINT. This can be done if the current connection is a GATT
     * connection and if the HEART RATE Service and its HEART RATE CONTROL POINT characteristic are supported by the
     * remote device.</p>
     *
     * @param control The control to send to the remote device.
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    @SuppressWarnings("SameParameterValue")
    boolean sendHeartRateControlPoint(byte control);

    /**
     * <p>To request to be notified by the remote device of the Heart Rate measurement.
     * This can be requested if the current connection is a GATT connection and if the remote device provides the Heart
     * Rate service with the heart rate measurement characteristic.</p>
     * <p>Any received notification will be sent asynchronously to the listeners attached to this Service using the
     * {@link Messages#GATT_MESSAGE GATT_MESSAGE} with
     * {@link GATTBLEService.GattMessage#HEART_RATE_MEASUREMENT HEART_RATE_MEASUREMENT}.</p>
     *
     * @return True if the request has successfully been added to the queue of requests.
     */
    boolean requestHeartMeasurementNotifications(boolean notify);

    /**
     * <p>To request updates of the RSSI value of the remote device.</p>
     * <p>This method works only if the remote device is connected and BLE only.</p>
     * <p>This method starts the updates if the process is not already started and the device fits the requirements.
     * This method will stop the updates if there is an ongoing process.</p>
     *
     * @param start True to start the updates, false to stop them.
     *
     * @return true if this request could start, false otherwise.
     */
    boolean startRssiUpdates(boolean start);
}
