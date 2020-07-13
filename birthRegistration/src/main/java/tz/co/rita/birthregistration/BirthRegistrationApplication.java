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
package tz.co.rita.birthregistration;

import com.sdmmllc.superdupermm.SDSmsReceiveCallback;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.receivers.BirthRegistrationSmsReceiver;
/**
 * This is the main Application class that represents the common state of  the entire application.
 * As long as any part of the application is running, the application object will exist.  It provides
 * a number of static global functions that can be invoked from anywhere within the application to
 * fetch application wide constants and configuration options as well as various application state values.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 *
 *
 */

public class BirthRegistrationApplication extends Application {

    /**
     * A string regular expression constant that is used to check whether a given string fits the pattern
     * for a URL.
     */
    private final String URLRegex = "\\b(https?)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]";
    /**
     * A string regular expression constant that is used to check whether a given string fits the pattern
     * for a mobile phone number.
     */
    private final String SMSNumberRegEx = "\\d{3,14}";
    /**
     * A string constant that is used to identify this class for logging purpuses.
     */
    private static final String TAG = BirthRegistrationApplication.class.getSimpleName();

    /**
     * A reference to the singleton instance of this class.
     */
    private static BirthRegistrationApplication singleton;
    /**
     * A handle to the SharedPreferences for this application, used to set or retrieve application-wide
     * preference settings.
     */
    private SharedPreferences mSharedPreference;
    /**
     * A boolean static member that indicates whether the application has been locked (prevented
     * from running by the system administrator.)  This state can only be changed through an authorised
     * message from the server either using SMS or Internet connectivity. <BR>
     *     The application can be made valid again through administrative command messages.
     *     The data will remain intact and operation would resume once the setting has been updated
     *     to valid again.  This is intended to ensure that a suspect rogue application is prevented
     *     from running and creating and uploading records.  The action can be reversed.
     */
    public static boolean app_locked;
    /**
     * A boolean static member that indicates whether the application has been permanently prevented
     * from running by the system administrator.  This state can only be changed through an authorised
     * message from the server either using SMS or Internet connectivity. <BR>
     *     When an administrator sends the command to make this application invalid, currently existing
     *     data is wiped-out and cannot be recovered. The application must also be reinstalled before
     *     it can be used again.  This state is intended to disable the application permanently and
     *     wipe any data currently stored in its internal database, in cases where the phone running
     *     the app is permanently compromised and the administrator wants to prevent it from reading,
     *     creating or in anyway accessing the application, its data or any of its features permanently.
     */
    public static boolean app_invalid;

    /**
     * A boolean member that indicates whether the application has been configured to display notifications.
     */
    private boolean isNotificationsEnabled;
    /**
     * A boolean member that indicates whether the uploading of records using SMS has been enabled.
     */
    private boolean isSMSUploadEnabled = true;
    /**
     * A boolean member that indicates whether the uploading of records using Internet has been enabled.
     */
    private boolean isURLUploadEnabled =  false;
    /**
     * holds the string representation of the number used for SMS uploading.
     */
    private String mUploadSMSNumber = BirthRegistrationConstants.SMS_ADDRESS;
    /**
     * holds the string representation of the URL used for Internet uploading.
     */
    private String mUploadURL = BirthRegistrationConstants.UPLOAD_URL;
    /**
     * holds the starting form number to be used for the validation of data entered by the users. <BR>
     *     This value is entered by the user in the settings menu of the application.
     */
    private long mStartingFormNumber;
    /**
     * holds the ending form number to be used for the validation of data entered by the users. <BR>
     *     This value is entered by the user in the settings menu of the application.
     */
    private long mEndingFormNumber;
    /**
     * holds the registration center's unique identification number to be used for the validation
     * of data entered by the users. <BR>
     *     This value is entered by the user in the settings menu of the application.
     */


    private long mStartingFormNumberDeath;
    /**
     * holds the ending form number to be used for the validation of data entered by the users. <BR>
     *     This value is entered by the user in the settings menu of the application.
     */
    private long mEndingFormNumberDeath;
    /**
     * holds the registration center's unique identification number to be used for the validation
     * of data entered by the users. <BR>
     *     This value is entered by the user in the settings menu of the application.
     */
    private String mRegistrationCenterNumber;


    private boolean initialRun;


