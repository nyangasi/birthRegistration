package com.sdmmllc.superdupermm;

import java.util.List; 

interface ISDSmsSendService {
	void sendTextMessage(
			String destinationAddress, String scAddress, String text,
			in PendingIntent sentIntent, in PendingIntent deliveryIntent);
	void sendMultipartTextMessage(
			String destinationAddress, String scAddress, in List<String> parts,
			in List<PendingIntent> sentIntents, in List<PendingIntent> deliveryIntents);
	void sendDataMessage(
			String destinationAddress, String scAddress, char destinationPort,
			in byte[] data, in PendingIntent sentIntent, in PendingIntent deliveryIntent);
	String testConnection(String str);
}

