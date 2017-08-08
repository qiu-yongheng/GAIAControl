/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.services;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.util.Log;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.gaia.UpgradeGaiaManager;
import com.qualcomm.gaiacontrol.models.gatt.GATT;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.gaiacontrol.models.gatt.GattServiceBattery;
import com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate;
import com.qualcomm.gaiacontrol.receivers.BondStateReceiver;
import com.qualcomm.libraries.ble.BLEService;
import com.qualcomm.libraries.ble.BLEUtils;
import com.qualcomm.libraries.ble.Characteristics;
import com.qualcomm.libraries.gaia.GAIA;
import com.qualcomm.libraries.vmupgrade.UpgradeError;
import com.qualcomm.libraries.vmupgrade.UpgradeManager;
import com.qualcomm.libraries.vmupgrade.UploadProgress;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>This {@link android.app.Service Service} allows to manage the Bluetooth Low Energy communication with a device and
 * can work as a Start/Stop service.</p>
 * <p>This BLE service works with the GAIA BLE Service and its characteristics in order to provide the BLE layer
 * which is required to communicate with a device using the GAIA protocol.</p>
 * <p>This Service also implements the upgrade process of a connected GAIA device.</p>
 * <p>This Service also implements some GATT services and profile such as the Proximity Profile for instance.</p>
 */
