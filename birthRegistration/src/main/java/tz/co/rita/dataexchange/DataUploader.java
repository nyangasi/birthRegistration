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
package tz.co.rita.dataexchange;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import tz.co.rita.birthregistration.BirthRegistrationApplication;
import tz.co.rita.birthregistration.ChartActivity;
import tz.co.rita.birthregistration.DashBoardActivity;
import tz.co.rita.birthregistration.R;
import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.BirthRecordModel;
import tz.co.rita.data.DeathRecordModel;

/**
 * Created by Molalgne Girmaw on 12/21/2015.
 * molbill@gmail.com
 */
public class DataUploader {

    private Context mContext;
    private BirthRegistrationApplication mAppInstance;

    public static final int NOTIFICATION_TYPE_SUCCESS = 0;
    public static final int NOTIFICATION_TYPE_FAILURE = 1;

    public DataUploader(Context context) {

        mContext = context;
        mAppInstance = BirthRegistrationApplication.getInstance();
    }

    public boolean Upload(BirthRecordModel birthRecord) {
        if (birthRecord != null) {

            if (mAppInstance.isURLUploadEnabled() && mAppInstance.isValidURL()) {

                ConnectivityManager connMgr = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // upload data using Internet
                    String content ="";
                    try {
                        content = birthRecord.getRecordUploadString(mContext, false);
                    }catch(Exception e){
                        return false;
                    }
                    Log.d("Internet Upload", "Started");

                    Log.d("Uploader Message Size", content.length() + "Bytes");
                    UploadToWebAddress(mAppInstance.getUploadURL(),
                            content, mContext);
                    birthRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                    birthRecord.updateUploadStatusToDb(mContext.getContentResolver());
                    return true;
                } else if (mAppInstance.isSMSUploadEnabled() && mAppInstance.isValidSMSNumber()) {
                    // upload data using SMS
                    String content ="";
                    try {
                        content = birthRecord.getRecordUploadString(mContext, false);
                    }catch(Exception e){
                        return false;
                    }

                    Log.d("Uploader Message Size", content.length() + "Bytes");
                    UploadToSMS(mAppInstance.getUploadSMSNumber(),
                            content);
                    birthRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                    birthRecord.updateUploadStatusToDb(mContext.getContentResolver());
                    return true;
                }
            } else if (mAppInstance.isSMSUploadEnabled() && mAppInstance.isValidSMSNumber()) {

                String content ="";
                try {
                    content = birthRecord.getRecordUploadString(mContext, false);
                }catch(Exception e){
                    return false;
                }
                UploadToSMS(mAppInstance.getUploadSMSNumber(),
                        content);
                Log.d("Uploader Message Size", content.length() + "Bytes");
                birthRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                birthRecord.updateUploadStatusToDb(mContext.getContentResolver());
                return true;
            } else {
                if (mAppInstance.isNotificationsEnabled())
                    DataUploader.showNotification(birthRecord.getFormNumber(), mContext, DataUploader.NOTIFICATION_TYPE_FAILURE);
            }
        }

