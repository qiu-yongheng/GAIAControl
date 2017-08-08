/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

/**
 * The data structure to define characteristic and descriptor requests. A request is defined by the call to the
 * synchronized methods of the {@link android.bluetooth.BluetoothGatt BluetoothGatt} object.
 */
@SuppressWarnings({"WeakerAccess"})
class Request {

    // ====== PRIVATE FIELDS =======================================================================

    /**
     * The type of the request.
     */
    @RequestType private final int mType;
    /**
     * If this request is about a characteristic, the Bluetooth characteristic for this request.
     */
    private final BluetoothGattCharacteristic mCharacteristic;
    /**
     * If this request is about a descriptor, the descriptor for this request.
     */
    private final BluetoothGattDescriptor mDescriptor;
    /**
     * If this request needs a boolean value, the value for this request.
     */
    private final boolean mBooleanData;
    /**
     * The number of attempts this request has been tried.
     */
    private int mAttempts = 0;
    /**
     * The data which should be used for this request.
     */
    private final byte[] mData;


    // ====== ENUM =======================================================================

    /**
     * All types of characteristic requests which can be asked to communicate with a Bluetooth device.
     */
    @IntDef(flag = true, value = { RequestType.CHARACTERISTIC_NOTIFICATION, RequestType.READ_CHARACTERISTIC,
            RequestType.READ_DESCRIPTOR, RequestType.WRITE_CHARACTERISTIC, RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC,
            RequestType.WRITE_DESCRIPTOR, RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING, RequestType.READ_RSSI })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface RequestType {
        /**
         * <p>This request type describes the request from this device to the remote device to be notified of any
         * change for the given characteristic. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)
         * setCharacteristicNotification} method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createCharacteristicNotificationRequest(BluetoothGattCharacteristic, boolean)
         * createCharacteristicNotificationRequest} static method. It has to provide the BluetoothGattCharacteristic
         * to use for this request as much as a boolean to turn on or off these notifications.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)
         * setCharacteristicNotification} method, please use
         * {@link Request#buildNotifyCharacteristic() buildNotifyCharacteristic} to get the characteristic parameter and
         * {@link Request#getBooleanData() getBooleanData} to get the related boolean.</p>
         */
        int CHARACTERISTIC_NOTIFICATION = 0;
        /**
         * <p>This request type describes the request from this device to the remote device to read the values of the
         * given characteristic. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic) readCharacteristic}
         * method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createReadCharacteristicRequest(BluetoothGattCharacteristic) createReadCharacteristicRequest}
         * static method. The BluetoothGattCharacteristic to use for this request has to be provided.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic) readCharacteristic},
         * please use {@link Request#buildReadCharacteristic() buildReadCharacteristic} to get the characteristic
         * parameter.</p>
         */
        int READ_CHARACTERISTIC = 1;
        /**
         * <p>This request type describes the request from this device to the remote device to write values on the
         * given characteristic. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic}
         * method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createWriteCharacteristicRequest(BluetoothGattCharacteristic, byte[])
         * createWriteCharacteristicRequest} static method. The BluetoothGattCharacteristic to use for this request has
         * to be provided as much as the data to write.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic},
         * please use {@link Request#buildWriteCharacteristic() buildWriteCharacteristic} to get the characteristic
         * parameter.</p>
         */
        int WRITE_CHARACTERISTIC = 2;
        /**
         * <p>This request type describes the request from this device to the remote device to write values on the
         * given characteristic without response required. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic}
         * method where the characteristic has been definied with the write type
         * {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE WRITE_TYPE_NO_RESPONSE}.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createWriteNoResponseCharacteristicRequest(BluetoothGattCharacteristic, byte[])
         * createWriteNoResponseCharacteristicRequest} static method. The BluetoothGattCharacteristic to use for this
         * request has to be provided as much as the data to write.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic},
         * please use {@link Request#buildWriteNoResponseCharacteristic() buildWriteNoResponseCharacteristic} to get the
         * characteristic parameter.</p>
         */
        int WRITE_NO_RESPONSE_CHARACTERISTIC = 3;
        /**
         * <p>This request type describes the request from this device to the remote device to read values on the
         * given descriptor. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#readDescriptor(BluetoothGattDescriptor) readDescriptor} method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createReadDescriptorRequest(BluetoothGattDescriptor) createReadDescriptorRequest} static
         * method. The BluetoothGattDescriptor to use for this request has to be provided.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#readDescriptor(BluetoothGattDescriptor) readDescriptor} method,
         * please use {@link Request#buildReadDescriptor() buildReadDescriptor} to get the descriptor parameter.</p>
         */
        int READ_DESCRIPTOR = 4;
        /**
         * <p>This request type describes the request from this device to the remote device to write values on the
         * given descriptor. This request is used to define the call to the
         * {@link android.bluetooth.BluetoothGatt#writeDescriptor(BluetoothGattDescriptor) writeDescriptor} method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createWriteDescriptorRequest(BluetoothGattDescriptor, byte[]) createWriteDescriptorRequest}
         * static method. The BluetoothGattDescriptor to use for this request has to be provided as much as the data
         * to write.</p>
         * <p>To get the needed parameters for the
         * {@link android.bluetooth.BluetoothGatt#writeDescriptor(BluetoothGattDescriptor) writeDescriptor} method,
         * please use {@link Request#buildWriteDescriptor() buildWriteDescriptor} to get the descriptor parameter.</p>
         */
        int WRITE_DESCRIPTOR = 5;
        /**
         * <p>This request works as the {@link #READ_CHARACTERISTIC} request.</p>
         * <p>This request is used instead of the READ_CHARACTERISTIC request to know that we attempt to induce the
         * pairing using a read characteristic request.</p>
         */
        int READ_CHARACTERISTIC_TO_INDUCE_PAIRING = 6;
        /**
         * <p>This request type describes the request from this device to read the RSSI of a remote device. This
         * request is used to define the call to the
         * {@link BluetoothGatt#readRemoteRssi() readRemoteRssi} method.</p>
         * <p>To use this type of request, a {@link Request Request} object has to be created through the
         * {@link Request#createReadRssiRequest() createReadRssiRequest} static method. This type of Request does not
         * have any parameter to configure in order to be used so it doesn't request any parameter.</p>
         */
        int READ_RSSI = 7;
    }