public class GATTBLEService extends BLEService implements BondStateReceiver.BondStateListener,
        UpgradeGaiaManager.GaiaManagerListener, BluetoothService {

    // ====== CONSTS FIELDS ========================================================================

    /**
     * To know if we are using the application in the debug mode.
     */
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * The time to wait before to process a new read RSSI request.
     */
    private static final int RSSI_WAITING_TIME = Consts.DELAY_TIME_FOR_RSSI;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "GATTBLEService";
    /**
     * <p>The handler to communicate with the app.</p>
     */
    private final List<Handler> mAppListeners = new ArrayList<>();
    /**
     * <p>The binder to return to the instance which will bind this service.</p>
     */
    private final IBinder mBinder = new LocalBinder();
    /**
     * To know the characteristics which have been registered for notifications.
     */
    private final ArrayList<UUID> mNotifiedCharacteristics = new ArrayList<>();
    /**
     * To keep the instance of the bond state receiver to be able to unregister it.
     */
    private final BondStateReceiver mBondStateReceiver = new BondStateReceiver(this);
    /**
     * <p>To know if the GATT connection is ready to be used, ie: services have been discovered and device has been
     * bonded if the GAIA Service is provided by the device.</p>
     */
    private boolean mIsGattReady = false;
    /**
     * <p>To know if the GAIA Service is ready, ie: the device is bonded, all services and characteristics have been
     * discovered and this Service has registered for notifications for GAIA packets.</p>
     */
    private boolean mIsGaiaReady = false;
    /**
     * To manage the GAIA packets which has been received from the device during the process of an upgrade. If there
     * is no upgrade processing, this field is null.
     */
    private UpgradeGaiaManager mUpgradeGaiaManager;
    /**
     * <p>To know the GATT services and characteristics which are supported by the remote device.</p>
     */
    private final GATTServices mGattServices = new GATTServices();
    /**
     * To know if a listener wants to be updated about the device RSSI level.
     */
    private boolean mUpdateRssi = false;
    /**
     * <p>The handler to run some tasks.</p>
     */
    private final Handler mHandler = new Handler();
    /**
     * The Runnable to post delayed in order to request the RSSI level.
     */
    private final Runnable mRssiRunnable = new Runnable() {
        @Override
        public void run() {
            if (mUpdateRssi) {
                requestReadRssi();
            }
        }
    };


    // ====== ENUM =================================================================================

    /**
     * <p>All types of GATT messages which are not GAIA related and can be thrown to any attached handler through
     * the {@link Messages#GATT_MESSAGE GATT_MESSAGE}.</p>
     * <p>If these messages contain complementary information it is contained in
     * <code>{@link android.os.Message#obj msg.obj}</code>.</li></p>
     */
    @IntDef(flag = true, value = { GattMessage.TX_POWER_LEVEL, GattMessage.LINK_LOSS_ALERT_LEVEL,
            GattMessage.RSSI_LEVEL, GattMessage.BATTERY_LEVEL_UPDATE })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface GattMessage {
        /**
         * <p>To inform a TX POWER LEVEL GATT message has been received from the device. This should be thrown once
         * the device had answered to {@link #requestTxPowerLevel() requestTxPowerLevel}.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The <code>int</code> value of the TX POWER level contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int TX_POWER_LEVEL = 0;
        /**
         * <p>To inform a ALERT LEVEL for the LINK LOSS Service GATT message has been received from the device. This
         * should be thrown once the device had answered to
         * {@link #requestLinkLossAlertLevel() requestLinkLossAlertLevel}.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The <code>int</code> value of the ALERT LEVEL contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int LINK_LOSS_ALERT_LEVEL = 1;
        /**
         * <p>To inform the RSSI level has been received from the device or the system. This
         * should be thrown once the device has answered to {@link #requestReadRssi() requestReadRssi}.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The <code>int</code> RSSI value contained in <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int RSSI_LEVEL = 2;
        /**
         * <p>To inform that a BATTERY LEVEL message for the GATT BATTERY Service has been received from the device.
         * This should be thrown when the device answers to the requests made from the
         * {@link #requestBatteryLevels() requestBatteryLevels} method.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The key of the BATTERY Service in the
         *     {@link GATTServices#gattServiceBatteries gattServiceBatteries} Map. This key is contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int BATTERY_LEVEL_UPDATE = 3;
        /**
         * <p>To inform that a HEART RATE MEASUREMENT message from the HEART RATE Service has been received from the
         * device.
         * This should be thrown when the device notifies the Android device after
         * {@link #requestHeartMeasurementNotifications(boolean) requestHeartMeasurementNotifications(true)} call has
         * been made.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The values corresponding to the characteristic contains in a
         *     {@link com.qualcomm.gaiacontrol.models.gatt.GattServiceHeartRate.HeartRateMeasurementValues
         *     HeartRateMeasurementValues} object. This object is contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int HEART_RATE_MEASUREMENT = 4;
        /**
         * <p>To inform that a BODY SENSOR LOCATION message from the HEART RATE Service has been received from the
         * device.
         * This should be thrown when the device answers to the requests made from the
         * {@link #requestBodySensorLocation() requestBodySensorLocation} method.</p>
         * <p>This type of {@link android.os.Message Message} also contains:</p>
         * <ul>
         *     <li>The <code>int</code> location value contained in
         *     <code>{@link android.os.Message#obj obj}</code>.</li>
         * </ul>
         */
        int BODY_SENSOR_LOCATION = 5;
    }


    // ====== PUBLIC METHODS =======================================================================

    @Override // BluetoothService
    public synchronized void addHandler(Handler handler) {
        if (!mAppListeners.contains(handler)) {
            this.mAppListeners.add(handler);
        }
    }

    @Override // BluetoothService
    public synchronized void removeHandler(Handler handler) {
        if (mAppListeners.contains(handler)) {
            this.mAppListeners.remove(handler);
        }
    }

    @Override // BluetoothService
    public void disconnectDevice() {
        if (super.getConnectionState() == BLEService.State.DISCONNECTED) {
            resetDeviceInformation();
        }
        else {
            unregisterNotifications();
            disconnectFromDevice();
        }
    }

    @Override // BluetoothService
    public int getBondState() {
        BluetoothDevice device = getDevice();
        return device != null ? device.getBondState() : BluetoothDevice.BOND_NONE;
    }

    @Override // BluetoothService
    public @Transport int getTransport() {
        return Transport.BLE;
    }

    @Override // BluetoothService
    public GATTServices getGattSupport() {
        return mGattServices;
    }

    @Override // BluetoothService
    public boolean sendGAIAPacket(byte[] packet) {
        return sendGaiaCommandEndpoint(packet);
    }

    /**
     * <p>This method returns true if all of the following are true:
     * <ul>
     *     <li>Device is connected</li>
     *     <li>{@link android.bluetooth.BluetoothGattCallback#onServicesDiscovered(BluetoothGatt, int)
     *     onServicesDiscovered} had successfully been called.</li>
     *     <li>The device is bonded over BLE.</li>
     *     <li>The service had successfully registered for characteristic notifications - meaning the
     *     {@link android.bluetooth.BluetoothGattCallback#onDescriptorWrite(BluetoothGatt, BluetoothGattDescriptor, int)
     *     onDescriptorWrite} has successfully been called.</li>
     * </ul></p>
     */
    @Override // BluetoothService
    public boolean isGaiaReady() {
        return mIsGaiaReady;
    }

    /**
     * <p>This service is GATT ready if:
     * <ul>
     *     <li>The BluetoothDevice has been connected over GATT.</li>
     *     <li>The discovery of the GATT services and characteristics has been done.</li>
     *     <li>If the GAIA Service is supported, its method will return false until the remote device will be
     * bonded over BLE</li>
     * </ul></p>
     */
    @Override // BluetoothService
    public boolean isGattReady() {
        return mIsGattReady;
    }


    // ====== UPGRADE METHODS ======================================================================

    @Override // BluetoothService
    public void startUpgrade(File file) {
        mUpgradeGaiaManager = new UpgradeGaiaManager(this, GAIA.Transport.BLE);
        mUpgradeGaiaManager.startUpgrade(file);
    }

    @Override // BluetoothService
    public @ResumePoints.Enum int getResumePoint() {
        return (mUpgradeGaiaManager!= null) ? mUpgradeGaiaManager.getResumePoint() : ResumePoints.Enum.DATA_TRANSFER;
    }

    @Override // BluetoothService
    public void abortUpgrade() {
        if (super.getConnectionState() == BLEService.State.CONNECTED && mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.abortUpgrade();
        }
    }

    @Override // BluetoothService
    public boolean isUpgrading() {
        return mUpgradeGaiaManager != null && mUpgradeGaiaManager.isUpgrading();
    }

    @Override // BluetoothService
    public void sendConfirmation(@UpgradeManager.ConfirmationType int type, boolean confirmation) {
        if (mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.sendConfirmation(type, confirmation);
        }
    }


    // ====== GATT REQUESTS ======================================================================

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean requestLinkLossAlertLevel() {
        return mGattServices.gattServiceLinkLoss.isSupported()
                && requestReadCharacteristic(mGattServices.gattServiceLinkLoss.getAlertLevelCharacteristic());
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean requestTxPowerLevel() {
        return mGattServices.gattServicetxPower.isSupported()
                && requestReadCharacteristic(mGattServices.gattServicetxPower.getTxPowerLevelCharacteristic());
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean requestBatteryLevels() {
        boolean done = true;
        if (mGattServices.isBatteryServiceSupported()) {
            for (int i=0; i<mGattServices.gattServiceBatteries.size(); i++) {
                GattServiceBattery service = mGattServices.gattServiceBatteries.get(
                        mGattServices.gattServiceBatteries.keyAt(i));
                if (!requestReadCharacteristic(service.getBatteryLevelCharacteristic())) {
                    done = false;
                }
            }
        }
        else {
            done = false;
        }

        return done;
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean requestBodySensorLocation() {
        return mGattServices.gattServiceHeartRate.isBodySensorLocationCharacteristicAvailable()
                && requestReadCharacteristic(mGattServices.gattServiceHeartRate.getBodySensorLocationCharacteristic());
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean sendLinkLossAlertLevel(@IntRange(from= GATT.AlertLevel.Levels.NONE, to= GATT.AlertLevel.Levels.HIGH) int level) {
        if (mGattServices.gattServiceLinkLoss.isSupported()) {
            byte[] bytes = new byte[GATT.AlertLevel.DATA_LENGTH_IN_BYTES];
            bytes[GATT.AlertLevel.LEVEL_BYTE_OFFSET] = (byte) level;
            return requestWriteCharacteristic(mGattServices.gattServiceLinkLoss.getAlertLevelCharacteristic(), bytes);
        }
        return false;
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean sendImmediateAlertLevel(@IntRange(from= GATT.AlertLevel.Levels.NONE, to= GATT.AlertLevel.Levels.HIGH)
                                                   int level) {
        if (mGattServices.gattServiceimmediateAlert.isSupported()) {
            byte[] bytes = new byte[GATT.AlertLevel.DATA_LENGTH_IN_BYTES];
            bytes[GATT.AlertLevel.LEVEL_BYTE_OFFSET] = (byte) level;
            return requestWriteNoResponseCharacteristic(mGattServices.gattServiceimmediateAlert
                    .getAlertLevelCharacteristic(), bytes);
        }
        else {
            return false;
        }
    }

    @Override // BluetoothService
    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    public boolean sendHeartRateControlPoint(byte control) {
        if (mGattServices.gattServiceHeartRate.isHeartRateControlPointCharacteristicAvailable()) {
            byte[] bytes = new byte[GATT.HeartRateControlPoint.CONTROL_LENGTH_IN_BYTES];
            bytes[GATT.HeartRateControlPoint.CONTROL_BYTE_OFFSET] = control;
            return requestWriteCharacteristic(mGattServices.gattServiceHeartRate
                    .getHeartRateControlPointCharacteristic(), bytes);
        }
        else {
            return false;
        }
    }

    @Override // BluetoothService
    @SuppressWarnings("UnusedReturnValue")
    public boolean requestHeartMeasurementNotifications(boolean notify) {
        boolean done = false;
        if (mGattServices.gattServiceHeartRate.isHeartRateMeasurementCharacteristicAvailable()
                && mGattServices.gattServiceHeartRate.isClientCharacteristicConfigurationDescriptorAvailable()) {
            done = requestCharacteristicNotification(mGattServices.gattServiceHeartRate
                    .getHeartRateMeasurementCharacteristic(), notify);
        }
        return done;
    }

    @Override // BluetoothService
    public boolean startRssiUpdates(boolean start) {
        if (start && !mUpdateRssi) {
            mUpdateRssi = requestReadRssi();
        }
        else if (!start && mUpdateRssi) {
            mUpdateRssi = false;
            mHandler.removeCallbacks(mRssiRunnable);
        }
        return mUpdateRssi;
    }


    // ====== ANDROID SERVICE ======================================================================

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "Service bound");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.i(TAG, "Service unbound");
        if (mAppListeners.isEmpty()) {
            disconnectDevice();
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.showDebugLogs(false);
        this.initialize();
        this.setDelayForRequest(60000); // with ADK there can be a long delay after pairing, observed time: 32s
        registerBondReceiver();
    }

    /*
     * The system calls this method when the service is no longer used and is being destroyed. Your service should
     * implement this to clean up any resources such as threads, registered listeners, receivers, etc. This is the last
     * call the service receives.
     */
    @Override
    public void onDestroy() {
        disconnectDevice();
        unregisterBondReceiver();
        if (DEBUG) Log.i(TAG, "Service destroyed");
        super.onDestroy();
    }


    // ====== UPGRADE GAIA MANAGER LISTENER METHODS =============================================

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onVMUpgradeDisconnected() {
        if (!isUpgrading()) {
            mUpgradeGaiaManager = null;
        }
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onResumePointChanged(@ResumePoints.Enum int point) {
        sendMessageToListener(Messages.UPGRADE_MESSAGE, UpgradeMessage.UPGRADE_STEP_HAS_CHANGED, point);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUpgradeError(UpgradeError error) {
        Log.e(TAG, "ERROR during upgrade: " + error.getString());
        sendMessageToListener(Messages.UPGRADE_MESSAGE, UpgradeMessage.UPGRADE_ERROR, error);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUploadProgress(UploadProgress progress) {
        sendMessageToListener(Messages.UPGRADE_MESSAGE, UpgradeMessage.UPGRADE_UPLOAD_PROGRESS, progress);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public boolean sendGAIAUpgradePacket(byte[] packet) {
        return sendGaiaCommandEndpoint(packet);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void onUpgradeFinish() {
        sendMessageToListener(Messages.UPGRADE_MESSAGE, UpgradeMessage.UPGRADE_FINISHED, null);
    }

    @Override // UpgradeGaiaManager.GaiaManagerListener
    public void askConfirmation(@UpgradeManager.ConfirmationType int type) {
        if (!sendMessageToListener(Messages.UPGRADE_MESSAGE, UpgradeMessage.UPGRADE_REQUEST_CONFIRMATION, type)) {
            // default behaviour? use a notification?
            sendConfirmation(type, true);
        }
    }


    // ====== BLUETOOTH SERVICE INTERFACE METHODS ===============================================================

    @Override // BLEService, BluetoothService
    public BluetoothDevice getDevice() {
        return super.getDevice();
    }

    @Override // BLEService, BluetoothService
    public @BluetoothService.State int getConnectionState() {
        switch(super.getConnectionState()) {
            case BLEService.State.CONNECTED:
                return BluetoothService.State.CONNECTED;
            case BLEService.State.CONNECTING:
                return BluetoothService.State.CONNECTING;
            case BLEService.State.DISCONNECTING:
                return BluetoothService.State.DISCONNECTING;
            case BLEService.State.DISCONNECTED:
            default:
                return BluetoothService.State.DISCONNECTED;
        }
    }

    @Override // BLEService, BluetoothService
    public boolean connectToDevice(String address) {
        return super.connectToDevice(address);
        // then wait for onConnectionStateChange in order to communicate over GATT with the device
    }

    @Override // BondStateReceiver.BondStateListener
    public void onBondStateChange(BluetoothDevice device, int state) {
        // we expect this method to be called when this service attempted to induce pairing when pairing is required
        // The BLEService requests pairing automatically (or the system) requestReadCharacteristicForPairing if needed
        BluetoothDevice connectedDevice = getDevice();
        if (device != null && connectedDevice != null && device.getAddress().equals(connectedDevice.getAddress())) {
            if (DEBUG) Log.i(TAG, "ACTION_BOND_STATE_CHANGED for " + device.getAddress()
                    + " with bond state " + BLEUtils.getBondStateName(state));

            sendMessageToListener(Messages.DEVICE_BOND_STATE_HAS_CHANGED, state);

            // once device is bonded
            if (state == BluetoothDevice.BOND_BONDED) {
                // if GATT required pairing to be used, it is now ready
                onGattReady();
            }
        }
    }


    // ====== ACTIVITY COMMUNICATION ===============================================================

    /**
     * <p>To inform the listener by sending a message to it.</p>
     *
     * @param message
     *         The type of message to send.
     */
    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"}) // the return value is used for some implementations
    private boolean sendMessageToListener(@Messages int message) {
        if (!mAppListeners.isEmpty()) {
            for (int i=0; i<mAppListeners.size(); i++) {
                mAppListeners.get(i).obtainMessage(message).sendToTarget();
            }
        }
        return !mAppListeners.isEmpty();
    }

    /**
     * <p>To inform the listener by sending a message to it.</p>
     *
     * @param message
     *         The type of message to send.
     * @param object
     *         Any complementary information to send to the listener.
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean sendMessageToListener(@Messages int message, Object object) {
        if (!mAppListeners.isEmpty()) {
            for (int i=0; i<mAppListeners.size(); i++) {
                mAppListeners.get(i).obtainMessage(message, object).sendToTarget();
            }
        }
        return !mAppListeners.isEmpty();
    }

    /**
     * <p>To inform the listener by sending it a message.</p>
     *
     * @param message
     *         The type of message to send.
     * @param subMessage
     *          Any complementary message to the message type.
     * @param object
     *         Any complementary object to send to the listener.
     */
    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private boolean sendMessageToListener(@Messages int message, int subMessage, Object object) {
        if (!mAppListeners.isEmpty()) {
            for (int i=0; i<mAppListeners.size(); i++) {
                mAppListeners.get(i).obtainMessage(message, subMessage, 0, object).sendToTarget();
            }
        }
        return !mAppListeners.isEmpty();
    }


    // ====== BLE SERVICE ==========================================================================


    @Override // extends BLEService
    protected synchronized void setState(int newState) {
        super.setState(newState);
        @BluetoothService.State int state = newState == BLEService.State.CONNECTED ? BluetoothService.State.CONNECTED :
                newState == BLEService.State.CONNECTING ? BluetoothService.State.CONNECTING :
                newState == BLEService.State.DISCONNECTING ? BluetoothService.State.DISCONNECTING :
                        BluetoothService.State.DISCONNECTED;
        sendMessageToListener(Messages.CONNECTION_STATE_HAS_CHANGED, state);
    }

    @Override // extends BLEService
    protected void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (DEBUG) Log.i(TAG, "onConnectionStateChange: " + BLEUtils.getGattStatusName(status, true));
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "Attempting to start service discovery: " + gatt.discoverServices());
            // now wait for onServicesDiscovered to be called in order to communicate over GATT with the device
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            resetDeviceInformation();
            if (isUpgrading()) {
                // delays the reconnection about 1s for ADK6.0 implementation
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reconnectToDevice();
                    }
                }, 1000);
            }
        }
    }

    @Override // extends BLEService
    protected void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // device BLE Services & Characteristics are ready to be used
            // we check if the needed BLE services and their characteristics used are available
            mGattServices.setSupportedGattServices(gatt.getServices());
            sendMessageToListener(Messages.GATT_SUPPORT, mGattServices);

            // As the used devices are DUAL MODE, we cannot detect if the device is already bonded through BLE.
            // For instance, if the device had been bonded over Bluetooth classic, device.getBondState returns "BONDED".
            // However if the device has not been bonded over BLE yet, it needs to be prior to using some characteristics.
            // So we read a characteristic in order to induce pairing for BLE.
            // By definition, if pairing is required the GAIA DATA characteristic requires encryption for the READ
            // property.
            if (mGattServices.gattServiceGaia.isSupported()) {
                requestReadCharacteristicForPairing(mGattServices.gattServiceGaia.getGaiaDataCharacteristic());

                // then wait for one of these events to happen in order to communicate over GATT with the device:
                //  - onCharacteristicRead to be called
                //  - onBondStateChange to be called
            }
            else {
                // through the GATT_SUPPORT message the listener already knows GAIA is not supported.
                // /!\ pairing is not induced if GAIA Service is not supported.
                onGattReady();
            }

            if (DEBUG) Log.i(TAG, mGattServices.toString());
        }
    }

    @Override // extends BLEService
    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null) {
            UUID uuid = characteristic.getUuid();

            if (!mIsGattReady && uuid.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_DATA_UUID)) {
                // This is reached when pairing induction is attempted with a READ request over the GAIA DATA
                // CHARACTERISTIC.
                // If the READ operation is successful, the bonding is already done and the application can process.
                // Otherwise, BLEService will request the pairing if the system did not do it automatically - only if
                // BOND state is NONE.

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // GATT is ready and can be used
                    if (DEBUG) Log.i(TAG, "Successful read characteristic to induce pairing: no need to bond device.");
                    onGattReady();
                }
            }
            else if (status == BluetoothGatt.GATT_SUCCESS && uuid.equals(GATT.UUIDs.CHARACTERISTIC_ALERT_LEVEL_UUID)
                    && characteristic.getService().getUuid().equals(GATT.UUIDs.SERVICE_LINK_LOSS_UUID)) {
                // asynchronous answer to requestLinkLossAlertLevel()
                int value = characteristic.getIntValue(GATT.AlertLevel.LEVEL_FORMAT, GATT.AlertLevel.LEVEL_BYTE_OFFSET);
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.LINK_LOSS_ALERT_LEVEL, value);
            }
            else if (status == BluetoothGatt.GATT_SUCCESS
                    && uuid.equals(GATT.UUIDs.CHARACTERISTIC_TX_POWER_LEVEL_UUID)) {
                // asynchronous answer to requestTxPowerLevel()
                int value = characteristic.getIntValue(GATT.TxPowerLevel.LEVEL_FORMAT,
                                                        GATT.TxPowerLevel.LEVEL_BYTE_OFFSET);
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.TX_POWER_LEVEL, value);
            }
            else if (status == BluetoothGatt.GATT_SUCCESS
                    && uuid.equals(GATT.UUIDs.CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
                // one of the asynchronous answers to requestBatteryLevels()
                int instance = characteristic.getService().getInstanceId();
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.BATTERY_LEVEL_UPDATE, instance);
            }
            else if (status == BluetoothGatt.GATT_SUCCESS
                    && uuid.equals(GATT.UUIDs.CHARACTERISTIC_BODY_SENSOR_LOCATION_UUID)) {
                // asynchronous answer to requestBodySensorLocation()
                int value = characteristic.getValue()[GATT.BodySensorLocation.LOCATION_BYTE_OFFSET];
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.BODY_SENSOR_LOCATION, value);
            }
        }
    }

    @Override // extends BLEService
    protected void onReceivedCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            UUID uuid = characteristic.getUuid();
            if (uuid.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_RESPONSE_UUID)) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    if (mUpgradeGaiaManager != null) {
                        mUpgradeGaiaManager.onReceiveGAIAPacket((data));
                    } else {
                        sendMessageToListener(Messages.GAIA_PACKET, data);
                    }
                }
            }
            else if (uuid.equals(GATT.UUIDs.CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID)) {
                GattServiceHeartRate.HeartRateMeasurementValues values = mGattServices.gattServiceHeartRate
                        .getHeartRateMeasurementValues();
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.HEART_RATE_MEASUREMENT, values);
            }
            else {
                if (DEBUG) Log.i(TAG, "Received notification over characteristic: " + characteristic.getUuid());
            }
        }
    }

    @Override // extends BLEService
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        UUID descriptorUuid = descriptor.getUuid();
        UUID characteristicUuid = descriptor.getCharacteristic().getUuid();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            mNotifiedCharacteristics.add(characteristicUuid);

            if (DEBUG) Log.i(TAG, "Successful write descriptor " + descriptorUuid.toString()
                    + " for characteristic " + characteristicUuid.toString());

            // if the registered descriptor was for GAIA notifications from device, GAIA is ready to be used
            if (mGattServices.gattServiceGaia.isSupported()
                    && descriptorUuid.equals(GATT.UUIDs.DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION_UUID)
                    && characteristicUuid.equals(GATT.UUIDs.CHARACTERISTIC_GAIA_RESPONSE_UUID)) {
                // this service will now receives any notification for the GAIA packets
                mIsGaiaReady = true;
                sendMessageToListener(Messages.GAIA_READY);

                // if an upgrade is processing it should go on
                if (isUpgrading()) {
                    mUpgradeGaiaManager.onGaiaReady();
                }
            } else if (mGattServices.gattServiceHeartRate.isSupported()
                    && characteristicUuid.equals(GATT.UUIDs.CHARACTERISTIC_HEART_RATE_MEASUREMENT_UUID)) {
                if (DEBUG) Log.d(TAG, "Received successful onDescriptorWrite for Heart Rate Measurement");
            }
        }
        else {
            Log.w(TAG, "Unsuccessful write descriptor " + descriptorUuid.toString()
                    + " for characteristic " + characteristicUuid.toString() + " with status " + BLEUtils
                    .getGattStatusName(status, false));
        }
    }

    @Override // extends BLEService
    protected void onRemoteRssiRead(BluetoothGatt gatt, int rssi, int status) {
        // This callback is only called if the device is BLE only
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (mUpdateRssi) {
                sendMessageToListener(Messages.GATT_MESSAGE, GattMessage.RSSI_LEVEL, rssi);
                mHandler.postDelayed(mRssiRunnable, RSSI_WAITING_TIME);
            }
        }
    }

    @Override // extends BLEService
    protected void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS && descriptor.getCharacteristic().getUuid().equals(GATT.UUIDs
                .CHARACTERISTIC_BATTERY_LEVEL_UUID)) {
            int instance = descriptor.getCharacteristic().getService().getInstanceId();
            GattServiceBattery service = mGattServices.gattServiceBatteries.get(instance);
            if (service != null) {
                service.updateDescription();
            }
        }
    }

    @Override // extends BLEService
    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override // extends BLEService, BluetoothService
    public boolean reconnectToDevice() {
        return super.reconnectToDevice();
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>To reset the values related to the device when it is disconnected or disconnecting.</p>
     */
    private void resetDeviceInformation() {
        mIsGattReady = false;
        mIsGaiaReady = false;
        if (mUpgradeGaiaManager != null) {
            mUpgradeGaiaManager.reset();
        }
//        mGattServices.reset();
    }

    /**
     * <p>To register the bond state receiver in order to be informed of any bond state change.</p>
     */
    private void registerBondReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        this.registerReceiver(mBondStateReceiver, filter);
    }

    /**
     * <p>To unregister the bond state receiver when the application is stopped or it is not necessary anymore.</p>
     */
    private void unregisterBondReceiver() {
        unregisterReceiver(mBondStateReceiver);
    }

    /**
     * <p>To unregister from all characteristic notifications this service has registered.</p>
     */
    private void unregisterNotifications() {
        for (int i = 0; i < mNotifiedCharacteristics.size(); i++) {
            requestCharacteristicNotification(mNotifiedCharacteristics.get(i), false);
        }
    }

    /**
     * <p>This method is called when this service considers that the GATT connection is ready to be used by this
     * service or any listener which would like to make some requests.</p>
     * <p>This method informs any attached listener that the the GATT connection is ready.</p>
     * <p>This method also sends the following requests:
     * <ul>
     *     <li>If the GAIA Service is supported: it requests to register for the GAIA RESPONSE characteristic
     *     notifications.</li>
     *     <li>If there is at least one BATTERY Service: it does a read descriptor request for each PRESENTATION
     *     FORMAT descriptor.</li>
     * </ul></p>
     */
    private void onGattReady() {
        mIsGattReady = true;

        if (DEBUG) {
            Log.i(TAG, "GATT connection is ready to be used.");
        }

        sendMessageToListener(Messages.GATT_READY);

        // if the device supports GAIA there are a few more steps
        if (mGattServices.gattServiceGaia.isSupported()) {
            if (DEBUG) {
                Log.i(TAG, "GAIA is supported, start request for GAIA notifications.");
            }
            requestCharacteristicNotification(mGattServices.gattServiceGaia.getGaiaResponseCharacteristic(), true);
            // then wait for onDescriptorWrite in order to use GAIA
        }

        // if there is at least one battery service we request the descriptor value if descriptor available
        if (mGattServices.isBatteryServiceSupported()) {
            for (int i=0; i<mGattServices.gattServiceBatteries.size(); i++) {
                if (DEBUG) {
                    Log.i(TAG, "Battery service is supported, request presentation format descriptors for service " +
                            (i+1) + ".");
                }
                GattServiceBattery service = mGattServices.gattServiceBatteries.get(
                        mGattServices.gattServiceBatteries.keyAt(i));
                if (service.isPresentationFormatDescriptorAvailable()) {
                    requestReadDescriptor(service.getPresentationFormatDescriptor());
                }
            }
        }
    }

    /**
     * <p>To write some data over the
     * {@link Characteristics#CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT}
     * characteristic.</p>
     *
     * @param data
     *          The byte array to send to a device.
     *
     * @return true if the sending could be done.
     */
    private boolean sendGaiaCommandEndpoint(byte[] data) {
        if (mGattServices.gattServiceGaia.isCharacteristicGaiaCommandAvailable()) {
            return requestWriteCharacteristic(mGattServices.gattServiceGaia.getGaiaCommandCharacteristic(), data);
        }
        else {
            Log.w(TAG, "Attempt to send data over CHARACTERISTIC_CSR_GAIA_COMMAND_ENDPOINT failed: characteristic not" +
                    " available.");
            return false;
        }
    }


    // ====== INNER CLASS ==========================================================================

    /**
     * <p>The class which allows an entity to communicate with this service when its bind.</p>
     */
    public class LocalBinder extends Binder {
        /**
         * <p>To retrieve the binder service.</p>
         *
         * @return the service.
         */
        public GATTBLEService getService() {
            return GATTBLEService.this;
        }
    }

}
