/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.ui.adapters.FilesListAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * A fragment to manage the display of a File list.
 */
public class FilePickerFragment extends Fragment implements View.OnClickListener,
        FilesListAdapter.IFilesListAdapterListener {

    /**
     * For debug mode, the tag to display for logs.
     */
    @SuppressWarnings("unused")
    private static final String TAG = "FilePickerFragment";
    /**
     * To know if we are using the application in the debug mode.
     */
    @SuppressWarnings("unused")
    private static final boolean DEBUG = Consts.DEBUG;
    /**
     * The adapter which allows display of the files list.
     */
    private FilesListAdapter mFilesAdapter;
    /**
     * The button to validate the file choice.
     */
    private Button mBtChoose;
    /**
     * The file to upload on the board.
     */
    private File mFile;
    /**
     * The text view to display when no file are available.
     */
    private TextView mTVFilesNotAvailable;
    /**
     * The text view to display the path title.
     */
    private TextView mTVPathTitle;
    /**
     * The button to allow a user to come back to the previous directory.
     */
    private Button mButtonPrevious;
    /**
     * The list of used paths to be able to come back to the previous one.
     */
    private final ArrayList<String> mPaths = new ArrayList<>();
    /**
     * The listener to trigger events from this fragment.
     */
    private FilePickerFragmentListener mListener;

    /**
     * The factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment UpgradeVMFragment.
     */
    public static FilePickerFragment newInstance() {
        return new FilePickerFragment();
    }

    /**
     * Empty constructor - required.
     */
    public FilePickerFragment() {
    }

    // This event fires first, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override // Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FilePickerFragmentListener) {
            this.mListener = (FilePickerFragmentListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_file, container, false);
        init(view);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch ((v.getId())) {
            case R.id.bt_start_upgrade:
                returnPickedFile();
                break;
            case R.id.bt_previous:
                updateFileListWithParent();
                break;
        }
    }

    @Override
    public void onFileSelected(boolean itemSelected) {
        enableValidateButton(itemSelected);
    }

    @Override
    public void onDirectorySelected(File directory) {
        String path = directory.getPath();
        updateFileList(path);
    }

    @Override
    public void onNoFileAvailable() {
        mTVFilesNotAvailable.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFileList(Environment.getExternalStorageDirectory().getPath());
    }

    /**
     * This method will check if this path is readable and corresponds to a directory, If it is it will update the list
     * of files/directories displayed with the ones contained in this path. If it is not readable and not a directory
     * this method will display a toast explaining the error.
     *
     * @param path
     *          The path to use to update the list of Files.
     */
    private void updateFileList(String path) {
        mTVFilesNotAvailable.setVisibility(View.GONE);
        File dir = new File(path);
        if (!dir.canRead()) {
            Toast.makeText(getContext(), "inaccessible", Toast.LENGTH_LONG).show();
        }
        else if (!dir.isDirectory()) {
            Toast.makeText(getContext(), "not a directory", Toast.LENGTH_LONG).show();
        }
        else {
            mPaths.add(path);
            mTVPathTitle.setText(path);
            enableValidateButton(false);
            mFilesAdapter.setFilesList(dir.listFiles());
        }
        mButtonPrevious.setEnabled(mPaths.size() > 1);
    }

    /**
     * This method will update the list of files/directories with the content of previous directory used.
     */
    private void updateFileListWithParent() {
        mTVFilesNotAvailable.setVisibility(View.GONE);
        if (mPaths.size() > 1) {
            mPaths.remove(mPaths.get(mPaths.size() - 1));
            File dir = new File(mPaths.get(mPaths.size() - 1));
            mTVPathTitle.setText(dir.getPath());
            enableValidateButton(false);
            mFilesAdapter.setFilesList(dir.listFiles());
            mButtonPrevious.setEnabled(mPaths.size() > 1);
        }
    }

    /**
     * This method return the file picked by the activity to be processed.
     */
    private void returnPickedFile() {
        if(mFile == null || !mFile.equals(mFilesAdapter.getSelectedItem())) {
            mFile = mFilesAdapter.getSelectedItem();
        }

        if (mFile != null) {
            String[] fileNameSplit = mFile.getName().split("\\.");
            String extension = fileNameSplit[fileNameSplit.length-1];

            if (extension.equalsIgnoreCase("bin")) {
                // file exists and is a bin file, it can provided to the UpgradeActivity in order to start the update
                mListener.onStartUpgradePressed(mFile);
            }
            else {
                // the file has to be a bin file
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_not_a_bin_file_title);
                builder.setMessage(R.string.alert_not_a_bin_file_message);
                builder.setPositiveButton(R.string.button_ok, null);
                builder.show();
            }
        }
        else {
            // there is no selected file or it fails to retrieve the file
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.alert_no_file_selected_title);
            builder.setMessage(R.string.alert_no_file_selected_message);
            builder.setPositiveButton(R.string.button_ok, null);
            builder.show();
        }
    }

    /**
     * To activate or deactivate the UI which allows validation of the selection and continuation to the next step.
     *
     * @param activated
     *            true to activate the validation button, false otherwise.
     */
    private void enableValidateButton(boolean activated) {
        mBtChoose.setEnabled(activated);
    }

    /**
     * This method allows initialisation of components.
     *
     * @param view
     *            The inflated view for this fragment.
     */
    private void init(View view) {
        // bind components
        mBtChoose = (Button) view.findViewById(R.id.bt_start_upgrade);
        mBtChoose.setOnClickListener(this);
        mButtonPrevious = (Button) view.findViewById(R.id.bt_previous);
        mButtonPrevious.setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_files);
        mTVFilesNotAvailable = (TextView) view.findViewById(R.id.tv_no_available_file);
        mTVPathTitle = (TextView) view.findViewById(R.id.tv_path_title);

        // use a linear layout manager for the recycler view
        LinearLayoutManager filesListLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(filesListLayoutManager);
        recyclerView.setHasFixedSize(true);

        // specify an adapter for the recycler view
        mFilesAdapter = new FilesListAdapter(this);
        recyclerView.setAdapter(mFilesAdapter);

        // we ask to the device to connect for the upgrade
        enableValidateButton(mFilesAdapter.hasSelection());
    }

    public interface FilePickerFragmentListener {

        void onStartUpgradePressed(File file);
    }
}
