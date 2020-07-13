package com.sdmmllc.superdupersmsmanager.sdk;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sdmmllc.superdupermm.SDSmsIntentReceiver;
import com.sdmmllc.superdupermm.SDSmsIntentRegistrationReceiver;
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

public class SDSmsManager {

	private static String TAG = "SDSmsManager";

	public static final int ACCESS_TYPE_SYSTEM 			= 0;
	public static final int ACCESS_TYPE_SYSRESERVED 	= 1;
	public static final int ACCESS_TYPE_ANTISPAM 		= 2;
	public static final int ACCESS_TYPE_PLUGIN	 		= 3;
	public static final int ACCESS_TYPE_AUTORESPONDER 	= 4;
	public static final int ACCESS_TYPE_MSGRESERVED 	= 5;
	public static final int ACCESS_TYPE_MESSAGING 		= 6;
	public static final int ACCESS_TYPE_BACKUP 			= 7;
	public static final int ACCESS_TYPE_OBSERVER		= 8;
	public static final int ACCESS_TYPE_UNREGISTERED 	= 9;
	
	public static final String CHECK_SDSMS_STATUS 			= "CheckSDSmsStatus";
	public static final String SDSMS_SEND_TEXT 				= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_MSG";
	public static final String SDSMS_SEND_DATA 				= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_DATA";
	public static final String SDSMS_SEND_MULTIPART_TEXT 	= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_MULTIPART";
	public static final String SDSMS_PACKAGE 				= "com.sdmmllc.superdupermessagingmanager";
	public static final String SDSMS_MAIN_CLASS				= "com.messagingappsllc.superdupermms.mms.ui.ConversationList";
	public static final String SDSMS_SEND_PKG 				= "com.sdmmllc.superdupermessagingmanager";
	public static final String SEND_SERVICE 				= "com.sdmmllc.superdupermm.SDSmsSendService";
	
	public static final String SEND_SMS_PERM 		= "com.sdmmllc.superdupersmsmanager.SEND_SMS";
	public static final String WRITE_SMS_PERM 		= "com.sdmmllc.superdupersmsmanager.WRITE_SMS";
	
	// fields for sending SMS
	public static final String DEST 			= "mDest";
	public static final String SVC_CENTER 		= "mServiceCenter";
	public static final String PORT 			= "port";
	public static final String MESSAGE 			= "text";
	public static final String MSG_DATA 		= "msg_data";
	public static final String MESSAGES 		= "messages";
	public static final String SENT_INTENT 		= "sentIntent";
	public static final String SENT_INTENTS 	= "sentIntents";
	public static final String DELIVERY_INTENT 	= "deliveryIntent";
	public static final String DELIVERY_INTENTS = "deliveryIntents";
	public static final String ABORT_BROADCAST 	= "abort";
	
	// fields for SDSmsManager comms
	public static final String TEST_BROADCAST = "test_sdmm";
	public static final String TEST_MSG = "This is a test message from Super Duper Messaging Manager";
	public static final String TEST_RESPONSE = "test_sdmm_response";
	public static final String TEST_DISPLAYED = "test_sdmm_displayed";
	public static final String TEST_UPDATE_REQUIRED = "test_sdmm_updated_required";
	public static final String TEST_SUCCESS = "test_sdmm_success";
	public static final String TEST_PACKAGE = "test_sdmm_package";
	public static final String TEST_VERSION = "test_sdmm_version";

