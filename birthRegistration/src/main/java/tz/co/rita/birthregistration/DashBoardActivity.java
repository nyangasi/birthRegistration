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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import tz.co.rita.data.UserAdapter;

public class DashBoardActivity extends Activity {

	public static final String RECORD_VIEW_MODE = "recordViewMode";
	public static final int RECORD_VIEW_MODE_NEW = 0;
	public static final int RECORD_VIEW_MODE_EDIT = 1;
	public static final int RECORD_VIEW_MODE_VIEW = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_dash_board);
		((TextView) findViewById(R.id.TextViewWelcomeUser))
				.setText(getString(R.string.welcome) + " "
						+ UserAdapter.currentUser);

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
	public void onNewRecordButtonClicked(View v) {

		Intent intent = new Intent(this, BirthRecordActivity.class);
		//intent.putExtra(RECORD_VIEW_MODE, RECORD_VIEW_MODE_NEW);
		startActivity(intent);
	}

	public void onListRecordsButtonClicked(View v) {
		Intent intent = new Intent(this, BirthRecordsListActivity.class);
		startActivity(intent);
	}

	public void onChangePasswordButtonClicked(View v) {
		Intent intent = new Intent(this, ChangePasswordActivity.class);
		startActivity(intent);
	}

	public void onLogoutButtonClicked(View v) {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	public void onChartsButtonClicked(View v) {
		Intent intent = new Intent(this, ChartActivity.class);
		startActivity(intent);
	}
	public void onSettingsButtonClicked(View v) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void onButtonDeathRegisterClicked(View v)
	{
		Intent intent = new Intent(this, DeathRegisterActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.log_out:
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.add_user:
			intent = new Intent(this, CreateNewUserActivity.class);
			startActivity(intent);
			return true;
		case R.id.change_password:
			intent = new Intent(this, ChangePasswordActivity.class);
			startActivity(intent);
		default:
			return true;
		}
	}

}
