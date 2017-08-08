/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade.codes;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.qualcomm.libraries.vmupgrade.VMUUtils;
import com.qualcomm.libraries.vmupgrade.packet.VMUPacket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class contains the different operation codes used in the {@link VMUPacket VMU packets} exchanged between the
 * Host and the Device during the upgrade process.</p> <p>It also contains complementary information about values or
 * packets structure for certain operation codes as defined in the protocol.</p>
 */
@SuppressWarnings({"deprecation", "unused"})
public final class OpCodes {

    /* --------------------------------------- OPCODES --------------------------------------- */

    /**
     * <p>The enumeration of all different operation codes.</p> <p/> <p>Each Operation code is defined by the
     * following:</p> <dl> <dt><b>Content</b></dt><dd>The information which should be in the "data" part of the
     * packet.</dd> <dt><b>Previous and next messages</b></dt><pp>The operation code of the messages which should happen
     * before and after this one.</pp> </dl>
     */
    @IntDef(flag = true, value = {Enum.UPGRADE_START_REQ, Enum.UPGRADE_START_CFM, Enum.UPGRADE_DATA_BYTES_REQ,
            Enum.UPGRADE_DATA, Enum.UPGRADE_ABORT_REQ, Enum.UPGRADE_ABORT_CFM, Enum.UPGRADE_TRANSFER_COMPLETE_IND,
            Enum.UPGRADE_TRANSFER_COMPLETE_RES, Enum.UPGRADE_IN_PROGRESS_RES, Enum.UPGRADE_COMMIT_REQ, Enum
            .UPGRADE_COMMIT_CFM, Enum.UPGRADE_ERROR_WARN_IND, Enum.UPGRADE_COMPLETE_IND, Enum.UPGRADE_SYNC_REQ,
            Enum.UPGRADE_START_DATA_REQ, Enum.UPGRADE_IS_VALIDATION_DONE_REQ, Enum.UPGRADE_IS_VALIDATION_DONE_CFM,
            Enum.UPGRADE_ERROR_WARN_RES, Enum.UPGRADE_ERASE_SQIF_CFM, Enum.UPGRADE_ERASE_SQIF_REQ,
            Enum.UPGRADE_SUSPEND_IND, Enum.UPGRADE_RESUME_IND, Enum.UPGRADE_PROGRESS_REQ, Enum.UPGRADE_PROGRESS_CFM,
            Enum.UPGRADE_IN_PROGRESS_IND, Enum.UPGRADE_SYNC_AFTER_REBOOT_REQ, Enum.UPGRADE_VERSION_REQ,
            Enum.UPGRADE_VERSION_CFM, Enum.UPGRADE_VARIANT_REQ, Enum.UPGRADE_VARIANT_CFM, Enum.UPGRADE_SYNC_CFM })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Enum {

        /**
         * <p>To request an upgrade procedure to start.</p> <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} from device.</dd> <dt><b>Next
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from the device.</dd> </dl>
         */
        byte UPGRADE_START_REQ = 0x01;

        /**
         * <p>To confirm the start of the upgrade procedure.</p> <dl> <dt><b>Content</b></dt><dd>Contains a value to
         * indicate if the Device is ready for the upgrade, see {@link UpgradeStartCFM}.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_START_REQ UPGRADE_START_REQ}</dd> <dt><b>Next
         * message</b></dt><dd>depends on the {@link ResumePoints} value received by the Host in the {@link
         * OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} message: <table> <tr> <td>{@link
         * ResumePoints.Enum#DATA_TRANSFER DATA_TRANSFER}</td> <td> &#8658; {@link OpCodes.Enum#UPGRADE_START_REQ} from
         * application.</td> </tr> <tr> <td>{@link ResumePoints.Enum#VALIDATION VALIDATION}</td> <td> &#8658; {@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ} from application.</td> </tr> <tr> <td>{@link
         * ResumePoints.Enum#TRANSFER_COMPLETE TRANSFER_COMPLETE}</td> <td> &#8658; {@link
         * OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES} from application.</td> </tr> <tr> <td>{@link
         * ResumePoints.Enum#IN_PROGRESS IN_PROGRESS}</td> <td> &#8658; {@link OpCodes.Enum#UPGRADE_IN_PROGRESS_RES}
         * from application.</td> </tr> <tr> <td>{@link ResumePoints.Enum#COMMIT COMMIT}</td> <td> &#8658; {@link
         * OpCodes.Enum#UPGRADE_COMMIT_CFM} from application.</td> </tr> </table> </dd> </dl>
         */
        byte UPGRADE_START_CFM = 0x02;

        /**
         * <p>To request the section of the upgrade image file bytes array expected by the board.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>The length and the offset of the required section from the upgrade image
         * file.</dd> <dt><b>Previous message</b></dt><dd> <ul style="list-style-type:none"> <li>{@link
         * OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA} from application</li> <li>{@link OpCodes.Enum#UPGRADE_START_DATA_REQ
         * UPGRADE_START_DATA_REQ} from application</li> </ul> </dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA} from the application.</dd> </dl>
         */
        byte UPGRADE_DATA_BYTES_REQ = 0x03;

        /**
         * <p>To transfer sections of the upgrade image file to the board.</p> <dl> <dt><b>Content</b></dt><dd>The
         * section from the upgrade file which has been requested by the Device.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} from device.</dd>
         * <dt><b>Next message</b></dt><dd> <ul style="list-style-type:none"> <li>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from application.</li> <li>{@link
         * OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} from device.</li> </ul> </dd> </dl>
         */
        byte UPGRADE_DATA = 0x04;

        /**
         * @deprecated <p>Was sent by the device.</p> <p>The device may send this message to suspend transmission of
         * {@link Enum#UPGRADE_DATA UPGRADE_DATA} messages from the Host. This is used as flow control when the device
         * is busy and cannot accept more data.</p>
         */
        byte UPGRADE_SUSPEND_IND = 0x05;

        /**
         * @deprecated <p>Was sent by device.</p> <p>If the device has sent an {@link Enum#UPGRADE_SUSPEND_IND
         * UPGRADE_SUSPEND_IND} message to the Host it will resume transmission of {@link Enum#UPGRADE_DATA
         * UPGRADE_DATA} messages by sending this message</p>
         */
        byte UPGRADE_RESUME_IND = 0x06;

        /**
         * <p>To abort the upgrade procedure.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>any message or none.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM} from device.</dd>
         * </dl>
         */
        byte UPGRADE_ABORT_REQ = 0x07;

        /**
         * <p>To confirm the abortion of the upgrade</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ} from application.</dd> <dt><b>Next
         * message</b></dt><dd>None: disconnection of the upgrade?</dd> </dl>
         */
        byte UPGRADE_ABORT_CFM = 0x08;

        /**
         * @deprecated <p>Was sent by Host.</p> <p>The host can use this message to request an update on the
         * progress of the upgrade image download. The device will respond with an {@link Enum#UPGRADE_PROGRESS_CFM
         * UPGRADE_PROGRESS_CFM} message</p>
         */
        byte UPGRADE_PROGRESS_REQ = 0x09;

        /**
         * @deprecated <p>Was sent by Device.</p> <p>The device uses this message to respond to an {@link
         * Enum#UPGRADE_PROGRESS_REQ UPGRADE_PROGRESS_REQ} message from the host. It indicates the current percentage of
         * completion of the upgrade image file download from the host.</p>
         */
        byte UPGRADE_PROGRESS_CFM = 0x0A;

        /**
         * <p>To indicate the upgrade image file has successfully been received and validated.</p>
         * <p/>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from application.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES
         * UPGRADE_TRANSFER_COMPLETE_RES} from application.</dd> </dl>
         */
        byte UPGRADE_TRANSFER_COMPLETE_IND = 0x0B;

        /**
         * <p>To respond to the {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND} message
         * .</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains {@link UpgradeTransferCompleteRES.Action#ABORT ABORT} or {@link
         * UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE} information.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND} from
         * device.</dd> <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} from
         * application after the reboot of the device.</dd> </dl>
         */
        byte UPGRADE_TRANSFER_COMPLETE_RES = 0x0C;

        /**
         * @deprecated <p>Was sent by Device.</p> <p>Following reboot of the device to perform the upgrade, the device
         * will reconnect to the host.</p>
         */
        byte UPGRADE_IN_PROGRESS_IND = 0x0D;

        /**
         * <p>To inform the Device that the Host would like to continue the upgrade process.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains {@link UpgradeInProgressRES.Action#CONTINUE CONTINUE}
         * information.</dd> <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_START_CFM
         * UPGRADE_START_CFM} which should contain the Resume point 3: {@link ResumePoints.Enum#IN_PROGRESS
         * IN_PROGRESS}.</dd> <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ}
         * from the device.</dd> </dl>
         */
        byte UPGRADE_IN_PROGRESS_RES = 0x0E;

        /**
         * <p>Used by the board to indicate it is ready for permission to commit the upgrade.</p> <dl>
         * <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_IN_PROGRESS_RES UPGRADE_IN_PROGRESS_RES} from the Host.</dd> <dt><b>Next
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_COMMIT_CFM UPGRADE_COMMIT_CFM} from the Host.</dd> </dl>
         */
        byte UPGRADE_COMMIT_REQ = 0x0F;

        /**
         * <p>To respond to the {@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ} message from the board.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>0x00 to indicate to continue the upgrade, 0x01 to abort. See {@link
         * UpgradeCommitCFM UpgradeCommitCFM}.</dd> <dt><b>Previous message</b></dt><dd>Two possibilities:<ul><li>{@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from the Device which should contain the Resume point 4:
         * {@link ResumePoints.Enum#COMMIT COMMIT}.</li> <li>{@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ}
         * from the Device.</li></ul></dd> <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND} from Device.</dd> </dl>
         */
        byte UPGRADE_COMMIT_CFM = 0x10;

        /**
         * <p>Used by the Device to inform the application about errors or warnings. Errors are considered as fatal.
         * Warnings are considered as informational.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains a {@link ReturnCodes ReturnCodes}.</dd> <dt><b>Previous
         * message</b></dt><dd>none</dd> <dt><b>Next message</b></dt><dd>depends on the Return Code and any user
         * action.</dd> </dl>
         */
        byte UPGRADE_ERROR_WARN_IND = 0x11;

        /**
         * <p>Used by the board to indicate the upgrade has been completed.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_COMMIT_CFM UPGRADE_COMMIT_CFM} from Host.</dd> <dt><b>Next message</b></dt><dd>None,
         * that one is the last one of a successful upgrade.</dd> </dl>
         */
        byte UPGRADE_COMPLETE_IND = 0x12;

        /**
         * <p>Used by the application to synchronize with the board before any other protocol message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>ID of the upgrade which corresponds to the MD5 check sum of the upgrade
         * file.</dd> <dt><b>Previous message</b></dt><dd>None, that one is the initiator of the process.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} from Device.</dd>
         * </dl>
         */
        byte UPGRADE_SYNC_REQ = 0x13;

        /**
         * <p>Used by the board to respond to the {@link OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>A {@link ResumePoints} value.</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_START_REQ} from Device.</dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_REQ UPGRADE_START_REQ} from Device.</dd> </dl>
         */
        byte UPGRADE_SYNC_CFM = 0x14;

        /**
         * <p>Used by the Host to start a data transfer.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from Device.</dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} from Device.</dd> </dl>
         */
        byte UPGRADE_START_DATA_REQ = 0x15;

        /**
         * <p>Used by the Host to request for executable partition validation status.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>Three possibilities from
         * Device: <ul><li>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_CFM UPGRADE_IS_VALIDATION_DONE_CFM}</li>
         * <li>{@link OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA}</li> <li>{@link OpCodes.Enum#UPGRADE_START_CFM
         * UPGRADE_START_CFM}</li> </ul></dd> <dt><b>Next message</b></dt><dd>Two possibilities from Device:
         * <ul><li>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_CFM UPGRADE_IS_VALIDATION_DONE_CFM}</li> <li>{@link
         * OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND}</li> </ul></dd> </dl>
         */
        byte UPGRADE_IS_VALIDATION_DONE_REQ = 0x16;

        /**
         * <p>Used by the Device to respond to the {@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ
         * UPGRADE_IS_VALIDATION_DONE_REQ} message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from Device.</dd> <dt><b>Next
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from
         * Device.</dd> </dl>
         */
        byte UPGRADE_IS_VALIDATION_DONE_CFM = 0x17;

        /**
         * @deprecated <p>Was sent by Host.</p> <p>The Host must send this message reboot for commit.</p>
         */
        byte UPGRADE_SYNC_AFTER_REBOOT_REQ = 0x18;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VERSION_REQ = 0x19;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VERSION_CFM = 0x1A;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VARIANT_REQ = 0x1B;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VARIANT_CFM = 0x1C;

        /**
         * @deprecated <p>Was sent by Device.</p> <p>The device may send this message instead of {@link
         * Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ} (it depends on file content).</p>
         */
        byte UPGRADE_ERASE_SQIF_REQ = 0x1D;

        /**
         * @deprecated <p>Was sent by Host.</p> <p>The host must respond to the {@link Enum#UPGRADE_ERASE_SQIF_REQ
         * UPGRADE_ERASE_SQIF_REQ} message from the device with this message.</p>
         */
        byte UPGRADE_ERASE_SQIF_CFM = 0x1E;

        /**
         * <p>Used by the Host to confirm it received an error or a warning message from the board.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>The {@link ReturnCodes ReturnCodes} received.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_ERROR_WARN_IND UPGRADE_ERROR_WARN_IND} from Device.</dd>
         * <dt><b>Next message</b></dt><dd>Depends on the received {@link ReturnCodes ReturnCodes} value.</dd> </dl>
         */
        byte UPGRADE_ERROR_WARN_RES = 0x1F;
    }