	public static final String sdmm_send_class 					= "com.sdmmllc.superdupersmsmanager.sdk.SEND_CLASS";
	public static final String sdmm_sendto_class 				= "com.sdmmllc.superdupersmsmanager.sdk.SENDTO_CLASS";
	public static final String sdmm_send_multiple_class			= "com.sdmmllc.superdupersmsmanager.sdk.SEND_MULTIPLE_CLASS";
	public static final String sdmm_send_message_class			= "com.sdmmllc.superdupersmsmanager.sdk.SEND_MESSAGE_CLASS";
	public static final String sdmm_sms_received_class			= "com.sdmmllc.superdupersmsmanager.sdk.SMS_RECEIVED_CLASS";
	public static final String sdmm_sms_deliver_class			= "com.sdmmllc.superdupersmsmanager.sdk.SMS_DELIVER_CLASS";
	public static final String sdmm_wap_push_received_class		= "com.sdmmllc.superdupersmsmanager.sdk.WAP_PUSH_RECEIVED_CLASS";
	public static final String sdmm_wap_push_deliver_class		= "com.sdmmllc.superdupersmsmanager.sdk.WAP_PUSH_DELIVER_CLASS";
	public static final String sdmm_mms_push_class				= "com.sdmmllc.superdupersmsmanager.sdk.MMS_PUSH_CLASS";
	public static final String sdmm_message_sent_class			= "com.sdmmllc.superdupersmsmanager.sdk.MESSAGE_SENT_CLASS";
	public static final String sdmm_respond_via_message_class	= "com.sdmmllc.superdupersmsmanager.sdk.RESPOND_VIA_MESSAGE_CLASS";

	public static final String RESPONSE_MSG = "SDMM Default Response";
	
	public static final short DEF_DATA_PORT = 8091;
	
	private static SmsManager sManager;
	private static SDSmsManager sInstance;
	private static SDSmsServiceManager mServiceManager;
	
	private static Context mContext;

	private static Boolean sdsmsManagerInstalled = false;
	private static Boolean sdsmsManagerDefault = false;
	private static SDSmsIntentReceiver receiver;
	private static SDSmsIntentRegistrationReceiver registrationReceiver;
	
	private SDSmsManager mInstance = null;
	private static String sdmmPrefs = "sdmm_prefs";
	private static SharedPreferences prefs;
	private static SharedPreferences.Editor editor;
	private static String version = "0.94.0";
	
	private SDSmsManager (Context context, String id, int type, 
			String sendReceiver, String receiveReceiver) {
		Log.w(SDSmsServiceManager.LOGS, "This version of SDSmsManager is deprecated");
		Log.w(SDSmsServiceManager.LOGS, "WARNING: SDMM may continue to appear to work");
		Log.w(SDSmsServiceManager.LOGS, "Please see the updated SDK integration instructions");
		mContext = context;
		isSdsmsIntstalled();
		sManager = SmsManager.getDefault();
		mInstance = this;
		prefs = context.getSharedPreferences(sdmmPrefs, 0);
		editor = prefs.edit();
		version = context.getString(R.string.sdmm_app_version);
		
		registrationReceiver();
		registerStartServiceReceiver();
		mServiceManager = SDSmsServiceManager.getInstance(context);
		SDSmsServiceManager.setSdsmsId(id);
		SDSmsServiceManager.setSdsmsType(type);
		SDSmsServiceManager.setSmsReceivedClass(receiveReceiver);
		SDSmsServiceManager.setSmsSendClass(sendReceiver);
	}
	
	private SDSmsManager (Context context) {
		Log.i(SDSmsServiceManager.LOGS, "Starting SDMM version: " + version);
		mContext = context;
		isSdsmsIntstalled();
		sManager = SmsManager.getDefault();
		mInstance = this;
		prefs = context.getSharedPreferences(sdmmPrefs, 0);
		editor = prefs.edit();
		String xmlVersion = context.getString(R.string.sdmmVersion);
		if (!xmlVersion.equals(version)) {
			Log.w(SDSmsServiceManager.WARNING, "SDMM SDK versions do not match: " + version + " compared to " + xmlVersion);
			Log.w(SDSmsServiceManager.WARNING, "Please update your app and your sdmm_default_strings.xml file");
		} else 	Log.i(SDSmsServiceManager.LOGS, "SDMM loading at version :" + version);

		
		registrationReceiver();
		registerStartServiceReceiver();
		mServiceManager = SDSmsServiceManager.getInstance(context);
	}
	
	public static Context getContext() {
		return mContext;
	}
	
	public static SDSmsManager getDefault(Context context) {
		if (sInstance == null) {
			sInstance = new SDSmsManager(context);
		}
		return sInstance;
	}
	
