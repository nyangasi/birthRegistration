package tz.co.rita.uploadverification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.Contracts;

/**
 * Created by mgirmaw on 10/22/2016.
 */
public class VerifyUpload {
    private Context mContext;

    private List<String> mMissingUploads = new ArrayList<String>();

    private static final String[] BIRTH_RECORD_FORM_NUMBER_PROJECTION = new String[]{
            Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID,
            Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER};

    private static final String[] MISSING_UPLOADS_NUMBER_PROJECTION = new String[]{
            "_ID",
            Contracts.MissingUploads.COLUMN_NAME_STARTING_NUMBER,
            Contracts.MissingUploads.COLUMN_NAME_ENDING_NUMBER};

    public VerifyUpload(Context context) {
        mContext = context;
    }

    public void FixUploadStatus() {
        Cursor cursor = mContext.getContentResolver().query(
                Contracts.BirthRecord.CONTENT_URI,
                BIRTH_RECORD_FORM_NUMBER_PROJECTION,
                null,
                null,
                null);

        int formNumberIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER);
        int idIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID);
        String formNumber;
        if (cursor.moveToFirst()) {
            do {
                formNumber = cursor.getString(formNumberIndex);
                if (formNumber.compareTo("400018357") < 0 || formNumber.compareTo("4000859981") > 0) {
                    mMissingUploads.add(String.valueOf(cursor.getInt(idIndex)));
                } else {
                    Cursor uploadCursor = mContext.getContentResolver().query(
                            Contracts.MissingUploads.CONTENT_URI,
                            MISSING_UPLOADS_NUMBER_PROJECTION,
                            formNumber + " >= " + Contracts.MissingUploads.COLUMN_NAME_STARTING_NUMBER + " AND " +
                                    formNumber + " <= " + Contracts.MissingUploads.COLUMN_NAME_ENDING_NUMBER
                            ,
                            null,
                            null);
                    if (uploadCursor.getCount() > 0) {
                        mMissingUploads.add(String.valueOf(cursor.getInt(idIndex)));
                    }
                    uploadCursor.close();
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        final ContentValues values = new ContentValues();
        values.put(Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS,
                BirthRegistrationConstants.UPLOAD_STAUS_PENDING);
        mContext.getContentResolver().update(
                Contracts.BirthRecord.CONTENT_URI,
                values,
                Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID
                        + " IN ( " + ListToString(mMissingUploads) + " )",
                null
        );
    }

    String ListToString(List<String> sList) {
        StringBuilder rString = new StringBuilder();
        String sep = ", ";
        int entries = sList.size();
        int count = 1;
        for (String each : sList) {
            if (count < entries) rString.append(each).append(sep);
            else rString.append(each);
            count++;
        }

        return rString.toString();
    }

}
