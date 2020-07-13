package com.sdmmllc.superdupersmsmanager.sdk;

import android.net.Uri;

public class SDSmsConsts {

	public static final Uri SDCONTENT_URI = Uri.parse("content://sdmms-sdsms/");
	public static final Uri SDMMS_URI = Uri.parse("content://sdmms/");
	public static final Uri SDSMS_URI = Uri.parse("content://sdsms/");

	public static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
	public static final Uri MMS_URI = Uri.parse("content://mms/");
	public static final Uri SMS_URI = Uri.parse("content://sms/");
	
	public static final String SDSMS_REGISTRATION_ID = "SDSmsSdkId";
	public static final String SDSMS_REGISTRATION_TYPE = "SDSmsSdkType";
	public static final String SDSMS_REGISTRATION_PACKAGE_NAME = "PackageName";
	public static final String SDSMS_REGISTRATION_RECEIVER_NAME = "ReceiverName";
	
	public static final String SendClass 				= "SendClass"; 
	public static final String SendToClass 				= "SendToClass";
	public static final String SendMulitpleClass 		= "SendMulitpleClass";
	public static final String SendMessageClass 		= "SendMessageClass";
	public static final String SmsReceivedClass 		= "SmsReceivedClass";
	public static final String SmsDeliverClass 			= "SmsDeliverClass";
	public static final String WapPushReceivedClass 	= "WapPushReceivedClass";
	public static final String WapPushDeliverClass 		= "WapPushDeliverClass";
	public static final String MmsPushClass 			= "MmsPushClass";
	public static final String MessageSentClass 		= "MessageSentClass";
	public static final String RespondViaMessageClass 	= "RespondViaMessageClass";
	
	public static final String SDSMS_INSTALL_PKG = "com.sdmmllc.superdupermessagingmanager";
	public static final String SDSMS_INTENT_RECEIVER = "com.sdmmllc.superdupermm.SDSmsIntentReceiver";
	public static final String SDSMS_RELOAD_RECEIVER = "com.sdmmllc.superdupermm.SDSmsIntentRegistrationReceiver";
	public static final String SDSMS_RELOAD = "com.sdmmllc.superdupersms.RELOAD_CONNECTION";
	
	public static final String SDSMS_ABORT = "abort";
	public static final String SDSMS_DUPLICATE = "duplicate";
	
	public static final String SDSMS_PERM_SEND = "com.sdmmllc.superdupersmsmanager.SEND_SMS";
	public static final String SDSMS_PERM_WRITE = "com.sdmmllc.superdupersmsmanager.WRITE_SMS";
	
	public static final boolean 
		DEBUG_UTILS 		= false,
		DEBUG_SERVICE 		= false,
		DEBUG_DEFAULT 		= false,
		DEBUG_SEND_SMS 		= false,
		DEBUG_SEND_MMS 		= false,
		DEBUG_DRAFT 		= false, 
		DEBUG_INSTALL_RECEIVER = false,
		DEBUG_RECEIVE_SMS 	= false,
		DEBUG_RECEIVE_MMS 	= false;
}
