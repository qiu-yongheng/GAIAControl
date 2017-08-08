/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.ble;

import android.bluetooth.BluetoothGatt;

/**
 * <p>This class encapsulates all known possible statuses which can be send to the library through the
 * {@link android.bluetooth.BluetoothGattCallback BluetoothGattCallback methods}.
 * <p>These statuses - except 0x00 for {@link HCI#SUCCESS HCI.SUCCESS} - are BLE error status and can correspond to any
 * of the layers which composed the Bluetooth communication.
 * <p>Some are given and explained in the {@link BluetoothGatt BluetoothGatt} class. However a lot of thrown statuses
 * do not match these and correspond to some HCI errors for instance.</p>
 */
@SuppressWarnings("WeakerAccess")
public final class ErrorStatus {

    /**
     * <p>This method builds a label which displays the name of the status errors which can be found in the
     * {@link BluetoothGatt BluetoothGatt} class.</p>
     * <p>If the <code>detailed</code> parameter is true, the label is as follows:
     * <blockquote><pre>FIELD_NAME: corresponding known information.</pre></blockquote>
     * Otherwise it only displays <code>FIELD_NAME</code>.</p>
     * <p>If there is no field matching the given status, this method returns "".</p>
     *
     * @param status
     *          The status to look for a name.
     * @param detailed
     *          To get complementary information about the status if found.
     *
     * @return A readable label corresponding to the status if it corresponds to one of the values in
     * {@link BluetoothGatt BluetoothGatt}.
     */
    public static String getBluetoothGattStatusLabel(int status, boolean detailed) {
        String message = "", details = "";

        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:                        // 0x00
                message = "GATT_SUCCESS";
                details = "A GATT operation completed successfully.";
                break;
            case BluetoothGatt.GATT_READ_NOT_PERMITTED:             // 0x02
                message = "GATT_READ_NOT_PERMITTED";
                details = "GATT read operation is not permitted.";
                break;
            case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:            // 0x03
                message = "GATT_WRITE_NOT_PERMITTED";
                details = "GATT write operation is not permitted.";
                break;
            case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:    // 0x05
                message = "GATT_INSUFFICIENT_AUTHENTICATION";
                details = "Insufficient authentication for a given operation.";
                break;
            case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:          // 0x06
                message = "GATT_REQUEST_NOT_SUPPORTED";
                details = "The given request is not supported.";
                break;
            case BluetoothGatt.GATT_INVALID_OFFSET:                 // 0x07
                message = "GATT_INVALID_OFFSET";
                details = "A read or write operation was requested with an invalid offset.";
                break;
            case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:       // 0x0d
                message = "GATT_INVALID_ATTRIBUTE_LENGTH";
                details = "A write operation exceeds the maximum length of the attribute.";
                break;
            case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:        // 0x0f
                message = "GATT_INSUFFICIENT_ENCRYPTION";
                details = "Insufficient encryption for a given operation.";
                break;
            case BluetoothGatt.GATT_CONNECTION_CONGESTED:           // 0x8f
                message = "GATT_CONNECTION_CONGESTED";
                details = "A remote device connection is congested.";
                break;
            case BluetoothGatt.GATT_FAILURE:                        // 0x0101
                message = "GATT_FAILURE";
                details = "A GATT operation failed, different error of the BluetoothGatt ones.";
                break;
        }

