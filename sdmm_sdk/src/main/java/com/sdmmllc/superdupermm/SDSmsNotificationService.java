package com.sdmmllc.superdupermm;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

public class SDSmsNotificationService implements Parcelable {

	private static final String TAG = "SDSmsNotificationService";
	
	private Intent mIntent;
	
	public SDSmsNotificationService() {
		
	}
	
	public boolean reload() {
		return true;
	}
	
	public SDSmsNotificationService(Intent intent) {
		mIntent = intent;
	}
	
	public Intent getIntent() {
		return mIntent;
	}
	
	public void setIntent(Intent intent) {
		mIntent = intent;
	}
	
	public boolean onReceive(Intent intent) throws RemoteException {
		Log.e(TAG, "SDSmsReceiveCallback custom callback onReceive intent:" + intent.getAction());
		mIntent = intent;
		return false;
	}
	
	public String testCallbackConnection(String text) {
		Log.e(TAG, "Test CallbackConnection with text: " + text);
		return "CallbackConnection response:" + text;
	}

	@Override
	public final int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public final void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mIntent, flags);
	}
	
	public void readFromParcel(Parcel in) {
		mIntent = in.readParcelable(null);
    }

	public static final Parcelable.Creator<SDSmsNotificationService> CREATOR = new Parcelable.Creator<SDSmsNotificationService>() {
        public SDSmsNotificationService createFromParcel(Parcel in) {
        	SDSmsNotificationService tmp = new SDSmsNotificationService();
        	tmp.readFromParcel(in);
            return tmp;
        }

        public SDSmsNotificationService[] newArray(int size) {
            return new SDSmsNotificationService[size];
        }
    };

}
