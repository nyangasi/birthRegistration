/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/ianmanyama/Dropbox/MyAndroidBullshit/iosched-master/BirthRegistration/sDMM_SDK/src/main/aidl/com/sdmmllc/superdupermm/ISDSmsNotificationService.aidl
 */
package com.sdmmllc.superdupermm;
public interface ISDSmsNotificationService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sdmmllc.superdupermm.ISDSmsNotificationService
{
private static final java.lang.String DESCRIPTOR = "com.sdmmllc.superdupermm.ISDSmsNotificationService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sdmmllc.superdupermm.ISDSmsNotificationService interface,
 * generating a proxy if needed.
 */
public static com.sdmmllc.superdupermm.ISDSmsNotificationService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sdmmllc.superdupermm.ISDSmsNotificationService))) {
return ((com.sdmmllc.superdupermm.ISDSmsNotificationService)iin);
}
return new com.sdmmllc.superdupermm.ISDSmsNotificationService.Stub.Proxy(obj);
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
case TRANSACTION_cancelNotification:
{
data.enforceInterface(DESCRIPTOR);
android.content.Intent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.content.Intent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _result = this.cancelNotification(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sdmmllc.superdupermm.ISDSmsNotificationService
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
@Override public boolean cancelNotification(android.content.Intent intent) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_cancelNotification, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_cancelNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean cancelNotification(android.content.Intent intent) throws android.os.RemoteException;
}
