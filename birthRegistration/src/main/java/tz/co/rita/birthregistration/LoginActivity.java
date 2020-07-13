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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.UserAdapter;
import tz.co.rita.services.PhoneStateChangeListenerService;
import tz.co.rita.uploadverification.VerifyUpload;

/**
 * Activity class for performing user authentication.  <BR>
 * Presents the login interface (loading the activity_login layout file.)  Allows the user to
 * enter a username and password and preform authentication.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 */
public class LoginActivity extends Activity {
    /**
     * an instance of the {@link UserAdapter} class to perform user related database actions.
     */
    private UserAdapter mUserAdapter;
    /**
     * A handle to the SharedPreferences for this application, used to set or retrieve application-wide
     * preference settings.
     */
    private SharedPreferences mSharedPreference;


    /**
     * Android life-cycle method called when the activity is created. <BR>
     * Sets up the singleton instance variable, starts the {@link PhoneStateChangeListenerService}
     * service, initializes private variables, displays the activity_login layout and changes
     * the user interface visibility state depending on the application state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, PhoneStateChangeListenerService.class));
        mUserAdapter = new UserAdapter((Context) this);
        mSharedPreference = this.getSharedPreferences(
                BirthRegistrationConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_login);
        if (mSharedPreference.getBoolean(BirthRegistrationConstants.PASSWORD_RESET_ENABLED, false))
            findViewById(R.id.buttonResetPassword).setVisibility(View.VISIBLE);
        else findViewById(R.id.buttonResetPassword).setVisibility(View.GONE);
        if (SDSmsManager.hasKitKat()) {
            if (!SDSmsManager.isSdsmsIntstalled()) {
              //  SDSmsManager.notifySdmmRequired(this);
            } else if (!SDSmsManager.isSdsmsDefaultSmsApp()) {
                SDSmsManager.notifySetDefaultSms(this);
            }
        }

        this.setTitle(this.getString(R.string.app_name));
        if (mUserAdapter.getCount() == 0) {
            //if no user account exists, display the  new user creation dialog.
            final Activity activity = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.first_use_message)
                    .setTitle(R.string.first_use_title)
                    .setCancelable(true)
                    .setInverseBackgroundForced(true)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    dialog.dismiss();
                                    Intent intent = new Intent(activity,
                                            CreateNewUserActivity.class);
                                    startActivity(intent);
                                }
                            }).create().show();

            // finish();
        }
    }


    /**
     * Android life-cycle method called when the activity is resuming, which happens once the activity
     * is created, started and the user interface has been displayed. Checks if the application is
     * either locked or invalid and loads the corresponding layout that displays an error message and
     * prevents the user from using the application further.  Does nothing if the application is not
     * in either one of these states.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (BirthRegistrationApplication.app_invalid) {
            setContentView(R.layout.app_disabled);
            ((TextView) findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_invalid_message));
            return;
        } else if (BirthRegistrationApplication.app_locked) {
            setContentView(R.layout.app_disabled);
            ((TextView) findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_locked_message));
            return;
        }
    }

    /**
     * Respond to the login button click event. <br/>
     *
     * @param v the View.
     */
    public void onLoginClick(View v) {
        if (BirthRegistrationApplication.getInstance().isInitialRun()) {
            VerifyUpload vu = new VerifyUpload(this);
            vu.FixUploadStatus();
            BirthRegistrationApplication.getInstance().setInitialRunToFalse();
        }

        //TESTING
      //Intent intent_temp = new Intent(this, DashBoardActivity.class);
      //startActivity(intent_temp);
      ///finish();


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.HIDE_NOT_ALWAYS);
        String username = ((EditText) findViewById(R.id.editTextUsername))
                .getText().toString();
        String password = ((EditText) findViewById(R.id.editTextPassword))
                .getText().toString();

        if (username.equals("") || username.equals(null) || password.equals("")
                || password.equals(null)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_message_login_failure)
                    .setTitle(R.string.error_title_login_failure)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setInverseBackgroundForced(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
            ((EditText) findViewById(R.id.editTextPassword)).setText("");
            return;
        } else if (mUserAdapter.isUserLocked(username)) {
            setContentView(R.layout.app_disabled);
            ((TextView) findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.account_disabled_message));
            return;

        } else if (mUserAdapter.authenticate(username, password)) {

            if (!BirthRegistrationApplication.getInstance().isUploadURLOrNumberSet()) {
                showUploadAddressWarning(this);
            } else {
                Intent intent = new Intent(this, DashBoardActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_message_login_failure)
                    .setTitle(R.string.error_title_login_failure)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setInverseBackgroundForced(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
            ((EditText) findViewById(R.id.editTextPassword)).setText("");
        }


    }

    /**
     * Respond to the create user account button click event. <br/>
     *
     * @param v the View.
     */
    public void onCreateUserAccountButtonClicked(View v) {
        Intent intent = new Intent(this, CreateNewUserActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Respond to the forgot password button click event. <br/>
     *
     * @param v the View.
     */
    public void onForgotPasswordButtonClicked(View v) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Respond to the reset password button click event. <br/>
     *
     * @param v the View.
     */
    public void onResetPasswordtButtonClicked(View v) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Display a warning message if the application has incorrect upload settings. <br/>
     *
     * @param activity the activity instance to be used for context.
     */
    private void showUploadAddressWarning(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_upload_settings_failure)
                .setTitle(R.string.error_title_upload_settings_failure)
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setInverseBackgroundForced(true)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.dismiss();
                                TaskStackBuilder.create(activity)
                                        .addNextIntentWithParentStack(new Intent(activity, SettingsActivity.class))
                                        .startActivities();

                                activity.finish();
                            }
                        }).create().show();
    }


}
