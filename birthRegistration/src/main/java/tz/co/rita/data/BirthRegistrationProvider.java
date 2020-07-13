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
package tz.co.rita.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class BirthRegistrationProvider extends ContentProvider {

	public static final String TAG = "BirthRegistrationProvider";
	public final static String PROJECTION = "projection";
	public final static String SELECTION = "selection";
	public final static String SELECTION_ARGS = "SelectionArgs";
	public final static String SORT_ORDER = "sortOrder";


	/**
	 * Defines a handle to the database helper object. The MainDatabaseHelper
	 * class is defined in a following snippet.
	 */
	private BirthRegistrationDbHelper mOpenHelper;

	/**
	 * Holds the database object
	 */
	private SQLiteDatabase db;

	/**
	 * Empty Constructor
	 */
	public BirthRegistrationProvider() {

	}

    /**
     * Wipes out all data in the database.  Designed to be used when phone is lost and
     * all data in the database needs to be deleted to prevent unauthorized access.
     */
	public void wipeData() {
		try {
			db = mOpenHelper.getWritableDatabase();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		try {
			db.execSQL(Contracts.SQL_DELETE_BIRTH_RECORD_TABLE);
			db.execSQL(Contracts.SQL_DELETE_USER_TABLE);
			db.execSQL(Contracts.SQL_DELETE_COUNTRY_TABLE);
			db.execSQL(Contracts.SQL_DELETE_MISSING_UPLOADS_TABLE);
			db.execSQL(Contracts.SQL_DELETE_DEATH_RECORD_TABLE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			db.close(); //
		}

	}

    /**
     * Deletes records from a database table <BR>
     * @param uri the uri of the table or a record.  If the last segment of the uri is
     *            a reference to the id of a record, the delete will work on a single record and will
     *            disregard the next two arguments.  If the last segment is a reference to the table
     *            itself the delete is performed on multiple records as specified by the next two
     *            argumens.
     * @param selection selection string to filter the records to delete
     * @param selectionArgs an array of strings that contains the selection arguments
     * @return
     */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = this.getId(uri);
		try {
			db = mOpenHelper.getWritableDatabase();

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		try {
			if (id < 0) {
				return db.delete(uri.getLastPathSegment(), selection,
						selectionArgs);
			} else {
				return db.delete(uri.getLastPathSegment(), "_ID" + "=" + id,
						null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			db.close(); //
		}
	}

    /**
     *
     * @param uri
     * @return
     */
	@Override
	public String getType(Uri uri) {

		String path = uri.getPath();
		String singleRecordMimeType = "";
		String multipleRecordMimeType = "";

		if (path.contains("country")) {
			singleRecordMimeType = Contracts.Country.SINGLE_RECORD_MIME_TYPE;
			multipleRecordMimeType = Contracts.Country.MULTIPLE_RECORDS_MIME_TYPE;
		} else if (path.contains("birthrecord")) {
			singleRecordMimeType = Contracts.BirthRecord.SINGLE_RECORD_MIME_TYPE;
			multipleRecordMimeType = Contracts.BirthRecord.MULTIPLE_RECORDS_MIME_TYPE;
		} else if (path.contains("user")) {
			singleRecordMimeType = Contracts.User.SINGLE_RECORD_MIME_TYPE;
			multipleRecordMimeType = Contracts.User.MULTIPLE_RECORDS_MIME_TYPE;
		}
		return this.getId(uri) < 0 ? multipleRecordMimeType
				: singleRecordMimeType;
	}

	public long getId(Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null) {
			try {
				return Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		try {
			db = mOpenHelper.getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		try {
			long id = db.insertOrThrow(uri.getLastPathSegment(), null, values);
			if (id == -1) {
				throw new RuntimeException(
						String.format(
								"%s: Failed to insert [%s] to [%s] for unknown reasons.",
								Contracts.TAG, values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			db.close();
		}
	}

    /**
     * Creates a new helper object. This method always returns quickly.
     * Notice that the database itself isn't created or opened until
     * SQLiteOpenHelper.getWritableDatabase is called
     */
	@Override
	public boolean onCreate() {

		mOpenHelper = new BirthRegistrationDbHelper(getContext(),
				Contracts.DB_NAME, null, Contracts.DB_VERSION);
		return true;
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = this.getId(uri);

		try {
			db = mOpenHelper.getReadableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		try {
			if (id < 0) {
				return db.query(uri.getLastPathSegment(), projection,
						selection, selectionArgs, null, null, sortOrder);
			} else {
				return db.query(uri.getLastPathSegment(), projection, "_ID"
						+ "=" + id, null, null, null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = this.getId(uri);

		// Log.i(TAG, "Updating: " + uri);
		try {
			db = mOpenHelper.getWritableDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		try {
			if (id < 0) {
				return db.update(uri.getLastPathSegment(), values, selection,
						selectionArgs);
			} else {
				return db.update(uri.getLastPathSegment(), values, "_ID" + "="
						+ id, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			db.close();
		}
	}

	/**
	 * Helper class that actually creates and manages the provider's underlying
	 * data repository.
	 */
	public static final class BirthRegistrationDbHelper extends
			SQLiteOpenHelper {

		Context mContext;

		/**
		 * Instantiates an open helper for the provider's SQLite data repository
		 * Do not do database creation and upgrade here.
		 */
		BirthRegistrationDbHelper(Context context, String db_name,
				SQLiteDatabase.CursorFactory factory, int db_version) {
			super(context, db_name, null, db_version);
			mContext = context;
		}

		/**
		 * Creates the data repository. This is called when the provider
		 * attempts to open the repository and SQLite reports that it doesn't
		 * exist.
		 */
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Contracts.SQL_CREATE_BIRTH_RECORD_TABLE);
			db.execSQL(Contracts.SQL_CREATE_USER_TABLE);
			db.execSQL(Contracts.SQL_CREATE_COUNTRY_TABLE);
			db.execSQL(Contracts.SQL_POPULATE_COUNTRY_TABLE);
			db.execSQL(Contracts.SQL_CREATE_DEATH_RECORD_TABLE);
		}

		/**
		 * Upgrades the data repository. This is done by deleting the table if
		 * it exists and executing the create sql statement.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(newVersion == 6){
				db.execSQL(Contracts.SQL_DELETE_MISSING_UPLOADS_TABLE);
				db.execSQL(Contracts.SQL_CREATE_MISSING_UPLOADS_TABLE);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_1);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_2);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_3);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_4);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_5);
				db.execSQL(Contracts.SQL_POPULATE_MISSING_UPLOADS_TABLE_6);
			}else {
				db.execSQL(Contracts.SQL_DELETE_BIRTH_RECORD_TABLE);
				db.execSQL(Contracts.SQL_DELETE_USER_TABLE);
				db.execSQL(Contracts.SQL_DELETE_COUNTRY_TABLE);
				onCreate(db);
			}

		}

		/**
		 * Downgrades the data repository. This is done by deleting the table if
		 * it exists and executing the create sql statement.
		 */
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			onUpgrade(db, oldVersion, newVersion);

		}
	}

}
