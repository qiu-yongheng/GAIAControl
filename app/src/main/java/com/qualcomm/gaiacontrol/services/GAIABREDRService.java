/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.models.gatt.GATTServices;
import com.qualcomm.gaiacontrol.receivers.BondStateReceiver;
import com.qualcomm.libraries.ble.BLEUtils;
import com.qualcomm.libraries.vmupgrade.UpgradeManager;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This {@link Service Service} provide the required BR/EDR connection with a device in order to
 * communicate with a device using the GAIA protocol.</p>
 * <p>In order to provide a BR/EDR connection with a device, this service instances the
 * {@link BREDRProvider BREDRProvider} class.</p>
 * <p>This Service also implements the upgrade process of a connected GAIA device.</p>
 */
public class GAIABREDRService extends Service implements BluetoothService, BondStateReceiver.BondStateListener {

    // ====== CONSTS FIELDS ========================================================================

    /**
     * To know if we are using the application in the debug mode.
     */
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "GAIABREDRService";


    // ====== PRIVATE FIELDS =======================================================================

    /**
     * <p>The different listeners which are used to communicate with the application.</p>
     */
    private final List<Handler> mAppListeners = new ArrayList<>();
    /**
     * <p>The binder to return to the instance which will bind this service.</p>
     */
    private final IBinder mBinder = new LocalBinder();
    /**
     * To keep the instance of the bond state receiver in order to unregister it.
     */
    private final BondStateReceiver mBondStateReceiver = new BondStateReceiver(this);
    /**
     * The handler which will receive messages from a BREDRProvider instance.
     */
    private final ProviderHandler mProviderHandler = new ProviderHandler(this);
    /**
     * The Provider of a BR/EDR connection with a BluetoothDevice able to detect GAIA packets.
     */
    private GAIABREDRProvider mGAIABREDRProvider;


    // ====== SERVICE METHODS ========================================================================

