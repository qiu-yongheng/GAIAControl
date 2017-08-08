/**************************************************************************************************
 * Copyright 2017 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.gaiacontrol.activities;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.qualcomm.gaiacontrol.Consts;
import com.qualcomm.gaiacontrol.R;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class is the abstract activity to extend for each activity in this application to avoid any permissions
 * issues with Android 6.0. This class checks if the requested permissions declared in the manifest are granted. For
 * any permission not granted, this class will ask to the user to grant it.</p>
 * <p>For some permission requests the application first informs the user about why these permissions are needed,
 * and this class will manage this before asking for the permissions to be granted.</p>
 */

public abstract class PermissionsActivity extends AppCompatActivity {

    /**
     * The list of the permissions requested by the application. This attribute is initialized in the method
     * {@link #onCreate(Bundle) onCreate} with the values given by {@link android.content.pm.PackageInfo PackageInfo}.
     */
    private String[] mPermissions;


    // ====== ACTIVITY METHODS =====================================================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Consts.ACTION_REQUEST_PERMISSIONS:
                // checkPermissions() is called after this method through the onResume() method
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // When the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();
        // every time the activity is resumed, the user can have deactivated the permissions.
        checkPermissions();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // we retrieve the list of permissions from the manifest
            mPermissions = getPackageManager().getPackageInfo(getPackageName(), PackageManager
                    .GET_PERMISSIONS).requestedPermissions;
        } catch (Exception e) {
            e.printStackTrace(); // PackageManager.NameNotFoundException or NullPointerException
        }
        if (mPermissions == null) {
            mPermissions = new String [0];
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>This method checks if all the requested permissions of the application are enabled to be sure that the
     * application can run all its features.</p>
     *
     * @return true if all needed permissions are granted, false if at least one of them is not enabled for the
     * application.
     */
    @SuppressWarnings("UnusedReturnValue") // the return value is used for some implementations
    private boolean checkPermissions() {
        // the check permission has only to be performed for Android 6 (API 23) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> notGrantedPermissions = new ArrayList<>(); // all permissions which needs to be enabled

            // some permissions can need that a message is provided to the user before to request them to be enabled
            boolean needsMessage = false;

            // for each permission we check if it has to be granted and if it required to inform the user
            //noinspection ForLoopReplaceableByForEach
            for (int i=0; i<mPermissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, mPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    notGrantedPermissions.add(mPermissions[i]);
                    needsMessage = needsMessage
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, mPermissions[i]);
                }
            }

            // if some permissions are not granted we request them
            if (notGrantedPermissions.size() > 0) {
                requestPermissions(notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]), needsMessage);
            }

            return notGrantedPermissions.size() == 0;
        }

        return true;
    }

    /**
     * <p>This method requests that the system asks the user to grant the permissions which are not granted.</p>
     * <p>If some permissions need the application to inform the user first, then prior to requesting them from the system,
     * this method will pop up a dialog to inform the user.</p>
     *
     * @param permissions
     *          The list of permissions the application needs to be granted.
     * @param needsMessage
     *          To know if the application has to inform the user before to request the permissions.
     */
    private void requestPermissions(final String[] permissions, boolean needsMessage) {
        // the permissions management has only to be performed for Android 6 (API 23) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (needsMessage) {
                // the user needs to be informed, we prompt a message
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.alert_permissions_title);
                builder.setMessage(R.string.alert_permissions_message);
                builder.setCancelable(false); // the user cannot cancel the dialog with native back button
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // when the user taps "OK" we request the needed permissions
                        ActivityCompat.requestPermissions(PermissionsActivity.this, permissions,
                                Consts.ACTION_REQUEST_PERMISSIONS);
                    }
                });
                builder.show();
            }
            else {
                // no need to inform the user, we directly request the permissions
                ActivityCompat.requestPermissions(this, permissions, Consts.ACTION_REQUEST_PERMISSIONS);
            }
        }
    }

}
