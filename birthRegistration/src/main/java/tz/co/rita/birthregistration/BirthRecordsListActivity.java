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

import java.util.Set;

import tz.co.rita.adapters.MultiSelectListAdapter;
import tz.co.rita.data.BirthRecordModel;
import tz.co.rita.data.Contracts;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BirthRecordsListActivity extends Activity implements
		LoaderManager.LoaderCallbacks<Cursor>,
		MultiSelectListAdapter.MultiSelectListAdapterCallBacks {

	// This is the Adapter being used to display the list's data
	MultiSelectListAdapter mAdapter;
	ListView mListView;
	LoaderManager mLoaderManager;
	BirthRecordsListActivity mBirthRecordsListActivity = this;
	ContentResolver mResolver;

	// These are the Contacts rows that we will retrieve
	static final String[] PROJECTION = new String[] {
			Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID,
			Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER,
			Contracts.BirthRecord.COLUMN_NAME_CHILD_FIRST_NAME,
			Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_DATE,
			Contracts.BirthRecord.COLUMN_NAME_CHILD_RESIDENCE_POST_CODE };

	@Override
	public void onBirthRecordItemClick(String recordId) {
		Intent intent = new Intent(this, BirthRecordActivity.class);
		intent.putExtra(DashBoardActivity.RECORD_VIEW_MODE,
				DashBoardActivity.RECORD_VIEW_MODE_EDIT);
		intent.putExtra(Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID,
				recordId);
		startActivity(intent);
	}

	@Override
	public void onListItemSelectionChanged(int selectionCount, int itemCount,
			boolean allItemsSelected) {
		((CheckBox) findViewById(R.id.checkBoxSelectAll))
				.setChecked(allItemsSelected);
		((TextView) findViewById(R.id.textViewSelectionMessage)).setText(String
				.format(getString(R.string.selection_message), selectionCount,
						itemCount));

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_birth_records_list);

		mLoaderManager = getLoaderManager();

		mResolver = getContentResolver();
		// TableObserver observer = new TableObserver(null);
		// mResolver.registerContentObserver(Contracts.BirthRecord.CONTENT_URI,
		// true, observer);

		mListView = (ListView) findViewById(R.id.listViewStoredRecords);

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = { Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER,
				Contracts.BirthRecord.COLUMN_NAME_CHILD_FIRST_NAME,
				Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_DATE,
				Contracts.BirthRecord.COLUMN_NAME_CHILD_RESIDENCE_POST_CODE,
				Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID };
		int[] toViews = { R.id.textViewFormNumberHolder,
				R.id.textViewBabyFirstNameHolder,
				R.id.textViewBabyBirthDateHolder,
				R.id.textViewBabyResidencePostCodeHolder,
				R.id.textViewRecordIdHolder };
		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new MultiSelectListAdapter(this, this,
				R.layout.birth_record_list_element, null, fromColumns, toViews,
				0);
		mListView.setAdapter(mAdapter);

		CheckBox checkBoxSelectAll = (CheckBox) findViewById(R.id.checkBoxSelectAll);
		checkBoxSelectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				boolean isChecked = cb.isChecked();
				if (isChecked) {
					mAdapter.selectAll();
				} else {
					mAdapter.clearSelection();
				}
				for (int i = 0; i < mListView.getChildCount(); i++) {
					View rowContainer = mListView.getChildAt(i);
					((CheckBox) rowContainer.findViewById(i))
							.setChecked(isChecked);
				}
			}
		});
		mLoaderManager.initLoader(0, null, this);
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

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Contracts.BirthRecord.CONTENT_URI,
				PROJECTION, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		((TextView) findViewById(R.id.textViewSelectionMessage))
				.setText(R.string.select_all);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	public void onUploadSelectedItemsButtonClick(View v) {
		int itemCount = mAdapter.getCount();
		int selectedItemCount = mAdapter.getSelectionCount();

		BirthRecordModel brm;
		Set<Integer> selection = mAdapter.getCurrentCheckedPosition();
		for (Integer item : selection) {
			brm = new BirthRecordModel(mAdapter.getBirthRecordId(mListView
					.getChildAt(item)), mResolver);
			brm.upload(this, mResolver);
		}

		Toast.makeText(
				this,
				String.format(getString(R.string.upload_message),
						selectedItemCount, itemCount), Toast.LENGTH_LONG)
				.show();

		mLoaderManager.restartLoader(0, null, this);

	}

	public void onDeleteSelectedItemsButtonClick(View v) {

		int itemCount = mAdapter.getCount();
		int selectedItemCount = mAdapter.getSelectionCount();

		BirthRecordModel brm;
		Set<Integer> selection = mAdapter.getCurrentCheckedPosition();
		for (Integer item : selection) {
			brm = new BirthRecordModel(mAdapter.getBirthRecordId(mListView
					.getChildAt(item)), mResolver);
			brm.deleteBirthRecord(mResolver);
		}

		Toast.makeText(
				this,
				String.format(getString(R.string.delete_message),
						selectedItemCount, itemCount), Toast.LENGTH_LONG)
				.show();
		mLoaderManager.restartLoader(0, null, this);

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
			finish();
			return true;
		case R.id.change_password:
			intent = new Intent(this, ChangePasswordActivity.class);
			startActivity(intent);
			finish();
		default:
			return true;
		}
	}

}