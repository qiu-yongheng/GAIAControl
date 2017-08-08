/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.ui.holders.FileViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * <p>This class allows management of a data set for a File list.</p>
 */
public class FilesListAdapter extends RecyclerView.Adapter<FileViewHolder> implements FileViewHolder.IFileViewHolder {

    /**
     * The default position when there is no item selected.
     */
    private static final int ITEM_NULL = -1;
    /**
     * The position for the item selected by the user.
     */
    private int mSelectedItem = ITEM_NULL;
    /**
     * The data list for this adapter.
     */
    private final ArrayList<File> mFiles = new ArrayList<>();
    /**
     * The listener for all user interactions.
     */
    private final IFilesListAdapterListener mListener;

    /**
     * The main constructor of this list to build a new instance for an adapter to a RecyclerView about a
     * BluetoothDevice list.
     *
     * @param listener
     *            The listener to use when the user interacts with the RecyclerView.
     */
    public FilesListAdapter(IFilesListAdapterListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClickItem(int position) {
        if (mFiles.get(position).isDirectory()) {
            mListener.onDirectorySelected(mFiles.get(position));
        }
        else {
            if (mSelectedItem == position) {
                mSelectedItem = ITEM_NULL;
            } else {
                int previousItem = mSelectedItem;
                mSelectedItem = position;
                notifyItemChanged(previousItem);
            }
            notifyItemChanged(position);
            mListener.onFileSelected(hasSelection());
        }
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_files_item, parent, false);
        return new FileViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        // we define the content of this view depending on the data set of this adapter.
        File file = mFiles.get(position);
        boolean isFolder = file.isDirectory();
        String date = DateFormat.format(Consts.DATE_FORMAT, new Date(file.lastModified())).toString();
        long size = file.length() / 1024; // size is in bytes and we want KB
        String sizeText = size + Consts.UNIT_FILE_SIZE;
        boolean isSelected = position == mSelectedItem;
        holder.refreshValues(isFolder, file.getName(), date, sizeText, isSelected);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    /**
     * This method allows definition of the data for this adapter.
     *
     * @param filesList
     *            The list of files to display on the RecyclerView.
     */
    public void setFilesList(File[] filesList) {
        mFiles.clear();
        mSelectedItem = ITEM_NULL;
        for (File file: filesList) {
            if (file.canRead()) {
                mFiles.add(file);
            }
        }
        notifyDataSetChanged();
        
        if (mFiles.size() == 0) {
            mListener.onNoFileAvailable();
        }
    }

    /**
     * This method allows the item selected by the user to be returned. If there is no selection the method returns null.
     *
     * @return the selected item by the user or null.
     */
    public File getSelectedItem() {
        if (hasSelection())
            return this.mFiles.get(mSelectedItem);
        else
            return null;
    }

    /**
     * This method is for finding out if this view has a selected item.
     *
     * @return true if the view has a selected item and false if none of the items is selected.
     */
    public boolean hasSelection() {
        return mSelectedItem >= 0 && mSelectedItem < mFiles.size();
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the RecyclerView. Such as a
     * fragment or an activity.
     */
    public interface IFilesListAdapterListener {
        /**
         * This method is called by the adapter when the user selects or deselects an item of the list.
         *
         * @param itemSelected
         *                  true if an item is selected, false otherwise.
         */
        void onFileSelected(boolean itemSelected);

        /**
         * This method is called when the user selects a directory in the File list.
         *
         * @param directory
         *          The directory which had been selected.
         */
        void onDirectorySelected(File directory);

        /**
         * This method is called when the list of files and directories does not contain any readable File.
         */
        void onNoFileAvailable();
    }
}