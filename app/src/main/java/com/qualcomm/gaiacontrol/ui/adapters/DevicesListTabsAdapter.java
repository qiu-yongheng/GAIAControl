/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.ui.fragments.DevicesListFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class DevicesListTabsAdapter extends FragmentPagerAdapter {

    /**
     * <p>To define the list as representing scanned devices.</p>
     */
    public final static int SCANNED_LIST_TYPE = 0;
    /**
     * <p>To define the list as representing bonded devices.</p>
     */
    public final static int BONDED_LIST_TYPE = SCANNED_LIST_TYPE + 1;
    /**
     * <p>The number of tabs. There are two: one for the scanned devices, one for the bonded devices.</p>
     */
    private final static int COUNT = 2;
    /**
     * For debug mode, the tag to display for logs.
     */
    private final static String TAG = "DevicesListTabsAdapter";
    /**
     * To know which fragment is the current one displayed to the user.
     */
    private int mCurrentFragment = 0;
    /**
     * <p>To have access to each of the fragments managed by this tab adapter.</p>
     */
    private final DevicesListFragment[] mFragments = new DevicesListFragment[COUNT];
    /**
     * To have an instance of the context to be able to load resources.
     */
    private final Context mContext;

    /**
     * default constructor.
     *
     * @param fm The fragment manager to use to display the fragments.
     */
    public DevicesListTabsAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position != SCANNED_LIST_TYPE && position != BONDED_LIST_TYPE) {
            Log.e(TAG, "Item requested at position " + position + " doesn't exist, position min: 0 and position max: " +
                    COUNT);
            return null;
        }
        if (mFragments[position] == null) {
            mFragments[position] = DevicesListFragment.newInstance(position);
        }
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    /**
     * <p>This method returns the selected device depending on the current displayed fragment.</p>
     *
     * @return the selected device in the current fragment or null if there is no selected item in the current fragment.
     */
    public BluetoothDevice getSelectedDevice() {
        DevicesListFragment fragment = mFragments[mCurrentFragment];
        if (fragment != null && fragment.hasSelection())
            return fragment.getSelectedDevice();
        else
            return null;
    }

    /**
     * <p>To know if the current fragment has a selected device.</p>
     *
     * @return true if there is a selected device in the current displayed fragment, false otherwise.
     */
    public boolean hasSelection() {
        // check if the current fragment is part of the known fragments in mFragments
        // then check if the current fragment exists and has a selected device
        return mCurrentFragment >= 0
                && mCurrentFragment < mFragments.length
                && mFragments[mCurrentFragment] != null
                && mFragments[mCurrentFragment].hasSelection();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case SCANNED_LIST_TYPE:
                return mContext.getString(R.string.scanned_devices);
            case BONDED_LIST_TYPE:
                return mContext.getString(R.string.bonded_devices);
        }
        return null;
    }

    /**
     * <p>This method updates the state of the adapter with the new tab position.</p>
     *
     * @param newPosition
     *          The new current position of the tabs.
     */
    public void onPageSelected(int newPosition) {
        DevicesListFragment newFragment = mFragments[newPosition];
        if (newFragment != null) {
            newFragment.onResumeFragment();
        }
        if (mFragments[mCurrentFragment] != null) {
            mFragments[mCurrentFragment].onPauseFragment();
        }
        mCurrentFragment = newPosition;
    }

    /**
     * <p>When the refresh of the list is finished this method is called to update the UI.</p>
     *
     * @param type
     *          The list type which has to finish the refreshing.
     */
    public void onScanFinished(int type) {
        if (type < COUNT && type >= 0 && mFragments[type] != null) {
            mFragments[type].stopRefreshing();
        }
    }
}
