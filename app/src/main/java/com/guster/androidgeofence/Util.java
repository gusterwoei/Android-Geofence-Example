package com.guster.androidgeofence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Gusterwoei on 9/19/14.
 *
 */
public class Util {
    private static ProgressDialog progressDialog;
    private static Util util;
    private Context context;

    public static Util getInstance(Context context) {
        if (util == null)
            util = new Util(context);
        return util;
    }

    private Util(Context context) {
        this.context = context;
    }

    public static void showProgressDialog(Context context) {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.show();
        }
    }

    public static void hideProgressDialog() {
        if(progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    public Location getCurrentDeviceLocation(String provider) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(provider);
        return location;
    }

    public void saveDbToSdCard(String dbName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/" + dbName;
                String backupDBPath = dbName;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            error("save to db: " + e.getMessage());
        }
    }

    public Dialog createDialog(Activity activity, int resId) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(resId);
        return dialog;
    }

    public void showConfirmationDialog(Activity activity, String title, String msg,
                                       String okBtnMsg, DialogInterface.OnClickListener okListener,
                                       String cancelBtnMsg, DialogInterface.OnClickListener cancelListener) {
        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        if(title != null)
            dialog.setTitle(title);
        if(msg != null)
            dialog.setMessage(msg);
        if(okListener != null)
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, okBtnMsg != null? okBtnMsg : "OK", okListener);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                cancelBtnMsg != null? cancelBtnMsg : "CANCEL",
                cancelListener != null? cancelListener : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public boolean savePreference(String key, Object value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        Class c = value.getClass();
        if(c.equals(Integer.class)) {
            editor.putInt(key, (Integer)value);
        } else if(c.equals(Float.class)) {
            editor.putFloat(key, (Float)value);
        } else if(c.equals(Long.class)) {
            editor.putLong(key, (Long)value);
        } else if(c.equals(Boolean.class)) {
            editor.putBoolean(key, (Boolean)value);
        } else if(c.equals(String.class)) {
            editor.putString(key, (String)value);
        } else {
            return false;
        }
        editor.apply();

        return true;
    }

    public Object getPreference(String key, Object defaultValue) {
        if(defaultValue == null) defaultValue = "";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Class c = defaultValue.getClass();
        if(c.equals(Integer.class)) {
            return preferences.getInt(key, (Integer) defaultValue);
        } else if(c.equals(Float.class)) {
            return preferences.getFloat(key, (Float) defaultValue);
        } else if(c.equals(Long.class)) {
            return preferences.getLong(key, (Long) defaultValue);
        } else if(c.equals(Boolean.class)) {
            return preferences.getBoolean(key, (Boolean) defaultValue);
        } else if(c.equals(String.class)) {
            return preferences.getString(key, (String)defaultValue);
        } else {
            return null;
        }
    }

    public boolean deletePreference(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(key).apply();
        return true;
    }

    public static void log(String msg) {
        Log.d("GEOFENCE", msg);
    }

    public static void error(String msg) {
        Log.e("GEOFENCE", msg);
    }

    public interface AsyncCallback<C> {
        C doInBackground();
        void onPostExecute(C c);
    }
    public static <C> void runAsync(final AsyncCallback<C> callback) {
        new AsyncTask<Void, Void, C>() {
            @Override
            protected C doInBackground(Void... as) {
                return callback.doInBackground();
            }

            @Override
            protected void onPostExecute(C c) {
                callback.onPostExecute(c);
            }
        }.execute();
    }
}
