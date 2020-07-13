/*
 * Copyright (C) 2015 UNICEF Tanzania.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tz.co.rita.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

import tz.co.rita.birthregistration.BirthRegistrationApplication;
import tz.co.rita.birthregistration.LoginActivity;
import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.BirthRecordModel;
import tz.co.rita.data.BirthRegistrationProvider;
import tz.co.rita.data.UserAdapter;
import tz.co.rita.dataexchange.DataUploader;

/**
 * Broadcast receiver for SMS messages intended for this application. The app interacts with the server
 * by sending and receiving SMS messages.  This class handles the reception and processing of messages
 * sent by the server.  <BR>
 * <BR>
 * Three types of messages are accepted and processed by this broadcast receiver.
 * <ul>
 * <li><b>Record Management Messages:</b> Report of successful reception of uploaded record.</li>
 * <li><b>User Management Messages:</b> These messages are sent by the server for managing
 * user accounts on this device.</li>
 * <li><b>App Management Messages:</b> These messages are sent by the server for managing
 * the application status on this device.</li>
 * </ul>
 * <BR>
 * Messages sent by the server must conform to one of the following two formats.
 * <ul>
 * <li>[Message Type Form][Space][Message Content]</li>
 * <li>[App Identifier][Separator][Message Type][Separator][Command][Separator][Argument]</li>
 * </ul>
 * The first format is used only for record management messages which acknowledge the reception of
 * an uploaded record by the server. The second format is used for user and app management messages as
 * described below.
 * <BR>
 * <BR>
 * <b>User Management Messages:</b> These messages are sent by the server for managing
 * user accounts on this device.  These messages must conform to the following format <BR>
 * [App Identifier][Separator][Message Type][Separator][Command][Separator][Argument], where
 * <ul>
 * <li>[App Identifier] = <code>tz.co.rita.constantsBirthRegistrationConstants.MBIRTH</code></li>
 * <li>[Separator] = <code>tz.co.rita.constantsBirthRegistrationConstants.SEPARATOR_SYMBOL</code></li>
 * <li>[Message Type] = <code>tz.co.rita.constantsBirthRegistrationConstants.USER</code></li>
 * <li>[Command] = Code identifying the task to perform</li>
 * <li>[Argument] = The username</li>
 * </ul>
 * <BR>
 * [Command] can be one of the following
 * <ul>
 * <li><code>BirthRegistrationConstants.USER_VERIFY_ACCEPTED</code>: Server acknowledges receiving
 * a previously sent request for verification (enabling) of the user account identified by the argument.
 * The initial request is sent automatically when a new user account is created.</li>
 * <li><code>BirthRegistrationConstants.USER_VERIFY_VERIFIED</code>: Server instruction to change status
 * of the user account identified by the argumentto verified.  Once this message is received the status of the user
 * account is changed to verified and it can be used to log in and create birth registration records.
 * </li>
 * <li><code>BirthRegistrationConstants.USER_ACCT_DISABLE</code>: Server instruction to change status
 * of the user account identified by the argument to disabled.  Up on  receiving this message the
 * status of the useraccount is changed to disabled. The user account cannot be used to log in to the app.</li>
 * <li><code>BirthRegistrationConstants.USER_ACCT_ENABLE</code>: Server instruction to change status
 * of the user account identified by the argument to enabled.  Up on  receiving this message the status of the user
 * account is changed to enabled. The user account can now be used to log in to the app.</li>
 * <li><code>BirthRegistrationConstants.USER_PASSWORD_RESET</code>: Server instruction to enable the password
 * reset feature.  Up on receiving this message the application enables the password reset feature.
 * The password reset link will now be visible in the login screen and a new password can be assigned
 * to an existing user account.</li>
 * </ul>
 * <BR>
 * <BR>
 * <b>App Management Messages:</b> These messages must conform to the following format <BR>
 * [App Identifier][Separator][Message Type][Separator][Command][Separator][Argument], where
 * <ul>
 * <li>[App Identifier] = <code>tz.co.rita.constantsBirthRegistrationConstants.MBIRTH</code></li>
 * <li>[Separator] = <code>tz.co.rita.constantsBirthRegistrationConstants.SEPARATOR_SYMBOL</code></li>
 * <li>[Message Type] = <code>tz.co.rita.constantsBirthRegistrationConstants.APP</code></li>
 * <li>[Command] = Code identifying the task to perform</li>
 * <li>[Argument] = optional (empty)</li>
 * </ul>
 * <BR>
 * [Command] can be one of the following
 * <ul>
 * <li><code>BirthRegistrationConstants.APP_LOCK</code>: Server instruction to lock the app. Once this
 * message is received the application is locked and cannot be used on this device until the server
 * sends a <code>BirthRegistrationConstants.APP_LOCK</code> message.</li>
 * <li><code>BirthRegistrationConstants.APP_UN_LOCK</code>: Server instruction to unlock the app previously
 * locked by a <code>BirthRegistrationConstants.APP_LOCK</code> message.
 * </li>
 * <li><code>BirthRegistrationConstants.WIPE</code>: Server instruction to wipe all application data.
 * When this message is received, all the application database tables are removed and the application
 * status is changed to invalid.  The application cannot be used any longer, unless a fresh installation
 * is performed.
 * </ul>
 * <BR>
 * <BR>
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @see tz.co.rita.birthregistration.LoginActivity
 * @see tz.co.rita.constants.BirthRegistrationConstants
 * @see tz.co.rita.data.UserAdapter
 * @since June, 2015
 */
public class BirthRegistrationSmsReceiver extends BroadcastReceiver {

