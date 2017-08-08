/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * <p>This service allows management of the connection and data communication with a GATT server hosted on a Bluetooth LE
 * device.</p>
 * <p>This service will manage the BluetoothGatt object and its BluetoothGattCallback.</p>
 * <p>In order to use this service, here is some useful information:
 * <ol>
 *     <li>Call the initialize method in order to initialize the BluetoothAdapter allowing this service to know if it
 *     can use Bluetooth.</li>
 *     <li>This class does not call the service discovery as the good moment to call this one can be implementation
 *     dependant. However it is recommended to do it once the device is connected.</li>
 *     <li>This service manages a Queue of requests in order to have only one request (read, write) running at a
 *     time. This avoids overloading the Android Bluetooth stack as the callbacks are asynchronous.</li>
 *     <li>To ask for a request to be done this service provides methods for this purpose.</li>
 *     <li>The subclass must implement all the abstract callback methods from the BluetoothGattCallBack as the
 *     following actions are implementation dependant.</li>
 *     <li>It is possible to display more logs by activating the debug logs with the method
 *     {@link #showDebugLogs(boolean) showDebugLogs}.</li>
 *     <li>It is possible to modify the delay used to know a request as being timed out by using the method
 *     {@link #setDelayForRequest(int) setDelayForRequest}.</li>
 * </ol></p>
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class BLEService extends Service {

    // ====== CONSTS ===============================================================================
    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("WeakerAccess")
    private final String TAG = "BLEService";
    /**
     * <p>The number of attempt for a Bluetooth request.</p>
     */
    private static final int REQUEST_MAX_ATTEMPTS = 2;
    /**
     * <p>The default time for a request time out.</p>
     */
    private static final int DEFAULT_DELAY_FOR_REQUEST = 60000;
    /**
     * <p>The time for a notification request time out.</p>
     */
    private static final int DEFAULT_DELAY_FOR_NOTIFICATION_REQUEST = 1000;


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The Bluetooth adapter to manage the Bluetooth connection.</p>
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * <p>The Bluetooth gatt to communicate with a connected device.</p>
     */
    private BluetoothGatt mBluetoothGatt;
    /**
     * <p>The device to which this service is connected.</p>
     */
    private BluetoothDevice mDevice;
    /**
     * <p>The connection state of this service with a Bluetooth device.</p>
     */
    private @State int mConnectionState = State.DISCONNECTED;
    /**
     * <p>The queue of pending transmissions</p>
     */
    private final Queue<Request> mRequestsQueue = new LinkedList<>();
    /**
     * <p>To know if the queue is processing at the moment.</p>
     */
    private boolean isQueueProcessing = false;
    /**
     * <p>The main handler to run some tasks.</p>>
     */
    private final Handler mHandler = new Handler();
    /**
     * <p>A runnable used to time out the requests.</p>
     */
    private TimeOutRequestRunnable mTimeOutRequestRunnable = null;
    /**
     * <p>All characteristics available during a connection with a device.</p>
     * <p><i>Restriction: this is irrelevant if different Services provide a characteristic with the same UUID.</i></p>
     */
    private final ArrayMap<UUID, BluetoothGattCharacteristic> mCharacteristics = new ArrayMap<>();
    /**
     * <p>To show the debug logs indicating when a method had been reached.</p>
     */
    private boolean mShowDebugLogs = false;
    /**
     * <p>The time to wait for the TimeOutRequestRunnable to start.</p>
     */
    private int mDelay = DEFAULT_DELAY_FOR_REQUEST;

    /**
     * <p>The call back used when connected to a GATT server.</p>
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            receiveConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            receiveServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            receiveCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            onReceivedCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            receiveDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            receiveCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            receiveDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            receiveRemoteRssiRead(gatt, rssi, status);
        }
    };


    // ====== ENUM =================================================================================

    /**
     * <p>The possible values for the Bluetooth connection state of this service.</p>
     */
    @IntDef({ State.CONNECTED, State.DISCONNECTED, State.CONNECTING, State.DISCONNECTING })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int DISCONNECTED = 0;
        int CONNECTING = 1;
        int CONNECTED = 2;
        int DISCONNECTING = 3;
    }


    // ====== PROTECTED METHODS =======================================================================

    /**
      * <p>To allow the display of the debug logs.</p>
      * <p>They give complementary information on any call of a method.
      * They can indicate that a method is reached but also the action the method does.</p>
      *
      * @param show True to show the debug logs, false otherwise.
      */
    protected void showDebugLogs(boolean show) {
        mShowDebugLogs = show;
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
    }

    /**
     * <p>In order to avoid blocking the service interactions if the system didn't give any callback to this service.
     * For a request known as processing, each request is timed out. This method sets the delay, in ms, to use to time
     * out the requests.</p>
     * <p>The default delay time is {@link #DEFAULT_DELAY_FOR_REQUEST DEFAULT_DELAY_FOR_REQUEST}</p>
     *
     * @param delay
     *          The new delay time to use.
     */
    protected synchronized void setDelayForRequest(int delay) {
        mDelay = delay;
    }

    /**
     * <p>To request to register or unregister a notification for the given characteristic UUID if this Android service is
     * connected to a Bluetooth device.</p>
     *
     * @param characteristicUUID
     *              the specified characteristic UUID.
     * @param register
     *              true to register, false to unregister.
     *
     * @return <p>false if it is not possible to request a characteristic notification. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "notify" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestCharacteristicNotification(UUID characteristicUUID, boolean register) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for notification on characteristic with UUID "
                    + characteristicUUID.toString() + " for " + (register ? "activation" : "deactivation"));
        }
        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request characteristic notification not initiated: device is disconnected.");
            return false;
        }

        BluetoothGattCharacteristic characteristic = mCharacteristics.get(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "request characteristic notification not initiated: characteristic not found for UUID " +
                    characteristicUUID + ".");
            return false;
        }

        // all check passed successfully, the request can be initiated
        return requestCharacteristicNotification(characteristic, register);
    }

    /**
     * <p>To request to register or unregister a notification for the given characteristic if this Android service is
     * connected to a Bluetooth device.</p>
     *
     * @param characteristic
     *              the specified characteristic.
     * @param register
     *              true to register, false to unregister.
     *
     * @return <p>false if it is not possible to request a characteristic notification. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "notify" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean register) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for notification on characteristic with UUID "
                    + characteristic.getUuid().toString() + " for " + (register ? "activation" : "deactivation"));
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request characteristic notification not initiated: device is disconnected.");
            return false;
        }

        if (characteristic == null) {
            Log.w(TAG, "request characteristic notification not initiated: characteristic is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(characteristic.getUuid())) {
            Log.w(TAG, "request characteristic notification not initiated: unknown characteristic UUID.");
            return false;
        }

        /*
        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) <= 0) {
                Log.w(TAG, "request characteristic notification not initiated: characteristic does not have the " +
                        "NOTIFY property.");
            return false;
        }
        */ // workaround: property not part of the Service description even if notification are available

        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(Characteristics.CLIENT_CHARACTERISTIC_CONFIG);

        if (descriptor == null) {
            Log.w(TAG, "request characteristic notification not initiated: no CLIENT_CHARACTERISTIC_CONFIGURATION" +
                    " descriptor.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request requestNotification = Request.createCharacteristicNotificationRequest(characteristic,
                register);
        byte[] data = register ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

        addToRequestsQueue(requestNotification);

        Request requestDescriptor = Request.createWriteDescriptorRequest(descriptor, data);
        addToRequestsQueue(requestDescriptor);
        return true;
    }

    /**
     * <p>To write some characteristic data for the given characteristic UUID if this Android service is connected to a Bluetooth device.</p>
     *
     * @param characteristicUUID
     *              the specified characteristic.
     * @param data
     *              the data to write.
     *
     * @return <p>false if it is not possible to request a characteristic write. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "write" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestWriteCharacteristic(UUID characteristicUUID, final byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for write on characteristic with UUID " + characteristicUUID.toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request write characteristic not initiated: device is disconnected.");
            return false;
        }

        BluetoothGattCharacteristic characteristic = mCharacteristics.get(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "request write characteristic not initiated: characteristic not found for UUID " +
                    characteristicUUID + ".");
            return false;
        }

        // all check passed successfully, the request can be initiated
        return requestWriteCharacteristic(characteristic, data);
    }

    /**
     * <p>To write some characteristic data for the given characteristic if this Android service is connected to a Bluetooth device.</p>
     *
     * @param characteristic
     *              the specified characteristic.
     * @param data
     *              the data to write.
     *
     * @return <p>false if it is not possible to request a characteristic write. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "write" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestWriteCharacteristic(BluetoothGattCharacteristic characteristic, final byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for write on characteristic with UUID " + characteristic.getUuid().toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request write characteristic not initiated: device is disconnected.");
            return false;
        }

        if (characteristic == null) {
            Log.w(TAG, "request write characteristic not initiated: characteristic is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(characteristic.getUuid())) {
            Log.w(TAG, "request write characteristic not initiated: unknown characteristic UUID.");
            return false;
        }

        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) <= 0) {
                Log.w(TAG, "request write characteristic not initiated: characteristic does not have the " +
                        "WRITE property.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request request = Request.createWriteCharacteristicRequest(characteristic, data);
        addToRequestsQueue(request);
        return true;
    }

    /**
     * <p>To write without response some characteristic data for the given characteristic if this Android service is
     * connected to a Bluetooth device.</p>
     *
     * @param characteristic
     *              the specified characteristic.
     * @param data
     *              the data to write.
     *
     * @return <p>false if it is not possible to request a characteristic write. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "write without response" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestWriteNoResponseCharacteristic(BluetoothGattCharacteristic characteristic, final
    byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for write without response on characteristic with UUID "
                    + characteristic.getUuid().toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request write without response characteristic not initiated: device is disconnected.");
            return false;
        }

        if (characteristic == null) {
            Log.w(TAG, "request write without response characteristic not initiated: characteristic is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(characteristic.getUuid())) {
            Log.w(TAG, "request write without response characteristic not initiated: unknown characteristic UUID.");
            return false;
        }

        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) <= 0) {
            Log.w(TAG, "request write without response characteristic not initiated: characteristic does not have " +
                    "the WRITE NO RESPONSE property.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request request = Request.createWriteNoResponseCharacteristicRequest(characteristic, data);
        addToRequestsQueue(request);
        return true;
    }

    /**
     * <p>To read characteristic information for the given characteristic UUID if this service is connected to a Bluetooth device.</p>
     *
     * @param characteristicUUID
     *              the specified characteristic.
     *
     * @return <p>false if it is not possible to request a characteristic read. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "read" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadCharacteristic(UUID characteristicUUID) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read on characteristic with UUID " + characteristicUUID.toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read characteristic not initiated: device is disconnected.");
            return false;
        }

        BluetoothGattCharacteristic characteristic = mCharacteristics.get(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "request read characteristic not initiated: characteristic not found for UUID " +
                    characteristicUUID + ".");
            return false;
        }

        // all check passed successfully, the request can be initiated
        return requestReadCharacteristic(characteristic);
    }

    /**
     * <p>To read characteristic information for the given characteristic if this service is connected to a Bluetooth device.</p>
     *
     * @param characteristic
     *              the specified characteristic.
     *
     * @return <p>false if it is not possible to request a characteristic read. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given characteristic does not correspond to any of the available characteristics of the connected device.</li>
     *     <li>The characteristic does not have the "read" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read on characteristic with UUID " + characteristic.getUuid().toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read characteristic not initiated: device is disconnected.");
            return false;
        }

        if (characteristic == null) {
            Log.w(TAG, "request read characteristic not initiated: characteristic is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(characteristic.getUuid())) {
            Log.w(TAG, "request read characteristic not initiated: unknown characteristic UUID.");
            return false;
        }

        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) <= 0) {
            Log.w(TAG, "request read characteristic not initiated: characteristic does not have the " +
                    "READ property.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request request = Request.createReadCharacteristicRequest(characteristic);
        addToRequestsQueue(request);
        return true;
    }

    /**
     * <p>To read characteristic information only once for the given characteristic UUID in order to induce Android to
     * start the bonding automatically if needed for the given characteristic. This method will proceed if this service
     * is connected to a Bluetooth device.</p>
     * <p>When a read request fails for
     * {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION GATT_INSUFFICIENT_AUTHENTICATION} (0x0005) or
     * GATT_AUTH_FAIL (0x0089) status, the Android system automatically starts the bonding. However, depending on
     * the Android system as well as the Android device Bluetooth implementation, the Android system can fail to
     * detect it should start the bonding. In order to be sure the application and the service can continue their
     * process, this service acts as follows:
     * <ul>
     *     <li>{@link #readCharacteristic(BluetoothGattCharacteristic) readCharacteristic} returns false: the service
     *     will attempt to bond in {@link #onRequestFailed(Request) onRequestFailed}.</li>
     *     <li>{@link BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)
     *     onCharacteristicRead} receives callback:
     *     <ul>
     *         <li>{@link BluetoothGatt#GATT_SUCCESS} status: no need to bond, the process will continue -
     *         application depending.</li>
     *         <li>Other status: the service will attempt to bond in {@link #onRequestFailed(Request) onRequestFailed}
     *          if Android didn't automatically started the bonding.</li>
     *     </ul></li>
     *     <li>No callback received for the {@link BluetoothGattCallback BluetoothGattCallback} object: a
     *     {@link Runnable Runnable} will be triggered which will lead to
     *     {@link #onRequestFailed(Request) onRequestFailed} to start the bonding if the system didn't already start
     *     it. The runnable will be triggered after the time defined with the method
     *     {@link #setDelayForRequest(int) setDelayForRequest}.</li>
     * </ul></p>
     * <p>This service uses a read characteristic request to induce the pairing because we noticed that it is the
     * most stable request as it is less parameter dependant than a write request for instance.</p>
     *
     * @param characteristicUUID
     *              Give a specified characteristic which requires pairing to be used.
     *
     * @return <p>false if it is not possible to request a characteristic read. The reasons could be:
     * <ul>
     *     <li>The device is not connected.</li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the
     *     connected device.</li>
     *     <li>The characteristic does not have the "read" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadCharacteristicForPairing(UUID characteristicUUID) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read to induce pairing on characteristic with UUID "
                    + characteristicUUID.toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: device is disconnected.");
            return false;
        }

        BluetoothGattCharacteristic characteristic = mCharacteristics.get(characteristicUUID);

        if (characteristic == null) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: characteristic not found for " +
                    "UUID " + characteristicUUID + ".");
            return false;
        }

        // all check passed successfully, the request can be initiated
        return requestReadCharacteristicForPairing(characteristic);
    }

    /**
     * <p>To read characteristic information only once for the given characteristic UUID in order to induce Android to
     * start the bonding automatically if needed for the given characteristic. This method will proceed if this service
     * is connected to a Bluetooth device.</p>
     * <p>When a read request fails for
     * {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION GATT_INSUFFICIENT_AUTHENTICATION} (0x0005) or
     * GATT_AUTH_FAIL (0x0089) status, the Android system automatically starts the bonding. However, depending on
     * the Android system as well as the Android device Bluetooth implementation, the Android system can fail to
     * detect it should start the bonding. In order to be sure the application and the service can continue their
     * process, this service acts as follows:
     * <ul>
     *     <li>{@link #readCharacteristic(BluetoothGattCharacteristic) readCharacteristic} returns false: the service
     *     will attempt to bond in {@link #onRequestFailed(Request) onRequestFailed}.</li>
     *     <li>{@link BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)
     *     onCharacteristicRead} receives callback:
     *     <ul>
     *         <li>{@link BluetoothGatt#GATT_SUCCESS} status: no need to bond, the process will continue -
     *         application depending.</li>
     *         <li>Other status: the service will attempt to bond in {@link #onRequestFailed(Request) onRequestFailed}
     *          if Android didn't automatically start the bonding.</li>
     *     </ul></li>
     *     <li>No callback received for the {@link BluetoothGattCallback BluetoothGattCallback} object: a
     *     {@link Runnable Runnable} will be triggered which will lead to
     *     {@link #onRequestFailed(Request) onRequestFailed} to start the bonding if the system didn't already start
     *     it. The runnable will be triggered after the time defined with the method
     *     {@link #setDelayForRequest(int) setDelayForRequest}.</li>
     * </ul></p>
     * <p>This service uses a read characteristic request to induce the pairing because we noticed that it is the
     * most stable request as it is less parameter dependant than a write request for instance.</p>
     *
     * @param characteristic
     *              Give a specified characteristic which requires pairing to be used.
     *
     * @return <p>false if it is not possible to request a characteristic read. The reasons could be:
     * <ul>
     *     <li>The device is not connected.</li>
     *     <li>The given characteristic UUID does not correspond to any of the available characteristics of the
     *     connected device.</li>
     *     <li>The characteristic does not have the "read" property.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadCharacteristicForPairing(BluetoothGattCharacteristic characteristic) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read to induce pairing on characteristic with UUID "
                    + characteristic.getUuid().toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: device is disconnected.");
            return false;
        }

        if (characteristic == null) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: characteristic is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(characteristic.getUuid())) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: unknown characteristic UUID.");
            return false;
        }

        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) <= 0) {
            Log.w(TAG, "request read to induce pairing characteristic not initiated: characteristic does not have " +
                    "the READ property.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request request = Request.createReadCharacteristicRequestToInducePairing(characteristic);
        // the request will be done only once as it is used to induce the pairing
        // if the Callback receives an unsuccessful status for this request there is no need to do this request again
        request.setAttempts(REQUEST_MAX_ATTEMPTS-1);
        addToRequestsQueue(request);
        return true;
    }

    /**
     * <p>To read descriptor information for the given characteristic if this service is connected to a
     * Bluetooth device.</p>
     *
     * @param descriptor
     *              the specified descriptor.
     *
     * @return <p>false if it is not possible to request a descriptor read. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The given descriptor does not correspond to any of the available characteristics of the connected device
     *     .</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadDescriptor(BluetoothGattDescriptor descriptor) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read on descriptor with UUID "
                    + descriptor.getUuid().toString());
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read on descriptor not initiated: device is disconnected.");
            return false;
        }

        if (descriptor == null) {
            Log.w(TAG, "request read on descriptor not initiated: descriptor is null.");
            return false;
        }

        if (!mCharacteristics.containsKey(descriptor.getCharacteristic().getUuid())) {
            Log.w(TAG, "request read on descriptor not initiated: unknown characteristic UUID.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        Request request = Request.createReadDescriptorRequest(descriptor);
        addToRequestsQueue(request);
        return true;
    }

    /**
     * <p>To request the RSSI of the remote device if this service is connected to a remote device.</p>
     * <p>Unfortunately this method only works for BLE only devices. When the device is
     * {@link BluetoothDevice#DEVICE_TYPE_DUAL DEVICE_TYPE_DUAL} the callback method
     * {@link BluetoothGattCallback#onReadRemoteRssi(BluetoothGatt, int, int) onReadRemoteRssi} is never called when the
     * {@link BluetoothGatt#readRemoteRssi() readRemoteRssi} is called.</p>
     *
     * @return <p>false if it is not possible to request the RSSI level. The reasons could be:
     * <ul>
     *     <li>The device is not connected./li>
     *     <li>The connected device is not known as BLE only.</li>
     * </ul>
     * Returns true if the request could be added to the requests queue.</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean requestReadRssi() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for read RSSI level");
        }

        if (mConnectionState != State.CONNECTED) {
            Log.w(TAG, "request read RSSI level not initiated: device is disconnected.");
            return false;
        }

        if (mDevice == null) {
            Log.w(TAG, "request read RSSI level not initiated: device is null.");
            return false;
        }

        if (mDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
            Log.w(TAG, "request read RSSI level not initiated: device is not LE only.");
            return false;
        }

        // all check passed successfully, the request can be initiated
            Request request = Request.createReadRssiRequest();
            addToRequestsQueue(request);
            return true;
    }

    /**
     * <p>Initializes a reference to the local Bluetooth adapter.</p>
     *
     * @return Return true if the initialization is successful or the Bluetooth adapter had already been initialised.
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean initialize() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for initialisation of the Bluetooth components");
        }

        if (mBluetoothAdapter != null && mShowDebugLogs) {
            Log.d(TAG, "Bluetooth adapter already initialized");
            return true;
        }

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Initialisation of the Bluetooth Adapter failed: unable to initialize BluetoothManager.");
            return false;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Initialisation of the Bluetooth Adapter failed: unable to initialize BluetoothManager.");
            return false;
        }

        return true;
    }

    /**
     * <p>Gets the connection state between the service and a BLE device.</p>
     *
     * @return the connection state.
     */
    protected @State int getConnectionState() {
        return mConnectionState;
    }

    /**
     * <p>Gets the device with which this service is connected or has been connected.</p>
     *
     * @return the BLE device.
     */
    protected BluetoothDevice getDevice() {
        return mDevice;
    }

    /**
     * <p>Gets the device with which this service is connected or has been connected.</p>
     *
     * @return the BLE device.
     */
    protected BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }


    // ====== PROTECTED METHODS ====================================================================

    /**
     * <p>Connects to the GATT server hosted on the Bluetooth LE device.</p>
     *
     * @param address
     *            The device to connect with.
     *
     * @return Return <code>true</code> if the connection is initiated successfully. The connection result is reported asynchronously
     *         through the {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean connectToDevice(String address) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to connect to a device with address " + address);
        }

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.w(TAG, "request connect to device not initiated: bluetooth address is unknown.");
            return false;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "request connect to device not initiated: unable to get a BluetoothDevice from address " +
                    address);
            return false;
        }

        // all check passed successfully, the request can be initiated
        return connectToDevice(device);
    }

    /**
     * <p>Connects to the GATT server hosted on the Bluetooth LE device.</p>
     * <p>Depending on the Android version running this code, to connect with the device this method uses
     * {@link BluetoothDevice#connectGatt(Context, boolean, BluetoothGattCallback)} for versions below Android 6
     * Marshmallow and uses the method {@link BluetoothDevice#connectGatt(Context, boolean, BluetoothGattCallback, int)}
     * with {@link BluetoothDevice#TRANSPORT_LE} for versions above.</p>
     *
     * @param device
     *            The device to connect with.
     *
     * @return Return <code>true</code> if the connection is initiated successfully. The connection result is reported asynchronously
     *         through the {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    protected boolean connectToDevice(BluetoothDevice device) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to connect to a BluetoothDevice");
        }

        if (device == null) {
            Log.w(TAG, "request connect to BluetoothDevice failed: device is null.");
            return false;
        }

        if (mConnectionState == State.CONNECTED) {
            Log.w(TAG, "request connect to BluetoothDevice failed: a device is already connected.");
            return false;
        }

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "request connect to BluetoothDevice failed: no BluetoothAdapter initialized.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        mDevice = device;

        setState(State.CONNECTING);

        // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
        // initiating a complete new connection is faster than reusing the same gatt connection with Android.
        Log.d(TAG, "request connect to BluetoothDevice " + mDevice.getAddress() + " over GATT starts.");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }
        else {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }

        return true;
    }

    /**
     * <p>Connects to the GATT server of a previous connected device.</p>
     *
     * @return Return <code>true</code> if the connection is initiated successfully. The connection result is reported asynchronously
     *         through the {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    @SuppressWarnings("UnusedReturnValue") // the return value is used for some implementations
    protected boolean reconnectToDevice() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to reconnect to a BluetoothDevice");
        }

        if (mDevice == null) {
            Log.w(TAG, "request reconnect to BluetoothDevice failed: device is null.");
            return false;
        }

        if (mConnectionState == State.CONNECTED) {
            Log.w(TAG, "request reconnect to BluetoothDevice failed: a device is already connected.");
            return false;
        }

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "request reconnect to BluetoothDevice failed: no BluetoothAdapter initialized.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        setState(State.CONNECTING);

        // We want to directly connect to the device once available - if the disconnection was coming from the
        // device - so we are setting the autoConnect parameter to true.
        // initiating a complete new connection is faster than reusing the same gatt connection with Android.
        Log.d(TAG, "request reconnect to BluetoothDevice " + mDevice.getAddress() + " over GATT starts.");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mBluetoothGatt = mDevice.connectGatt(this, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        }
        else {
            mBluetoothGatt = mDevice.connectGatt(this, true, mGattCallback);
        }

        return true;
    }

    /**
     * <p>After using a given BLE device, this method must have to be called to ensure resources are released
     * properly.</p>
     * <p>This method resets all information, disconnects an existing connection or cancel a pending one. The result is
     * reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} callback.</p>
     */
    protected void disconnectFromDevice() {
        resetQueue();
        mCharacteristics.clear();

        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to disconnect from a BluetoothDevice");
        }

        if (mBluetoothAdapter == null) {
            Log.i(TAG, "request disconnect from BluetoothDevice: BluetoothAdapter is null.");
            setState(State.DISCONNECTED);
            return;
        }

        if (mBluetoothGatt == null) {
            Log.i(TAG, "request disconnect from BluetoothDevice: BluetoothGatt is null.");
            setState(State.DISCONNECTED);
            return;
        }

        // all check passed successfully, the request can be initiated
        Log.i(TAG, "Request disconnect from BluetoothDevice " + mBluetoothGatt.getDevice().getAddress() + " starts.");
        setState(State.DISCONNECTING);
        mBluetoothGatt.disconnect();
    }

    /**
     * <p>To set the connection state of this Service with a Bluetooth device.</p>
     * <p>Any subclass should override this class - and call the super implementation - in order to know when the
     * connection state changes.</p>
     *
     * @param newState The new State of the connection.
     */
    protected synchronized void setState(@State int newState) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Connection state changes from " + BLEUtils.getConnectionStateName(mConnectionState) + " to "
                    + BLEUtils.getConnectionStateName(newState));
        }
        mConnectionState = newState;
    }



    // ====== PROTECTED ABSTRACT METHODS TO IMPLEMENT ==============================================

    /**
     * <p>This method is called when the connection state of the specified Bluetooth gatt changed.</p>
     *
     * @param gatt
     *              The Bluetooth gatt concerned for the connection state change.
     * @param status
     *              The status of this connection change: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     * @param newState
     *              The new connection state: Can be one of {@link BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

    /**
     * <p>This method is called when the services of the specified Bluetooth gatt has been discovered.</p>
     *
     * @param gatt
     *              The Bluetooth gatt concerned for the services discovering.
     * @param status
     *              The status of the service discovering: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    /**
     * <p>This method is called when the Bluetooth GATT client receives a value for a characteristic read request.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which received the information.
     * @param characteristic
     *              The Bluetooth Characteristic for which there is a received information.
     * @param status
     *              The status of this characteristic reading: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    /**
     * <p>This method is called when the characteristic notification status changes.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which received the information.
     * @param characteristic
     *              The Bluetooth Characteristic for which there is a change.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onReceivedCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    /**
     * <p>This method is called when a descriptor write operation has been requested.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the descriptor write operation.
     * @param descriptor
     *              The Bluetooth Characteristic Descriptor for which a request has been made.
     * @param status
     *              The result of the write operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    /**
     * <p>This method is called when the asynchronous answer to the {@link #requestReadRssi() requestReadRssi}
     * method had been received.</p>
     * <p><i>If the device type is {@link BluetoothDevice#DEVICE_TYPE_DUAL DEVUCE_TYPE_DUAL} no asynchronous callback
     * is sent to the {@link BluetoothGattCallback BluetoothGattCallback}.</i></p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the remote rssi.
     * @param rssi
     *              The rssi value sent by the remote device.
     * @param status
     *              The result of the read operation, {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onRemoteRssiRead(BluetoothGatt gatt, int rssi, int status);

    /**
     * <p>This method is called when a descriptor write operation has been requested</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the descriptor read operation.
     * @param descriptor
     *              The Bluetooth Characteristic Descriptor for which a request has been made.
     * @param status
     *              The result of the read operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    /**
     * <p>This method is called when a characteristic write operation has been requested by the Android device.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the characteristic write operation.
     * @param characteristic
     *              The Bluetooth Characteristic Characteristic for which a request has been made.
     * @param status
     *              The result of the write operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected abstract void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);


    // ====== PRIVATE METHODS FOR BLUETOOTH CALL BACKS =============================================

    /**
     * <p>This method is called when the connection state of the specified Bluetooth gatt changed.</p>
     *
     * @param gatt
     *              The Bluetooth gatt concerned for the connection state change.
     * @param status
     *              The status of this connection change: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     * @param newState
     *              The new connection state: Can be one of {@link BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}.
     */
    private void receiveConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onConnectionStateChange, newState=" + newState + ", status=" + status);
        }

        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            setState(State.CONNECTED);
            Log.i(TAG, "Successful connection to device: " + gatt.getDevice().getAddress());
            if (mBluetoothGatt == null) {
                mBluetoothGatt = gatt;
            }
            // service discovery to be done by subclass
        }

        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            if (mConnectionState == State.DISCONNECTING) {
                Log.i(TAG, "Successful disconnection from device: " + gatt.getDevice().getAddress());
            }
            else {
                Log.i(TAG, "Disconnected from device: " + gatt.getDevice().getAddress());
            }
            setState(State.DISCONNECTED);
            resetQueue();
            mCharacteristics.clear();

            if (mShowDebugLogs) {
                Log.d(TAG, "Device disconnected, closing BluetoothGatt object.");
            }
            // we will initiate a new Bluetooth GATT connection
            gatt.close();
            mBluetoothGatt = null;
        }

        onConnectionStateChange(gatt, status, newState);
    }

    /**
     * <p>This method is called when the services of the specified Bluetooth gatt has been discovered.</p>
     *
     * @param gatt
     *              The Bluetooth gatt concerned for the services discovering.
     * @param status
     *              The status of the service discovering: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveServicesDiscovered(BluetoothGatt gatt, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onServicesDiscovered, status=" + status);
        }

        if (status == BluetoothGatt.GATT_SUCCESS) {
            for (BluetoothGattService service : gatt.getServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    mCharacteristics.put(characteristic.getUuid(), characteristic);
                }
            }
        } else {
            Log.w(TAG, "Unsuccessful status for GATT Services discovery on callback: "
                    + BLEUtils.getGattStatusName(status, false));
        }

        processNextRequest();

        onServicesDiscovered(gatt, status);
    }

    /**
     * <p>This method is called when the Bluetooth GATT client receives a value for a characteristic read request.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which received the information.
     * @param characteristic
     *              The Bluetooth Characteristic for which there is a received information.
     * @param status
     *              The status of this characteristic reading: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onCharacteristicRead, characteristic=" + characteristic.getUuid() + "status="
                    + status);
        }

        // 2 types of request can lead to this callback
        // if there is an expected callback for READ_CHARACTERISTIC_TO_INDUCE_PAIRING this request type will be used
        // as the expected request type, otherwise READ_CHARACTERISTIC will be used as the default type.
        @Request.RequestType int expectedRequestType = mTimeOutRequestRunnable != null
               && mTimeOutRequestRunnable.request.getType()
                                == Request.RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING ?
                       Request.RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING :
                       Request.RequestType.READ_CHARACTERISTIC;
        Request request = onReceiveCallback(expectedRequestType, characteristic);
        boolean expectedCallback = request != null;

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Unsuccessful read characteristic for characteristic " + characteristic.getUuid().toString()
                    + " - status: " + BLEUtils.getGattStatusName(status, false));
            if (expectedCallback) onRequestFailed(request);
        }
        else if (expectedCallback) {
            processNextRequest();
        }

        onCharacteristicRead(gatt, characteristic, status);
    }

    /**
     * <p>This method is called when the Bluetooth GATT client receives a value for a remote rssi read request.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which received the information.
     * @param rssi
     *              The rssi value which had given.
     * @param status
     *              The status of this characteristic reading: {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveRemoteRssiRead(BluetoothGatt gatt, int rssi, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onRemoteRssiRead, rssi=" + rssi + "status=" + status);
        }

        Request request = onReceiveCallback(Request.RequestType.READ_RSSI);
        boolean expectedCallback = request != null;

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Unsuccessful remote rssi read - status: " + BLEUtils.getGattStatusName(status, false));
            if (expectedCallback) onRequestFailed(request);
        }
        else if (expectedCallback) {
            processNextRequest();
        }

        onRemoteRssiRead(gatt, rssi, status);
    }

    /**
     * <p>This method is called when a descriptor write operation has been requested.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the descriptor write operation.
     * @param descriptor
     *              The Bluetooth Characteristic Descriptor for which a request has been made.
     * @param status
     *              The result of the write operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onDescriptorWrite, descriptor=" + descriptor.getUuid() + "status=" + status);
        }
        Request request = onReceiveCallback(Request.RequestType.WRITE_DESCRIPTOR, descriptor);
        boolean expectedCallback = request != null;

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Unsuccessful write descriptor for characteristic "
                    + descriptor.getCharacteristic().getUuid().toString() + " - status: "
                    + BLEUtils.getGattStatusName(status, false));
            if (expectedCallback) onRequestFailed(request);
        }
        else if (expectedCallback) {
            processNextRequest();
        }

        onDescriptorWrite(gatt, descriptor, status);
    }

    /**
     * <p>This method is called when a descriptor write operation has been requested</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the descriptor read operation.
     * @param descriptor
     *              The Bluetooth Characteristic Descriptor for which a request has been made.
     * @param status
     *              The result of the read operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onDescriptorRead, descriptor=" + descriptor.getUuid() + "status=" + status);
        }
        Request request = onReceiveCallback(Request.RequestType.READ_DESCRIPTOR, descriptor);
        boolean expectedCallback = request != null;

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Unsuccessful read descriptor for characteristic "
                    + descriptor.getCharacteristic().getUuid().toString()
                    + " - status: " + BLEUtils.getGattStatusName(status, false));
            if (expectedCallback) onRequestFailed(request);
        }
        else if (expectedCallback) {
            processNextRequest();
        }

        onDescriptorRead(gatt, descriptor, status);
    }

    /**
     * <p>This method is called when a characteristic write operation has been requested by the Android device.</p>
     *
     * @param gatt
     *              The Bluetooth gatt which requested the characteristic write operation.
     * @param characteristic
     *              The Bluetooth Characteristic Characteristic for which a request has been made.
     * @param status
     *              The result of the write operation {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    private void receiveCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mShowDebugLogs) {
            Log.d(TAG, "GattCallback - onCharacteristicWrite, characteristic=" + characteristic.getUuid() + "status=" +
                    status);
        }
        // 2 types of request can lead to this callback
        // if there is an expected callback for WRITE_NO_RESPONSE_CHARACTERISTIC this request type will be used
        // as the expected request type, otherwise WRITE_CHARACTERISTIC will be used as the default type.
        @Request.RequestType int expectedRequestType = mTimeOutRequestRunnable != null
                && mTimeOutRequestRunnable.request.getType()
                == Request.RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC ?
                Request.RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC :
                Request.RequestType.WRITE_CHARACTERISTIC;
        Request request = onReceiveCallback(expectedRequestType, characteristic);
        boolean expectedCallback = request != null;

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Unsuccessful write characteristic for characteristic " + characteristic.getUuid().toString()
                    + " - status: " + BLEUtils.getGattStatusName(status, false));
            if (expectedCallback) onRequestFailed(request);
        }
        else if (expectedCallback) {
            processNextRequest();
        }

        onCharacteristicWrite(gatt, characteristic, status);
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported asynchronously through
     * the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.</p>
     *
     * @param characteristic
     *            The characteristic to read from.
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process request read characteristic for characteristic " + characteristic.getUuid());
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Read characteristic cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Read characteristic cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.readCharacteristic(characteristic);
        if (mShowDebugLogs) {
            Log.d(TAG, "Request read characteristic dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>Enables or disables notification for the specified characteristic.</p>
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process request set characteristic notification for characteristic " + characteristic.getUuid
                    () + " with enabled=" + enabled);
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Set characteristic notification cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Set characteristic notification cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (mShowDebugLogs) {
            Log.d(TAG, "Request set characteristic notification dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>To read the value of a given descriptor from the associated remote device.</p>
     *
     * @param descriptor
     *              the descriptor to request a read operation on.
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process request read descriptor for descriptor " + descriptor.getUuid());
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Read descriptor cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Read descriptor cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.readDescriptor(descriptor);
        if (mShowDebugLogs) {
            Log.d(TAG, "Request read descriptor dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>To write the value of a given descriptor to the associated remote device.</p>
     *
     * @param descriptor
     *              the descriptor to request a write operation on.
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean writeDescriptor(BluetoothGattDescriptor descriptor) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process request write descriptor for descriptor " + descriptor.getUuid());
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Write descriptor cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Write descriptor cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.writeDescriptor(descriptor);
        if (mShowDebugLogs) {
            Log.d(TAG, "Request write descriptor dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>To write the value of a given characteristic to the associated remote device.</p>
     *
     * @param characteristic
     *              the characteristic to request a write operation on.
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process request write characteristic for characteristic " + characteristic.getUuid());
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Write characteristic cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Write characteristic cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.writeCharacteristic(characteristic);
        if (mShowDebugLogs) {
            Log.d(TAG, "Request write characteristic dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>Request the RSSI read on the remote device. The read result is reported asynchronously through
     * the {@code BluetoothGattCallback{@link #onRemoteRssiRead(BluetoothGatt, int, int)} callback.</p>
     *
     * @return true, if the read operation was initiated successfully.
     */
    private boolean readRemoteRssi() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Process read remote RSSI");
        }
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Read remote RSSI cannot be processed: BluetoothAdapter is null.");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "Read remote RSSI cannot be processed: BluetoothGatt is null.");
            return false;
        }
        boolean done = mBluetoothGatt.readRemoteRssi();
        if (mShowDebugLogs) {
            Log.d(TAG, "Request read remote RSSI dispatched to system: " + done);
        }
        return done;
    }

    /**
     * <p>Retrieves a list of supported GATT services on the connected device. This should be invoked only after
     * {@code BluetoothGatt#discoverServices()} completes successfully.</p>
     *
     * @return A {@code List} of supported services.
     */
    protected List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "getSupportedGattServices() - BluetoothGatt is null.");
            return null;
        }

        return mBluetoothGatt.getServices();
    }


    // ====== REQUESTS PROCESS ========================================================================

    /**
     * <p>This method is called when an ongoing request has failed. A request is marked as failed when:
     * <ul>
     *     <li>this service didn't receive any callback for it yet and it is timed out. This time is defined using
     *     the method {@link #setDelayForRequest(int) setDelayForRequest}</li>
     *     <li>The service received a callback with an unsuccessfulThe Bluetooth state.</li>
     * </ul></p>
     *
     * @param request
     *        The request which has failed.
     */
    private void onRequestFailed(Request request) {
        if (request != null && request.getAttempts() < REQUEST_MAX_ATTEMPTS) {
            addToRequestsQueue(request);
        }
        else {
            if (request != null) {
                // if the request to induce pairing has timed out and the pairing not induced, we create the bond
                Log.w(TAG, "Request " + Request.getRequestTypeLabel(request.getType()) + " failed");
                if (request.getType() == Request.RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING
                        && mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.i(TAG, "Induce pairing by creating bond manually.");
                    mDevice.createBond();
                }
            }
            else {
                Log.w(TAG, "An unknown request failed (null request object).");
            }
        }
        // queue not released and it is an expected request which failed
        processNextRequest();
    }

    /**
     * <p>This method is called when the GATT callback received a callback for requests this service has done.</p>
     * <p>This method will check if the callback was expected and fits the registered awaiting request.</p>
     *
     * @param requestType
     *          The type of request which received a callback.
     * @param characteristic
     *          The characteristic of the callback.
     *
     * @return The registered awaiting request if this callback was expected, null otherwise.
     */
    private Request onReceiveCallback(@Request.RequestType int requestType, BluetoothGattCharacteristic
            characteristic) {
        if (mTimeOutRequestRunnable != null
                && mTimeOutRequestRunnable.request.getType() == requestType && characteristic != null
                && mTimeOutRequestRunnable.request.getCharacteristic() != null
                && mTimeOutRequestRunnable.request.getCharacteristic().getUuid().equals(characteristic.getUuid())) {
            Request request = mTimeOutRequestRunnable.request;
            cancelTimeOutRequestRunnable();
            return request;
        }
        else {
            Log.w(TAG, "Received unexpected callback for characteristic " +
                    ((characteristic != null) ? characteristic.getUuid() : "null")
                    + " with request type = " + Request.getRequestTypeLabel(requestType));
            return null;
        }
    }

    /**
     * <p>This method is called when the GATT callback received a callback for requests this service might have done
     * .</p>
     * <p>This method will check if the callback was expected and fits the registered awaiting request.</p>
     *
     * @param requestType
     *          The type of request which received a callback.
     *
     * @return The registered awaiting request if this callback was expected, null otherwise.
     */
    private Request onReceiveCallback(@Request.RequestType int requestType) {
        if (mTimeOutRequestRunnable != null
                && mTimeOutRequestRunnable.request.getType() == requestType) {
            Request request = mTimeOutRequestRunnable.request;
            cancelTimeOutRequestRunnable();
            return request;
        }
        else {
            Log.w(TAG, "Received unexpected callback for request type = " + Request.getRequestTypeLabel(requestType));
            return null;
        }
    }

    /**
     * <p>This method is called when the GATT callback received a callback for requests this service has done.</p>
     * <p>This method will check if the callback was expected and fits the registered awaiting request.</p>
     *
     * @param requestType
     *          The type of request which received a callback.
     * @param descriptor
     *          The descriptor of the callback.
     *
     * @return The registered awaiting request if this callback was expected, null otherwise.
     */
    private Request onReceiveCallback(@Request.RequestType int requestType, BluetoothGattDescriptor descriptor) {
        if (mTimeOutRequestRunnable != null
                && mTimeOutRequestRunnable.request.getType() == requestType && descriptor != null
                && mTimeOutRequestRunnable.request.getDescriptor() != null
                && mTimeOutRequestRunnable.request.getDescriptor().getUuid().equals(descriptor.getUuid())) {
            Request request = mTimeOutRequestRunnable.request;
            cancelTimeOutRequestRunnable();
            return request;
        }
        else {
            Log.w(TAG, "Received unexpected callback for descriptor " +
                    ((descriptor != null) ? descriptor.getUuid() : "null")
                    + " with request type = " + Request.getRequestTypeLabel(requestType));
            return null;
        }
    }


    // ====== QUEUE PROCESS ========================================================================

    /**
     * <p>Add a request item to the requests queue.</p>
     *
     * @param request
     *              The request item to add to the queue.
     */
    private void addToRequestsQueue(Request request) {
        if (request.getAttempts() < REQUEST_MAX_ATTEMPTS) {
            if (mShowDebugLogs) {
                Log.d(TAG, "Add request of type " + Request.getRequestTypeLabel(request.getType())
                        + "to the Queue of requests to process.");
            }
            mRequestsQueue.add(request);
        }
        else {
            Log.w(TAG, "Request " + Request.getRequestTypeLabel(request.getType()) + " failed after "
                    + request.getAttempts() + " attempts.");
        }

        if (!isQueueProcessing) {
            processNextRequest();
        }
    }

    /**
     * <p>To reset the queue process to an empty state: clearing the queue and reset the TimeOutRequestRunnable.</p>
     */
    private void resetQueue() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Reset the Queue of requests to process.");
        }
        mRequestsQueue.clear();
        isQueueProcessing = false;
        cancelTimeOutRequestRunnable();
    }

    /**
     * <p>To cancel the TimeOutRequestRunnable if there is any.</p>
     */
    private void cancelTimeOutRequestRunnable() {
        if (mTimeOutRequestRunnable != null) {
            mHandler.removeCallbacks(mTimeOutRequestRunnable);
            mTimeOutRequestRunnable = null;
        }
    }

    /**
     * <p>Call when a request can start. Will process the next request if queued.</p>
     */
    private void processNextRequest() {
        // in case a request is lost and there is no callback about it, the service has to be able to start a new one.
        isQueueProcessing = true;

        // if there is a time out running, we have to wait for it to finish
        if(mTimeOutRequestRunnable != null) {
            return;
        }

        // Queue is empty: no request to process
        if (mRequestsQueue.size() <= 0) {
            isQueueProcessing = false;
            return;
        }

        // if there is no device connected, the requests are cancelled
        if (mConnectionState != State.CONNECTED) {
            resetQueue();
            return;
        }

        // processing the next request
        Request request = mRequestsQueue.remove();
        request.increaseAttempts();

        if (mShowDebugLogs) {
            Log.d(TAG, "Processing request of type " + Request.getRequestTypeLabel(request.getType()));
        }

        // to know if the request has successfully started
        boolean done = false;

        // processing the request depending on its type
        switch (request.getType()) {
            case Request.RequestType.READ_CHARACTERISTIC:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattCharacteristic readCharacteristic = request.buildReadCharacteristic();
                done = readCharacteristic != null && readCharacteristic(readCharacteristic);
                break;

            case Request.RequestType.WRITE_DESCRIPTOR:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattDescriptor descriptor = request.buildWriteDescriptor();
                done = descriptor != null && writeDescriptor(descriptor);
                break;

            case Request.RequestType.WRITE_CHARACTERISTIC:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattCharacteristic writeCharacteristic = request.buildWriteCharacteristic();
                done = writeCharacteristic != null && writeCharacteristic(writeCharacteristic);
                break;

            case Request.RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattCharacteristic writeNoResponse = request.buildWriteNoResponseCharacteristic();
                done = writeNoResponse != null && writeCharacteristic(writeNoResponse);
                break;

            case Request.RequestType.READ_DESCRIPTOR:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattDescriptor readDescriptor = request.buildReadDescriptor();
                done = readDescriptor != null && readDescriptor(readDescriptor);
                break;

            case Request.RequestType.CHARACTERISTIC_NOTIFICATION:
                processNotificationCharacteristicRequest(request);
                // end as this request needs a different workaround when its execution is successful or unsuccessful
                return;

            case Request.RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                BluetoothGattCharacteristic readCharacteristicForParing = request.buildReadCharacteristic();
                done = readCharacteristicForParing != null && readCharacteristic(readCharacteristicForParing);
                break;

            case Request.RequestType.READ_RSSI:
                mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
                mHandler.postDelayed(mTimeOutRequestRunnable, mDelay);
                done = readRemoteRssi();
                break;
        }

        if (!done) {
            cancelTimeOutRequestRunnable();
            Log.w(TAG, "Request " + Request.getRequestTypeLabel(request.getType()) + " fails to process.");
            onRequestFailed(request);
        }
    }

    /**
     * <p>This method processes the {@link Request.RequestType#CHARACTERISTIC_NOTIFICATION
     * CHARACTERISTIC_NOTIFICATION} request as it needs a special process.</p>
     * <p>For this request, the {@link BluetoothGattCallback BluetoothGattCallback} does not always receive a
     * callback. So this method processes this request and then starts the next one after a delay defined by
     * {@link BLEService#DEFAULT_DELAY_FOR_NOTIFICATION_REQUEST DEFAULT_DELAY_FOR_NOTIFICATION_REQUEST}.</p>
     * <p>This method is only called by the method {@link BLEService#processNextRequest() processNextRequest}.
     * To successfully manage the Request queue this method MUST NOT be called by any other method. </p>
     *
     * @param request The notification characteristic to process.
     */
    private void processNotificationCharacteristicRequest(Request request) {
        BluetoothGattCharacteristic notifyCharacteristic = request.buildNotifyCharacteristic();
        boolean done = notifyCharacteristic != null
                       && setCharacteristicNotification(notifyCharacteristic, request.getBooleanData());

        if (!done && request.getAttempts() < REQUEST_MAX_ATTEMPTS) {
            addToRequestsQueue(request);
            processNextRequest();
        }
        else if (done) {
            // needs a delay before to start the next request
            mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processNextRequest();
                    }
                },
                    DEFAULT_DELAY_FOR_NOTIFICATION_REQUEST);
        }
        else {
            request.setAttempts(REQUEST_MAX_ATTEMPTS);
            mTimeOutRequestRunnable = new TimeOutRequestRunnable(request);
            mHandler.postDelayed(mTimeOutRequestRunnable, DEFAULT_DELAY_FOR_NOTIFICATION_REQUEST);
        }
    }


    // ====== INNER CLASS ==========================================================================

    /**
     * <p>A Runnable to define what should be done if a request is timed out.</p>
     *
     */
    private class TimeOutRequestRunnable implements Runnable {
        /**
         * <p>The request which is monitored for a time out.</p>
         */
        private final Request request;

        /**
         * <p>Constructor for this class.</p>
         *
         * @param request
         *              The corresponding request.
         */
        /* package */ TimeOutRequestRunnable(Request request) {
            this.request = request;
        }

        @Override
        public void run() {
            mTimeOutRequestRunnable = null;
            Log.w(TAG, "Request " + Request.getRequestTypeLabel(request.getType()) + ": TIME OUT");
            onRequestFailed(request);
        }
    }
}