	public static SDSmsManager getDefault() {
		if (sInstance == null) {
			Log.w(SDSmsServiceManager.WARNING, "You are requesting SDSmsManager before you initialized it.");
		}
		return sInstance;
	}
	
	public static SDSmsManager initialize(Context context) {
		if (sInstance == null) {
			sInstance = new SDSmsManager(context);
		}
		return sInstance;
	}
	
	public static SDSmsManager getDefault(Context context, String id, int type, 
			String sendReceiver, String receiveReceiver) {
		if (sInstance == null) {
			sInstance = new SDSmsManager(context, id, type, sendReceiver, receiveReceiver);
		}
		return sInstance;
	}
	
	public static boolean checkConnections() {
		return mServiceManager.checkConnections();
	}
	
	public static boolean isWaitingForServices() {
		return mServiceManager.isWaitingForServices();
	}
	
	public static void requestReconnect(String pkgName) {
		if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "Sending reconnect intent to: " + pkgName + 
				" receiver: " + SDSmsConsts.SDSMS_RELOAD_RECEIVER); 
		requestConnect(pkgName);
	}
	
	public static void requestConnect(String pkgName) {
		Intent intent = new Intent(SDSmsConsts.SDSMS_RELOAD);
		intent.setClassName(pkgName, SDSmsConsts.SDSMS_RELOAD_RECEIVER);
		mContext.sendBroadcast(intent);
		if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "Sending connect intent to: " + pkgName + 
				" receiver: " + SDSmsConsts.SDSMS_RELOAD_RECEIVER); 
	}

	private void registrationReceiver() {
    	if (SDSmsConsts.DEBUG_SERVICE) 
			Log.i(TAG, "registrationReceiver registering...");
		IntentFilter filter = new IntentFilter();
		filter.addAction(SDSmsConsts.SDSMS_RELOAD);
		registrationReceiver = new SDSmsIntentRegistrationReceiver();
		mContext.registerReceiver(registrationReceiver, filter);
	}
	
	private void registerStartServiceReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SDSmsServiceManager.START_SERVICE);
		receiver = new SDSmsIntentReceiver();
		mContext.registerReceiver(receiver, filter);
	}
	
	public static SDSmsServiceManager getConnectionManager() {
		return mServiceManager;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public static int getVersionCode() {
		try {
			return mContext.getPackageManager().getPackageInfo(
					mContext.getString(R.string.sdmm_package_name), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public static boolean isListenerSetup() {
		String enabledNotificationListeners = Settings.Secure.getString(mContext.getContentResolver(),
				"enabled_notification_listeners");
		if (enabledNotificationListeners == null ||
				!enabledNotificationListeners.contains(
						mContext.getString(R.string.sdmm_package_name))) {
        	return false;
        } else {
        	return true;
        }
	}
	
	public static boolean isNonDefaultSetupComplete() {
		return isListenerSetup();
	}
	
	public static boolean betaEnabled() {
		String packName = mContext.getString(R.string.sdmm_package_name);
	    String mResourceName = "sdmmBeta";
	    boolean betaEnabled = false;

	    try {
	        PackageManager manager = mContext.getPackageManager();
	        Resources mApk1Resources = manager.getResourcesForApplication(packName);
	        int mBooleanResID = mApk1Resources.getIdentifier(mResourceName, "bool", packName);
	        betaEnabled = mApk1Resources.getBoolean( mBooleanResID );
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

		return mContext.getResources().getBoolean(R.bool.sdmmBeta)
				&& betaEnabled;
	}
	
	public static void notifySdmmListenerSetupRequired(final Context context) {
		if (isListenerSetup()) return;
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View helpTextView = mInflater.inflate(R.layout.act_init_listener_setup, null);
		AlertDialog ad = new AlertDialog.Builder(context)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.initListenerContinueBtnTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            	context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.initListenerCancelBtnTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public void setReceiverCallback(SDSmsReceiveCallback callback) {
		mServiceManager.setReceiverCallback(callback);
	}
	
	public static boolean isSdsmsIntstalled(){
		if (mContext == null) {
			Log.w(TAG, "SDSmsManager.isSdsmsIntstalled() method called with a null context; check to be sure you Application or Activity has called getDefault(...) first");
			return false;
		}
		for (ApplicationInfo packageInfo : mContext.getPackageManager().getInstalledApplications(0)) {
			if (packageInfo.packageName.equals(SDSMS_PACKAGE)) {
				sdsmsManagerInstalled = true;
				if (sdsmsManagerInstalled) isSdsmsDefaultSmsApp();
				if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "Found SDMM Package");
				return true;
			}
		}        
		sdsmsManagerInstalled = false;
		return false;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	public static boolean isSdsmsDefaultSmsApp() {
		if (hasKitKat()) {
			sdsmsManagerDefault = SDSMS_PACKAGE.equals(Telephony.Sms.getDefaultSmsPackage(mContext));
			if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "Status of SDMM Package as Default: " + sdsmsManagerDefault);
			return sdsmsManagerDefault;
		}
		return false;
	}
	
	public static boolean isSdmmVerified() {
		return prefs.getBoolean(TEST_BROADCAST, false);
	}
	
	public static void setSdmmVerified(boolean verified) {
		editor.putBoolean(TEST_BROADCAST, verified);
		editor.commit();
	}
	
	public static boolean hasSdmmVerifiedDisplayed() {
		return prefs.getBoolean(TEST_DISPLAYED, false);
	}
	
	public static void setSdmmVerifiedDisplayed(boolean verified) {
		editor.putBoolean(TEST_DISPLAYED, verified);
		editor.commit();
	}
	
	public static void displaySdmmVerified() {
		if (prefs.getBoolean(TEST_BROADCAST, false) && !prefs.getBoolean(TEST_DISPLAYED, false))
			notifySdmmVerified(mContext);
	}
	
	public static void notifySetDefaultSms(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmChangeDefSmsTitleTxt));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmChangeDefSmsDescrTxt)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmChangeDefSmsChangeTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SDSMS_PACKAGE);

    			//startActivity(intent);
    			context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.sdmmChangeDefSmsNoChangeTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void notifySdmmRequired(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.instKitKatFoundWarningTitle));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.instKitKatFoundWarningDescr)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.instKitKatFoundWarningInstall), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse("https://play.google.com/store/apps/details?id=com.sdmmllc.superdupermessagingmanager"));
				context.startActivity(browserIntent);				
            }
        })
        .setNegativeButton(context.getString(R.string.instKitKatFoundWarningMoreInfo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void notifySdmmVerified(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmVerifiedTitle));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmVerifiedDescr)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmVerifiedDone), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SDSMS_PACKAGE);

    			//startActivity(intent);
    			context.startActivity(intent);
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void launchSuperDuper(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmLaunchTitleTxt));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmLaunchDescrTxt)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmLaunchTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent();
            	intent.setClassName(SDSMS_PACKAGE, SDSMS_MAIN_CLASS);
    			context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.sdmmLaunchNoLaunchTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public void sendTextMessage(
			String destinationAddress, String scAddress, String text,
			PendingIntent sentIntent, PendingIntent deliveryIntent) {
		if (hasKitKat()) {
			try {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage using service");
				mServiceManager.getSendService().sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
			} catch (RemoteException e) {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage broadcast intent");
				Intent sendMsg = new Intent(SEND_SERVICE);
				sendMsg = new Intent(SDSMS_SEND_TEXT);
				sendMsg.putExtra(DEST, destinationAddress);
				sendMsg.putExtra(SVC_CENTER, scAddress);
				sendMsg.putExtra(MESSAGE, text);
				sendMsg.putExtra(SENT_INTENT, sentIntent);
				sendMsg.putExtra(DELIVERY_INTENT, deliveryIntent);
				SDSmsManager.mContext.sendBroadcast(sendMsg);
				e.printStackTrace();
				mServiceManager.hasServiceConnection();
			}
		} else {
			sManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
		}
	}

	public void sendMultipartTextMessage (
			String destinationAddress, String scAddress, ArrayList<String> parts,
			ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
		if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, "sendMultipartTextMessage sending message");
		if (hasKitKat()) {
			try {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendMultipartTextMessage using service");
				mServiceManager.getSendService().sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
			} catch (RemoteException e) {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendMultipartTextMessage broadcast intent");
				Intent sendMsg = new Intent(SEND_SERVICE);
				sendMsg = new Intent(SDSMS_SEND_MULTIPART_TEXT);
				sendMsg.putExtra(DEST, destinationAddress);
				sendMsg.putExtra(SVC_CENTER, scAddress);
				sendMsg.putExtra(MESSAGES, parts);
				sendMsg.putExtra(SENT_INTENTS, sentIntents);
				sendMsg.putExtra(DELIVERY_INTENTS, deliveryIntents);
				if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".sendMultipartTextMessage broadcast intent");
				SDSmsManager.mContext.sendBroadcast(sendMsg);
				e.printStackTrace();
				mServiceManager.hasServiceConnection();
			}
		} else {
			sManager.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
		}
	}

	public void sendDataMessage (
			String destinationAddress, String scAddress, short destinationPort,
			byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
		if (hasKitKat()) {
			try {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendDataMessage using service");
				mServiceManager.getSendService().sendDataMessage(destinationAddress, scAddress, (char)destinationPort,
						data, sentIntent, deliveryIntent);
			} catch (RemoteException ee) {
				Intent sendMsg = new Intent(SDSMS_SEND_DATA);
				sendMsg.putExtra(DEST, destinationAddress);
				sendMsg.putExtra(SVC_CENTER, scAddress);
				sendMsg.putExtra(PORT, destinationPort);
				sendMsg.putExtra(MSG_DATA, data);
				sendMsg.putExtra(SENT_INTENT, sentIntent);
				sendMsg.putExtra(DELIVERY_INTENT, deliveryIntent);
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendDataMessage broadcast intent");
				SDSmsManager.mContext.sendBroadcast(sendMsg);
				mServiceManager.hasServiceConnection();
			}
		} else {
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendDataMessage using SmsManager, destinationAddress: " + destinationAddress + 
					" scAddress: " + scAddress);
			sManager.sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
		}
	}

	public ArrayList<String> divideMessage(String text) {
		return sManager.divideMessage(text);
	}

	public static String contentMms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDMMS_URI.toString();
		else return SDSmsConsts.MMS_URI.toString();
	}

	public static String startsWithMms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return "sdmms";
		else return "mms";
	}

	public static String getTypeFromAuthority(String authority) {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp() && authority.equals("sdmms")) return "mms";
		else return authority;
	}

	public static String contentSms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentSms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDSMS_URI.toString();
		else return SDSmsConsts.SMS_URI.toString();
	}

	public static String contentMmsSms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMmsSms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDCONTENT_URI.toString();
		else return SDSmsConsts.CONTENT_URI.toString();
	}

	public static final class Sms {

		private Sms () {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://sms");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		public static final class Inbox {
			private Inbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/inbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/inbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Sent {
			private Sent() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/sent");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/sent");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Draft {
			private Draft() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/draft");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/draft");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Outbox {
			private Outbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/outbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/outbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Conversations {
			private static final Uri CONTENT_URI = Uri.parse("content://sms/conversations");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/conversations");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class Mms {

		private Mms() {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://mms");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		private static final Uri REPORT_REQUEST_URI = Uri.withAppendedPath(CONTENT_URI, "report-request");
		private static final Uri SDREPORT_REQUEST_URI = Uri.withAppendedPath(SDCONTENT_URI, "report-request");

		public static Uri REPORT_REQUEST_URI() {
			if (isSdsmsDefaultSmsApp()) return SDREPORT_REQUEST_URI;
			else return REPORT_REQUEST_URI;
		}

		private static final Uri REPORT_STATUS_URI = Uri.withAppendedPath(CONTENT_URI, "report-status");
		private static final Uri SDREPORT_STATUS_URI = Uri.withAppendedPath(SDCONTENT_URI, "report-status");

		public static Uri REPORT_STATUS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDREPORT_STATUS_URI;
			else return REPORT_STATUS_URI;
		}

		public static final String DEFAULT_SORT_ORDER = "date DESC";

		public static final class Inbox {
			private Inbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://mms/inbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/inbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Sent {
			private Sent() {
			}

			private static final Uri CONTENT_URI = Uri.parse("content://mms/sent");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/sent");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Draft {
			private Draft() {
			}

			private static final Uri CONTENT_URI = Uri.parse("content://mms/drafts");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/drafts");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Outbox {
			private Outbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://mms/outbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/outbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class MmsSms {
		private MmsSms() {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms-sdsms/");
		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}
		
		private static final Uri CONTENT_DRAFT_URI = Uri.parse("content://mms-sms/draft");
		private static final Uri SDCONTENT_DRAFT_URI = Uri.parse("content://sdmms-sdsms/draft");
		public static Uri CONTENT_DRAFT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_DRAFT_URI;
			else return CONTENT_DRAFT_URI;
		}
		
		private static final Uri CONTENT_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
		private static final Uri SDCONTENT_CONVERSATIONS_URI = Uri.parse("content://sdmms-sdsms/conversations");
		public static Uri CONTENT_CONVERSATIONS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_CONVERSATIONS_URI;
			else return CONTENT_CONVERSATIONS_URI;
		}

		private static final Uri CONTENT_FILTER_BYPHONE_URI = Uri.parse("content://mms-sms/messages/byphone");
		private static final Uri SDCONTENT_FILTER_BYPHONE_URI = Uri.parse("content://sdmms-sdsms/messages/byphone");
		public static Uri CONTENT_FILTER_BYPHONE_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_FILTER_BYPHONE_URI;
			else return CONTENT_FILTER_BYPHONE_URI;
		}

		private static final Uri CONTENT_UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
		private static final Uri SDCONTENT_UNDELIVERED_URI = Uri.parse("content://sdmms-sdsms/undelivered");
		public static Uri CONTENT_UNDELIVERED_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_UNDELIVERED_URI;
			else return CONTENT_UNDELIVERED_URI;
		}

		private static final Uri CONTENT_LOCKED_URI = Uri.parse("content://mms-sms/locked");
		private static final Uri SDCONTENT_LOCKED_URI = Uri.parse("content://sdmms-sdsms/locked");
		public static Uri CONTENT_LOCKED_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_LOCKED_URI;
			else return CONTENT_LOCKED_URI;
		}

		private static final Uri SEARCH_URI = Uri.parse("content://mms-sms/search");
		private static final Uri SDSEARCH_URI = Uri.parse("content://sdmms-sdsms/search");
		public static Uri SEARCH_URI() {
			if (isSdsmsDefaultSmsApp()) return SDSEARCH_URI;
			else return SEARCH_URI;
		}


		public static final class PendingMessages {
			private PendingMessages() {
			}
			
			private static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, "pending");
			private static final Uri SDCONTENT_URI = Uri.withAppendedPath(MmsSms.SDCONTENT_URI, "pending");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class Threads {
		private Threads() {
		}

		private static final Uri THREAD_ID_CONTENT_URI = Uri.parse("content://mms-sms/threadID");
		private static final Uri SDTHREAD_ID_CONTENT_URI = Uri.parse("content://sdmms-sdsms/threadID");

		public static Uri THREAD_ID_CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDTHREAD_ID_CONTENT_URI;
			else return THREAD_ID_CONTENT_URI;
		}

		private static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations");
		private static final Uri SDCONTENT_URI = Uri.withAppendedPath(MmsSms.SDCONTENT_URI, "conversations");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		private static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(CONTENT_URI, "obsolete");
		private static final Uri SDOBSOLETE_THREADS_URI = Uri.withAppendedPath(SDCONTENT_URI, "obsolete");

		public static Uri OBSOLETE_THREADS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDOBSOLETE_THREADS_URI;
			else return OBSOLETE_THREADS_URI;
		}

	}

	public static final class Rate {
		private Rate() {
		}

		private static final Uri CONTENT_URI = Uri.withAppendedPath(Mms.CONTENT_URI, "rate");
		private static final Uri SDCONTENT_URI = Uri.withAppendedPath(Mms.SDCONTENT_URI, "rate");
		
		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		public static final String SENT_TIME = "sent_time";
	}
	
}