    /**
     * Get an instance of the application object.
     * @return the singleton instance of this application.
     */
    public static BirthRegistrationApplication getInstance() {
        return singleton;
    }

    /**
     * Android life-cycle method called when the application is starting. <BR>
     *     Sets up the singleton instance variable, gets a handle of the sharedpreferences
     *     Updates all preference member fields by reading their current value in the
     *     preferences list and initializes the SDSMS Manager and assigns callback variable
     *     to the SDSMS managers receiver callback method.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        updatePreferences();
        app_locked = mSharedPreference.getBoolean(BirthRegistrationConstants.APP_LOCKED, false);
        app_invalid = mSharedPreference.getBoolean(BirthRegistrationConstants.APP_INVALID, false);
        initialRun = mSharedPreference.getBoolean(BirthRegistrationConstants.INITIAL_RUN, true);

        SDSmsManager.initialize(this);
        SDSmsManager.getDefault().setReceiverCallback(mCallback);
    }

    /**
     * Callback variable that provides the actions to perform when an SMS is received by
     * the SDSMS manager intended for this application.
     */
    private SDSmsReceiveCallback mCallback = new SDSmsReceiveCallback() {
        @Override
        public boolean onReceive(Intent intent) throws RemoteException {
            BirthRegistrationSmsReceiver receiver = new BirthRegistrationSmsReceiver();
            receiver.onReceive(SDSmsManager.getContext(), intent);
            boolean aborted = false;
            if (intent.hasExtra(SDSmsConsts.SDSMS_ABORT))
                aborted = true;
            return aborted;
        }

        @Override
        public String testCallbackConnection(String text) {
            return "Connection successful for " + TAG;
        }

        @Override
        public boolean reload() {
            return false;
        }
    };

    /**
     * Getter method for the maximum allowed age of the registrant at registration time.
     * This value should be set in the shared prefence of the application,  If not the default
     * value assigned in {@link BirthRegistrationConstants#DEFAULT_MAX_ALLOWED_REGISTRANT_AGE} is used.
     * @return the maximum allowed age of the registrant in years
     */
    public int getMaxAllowedRegistrantAge(){
        return mSharedPreference.getInt(BirthRegistrationConstants.MAX_ALLOWED_REGISTRANT_AGE,
                BirthRegistrationConstants.DEFAULT_MAX_ALLOWED_REGISTRANT_AGE);
    }

    /**
     * Getter method for {@link BirthRegistrationApplication#isURLUploadEnabled}
     * @return {@link BirthRegistrationApplication#isURLUploadEnabled}
     */
    public boolean isURLUploadEnabled(){
        return isURLUploadEnabled;
    }
    /**
     * Getter method for {@link BirthRegistrationApplication#isNotificationsEnabled}
     * @return {@link BirthRegistrationApplication#isNotificationsEnabled}
     */
    public boolean isNotificationsEnabled(){
        return isNotificationsEnabled;
    }

    /**
     * Getter method for {@link BirthRegistrationApplication#isSMSUploadEnabled}
     * @return {@link BirthRegistrationApplication#isSMSUploadEnabled}
     */
    public boolean isSMSUploadEnabled(){
        return isSMSUploadEnabled;
    }

    /**
     * Getter method for {@link BirthRegistrationApplication#mUploadSMSNumber}
     * @return {@link BirthRegistrationApplication#mUploadSMSNumber}
     */
    public String getUploadSMSNumber() {
        // Toast.makeText(this,"SMS No = " + mUploadSMSNumber, Toast.LENGTH_SHORT).show();
        return mUploadSMSNumber;
    }

    /**
     * Getter method for {@link BirthRegistrationApplication#mUploadURL}
     * @return {@link BirthRegistrationApplication#mUploadURL}
     */
    public String getUploadURL() {
        // Toast.makeText(this,"SMS No = " + mUploadSMSNumber, Toast.LENGTH_SHORT).show();
        return mUploadURL;
    }

    /**
     * Getter method for {@link BirthRegistrationApplication#mRegistrationCenterNumber}
     * @return {@link BirthRegistrationApplication#mRegistrationCenterNumber}
     */
    public String getRegistrationCenterCode(){
        return mRegistrationCenterNumber;
    }

