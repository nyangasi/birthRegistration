/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/ianmanyama/Dropbox/MyAndroidBullshit/iosched-master/BirthRegistration/sDMM_SDK/src/main/aidl/com/sdmmllc/superdupermm/ISDSmsReceiveService.aidl
 */
package com.sdmmllc.superdupermm;
public interface ISDSmsReceiveService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sdmmllc.superdupermm.ISDSmsReceiveService
{
private static final java.lang.String DESCRIPTOR = "com.sdmmllc.superdupermm.ISDSmsReceiveService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sdmmllc.superdupermm.ISDSmsReceiveService interface,
 * generating a proxy if needed.
 */
public static com.sdmmllc.superdupermm.ISDSmsReceiveService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sdmmllc.superdupermm.ISDSmsReceiveService))) {
return ((com.sdmmllc.superdupermm.ISDSmsReceiveService)iin);
}
return new com.sdmmllc.superdupermm.ISDSmsReceiveService.Stub.Proxy(obj);
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
case TRANSACTION_registerApp:
{
data.enforceInterface(DESCRIPTOR);
android.content.Intent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.content.Intent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.registerApp(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_registerReceiver:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
android.content.Intent _arg2;
if ((0!=data.readInt())) {
_arg2 = android.content.Intent.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
com.sdmmllc.superdupermm.SDSmsReceiveCallback _arg3;
if ((0!=data.readInt())) {
_arg3 = com.sdmmllc.superdupermm.SDSmsReceiveCallback.CREATOR.createFromParcel(data);
}
else {
_arg3 = null;
}
java.lang.String _result = this.registerReceiver(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeString(_result);
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
private static class Proxy implements com.sdmmllc.superdupermm.ISDSmsReceiveService
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
@Override public boolean registerApp(android.content.Intent intent) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((intent!=null)) {
_data.writeInt(1);
intent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_registerApp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String registerReceiver(java.lang.String id, int type, android.content.Intent intent, com.sdmmllc.superdupermm.SDSmsReceiveCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(id);
_data.writeInt(type);
if ((intent!=null)) {
_data.writeInt(1);
intent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((callback!=null)) {
_data.writeInt(1);
callback.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_registerReceiver, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String testConnection(java.lang.String test) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(test);
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
static final int TRANSACTION_registerApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_registerReceiver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_testConnection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public boolean registerApp(android.content.Intent intent) throws android.os.RemoteException;
public java.lang.String registerReceiver(java.lang.String id, int type, android.content.Intent intent, com.sdmmllc.superdupermm.SDSmsReceiveCallback callback) throws android.os.RemoteException;
public java.lang.String testConnection(java.lang.String test) throws android.os.RemoteException;
}