    @Override // Service
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "Service bound");
        registerBondReceiver();
        return mBinder;
    }

    @Override // Service
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.i(TAG, "Service unbound");
        unregisterBondReceiver();
        if (mAppListeners.isEmpty()) {
            disconnectDevice();
        }

        return super.onUnbind(intent);
    }

    @Override // Service
    public void onCreate() {
        super.onCreate();
        if (mGAIABREDRProvider == null) {
            mGAIABREDRProvider = new GAIABREDRProvider(mProviderHandler,
                    (BluetoothManager) getSystemService(BLUETOOTH_SERVICE));
        }
        mGAIABREDRProvider.showDebugLogs(DEBUG);
    }

    /*
     * The system calls this method when the service is no longer used and is being destroyed. Your service should
     * implement this to clean up any resources such as threads, registered listeners, receivers, etc. This is the last
     * call the service receives.
     */
    @Override // Service
    public void onDestroy() {
        disconnectDevice();
        if (DEBUG) Log.i(TAG, "Service destroyed");
        super.onDestroy();
    }

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


    // ====== CONNECTION METHODS ========================================================================

    @Override // BluetoothService
    public boolean connectToDevice(String address) {
        if (mGAIABREDRProvider.getState() == BREDRProvider.State.CONNECTED) {
            Log.w(TAG, "connection failed: a device is already connected.");
            return false;
        }

        return mGAIABREDRProvider.connect(address);
    }

    @Override // BluetoothService
    public boolean reconnectToDevice() {
        return mGAIABREDRProvider.reconnectToDevice();
        // then wait for mProviderHandler to get a message
    }

    @Override // BluetoothService
    public void disconnectDevice() {
        mGAIABREDRProvider.disconnect();
        // then wait for mProviderHandler to get a message
    }


    // ====== STATE METHODS ========================================================================

    @Override // BluetoothService
    public int getTransport() {
        return Transport.BR_EDR;
    }

    @Override // BluetoothService
    public int getBondState() {
        BluetoothDevice device = getDevice();
        return device != null ? device.getBondState() : BluetoothDevice.BOND_NONE;
    }

    @Override // BluetoothService
    public BluetoothDevice getDevice() {
        return mGAIABREDRProvider.getDevice();
    }

    @Override // BluetoothService
    public @State int getConnectionState() {
        switch(mGAIABREDRProvider.getState()) {
            case BREDRProvider.State.CONNECTED:
                return BluetoothService.State.CONNECTED;
            case BREDRProvider.State.CONNECTING:
                return BluetoothService.State.CONNECTING;
            case BREDRProvider.State.DISCONNECTING:
                return BluetoothService.State.DISCONNECTING;
            case BREDRProvider.State.DISCONNECTED:
            case BREDRProvider.State.NO_STATE:
            default:
                return BluetoothService.State.DISCONNECTED;
        }
    }


    // ====== GAIA METHODS ========================================================================

    @Override // BluetoothService
    public boolean sendGAIAPacket(byte[] packet) {
        return mGAIABREDRProvider.sendData(packet);
    }

    @Override // BluetoothService
    public boolean isGaiaReady() {
        return mGAIABREDRProvider.isGaiaReady();
    }


    // ====== UPGRADE METHODS ========================================================================

    @Override // BluetoothService
    public void startUpgrade(File file) {
        mGAIABREDRProvider.startUpgrade(file);
    }

    @Override // BluetoothService
    public @ResumePoints.Enum int getResumePoint() {
        return mGAIABREDRProvider.getResumePoint();
    }

    @Override // BluetoothService
    public void abortUpgrade() {
        mGAIABREDRProvider.abortUpgrade();
    }

    @Override // BluetoothService
    public boolean isUpgrading() {
        return mGAIABREDRProvider.isUpgrading();
    }

    @Override // BluetoothService
    public void sendConfirmation(@UpgradeManager.ConfirmationType int type, boolean confirmation) {
        mGAIABREDRProvider.sendConfirmation(type, confirmation);
    }





    // ====== GATT REQUESTS ======================================================================
    // ADK applications do not support GATT over BR/EDR

    @Override // BluetoothService
    public boolean isGattReady() {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public GATTServices getGattSupport() {
        // GATT not supported over BR/EDR
        return null;
    }

    @Override // BluetoothService
    public boolean requestLinkLossAlertLevel() {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean requestTxPowerLevel() {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean requestBatteryLevels() {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean requestBodySensorLocation() {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean sendLinkLossAlertLevel(int level) {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean sendImmediateAlertLevel(int level) {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean sendHeartRateControlPoint(byte control) {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean requestHeartMeasurementNotifications(boolean notify) {
        // GATT not supported over BR/EDR
        return false;
    }

    @Override // BluetoothService
    public boolean startRssiUpdates(boolean start) {
        // GATT not supported over BR/EDR
        return false;
    }


    // ====== OTHER OVERRIDE METHODS ======================================================================

    @Override // BondStateReceiver.BondStateListener
    public void onBondStateChange(BluetoothDevice device, int state) {
        // we expect this method to be called when the device has not been bonded yet
        BluetoothDevice connectedDevice = getDevice();
        if (device != null && connectedDevice != null && device.getAddress().equals(connectedDevice.getAddress())) {
            if (DEBUG) Log.i(TAG, "ACTION_BOND_STATE_CHANGED for " + device.getAddress()
                    + " with bond state " + BLEUtils.getBondStateName(state));

            sendMessageToListener(Messages.DEVICE_BOND_STATE_HAS_CHANGED, state);

            // if the device wasn't bonded yet we need to discover its UUIDs in order to connect over BR/EDR
            // According to the Android framework documentation (see BluetoothClass):
            // "Accurate service discovery is done through SDP requests, which are automatically performed when
            // creating an RFCOMM socket with createRfcommSocketToServiceRecord(UUID) and
            // listenUsingRfcommWithServiceRecord(String, UUID)"
            // However while testing for upgrade from a non bonded device, it didn't seem to happen so it is forced here
            if (state == BluetoothDevice.BOND_BONDED) {
                device.fetchUuidsWithSdp();
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
     *         Any object to send to the listener.
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
     * <p>To inform the listener by sending a message to it.</p>
     *
     * @param message
     *         The type of message to send.
     * @param subMessage
     *          Any complementary message to the message type.
     * @param object
     *         Any object to send to the listener.
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


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method is called when the {@link ProviderHandler ProviderHandler} receives a message from the
     * {@link BREDRProvider BREDRProvider}.</p>
     * <p>This method will act dependently of the received message.</p>
     *
     * @param msg
     *          The message received from the service.
     */
    private void handleMessageFromProvider(Message msg) {
        String handleMessage = "Handle a message from BR/EDR Provider: ";

        switch (msg.what) {
            case GAIABREDRProvider.Messages.CONNECTION_STATE_HAS_CHANGED:
                @BREDRProvider.State int receivedState = (int) msg.obj;
                if (DEBUG) {
                    String stateLabel = receivedState == BREDRProvider.State.CONNECTED ? "CONNECTED"
                            : receivedState == BREDRProvider.State.CONNECTING ? "CONNECTING"
                            : receivedState == BREDRProvider.State.DISCONNECTING ? "DISCONNECTING"
                            : receivedState == BREDRProvider.State.DISCONNECTED ? "DISCONNECTED"
                            : "UNKNOWN";
                    Log.i(TAG, handleMessage + "CONNECTION_STATE_HAS_CHANGED: " + stateLabel);
                }
                onConnectionStateHasChanged(receivedState);
                break;

            case GAIABREDRProvider.Messages.GAIA_PACKET:
                byte[] data = (byte[]) msg.obj;
                if (DEBUG) Log.i(TAG, handleMessage + "GAIA_PACKET");
                onGaiaDataReceived(data);
                break;

            case GAIABREDRProvider.Messages.GAIA_READY:
                if (DEBUG) Log.i(TAG, handleMessage + "GAIA_READY");
                sendMessageToListener(Messages.GAIA_READY);
                break;

            case GAIABREDRProvider.Messages.ERROR:
                int obj = (int) msg.obj;
                String error = obj == BREDRProvider.Errors.CONNECTION_FAILED ? "CONNECTION_FAILED" :
                        obj == BREDRProvider.Errors.CONNECTION_LOST ? "CONNECTION_LOST" : "UNKNOWN " + obj;
                Log.w(TAG, handleMessage + "ERROR: " + error);
                onErrorReceived(obj);
                break;

            case GAIABREDRProvider.Messages.UPGRADE_MESSAGE:
                @UpgradeMessage int upgradeMessage = msg.arg1;
                Object content = msg.obj;
                sendMessageToListener(Messages.UPGRADE_MESSAGE, upgradeMessage, content);
                break;

            default:
                if (DEBUG)
                    Log.d(TAG, handleMessage + "UNKNOWN MESSAGE: " + msg.what + " obj: " + msg.obj);
                break;
        }
    }

    /**
     * <p>This method is called when the Provider dispatches a
     * {@link GAIABREDRProvider.Messages#CONNECTION_STATE_HAS_CHANGED CONNECTION_STATE_HAS_CHANGED}
     * message.</p>
     * <p>This method dispatches the information to its listener(s).</p>
     * <p>If an upgrade is going on and the device is disconnected this method will ask to the BREDRProvider to
     * reconnect with the device.</p>
     *
     * @param receivedState
     *              The new connection state
     */
    private void onConnectionStateHasChanged(int receivedState) {
        @BluetoothService.State int state =
                receivedState == BREDRProvider.State.CONNECTED ? BluetoothService.State.CONNECTED :
                receivedState == BREDRProvider.State.CONNECTING ? BluetoothService.State.CONNECTING :
                        receivedState == BREDRProvider.State.DISCONNECTING ? BluetoothService.State.DISCONNECTING :
                                BluetoothService.State.DISCONNECTED;

        sendMessageToListener(Messages.CONNECTION_STATE_HAS_CHANGED, state);

        if (receivedState == BREDRProvider.State.DISCONNECTED && isUpgrading()) {
            reconnectToDevice();
        }
    }

    /**
     * <p>This method is called when this service receives a
     * {@link GAIABREDRProvider.Messages#ERROR ERROR} message from the BR/EDR
     * Connection provider.</p>
     * <p>This method will display a toast corresponding to the received error.</p>
     *
     * @param error
     *          The {@link BREDRProvider.Errors Errors} sent by the provider.
     */
    private void onErrorReceived(int error) {
        int message = R.string.toast_connection_error_unknown;
        switch (error) {
            case BREDRProvider.Errors.CONNECTION_FAILED:
                message = R.string.toast_connection_error_failed;
                break;
            case BREDRProvider.Errors.CONNECTION_LOST:
                message = R.string.toast_connection_error_lost;
                break;
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * <p>This method is called when the {@link GAIABREDRProvider GAIABREDRProvider} has built a GAIA packet from
     * incoming data from the connected device.</p>
     * <p>This method will dispatch the packet to its listeners if there is no active upgrade.</p>
     *
     * @param data
     *          The packet.
     */
    private void onGaiaDataReceived(byte[] data) {
        sendMessageToListener(Messages.GAIA_PACKET, data);
    }

    /**
     * <p>To register the bond stat receiver in order to be informed of any bond state change.</p>
     */
    private void registerBondReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        this.registerReceiver(mBondStateReceiver, filter);
    }

    /**
     * <p>To unregister the bond stat receiver when the application is stopped or it is not needed anymore.</p>
     */
    private void unregisterBondReceiver() {
        unregisterReceiver(mBondStateReceiver);
    }


    // ====== INNER CLASS ==========================================================================

    /**
     * <p>The class which allows an entity to communicate with this service when it is bound.</p>
     */
    public class LocalBinder extends Binder {
        /**
         * <p>To retrieve the binder service.</p>
         *
         * @return the service.
         */
        public GAIABREDRService getService() {
            return GAIABREDRService.this;
        }
    }

    /**
     * <p>This class allows to receive and manage messages from a {@link BREDRProvider BREDRProvider}.</p>
     */
    private static class ProviderHandler extends Handler {

        /**
         * The reference to this service.
         */
        final WeakReference<GAIABREDRService> mmReference;

        /**
         * The constructor for this handler.
         *
         * @param service
         *            this service.
         */
        ProviderHandler(GAIABREDRService service) {
            super();
            mmReference = new WeakReference<>(service);
        }

        @Override // Handler
        public void handleMessage(Message msg) {
            GAIABREDRService service = mmReference.get();
            service.handleMessageFromProvider(msg);
        }
    }

}
