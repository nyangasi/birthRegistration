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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import tz.co.rita.data.UserAdapter;

public class ChangePasswordActivity extends Activity {
	UserAdapter mUserAdapter;

	public static final String APP_USER_COUNT = "app_users_count";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserAdapter = new UserAdapter((Context) this);
		setContentView(R.layout.activity_change_password);
		this.setTitle(getString(R.string.change_password));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (BirthRegistrationApplication.app_invalid) {
			setContentView(R.layout.app_disabled);
			((TextView) findViewById(R.id.textViewAppInvalid))
					.setText(getString(R.string.app_invalid_message));
			return;
		}
		if (BirthRegistrationApplication.app_locked) {
			setContentView(R.layout.app_disabled);
			((TextView) findViewById(R.id.textViewAppInvalid))
					.setText(getString(R.string.app_locked_message));
			return;
		}
	}

	public void onChangePasswordButtonClick(View v) {
		EditText editTextUsername = (EditText) findViewById(R.id.editTextUsername);
		EditText editTextCurrentPassword = (EditText) findViewById(R.id.editTextCurrentPassword);
		EditText editTextNewPassword = (EditText) findViewById(R.id.editTextNewPassword);
		EditText editTextConfirmNewPassword = (EditText) findViewById(R.id.editTextConfirmNewPassword);

		String username = editTextUsername.getText().toString();
		String currentPassword = editTextCurrentPassword.getText().toString();
		String newPassword = editTextNewPassword.getText().toString();
		String confirmPassword = editTextConfirmNewPassword.getText()
				.toString();

		if (username.equals("") || username.equals("")
				|| currentPassword.equals("") || currentPassword.equals("")) {
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
		}
		if (mUserAdapter.authenticate(username, currentPassword)) {
			String errorMessage = "";
			if (newPassword.equals("") || newPassword.equals(null)) {
				errorMessage += getString(R.string.error_empty_username_or_password);
			}

			if (!newPassword.equals(confirmPassword)) {
				errorMessage += getString(R.string.error_password_mismatch);
			}

			// *****************************************
			// ************************************(******

			// ***********************************************

			if (errorMessage.equals("")) {
				mUserAdapter.updateUserPassword(username, newPassword);
				Intent intent = new Intent(this, DashBoardActivity.class);
				startActivity(intent);
				finish();
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

}
