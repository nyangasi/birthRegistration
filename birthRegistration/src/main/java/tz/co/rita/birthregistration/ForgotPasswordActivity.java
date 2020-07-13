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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import tz.co.rita.constants.BirthRegistrationConstants;

public class ForgotPasswordActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_forgot_password);
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
	public void onRequestPasswordResetButtonClick(View v) {

		String userName = ((EditText) findViewById(R.id.editTextUsername))
				.getText().toString();
		String userFullName = ((EditText) findViewById(R.id.editTextFullName))
				.getText().toString();
		String phoneNumber = ((EditText) findViewById(R.id.editTextPhoneNumber))
				.getText().toString();

		String message = BirthRegistrationConstants.PASSWORD_RESET_REQUEST
				+ BirthRegistrationConstants.SEPARATOR_SYMBOL + userName
				+ BirthRegistrationConstants.SEPARATOR_SYMBOL
				+ BirthRegistrationConstants.SEPARATOR_SYMBOL + userFullName
				+ BirthRegistrationConstants.SEPARATOR_SYMBOL + phoneNumber
				+ BirthRegistrationConstants.SEPARATOR_SYMBOL;

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(BirthRegistrationConstants.SMS_ADDRESS, null,
				message, null, null);
		
		final Activity activity = this;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.password_reset_message_sent_message)
				.setTitle(R.string.password_reset_message_sent_title)
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setInverseBackgroundForced(true)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								Intent intent = new Intent(activity, LoginActivity.class);
								startActivity(intent);
								activity.finish();
								dialog.dismiss();
							}
						}).create().show();
	}
}
