package com.sdmmllc.superdupersmsmanager.sdk;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupermm.ISDSmsNotificationService;
import com.sdmmllc.superdupermm.ISDSmsReceiveService;
import com.sdmmllc.superdupermm.ISDSmsSendService;
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

public class SDSmsServiceManager {
	
	public static final String TAG = "SDSmsServiceManager";

	public static final String SEND_SERVICE 				= "com.sdmmllc.superdupermm.SDSmsSendService";
	public static final String SEND_SERVICE_ACTION 			= "com.sdmmllc.superdupermm.ISDSmsSendService";
	public static final String SEND_SERVICE_PACKAGE 		= "com.sdmmllc.superdupermessagingmanager";
	public static final String RECEIVE_SERVICE 				= "com.sdmmllc.superdupermm.MessagingService";
	public static final String RECEIVE_SERVICE_ACTION 		= "com.sdmmllc.superdupermm.ISDSmsReceiveService";
	public static final String RECEIVE_SERVICE_PACKAGE 		= "com.sdmmllc.superdupermessagingmanager";
	public static final String START_SERVICE 				= "com.sdmmllc.superdupermm.SDSmsIntentReceiver";

	private static SDSmsServiceManager sInstance;
	private ISDSmsSendService mISDSmsSendService = null;
	private SDSmsReceiveCallback tempSDSmsReceiveCallback = null;
	private ServiceConnection svcConn = null;

	private ISDSmsReceiveService mISDSmsReceiveService = null;
	private SDSmsReceiveCallbackConnection mSDSmsReceiveCallbackConn = null;
	private boolean connected = false;
	private boolean settingNewCallback = false;

	private String sdsmsID = "";
	private int sdsmsType = SDSmsManager.ACCESS_TYPE_UNREGISTERED;
	private String packageName = "";
	private String 
		mSendClass = "", 
		mSendToClass = "",
		mSendMulitpleClass = "",
		mSendMessageClass = "",
		mSmsReceivedClass = "",
		mSmsDeliverClass = "",
		mWapPushReceivedClass = "",
		mWapPushDeliverClass = "",
		mMmsPushClass = "",
		mMessageSentClass = "",
		mRespondViaMessageClass = "",
		sdmm_send_class 				= "com.sdmmllc.superdupersmsmanager.sdk.SEND_CLASS",
		sdmm_sendto_class				= "com.sdmmllc.superdupersmsmanager.sdk.SENDTO_CLASS",
		sdmm_send_multiple_class		= "com.sdmmllc.superdupersmsmanager.sdk.SEND_MULTIPLE_CLASS",
		sdmm_send_message_class 		= "com.sdmmllc.superdupersmsmanager.sdk.SEND_MESSAGE_CLASS",
		sdmm_sms_received_class 		= "com.sdmmllc.superdupersmsmanager.sdk.SMS_RECEIVED_CLASS",
		sdmm_sms_deliver_class			= "com.sdmmllc.superdupersmsmanager.sdk.SMS_DELIVER_CLASS",
		sdmm_wap_push_received_class	= "com.sdmmllc.superdupersmsmanager.sdk.WAP_PUSH_RECEIVED_CLASS",
		sdmm_wap_push_deliver_class		= "com.sdmmllc.superdupersmsmanager.sdk.WAP_PUSH_DELIVER_CLASS",
		sdmm_mms_push_class 			= "com.sdmmllc.superdupersmsmanager.sdk.MMS_PUSH_CLASS",
		sdmm_message_sent_class			= "com.sdmmllc.superdupersmsmanager.sdk.MESSAGE_SENT_CLASS",
		sdmm_respond_via_message_class	= "com.sdmmllc.superdupersmsmanager.sdk.RESPOND_VIA_MESSAGE_CLASS";
		
	private boolean waitingForServices = false;
	private List<SDSmsConnectionListener> mListeners = new ArrayList<SDSmsConnectionListener>();
	private List<ISDSmsNotificationService> mNotifications = new ArrayList<ISDSmsNotificationService>();
	private String svcConnId = "", callbackConnId = "";
	private boolean svcConnBound = false, callbackConnBound = false;

