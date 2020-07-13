/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\cliff.maregeli\\git\\BirthRegistration\\sdmm_sdk\\src\\main\\aidl\\com\\sdmmllc\\superdupermm\\ISDSmsSendService.aidl
 */
package com.sdmmllc.superdupermm;
public interface ISDSmsSendService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sdmmllc.superdupermm.ISDSmsSendService
{
private static final java.lang.String DESCRIPTOR = "com.sdmmllc.superdupermm.ISDSmsSendService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sdmmllc.superdupermm.ISDSmsSendService interface,
 * generating a proxy if needed.
 */
public static com.sdmmllc.superdupermm.ISDSmsSendService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sdmmllc.superdupermm.ISDSmsSendService))) {
return ((com.sdmmllc.superdupermm.ISDSmsSendService)iin);
}
return new com.sdmmllc.superdupermm.ISDSmsSendService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_sendTextMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
android.app.PendingIntent _arg3;
if ((0!=data.readInt())) {
_arg3 = android.app.PendingIntent.CREATOR.createFromParcel(data);
}
else {
_arg3 = null;
}
android.app.PendingIntent _arg4;
if ((0!=data.readInt())) {
_arg4 = android.app.PendingIntent.CREATOR.createFromParcel(data);
}
else {
_arg4 = null;
}
this.sendTextMessage(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_sendMultipartTextMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.util.List<java.lang.String> _arg2;
_arg2 = data.createStringArrayList();
java.util.List<android.app.PendingIntent> _arg3;
_arg3 = data.createTypedArrayList(android.app.PendingIntent.CREATOR);
java.util.List<android.app.PendingIntent> _arg4;
_arg4 = data.createTypedArrayList(android.app.PendingIntent.CREATOR);
this.sendMultipartTextMessage(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_sendDataMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
char _arg2;
_arg2 = (char)data.readInt();
byte[] _arg3;
_arg3 = data.createByteArray();
android.app.PendingIntent _arg4;
if ((0!=data.readInt())) {
_arg4 = android.app.PendingIntent.CREATOR.createFromParcel(data);
}
else {
_arg4 = null;
}
android.app.PendingIntent _arg5;
if ((0!=data.readInt())) {
_arg5 = android.app.PendingIntent.CREATOR.createFromParcel(data);
}
else {
_arg5 = null;
}
this.sendDataMessage(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_testConnection:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.testConnection(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sdmmllc.superdupermm.ISDSmsSendService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void sendTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.lang.String text, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(destinationAddress);
_data.writeString(scAddress);
_data.writeString(text);
if ((sentIntent!=null)) {
_data.writeInt(1);
sentIntent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((deliveryIntent!=null)) {
_data.writeInt(1);
deliveryIntent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendTextMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void sendMultipartTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.util.List<java.lang.String> parts, java.util.List<android.app.PendingIntent> sentIntents, java.util.List<android.app.PendingIntent> deliveryIntents) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(destinationAddress);
_data.writeString(scAddress);
_data.writeStringList(parts);
_data.writeTypedList(sentIntents);
_data.writeTypedList(deliveryIntents);
mRemote.transact(Stub.TRANSACTION_sendMultipartTextMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void sendDataMessage(java.lang.String destinationAddress, java.lang.String scAddress, char destinationPort, byte[] data, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(destinationAddress);
_data.writeString(scAddress);
_data.writeInt(((int)destinationPort));
_data.writeByteArray(data);
if ((sentIntent!=null)) {
_data.writeInt(1);
sentIntent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((deliveryIntent!=null)) {
_data.writeInt(1);
deliveryIntent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendDataMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String testConnection(java.lang.String str) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(str);
mRemote.transact(Stub.TRANSACTION_testConnection, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_sendTextMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_sendMultipartTextMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_sendDataMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_testConnection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void sendTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.lang.String text, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) throws android.os.RemoteException;
public void sendMultipartTextMessage(java.lang.String destinationAddress, java.lang.String scAddress, java.util.List<java.lang.String> parts, java.util.List<android.app.PendingIntent> sentIntents, java.util.List<android.app.PendingIntent> deliveryIntents) throws android.os.RemoteException;
public void sendDataMessage(java.lang.String destinationAddress, java.lang.String scAddress, char destinationPort, byte[] data, android.app.PendingIntent sentIntent, android.app.PendingIntent deliveryIntent) throws android.os.RemoteException;
public java.lang.String testConnection(java.lang.String str) throws android.os.RemoteException;
}
