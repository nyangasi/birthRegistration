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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by mgirmaw on 1/23/2016.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_ENABLE_NOTIFICATIONS = "pref_key_show_notifications";
    public static String KEY_UPLOAD_SMS_NUMBER = "pref_key_sms_upload_number";
    public static String KEY_UPLOAD_URL = "pref_key_upload_url";
    public static String KEY_ENABLE_SMS_UPLOAD = "pref_key_enable_sms_upload";
    public static String KEY_ENABLE_URL_UPLOAD = "pref_key_enable_internet_upload";
    public static String KEY_STARTING_FORM_NUMBER = "pref_key_starting_form_number";
    public static String KEY_ENDING_FORM_NUMBER = "pref_key_ending_form_number";
    public static String KEY_REGISTRATION_CENTER_NUMBER = "pref_key_registration_center_number";

    public static String KEY_DEATH_STARTING_FORM_NUMBER = "pref_key_death_starting_form_number";
    public static String KEY_DEATH_ENDING_FORM_NUMBER = "pref_key_death_ending_form_number";


    private BirthRegistrationApplication mAppInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppInstance = BirthRegistrationApplication.getInstance();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        mAppInstance.updatePreferences();
        if(!mAppInstance.isUploadURLOrNumberSet()){
            showUploadAddressWarning();
        }

    }

    private void showUploadAddressWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_upload_settings_failure)
                .setTitle(R.string.error_title_upload_settings_failure)
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setInverseBackgroundForced(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.dismiss();
                            }
                        }).create().show();
    }

}