    /**
     * Gets all preference values from the shared preferences and stores them into the local variables.
     * Called from {@link BirthRegistrationApplication#onCreate()}
     */
    public void updatePreferences() {
        try {
            mStartingFormNumber = Long.valueOf(mSharedPreference.getString(SettingsActivity.KEY_STARTING_FORM_NUMBER, "0"));
            mEndingFormNumber = Long.valueOf(mSharedPreference.getString(SettingsActivity.KEY_ENDING_FORM_NUMBER, "0"));

            mStartingFormNumberDeath = Long.valueOf(mSharedPreference.getString(SettingsActivity.KEY_DEATH_STARTING_FORM_NUMBER, "0"));
            mEndingFormNumberDeath = Long.valueOf(mSharedPreference.getString(SettingsActivity.KEY_DEATH_ENDING_FORM_NUMBER, "0"));
        } catch(Exception e){
            mStartingFormNumber = Long.valueOf("0");
            mEndingFormNumber = Long.valueOf("0");

            mStartingFormNumberDeath = Long.valueOf("0");
            mEndingFormNumberDeath = Long.valueOf("0");
        }
       // mUploadSMSNumber = mSharedPreference.getString(SettingsActivity.KEY_UPLOAD_SMS_NUMBER, "");
       // mUploadURL = mSharedPreference.getString(SettingsActivity.KEY_UPLOAD_URL, "");
        mRegistrationCenterNumber = mSharedPreference.getString(SettingsActivity.KEY_REGISTRATION_CENTER_NUMBER, "");
       // isSMSUploadEnabled = mSharedPreference.getBoolean(SettingsActivity.KEY_ENABLE_SMS_UPLOAD, false);
        isURLUploadEnabled = mSharedPreference.getBoolean(SettingsActivity.KEY_ENABLE_URL_UPLOAD, false);
        isNotificationsEnabled = mSharedPreference.getBoolean(SettingsActivity.KEY_ENABLE_NOTIFICATIONS, true);
    }

    /**
     * Checks if the upload settings have been assigned a valid value.
     * @return true if either one or both of the settings for upload (URL and SMS) are set and are vlid.
     */
    public boolean isUploadURLOrNumberSet() {
        if (!isSMSUploadEnabled && !isURLUploadEnabled) return false;
        if (isSMSUploadEnabled && !isURLUploadEnabled) return isValidSMSNumber();
        if (!isSMSUploadEnabled && isURLUploadEnabled) return isValidURL();
        if (isSMSUploadEnabled && isURLUploadEnabled)
            return (isValidSMSNumber() && isValidURL());
        return false;
    }

    /**
     * Checks if the upload SMS number is valid.
     * @return true if upload SMS number is valid, false otherwise.
     */
    public boolean isValidSMSNumber() {
        return isMatch(mUploadSMSNumber, SMSNumberRegEx);
    }

    /**
     * Checks if the upload URL is valid.
     * @return true if upload URL is valid, false otherwise.
     */
    public boolean isValidURL() {
        return isMatch(mUploadURL, URLRegex);
    }

    /**
     * Checks if a string matches a pattern.
     * @param s the string to be tested.
     * @param pattern the pattern to be applied.
     * @return true if string matches pattern, false otherwise.
     */

    private boolean isMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Getter for the starting form number {@link BirthRegistrationApplication#mStartingFormNumber}
     * @return {@link BirthRegistrationApplication#mStartingFormNumber}
     */
    public long getStartingDeathFormNumber(){
        return mStartingFormNumberDeath;
    }
    /**
     * Getter for the starting form number {@link BirthRegistrationApplication#mEndingFormNumber}
     * @return {@link BirthRegistrationApplication#mEndingFormNumber}
     */
    public long getEndingDeathFormNumber(){
        return mEndingFormNumberDeath;
    }


    public long getStartingFormNumber(){
        return mStartingFormNumber;
    }
    /**
     * Getter for the starting form number {@link BirthRegistrationApplication#mEndingFormNumber}
     * @return {@link BirthRegistrationApplication#mEndingFormNumber}
     */
    public long getEndingFormNumber(){
        return mEndingFormNumber;
    }

    public void setInitialRunToFalse(){
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(BirthRegistrationConstants.INITIAL_RUN, false);
        editor.commit();
        initialRun = false;
    }

    public boolean isInitialRun(){
        return initialRun;
    }
}