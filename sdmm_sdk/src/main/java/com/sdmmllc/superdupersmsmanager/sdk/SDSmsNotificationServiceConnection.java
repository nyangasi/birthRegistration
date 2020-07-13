package com.sdmmllc.superdupersmsmanager.sdk;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupermm.ISDSmsNotificationService;

public class SDSmsNotificationServiceConnection implements ServiceConnection {

	public static final String TAG = "SDSmsNotificationServiceConnection";
	
    private ISDSmsNotificationService mService;

    public void cancelNotification(Intent intent) throws RemoteException {
    	if (SDSmsConsts.DEBUG_RECEIVE_SMS) 
    		Log.i(TAG, "cancelNotification called -------- " +
    			" (mService == null): " + (mService == null));
    	int count = 10;
    	while (mService == null && count < 100) {
    		synchronized (this) {
    			try {
    				wait(10);
    				count++;
    			} catch (Exception e) {
    				// do nothing...
    			}
    		}
    	}
        if (mService != null) mService.cancelNotification(intent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
    	if (SDSmsConsts.DEBUG_RECEIVE_SMS) 
    		Log.i(TAG, "onServiceConnected (service == null): " + (service == null));
        mService = ISDSmsNotificationService.Stub.asInterface(service);
        if (SDSmsConsts.DEBUG_RECEIVE_SMS) 
        	Log.i(TAG, "onServiceConnected (mService == null): " + (mService == null));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    	if (SDSmsConsts.DEBUG_RECEIVE_SMS) 
    		Log.i(TAG, "onServiceDisconnected called!!! " +
    			" (mService == null): " + (mService == null));
    }

}
