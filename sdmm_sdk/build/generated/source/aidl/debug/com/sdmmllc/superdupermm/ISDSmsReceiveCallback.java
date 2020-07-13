/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/ianmanyama/Dropbox/MyAndroidBullshit/iosched-master/BirthRegistration/sDMM_SDK/src/main/aidl/com/sdmmllc/superdupermm/ISDSmsReceiveCallback.aidl
 */
package com.sdmmllc.superdupermm;
public interface ISDSmsReceiveCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sdmmllc.superdupermm.ISDSmsReceiveCallback
{
private static final java.lang.String DESCRIPTOR = "com.sdmmllc.superdupermm.ISDSmsReceiveCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sdmmllc.superdupermm.ISDSmsReceiveCallback interface,
 * generating a proxy if needed.
 */
public static com.sdmmllc.superdupermm.ISDSmsReceiveCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sdmmllc.superdupermm.ISDSmsReceiveCallback))) {
return ((com.sdmmllc.superdupermm.ISDSmsReceiveCallback)iin);
}
return new com.sdmmllc.superdupermm.ISDSmsReceiveCallback.Stub.Proxy(obj);
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
case TRANSACTION_onReceive:
{
data.enforceInterface(DESCRIPTOR);
com.sdmmllc.superdupermm.SDSmsReceiveCallback _arg0;
if ((0!=data.readInt())) {
_arg0 = com.sdmmllc.superdupermm.SDSmsReceiveCallback.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.onReceive(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_testCallbackConnection:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.testCallbackConnection(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sdmmllc.superdupermm.ISDSmsReceiveCallback
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
@Override public boolean onReceive(com.sdmmllc.superdupermm.SDSmsReceiveCallback intent) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_onReceive, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
if ((0!=_reply.readInt())) {
intent.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String testCallbackConnection(java.lang.String text) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(text);
mRemote.transact(Stub.TRANSACTION_testCallbackConnection, _data, _reply, 0);
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
static final int TRANSACTION_onReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_testCallbackConnection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public boolean onReceive(com.sdmmllc.superdupermm.SDSmsReceiveCallback intent) throws android.os.RemoteException;
public java.lang.String testCallbackConnection(java.lang.String text) throws android.os.RemoteException;
}
