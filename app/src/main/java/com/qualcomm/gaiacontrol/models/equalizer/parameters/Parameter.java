/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.models.equalizer.parameters;

/**
 * <p>This class defines all the properties for each customizable parameter. Each parameter is defined
 * by a range, a value and a {@link ParameterType parameter type}.</p>
 * <p>The value corresponds to an <code>Integer</code> in a range defined by the parameter type.</p>
 * <p>This value is the raw value get from the device. To get a human readable value for a parameter it is calculated
 * as follows: <code>readableValue = deviceValue / factor</code></p>
 * <p>The bounds follow the same principle: they are kept as bounds of the raw value and to get a human readable
 * version the same calculation will be used.</p>
 */
public abstract class Parameter {

    // ====== CONSTS ===============================================================================

    /**
     * The allocated case in an array to get the minimum value for a parameter.
     */
    private static final int MIN_BOUND_OFFSET = 0;
    /**
     * The allocated case in an array to get the maximum value for a parameter.
     */
    private static final int MAX_BOUND_OFFSET = 1;


    // ====== PRIVATE FIELDS ===============================================================================

    /**
     * The bounds for the raw range of this parameter value.
     */
    private final int[] mRawBounds = new int[2];
    /**
     * The label values for the readable bounds of the parameter.
     */
    private final String[] mLabelBounds = new String[2];
    /**
     * To know if this parameter has to be updated.
     */
    private boolean isUpToDate = false;
    /**
     * To know the type if this parameter.
     */
    private final ParameterType mType;
    /**
     * The current raw value of this parameter.
     */
    private int mRawValue;
    /**
     * The factor used to calculate the real value of this parameter.
     */
    private final int mFactor;


    // ====== PACKAGE FIELD ===============================================================================

    /**
     * To know if this parameter can be modified - depending on the filter of a band, a parameter can be no modifiable.
     */
    boolean isConfigurable = false;


    // ====== CONSTRUCTOR ===============================================================================

    /**
     * To build a Parameter object defined by its type.
     *
     * @param parameterType
     *              the type of the parameter.
     */
    Parameter(ParameterType parameterType) {
        mType = parameterType;
        mFactor = getFactor();
    }


    // ====== GETTERS ===============================================================================

    /**
     * <p>To get the type of this parameter.</p>
     *
     * @return The type of this parameter.
     */
    public ParameterType getParameterType() {
        return mType;
    }

    /**
     * <p>To get the raw value of this parameter as known by the device.</p>
     *
     * return The value contains in the integer range defined by the factor of this parameter.
     */
    public int getValue() {
        return mRawValue;
    }

    /**
     * <p>To get the position of this parameter value in a range from 0 to the value given by
     * {@link #getBoundsLength() getBoundsLength}. <i>This can be used to set up a
     * {@link android.widget.SeekBar SeekBar} for instance.</i></p>
     * <p>This method will calculate the returned value as follows: <code>rawValue - minBound</code>.</p>
     *
     * @return the position of the value in a range from 0 to the length of this parameter range.
     */
    public int getPositionValue() {
        return mRawValue - mRawBounds[MIN_BOUND_OFFSET];
    }

    /**
     * <p>To get the length of range. This could be used to create an interval from 0 to this
     * value, for instance to know the maximum bound of a {@link android.widget.SeekBar SeekBar}.</p>
     *
     * @return the difference between the maximum bound and the minimum bound.
     */
    public int getBoundsLength() {
        return mRawBounds[MAX_BOUND_OFFSET] - mRawBounds[MIN_BOUND_OFFSET];
    }

    /**
     * <p>To know if this parameter is configurable. A parameter is configurable depending on the filter set up on
     * the device for the band to which this parameter is attached.</p>
     * <p>If this parameter represents a Master Gain, the parameter is always configurable.</p>
     *
     * @return true if it is configurable, false otherwise.
     */
    public boolean isConfigurable() {
        return isConfigurable;
    }

    /**
     * <p>To know if this parameter is up to date.</p>
     * <p>When one of the parameters of the bank had changed it can impact all other parameters. <i>For
     * instance, if the filter of a band is set up to a new value, the gain, quality and frequency values of the band
     * have to be updated as well.</i> In which case all the parameters are set up to an out of date state.</p>
     *
     * @return true if this parameter has been defined as up to date, false if it has to be updated.
     */
    public boolean isUpToDate() {
        return isUpToDate;
    }

