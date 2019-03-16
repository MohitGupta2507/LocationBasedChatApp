package com.example.mohit.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class NewGpsBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            
        }
    }
}
