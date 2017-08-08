/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.ui.adapters.DevicesListAdapter;
import com.qualcomm.gaiacontrol.ui.adapters.DevicesListTabsAdapter;

/**
 * A fragment to mange the display of a list of Bluetooth devices.
 */
public class DevicesListFragment extends Fragment implements DevicesListAdapter.IDevicesListAdapterListener {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_LIST_TYPE = "list_type";
    /**
     * The listener to trigger events from this fragment.
     */
    private DevicesListFragmentListener mListener;
    /**
     * To know which type of list is managed by this fragment:
     * {@link DevicesListTabsAdapter#SCANNED_LIST_TYPE SCANNED_LIST_TYPE} or
     * {@link DevicesListTabsAdapter#BONDED_LIST_TYPE BONDED_LIST_TYPE}.
     */
    private int mListType = -1;
    /**
     * The data set adapter for the Bluetooth devices managed here.
     */
    private DevicesListAdapter mDevicesListAdapter;
    /**
     * The layout which is in charge of the "pull to refresh" feature.
     */
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * Th text view to display a message when no devices are available.
     */
    private TextView mTVNoDeviceAvailable;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static DevicesListFragment newInstance(int type) {
        DevicesListFragment fragment = new DevicesListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    // default empty constructor, required for Fragment.
    public DevicesListFragment() {
    }

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override // Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DevicesListFragmentListener) {
            this.mListener = (DevicesListFragmentListener) context;
        }
    }

    @Override // Fragment
    public void onResume() {
        super.onResume();

        switch (mListType) {
            case DevicesListTabsAdapter.SCANNED_LIST_TYPE:
                mDevicesListAdapter.reset();
                mRefreshLayout.setRefreshing(true);
                mListener.startScan(mDevicesListAdapter);
                break;

            case DevicesListTabsAdapter.BONDED_LIST_TYPE:
                mDevicesListAdapter.reset();
                mRefreshLayout.setRefreshing(true);
                mListener.getBondedDevices(mDevicesListAdapter);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListType = getArguments().getInt(ARG_LIST_TYPE);

        View rootView = inflater.inflate(R.layout.fragment_devices, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_devices_list);
        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mTVNoDeviceAvailable = (TextView) rootView.findViewById(R.id.tv_no_available_device);

        String text = mListType == DevicesListTabsAdapter.BONDED_LIST_TYPE ?
                getString(R.string.connect_no_available_paired_device) :
                getString(R.string.connect_no_available_scanned_device);
        mTVNoDeviceAvailable.setText(text);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDevicesListAdapter.reset();
                mTVNoDeviceAvailable.setVisibility(View.GONE);
                switch (mListType) {
                    case DevicesListTabsAdapter.SCANNED_LIST_TYPE:
                        mListener.startScan(mDevicesListAdapter);
                        break;

                    case DevicesListTabsAdapter.BONDED_LIST_TYPE:
                        mListener.getBondedDevices(mDevicesListAdapter);
                        break;
                }
            }
        });

        // use a linear layout manager for the recycler view
        LinearLayoutManager devicesListLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(devicesListLayoutManager);
        recyclerView.setHasFixedSize(true);

        // specify an adapter for the recycler view
        mDevicesListAdapter = new DevicesListAdapter(this);
        recyclerView.setAdapter(mDevicesListAdapter);

        return rootView;
    }

    @Override // DevicesListAdapter.IDevicesListAdapterListener
    public void onItemSelected(boolean itemSelected) {
        mListener.onItemSelected(itemSelected);
    }

    /**
     * <p>To know if this fragment has a device selected.</p>
     *
     * @return true if there is a selected device in its data set, false otherwise.
     */
    public boolean hasSelection() {
        return mDevicesListAdapter.hasSelection();
    }

    /**
     * <p>To inform the swipe refresh layout to stop to display the "on refresh" view.</p>
     */
    public void stopRefreshing() {
        mRefreshLayout.setRefreshing(false);
        int visibility = mDevicesListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE;
        mTVNoDeviceAvailable.setVisibility(visibility);
    }

    /**
     * <p>To get the selected device if there is one.</p>
     *
     * @return the selected device if there is one, null otherwise.
     */
    public BluetoothDevice getSelectedDevice() {
        return mDevicesListAdapter.getSelectedItem();
    }

    /**
     * <p>This method is called when this fragment is not in the view displayed by the user anymore.</p>
     * <p>The tabs view pager always keeps the previous, current and next fragments on, so the usual onResume and
     * onPause methods of Fragment are not called when this fragment is not displayed anymore. Unless it is not the
     * previous or the next anymore.</p>
     */
    @SuppressWarnings("EmptyMethod") // not need to be implemented at the moment
    public void onPauseFragment() {
    }

    /**
     * <p>This method is called when this fragment is displayed to the user.</p>
     * <p>The tabs view pager always keeps the previous, current and next fragments on, so the usual onResume and
     * onPause methods of Fragment are not called when this fragment is not displayed anymore. Unless it is not the
     * previous or the next anymore.</p>
     */
    @SuppressWarnings("EmptyMethod") // not need to be implemented at the moment
    public void onResumeFragment() {
    }

    /**
     * The listener triggered by events from this fragment.
      */
    public interface DevicesListFragmentListener {
        /**
         * <p>To ask to the listener to give the complete list of bonded devices.</p>
         *
         * @param mDevicesListAdapter
         *          The adapter the bonded devices have to be provided.
         */
        void getBondedDevices(DevicesListAdapter mDevicesListAdapter);

        /**
         * <p>To inform the listener that the user asked for the devices list to be refreshed.</p>
         *
         * @param mDevicesListAdapter
         *        The adapter to provide the devices.
         */
        void startScan(DevicesListAdapter mDevicesListAdapter);

        /**
         * <p>When the user selects or unselects an item this method is called.</p>
         *
         * @param selected
         *          true if a device had been selected, false otherwise.
         */
        void onItemSelected(boolean selected);
    }
}
