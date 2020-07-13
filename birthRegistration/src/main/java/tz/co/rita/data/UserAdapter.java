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


import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.utils.UtilsEncryption;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
/**
 * Class for performing user data actions.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 *
 *
 */
public class UserAdapter {
	
	String TAG = UserAdapter.class.getSimpleName();
	public static String currentUser = "";
	public static String currentUserName = "";
	public static boolean isCurrentUserLocked = false;
	Context mContext;

	public UserAdapter(Context context) {
		this.mContext = context;
	}

	public int getCount() {
		Cursor cursor = mContext.getContentResolver().query(Contracts.User.CONTENT_URI,
                USER_NAME_PASS_SALT_PROJECTION, null, null, null);
		int count =	 cursor.getCount();
        cursor.close();
		return count;
	}

	public Cursor getCursor(String[] projection) {
		return mContext.getContentResolver().query(Contracts.User.CONTENT_URI, projection,
                null, null, null);

	}



	public Cursor getCursor(String[] projection, String selection) {
		return mContext.getContentResolver().query(Contracts.User.CONTENT_URI, projection,
                selection, null, null);
	}

	public Cursor getCursor(String[] projection, String selection,
			String[] selectionArgs) {
		return mContext.getContentResolver().query(Contracts.User.CONTENT_URI, projection,
                selection, selectionArgs, null);
	}

	public boolean userExists(String username) {
		String[] selectionArgs = { username };
		boolean exists = false;
		Cursor loginCursor = getCursor(USER_FULL_PROJECTION,
				USERNAME_SELECTION, selectionArgs);
		if (loginCursor != null) {
			if (loginCursor.getCount() > 0) {
				 exists = true;
			}
			loginCursor.close();
		}
		return exists;
	}
	
	public boolean isUserLocked(String username){
		String[] selectionArgs = { username };
		boolean locked = true;
		Cursor loginCursor = getCursor(USER_FULL_PROJECTION,
				USERNAME_SELECTION, selectionArgs);
		
		int lockedIndex = loginCursor
				.getColumnIndexOrThrow(Contracts.User.COLUMN_NAME_LOCKED);

		if (loginCursor != null) {
			if (loginCursor.getCount() > 0) {				
				loginCursor.moveToFirst();
				locked = loginCursor.getInt(lockedIndex) == 1;
			}
			loginCursor.close();
		}
		return locked;
	}
	
	public int unlockUser(String username) {
		ContentValues values = new ContentValues();
		values.put(Contracts.User.COLUMN_NAME_LOCKED, 0);
		values.put(Contracts.User.COLUMN_NAME_MODIFIED_DATE,
				System.currentTimeMillis());		

		String[] selectionArgs = { username };
		int result = mContext.getContentResolver().update(Contracts.User.CONTENT_URI, values,
                USERNAME_SELECTION, selectionArgs);
		if(result > 0 && username.equals(currentUserName)) isCurrentUserLocked = false;

		return result;

	}
	
	public int lockUser(String username) {
		Log.i(TAG, " Locking " + username);
		ContentValues values = new ContentValues();
		values.put(Contracts.User.COLUMN_NAME_LOCKED, 1);
		values.put(Contracts.User.COLUMN_NAME_MODIFIED_DATE,
				System.currentTimeMillis());		

		String[] selectionArgs = { username };
		int result = mContext.getContentResolver().update(Contracts.User.CONTENT_URI, values,
                USERNAME_SELECTION, selectionArgs);
		if(result > 0 && username.equals(currentUserName)) isCurrentUserLocked = true;
		Log.i(TAG, " Locked Accounts count = " + result);
		return result;

	}

