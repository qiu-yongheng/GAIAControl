/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.gaiacontrol.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * <p>This class provides the tools to connect, manage a connection and disconnect with a BR/EDR device over RFCOMM.</p>
 * <p>The connection will be instantiated using {@link UUIDs#SPP SPP UUID} or {@link UUIDs#GAIA GAIA UUID} depending on
 * which UUID are given by the method {@link BluetoothDevice#getUuids() getUuids()}. If this list is empty - it can
 * happen when the system didn't fetch the UUIDs yet - the application will try with {@link UUIDs#SPP SPP UUID}
 * by default.</p>
 */
/*package*/ abstract class BREDRProvider {

    // ====== CONSTS ========================================================================================

    /**
     * <p>All UUIDs over RFCOMM this application can use.</p>
     */
    private static class UUIDs {
        /**
         * The SPP UUID as defined by Bluetooth specifications.
         */
        private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        /**
         * The specific GAIA UUID.
         */
        private static final UUID GAIA = UUID.fromString("00001107-D102-11E1-9B23-00025B00A5A5");
    }


    // ====== CONSTS FIELDS =================================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "BREDRProvider";


    // ====== PRIVATE FIELDS ================================================================================

    /**
     * The Bluetooth Device for which this class/object provides a BR EDR connection.
     */
    private BluetoothDevice mDevice = null;
    /**
     * <p>The Bluetooth Adapter used to know if the Bluetooth is available and to initiate some process to connect
     * to a device. For instance to check if the BluetoothDevice exists by checking its Bluetooth address.</p>
     */
    private final BluetoothAdapter mBluetoothAdapter;
    /**
     * <p>The Thread which processes a connection to a device.</p>
     * <p>This field is null when there is no ongoing connection or once the connection has been established.</p>
     */
    private ConnectionThread mConnectionThread = null;
    /**
     * <p>The Thread which allows communication with a connected device.</p>
     * <p>This field is null if there is no active connection.</p>
     */
    private CommunicationThread mCommunicationThread = null;
    /**
     * <p>The UUID to use to connect over RFCOMM: SPP or GAIA.</p>
     */
    private UUID mUUIDTransport;
    /**
     * The connection state of this provider.
     */
    private @State int mState = State.NO_STATE;
    /**
     * <p>To show the debug logs indicating when a method has been reached.</p>
     */
    private boolean mShowDebugLogs = false;


    // ====== ENUMS =========================================================================================

    /**
     * <p>The possible values for the Bluetooth connection state of this provider.</p>
     */
    @IntDef({State.CONNECTED, State.DISCONNECTED, State.CONNECTING, State.DISCONNECTING, State.NO_STATE})
    @Retention(RetentionPolicy.SOURCE)
    /*package*/ @interface State {
        int DISCONNECTED = 0;
        int CONNECTING = 1;
        int CONNECTED = 2;
        int DISCONNECTING = 3;
        int NO_STATE = 4;
    }

    /**
     * <p>The possible values for errors which can happen during the use of this provider.</p>
     */
    @IntDef({Errors.CONNECTION_FAILED, Errors.CONNECTION_LOST})
    @Retention(RetentionPolicy.SOURCE)
    /*package*/ @interface Errors {
        /**
         * <p>This error is thrown when the asynchronous connection process fails.</p>
         */
        int CONNECTION_FAILED = 0;
        /**
         * <p>This error is thrown when the connection is lost while listening through an ongoing connection.</p>
         */
        int CONNECTION_LOST = 1;
    }


    // ====== CONSTRUCTOR ==================================================================================

    /**
     * <p>Constructor of this class to get a provider of BR/EDR connection.</p>
     *
     * @param manager
     *          The BluetoothManager this provider should use to get a BluetoothAdapter. If this is null the provider
     *          will use {@link BluetoothAdapter#getDefaultAdapter() getDefaultAdapter}, known as less efficient than
     *          {@link BluetoothManager#getAdapter() getAdapter}.
     */
    /*package*/ BREDRProvider(BluetoothManager manager) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Creation of a new instance of BREDRProvider: " + this);
        }

        if (manager == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.i(TAG, "No available BluetoothManager, BluetoothAdapter initialised with BluetoothAdapter" +
                    ".getDefaultAdapter.");
        }
        else {
            mBluetoothAdapter = manager.getAdapter();
        }

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Initialisation of the Bluetooth Adapter failed: unable to initialize BluetoothAdapter.");
        }
    }


    // ====== PROTECTED METHODS ============================================================================

    /**
     * <p>To allow the display of the debug logs.</p>
     * <p>They give complementary information on any call of a method.
     * They can indicate that a method is reached but also the action the method does.</p>
     *
     * @param show True to show the debug logs, false otherwise.
     */
    /*package*/
    @SuppressWarnings("SameParameterValue")
    void showDebugLogs(boolean show) {
        mShowDebugLogs = show;
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
    }

    /**
     * <p>To get the device which is connected or had been connected through this provider.</p>
     * <p>If no successful connection had been made yet, this method returns null or a BluetoothDevice which can be
     * irrelevant.</p>
     *
     * @return The known BluetoothDevice with which a connection exists, has been made or attempted. The return value
     * can be null.
     */
    /*package*/ BluetoothDevice getDevice() {
        return mDevice;
    }

    /**
     * <p>This method gets the BluetoothDevice which corresponds to the given address and initiates the connection
     * through the {@link #connect(BluetoothDevice) connect(BluetoothDevice} method.</p>
     *
     * @param address
     *            The device address to connect with.
     *
     * @return Return <code>true</code> if the connection is initiated successfully. The connection result will be
     * reported asynchronously through the sending of the message
     * {@link GAIABREDRProvider.Messages#CONNECTION_STATE_HAS_CHANGED CONNECTION_STATE_HAS_CHANGED}.
     */
    @SuppressWarnings("UnusedReturnValue")
    /*package*/ boolean connect(String address) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to connect to a device with address " + address);
        }

        if (address == null) {
            Log.w(TAG, "connection failed: Bluetooth address is null.");
            return false;
        }

        if (address.length() == 0) {
            Log.w(TAG, "connection failed: Bluetooth address null or empty.");
            return false;
        }

        if (!isBluetoothAvailable()) {
            Log.w(TAG, "connection failed: unable to get the adapter to get the device object from BT address.");
            return false;
        }

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.w(TAG, "connection failed: unknown BT address.");
            return false;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);


        if (device == null) {
            // unknown device
            Log.w(TAG, "connection failed: get device from BT address failed.");
            return false;
        }

        // all check passed successfully, the request can be initiated
        return connect(device);
    }

    /**
     * <p>This method will initiate a connection with the last known BluetoothDevice with which a connection had
     * been established or attempted.</p>
     *
     * @return <code>true</code> if the reconnection process could be initiated.
     */
    /*package*/ boolean reconnectToDevice() {
        return mDevice != null && connect(mDevice);
    }

    /**
     * <p>To disconnect from an ongoing connection or to stop a connection process.</p>
     * <p>This method will cancel any ongoing Thread related to the connection of a BluetoothDevice.</p>
     *
     * @return This method returns <code>false</code> only if this provider was already disconnected from any
     * device.
     */
    /*package*/
    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
    boolean disconnect() {
        if (mShowDebugLogs) {
            Log.d(TAG, "Receives request to disconnect from device " +
                    (mDevice != null ? mDevice.getAddress() : "null"));
        }

        if (mState == State.DISCONNECTED) {
            Log.w(TAG, "disconnection failed: no device connected.");
            return false;
        }

        setState(State.DISCONNECTING);

        // cancel any running thread
        cancelConnectionThread();
        cancelCommunicationThread();

        setState(State.DISCONNECTED);

        Log.i(TAG, "Provider disconnected from BluetoothDevice " + (mDevice != null ? mDevice.getAddress() : "null"));

        return true;
    }

    /**
     * <p>To send some data to a connected BluetoothDevice.</p>
     *
     * @param data
     *          The bytes to send to the BluetoothDevice.
     * @return
     *          true if the sending could be initiated.
     */
    /*package*/ boolean sendData(byte[] data) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received for sending data to a device.");
        }

        // Create temporary object
        CommunicationThread thread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != State.CONNECTED) {
                Log.w(TAG, "Attempt to send data failed: provider not currently connected to a device.");
                return false;
            }
            if (mCommunicationThread == null) {
                Log.w(TAG, "Attempt to send data failed: CommunicationThread is null.");
                return false;
            }

            thread = mCommunicationThread;
        }

        // Perform a non synchronized write
        return thread.sendStream(data);
    }

    /**
     * <p>To know if the provider is ready to communicate with the remote device using the GAIA
     * protocol.</p>
     *
     * @return True if the provider is ready to let the application communicate with the device using the GAIA
     * protocol.
     */
    /*package*/ boolean isGaiaReady() {
        // to be ready the mBluetoothProvider needs to have a running connection
        return mState == State.CONNECTED && mCommunicationThread != null && mCommunicationThread.mmIsRunning;
    }

    /**
     * <p>Gets the current connection state between this service and a Bluetooth device.</p>
     *
     * @return the current connection state. This method can return {@link State#NO_STATE NO_STATE} if no connection
     * had been initiated yet.
     */
    /*package*/ synchronized @State int getState() {
        return mState;
    }


    // ====== ABSTRACT METHODS FOR SUBCLASSES ===============================================================

    /**
     * <p>This method is called by this provider when the connection state changes: device is disconnected,
     * connection had been lost, device is connecting, etc.</p>
     * <p>If the connection state has changed due to en error, the method
     * {@link #onConnectionError(int) onConnectionError} will also be called.</p>
     *
     * @param state the new state of the connection.
     */
    abstract void onConnectionStateChanged(@State int state);

    /**
     * <p>When an error occurs related to the BR/EDR connection this method is called by this provider.</p>
     *
     * @param error The type of error which occurred. Please see {@link Errors} for more information.
     */
    abstract void onConnectionError(@Errors int error);

    /**
     * <p>This method is called from the {@link CommunicationThread CommunicationThread} while running to let know
     * the application that it is ready to send and receive messages through an ongoing connection.</p>
     * <p>As this method is called from the running thread, the implementation if this class must take care of not
     * blocking the running thread in order to receive information from the device.</p>
     */
    abstract void onCommunicationRunning();

    /**
     * <p>This method is called when the {@link CommunicationThread CommunicationThread} has received some bytes
     * from the device.</p>
     *
     * @param data
     *          The bytes received from the device.
     */
    abstract void onDataFound(byte[] data);


    // ====== PRIVATE METHODS ==============================================================================

    /**
     * Set the current state of the chat connection
     *
     * @param state
     *         An integer defining the current connection state
     */
    private synchronized void setState(@State int state) {
        if (mShowDebugLogs) {

            Log.d(TAG, "Connection state changes from " + getConnectionStateName(mState) + " to "
                    + getConnectionStateName(state));
        }
        mState = state;
        onConnectionStateChanged(state);
    }

    /**
     * <p>To get a label for the different known connection states.</p>
     * <p>If the given state is not one of the states known as
     * {@link State#CONNECTED CONNECTED}, {@link State#CONNECTING CONNECTING},
     * {@link State#DISCONNECTING DISCONNECTING}, {@link State#DISCONNECTED DISCONNECTED} and
     * {@link State#NO_STATE NO_STATE}, this method returns "UNKNOWN" as the label.</p>
     *
     * @param state
     *          The state to get a corresponding label for.
     *
     * @return The label which corresponds to the given state.
     */
    private static String getConnectionStateName(@State int state ) {
        switch (state) {
            case State.CONNECTED:
                return "CONNECTED";
            case State.CONNECTING:
                return "CONNECTING";
            case State.DISCONNECTED:
                return "DISCONNECTED";
            case State.DISCONNECTING:
                return "DISCONNECTING";
            case State.NO_STATE:
                return "NO STATE";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * <p>This method aims to find the UUID transport this provider should use.</p>
     * <p>It goes through an array of {@link ParcelUuid ParcelUuid} in order to find the {@link UUIDs#SPP SPP} or
     * {@link UUIDs#GAIA GAIA} UUID.</p>
     *
     * @param uuids
     *          The list of UUIDs in which to look for the {@link UUIDs#SPP SPP} or  {@link UUIDs#GAIA GAIA} UUID.
     *
     * @return the first UUID found which matches {@link UUIDs#SPP SPP} or {@link UUIDs#GAIA GAIA}. If none of these
     * UUID could be found, this method returns null.
     */
    private UUID getUUIDTransport(ParcelUuid[] uuids) {
        if (uuids == null) {
            return null;
        }

        for (ParcelUuid parcel : uuids) {
            UUID uuid = parcel.getUuid();
            if (uuid.equals(UUIDs.SPP) || uuid.equals(UUIDs.GAIA)) {
                return uuid;
            }
        }

        return null;
    }

    /**
     * <p>This method will cancel the current ConnectionThread.</p>
     */
    private void cancelConnectionThread() {
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }
    }

    /**
     * <p>This method will cancel the current CommunicationThread.</p>
     */
    private void cancelCommunicationThread() {
        if (mCommunicationThread != null) {
            mCommunicationThread.cancel();
            mCommunicationThread = null;
        }
    }

    /**
     * <p>To initiate the BR/EDR connection with the given Bluetooth device.</p> <p>This method returns true if it has
     * been able to successfully initiate a BR/EDR connection with the device.</p> <p>The reasons for the connection
     * initiation to not be successful could be: <ul> <li>There is already a connected device.</li> <li>The device is
     * not BR/EDR compatible.</li> <li>Bluetooth is not available - could have been turned off, Android device doesn't
     * provide the feature, etc.</li> <li>The Bluetooth device address is unknown.</li> <li>A Bluetooth socket cannot be
     * established with the device.</li> <li>The UUIDs provided by the device does not contain {@link UUIDs#SPP SPP} or
     * {@link UUIDs#GAIA GAIA}.</li> </ul></p>
     *
     * @param device
     *         The device to connect with over a BR/EDR connection.
     *
     * @return True if the connection had been initialised, false if it can't be started.
     */
    private boolean connect(@NonNull BluetoothDevice device) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to connect to a BluetoothDevice " + device.getAddress());
        }

        if (mState == State.CONNECTED) {
            // already connected
            Log.w(TAG, "connection failed: a device is already connected");
            return false;
        }
        if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC
                && device.getType() != BluetoothDevice.DEVICE_TYPE_DUAL) {
            Log.w(TAG, "connection failed: the device is not BR/EDR compatible.");
            // the device is not BR EDR compatible
            return false;
        }
        if (!isBluetoothAvailable()) {
            // Bluetooth not available on Android device
            Log.w(TAG, "connection failed: Bluetooth is not available.");
            return false;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            // device cannot be found
            Log.w(TAG, "connection failed: device address not found in list of devices known by the system.");
            return false;
        }

        UUID transport = getUUIDTransport(device.getUuids());

        // connection can be processed only if a compatible transport has been found.
        // if the device has not yet been bonded, the UUIDs has not been fetched yet by the system.
        if (transport == null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "connection: device not bonded, no UUID available, attempt to connect using SPP.");
            transport = UUIDs.SPP;
        }
        else if (transport == null) {
            Log.w(TAG, "connection failed: device bonded and no compatible UUID available.");
            return false;
        }

        return connect(device, transport);
    }

    /**
     * <p><p>To initiate the BR/EDR connection with the given Bluetooth device using the given transport.</p>
     * <p>This method returns true if it has been able to successfully initiate the BR/EDR connection with the device
     * .</p>
     * <p>The reasons for the connection initiation to not be successful could be:
     * <ul>
     *     <li>There is already a connected device.</li>
     *     <li>The creation of the BluetoothSocket failed.</li>
     * </ul></p>
     *
     * @param device
     *          The BluetoothDevice to connect with.
     * @param transport
     *          The UUID transport to use to connect over RFCOMM.
     *
     * @return <code>true</code> if the connection had been successfully initiated.
     */
    private boolean connect(@NonNull BluetoothDevice device, @NonNull UUID transport) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Request received to connect to a BluetoothDevice with UUID " + transport.toString());
        }

        // Check there is no running connection
        if (mState == State.CONNECTED && mCommunicationThread != null) {
            Log.w(TAG, "connection failed: Provider is already connected to a device with an active communication.");
            return false;
        }

        // Cancel any thread attempting to make a connection
        cancelConnectionThread();
        // Cancel any thread currently running a connection
        cancelCommunicationThread();

        // attempt connection
        setState(State.CONNECTING);

        // create the Bluetooth socket
        BluetoothSocket socket = createSocket(device, transport);
        if (socket == null) {
            Log.w(TAG, "connection failed: creation of a Bluetooth socket failed.");
            return false; // socket creation failed
        }

        // all check passed successfully, the request can be initiated
        if (mShowDebugLogs) {
            Log.d(TAG, "Request connect to BluetoothDevice "
                    + socket.getRemoteDevice().getAddress() + " over RFCOMM starts.");
        }

        // keep device and UUID information
        mUUIDTransport = transport;
        mDevice = device;

        // Start the thread to connect with the given device
        mConnectionThread = new ConnectionThread(socket);
        mConnectionThread.start();

        return true;
    }

    /**
     * Returns the availability of Bluetooth feature for the Android device.
     *
     * @return true if the local Bluetooth adapter is available.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBluetoothAvailable() {
        // Bluetooth is available only if the adapter exists
        return mBluetoothAdapter != null;
    }

    /**
     * Check for RFCOMM security.
     *
     * @return True if RFCOMM security is implemented.
     */
    private boolean btIsSecure() {
        // Establish if RFCOMM security is implemented, in which case we'll
        // use the insecure variants of the RFCOMM functions
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * Create the RFCOMM bluetooth socket.
     *
     * @return BluetoothSocket object.
     */
    private BluetoothSocket createSocket(BluetoothDevice device, UUID transport) {
        if (mShowDebugLogs) {
            Log.d(TAG, "Creating Bluetooth socket for device " + device.getAddress() + " using UUID " + transport);
        }

        try {
            if (btIsSecure()) {
                return device.createInsecureRfcommSocketToServiceRecord(transport);
            } else {
                return device.createRfcommSocketToServiceRecord(transport);
            }
        } catch (IOException e) {
            if (mShowDebugLogs) {
                Log.w(TAG, "Exception occurs while creating Bluetooth socket: " + e.toString());
            }

            try {
                // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                //noinspection RedundantArrayCreation
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                // noinspection UnnecessaryBoxing
                return (BluetoothSocket) method.invoke(device, Integer.valueOf(1));
            } catch (Exception e1) {
                // NoSuchMethodException from method getMethod: impossible to retrieve the method.
                // IllegalArgumentException from method invoke: problem with arguments which don't match with
                // expectations.
                // IllegalAccessException from method invoke: if invoked object is not accessible.
                // InvocationTargetException from method invoke: Exception thrown by the invoked method.
                if (mShowDebugLogs) {
                    Log.w(TAG, "Exception occurs while creating Bluetooth socket by invoking method: " + e.toString());
                }
            }
        }

        return null;
    }

    /**
     * Indicates that the connection attempt failed and notifies the listener.
     */
    private void onConnectionFailed() {
        // update connection state
        setState(State.DISCONNECTED);
        // Dispatch the corresponding failure message
        onConnectionError(Errors.CONNECTION_FAILED);
    }

    /**
     * Indicates that the connection was lost and notifies the listener.
     */
    private void onConnectionLost() {
        // update connection state
        setState(State.DISCONNECTED);
        // Dispatch the corresponding failure message
        onConnectionError(Errors.CONNECTION_LOST);
    }

    /**
     * <p>This method is called when the BluetoothSocket had successfully connected to a BluetoothDevice.</p>
     * <p>This method cancel the Connection and Communication Threads if they exist and creates a
     * new instance of CommunicationThread in order to listen for incoming messages from the connected device.</p>
     *
     * @param socket
     *          The socket used to run the successful connection.
     */
    private void onSocketConnected(BluetoothSocket socket) {
        Log.i(TAG, "Successful connection to device: " + getDevice().getAddress());

        if (mShowDebugLogs) {
            Log.d(TAG, "Initialisation of ongoing communication by creating and running a CommunicationThread.");
        }
        // Cancel the thread that completed the connection
        cancelConnectionThread();
        // Cancel any thread currently running a connection
        cancelCommunicationThread();

        // the Bluetooth connection is now established
        setState(State.CONNECTED);

        mCommunicationThread = new CommunicationThread(socket);
        mCommunicationThread.start();
    }


    // ====== INNER CLASS ==================================================================================

    /**
     * <p>Thread to use in order to connect a BluetoothDevice using a BluetoothSocket.</p>
     * <p>The connection to a BluetoothDevice using a BluetoothSocket is synchronous but can take time. To avoid to
     * block the current Thread of the application - in general the UI Thread - the connection runs in its own
     * Thread.</p>
     */
    private class ConnectionThread extends Thread {

        /**
         * The Bluetooth socket to use to connect to the remote device.
         */
        private final BluetoothSocket mmConnectorSocket;
        /**
         * <p>The tag to display for logs of this Thread.</p>
         */
        private final String THREAD_TAG = "ConnectionThread";

        /**
         * <p>To create a new instance of this class.</p>
         *
         * @param socket
         *          The necessary Bluetooth socket for this Thread to connect with a device.
         */
        private ConnectionThread(@NonNull BluetoothSocket socket) {
            setName(THREAD_TAG + getId());
            mmConnectorSocket = socket;
        }

        @Override
        public void run() {
            try {
                if (mShowDebugLogs) {
                    Log.d(THREAD_TAG, "Attempt to connect device over BR/EDR: " + mDevice.getAddress()
                            + " using " + (mUUIDTransport.equals(UUIDs.SPP) ? "SPP" : "GAIA"));
                }
                // Cancel discovery otherwise it slows down the connection.
                mBluetoothAdapter.cancelDiscovery();
                // Connect to the remote device through the socket.
                // This call blocks until it succeeds or throws an exception.
                mmConnectorSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                if (mShowDebugLogs) {
                    Log.w(THREAD_TAG, "Exception while connecting: " + connectException.toString());
                }
                try {
                    mmConnectorSocket.close();
                } catch (IOException closeException) {
                    Log.w(THREAD_TAG, "Could not close the client socket", closeException);
                }
                onConnectionFailed();
                mConnectionThread = null;
                return;
            }

            // connection succeeds
            onSocketConnected(mmConnectorSocket);
        }

        /**
         * To cancel this thread if it was running.
         */
        private void cancel() {
            // stop the thread if still running
            // because the BluetoothSocket.connect() method is synchronous the only way to stop this Thread is to use
            // the interrupt() method even if it is not recommended.
            interrupt();
        }
    }

    /**
     * <p>Thread to use in order to listen for incoming message from a connected BluetoothDevice.</p>
     * <p>To get messages from a remote device connected using a BluetoothSocket, an application has to constantly
     * read bytes over the InputStream of the BluetoothSocket. In order to avoid to block the current Thread of an
     * application - usually the UI Thread - it is recommended to do it in its own thread.</p>
     */
    private class CommunicationThread extends Thread {
        /**
         * The InputStream object to read bytes from in order to get messages from a connected remote device.
         */
        private final InputStream mmInputStream;
        /**
         * The OutputStream object to write bytes on in  order to send messages to a connected remote device.
         */
        private final OutputStream mmOutputStream;
        /**
         * The BluetoothSocket which has successfully been connected to a BluetoothDevice.
         */
        private final BluetoothSocket mmSocket;
        /**
         * To constantly read messages coming from the remote device.
         */
        private boolean mmIsRunning = false;
        /**
         * The tag to display for logs of this Thread.
         */
        private final String THREAD_TAG = "CommunicationThread";

        /**
         * <p>To create a new instance of this class.</p>
         * <p>This constructor will initialized all its private field depending on the given BluetoothSocket.</p>
         *
         * @param socket
         *          A BluetoothSocket which has successfully been connected to a BluetoothDevice.
         */
        CommunicationThread(@NonNull BluetoothSocket socket) {
            setName("CommunicationThread" + getId());
            mmSocket = socket;

            // temporary object to get the Bluetooth socket input and output streams as they are final
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(THREAD_TAG, "Error occurred when getting input and output streams", e);
            }

            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;
        }


        @Override // Thread
        public void run() {
            if (mmInputStream == null) {
                Log.w(THREAD_TAG, "Run thread failed: InputStream is null.");
                disconnect();
                return;
            }

            if (mmOutputStream == null) {
                Log.w(THREAD_TAG, "Run thread failed: OutputStream is null.");
                disconnect();
                return;
            }

            if (mmSocket == null) {
                Log.w(THREAD_TAG, "Run thread failed: BluetoothSocket is null.");
                disconnect();
                return;
            }

            if (!mmSocket.isConnected()) {
                Log.w(THREAD_TAG, "Run thread failed: BluetoothSocket is not connected.");
                disconnect();
                return;
            }

            // all check passed successfully, the listening can start
            listenStream();
        }

        /**
         * <p>This method runs the constant read of the InputStream in order to get messages from the connected
         * remote device.</p>
         */
        private void listenStream() {
            final int MAX_BUFFER = 1024;
            byte[] buffer = new byte[MAX_BUFFER];

            if (mShowDebugLogs) {
                Log.d(THREAD_TAG, "Start to listen for incoming streams.");
            }

            mmIsRunning = true;
            // to inform subclass it is possible to communicate with the device
            onCommunicationRunning();

            while (mState == BREDRProvider.State.CONNECTED && mmIsRunning) {
                int length;
                try {
                    length = mmInputStream.read(buffer);
                } catch (IOException e) {
                    Log.e(THREAD_TAG, "Reception of data failed: exception occurred while reading: " + e.toString());
                    mmIsRunning = false;
                    if (mState == BREDRProvider.State.CONNECTED) {
                        onConnectionLost();
                    }
                    mCommunicationThread = null;
                    break;
                }

                // if buffer contains some bytes, they are sent to the listener
                if (length > 0) {
                    byte[] data = new byte[length];
                    System.arraycopy(buffer, 0, data, 0, length);
                    if (mShowDebugLogs) {
                        Log.d(THREAD_TAG, "Reception of data: " + Utils.getStringFromBytes(data));
                    }
                    onDataFound(data);
                }
            }

            if (mShowDebugLogs) {
                Log.d(THREAD_TAG, "Stop to listen for incoming streams.");
            }
        }

        /**
         * <p>To write some data on the OutputStream in order to send it to a connected remote device.</p>
         *
         * @param data
         *              the data to send.
         *
         * @return true, if the data had successfully been writing on the OutputStream.
         */
        /*package*/ boolean sendStream(byte[] data) {
            if (mShowDebugLogs) {
                Log.d(TAG, "Process sending of data to the device starts");
            }

            if (mmSocket == null) {
                Log.w(THREAD_TAG, "Sending of data failed: BluetoothSocket is null.");
                return false;
            }

            if (!mmSocket.isConnected()) {
                Log.w(THREAD_TAG, "Sending of data failed: BluetoothSocket is not connected.");
                return false;
            }

            if (mState != BREDRProvider.State.CONNECTED) {
                Log.w(THREAD_TAG, "Sending of data failed: Provider is not connected.");
                return false;
            }

            if (mmOutputStream == null) {
                Log.w(THREAD_TAG, "Sending of data failed: OutputStream is null.");
                return false;
            }

            try {
                mmOutputStream.write(data);
                // flush the data to make sure the packet is sent immediately.
                // this is less efficient for the Android application, but helpful for ADK applications.
                // The sendStream() method can be called more than once before the write() method sends the packet
                // from the previous call. The sending is processed asynchronously for system efficiency. If the
                // write method is called more than once before the sending is done, all packets are buffered and
                // are sent using only one message.
                // ADK handles a packet per message sent faster than a message which contains more than one packet.
                mmOutputStream.flush();
            } catch (IOException e) {
                Log.w(THREAD_TAG, "Sending of data failed: Exception occurred while writing data: " + e.toString());
                return false;
            }

            if (mShowDebugLogs) {
                Log.d(TAG, "Success sending of data.");
            }

            return true;
        }

        /**
         * To cancel this thread if it was running.
         */
        /*package*/ void cancel() {
            if (mShowDebugLogs) {
                Log.d(TAG, "Thread is cancelled.");
            }

            mmIsRunning = false;

            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.w(THREAD_TAG, "Cancellation of the Thread: Close of BluetoothSocket failed: " + e.toString());
            }
        }

    }

}
