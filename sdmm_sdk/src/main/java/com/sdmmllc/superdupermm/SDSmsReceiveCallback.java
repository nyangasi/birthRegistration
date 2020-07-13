package com.sdmmllc.superdupermm;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

public class SDSmsReceiveCallback implements Parcelable {

	private static final String TAG = "SDSmsReceiveCallback";
	
	private Intent mIntent;
	
	public SDSmsReceiveCallback() {
		
	}
	
	public boolean reload() {
		return true;
	}
	
	public SDSmsReceiveCallback(Intent intent) {
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

	public static final Parcelable.Creator<SDSmsReceiveCallback> CREATOR = new Parcelable.Creator<SDSmsReceiveCallback>() {
        public SDSmsReceiveCallback createFromParcel(Parcel in) {
        	SDSmsReceiveCallback tmp = new SDSmsReceiveCallback();
        	tmp.readFromParcel(in);
            return tmp;
        }

        public SDSmsReceiveCallback[] newArray(int size) {
            return new SDSmsReceiveCallback[size];
        }
    };

}
