/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qualcomm.gaiacontrol.R;
import com.qualcomm.gaiacontrol.Utils;
import com.qualcomm.libraries.vmupgrade.UploadProgress;
import com.qualcomm.libraries.vmupgrade.codes.ResumePoints;

import java.text.DecimalFormat;

/**
 * This fragment allows building of a dialog to display information during the VM upgrade.
 */

@SuppressWarnings("unused")
public class VMUpgradeDialog extends DialogFragment {

    /**
     * The listener to interact with the fragment which implements this fragment.
     */
    private UpgradeDialogListener mListener;
    /**
     * The progress bar displayed to the user to show the transfer progress.
     */
    private ProgressBar mProgressBar;
    /**
     * The progress bar displayed to the user to for steps other than the DATA_TRANSFER.
     */
    private View mIndeterminateProgressBar;
    /**
     * The textView error to display the error message which corresponds to the error code.
     */
    private TextView mTVErrorCodeMessage;
    /**
     * The text view to display the actual step.
     */
    private TextView mTVStep;
    /**
     * The text view to display a percentage during a process.
     */
    private TextView mTVPercentage;
    /**
     * The view to display information about data transfer.
     */
    private View mLTransfer;
    /**
     * The text view to display the time during the transfer.
     */
    private TextView mTVTime;
    /**
     * The view to display an error.
     */
    private View mLayoutError;
    /**
     * The view to inform the user the error is coming from the board.
     */
    private TextView mTVError;
    /**
     * The text view to display the error code.
     */
    private TextView mTVErrorCode;
    /**
     * To display a number in a specific decimal format.
     */
    private final DecimalFormat mDecimalFormat = new DecimalFormat();
    private Button mPositiveButton;
    private Button mNegativeButton;
    private AlertDialog mDialog;

    /**
     * The factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment VMUpgradeDialog.
     */
    public static VMUpgradeDialog newInstance(UpgradeDialogListener listener) {
        VMUpgradeDialog fragment = new VMUpgradeDialog();
        fragment.setListener(listener);
        return fragment;
    }

    /**
     * Constructor.
     */
    public VMUpgradeDialog() {
        super();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPositiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mNegativeButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        clear();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // the central view: no other choice than "null" for the last parameter, see Android developer documentation.
        @SuppressLint("InflateParams")
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_upgrade_progress, null);
        builder.setView(view);
        // the abort button
        builder.setNegativeButton(R.string.button_abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clear();
                if (mListener != null) {
                    mListener.abortUpgrade();
                }
            }
        });
        builder.setPositiveButton(R.string.button_ok, null);
        // the user can not dismiss the dialog using the back button.
        setCancelable(false);

        mDialog = builder.create();

        init(view);

        return mDialog;
    }

    /**
     * To display a percentage number during the Step DATA_TRANSFER.
     *
     * @param progress
     *        A progress object which contains the percentage of how many bytes of the file have been sent to
     *        the Board and an estimation of how long this manager will need to upload the rest of the file.
     */
    public void displayTransferProgress(UploadProgress progress) {
        if (this.isAdded() || this.isVisible()) {
            double percentage = progress.getPercentage();
            mTVPercentage.setText(Utils.getStringForPercentage(percentage));
            mProgressBar.setProgress((int) percentage);
            mTVTime.setText(Utils.getStringForTime(progress.getRemainingTime()));
        }
    }

    /**
     * To display an error message.
     */
    public void displayError(String message, String code) {
        if (this.isAdded() ||this.isVisible()) {
            mLayoutError.setVisibility(View.VISIBLE);
            mTVError.setText(getResources().getString(R.string.dialog_upgrade_error_from_board));
            mIndeterminateProgressBar.setVisibility(View.GONE);
            mPositiveButton.setVisibility(View.VISIBLE);
            mNegativeButton.setVisibility(View.GONE);

            if (code.length() > 0) {
                mTVErrorCode.setVisibility(View.VISIBLE);
                mTVErrorCode.setText(code);
            } else {
                mTVErrorCode.setVisibility(View.GONE);
            }
            if (message.length() > 0) {
                mTVErrorCodeMessage.setVisibility(View.VISIBLE);
                mTVErrorCodeMessage.setText(message);
            } else {
                mTVErrorCodeMessage.setVisibility(View.GONE);
            }
        }
    }

    /**
     * To display a specific message as an error.
     *
     * @param message
     *              THe specific message to display.
     */
    public void displayError(String message) {
        mLayoutError.setVisibility(View.VISIBLE);
        mTVErrorCode.setVisibility(View.GONE);
        mTVErrorCodeMessage.setVisibility(View.GONE);
        mTVError.setText(message);
        mIndeterminateProgressBar.setVisibility(View.GONE);
        mPositiveButton.setVisibility(View.VISIBLE);
        mNegativeButton.setVisibility(View.GONE);
    }

    /**
     * To update the view depending on the actual step.
     */
    public void updateStep(@ResumePoints.Enum int step) {
        String text = ResumePoints.getLabel(step);
        mTVStep.setText(text);

        if (step == ResumePoints.Enum.DATA_TRANSFER) {
            mLTransfer.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mIndeterminateProgressBar.setVisibility(View.GONE);
        }
        else {
            mLTransfer.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mIndeterminateProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * To clear the content on this view.
     */
    public void clear () {
        updateStep(mListener.getResumePoint());
        hideError();
    }

    /**
     * To hide the error message.
     */
    private void hideError() {
        mLayoutError.setVisibility(View.GONE);
        mPositiveButton.setVisibility(View.GONE);
        mNegativeButton.setVisibility(View.VISIBLE);
    }

    /**
     * This method allows initialisation of components.
     *
     * @param view
     *            The inflated view for this fragment.
     */
    private void init(View view) {
        mTVStep = (TextView) view.findViewById(R.id.tv_step);
        mLTransfer = view.findViewById(R.id.layout_transfer);
        mTVPercentage = (TextView) view.findViewById(R.id.tv_percentage);
        mTVTime = (TextView) view.findViewById(R.id.tv_time);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_upgrade);
        mIndeterminateProgressBar = view.findViewById(R.id.pb_upgrade_indeterminate);
        mLayoutError = view.findViewById(R.id.layout_error);
        mTVError = (TextView) view.findViewById(R.id.tv_upgrade_error_message);
        mTVErrorCode = (TextView) view.findViewById(R.id.tv_upgrade_error_code);
        mTVErrorCodeMessage = (TextView) view.findViewById(R.id.tv_upgrade_error_code_message);

        mDecimalFormat.setMaximumFractionDigits(1);
    }

    /**
     * To define the listener for actions on this dialog. We can't use the onAttach method to define a listener: here
     * the listener is a fragment.
     *
     * @param listener
     *            The listener which will listen this dialog.
     */
    private void setListener(UpgradeDialogListener listener) {
        this.mListener = listener;
    }

    /**
     * This interface allows this Dialog fragment to communicate with its listener.
     */
    @SuppressWarnings("EmptyMethod")
    public interface UpgradeDialogListener {
        /**
         * To abort the upgrade.
         */
        void abortUpgrade();

        /**
         * To know the step for the dialog to display.
         *
         * @return
         *          The actual resume point to display the step into the dialog.
         */
        @ResumePoints.Enum int getResumePoint();
    }
}