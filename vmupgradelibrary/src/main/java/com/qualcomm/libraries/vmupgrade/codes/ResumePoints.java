/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade.codes;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class contains all the resume points as defined in the VM Upgrade protocol the Device can send to the
 * Host through the {@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} packet.</p>
 * <p>Each resume point represents the step where the upgrade should restart once the INIT PHASE of the process has
 * been finished.</p>
 * <p>The resume points are defined from the VM library documentation. Except the last one of the
 * {@link ResumePoints.Enum enumeration} which is only used for display.</p>
 */
@SuppressWarnings("unused")
public final class ResumePoints {

    @SuppressWarnings("FieldCanBeLocal")
    private static final int RESUME_POINTS_COUNT = 5;

    /**
     * <p>The enumeration of all resume points.</p>
     */
    @IntDef(flag = true, value = { Enum.DATA_TRANSFER, Enum.VALIDATION, Enum.TRANSFER_COMPLETE, Enum.IN_PROGRESS,
            Enum.COMMIT })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Enum {
        /**
         * This is the resume point "0", that means the upgrade will start from the beginning, the UPGRADE_START_DATA_REQ
         * request.
         */
        byte DATA_TRANSFER = 0x00;
        /**
         * This is the 1st resume point, that means the upgrade should resume from the UPGRADE_IS_CSR_VALID_DONE_REQ
         * request.
         */
        byte VALIDATION = 0x01;
        /**
         * This is the 2nd resume point, that means the upgrade should resume from the UPGRADE_TRANSFER_COMPLETE_RES request.
         */
        byte TRANSFER_COMPLETE = 0x02;
        /**
         * This is the 3rd resume point, that means the upgrade should resume from the UPGRADE_IN_PROGRESS_RES request.
         */
        byte IN_PROGRESS = 0x03;
        /**
         * This is the 4th resume point, that means the upgrade should resume from the UPGRADE_COMMIT_CFM confirmation request.
         */
        byte COMMIT = 0x04;
    }

    /**
     * To get the number of resume points in this enumeration.
     *
     * @return
     *          the number of resume points.
     */
    public static int getLength () {
        return RESUME_POINTS_COUNT;
    }

    /**
     * To get the label for the corresponding resume point.
     *
     * @return The label which corresponds to the resume point.
     */
    public static String getLabel(@ResumePoints.Enum int step) {
        switch (step) {
            case Enum.DATA_TRANSFER:
                return "Data transfer";
            case Enum.VALIDATION:
                return "Data validation";
            case Enum.TRANSFER_COMPLETE:
                return "Data transfer complete";
            case Enum.IN_PROGRESS:
                return "Upgrade in progress";
            case Enum.COMMIT:
                return "Upgrade commit";
            default:
                return "Initialisation";
        }
    }

    /**
     * To get the ResumePoint corresponding to the given value.
     *
     * @param value
     *          The value for which we would like the corresponding Resume Point.
     *
     * @return The corresponding ResumePoint, and {@link ResumePoints.Enum#DATA_TRANSFER} as the default one if the
     * value does not have a corresponding ResumePoint.
     */
    public static @ResumePoints.Enum int getResumePoint (byte value) {
        switch (value) {
            case Enum.DATA_TRANSFER:
                return Enum.DATA_TRANSFER;
            case Enum.VALIDATION:
                return Enum.VALIDATION;
            case Enum.TRANSFER_COMPLETE:
                return Enum.TRANSFER_COMPLETE;
            case Enum.IN_PROGRESS:
                return Enum.IN_PROGRESS;
            case Enum.COMMIT:
                return Enum.COMMIT;
            default:
                return Enum.DATA_TRANSFER;
        }
    }

}
