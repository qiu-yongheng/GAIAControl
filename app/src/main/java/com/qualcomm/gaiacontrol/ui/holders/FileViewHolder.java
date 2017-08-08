/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;

/**
 * <p>This view holder represents a file item display. It is used in a File list to display and update the
 * information of a file/directory for the layout {@link R.layout#list_files_item list_files_item}.</p>
 */
public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    /**
     * The text views to display information for a file.
     */
    private final TextView mTextViewFileName, mTextViewFileLastModification, mTextViewFileSize;
    /**
     * The instance of the parent to interact with it as a listener.
     */
    private final IFileViewHolder mListener;
    /**
     * The image view when an user selects an item.
     */
    private final CheckBox mCheckBoxSelected;
    /**
     * The image view to represent a directory
     */
    private final ImageView mIVDirectory;

    /**
     * The constructor of this class to build this view.
     * @param v
     *          The inflated layout for this view.
     * @param listener
     *          The instance of the parent to interact with it as a listener.
     */
    public FileViewHolder(View v, IFileViewHolder listener) {
        super(v);
        mTextViewFileName = (TextView) v.findViewById(R.id.tv_file_name);
        mTextViewFileLastModification = (TextView) v.findViewById(R.id.tv_file_last_modification);
        mTextViewFileSize = (TextView) v.findViewById(R.id.tv_file_size);
        mCheckBoxSelected = (CheckBox) v.findViewById(R.id.checkbox_item_selected);
        mIVDirectory = (ImageView) v.findViewById(R.id.iv_directory);
        mListener = listener;
        mCheckBoxSelected.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onClickItem(this.getAdapterPosition());
    }

    /**
     * This method is for refreshing all the values displayed for the corresponding view which show all
     * information related to a File.
     *
     * @param isDirectory
     *          to know if this view corresponds to a directory.
     * @param name
     *          the name of the file to display.
     * @param lastModified
     *          the date of the last modification of the file to display.
     * @param size
     *          the size of the File
     * @param isSelected
     *          to know if this File has been selected by the user. If the File is a directory this parameter does
     *          not matter.
     */
    public void refreshValues(boolean isDirectory, String name, String lastModified, String size, boolean isSelected) {
        if (isDirectory) {
            mIVDirectory.setVisibility(View.VISIBLE);
            mCheckBoxSelected.setVisibility(View.GONE);
        }
        else {
            mIVDirectory.setVisibility(View.GONE);
            mCheckBoxSelected.setVisibility(View.VISIBLE);
            mCheckBoxSelected.setChecked(isSelected);
            itemView.setActivated(isSelected);
        }

        mTextViewFileName.setText(name);
        mTextViewFileLastModification.setText(lastModified);
        mTextViewFileSize.setText(size);
    }

    /**
     * The interface to allow this class to interact with its parent.
     */
    public interface IFileViewHolder {
        /**
         * This method is called when the user clicks on the main view of an item.
         *
         * @param position
         *              The position of the item in the list.
         */
        void onClickItem(int position);
    }
}