    private final String TAG = "BirthRegistrationSmsReceiver";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private Intent mIntent;
    private Context mContext;
    private SharedPreferences mSharedPreference;
    private BirthRegistrationApplication mAppInstance;

    @Override
    public void onReceive(Context context, Intent intent) {
        mIntent = intent;
        mContext = context;
        mAppInstance = BirthRegistrationApplication.getInstance();
        mSharedPreference = mContext.getSharedPreferences(
                BirthRegistrationConstants.PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        String action = intent.getAction();
        String address = "";
        String content = "";
        if (action.equals(ACTION_SMS_RECEIVED)) {
            SmsMessage[] msgs = getMessagesFromIntent(mIntent);
            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                    address = msgs[i].getOriginatingAddress();
                    content += msgs[i].getMessageBody().toString();
                    content += "\n";
                }
            }
        }


         if (address.equals(BirthRegistrationConstants.SMS_ADDRESS)) {

            processRemoteAction(content);
            String sdmmDup = "";
            if (intent.hasExtra(SDSmsConsts.SDSMS_DUPLICATE)) {
                sdmmDup = SDSmsConsts.SDSMS_DUPLICATE;
                if (!SDSmsConsts.SDSMS_DUPLICATE.equals(sdmmDup)) {

                    setResultCode(0);
                    setResultData(SDSmsConsts.SDSMS_ABORT);
                    intent.putExtra(SDSmsConsts.SDSMS_ABORT, true);

                }
            } else {
                abortBroadcast();

            }
        }

    }

    private void processRemoteAction(String content) {
        Log.i("Messagee Content", content);
        String[] msgElements;
        if (content.startsWith(BirthRegistrationConstants.FORM_RECEIVE_PREFIX)) {
            //Indicates record has been successfully received.
            msgElements = content.split(" ");
            String formNumber = msgElements[3];
            //BirthRecordModel.deleteBirthRecord(mContext.getContentResolver(), formNumber);
            BirthRecordModel.updateUploadStatusToDb(mContext.getContentResolver(), formNumber,
                    BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED);
            int currentUploadCount = this.mSharedPreference.getInt(BirthRegistrationConstants.UPLOAD_COUNT, 0);
            SharedPreferences.Editor editor = this.mSharedPreference.edit();
            editor.putInt(BirthRegistrationConstants.UPLOAD_COUNT, ++currentUploadCount);
            editor.commit();
            if (mAppInstance.isNotificationsEnabled())
                DataUploader.showNotification(formNumber, mContext, DataUploader.NOTIFICATION_TYPE_SUCCESS);
            return;
        }
        msgElements = content
                .split(BirthRegistrationConstants.SEPARATOR_SYMBOL);

        if (msgElements.length < 4) {
            //Invalid message length, nothing to do.
            return;
        }

        if (msgElements[0].equals(BirthRegistrationConstants.MBIRTH)) {
            if (msgElements[1].equals(BirthRegistrationConstants.USER)) {
                // SMS message is instruction about user accounts.
                UserAdapter ua = new UserAdapter(mContext);
                if (msgElements[2]
                        .equals(BirthRegistrationConstants.USER_VERIFY_ACCEPTED)) {
                    // SMS message to report the user verify request has been received by the server.

                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.USER_VERIFY_VERIFIED)) {
                    // SMS Message to change the status of  a user account to verified has
                    // been received.
                    Log.i("Messagee Command", msgElements[3]);
                    ua.unlockUser(msgElements[3]);
                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.USER_PASSWORD_RESET)) {
                    // SMS Message for enabling password resetting function received
                    SharedPreferences.Editor editor = this.mSharedPreference
                            .edit();
                    editor.putBoolean(
                            BirthRegistrationConstants.PASSWORD_RESET_ENABLED,
                            true);
                    editor.commit();
                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.USER_ACCT_DISABLE)) {
                    // SMS Message for disabling a user account has been received
                    ua.lockUser(msgElements[3]);
                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.USER_ACCT_ENABLE)) {
                    // SMS Message for enabling a user account has been received
                    ua.unlockUser(msgElements[3]);
                }

            } else if (msgElements[1].equals(BirthRegistrationConstants.APP)) {
                // SMS message is instruction about application state.
                if (msgElements[2].equals(BirthRegistrationConstants.APP_LOCK)) {
                    // SMS message instruction to lock application
                    BirthRegistrationApplication.app_locked = true;
                    SharedPreferences.Editor editor = this.mSharedPreference
                            .edit();
                    editor.putBoolean(BirthRegistrationConstants.APP_LOCKED,
                            true);
                    editor.commit();
                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.APP_UN_LOCK)) {
                    // SMS message instruction to unlock application
                    BirthRegistrationApplication.app_locked = false;
                    SharedPreferences.Editor editor = this.mSharedPreference
                            .edit();
                    editor.putBoolean(BirthRegistrationConstants.APP_LOCKED,
                            false);
                    editor.commit();

                } else if (msgElements[2]
                        .equals(BirthRegistrationConstants.APP_WIPE)) {

                    BirthRegistrationApplication.app_invalid = true;
                    SharedPreferences.Editor editor = this.mSharedPreference
                            .edit();
                    editor.putBoolean(BirthRegistrationConstants.APP_LOCKED,
                            true);
                    editor.commit();
                    BirthRegistrationProvider brp = new BirthRegistrationProvider();
                    brp.wipeData();
                    Uri packageURI = Uri.parse("package:" + LoginActivity.class.getPackage().getName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    mContext.startActivity(uninstallIntent);
                }

            }

        }
    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    protected void showNotification(int contactId, String message) {
        // Display notification...
    }
}