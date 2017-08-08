/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.libraries.vmupgrade;

/**
 * <p>This class encapsulates the progress during the file upload.</p>
 * <p>It provides the current percentage and an estimation of the remaining time. The estimated remaining time is
 * based on the spent time since the last resume of the upgrade.</p>
 */
public class UploadProgress {

    /**
     * Represents the percentage of the file which has been uploaded on the Device.
     */
    private final double mPercentage;
    /**
     * Represents the estimated remaining time.
     */
    private final long mRemainingTime;

    /**
     * <p>Constructor to build an instance of this class. The given values cannot be changed.</p>
     *
     * @param percentage
     *        The percentage of the file which has already been uploaded on the Device. This value has to be between
     *        0 and 100 included.
     * @param remainingTime
     *        The estimated remaining time. This value has to be greater than 0.
     */
    public UploadProgress (double percentage, long remainingTime) {
        mPercentage = (percentage < 0) ? 0 : (percentage > 100) ? 100 : percentage;
        mRemainingTime = remainingTime < 0 ? 0 : remainingTime;
    }

    /**
     * To get the exact percentage of the file which has been uploaded on the Device.
     *
     * @return a value between 0 and 100 included.
     */
    public double getPercentage() {
        return mPercentage;
    }

    /**
     * To get the exact value of the remaining time.
     *
     * @return The exact value of the remaining time in ms.
     */
    public long getRemainingTime() {
        return mRemainingTime;
    }

}