        return message.length() > 0 ?
                message + (detailed && (details.length() > 0) ? ": " + details : "")
                : "";
    }

    /**
     * <p>This class groups all error statuses which can be found in the Android framework source code and which can be
     * thrown through the {@link android.bluetooth.BluetoothGattCallback BluetoothGattCallback callbacks}. From the
     * source code these errors can be found under the C Header file <code>gatt_api.h</code>.</p>
     * <p>This class only includes the values which do not correspond to the ATT or the HCI layer.</p>
     * @link https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h
     */
    public static class GattApi {

        /**
         * <p>This method builds a label which displays the name of the status errors which match a value from the
         * {@link GattApi GattApi} class.</p>
         * <p>If the <code>detailed</code> parameter is true, the label is as follows:
         * <blockquote><pre>FIELD_NAME: corresponding known information.</pre></blockquote>
         * Otherwise it only displays <code>FIELD_NAME</code>.</p>
         * <p>If there is no field matching the given status, this method returns "".</p>
         *
         * @param status
         *          The status to look for a name.
         * @param detailed
         *          To get complementary information about the status if found.
         *
         * @return a readable label corresponding to the status if it matches the values of the
         * {@link GattApi GattApi} class.
         */
        public static String getLabel(int status, boolean detailed) {
            String message = "", details = "";

            switch (status) {
                case GattApi.GATT_NO_RESOURCES:                 // 0x80
                    message = "GATT_NO_RESOURCES";
                    break;
                case GattApi.GATT_INTERNAL_ERROR:               // 0x81
                    message = "GATT_INTERNAL_ERROR";
                    break;
                case GattApi.GATT_WRONG_STATE:                  // 0x82
                    message = "GATT_WRONG_STATE";
                    break;
                case GattApi.GATT_DB_FULL:                      // 0x83
                    message = "GATT_DB_FULL";
                    break;
                case GattApi.GATT_BUSY:                         // 0x84
                    message = "GATT_BUSY";
                    break;
                case GattApi.GATT_ERROR:                        // 0x85
                    message = "GATT_ERROR";
                    break;
                case GattApi.GATT_CMD_STARTED:                  // 0x86
                    message = "GATT_CMD_STARTED";
                    break;
                case GattApi.GATT_ILLEGAL_PARAMETER:            // 0x87
                    message = "GATT_ILLEGAL_PARAMETER";
                    break;
                case GattApi.GATT_PENDING:                      // 0x88
                    message = "GATT_PENDING";
                    break;
                case GattApi.GATT_AUTH_FAIL:                    // 0x89
                    message = "GATT_AUTH_FAIL";
                    break;
                case GattApi.GATT_MORE:                         // 0x8a
                    message = "GATT_MORE";
                    break;
                case GattApi.GATT_INVALID_CFG:                  // 0x8b
                    message = "GATT_INVALID_CFG";
                    break;
                case GattApi.GATT_SERVICE_STARTED:              // 0x8c
                    message = "GATT_SERVICE_STARTED";
                    break;
                case GattApi.GATT_ENCRYPED_NO_MITM:             // 0x8d
                    message = "GATT_ENCRYPED_NO_MITM";
                    break;
                case GattApi.GATT_NOT_ENCRYPTED:                // 0x8e
                    message = "GATT_NOT_ENCRYPTED";
                    break;
                case GattApi.GATT_CCC_CFG_ERR:                  // 0xFD
                    message = "GATT_CCC_CFG_ERR";
                    details = "Client Characteristic Configuration Descriptor improperly configured.";
                    break;
                case GattApi.GATT_PRC_IN_PROGRESS:              // 0xFE
                // case L2CAP_CONN_AMP_FAILED:                  // 254 from l2cdefs.h"
                    message = "GATT_PRC_IN_PROGRESS or L2CAP_CONN_AMP_FAILED from l2cdefs.h";
                    details = "Procedure already in progress for GATT_PRC_IN_PROGRESS.";
                    break;
                case GattApi.GATT_OUT_OF_RANGE:                 // 0xFF
                // case L2CAP_CONN_NO_LINK:                     // 255 from l2cdefs.h"
                    message = "GATT_OUT_OF_RANGE or L2CAP_CONN_NO_LINK from l2cdefs.h";
                    details = "Attribute value out of range for GATT_OUT_OF_RANGE.";
                    break;
                case GattApi.GATT_CONN_CANCEL:                  // 0x0100
                // case L2CAP_CONN_CANCEL:                      // 256 from l2cdefs.h"
                    message = "L2CAP_CONN_CANCEL";
                    details = "L2CAP connection cancelled";
                    break;
            }

            return message.length() > 0 ?
                    message + (detailed && (details.length() > 0) ? ": " + details : "")
                    : "";
        }

        public static final int GATT_NO_RESOURCES = 0x80;
        public static final int GATT_INTERNAL_ERROR = 0x81;
        public static final int GATT_WRONG_STATE = 0x82;
        public static final int GATT_DB_FULL = 0x83;
        public static final int GATT_BUSY = 0x84;
        public static final int GATT_ERROR = 0x85;
        public static final int GATT_CMD_STARTED = 0x86;
        public static final int GATT_ILLEGAL_PARAMETER = 0x87;
        public static final int GATT_PENDING = 0x88;
        public static final int GATT_AUTH_FAIL = 0x89;
        public static final int GATT_MORE = 0x8a;
        public static final int GATT_INVALID_CFG = 0x8b;
        public static final int GATT_SERVICE_STARTED = 0x8c;
        public static final int GATT_ENCRYPED_NO_MITM = 0x8d;
        public static final int GATT_NOT_ENCRYPTED = 0x8e;
        /* 0xE0 ~ 0xFC reserved for future use */
        /**
         * Client Characteristic Configuration Descriptor improperly configured.
         */
        public static final int GATT_CCC_CFG_ERR = 0xFD; /*  */
        /**
         * Procedure already in progress.
         */
        public static final int GATT_PRC_IN_PROGRESS = 0xFE; /*  */
        /**
         * Attribute value out of range.
         */
        public static final int GATT_OUT_OF_RANGE = 0xFF;
        /**
         * 0x0100 L2CAP connection cancelled
         */
        public static final int GATT_CONN_CANCEL = 0x0100; // l2cdefs.h : L2CAP_CONN_CANCEL
    }

    /**
     * <p>This class groups all error statuses from the ATT definition as defined in the Bluetooth Core Specification
     * 5.0 Vol3 PartF 3.4.1.</p>
     */
    public static class ATT {

        /**
         * <p>This method builds a label which displays the name of the status errors which match a value from the
         * {@link ATT ATT} class.</p>
         * <p>If the <code>detailed</code> parameter is true, the label is as follows:
         * <blockquote><pre>ERROR_NAME: corresponding known information.</pre></blockquote>
         * Otherwise it only displays <code>ERROR_NAME</code>.</p>
         * <p>If there is no field matching the given status, this method returns "".</p>
         *
         * @param status
         *          The status to look for a name.
         * @param detailed
         *          To get complementary information about the status if found.
         *
         * @return a readable label corresponding to the status if it matches the values of the
         * {@link ATT ATT} class.
         */
        public static String getLabel(int status, boolean detailed) {
            String message = "", details = "";

            switch (status) {
                case ATT.INVALID_HANDLE:                                // 0x01
                    message = "INVALID_HANDLE";
                    details = "The attribute handle given was not valid on this server.";
                    break;
                case ATT.READ_NOT_PERMITTED:                            // 0x02
                    message = "READ_NOT_PERMITTED";
                    details = "The attribute cannot be read.";
                    break;
                case ATT.WRITE_NOT_PERMITTED:                           // 0x03
                    message = "WRITE_NOT_PERMITTED";
                    details = "The attribute cannot be written.";
                    break;
                case ATT.INVALID_PDU:                                   // 0x04
                    message = "INVALID_PDU";
                    details = "The attribute PDU was invalid.";
                    break;
                case ATT.INSUFFICIENT_AUTHENTICATION:                   // 0x05
                    message = "INSUFFICIENT_AUTHENTICATION";
                    details = "The attribute requires authentication before it can be read or written.";
                    break;
                case ATT.REQUEST_NOT_SUPPORTED:                         // 0x06
                    message = "REQUEST_NOT_SUPPORTED";
                    details = "Attribute server does not support the request received from the client.";
                    break;
                case ATT.INVALID_OFFSET:                                // 0x07
                    message = "INVALID_OFFSET";
                    details = "Offset specified was past the end of the attribute.";
                    break;
                case ATT.INSUFFICIENT_AUTHORIZATION:                    // 0x08
                    message = "INSUFFICIENT_AUTHORIZATION";
                    details = "The attribute requires authorization before it can be read or written.";
                    break;
                case ATT.PREPARE_QUEUE_FULL:                            // 0x09
                    message = "PREPARE_QUEUE_FULL";
                    details = "Too many prepare writes have been queued.";
                    break;
                case ATT.ATTRIBUTE_NOT_FOUND:                           // 0x0A
                    message = "ATTRIBUTE_NOT_FOUND";
                    details = "No attribute found within the given attribute handle range.";
                    break;
                case ATT.ATTRIBUTE_NOT_LONG:                            // 0x0B
                    message = "ATTRIBUTE_NOT_LONG";
                    details = "The attribute cannot be read using the Read Blob Request.";
                    break;
                case ATT.INSUFFICIENT_ENCRYPTION_KEY_SIZE:              // 0x0C
                    message = "INSUFFICIENT_ENCRYPTION_KEY_SIZE";
                    details = "The Encryption Key Size used for encrypting this link is insufficient.";
                    break;
                case ATT.INVALID_ATTRIBUTE_VALUE_LENGTH:                // 0x0D
                    message = "INVALID_ATTRIBUTE_VALUE_LENGTH";
                    details = "The attribute value length is invalid for the operation.";
                    break;
                case ATT.UNLIKELY_ERROR:                                // 0x0E
                    message = "UNLIKELY_ERROR";
                    details = "The attribute request that was requested has encountered an error that was unlikely, and therefore could not be completed as requested.";
                    break;
                case ATT.INSUFFICIENT_ENCRYPTION:                       // 0x0F
                    message = "INSUFFICIENT_ENCRYPTION";
                    details = "The attribute requires encryption before it can be read or written.";
                    break;
                case ATT.UNSUPPORTED_GROUP_TYPE:                        // 0x10
                    message = "UNSUPPORTED_GROUP_TYPE";
                    details = "The attribute type is not a supported grouping attribute as defined by a higher layer specification.";
                    break;
                case ATT.INSUFFICIENT_RESOURCES:                        // 0x11
                    message = "INSUFFICIENT_RESOURCES";
                    details = "Insufficient Resources to complete the request.";
                    break;
            }

            return message.length() > 0 ?
                    message + (detailed && (details.length() > 0) ? ": " + details : "")
                    : "";
        }

        /**
         * The attribute handle given was not valid on this server.
         */
        public static final int INVALID_HANDLE = 0x01;
        /**
         * The attribute cannot be read.
         */
        public static final int READ_NOT_PERMITTED = 0x02;
        /**
         * The attribute cannot be written.
         */
        public static final int WRITE_NOT_PERMITTED = 0x03;
        /**
         * The attribute PDU was invalid.
         */
        public static final int INVALID_PDU = 0x04;
        /**
         * The attribute requires authentication before it can be read or written.
         */
        public static final int INSUFFICIENT_AUTHENTICATION = 0x05;
        /**
         * Attribute server does not support the request received from the client.
         */
        public static final int REQUEST_NOT_SUPPORTED = 0x06;
        /**
         * Offset specified was past the end of the attribute.
         */
        public static final int INVALID_OFFSET = 0x07;
        /**
         * The attribute requires authorization before it can be read or written.
         */
        public static final int INSUFFICIENT_AUTHORIZATION = 0x08;
        /**
         * Too many prepare writes have been queued.
         */
        public static final int PREPARE_QUEUE_FULL = 0x09;
        /**
         * No attribute found within the given attribute handle range .
         */
        public static final int ATTRIBUTE_NOT_FOUND = 0x0A;
        /**
         * The attribute cannot be read using the Read Blob Request.
         */
        public static final int ATTRIBUTE_NOT_LONG = 0x0B;
        /**
         * The Encryption Key Size used for encrypting this link is insufficient.
         */
        public static final int INSUFFICIENT_ENCRYPTION_KEY_SIZE = 0x0C;
        /**
         * The attribute value length is invalid for the operation .
         */
        public static final int INVALID_ATTRIBUTE_VALUE_LENGTH = 0x0D;
        /**
         * The attribute request that was requested has encountered an error that was unlikely, and therefore could
         * not be completed as requested.
         */
        public static final int UNLIKELY_ERROR = 0x0E;
        /**
         * The attribute requires encryption before it can be read or written.
         */
        public static final int INSUFFICIENT_ENCRYPTION = 0x0F;
        /**
         * The attribute type is not a supported grouping attribute as defined by a higher layer specification.
         */
        public static final int UNSUPPORTED_GROUP_TYPE = 0x10;
        /**
         * Insufficient Resources to complete the request.
         */
        public static final int INSUFFICIENT_RESOURCES = 0x11;

        // Application Error 0x80-0x9F Application error code defined by a higher layer specification.

    }

    /**
     * <p>This class regroups all error status from the HCI definition as defined in the Bluetooth Core Specification
     * 5.0 Vol2 PartD.</p>
     */
    public static class HCI {

        /**
         * <p>This method builds a label which displays the name of the status errors which match a value from the
         * {@link HCI HCI} class.</p>
         * <p>If the <code>detailed</code> parameter is true, the label is as follows:
         * <blockquote><pre>ERROR_NAME: corresponding known information.</pre></blockquote>
         * Otherwise it only displays <code>ERROR_NAME</code>.</p>
         * <p>If there is no field matching the given status, this method returns "".</p>
         *
         * @param status
         *          The status to look for a name.
         * @param detailed
         *          To get complementary information about the status if found.
         *
         * @return a readable label corresponding to the status if it matches the values of the
         * {@link HCI HCI} class.
         */
        public static String getLabel(int status, boolean detailed) {
            String message = "", details = "";

            switch (status) {
                case HCI.SUCCESS:                                                   // 0x00
                    message = "SUCCESS";
                    break;
                case HCI.UNKNOWN_HCI_COMMAND:                                       // 0x01
                    message = "UNKNOWN_HCI_COMMAND";
                    details = "The Unknown HCI Command error code indicates that the Controller does not " +
                            "understand the HCI Command Packet OpCode that the Host sent. The OpCode given might " +
                            "not correspond to any of the OpCodes specified in this document, or any " +
                            "vendor-specific OpCodes, or the command may have not been implemented.";
                    break;
                case HCI.UNKNOWN_CONNECTION_IDENTIFIER:                             // 0x02
                    message = "UNKNOWN_CONNECTION_IDENTIFIER";
                    details = "The Unknown Connection Identifier error code indicates that a command was sent " +
                            "from the Host that should identify a connection, but that connection does not exist.";
                    break;
                case HCI.HARDWARE_FAILURE:                                          // 0x03
                    message = "HARDWARE_FAILURE";
                    details = "The Hardware Failure error code indicates to the Host that something in the " +
                            "Controller has failed in a manner that cannot be described with any other error " +
                            "code. The meaning implied with this error code is implementation dependent.";
                    break;
                case HCI.PAGE_TIMEOUT:                                              // 0x04
                    message = "PAGE_TIMEOUT";
                    details = "The Page Timeout error code indicates that a page timed out because of the Page " +
                            "Timeout configuration parameter. This error code may occur only with the " +
                            "Remote_Name_Request and Create_Connection commands.";
                    break;
                case HCI.AUTHENTICATION_FAILURE:                                    // 0x05
                    message = "AUTHENTICATION_FAILURE";
                    details = "The Authentication Failure error code indicates that pairing or authentication " +
                            "failed due to incorrect results in the pairing or authentication procedure. This " +
                            "could be due to an incorrect PIN or Link Key.";
                    break;
                case HCI.PIN_OR_KEY_MISSING:                                        // 0x06
                    message = "PIN_OR_KEY_MISSING";
                    details = "The PIN or Key Missing error code is used when pairing failed because of a " +
                            "missing PIN, or authentication failed because of a missing Key.";
                    break;
                case HCI.MEMORY_CAPACITY_EXCEEDED:                                  // 0x07
                    message = "MEMORY_CAPACITY_EXCEEDED";
                    details = "The Memory Capacity Exceeded error code indicates to the Host that the Controller " +
                            "has run out of memory to store new parameters.";
                    break;
                case HCI.CONNECTION_TIMEOUT:                                        // 0x08
                    message = "CONNECTION_TIMEOUT";
                    details = "The Connection Timeout error code indicates that the link supervision timeout has " +
                            "expired for a given connection.";
                    break;
                case HCI.CONNECTION_LIMIT_EXCEEDED:                                 // 0x09
                    message = "CONNECTION_LIMIT_EXCEEDED";
                    details = "The Connection Limit Exceeded error code indicates that an attempt to create " +
                            "another connection failed because the Controller is already at its limit of the " +
                            "number of connections it can support. The number of connections a device can " +
                            "support is implementation dependent.";
                    break;
                case HCI.SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED:         // 0x0A
                    message = "SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED";
                    details = "The Synchronous Connection Limit to a Device Exceeded error code indicates that " +
                            "the Controller has reached the limit to the number of synchronous connections that " +
                            "can be achieved to a device. The number of synchronous connections a device can " +
                            "support is implementation dependent.";
                    break;
                case HCI.CONNECTION_ALREADY_EXISTS:                                 // 0x0B
                    message = "CONNECTION_ALREADY_EXISTS";
                    details = "The Connection Already Exists error code indicates that an attempt was made to " +
                            "create a new Connection to a device when there is already a connection to this " +
                            "device and multiple connections to the same device are not permitted.";
                    break;
                case HCI.COMMAND_DISALLOWED:                                        // 0x0C
                    message = "COMMAND_DISALLOWED";
                    details = "The Command Disallowed error code indicates that the command requested cannot be " +
                            "executed because the Controller is in a state where it cannot process this command " +
                            "at this time. This error shall not be used for command OpCodes where the error code " +
                            "Unknown HCI Command is valid.";
                    break;
                case HCI.CONNECTION_REJECTED_LIMITED_RESOURCES:                     // 0x0D
                    message = "CONNECTION_REJECTED_LIMITED_RESOURCES";
                    details = "The Connection Rejected Due To Limited Resources error code indicates that a " +
                            "connection was rejected due to limited resources.";
                    break;
                case HCI.CONNECTION_REJECTED_SECURITY_REASONS:                      // 0x0E
                    message = "CONNECTION_REJECTED_SECURITY_REASONS";
                    details = "The Connection Rejected Due To Security Reasons error code indicates that a " +
                            "connection was rejected due to security requirements not being fulfilled, like " +
                            "authentication or pairing.";
                    break;
                case HCI.CONNECTION_REJECTED_UNACCEPTABLE_BD_ADDR:                  // 0x0F
                    message = "CONNECTION_REJECTED_UNACCEPTABLE_BD_ADDR";
                    details = "The Connection Rejected due to Unacceptable BD_ADDR error code indicates that a " +
                            "connection was rejected because this device does not accept the BD_ADDR. This may " +
                            "be because the device will only accept connections from specific BD_ADDRs.";
                    break;
                case HCI.CONNECTION_ACCEPT_TIMEOUT_EXCEEDED:                        // 0x10
                    message = "CONNECTION_ACCEPT_TIMEOUT_EXCEEDED";
                    details = "The Connection Accept Timeout Exceeded error code indicates that the Connection " +
                            "Accept Timeout has been exceeded for this connection attempt.";
                    break;
                case HCI.UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE:                    // 0x11
                    message = "UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE";
                    details = "The Unsupported Feature Or Parameter Value error code indicates that a feature " +
                            "or parameter value in the HCI command is not supported. This error code shall not " +
                            "be used in an LMP PDU.";
                    break;
                case HCI.INVALID_HCI_COMMAND_PARAMETERS:                            // 0x12
                    message = "INVALID_HCI_COMMAND_PARAMETERS";
                    details = "The Invalid HCI Command Parameters error code indicates that at least one of " +
                            "the HCI command parameters is invalid. This shall be used when:\n" +
                            "\t\t\t\t- the parameter total length is invalid.\n" +
                            "\t\t\t\t- a command parameter is an invalid type.\n" +
                            "\t\t\t\t- a connection identifier does not match the corresponding event.\n" +
                            "\t\t\t\t- a parameter is odd when it is required to be even.\n" +
                            "\t\t\t\t- a parameter is outside of the specified range.\n" +
                            "\t\t\t\t- two or more parameter values have inconsistent values.\n" +
                            "Note: An invalid type can be, for example, when a SCO connection handle is used " +
                            "where an ACL connection handle is required. </p>";
                    break;
                case HCI.REMOTE_USER_TERMINATED_CONNECTION:                         // 0x13
                    message = "";
                    details = "The Remote User Terminated Connection error code indicates that the user on the " +
                            "remote device terminated the connection.";
                    break;
                case HCI.REMOTE_DEVICE_TERMINATED_CONNECTION_LOW_RESOURCES:         // 0x14
                    message = "REMOTE_USER_TERMINATED_CONNECTION";
                    details = "The Remote Device Terminated Connection due to Low Resources error code indicates " +
                            "that the remote device terminated the connection because of low resources.";
                    break;
                case HCI.REMOTE_DEVICE_TERMINATED_CONNECTION_POWER_OFF:             // 0x15
                    message = "REMOTE_DEVICE_TERMINATED_CONNECTION_POWER_OFF";
                    details = "The Remote Device Terminated Connection due to Power Off error code indicates " +
                            "that the remote device terminated the connection because the device is about to " +
                            "power off.";
                    break;
                case HCI.CONNECTION_TERMINATED_BY_LOCAL_HOST:                       // 0x16
                    message = "CONNECTION_TERMINATED_BY_LOCAL_HOST";
                    details = "The Connection Terminated By Local Host error code indicates that the local " +
                            "device terminated the connection.";
                    break;
                case HCI.REPEATED_ATTEMPTS:                                         // 0x17
                    message = "REPEATED_ATTEMPTS";
                    details = "The Repeated Attempts error code indicates that the Controller is disallowing an " +
                            "authentication or pairing procedure because too little time has elapsed since the " +
                            "last authentication or pairing attempt failed.";
                    break;
                case HCI.PAIRING_NOT_ALLOWED:                                       // 0x18
                    message = "PAIRING_NOT_ALLOWED";
                    details = "The Pairing Not Allowed error code indicates that the device does not allow " +
                            "pairing. For example, when a device only allows pairing during a certain time " +
                            "window after some user input allows pairing.";
                    break;
                case HCI.UNKNOWN_LMP_PDU:                                           // 0x19
                    message = "UNKNOWN_LMP_PDU";
                    details = "The Unknown LMP PDU error code indicates that the Controller has received an " +
                            "unknown LMP OpCode.";
                    break;
                case HCI.UNSUPPORTED_REMOTE_FEATURE_OR_LMP_FEATURE:                 // 0x1A
                    message = "UNSUPPORTED_REMOTE_FEATURE_OR_LMP_FEATURE";
                    details = "The Unsupported Remote Feature error code indicates that the remote device does " +
                            "not support the feature associated with the issued command or LMP PDU.";
                    break;
                case HCI.SCO_OFFSET_REJECTED:                                       // 0x1B
                    message = "SCO_OFFSET_REJECTED";
                    details = "The SCO Offset Rejected error code indicates that the offset requested in the " +
                            "LMP_SCO_link_req PDU has been rejected.";
                    break;
                case HCI.SCO_INTERVAL_REJECTED:                                     // 0x1C
                    message = "SCO_INTERVAL_REJECTED";
                    details = "The SCO Interval Rejected error code indicates that the interval requested in the " +
                            "LMP_SCO_link_req PDU has been rejected.";
                    break;
                case HCI.SCO_AIR_MODE_REJECTED:                                     // 0x1D
                    message = "SCO_AIR_MODE_REJECTED";
                    details = "The SCO Air Mode Rejected error code indicates that the air mode requested in the " +
                            "LMP_SCO_link_req PDU has been rejected.";
                    break;
                case HCI.INVALID_LMP_OR_LL_PARAMETERS:                              // 0x1E
                    message = "INVALID_LMP_OR_LL_PARAMETERS";
                    details = "The Invalid LMP Parameters / Invalid LL Parameters error code indicates that some " +
                            "LMP PDU / LL Control PDU parameters were invalid. This shall be used when:\n" +
                            "\t\t\t\t- the PDU length is invalid.\n" +
                            "\t\t\t\t- a parameter is odd when it is required to be even.\n" +
                            "\t\t\t\t- a parameter is outside of the specified range.\n" +
                            "\t\t\t\t- two or more parameters have inconsistent values.";
                    break;
                case HCI.UNSPECIFIED_ERROR:                                         // 0x1F
                    message = "UNSPECIFIED_ERROR";
                    details = "The Unspecified Error error code indicates that no other error code specified is " +
                            "appropriate to use.";
                    break;
                case HCI.UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE:                     // 0x20
                    message = "UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE";
                    details = "The Unsupported LMP Parameter Value / Unsupported LL Parameter Value error code " +
                            "indicates that an LMP PDU or an LL Control PDU contains at least one parameter " +
                            "value that is not supported by the Controller at this time. This is normally used " +
                            "after a long negotiation procedure, for example during an LMP_hold_req, " +
                            "LMP_sniff_req and LMP_encryption_key_size_req PDU exchanges. This may be used by " +
                            "the Link Layer, for example during the Connection Parameters Request Link Layer " +
                            "Control procedure.";
                    break;
                case HCI.ROLE_CHANGE_NOT_ALLOWED:                                   // 0x21
                    message = "ROLE_CHANGE_NOT_ALLOWED";
                    details = "The Role Change Not Allowed error code indicates that a Controller will not allow " +
                            "a role change at this time.";
                    break;
                case HCI.LMP_OR_LL_RESPONSE_TIMEOUT:                                // 0x22
                    message = "LMP_OR_LL_RESPONSE_TIMEOUT";
                    details = "The LMP Response Timeout / LL Response Timeout error code indicates that an LMP " +
                            "transaction failed to respond within the LMP response timeout or an LL transaction " +
                            "failed to respond within the LL response timeout.";
                    break;
                case HCI.LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION: // 0x23
                    message = "LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION";
                    details = "The LMP Error Transaction Collision / LL Procedure Collision error code indicates " +
                            "that an LMP transaction or LL procedure has collided with the same transaction or " +
                            "procedure that is already in progress.";
                    break;
                case HCI.LMP_PDU_NOT_ALLOWED:                                       // 0x24
                    message = "LMP_PDU_NOT_ALLOWED";
                    details = "The LMP PDU Not Allowed error code indicates that a Controller sent an LMP PDU " +
                            "with an OpCode that was not allowed.";
                    break;
                case HCI.ENCRYPTION_MODE_NOT_ACCEPTABLE:                            // 0x25
                    message = "ENCRYPTION_MODE_NOT_ACCEPTABLE";
                    details = "The Encryption Mode Not Acceptable error code indicates that the requested " +
                            "encryption mode is not acceptable at this time.";
                    break;
                case HCI.LINK_KEY_CANNOT_BE_CHANGED:                                // 0x26
                    message = "LINK_KEY_CANNOT_BE_CHANGED";
                    details = "The Link Key cannot be Changed error code indicates that a link key cannot be " +
                            "changed because a fixed unit key is being used.";
                    break;
                case HCI.REQUESTED_QOS_NOT_SUPPORTED:                               // 0x27
                    message = "REQUESTED_QOS_NOT_SUPPORTED";
                    details = "The Requested QoS Not Supported error code indicates that the requested Quality " +
                            "of Service is not supported.";
                    break;
                case HCI.INSTANT_PASSED:                                            // 0x28
                    message = "INSTANT_PASSED";
                    details = "The Instant Passed error code indicates that an LMP PDU or LL PDU that includes " +
                            "an instant cannot be performed because the instant when this would have occurred " +
                            "has passed.";
                    break;
                case HCI.PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED:                       // 0x29
                    message = "PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED";
                    details = "The Pairing With Unit Key Not Supported error code indicates that it was not " +
                            "possible to pair as a unit key was requested and it is not supported.";
                    break;
                case HCI.DIFFERENT_TRANSACTION_COLLISION:                           // 0x2A
                    message = "DIFFERENT_TRANSACTION_COLLISION";
                    details = "The Different Transaction Collision error code indicates that an LMP transaction " +
                            "or LL Procedure was started that collides with an ongoing transaction.";
                    break;
                case HCI.UNACCEPTABLE_PARAMETER:                                    // 0x2C
                    message = "UNACCEPTABLE_PARAMETER";
                    details = "The QoS Unacceptable Parameter error code indicates that the specified quality of " +
                            "service parameters could not be accepted at this time, but other parameters may be " +
                            "acceptable.";
                    break;
                case HCI.QOS_REJECTED:                                              // 0x2D
                    message = "QOS_REJECTED";
                    details = "The QoS Rejected error code indicates that the specified quality of service " +
                            "parameters cannot be accepted and QoS negotiation should be terminated";
                    break;
                case HCI.CHANNEL_CLASSIFICATION_NOT_SUPPORTED:                      // 0x2E
                    message = "CHANNEL_CLASSIFICATION_NOT_SUPPORTED";
                    details = "The Channel Assessment Not Supported error code indicates that the Controller " +
                            "cannot perform channel assessment because it is not supported.";
                    break;
                case HCI.INSUFFICIENT_SECURITY:                                     // 0x2F
                    message = "INSUFFICIENT_SECURITY";
                    details = "The Insufficient Security error code indicates that the HCI command or LMP PDU " +
                            "sent is only possible on an encrypted link.";
                    break;
                case HCI.PARAMETER_OUT_OF_MANDATORY_RANGE:                          // 0x30
                    message = "PARAMETER_OUT_OF_MANDATORY_RANGE";
                    details = "The Parameter Out Of Mandatory Range error code indicates that a parameter value " +
                            "requested is outside the mandatory range of parameters for the given HCI command " +
                            "or LMP PDU and the recipient does not accept that value.";
                    break;
                case HCI.ROLE_SWITCH_PENDING:                                       // 0x32
                    message = "ROLE_SWITCH_PENDING";
                    details = "The Role Switch Pending error code indicates that a Role Switch is pending. This " +
                            "can be used when an HCI command or LMP PDU cannot be accepted because of a pending " +
                            "role switch. This can also be used to notify a peer device about a pending role " +
                            "switch.";
                    break;
                case HCI.RESERVED_SLOT_VIOLATION:                                   // 0x34
                    message = "RESERVED_SLOT_VIOLATION";
                    details = "The Reserved Slot Violation error code indicates that the current Synchronous " +
                            "negotiation was terminated with the negotiation state set to Reserved Slot Violation.";
                    break;
                case HCI.ROLE_SWITCH_FAILED:                                        // 0x35
                    message = "ROLE_SWITCH_FAILED";
                    details = "The Role Switch Failed error code indicates that a role switch was attempted but " +
                            "it failed and the original piconet structure is restored. The switch may have failed" +
                            " because the TDD switch or piconet switch failed.";
                    break;
                case HCI.EXTENDED_INQUIRY_RESPONSE_TOO_LARGE:                       // 0x36
                    message = "EXTENDED_INQUIRY_RESPONSE_TOO_LARGE";
                    details = "The Extended Inquiry Response Too Large error code indicates that the extended " +
                            "inquiry response, with the requested requirements for FEC, is too large to fit in " +
                            "any of the packet types supported by the Controller.";
                    break;
                case HCI.SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST:               // 0x37
                    message = "SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST";
                    details = "The Secure Simple Pairing Not Supported by Host error code indicates that the IO " +
                            "capabilities request or response was rejected because the sending Host does not " +
                            "support Secure Simple Pairing even though the receiving Link Manager does.";
                    break;
                case HCI.HOST_BUSY_PAIRING:                                         // 0x38
                    message = "HOST_BUSY_PAIRING";
                    details = "The Host Busy - Pairing error code indicates that the Host is busy with another " +
                            "pairing operation and unable to support the requested pairing. The receiving device " +
                            "should retry pairing again later.";
                    break;
                case HCI.CONNECTION_REJECTED_NO_SUITABLE_CHANNEL_FOUND:             // 0x39
                    message = "CONNECTION_REJECTED_NO_SUITABLE_CHANNEL_FOUND";
                    details = "The Connection Rejected due to No Suitable Channel Found error code indicates " +
                            "that the Controller could not calculate an appropriate value for the Channel " +
                            "selection operation.";
                    break;
                case HCI.CONTROLLER_BUSY:                                           // 0x3A
                    message = "CONTROLLER_BUSY";
                    details = "The Controller Busy error code indicates that the operation was rejected because " +
                            "the Controller was busy and unable to process the request.";
                    break;
                /**
                 */
                case HCI.UNACCEPTABLE_CONNECTION_PARAMETERS:                        // 0x3B
                    message = "UNACCEPTABLE_CONNECTION_PARAMETERS";
                    details = "The Unacceptable Connection Parameters error code indicates that the remote " +
                            "device either terminated the connection or rejected a request because of one or more" +
                            " unacceptable connection parameters.";
                    break;
                case HCI.DIRECTED_ADVERTISING_TIMEOUT:                              // 0x3C
                    message = "DIRECTED_ADVERTISING_TIMEOUT";
                    details = "The Advertising Timeout error code indicates that advertising for a fixed " +
                            "duration completed or, for directed advertising, that advertising completed without " +
                            "a connection being created. (Formerly called Directed Advertising Timeout)";
                    break;
                case HCI.CONNECTION_TERMINATED_MIC_FAILURE:                         // 0x3D
                    message = "CONNECTION_TERMINATED_MIC_FAILURE";
                    details = "The Connection Terminated Due to MIC Failure error code indicates that the " +
                            "connection was terminated because the Message Integrity Check (MIC) failed on a " +
                            "received packet.";
                    break;
                case HCI.CONNECTION_ESTABLISHMENT_FAILED:                           // 0x3E
                    message = "CONNECTION_ESTABLISHMENT_FAILED";
                    details = "The Connection Failed to be Established error code indicates that the LL " +
                            "initiated a connection but the connection has failed to be established.";
                    break;
                case HCI.MAC_CONNECTION_FAILED:                                     // 0x3F
                    message = "MAC_CONNECTION_FAILED";
                    details = "The MAC of the 802.11 AMP was requested to connect to a peer, but the connection " +
                            "failed.";
                    break;
                case HCI.COARSE_CLOCK_ADJUSTMENT_REJECTED:                          // 0x40
                    message = "COARSE_CLOCK_ADJUSTMENT_REJECTED";
                    details = "The Coarse Clock Adjustment Rejected but Will Try to Adjust Using Clock Dragging " +
                            "error code indicates that the master, at this time, is unable to make a coarse " +
                            "adjustment to the piconet clock, using the supplied parameters. Instead the master " +
                            "will attempt to move the clock using clock dragging.";
                    break;
                case HCI.TYPE_0_SUBMAP_NOT_DEFINED:                                 // 0x41
                    message = "TYPE_0_SUBMAP_NOT_DEFINED";
                    details = "The Type0 Submap Not Defined error code indicates that the LMP PDU is rejected " +
                            "because the Type 0 submap is not currently defined.";
                    break;
                case HCI.UNKNOWN_ADVERTISING_IDENTIFIER:                            // 0x42
                    message = "UNKNOWN_ADVERTISING_IDENTIFIER";
                    details = "The Unknown Advertising Identifier error code indicates that a command was sent " +
                            "from the Host that should identify an Advertising or Sync handle, but the " +
                            "Advertising or Sync handle does not exist.";
                    break;
                case HCI.LIMIT_REACHED:                                             // 0x43
                    message = "LIMIT_REACHED";
                    details = "The Limit Reached error code indicates that number of operations requested has " +
                            "been reached and has indicated the completion of the activity (e.g., advertising or " +
                            "scanning).";
                    break;
                case HCI.OPERATION_CANCELLED_BY_HOST:                               // 0x44
                    message = "OPERATION_CANCELLED_BY_HOST";
                    details = "The Operation Cancelled by Host error code indicates a request to the Controller " +
                            "issued by the Host and still pending was successfully canceled.";
                    break;
            }

            return message.length() > 0 ?
                    message + (detailed && (details.length() > 0) ? ": " + details : "")
                    : "";
        }

        public static final int SUCCESS = 0x00;
        /**
         * The Unknown HCI Command error code indicates that the Controller does not understand the HCI Command
         * Packet OpCode that the Host sent. The OpCode given might not correspond to any of the OpCodes specified
         * in this document, or any vendor-specific OpCodes, or the command may have not been implemented.
         */
        public static final int UNKNOWN_HCI_COMMAND = 0x01;
        /**
         * The Unknown Connection Identifier error code indicates that a command was sent from the Host that should
         * identify a connection, but that connection does not exist.
         */
        public static final int UNKNOWN_CONNECTION_IDENTIFIER = 0x02;
        /**
         * The Hardware Failure error code indicates to the Host that something in the Controller has failed in a
         * manner that cannot be described with any other error code. The meaning implied with this error code is
         * implementation dependent.
         */
        public static final int HARDWARE_FAILURE = 0x03;
        /**
         * The Page Timeout error code indicates that a page timed out because of the Page Timeout configuration
         * parameter. This error code may occur only with the Remote_Name_Request and Create_Connection commands
         */
        public static final int PAGE_TIMEOUT = 0x04;
        /**
         * The Authentication Failure error code indicates that pairing or authentication failed due to incorrect
         * results in the pairing or authentication procedure. This could be due to an incorrect PIN or Link Key.
         */
        public static final int AUTHENTICATION_FAILURE = 0x05;
        /**
         * The PIN or Key Missing error code is used when pairing failed because of a missing PIN, or authentication
         * failed because of a missing Key.
         */
        public static final int PIN_OR_KEY_MISSING = 0x06;
        /**
         * The Memory Capacity Exceeded error code indicates to the Host that the Controller has run out of memory
         * to store new parameters.
         */
        public static final int MEMORY_CAPACITY_EXCEEDED = 0x07;
        /**
         * The Connection Timeout error code indicates that the link supervision timeout has expired for a given
         * connection.
         */
        public static final int CONNECTION_TIMEOUT = 0x08;
        /**
         * The Connection Limit Exceeded error code indicates that an attempt to create another connection failed
         * because the Controller is already at its limit of the number of connections it can support. The number of
         * connections a device can support is implementation dependent.
         */
        public static final int CONNECTION_LIMIT_EXCEEDED = 0x09;
        /**
         * The Synchronous Connection Limit to a Device Exceeded error code indicates that the Controller has
         * reached the limit to the number of synchronous connections that can be achieved to a device. The number
         * of synchronous connections a device can support is implementation dependent.
         */
        public static final int SYNCHRONOUS_CONNECTION_LIMIT_TO_A_DEVICE_EXCEEDED = 0x0A;
        /**
         * The Connection Already Exists error code indicates that an attempt was made to create a new Connection to
         * a device when there is already a connection to this device and multiple connections to the same device
         * are not permitted.
         */
        public static final int CONNECTION_ALREADY_EXISTS = 0x0B;
        /**
         * The Command Disallowed error code indicates that the command requested cannot be executed because the
         * Controller is in a state where it cannot process this command at this time. This error shall not be used
         * for command OpCodes where the error code Unknown HCI Command is valid.
         */
        public static final int COMMAND_DISALLOWED = 0x0C;
        /**
         * The Connection Rejected Due To Limited Resources error code indicates that a connection was rejected due
         * to limited resources.
         */
        public static final int CONNECTION_REJECTED_LIMITED_RESOURCES = 0x0D;
        /**
         * The Connection Rejected Due To Security Reasons error code indicates that a connection was rejected due
         * to security requirements not being fulfilled, like authentication or pairing.
         */
        public static final int CONNECTION_REJECTED_SECURITY_REASONS = 0x0E;
        /**
         * The Connection Rejected due to Unacceptable BD_ADDR error code indicates that a connection was rejected
         * because this device does not accept the BD_ADDR. This may be because the device will only accept
         * connections from specific BD_ADDRs.
         */
        public static final int CONNECTION_REJECTED_UNACCEPTABLE_BD_ADDR = 0x0F;
        /**
         * The Connection Accept Timeout Exceeded error code indicates that the Connection Accept Timeout has been
         * exceeded for this connection attempt.
         */
        public static final int CONNECTION_ACCEPT_TIMEOUT_EXCEEDED = 0x10;
        /**
         * The Unsupported Feature Or Parameter Value error code indicates that a feature or parameter value in the
         * HCI command is not supported. This error code shall not be used in an LMP PDU.
         */
        public static final int UNSUPPORTED_FEATURE_OR_PARAMETER_VALUE = 0x11;
        /**
         * <p>The Invalid HCI Command Parameters error code indicates that at least one of the HCI command
         * parameters is invalid.<br/>This shall be used when: <ul> <Li>the parameter total length is invalid.</Li>
         * <li>a command parameter is an invalid type.</Li> <li>a connection identifier does not match the
         * corresponding event.</Li> <li>a parameter is odd when it is required to be even.</Li> <li>a parameter is
         * outside of the specified range.</Li> <li>two or more parameter values have inconsistent values.</Li>
         * </ul> Note: An invalid type can be, for example, when a SCO connection handle is used where an ACL
         * connection handle is required. </p>
         */
        public static final int INVALID_HCI_COMMAND_PARAMETERS = 0x12;
        /**
         * The Remote User Terminated Connection error code indicates that the user on the remote device terminated
         * the connection.
         */
        public static final int REMOTE_USER_TERMINATED_CONNECTION = 0x13;
        /**
         * The Remote Device Terminated Connection due to Low Resources error code indicates that the remote device
         * terminated the connection because of low resources.
         */
        public static final int REMOTE_DEVICE_TERMINATED_CONNECTION_LOW_RESOURCES = 0x14;
        /**
         * The Remote Device Terminated Connection due to Power Off error code indicates that the remote device
         * terminated the connection because the device is about to power off.
         */
        public static final int REMOTE_DEVICE_TERMINATED_CONNECTION_POWER_OFF = 0x15;
        /**
         * The Connection Terminated By Local Host error code indicates that the local device terminated the
         * connection.
         */
        public static final int CONNECTION_TERMINATED_BY_LOCAL_HOST = 0x16;
        /**
         * The Repeated Attempts error code indicates that the Controller is disallowing an authentication or
         * pairing procedure because too little time has elapsed since the last authentication or pairing attempt
         * failed.
         */
        public static final int REPEATED_ATTEMPTS = 0x17;
        /**
         * The Pairing Not Allowed error code indicates that the device does not allow pairing. For example, when a
         * device only allows pairing during a certain time window after some user input allows pairing.
         */
        public static final int PAIRING_NOT_ALLOWED = 0x18;
        /**
         * The Unknown LMP PDU error code indicates that the Controller has received an unknown LMP OpCode.
         */
        public static final int UNKNOWN_LMP_PDU = 0x19;
        /**
         * The Unsupported Remote Feature error code indicates that the remote device does not support the feature
         * associated with the issued command or LMP PDU.
         */
        public static final int UNSUPPORTED_REMOTE_FEATURE_OR_LMP_FEATURE = 0x1A;
        /**
         * The SCO Offset Rejected error code indicates that the offset requested in the LMP_SCO_link_req PDU has
         * been rejected.
         */
        public static final int SCO_OFFSET_REJECTED = 0x1B;
        /**
         * The SCO Interval Rejected error code indicates that the interval requested in the LMP_SCO_link_req PDU
         * has been rejected.
         */
        public static final int SCO_INTERVAL_REJECTED = 0x1C;
        /**
         * The SCO Air Mode Rejected error code indicates that the air mode requested in the LMP_SCO_link_req PDU
         * has been rejected.
         */
        public static final int SCO_AIR_MODE_REJECTED = 0x1D;
        /**
         * <p>The Invalid LMP Parameters / Invalid LL Parameters error code indicates that some LMP PDU / LL Control
         * PDU parameters were invalid.<br/>This shall be used when: <ul> <li>the PDU length is invalid.</li> <li>a
         * parameter is odd when it is required to be even.</li> <li>a parameter is outside of the specified
         * range.</li> <li>two or more parameters have inconsistent values.</li> </ul> </p>
         */
        public static final int INVALID_LMP_OR_LL_PARAMETERS = 0x1E;
        /**
         * The Unspecified Error error code indicates that no other error code specified is appropriate to use.
         */
        public static final int UNSPECIFIED_ERROR = 0x1F;
        /**
         * The Unsupported LMP Parameter Value / Unsupported LL Parameter Value error code indicates that an LMP PDU
         * or an LL Control PDU contains at least one parameter value that is not supported by the Controller at
         * this time. This is normally used after a long negotiation procedure, for example during an LMP_hold_req,
         * LMP_sniff_req and LMP_encryption_key_size_req PDU exchanges. This may be used by the Link Layer, for
         * example during the Connection Parameters Request Link Layer Control procedure.
         */
        public static final int UNSUPPORTED_LMP_OR_LL_PARAMETER_VALUE = 0x20;
        /**
         * The Role Change Not Allowed error code indicates that a Controller will not allow a role change at this
         * time.
         */
        public static final int ROLE_CHANGE_NOT_ALLOWED = 0x21;
        /**
         * The LMP Response Timeout / LL Response Timeout error code indicates that an LMP transaction failed to
         * respond within the LMP response timeout or an LL transaction failed to respond within the LL response
         * timeout.
         */
        public static final int LMP_OR_LL_RESPONSE_TIMEOUT = 0x22;
        /**
         * The LMP Error Transaction Collision / LL Procedure Collision error code indicates that an LMP transaction
         * or LL procedure has collided with the same transaction or procedure that is already in progress.
         */
        public static final int LMP_ERROR_TRANSACTION_COLLISION_OR_LL_PROCEDURE_COLLISION = 0x23;
        /**
         * The LMP PDU Not Allowed error code indicates that a Controller sent an LMP PDU with an OpCode that was
         * not allowed.
         */
        public static final int LMP_PDU_NOT_ALLOWED = 0x24;
        /**
         * The Encryption Mode Not Acceptable error code indicates that the requested encryption mode is not
         * acceptable at this time.
         */
        public static final int ENCRYPTION_MODE_NOT_ACCEPTABLE = 0x25;
        /**
         * The Link Key cannot be Changed error code indicates that a link key cannot be changed because a fixed
         * unit key is being used.
         */
        public static final int LINK_KEY_CANNOT_BE_CHANGED = 0x26;
        /**
         * The Requested QoS Not Supported error code indicates that the requested Quality of Service is not
         * supported.
         */
        public static final int REQUESTED_QOS_NOT_SUPPORTED = 0x27;
        /**
         * The Instant Passed error code indicates that an LMP PDU or LL PDU that includes an instant cannot be
         * performed because the instant when this would have occurred has passed.
         */
        public static final int INSTANT_PASSED = 0x28;
        /**
         * The Pairing With Unit Key Not Supported error code indicates that it was not possible to pair as a unit
         * key was requested and it is not supported.
         */
        public static final int PAIRING_WITH_UNIT_KEY_NOT_SUPPORTED = 0x29;
        /**
         * The Different Transaction Collision error code indicates that an LMP transaction or LL Procedure was
         * started that collides with an ongoing transaction.
         */
        public static final int DIFFERENT_TRANSACTION_COLLISION = 0x2A;
        /**
         * The QoS Unacceptable Parameter error code indicates that the specified quality of service parameters
         * could not be accepted at this time, but other parameters may be acceptable.
         */
        public static final int UNACCEPTABLE_PARAMETER = 0x2C;
        /**
         * The QoS Rejected error code indicates that the specified quality of service parameters cannot be accepted
         * and QoS negotiation should be terminated
         */
        public static final int QOS_REJECTED = 0x2D;
        /**
         * The Channel Assessment Not Supported error code indicates that the Controller cannot perform channel
         * assessment because it is not supported.
         */
        public static final int CHANNEL_CLASSIFICATION_NOT_SUPPORTED = 0x2E;
        /**
         * The Insufficient Security error code indicates that the HCI command or LMP PDU sent is only possible on
         * an encrypted link.
         */
        public static final int INSUFFICIENT_SECURITY = 0x2F;
        /**
         * The Parameter Out Of Mandatory Range error code indicates that a parameter value requested is outside the
         * mandatory range of parameters for the given HCI command or LMP PDU and the recipient does not accept that
         * value.
         */
        public static final int PARAMETER_OUT_OF_MANDATORY_RANGE = 0x30;
        /**
         * The Role Switch Pending error code indicates that a Role Switch is pending. This can be used when an HCI
         * command or LMP PDU cannot be accepted because of a pending role switch. This can also be used to notify a
         * peer device about a pending role switch.
         */
        public static final int ROLE_SWITCH_PENDING = 0x32;
        /**
         * The Reserved Slot Violation error code indicates that the current Synchronous negotiation was terminated
         * with the negotiation state set to Reserved Slot Violation.
         */
        public static final int RESERVED_SLOT_VIOLATION = 0x34;
        /**
         * The Role Switch Failed error code indicates that a role switch was attempted but it failed and the
         * original piconet structure is restored. The switch may have failed because the TDD switch or piconet
         * switch failed.
         */
        public static final int ROLE_SWITCH_FAILED = 0x35;
        /**
         * The Extended Inquiry Response Too Large error code indicates that the extended inquiry response, with the
         * requested requirements for FEC, is too large to fit in any of the packet types supported by the
         * Controller.
         */
        public static final int EXTENDED_INQUIRY_RESPONSE_TOO_LARGE = 0x36;
        /**
         * The Secure Simple Pairing Not Supported by Host error code indicates that the IO capabilities request or
         * response was rejected because the sending Host does not support Secure Simple Pairing even though the
         * receiving Link Manager does.
         */
        public static final int SECURE_SIMPLE_PAIRING_NOT_SUPPORTED_BY_HOST = 0x37;
        /**
         * The Host Busy - Pairing error code indicates that the Host is busy with another pairing operation and
         * unable to support the requested pairing. The receiving device should retry pairing again later.
         */
        public static final int HOST_BUSY_PAIRING = 0x38;
        /**
         * The Connection Rejected due to No Suitable Channel Found error code indicates that the Controller could
         * not calculate an appropriate value for the Channel selection operation.
         */
        public static final int CONNECTION_REJECTED_NO_SUITABLE_CHANNEL_FOUND = 0x39;
        /**
         * The Controller Busy error code indicates that the operation was rejected because the Controller was busy
         * and unable to process the request.
         */
        public static final int CONTROLLER_BUSY = 0x3A;
        /**
         * The Unacceptable Connection Parameters error code indicates that the remote device either terminated the
         * connection or rejected a request because of one or more unacceptable connection parameters.
         */
        public static final int UNACCEPTABLE_CONNECTION_PARAMETERS = 0x3B;
        /**
         * The Advertising Timeout error code indicates that advertising for a fixed duration completed or, for
         * directed advertising, that advertising completed without a connection being created. (Formerly called
         * Directed Advertising Timeout)
         */
        public static final int DIRECTED_ADVERTISING_TIMEOUT = 0x3C;
        /**
         * The Connection Terminated Due to MIC Failure error code indicates that the connection was terminated
         * because the Message Integrity Check (MIC) failed on a received packet.
         */
        public static final int CONNECTION_TERMINATED_MIC_FAILURE = 0x3D;
        /**
         * The Connection Failed to be Established error code indicates that the LL initiated a connection but the
         * connection has failed to be established.
         */
        public static final int CONNECTION_ESTABLISHMENT_FAILED = 0x3E;
        /**
         * The MAC of the 802.11 AMP was requested to connect to a peer, but the connection failed.
         */
        public static final int MAC_CONNECTION_FAILED = 0x3F;
        /**
         * The Coarse Clock Adjustment Rejected but Will Try to Adjust Using Clock Dragging error code indicates
         * that the master, at this time, is unable to make a coarse adjustment to the piconet clock, using the
         * supplied parameters. Instead the master will attempt to move the clock using clock dragging.
         */
        public static final int COARSE_CLOCK_ADJUSTMENT_REJECTED = 0x40;
        /**
         * The Type0 Submap Not Defined error code indicates that the LMP PDU is rejected because the Type 0 submap
         * is not currently defined.
         */
        public static final int TYPE_0_SUBMAP_NOT_DEFINED = 0x41;
        /**
         * The Unknown Advertising Identifier error code indicates that a command was sent from the Host that should
         * identify an Advertising or Sync handle, but the Advertising or Sync handle does not exist.
         */
        public static final int UNKNOWN_ADVERTISING_IDENTIFIER = 0x42;
        /**
         * The Limit Reached error code indicates that number of operations requested has been reached and has
         * indicated the completion of the activity (e.g., advertising or scanning).
         */
        public static final int LIMIT_REACHED = 0x43;
        /**
         * The Operation Cancelled by Host error code indicates a request to the Controller issued by the Host and
         * still pending was successfully canceled.
         */
        public static final int OPERATION_CANCELLED_BY_HOST = 0x44;
    }
}