	public static String 
		WARNING		= "SDMM Warning", 
		LOGS 		= "SDMM Log",
		SDK 		= "Super Duper Messaging Manager SDK",
		SDMM_ID 	= "sdmm_test_id",
		SEND_PERM 	= "com.sdmmllc.superdupersmsmanager.SEND_SMS",
		WRITE_PERM 	= "com.sdmmllc.superdupersmsmanager.WRITE_SMS";
	
	private Context mContext;
	private boolean noReceivers = true;
	private static boolean sdmmWarnings = false;
	private static boolean sdmmLogs = true;
	
	private SDSmsServiceManager (Context context) {
		mContext = context;
		sdmmWarnings = context.getResources().getBoolean(R.bool.sdmmWarnings);
		sdmmLogs = context.getResources().getBoolean(R.bool.sdmmLogs);
		
		if ((context.getString(R.string.sdmm_app_name).equals(SDK) ||
				context.getString(R.string.sdmm_app_name).length() < 1) && sdmmWarnings) {
			if (sdmmWarnings) Log.w(WARNING, "You did not enter your app name in sdmm_default_strings.xml");
			if (sdmmWarnings) Log.w(WARNING, "sdmm_app_name: " + context.getString(R.string.sdmm_app_name));
		}
		
		packageName = context.getPackageName();
		sdsmsID = mContext.getString(R.string.sdmm_app_id);

		String type = mContext.getString(R.string.sdmm_access_type).replaceAll( "[^\\d]", "" );
		if (type == null || type.length() < 1) {
			Log.w(WARNING, "You have not specified a valid ACCESS_TYPE in sdmm_default_strings.xml");
			Log.w(WARNING, "Your app will be assigned ACCESS_TYPE_OBSERVER");
		}
		if (Integer.parseInt(type, SDSmsManager.ACCESS_TYPE_OBSERVER) == SDSmsManager.ACCESS_TYPE_OBSERVER) {
			Log.w(WARNING, "Your app will be assigned ACCESS_TYPE_OBSERVER instead of: **" + type + "**");
		}
		
		// check SDMM comms classes
		sdsmsType = Integer.parseInt(type, SDSmsManager.ACCESS_TYPE_OBSERVER);
		mSendClass = mContext.getString(R.string.sdmm_send_class);
		mSendToClass = mContext.getString(R.string.sdmm_sendto_class);
		mSendMulitpleClass = mContext.getString(R.string.sdmm_send_multiple_class);
		mSendMessageClass = mContext.getString(R.string.sdmm_send_message_class);
		mSmsReceivedClass = mContext.getString(R.string.sdmm_sms_received_class);
		mSmsDeliverClass = mContext.getString(R.string.sdmm_sms_deliver_class);
		mWapPushReceivedClass = mContext.getString(R.string.sdmm_wap_push_received_class);
		mWapPushDeliverClass = mContext.getString(R.string.sdmm_wap_push_deliver_class);
		mMmsPushClass = mContext.getString(R.string.sdmm_mms_push_class);
		mMessageSentClass = mContext.getString(R.string.sdmm_message_sent_class);
		mRespondViaMessageClass = mContext.getString(R.string.sdmm_respond_via_message_class);
		
		if (sdmmLogs || sdmmWarnings) runDeveloperMessages();

		sInstance = this;
		init();
	}
	
	public static void setSdsmsId(String id) {
		if (sInstance != null) sInstance.sdsmsID = id;
	}
	
	public static void setSdsmsType(int type) {
		if (sInstance != null) sInstance.sdsmsType = type;
	}
	
	public static void setSmsReceivedClass(String receiver) {
		if (sInstance != null) sInstance.mSmsReceivedClass = receiver;
	}
	
	public static void setSmsSendClass(String sender) {
		if (sInstance != null) sInstance.mSendClass = sender;
	}
	
	public static SDSmsServiceManager getInstance (Context context) {
		if (sInstance == null) sInstance = new SDSmsServiceManager(context);
		return sInstance;
	}
	
	public static SDSmsServiceManager getInstance() {
		if (sInstance == null) return null;
		return sInstance;
	}
	
	public void setListener(SDSmsConnectionListener newListener) {
		sInstance.mListeners.add(newListener);
	}

	public void removeListener(SDSmsConnectionListener oldListener) {
		sInstance.mListeners.remove(oldListener);
	}
	