    // ====== STATIC METHODS =======================================================================

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#CHARACTERISTIC_NOTIFICATION
     * CHARACTERISTIC_NOTIFICATION}. This will allow building of information for the call to the
     * {@link android.bluetooth.BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)
     * setCharacteristicNotification} method.</p>
     *
     * @param characteristic
     *          The characteristic the device should enable or disable the notification for.
     * @param notify
     *          True to activate notification on the given characteristic, false otherwise.
     *
     * @return A new Request object of type {@link RequestType#CHARACTERISTIC_NOTIFICATION CHARACTERISTIC_NOTIFICATION}.
     */
    @NonNull
    public static Request createCharacteristicNotificationRequest(@NonNull BluetoothGattCharacteristic characteristic,
                                                                  boolean notify) {
        return new Request(Request.RequestType.CHARACTERISTIC_NOTIFICATION, characteristic, null, null, notify);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#READ_CHARACTERISTIC
     * READ_CHARACTERISTIC}. This will allow building of information for the call to the
     * {@link android.bluetooth.BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic) readCharacteristic}
     * method.</p>
     *
     * @param characteristic
     *          The characteristic to read the values from.
     *
     * @return A new Request object of type {@link RequestType#READ_CHARACTERISTIC READ_CHARACTERISTIC}.
     */
    @NonNull
    public static Request createReadCharacteristicRequest(@NonNull BluetoothGattCharacteristic characteristic) {
        return new Request(RequestType.READ_CHARACTERISTIC, characteristic, null, null, false);
    }

    /**
     * <p>This static method allows creation of a request of the type
     * {@link RequestType#READ_CHARACTERISTIC_TO_INDUCE_PAIRING READ_CHARACTERISTIC_TO_INDUCE_PAIRING}. This will
     * allow building information for the call to the
     * {@link android.bluetooth.BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic) readCharacteristic}
     * method.</p>
     * <p>The {@link RequestType#READ_CHARACTERISTIC_TO_INDUCE_PAIRING READ_CHARACTERISTIC_TO_INDUCE_PAIRING}
     * request is used to induce the pairing. This type of request is attempted only once, this means that the object
     * creating this request should set the attempts number to the maximum it accepts to run the request once.</p>
     *
     * @param characteristic
     *          The characteristic that needs pairing to read the values from.
     *
     * @return A new Request object of type
     *      {@link RequestType#READ_CHARACTERISTIC_TO_INDUCE_PAIRING READ_CHARACTERISTIC_TO_INDUCE_PAIRING}.
     */
    @NonNull
    public static Request createReadCharacteristicRequestToInducePairing(@NonNull BluetoothGattCharacteristic
                                                                               characteristic) {
        return new Request(RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING, characteristic, null, null, false);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#READ_DESCRIPTOR READ_DESCRIPTOR}.
     * This will allow building of information for the call to the
     * {@link android.bluetooth.BluetoothGatt#readDescriptor(BluetoothGattDescriptor) readDescriptor} method.</p>
     *
     * @param descriptor
     *          The descriptor to read values from.
     *
     * @return A new Request object of type {@link RequestType#READ_DESCRIPTOR READ_DESCRIPTOR}.
     */
    @SuppressWarnings("unused") // implementation dependant
    @NonNull
    public static Request createReadDescriptorRequest(@NonNull BluetoothGattDescriptor descriptor) {
        return new Request(RequestType.READ_DESCRIPTOR, null, descriptor, null, false);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#WRITE_CHARACTERISTIC
     * WRITE_CHARACTERISTIC}. This will allow building of information for the call to the
     * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic}
     * method.</p>
     *
     * @param characteristic
     *          The characteristic to write values on.
     * @param data
     *          The data to write.
     *
     * @return A new Request object of type {@link RequestType#WRITE_CHARACTERISTIC WRITE_CHARACTERISTIC}.
     */
    @NonNull
    public static Request createWriteCharacteristicRequest(@NonNull BluetoothGattCharacteristic characteristic,
                                                           @NonNull byte[] data) {
        return new Request(RequestType.WRITE_CHARACTERISTIC, characteristic, null, data, false);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#WRITE_NO_RESPONSE_CHARACTERISTIC
     * WRITE_NO_RESPONSE_CHARACTERISTIC}. This will allow building information for the call to the
     * {@link android.bluetooth.BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic) writeCharacteristic}
     * method with the write type {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE WRITE_TYPE_NO_RESPONSE}.</p>
     *
     * @param characteristic
     *          The characteristic to write values on.
     * @param data
     *          The data to write.
     *
     * @return A new Request object of type
     * {@link RequestType#WRITE_NO_RESPONSE_CHARACTERISTIC WRITE_NO_RESPONSE_CHARACTERISTIC}.
     */
    @NonNull
    public static Request createWriteNoResponseCharacteristicRequest(@NonNull BluetoothGattCharacteristic
                                                                                 characteristic,
                                                           @NonNull byte[] data) {
        return new Request(RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC, characteristic, null, data, false);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#WRITE_DESCRIPTOR
     * WRITE_DESCRIPTOR}. This will allow building of information for the call to the
     * {@link android.bluetooth.BluetoothGatt#writeDescriptor(BluetoothGattDescriptor) writeDescriptor}
     * method.</p>
     *
     * @param descriptor
     *          The descriptor to write values on.
     * @param data
     *          The data to write.
     *
     * @return A new Request object of type {@link RequestType#WRITE_DESCRIPTOR WRITE_DESCRIPTOR}.
     */
    @NonNull
    public static Request createWriteDescriptorRequest(@NonNull BluetoothGattDescriptor descriptor,
                                                       @NonNull byte[] data) {
        return new Request(RequestType.WRITE_DESCRIPTOR, null, descriptor, data, false);
    }

    /**
     * <p>This static method allows creation of a request of the type {@link RequestType#READ_RSSI READ_RSSI}.
     * This request is used to call the {@link BluetoothGatt#readRemoteRssi() readRemoteRssi} method and does not
     * need any parameter.</p>
     *
     * @return A new Request object of type {@link RequestType#READ_RSSI READ_RSSI}.
     */
    @NonNull
    public static Request createReadRssiRequest() {
        return new Request(RequestType.READ_RSSI, null, null, null, false);
    }

    /**
     * <p>To get a human readable label value for the request types.</p>
     *
     * @param type
     *          The type to get a label.
     *
     * @return A human readable value for the given Request type.
     */
    public static String getRequestTypeLabel(@RequestType int type) {
        switch (type) {
            case RequestType.CHARACTERISTIC_NOTIFICATION:
                return "CHARACTERISTIC_NOTIFICATION";
            case RequestType.READ_CHARACTERISTIC:
                return "READ_CHARACTERISTIC";
            case RequestType.READ_DESCRIPTOR:
                return "READ_DESCRIPTOR";
            case RequestType.WRITE_CHARACTERISTIC:
                return "WRITE_CHARACTERISTIC";
            case RequestType.WRITE_DESCRIPTOR:
                return "WRITE_DESCRIPTOR";
            case RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING:
                return "READ_CHARACTERISTIC_TO_INDUCE_PAIRING";
            case RequestType.READ_RSSI:
                return "READ_RSSI";
        }
        return "UNKNOWN " + type;
    }


    // ====== CONSTRUCTORS =======================================================================

    /**
     * <p>The main constructor of this class.</p>
     * <p>This constructor is private, please use the static create methods to create a new object from this class.</p>
     *
     * @param type
     *          The type of request
     * @param characteristic
     *          The characteristic, if required, attached to the request.
     * @param descriptor
     *          The descriptor, if required, attached to this request.
     * @param data
     *          The data, if required, for this request.
     * @param booleanData
     *          The boolean value, if required, for this request.
     */
    private Request(@RequestType int type, BluetoothGattCharacteristic characteristic,
                   BluetoothGattDescriptor descriptor, byte[] data, boolean booleanData) {
        // type, characteristic, descriptor, data, booleanData
        this.mType = type;
        this.mCharacteristic = characteristic;
        this.mDescriptor = descriptor;
        this.mData = data;
        this.mBooleanData = booleanData;
    }


    // ====== SPECIFIC GETTERS =======================================================================

    /**
     * <p>This method allows building of the characteristic information for the {@link RequestType#WRITE_CHARACTERISTIC
     * WRITE_CHARACTERISTIC}. This method will add the data given when creating the request to the given
     * characteristic and will return that characteristic.</p>
     *
     * @return The attached characteristic with the data set as the characteristic value. If the request is not a
     *          {@link RequestType#WRITE_CHARACTERISTIC WRITE_CHARACTERISTIC} request, this
     *          method returns null.
     */
    public BluetoothGattCharacteristic buildWriteCharacteristic() {
        if (mType == RequestType.WRITE_CHARACTERISTIC && mCharacteristic != null
                    && (mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            if (mData != null) mCharacteristic.setValue(mData);
            mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return mCharacteristic;
        }
        else {
            return null;
        }
    }

    /**
     * <p>This method allows building of the characteristic information for the {@link RequestType#WRITE_CHARACTERISTIC
     * WRITE_CHARACTERISTIC}. This method will add the data given when created the request to the given
     * characteristic and will return that characteristic.</p>
     *
     * @return The attached characteristic with the data sets as the characteristic value. If the request is not a
     *          {@link RequestType#WRITE_CHARACTERISTIC WRITE_CHARACTERISTIC} request, this
     *          method returns null.
     */
    public BluetoothGattCharacteristic buildWriteNoResponseCharacteristic() {
        if (mType == RequestType.WRITE_NO_RESPONSE_CHARACTERISTIC && mCharacteristic != null
                && (mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            if (mData != null) mCharacteristic.setValue(mData);
            mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            return mCharacteristic;
        }
        else {
            return null;
        }
    }

    /**
     * <p>This method allows building of the characteristic information for the {@link RequestType#READ_CHARACTERISTIC
     * READ_CHARACTERISTIC}. For a read characteristic request, there is nothing to add to the characteristic, so this
     * method returns the characteristic.</p>
     *
     * @return The attached characteristic. If the request is not a {@link RequestType#WRITE_CHARACTERISTIC
     *          WRITE_CHARACTERISTIC} request, this method returns null.
     */
    public BluetoothGattCharacteristic buildReadCharacteristic() {
        if (mType == RequestType.READ_CHARACTERISTIC
                || mType == RequestType.READ_CHARACTERISTIC_TO_INDUCE_PAIRING) {
            return mCharacteristic;
        }
        else {
            return null;
        }
    }

    /**
     * <p>This method allows building of the characteristic information for the
     * {@link RequestType#CHARACTERISTIC_NOTIFICATION CHARACTERISTIC_NOTIFICATION}. For a notification request,
     * there is nothing to add to the characteristic as the attached boolean data will be used by calling
     * {@link #getBooleanData() getBooleanData} method.</p>
     *
     * @return The attached characteristic. If the request is not a {@link RequestType#CHARACTERISTIC_NOTIFICATION
     *          CHARACTERISTIC_NOTIFICATION} request, this method returns null.
     */
    public BluetoothGattCharacteristic buildNotifyCharacteristic() {
        if (mType == RequestType.CHARACTERISTIC_NOTIFICATION) {
            // the value (booleanData) is set directly in the BluetoothGatt method "setCharacteristicNotification"
            return mCharacteristic;
        }
        else {
            return null;
        }
    }

    /**
     * <p>This method allows building of the descriptor information for the {@link RequestType#WRITE_DESCRIPTOR
     * WRITE_DESCRIPTOR}. This method will add the data given when creating the request to the given
     * descriptor and will return that descriptor.</p>
     *
     * @return The attached descriptor with the data sets as the descriptor value. If the request is not a
     *          {@link RequestType#WRITE_DESCRIPTOR WRITE_DESCRIPTOR} request, this
     *          method returns null.
     */
    public BluetoothGattDescriptor buildWriteDescriptor() {
        if (mType == RequestType.WRITE_DESCRIPTOR) {
            if (mData != null) mDescriptor.setValue(mData);
            return mDescriptor;
        }
        else {
            return null;
        }
    }

    /**
     * <p>This method allows building of the descriptor information for the {@link RequestType#READ_DESCRIPTOR
     * READ_DESCRIPTOR}. This method will add the data given when creating the request to the given
     * descriptor and will return that descriptor.</p>
     *
     * @return The attached descriptor. If the request is not a {@link RequestType#READ_DESCRIPTOR READ_DESCRIPTOR}
     *          request, this method returns null.
     */
    @SuppressWarnings("unused") // implementation dependant
    public BluetoothGattDescriptor buildReadDescriptor() {
        if (mType == RequestType.READ_DESCRIPTOR) {
            return mDescriptor;
        }
        else {
            return null;
        }
    }


    // ====== GETTERS =======================================================================

    /**
     * <p>To get the number of times this request has been attempted.</p>
     *
     * @return the number of attempts.
     */
    public int getAttempts() {
        return mAttempts;
    }

    /**
     * <p>To get the type of this request.</p>
     *
     * @return the type of the request.
     */
    public @RequestType int getType() {
        return mType;
    }

    /**
     * <p>To get the attached characteristic.</p>
     *
     * @return the characteristic which has been attached to this request. If the request is not a request containing a
     * characteristic such as {@link RequestType#WRITE_CHARACTERISTIC WRITE_CHARACTERISTIC},
     * {@link RequestType#READ_CHARACTERISTIC_TO_INDUCE_PAIRING READ_CHARACTERISTIC_TO_INDUCE_PAIRING},
     * {@link RequestType#READ_CHARACTERISTIC READ_CHARACTERISTIC} or {@link RequestType#CHARACTERISTIC_NOTIFICATION
     * CHARACTERISTIC_NOTIFICATION}, this method will return null.
     */
    public BluetoothGattCharacteristic getCharacteristic() {
        return mCharacteristic;
    }

    /**
     * <p>To get the attached descriptor.</p>
     *
     * @return the descriptor which has been attached to this request. If the request is not a request containing a
     * descriptor such as
     * {@link RequestType#WRITE_DESCRIPTOR WRITE_DESCRIPTOR} or {@link RequestType#READ_DESCRIPTOR READ_DESCRIPTOR},
     * this method will return null.
     */
    public BluetoothGattDescriptor getDescriptor() {
        return mDescriptor;
    }

    /**
     * <p>To get the boolean data given when creating the request.</p>
     *
     * @return the boolean data which has been attached to this request. If the request is not a request containing
     * boolean data such as {@link RequestType#CHARACTERISTIC_NOTIFICATION CHARACTERISTIC_NOTIFICATION}, this method
     * will return false.
     */
    public boolean getBooleanData() {
        return mBooleanData;
    }


    // ====== SETTERS =======================================================================

    /**
     * <p>To set the number of attempts to a specific value.</p>
     *
     * @param attempts the number to set up the number of attempts of this request.
     */
    @SuppressWarnings("SameParameterValue") // this could change using any other implementation
    public void setAttempts(int attempts) {
        this.mAttempts = attempts;
    }

    /**
     * <p>To increase the number of attempts by 1.</p>
     */
    public void increaseAttempts() {
        mAttempts++;
    }

}