    /* ******* STRUCTURE AND CONTENT FOR PACKETS INFORMATION ******* */

    /**
     * <p>Complementary information for the structure and content of the {@link Enum#UPGRADE_START_CFM
     * UPGRADE_START_REQ} message.</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeStartREQ {
        /**
         * length of the data for the {@link Enum#UPGRADE_START_REQ UPGRADE_START_REQ} message.
         */
        public static final byte DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3
     *      +--------+--------+--------+
     *      | STATUS |  BATTERY LEVEL  |
     *      +--------+--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeStartCFM {
        /**
         * <p>All the status which can be returned for the STATUS information.</p>
         */
        public class Status {

            /**
             * Value for an {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message when the device is ready to start
             * the upgrade process.
             */
            public static final byte SUCCESS = 0x00;
            /**
             * Value for an {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message when the device is not ready to
             * start the upgrade process.
             */
            public static final byte ERROR_APP_NOT_READY = 0x09;
        }

        /**
         * The offset to get the status information.
         */
        public static final int STATUS_OFFSET = 0;
        /**
         * To know how many bytes represent the status information.
         */
        public static final int STATUS_LENGTH = 1;
        /**
         * The offset to get the battery level information.
         */
        public static final int BATTERY_LEVEL_OFFSET = STATUS_OFFSET + STATUS_LENGTH;
        /**
         * Nb of bytes which represent the battery level information.
         */
        public static final int BATTERY_LEVEL_LENGTH = 2;
        /**
         * length of the data for the {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message.
         */
        public static final byte DATA_LENGTH = STATUS_LENGTH + BATTERY_LEVEL_LENGTH;
    }


    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_DATA_BYTES_REQ
     * UPGRADE_DATA_BYTES_REQ} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3        4        5        6        7        8
     *      +--------+--------+--------+--------+--------+--------+--------+--------+
     *      |     NUMBER OF BYTES REQUESTED     |         FILE START OFFSET         |
     *      +--------+--------+--------+--------+--------+--------+--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeDataBytesREQ {
        /**
         * <p>The number of bytes which contains the number of bytes of the uploading file to send.</p>
         */
        public static final int NB_BYTES_LENGTH = 4;
        /**
         * <p>The offset in the {@link Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} bytes data where the "number
         * of bytes to send" information starts.</p>
         */
        public static final int NB_BYTES_OFFSET = 0;
        /**
         * <p>The number of bytes which contains the byte offset within the upgrade file from which the host should
         * start transferring data to the device.</p>
         */
        public static final int FILE_OFFSET_LENGTH = 4;
        /**
         * <p>The offset in the {@link Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} bytes data where the file
         * offset information starts. .</p>
         */
        public static final int FILE_OFFSET_OFFSET = NB_BYTES_OFFSET + NB_BYTES_LENGTH;
        /**
         * The length for the data of the {@link Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} message.
         */
        public static final int DATA_LENGTH = FILE_OFFSET_LENGTH + NB_BYTES_LENGTH;

    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_DATA UPGRADE_DATA} message.
     * .</p>
     * <blockquote><pre>
     *      0 bytes       1       ...       n
     *      +-------------+--------+--------+
     *      | LAST PACKET |    DATA...
     *      +-------------+--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeData {
        /**
         * The offset which contains the last packet information in the UPGRADE_DATA data message.
         */
        public static final int LAST_PACKET_OFFSET = 0;

        /**
         * The number of bytes which contains the last packet information.
         */
        public static final int LAST_PACKET_LENGTH = 1;

        /**
         * All the values which can be sent to know if the {@link Enum#UPGRADE_DATA UPGRADE_DATA} message is sending the
         * last packet of the upgrade.
         */
        public class LastPacket {
            /**
             * Value for the first byte for data when we are sending the last {@link Enum#UPGRADE_DATA UPGRADE_DATA}
             * message.
             */
            public static final byte IS_LAST_PACKET = 0x01;
            /**
             * Value for the first byte for data when we are not sending the last {@link Enum#UPGRADE_DATA UPGRADE_DATA}
             * message.
             */
            public static final byte IS_NOT_LAST_PACKET = 0x00;
        }

        /**
         * <p>The offset which contains the file bytes to send.</p>
         */
        public static final int FILE_BYTES_OFFSET = LAST_PACKET_OFFSET + LAST_PACKET_LENGTH;
        /**
         * The minimum length for the data of the {@link Enum#UPGRADE_DATA UPGRADE_DATA} message.
         */
        public static final int MIN_DATA_LENGTH = LAST_PACKET_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ}
     * message..</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeAbortREQ {
        /**
         * The length for the data of the {@link Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ} message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM}
     * message..</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeAbortCFM {
        /**
         * The length for the data of the {@link Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM} message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_TRANSFER_COMPLETE_IND
     * UPGRADE_TRANSFER_COMPLETE_IND} message..</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeTransferCompleteIND {
        /**
         * The length for the data of the {@link Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND}
         * message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_TRANSFER_COMPLETE_RES
     * UPGRADE_TRANSFER_COMPLETE_RES} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public class UpgradeTransferCompleteRES {
        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public class Action {
            /**
             * Used by the application to confirm that the upgrade should continue.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm that the upgrade should abort.
             */
            public static final byte ABORT = 0x01;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        public static final int ACTION_LENGTH = 1;
        /**
         * The length of the data for this message.
         */
        public static final int DATA_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_IN_PROGRESS_RES
     * UPGRADE_IN_PROGRESS_RES} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public class UpgradeInProgressRES {
        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public class Action {
            /**
             * Used by the application to confirm to the board the user wishes to continue the upgrade process.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm that the upgrade should abort.
             */
            public static final byte ABORT = 0x01;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        public static final int ACTION_LENGTH = 1;
        /**
         * The length of the data for this message.
         */
        public static final int DATA_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_COMMIT_REQ
     * UPGRADE_COMMIT_REQ} message..</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeCommitREQ {
        /**
         * The length for the data of the {@link Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ} message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_COMMIT_CFM
     * UPGRADE_COMMIT_CFM} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public class UpgradeCommitCFM {
        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public class Action {
            /**
             * Used by the application to confirm the user wants to commit the upgrade.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm the user doesn't want to commit the upgrade for now.
             */
            public static final byte ABORT = 0x01;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        public static final int ACTION_LENGTH = 1;
        /**
         * The length of the data for this message.
         */
        public static final int DATA_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_ERROR_WARN_IND
     * UPGRADE_ERROR_WARN_IND} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |   RETURN CODE   |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeErrorWarnIND {
        /**
         * <p>The offset where the return code information starts.</p>
         */
        public static final int RETURN_CODE_OFFSET = 0;
        /**
         * <p>The number of bytes which contains the return code information.</p>
         */
        public static final int RETURN_CODE_LENGTH = 2;
        /**
         * Used by the application to build the confirmation sent with {@link Enum#UPGRADE_ERROR_WARN_IND
         * UPGRADE_ERROR_WARN_IND} message that it has received an error or warning message: see {@link ReturnCodes}.
         */
        public static final int DATA_LENGTH = RETURN_CODE_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_COMPLETE_IND
     * UPGRADE_COMPLETE_IND} message.</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeCompleteIND {
        /**
         * The length for the data of the {@link Enum#UPGRADE_COMPLETE_IND UPGRADE_COMPLETE_IND} message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3        4
     *      +--------+--------+--------+--------+
     *      |      IN PROGRESS IDENTIFIER       |
     *      +--------+--------+--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeSyncREQ {
        /**
         * The offset where the identifier information starts.
         */
        public static final int IDENTIFIER_OFFSET = 0;
        /**
         * The number of bytes which contain the identifier information.
         */
        public static final int IDENTIFIER_LENGTH = 4;
        /**
         * The length for the data of the {@link Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} message.
         */
        public static final int DATA_LENGTH = IDENTIFIER_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes            1        2        3        4        5                  6
     *      +------------------+--------+--------+--------+--------+------------------+
     *      |   RESUME POINT   |       IN PROGRESS IDENTIFIER      | PROTOCOL VERSION |
     *      +------------------+--------+--------+--------+--------+------------------+
     * </pre></blockquote>
     */
    public class UpgradeSyncCFM {
        /**
         * <p>The offset from where the resume point information starts.</p>
         */
        public static final int RESUME_POINT_OFFSET = 0;
        /**
         * <p>The number of bytes which contain the resume point information.</p>
         */
        public static final int RESUME_POINT_LENGTH = 1;
        /**
         * <p>The offset from where the identifier information starts.</p>
         */
        public static final int IDENTIFIER_OFFSET = RESUME_POINT_OFFSET + RESUME_POINT_LENGTH;
        /**
         * <p>The number of bytes which contain the identifier information.</p>
         */
        public static final int IDENTIFIER_LENGTH = 4;
        /**
         * <p>The offset from where the protocol version starts.</p>
         */
        public static final int PROTOCOL_VERSION_OFFSET = IDENTIFIER_OFFSET + IDENTIFIER_LENGTH;
        /**
         * <p>The number of bytes which contain the protocol version information.</p>
         */
        public static final int PROTOCOL_VERSION_LENGTH = 1;
        /**
         * The length for the data of the {@link Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} message.
         */
        public static final int DATA_LENGTH = RESUME_POINT_LENGTH + IDENTIFIER_LENGTH + PROTOCOL_VERSION_LENGTH;

    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_START_DATA_REQ
     * UPGRADE_START_DATA_REQ} message.</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeStartDataREQ {
        /**
         * The length for the data of the {@link Enum#UPGRADE_START_DATA_REQ UPGRADE_START_DATA_REQ} message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_REQ
     * UPGRADE_IS_VALIDATION_DONE_REQ} message.</p>
     * <blockquote><pre>
     *      no data
     * </pre></blockquote>
     */
    @SuppressWarnings("WeakerAccess")
    public class UpgradeIsValidationDoneREQ {
        /**
         * The length for the data of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ}
         * message.
         */
        public static final int DATA_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_CFM
     * UPGRADE_IS_VALIDATION_DONE_CFM} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |   WAITING TIME  |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeIsValidationDoneCFM {
        /**
         * The offset from where the waiting time information starts.
         */
        public static final int WAITING_TIME_OFFSET = 0;
        /**
         * The number of bytes which represent the waiting time information.
         */
        public static final int WAITING_TIME_LENGTH = 2;
        /**
         * The length for the data of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_CFM UPGRADE_IS_VALIDATION_DONE_CFM}
         * message.
         */
        public static final int DATA_LENGTH = WAITING_TIME_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_ERROR_WARN_RES
     * UPGRADE_ERROR_WARN_RES} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |    ERROR CODE   |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public class UpgradeErrorWarnRES {
        /**
         * The offset from where the error code information as a {@link com.qualcomm.libraries.vmupgrade.codes.ReturnCodes.Enum
         * ReturnCodes} starts.
         */
        public static final int ERROR_OFFSET = 0;
        /**
         * The number of bytes which represent the Error information.
         */
        public static final int ERROR_LENGTH = 2;
        /**
         * Used by the application to build the confirmation sent with {@link Enum#UPGRADE_ERROR_WARN_RES
         * UPGRADE_ERROR_WARN_RES} message that it has received an error or warning message.
         */
        public static final int DATA_LENGTH = ERROR_LENGTH;
    }


    /* ******* METHODS ******* */

    /**
     * <p>To retrieve the OpCode value from its byte value.</p>
     *
     * @param opCode
     *          The byte to obtain the corresponding {@link OpCodes.Enum OpCodes}.
     *
     * @return The corresponding OpCodes or -1 if not found.
     */
    public static @OpCodes.Enum int getOpCode(byte opCode) {
        switch (opCode) {
            case Enum.UPGRADE_START_REQ:
                return Enum.UPGRADE_START_REQ;

            case Enum.UPGRADE_START_CFM:
                return Enum.UPGRADE_START_CFM;

            case Enum.UPGRADE_DATA_BYTES_REQ:
                return Enum.UPGRADE_DATA_BYTES_REQ;

            case Enum.UPGRADE_DATA:
                return Enum.UPGRADE_DATA;

            case Enum.UPGRADE_ABORT_REQ:
                return Enum.UPGRADE_ABORT_REQ;

            case Enum.UPGRADE_ABORT_CFM:
                return Enum.UPGRADE_ABORT_CFM;

            case Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                return Enum.UPGRADE_TRANSFER_COMPLETE_IND;

            case Enum.UPGRADE_TRANSFER_COMPLETE_RES:
                return Enum.UPGRADE_TRANSFER_COMPLETE_RES;

            case Enum.UPGRADE_IN_PROGRESS_RES:
                return Enum.UPGRADE_IN_PROGRESS_RES;

            case Enum.UPGRADE_COMMIT_REQ:
                return Enum.UPGRADE_COMMIT_REQ;

            case Enum.UPGRADE_COMMIT_CFM:
                return Enum.UPGRADE_COMMIT_CFM;

            case Enum.UPGRADE_ERROR_WARN_IND:
                return Enum.UPGRADE_ERROR_WARN_IND;

            case Enum.UPGRADE_COMPLETE_IND:
                return Enum.UPGRADE_COMPLETE_IND;

            case Enum.UPGRADE_SYNC_REQ:
                return Enum.UPGRADE_SYNC_REQ;

            case Enum.UPGRADE_SYNC_CFM:
                return Enum.UPGRADE_SYNC_CFM;

            case Enum.UPGRADE_START_DATA_REQ:
                return Enum.UPGRADE_START_DATA_REQ;

            case Enum.UPGRADE_IS_VALIDATION_DONE_REQ:
                return Enum.UPGRADE_IS_VALIDATION_DONE_REQ;

            case Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                return Enum.UPGRADE_IS_VALIDATION_DONE_CFM;

            case Enum.UPGRADE_ERROR_WARN_RES:
                return Enum.UPGRADE_ERROR_WARN_RES;

            case Enum.UPGRADE_ERASE_SQIF_CFM:
                return Enum.UPGRADE_ERASE_SQIF_CFM;

            case Enum.UPGRADE_ERASE_SQIF_REQ:
                return Enum.UPGRADE_ERASE_SQIF_REQ;

            case Enum.UPGRADE_SUSPEND_IND:
                return Enum.UPGRADE_SUSPEND_IND;

            case Enum.UPGRADE_RESUME_IND:
                return Enum.UPGRADE_RESUME_IND;

            case Enum.UPGRADE_PROGRESS_REQ:
                return Enum.UPGRADE_PROGRESS_REQ;

            case Enum.UPGRADE_PROGRESS_CFM:
                return Enum.UPGRADE_PROGRESS_CFM;

            case Enum.UPGRADE_IN_PROGRESS_IND:
                return Enum.UPGRADE_IN_PROGRESS_IND;

            case Enum.UPGRADE_SYNC_AFTER_REBOOT_REQ:
                return Enum.UPGRADE_SYNC_AFTER_REBOOT_REQ;

            case Enum.UPGRADE_VERSION_REQ:
                return Enum.UPGRADE_VERSION_REQ;

            case Enum.UPGRADE_VERSION_CFM:
                return Enum.UPGRADE_VERSION_CFM;

            case Enum.UPGRADE_VARIANT_REQ:
                return Enum.UPGRADE_VARIANT_REQ;

            case Enum.UPGRADE_VARIANT_CFM:
                return Enum.UPGRADE_VARIANT_CFM;

            default:
                return -1;
        }

    }


    /**
     * <p>To get a readable version of the given OpCodes.</p>
     *
     * @param opCode
     *              The OpCodes.
     *
     * @return a human readable information for an OpCodes.
     */
    public static String getString(@OpCodes.Enum int opCode) {
        switch (opCode) {
            case Enum.UPGRADE_START_REQ:
                return "UPGRADE_START_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_START_CFM:
                return "UPGRADE_START_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_DATA_BYTES_REQ:
                return "UPGRADE_DATA_BYTES_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_DATA:
                return "UPGRADE_DATA " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ABORT_REQ:
                return "UPGRADE_ABORT_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ABORT_CFM:
                return "UPGRADE_ABORT_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                return "UPGRADE_TRANSFER_COMPLETE_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_TRANSFER_COMPLETE_RES:
                return "UPGRADE_TRANSFER_COMPLETE_RES " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_IN_PROGRESS_RES:
                return "UPGRADE_IN_PROGRESS_RES " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_COMMIT_REQ:
                return "UPGRADE_COMMIT_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_COMMIT_CFM:
                return "UPGRADE_COMMIT_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ERROR_WARN_IND:
                return "UPGRADE_ERROR_WARN_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_COMPLETE_IND:
                return "UPGRADE_COMPLETE_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_SYNC_REQ:
                return "UPGRADE_SYNC_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_SYNC_CFM:
                return "UPGRADE_SYNC_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_START_DATA_REQ:
                return "UPGRADE_START_DATA_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_IS_VALIDATION_DONE_REQ:
                return "UPGRADE_IS_VALIDATION_DONE_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                return "UPGRADE_IS_VALIDATION_DONE_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ERROR_WARN_RES:
                return "UPGRADE_ERROR_WARN_RES " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ERASE_SQIF_CFM:
                return "UPGRADE_ERASE_SQIF_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_ERASE_SQIF_REQ:
                return "UPGRADE_ERASE_SQIF_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_SUSPEND_IND:
                return "UPGRADE_SUSPEND_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_RESUME_IND:
                return "UPGRADE_RESUME_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_PROGRESS_REQ:
                return "UPGRADE_PROGRESS_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_PROGRESS_CFM:
                return "UPGRADE_PROGRESS_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_IN_PROGRESS_IND:
                return "UPGRADE_IN_PROGRESS_IND " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_SYNC_AFTER_REBOOT_REQ:
                return "UPGRADE_SYNC_AFTER_REBOOT_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_VERSION_REQ:
                return "UPGRADE_VERSION_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_VERSION_CFM:
                return "UPGRADE_VERSION_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_VARIANT_REQ:
                return "UPGRADE_VARIANT_REQ " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            case Enum.UPGRADE_VARIANT_CFM:
                return "UPGRADE_VARIANT_CFM " + VMUUtils.getHexadecimalStringTwoBytes(opCode);

            default:
                return "UNKNOWN OPCODE " + VMUUtils.getHexadecimalStringTwoBytes(opCode);
        }

    }

}