        return false;
    }

    public void UploadToSMS(String smsAddress, String content) {
        content = BirthRegistrationConstants.MBIRH_V2_PREFIX + " " + content;
        //   ApiCrypter apiCrypter = new ApiCrypter();
        //   String encryptedContent = ApiCrypter.bytesToHex(apiCrypter.encrypt(content));
        //   String urlWithData = url + "?message=" + encryptedContent;
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(content);
        sms.sendMultipartTextMessage(smsAddress, null, parts, null, null);
    }


    public void UploadToSMSDeath(String smsAddress, String content) {
        //content = BirthRegistrationConstants.MBIRH_V2_PREFIX + " " + content;
        //   ApiCrypter apiCrypter = new ApiCrypter();
        //   String encryptedContent = ApiCrypter.bytesToHex(apiCrypter.encrypt(content));
        //   String urlWithData = url + "?message=" + encryptedContent;
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(content);
        sms.sendMultipartTextMessage(smsAddress, null, parts, null, null);
    }

    public void UploadToWebAddress(String url, String content, Context context) {
        content = BirthRegistrationConstants.MBIRH_V2_PREFIX + " " + content;
        try {
            //   ApiCrypter apiCrypter = new ApiCrypter();
            //   String encryptedContent = ApiCrypter.bytesToHex(apiCrypter.encrypt(content));
            //   String urlWithData = url + "?message=" + encryptedContent;
            new UploadToWebAddressTask().execute(url, content);
        } catch (Exception e) {

        }


    }

    private class UploadToWebAddressTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return uploadToUrl(data[0], data[1]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            String[] msgElements;
            if (result == null) result = "";
            if (result.startsWith(BirthRegistrationConstants.FORM_RECEIVE_PREFIX)) {
                //Indicates record has been successfully received.
                msgElements = result.split(" ");
                String formNumber = msgElements[3];
                BirthRecordModel.updateUploadStatusToDb(mContext.getContentResolver(), formNumber,
                        BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED);
                SharedPreferences sharedPreference = mContext.getSharedPreferences(
                        BirthRegistrationConstants.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
                int currentUploadCount = sharedPreference.getInt(BirthRegistrationConstants.UPLOAD_COUNT, 0);
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putInt(BirthRegistrationConstants.UPLOAD_COUNT, ++currentUploadCount);
                editor.commit();
                if (mAppInstance.isNotificationsEnabled())
                    DataUploader.showNotification(formNumber, mContext, DataUploader.NOTIFICATION_TYPE_SUCCESS);
            }
        }
    }

    public static void showNotification(String formNumber, Context context, int notificationType) {
        String notificationMessage = "";
        if (notificationType == DataUploader.NOTIFICATION_TYPE_SUCCESS) {
            notificationMessage = "Record with Form Number "
                    + formNumber + " has been successfully uploaded.";
        } else if (notificationType == DataUploader.NOTIFICATION_TYPE_FAILURE) {
            notificationMessage = "The app was unable to upload record with Form Number "
                    + formNumber + ". Please check your upload settings.";
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(notificationMessage);
        Intent resultIntent = new Intent(context, ChartActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DashBoardActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = formNumber.length() <= 4 ?
                Integer.parseInt(formNumber) :
                Integer.parseInt(formNumber.substring(formNumber.length() - 5, formNumber.length() - 1));
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    private String uploadToUrl(String uploadURL, String content) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("message");
        sb.append("=");
        sb.append(content);
        Log.d("EncMes", sb.toString());

        InputStream is = null;
        URL url = new URL(uploadURL);
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream outputPost =conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            conn.connect();

          //  int response = conn.getResponseCode();
            Log.d("Post Result", "Result = " + conn.getResponseMessage());
            //    is = conn.getInputStream();
            // Convert the InputStream into a string
            //    String contentAsString = readIt(is, len);

            //   Log.d("POst", contentAsString);
            return "contentAsString";

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            Log.d("Upload Error", e.getMessage());
            return null;
        } finally {

            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    public boolean UploadDeath(DeathRecordModel deathRecord) {
        if (deathRecord != null) {

            if (mAppInstance.isURLUploadEnabled() && mAppInstance.isValidURL()) {

                ConnectivityManager connMgr = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // upload data using Internet
                    String content ="";
                    try {
                        content = deathRecord.getRecordUploadString(mContext, false);
                    }catch(Exception e){
                        return false;
                    }
                    Log.d("Internet Upload", "Started");

                    Log.d("Uploader Message Size", content.length() + "Bytes");
                    Log.d("Uploader Message Size", content + "");
                    UploadToWebAddress(mAppInstance.getUploadURL(),
                            content, mContext);
                    deathRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                    deathRecord.updateUploadStatusToDb(mContext.getContentResolver());
                    return true;
                } else if (mAppInstance.isSMSUploadEnabled() && mAppInstance.isValidSMSNumber()) {
                    // upload data using SMS
                    String content ="";
                    try {
                        content = deathRecord.getRecordUploadString(mContext, false);
                    }catch(Exception e){
                        return false;
                    }

                    Log.d("Uploader Message Size", content.length() + "Bytes");
                    Log.d("Uploader Message", content+ "");
                    UploadToSMSDeath(mAppInstance.getUploadSMSNumber(),
                            content);
                    deathRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                    deathRecord.updateUploadStatusToDb(mContext.getContentResolver());
                    return true;
                }
            } else if (mAppInstance.isSMSUploadEnabled() && mAppInstance.isValidSMSNumber()) {

                String content ="";
                try {
                    content = deathRecord.getRecordUploadString(mContext, false);
                }catch(Exception e){
                    return false;
                }
                UploadToSMSDeath(mAppInstance.getUploadSMSNumber(),
                        content);
                Log.d("Uploader Message Size", content.length() + "Bytes");
                Log.d("Uploader Message ", content + "");
                deathRecord.setUploadStatus(BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
                deathRecord.updateUploadStatusToDb(mContext.getContentResolver());
                return true;
            } else {
                if (mAppInstance.isNotificationsEnabled())
                    DataUploader.showNotification(deathRecord.getFormNumber(), mContext, DataUploader.NOTIFICATION_TYPE_FAILURE);
            }
        }

        return false;
    }

}