package com.example.cs360_project;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import grid.StockItem;

public class WarehouseApplication extends Application {
    private static WarehouseApplication instance; // potential memory leak?


    public static WarehouseApplication getInstance(){
        // no null check, as instance is set in onCreate of application. Should work?
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getApplicationContext().getSharedPreferences("warehousing_helper", MODE_PRIVATE); // private meaning only this app can modify
        if(!prefs.contains("sms_perms_prompted")){
            // if key isn't there, this is user's first time opening app (in theory?)
            // idea is that user gets prompted once, makes a decision.
            // after decision made, no longer prompted.
            // to change perms, they'll have to use device's app settings to do so.
            prefs.edit().putBoolean("sms_perms_prompted", false).apply();
        }
        smsManager = getApplicationContext().getSystemService(SmsManager.class);
    }


    public Context getAppCtx() {
        return this.getApplicationContext();
    }

    //region active user info
    private String username;
    private String role;

    // set
    public void setUser(String name){
        username = name;
        this.role = SQLiteDB.getInstance().getUserRole(name);
    }

    // get
    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
    //endregion

    //region preferences
    private SharedPreferences prefs;

    public SharedPreferences getPrefs() {
        return prefs;
    }
    //endregion

    //region permissions
    public boolean appHasSmsPerms(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    //endregion

    //region Sending SMS
    SmsManager smsManager;

    public void sendLowStockSMS(StockItem item){
        if(!appHasSmsPerms()) return;

        String msg = "The warehouse is running low! " + item.getName() + " needs restocking.\n"+
                "Item SKU: " + item.getSku() + "\n" +
                "# in stock: " + item.getStockCount();

        final String emulatorPhoneNum = "15551234567"; // I assume same number will be used on prof's VM?
        smsManager.sendTextMessage(
                emulatorPhoneNum,
                null,
                msg,
                null, null
                );
    }
    //endregion
}