    /**
     * <p>To get the label which corresponds to the minimum bound - as a readable value - for the range of this
     * parameter.</p>
     *
     * @return A readable value with the unit corresponding to the minimum bound for this parameter.
     */
    public String getLabelMinBound() {
        if (isConfigurable) {
            return mLabelBounds[MIN_BOUND_OFFSET];
        }
        else {
            return "";
        }
    }

    /**
     * To get the label which corresponds to the maximum bound - as a readable value - for the range of this parameter.
     *
     * @return A readable value with the unit corresponding to the maximum bound for this parameter.
     */
    public String getLabelMaxBound() {
        if (isConfigurable) {
            return mLabelBounds[MAX_BOUND_OFFSET];
        } else {
            return "";
        }
    }

    /**
     * <p>To get the label which corresponds to the readable value of this parameter.</p>
     * <p>This method first calculates the readable value of the parameter and then uses the method
     * {@link #getLabel(double) getLabel} to get the corresponding String value which includes the unit.</p>
     *
     * @return The readable value with the unit corresponding to this parameter.
     */
    public String getLabelValue() {
        double realValue = mRawValue / (double) mFactor;
        return getLabel(realValue);
    }

    /**
     * <p>To get the raw value of the minimum bound of the range.</p>
     *
     * @return the minimum bound.
     */
    int getMinBound() {
        return mRawBounds[MIN_BOUND_OFFSET];
    }

    /**
     * <p>To get the raw value of the maximum bound of the range.</p>
     *
     * @return the maximum bound.
     */
    int getMaxBound() {
        return mRawBounds[MAX_BOUND_OFFSET];
    }


    // ====== SETTERS ===============================================================================

    /**
     * <p>To define the raw value of this parameter.</p>
     */
    public void setValue(int parameterValue) {
        isUpToDate = true;
        mRawValue = parameterValue;
    }

    /**
     * <p>To define the raw value of this parameter by giving the corresponding proportion value in an interval from
     * 0 to the range between the 2 bounds of this parameter.</p>
     *
     * @param lengthValue
     *          The corresponding proportion value.
     */
    public void setValueFromProportion(int lengthValue) {
        mRawValue = lengthValue + mRawBounds[MIN_BOUND_OFFSET];
    }

    /**
     * To define this parameter as configurable by giving its new readable range bounds values.
     *
     * @param minBound
     *          The minimum bound of the readable range.
     * @param maxBound
     *          The maximum bound of the readable range.
     */
    public void setConfigurable(double minBound, double maxBound) {
        isConfigurable = true;
        setBound(MIN_BOUND_OFFSET, minBound);
        setBound(MAX_BOUND_OFFSET, maxBound);
    }

    /**
     * To define this parameter as not configurable by the user.
     */
    void setNotConfigurable() {
        isConfigurable = false;
    }

    /**
     * <p>To define this parameter as out of date.</p>
     * <p>When one of the parameters of the bank has changed it can impact all other parameters. <i>For
     * instance, if the filter of a band is set up to a new value, the gain, quality and frequency values of the band
     * have to be updated as well.</i> In which case all the parameters are set up to an out of date state.</p>
     */
    public void hasTobeUpdated() {
        isUpToDate = false;
    }


    // ====== PRIVATE METHODS ===============================================================================

    /**
     * <p>To define one of the bounds of this parameter range. This method will create the label corresponding to
     * the readable bound, and will create the raw value corresponding to the raw range.</p>
     *
     * @param position
     *              The position as one of the followings: {@link #MIN_BOUND_OFFSET MIN_BOUND_OFFSET},
     *              {@link #MAX_BOUND_OFFSET MAX_BOUND_OFFSET}.
     * @param value
     *              The value to set to the bound.
     */
    private void setBound(int position, double value) {
        mLabelBounds[position] = getLabel(value);
        mRawBounds[position] = (int) (value * mFactor);
    }


    // ====== ABSTRACT METHODS ===============================================================================

    /**
     * To get a human readable value to display.
     *
     * @param value
     *          the value to get as a readable one.
     *
     * @return A formatted value readable for humans.
     */
    abstract String getLabel(double value);

    /**
     * <p>To get the factor corresponding to this parameter.</p>
     *
     * @return The factor defined by the type of this parameter.
     */
    abstract int getFactor();
}
