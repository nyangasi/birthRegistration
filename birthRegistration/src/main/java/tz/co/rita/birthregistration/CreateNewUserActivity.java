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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import tz.co.rita.data.UserAdapter;

public class CreateNewUserActivity extends Activity {
	UserAdapter mUserAdapter;

	public static final String APP_USER_COUNT = "app_users_count";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserAdapter = new UserAdapter((Context) this);
		setContentView(R.layout.activity_create_user);
		this.setTitle(getString(R.string.new_user_title));
	}

	@Override
	public void onResume(){
		super.onResume();
		if(BirthRegistrationApplication.app_invalid){
			setContentView(R.layout.app_disabled);			
			((TextView)findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_invalid_message));
			return;
		}
		if(BirthRegistrationApplication.app_locked){
			setContentView(R.layout.app_disabled);			
			((TextView)findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_locked_message));
			return;
		}
	}
	public void onCreateNewUserButtonClick(View v) {
		EditText editTextNewUsername = (EditText) findViewById(R.id.editTextNewUsername);
		EditText editTextNewUserFullName = (EditText) findViewById(R.id.editTextNewUserFullName);
		EditText editTextNewPassword = (EditText) findViewById(R.id.editTextNewPassword);
		EditText editTextConfirmNewPassword = (EditText) findViewById(R.id.editTextConfirmNewPassword);

		String newUsername = editTextNewUsername.getText().toString();
		String newUserFullName = editTextNewUserFullName.getText().toString();
		String newPassword = editTextNewPassword.getText().toString();
		String confirmPassword = editTextConfirmNewPassword.getText()
				.toString();

		String errorMessage = "";
		if (newUsername.equals("") || newUsername.equals(null)
				|| newPassword.equals("") || newPassword.equals(null)) {
			errorMessage += getString(R.string.error_empty_username_or_password);
		}
		if (mUserAdapter.userExists(newUsername)) {
			errorMessage += getString(R.string.error_user_exists);
		}
		if (!newPassword.equals(confirmPassword)) {
			errorMessage += getString(R.string.error_password_mismatch);
		}
		final Activity activity = this;

		if (errorMessage.equals("")) {
			mUserAdapter.addUser(newUsername, newUserFullName, newPassword) ;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.account_created_message)
					.setTitle(R.string.account_created_message_title)
					.setCancelable(false)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setInverseBackgroundForced(true)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(activity,
											LoginActivity.class);
									startActivity(intent);
									activity.finish();
									dialog.dismiss();
								}
							}).create().show();

		} else {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(errorMessage)
					.setTitle(R.string.error_title_account_create_failure)
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
			editTextNewPassword.setText("");
			editTextConfirmNewPassword.setText("");

		}

	}

}
