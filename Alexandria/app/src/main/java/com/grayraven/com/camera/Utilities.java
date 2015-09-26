package com.grayraven.com.camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import it.jaschke.alexandria.R;

/**
 * Created by jhoward on 9/25/2015.
 */
public class Utilities {

    private static final String TAG = "Utilities";

    public static void showAlertDialog(int msgId, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(R.string.alert_dlg_title);
        String msg = context.getResources().getString(msgId);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Make sure we have an internet connection and that Google is reachable
     * @return
     */
    public static boolean internetAvailable(Context context) {
        if(context == null) {
            Log.e(TAG, "Null context passted to internetAvailable");
            return false;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void dismissKeyboard(Activity activity) {
        if((activity == null) || (activity.getCurrentFocus() == null)) {
            Log.e(TAG, "Null passed to dismiss keyboard");
            return;
        }
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