	public void setNotification(ISDSmsNotificationService newListener) {
		sInstance.mNotifications.add(newListener);
	}

	public void removeNotification(ISDSmsNotificationService oldListener) {
		sInstance.mNotifications.remove(oldListener);
	}
	
	public List<ISDSmsNotificationService> getNotifications() {
		return sInstance.mNotifications;
	}
	
	public ISDSmsSendService getSendService() {
		if (sInstance == null) {
			Log.e(TAG, "Send service requested before connection was initialized");
			throw new NullPointerException();
		}
		return sInstance.mISDSmsSendService;
	}

    public boolean hasServiceConnection() {
    	if (!sInstance.waitForServices("checkServiceConnection")) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "checkServiceConnection waitingForServices timed out, continuing...");
    	}
        if ((sInstance.mISDSmsSendService == null) || (sInstance.mSDSmsReceiveCallbackConn == null)) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "checkServiceConnection timed out and mISDSmsSendService / mSDSmsReceiveCallbackConn" +
					" is null, calling SDSmsManager.reconnect........");
			reconnect("hasServiceConnection", false);
			return false;
        }
        return true;
	}
	
	public boolean isWaitingForServices() {
		return sInstance.waitingForServices;
	}
	
	public static boolean sdmmLogs() {
		return sdmmLogs;
	}
	
	public static boolean sdmmWarnings() {
		return sdmmWarnings;
	}
	
	private void runDeveloperMessages() {
		if (sdmmLogs) Log.w(LOGS, "sdmm_app_name: " + mContext.getString(R.string.sdmm_app_name));
		if (sdmmLogs) Log.w(LOGS, "sdmm_app_id: " + mContext.getString(R.string.sdmm_app_id));
		if (sdsmsID == null || sdsmsID.length() < 1) {
			Log.w(WARNING, "You have not specified a valid sdmm_app_id in sdmm_default_strings.xml");
		} else if (SDMM_ID.equals(sdsmsID)){
			Log.i(WARNING, "Your apps is using the sdmm_test_id. " +
					"Please obtain a valid key or your app will be disabled if you go live");
		} else {
			if (sdmmLogs) Log.i(LOGS, "The sdmm_app_id your entered is: " + sdsmsID);
		}

		if (mSendClass.equals(sdmm_send_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_send_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_send_class: " + mSendClass);

		if (mSendToClass.equals(sdmm_sendto_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_sendto_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_sendto_class: " + mSendToClass);

		if (mSendMulitpleClass.equals(sdmm_send_multiple_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_send_multiple_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_send_multiple_class: " + mSendMulitpleClass);

		if (mSendMessageClass.equals(sdmm_send_message_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_send_message_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_send_message_class: " + mSendMessageClass);

		if (mSmsReceivedClass.equals(sdmm_sms_received_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_sms_received_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_sms_received_class: " + mSmsReceivedClass);

		if (mSmsDeliverClass.equals(sdmm_sms_deliver_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_sms_deliver_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_sms_deliver_class: " + mSmsDeliverClass);

		if (mWapPushReceivedClass.equals(sdmm_wap_push_received_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_wap_push_received_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_wap_push_received_class: " + mWapPushReceivedClass);

		if (mWapPushDeliverClass.equals(sdmm_wap_push_deliver_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_wap_push_deliver_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_wap_push_deliver_class: " + mWapPushDeliverClass);

		if (mMmsPushClass.equals(sdmm_mms_push_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_mms_push_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_mms_push_class: " + mMmsPushClass);

		if (mMessageSentClass.equals(sdmm_message_sent_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_message_sent_class");
		} else if (sdmmLogs) Log.i(LOGS, "Registered sdmm_message_sent_class: " + mMessageSentClass);

		if (mRespondViaMessageClass.equals(sdmm_respond_via_message_class)) {
			noReceivers = false;
			if (sdmmLogs) Log.i(LOGS, "No class registered for sdmm_respond_via_message_class");
		} else {
			if (sdmmLogs) Log.i(LOGS, "Registered sdmm_respond_via_message_class: " + mRespondViaMessageClass);
		}

		if (sdmmWarnings && !noReceivers) {
			Log.w(WARNING, "You did not register any SDMM integrated classes in sdmm_default_strings.xml");
			Log.w(WARNING, "To block, send or modify messages, your app needs at least one class that will communicate with SDMM");
			if (sdsmsType == SDSmsManager.ACCESS_TYPE_OBSERVER) Log.w(WARNING, "ACCESS_TYPE_OBSERVER apps can ignore this warning");
		}
		
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pkgInfo;
        try {
        	pkgInfo = pm.getPackageInfo(
        		mContext.getPackageName(), 
        		PackageManager.GET_PERMISSIONS
        	);
        	if (pkgInfo != null && pkgInfo.permissions != null) {
            	// reset to false - in case permissions were removed
	        	for (PermissionInfo perm : pkgInfo.permissions) {
	        		if (perm.name.equals(SEND_PERM) ||
	        				perm.name.equals(WRITE_PERM)) {
	            		if (sdmmLogs) 
	            			Log.i(LOGS, "SDMM permissions found for package: " + mContext.getPackageName());
	        		} else
	            		if (sdmmWarnings) {
	            			Log.i(WARNING, "No SDMM package permissions for package: " + mContext.getPackageName());
	            			Log.i(WARNING, "SDMM requires the following the permissions be declared in your Manifest:");
	            			Log.i(WARNING, SEND_PERM);
	            			Log.i(WARNING, WRITE_PERM);
	            		}

	        	}
        	}
        	else 
        		if (sdmmLogs || sdmmWarnings) {
        			Log.i(WARNING, "SDMM package permissions null for package: " + mContext.getPackageName());
        			Log.i(WARNING, "Either your declares no permissions in the Manifest or there is an error with your manifest");
        		}

        } catch (NameNotFoundException e) {
         	if (sdmmLogs || sdmmWarnings) {
        		Log.i(WARNING, "Error getting SDMM Manifest permissions for:" + mContext.getPackageName());
        		Log.i(WARNING, "Android PackageManager did not find your package installed");
         	}
         	e.printStackTrace();
        }
	}
	
	private boolean requestReconnect = false;
	public void reconnect(String methodCalling, boolean forceReload) {
		if (sInstance == null) return;
		if (!forceReload && sInstance.mSDSmsReceiveCallbackConn != null &&
				sInstance.mSDSmsReceiveCallbackConn.getCallBack() != null && 
				sInstance.mSDSmsReceiveCallbackConn.getCallBack().testCallbackConnection("success").contains("success")) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, " reconnect requested but already connected, aborting...");
		}
    	if (!sInstance.waitForServices(methodCalling + " waiting to reconnect")) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "checkServiceConnection reconnect timed out, continuing...");
    	}
        sInstance.requestReconnect = true;
		sInstance.removeServices(methodCalling);
		sInstance.initServices(methodCalling + " called reconnect");
	}
	
	public boolean checkConnections() {
        if ((sInstance.mISDSmsSendService == null) || (sInstance.mSDSmsReceiveCallbackConn == null)) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "checkServiceConnection timed out and mISDSmsSendService / mSDSmsReceiveCallbackConn" +
					" is null........");
			return false;
        }
        return true;
	}
	
	public void disconnect() {
    	if (!sInstance.waitForServices("disconnect")) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "checkServiceConnection disconnect timed out, continuing...");
    	}
		sInstance.removeServices("disconnect");
	}
	
	public void setReceiverCallback(SDSmsReceiveCallback callback) {
		sInstance.tempSDSmsReceiveCallback = callback;
		sInstance.newCallbackThread = new Thread(null, sInstance.setNewCallbackMonitor, "newCallbackMonitor Thread : " + TAG);
		sInstance.newCallbackThread.start();
	}
	
	public void monitorMessages() {
		Intent intent = new Intent();
		intent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
		intent = setupIntent(intent);
		try {
			sInstance.mSDSmsReceiveCallbackConn.getCallBack().testCallbackConnection(TAG + ".monitorMessages testing");
			sInstance.mISDSmsReceiveService.registerReceiver(
					sInstance.sdsmsID, 
					sInstance.sdsmsType, 
					intent, 
					sInstance.mSDSmsReceiveCallbackConn.getCallBack());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() {
		if (!initServicesWaitRunning) initServices("init");
	}
	
	private boolean svcTestRunning = false;
    private Thread svcTestThread;
    private Runnable svcTest = new Runnable() {
	    @Override
	    public void run() {
	    	svcTestRunning = true;
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "run .............................");
	    	int count = 0;
	    	synchronized (this) {
		    	while ((mISDSmsSendService == null) && (count < 2000) && !requestReconnect) {
		    		try {
		    			this.wait(250);
		    		} catch (Exception e) {
		    			// do nothing but keep waiting...
		    		}
		    		if (count % 4 == 0) 
		    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcTest waiting for mISDSmsSendService count: " + count);
		    		count++;
		    	}
	    	}
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcTest continuing...........");
			if (mISDSmsSendService == null) {
				// reconnect occurred after loop exit
	    		waitingForServices = false;
	    		svcTestRunning = false;
				return;
			}
			try {
				mISDSmsSendService.testConnection("Testing connection");
    			if (sdmmLogs) Log.i(LOGS, "Sent test to SDMM service for " + mContext.getPackageName());
			} catch (Exception e1) {
    			if (sdmmWarnings) Log.i(WARNING, "Test failed on SDMM service for " + mContext.getPackageName());
				e1.printStackTrace();
			}
	        if (mSDSmsReceiveCallbackConn == null) {
	    		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcTest restmSDSmsReceiveCallback is null, quitting...");
	    		waitingForServices = false;
	    		svcTestRunning = false;
	        	return;
	        } else {
	        	synchronized (this) {
		            while(!mSDSmsReceiveCallbackConn.connected()) {
		                try {
		    				if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "setNewCallbackMonitor mSDSmsReceiveCallbackConn.connected() not connected");
		                	this.wait(500);
		                } catch (InterruptedException e) {
		                }
		    	        if (mSDSmsReceiveCallbackConn == null) {
		    	    		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcTest restmSDSmsReceiveCallback is null, quitting...");
		    	    		waitingForServices = false;
		    	    		svcTestRunning = false;
		    	        	return;
		    	        }
		            }
	        	}
	    		try {
	    			Intent intent = new Intent();
	    			intent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
	    			intent = setupIntent(intent);
	    			
	    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcMonitor: (mSDSmsReceiveCallbackConn == null): " + (mSDSmsReceiveCallbackConn.getCallBack() == null));
	    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcMonitor: sInstance.sdsmsID: " + sInstance.sdsmsID + " sInstance.sdsmsType: " + sInstance.sdsmsType);
	    			mSDSmsReceiveCallbackConn.getCallBack().testCallbackConnection(TAG + ".svcMonitor testing");
	    			mSDSmsReceiveCallbackConn.testConnection("testing connection for: " + sdsmsID);
	    			boolean registered = mSDSmsReceiveCallbackConn.registerApp(intent);
	    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "svcMonitor: app registered: " + registered);
	    			if (sdmmLogs) Log.i(LOGS, "Testing SDMM callback service to " + mContext.getPackageName());
	    			mSDSmsReceiveCallbackConn.registerReceiver(sdsmsID, sdsmsType, intent);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		waitingForServices = false;
	        }
    		waitingForServices = false;
	        svcTestRunning = false;
	    }
    };
    
    private boolean waitForServices(String callingMethod) {
    	int count = 0;
    	synchronized (this) {
	        while(waitingForServices && !requestReconnect) {
	            try {
					if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, callingMethod + " waitingForServices");
	            	wait(500);
	            } catch (InterruptedException e) {
	            }
	            count++;
	            if (count > 10) return false;
	        }
	        return true;
    	}
    }
   
    private Intent setupIntent(Intent intent) {
		intent.putExtra(SDSmsConsts.SDSMS_REGISTRATION_ID, sInstance.sdsmsID);
		intent.putExtra(SDSmsConsts.SDSMS_REGISTRATION_TYPE, sInstance.sdsmsType);
		intent.putExtra(SDSmsConsts.SDSMS_REGISTRATION_PACKAGE_NAME, sInstance.packageName);
		intent.putExtra(SDSmsConsts.SDSMS_REGISTRATION_RECEIVER_NAME, sInstance.mSmsReceivedClass);
		intent.putExtra(SDSmsConsts.SendClass, sInstance.mSendClass);
		intent.putExtra(SDSmsConsts.SendToClass, sInstance.mSendToClass);
		intent.putExtra(SDSmsConsts.SendMulitpleClass, sInstance.mSendMulitpleClass);
		intent.putExtra(SDSmsConsts.SendMessageClass, sInstance.mSendMessageClass);
		intent.putExtra(SDSmsConsts.SmsReceivedClass, sInstance.mSmsReceivedClass);
		intent.putExtra(SDSmsConsts.SmsDeliverClass, sInstance.mSmsDeliverClass);
		intent.putExtra(SDSmsConsts.WapPushReceivedClass,sInstance.mWapPushReceivedClass);
		intent.putExtra(SDSmsConsts.WapPushDeliverClass, sInstance.mWapPushDeliverClass);
		intent.putExtra(SDSmsConsts.MmsPushClass, sInstance.mMmsPushClass);
		intent.putExtra(SDSmsConsts.MessageSentClass, sInstance.mMessageSentClass);
		intent.putExtra(SDSmsConsts.RespondViaMessageClass, sInstance.mRespondViaMessageClass);
		return intent;
    }
    
    private boolean newCallbackAttempt = false;
    private Thread newCallbackThread;
    private Runnable setNewCallbackMonitor = new Runnable() {
        @Override
        public void run() {
        	synchronized (this) {
            	int count = 0;
    	        while(waitingForServices && svcTestRunning) {
    	            try {
    					if (count % 100 == 0) if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "setNewCallbackMonitor waitingForServices count:" + count);
    	            	this.wait(500);
    	            } catch (InterruptedException e) {
    	            }
        	        count++;
    	        }
				if (SDSmsConsts.DEBUG_SERVICE && count >= 10) Log.w(TAG, "setNewCallbackMonitor waitingForServices timed out, continuing...");
        	}
        	mSDSmsReceiveCallbackConn.resetCallBack(tempSDSmsReceiveCallback);
    		tempSDSmsReceiveCallback = null;
    		removeServices("newCallbackThread");
    		if (!initServicesWaitRunning) initServices("newCallbackThread");
        }
    };
    
    private boolean initServicesWaitRunning = false;
    private Thread initServicesThread;

    private void initServices(final String methodCalling) {
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices called by: " + methodCalling);
		if (initServicesWaitRunning) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices already initializing, ignoring method: " + methodCalling);
			return;
		}
		requestReconnect = false;
    	waitingForServices = true;
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices svcConn starting send service");
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices SEND_SERVICE_PACKAGE: " + SEND_SERVICE_PACKAGE +
				" SEND_SERVICE: " + SEND_SERVICE + " sdsmsID: " + sdsmsID);
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices packageName: " + packageName + " mSendReceiver: " +
				mSendClass + " sdsmsType: " + sdsmsType);
    	svcConn = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mISDSmsSendService = ISDSmsSendService.Stub.asInterface(binder);
				if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices.onServiceConnected ISDSmsSendService status of IBinder, service is null: " + (mISDSmsSendService == null));
			}

			public void onServiceDisconnected(ComponentName className) {
				mISDSmsSendService = null;
				if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "initServices.onServiceDisconnected ISDSmsSendService status of IBinder, service is null: " + (mISDSmsSendService == null));
			}
		};
		svcConnId = svcConn.toString(); 

		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices mSendClass = " + mSendClass + ",\n " + 
				" mSendToClass = " + mSendToClass + ", \n" +
				" mSendMulitpleClass = " + mSendMulitpleClass + ",\n " + 
				" mSendMessageClass = " + mSendMessageClass + ",\n " +
				" mSmsReceivedClass = " + mSmsReceivedClass + ",\n " +
				" mSmsDeliverClass = " + mSmsDeliverClass + ", \n" +
				" mWapPushReceivedClass = " + mWapPushReceivedClass + ",\n " +
				" mWapPushDeliverClass = " + mWapPushDeliverClass + ", \n" +
				" mMmsPushClass = " + mMmsPushClass + ",\n " +
				" mMessageSentClass = " + mMessageSentClass + ", \n" +
				" mRespondViaMessageClass = " + mRespondViaMessageClass);

		if (sdmmLogs) Log.i(LOGS, "Binding SDMM send service to " + mContext.getPackageName());
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices RECEIVE_SERVICE_PACKAGE: " + RECEIVE_SERVICE_PACKAGE +
				" SEND_SERVICE: " + SEND_SERVICE + " sdsmsID: " + sdsmsID);
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices packageName: " + packageName + " mReceiveReceiver: " +
				mSmsReceivedClass);
		//Intent sendServiceIntent = new Intent(SEND_SERVICE_ACTION);
		Intent sendServiceIntent = new Intent();
		sendServiceIntent.setClassName(SEND_SERVICE_PACKAGE, SEND_SERVICE);
		sendServiceIntent = setupIntent(sendServiceIntent);
		
		mContext.bindService(sendServiceIntent, svcConn, Context.BIND_AUTO_CREATE);
		if (sdmmLogs) Log.i(LOGS, "Binding callback SDMM service for " + mContext.getPackageName() + " to SDMM");
		svcConnBound = true;

		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices mSDSmsReceiveCallbackConn starting receive service");
		
		
		// TODO
		// callback should not be null, it should be constructed from available data
		if (mSDSmsReceiveCallbackConn == null) {
			mSDSmsReceiveCallbackConn = new SDSmsReceiveCallbackConnection(mContext, 
				mSendClass, 
				null);
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "initServices mSDSmsReceiveCallbackConn was NULL");
		}
		//Intent receiveServiceIntent = new Intent(RECEIVE_SERVICE_ACTION);
		Intent receiveServiceIntent = new Intent();
		receiveServiceIntent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
		receiveServiceIntent = setupIntent(receiveServiceIntent);
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices RECEIVE_SERVICE_PACKAGE: " + RECEIVE_SERVICE_PACKAGE +
				" RECEIVE_SERVICE: " + RECEIVE_SERVICE + " sdsmsID: " + sdsmsID);
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices packageName: " + packageName + " mReceiveReceiver: " +
				mSmsReceivedClass);

		mSDSmsReceiveCallbackConn.getCallBack().setIntent(receiveServiceIntent);
		mContext.bindService(receiveServiceIntent, mSDSmsReceiveCallbackConn, Context.BIND_AUTO_CREATE);
		if (sdmmLogs) Log.i(LOGS, "Binding SDMM callback service to " + mContext.getPackageName());
		callbackConnId = mSDSmsReceiveCallbackConn.toString();
		callbackConnBound = true;
		
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices starting svcTest thread");
		if (!svcTestRunning) {
			svcTestThread = new Thread(null, svcTest, "svcTest Thread : " + TAG);
			svcTestThread.start();
		}
	}

	private void removeServices(String callingMethod) {
    	if (!waitForServices(callingMethod + " called removeServices")) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, callingMethod + " removeServices timed out, continuing...");
    	}
		if (svcConn != null) {
			try {
				mContext.unbindService(svcConn);
			} catch (Exception e) {
				if (SDSmsConsts.DEBUG_SERVICE) {
					Log.w(TAG, " unbinding failed for svcConn");
				}
				e.printStackTrace();
			}
			//svcConn. = null;
			svcConnBound = false;
		}
		// check for connection - do not unbind an unconnected service
		if (mSDSmsReceiveCallbackConn != null) {
			try {
				mContext.unbindService(mSDSmsReceiveCallbackConn);
			} catch (Exception e) {
				if (SDSmsConsts.DEBUG_SERVICE) {
					Log.w(TAG, " unbinding failed for mSDSmsReceiveCallbackConn");
				}
				e.printStackTrace();
			}
			//mSDSmsReceiveCallbackConn = null;
			callbackConnBound = false;
		}
	}

	public abstract class SDSmsConnectionAdapter implements SDSmsConnectionListener{
		private boolean connected = false;
		private boolean isWaiting = false;
		
		public boolean isConnected() {
			return connected;
		}
		
		public boolean isWaitingForConnection() {
			return isWaiting;
		}
		
		public void setConnected(boolean status) {
			connected = status;
		}
		
		public void setWaitingForConnection(boolean status) {
			isWaiting = status;
		}
		
		public void waitOnConnection() {
			while (!connected) {
				synchronized (this) {
					try {
						if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "waitOnConnection is !connected");
						wait(250);
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}
	}
	
	public interface SDSmsConnectionListener {
		public boolean isConnected();
		public boolean isWaitingForConnection();
	}
}