	public Uri addUser(String username, String fullname, String password) {
		String saltString = UtilsEncryption.generateSaltString();
		String encryptedPassWord = UtilsEncryption.encryptPassword(password,
				saltString);

		if (encryptedPassWord.equals(null)) {
			return null;
		}

		
		String usermessage = "newuser;" + username + ";" + fullname + ";" ;
		
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(BirthRegistrationConstants.SMS_ADDRESS, null, usermessage, null, null);
		
		ContentValues values = new ContentValues();
		values.put(Contracts.User.COLUMN_NAME_USER_NAME, username);
		values.put(Contracts.User.COLUMN_NAME_USER_FULL_NAME, fullname);
		values.put(Contracts.User.COLUMN_NAME_PASSWORD, encryptedPassWord);
		values.put(Contracts.User.COLUMN_NAME_LOCKED, 1);
		values.put(Contracts.User.COLUMN_NAME_EXPIRED, 0);
		values.put(Contracts.User.COLUMN_NAME_EXPIRY_DATE,
				System.currentTimeMillis());
		values.put(Contracts.User.COLUMN_NAME_LOGIN_ATTEMPTS, 0);
		values.put(Contracts.User.COLUMN_NAME_LAST_LOGIN,
				System.currentTimeMillis());
		values.put(Contracts.User.COLUMN_NAME_MODIFIED_DATE,
				System.currentTimeMillis());
		values.put(Contracts.User.COLUMN_NAME_CREATED_DATE,
				System.currentTimeMillis());
		values.put(Contracts.User.COLUMN_NAME_SALT, saltString);
		Uri result=mContext.getContentResolver().insert(Contracts.User.CONTENT_URI, values);
		Log.i("URI",result + "");
		return result;

	}

	public int updateUserPassword(String username, String newPassword) {
		String saltString = UtilsEncryption.generateSaltString();
		String encryptedPassWord = UtilsEncryption.encryptPassword(newPassword,
				saltString);

		if (encryptedPassWord.equals(null)) {
			return 0;
		}

		ContentValues values = new ContentValues();
		values.put(Contracts.User.COLUMN_NAME_PASSWORD, encryptedPassWord);
		values.put(Contracts.User.COLUMN_NAME_MODIFIED_DATE,
				System.currentTimeMillis());
		values.put(Contracts.User.COLUMN_NAME_SALT, saltString);

		String[] selectionArgs = { username };
		return mContext.getContentResolver().update(Contracts.User.CONTENT_URI, values,
				USERNAME_SELECTION, selectionArgs);

	}

	public boolean authenticate(String username, String password)
			throws SQLException {
		Log.i(TAG, " Authenticating " + username);
		if (userExists(username)) {
			Log.i(TAG, username + " Exists.  Locked = " + isUserLocked(username));
			if(isUserLocked(username)) return false;
			String[] selectionArgs = { username };
			Cursor loginCursor = getCursor(USER_FULL_PROJECTION,
					USERNAME_SELECTION, selectionArgs);

			int saltStringIndex = loginCursor
					.getColumnIndexOrThrow(Contracts.User.COLUMN_NAME_SALT);
			int userFullNameIndex = loginCursor.getColumnIndexOrThrow(Contracts.User.COLUMN_NAME_USER_FULL_NAME);
			int passwordIndex = loginCursor
					.getColumnIndexOrThrow(Contracts.User.COLUMN_NAME_PASSWORD);
			loginCursor.moveToFirst();
			String saltString = loginCursor.getString(saltStringIndex);
			String encryptedPassword = UtilsEncryption.encryptPassword(
					password, saltString);
			String storedEncryptedPassword = loginCursor
					.getString(passwordIndex);
			currentUser = loginCursor.getString(userFullNameIndex);
			loginCursor.close();

			currentUserName = username;
			isCurrentUserLocked = isUserLocked(username);
			return encryptedPassword.equals(storedEncryptedPassword);
		}

		return false;
	}

	public static final String[] USER_FULL_PROJECTION = new String[] {
			Contracts.User._ID, Contracts.User.COLUMN_NAME_USER_NAME,
			Contracts.User.COLUMN_NAME_USER_FULL_NAME,
			Contracts.User.COLUMN_NAME_PASSWORD,
			Contracts.User.COLUMN_NAME_LOCKED,
			Contracts.User.COLUMN_NAME_EXPIRED,
			Contracts.User.COLUMN_NAME_EXPIRY_DATE,
			Contracts.User.COLUMN_NAME_LOGIN_ATTEMPTS,
			Contracts.User.COLUMN_NAME_LAST_LOGIN,
			Contracts.User.COLUMN_NAME_MODIFIED_DATE,
			Contracts.User.COLUMN_NAME_CREATED_DATE,
			Contracts.User.COLUMN_NAME_SALT };

	public static final String[] USER_NAME_PASS_SALT_PROJECTION = new String[] {
			Contracts.User._ID, Contracts.User.COLUMN_NAME_USER_NAME,
			Contracts.User.COLUMN_NAME_PASSWORD,
			Contracts.User.COLUMN_NAME_SALT };

	public static final String USERNAME_PASSWORD_SELECTION = "username=? AND password=?";
	public static final String USERNAME_SELECTION = Contracts.User.COLUMN_NAME_USER_NAME
			+ "=?";

}
