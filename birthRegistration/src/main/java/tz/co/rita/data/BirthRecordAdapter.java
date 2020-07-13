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

import android.annotation.SuppressLint;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tz.co.rita.birthregistration.BirthRegistrationApplication;
import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.dataexchange.DataUploader;

@SuppressLint("UnlocalizedSms")
public class BirthRecordAdapter {
    Context mContext;
    ContentResolver contentResolver;

    public BirthRecordAdapter(Context context) {
        this.mContext = context;
        contentResolver = context.getContentResolver();
    }

    public int getCount(int recordsToCount) {
        if (recordsToCount == BirthRecordAdapter.ALL_RECORDS) {
            return contentResolver.query(
                    Contracts.BirthRecord.CONTENT_URI,
                    BIRTH_RECORD_ID_COLUMN_PROJECTION,
                    null, null, null).getCount();
        } else if (recordsToCount == BirthRecordAdapter.PENDING_AND_UNCONFIRMED_RECORDS) {
            return contentResolver.query(
                    Contracts.BirthRecord.CONTENT_URI,
                    BIRTH_RECORD_ID_COLUMN_PROJECTION,
                    BIRTH_RECORD_UPLOAD_LIST_SELECTION,
                    UPLOAD_LIST_SELECTION_ARG,
                    null).getCount();
        } else return 0;

    }


    public Cursor getCursor() {
        return contentResolver.query(Contracts.BirthRecord.CONTENT_URI,
                BIRTH_RECORD_FULL_PROJECTION,
                null, null, null);
    }

    public Cursor getCursor(String[] projection) {
        return contentResolver.query(Contracts.BirthRecord.CONTENT_URI,
                projection, null, null, null);
    }

    public Cursor getCursor(String[] projection, String selection,
                            String[] selectionArgs, String order) {
        return contentResolver.query(Contracts.BirthRecord.CONTENT_URI,
                projection, selection, selectionArgs, order);
    }

    public void uploadRecords(int recordsToUpload) {
        BirthRecordModel[]  birthRecords = getRecords(recordsToUpload);
        if (birthRecords != null && birthRecords.length > 0) {
            DataUploader dataUploader = new DataUploader(mContext);
            int uploadsToProcess = birthRecords.length;
            if( !BirthRegistrationApplication.getInstance().isURLUploadEnabled()){
                uploadsToProcess = birthRecords.length > BirthRegistrationConstants.MAX_CONCURRENT_UPLOAD ?
                        BirthRegistrationConstants.MAX_CONCURRENT_UPLOAD : birthRecords.length;
            }
            for (int i=0; i< uploadsToProcess; i++) {
                dataUploader.Upload(birthRecords[i]);
            }
        }
    }

    public BirthRecordModel[] getRecords(int recordsToGet) {
        Cursor cursor = null;
        if (recordsToGet == BirthRecordAdapter.ALL_RECORDS) {
            cursor = getCursor(BIRTH_RECORD_FULL_PROJECTION);
        } else if (recordsToGet == BirthRecordAdapter.PENDING_AND_UNCONFIRMED_RECORDS) {

            cursor = getCursor(
                    BIRTH_RECORD_FULL_PROJECTION,
                    BIRTH_RECORD_UPLOAD_LIST_SELECTION,
                    UPLOAD_LIST_SELECTION_ARG, null);
        }
        if (cursor != null && cursor.getCount() > 0) {
           BirthRecordModel[] birthRecordsList = new BirthRecordModel[cursor.getCount()];
            int recordIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID);
            int formNumberIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER);
            int childFirstNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_FIRST_NAME);
            int childSecondNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_SECOND_NAME);
            int childSexIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX);
            int childBirthDateIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_DATE);
            int childBirthPlaceIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE);
            int childBirthPlaceDistrictCodeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE_DISTRICT_CODE);
            int childStateAtBirthIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_STATE_AT_BIRTH);
            int childTypeOfBirthIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_TYPE_OF_BIRTH);
            int childResidencePostCodeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CHILD_RESIDENCE_POST_CODE);
            int motherFirstNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_FIRST_NAME);
            int motherSecondNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_SECOND_NAME);
            int motherLastNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_LAST_NAME);
            int motherCountryOfBirthIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_COUNTRY_OF_BIRTH);
            int motherDistrictCodeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_DISTRICT_CODE);
            int motherNumberOfChildrenIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_NUMBER_OF_CHILDREN);
            int motherAgeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_MOTHER_AGE);

            int fatherFirstNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FATHER_FIRST_NAME);
            int fatherSecondNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FATHER_SECOND_NAME);
            int fatherLastNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FATHER_LAST_NAME);
            int fatherCountryOfBirthIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FATHER_COUNTRY_OF_BIRTH);
            int fatherDistrictCodeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FATHER_DISTRICT_CODE);

            int filledByIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY);
            int fillerFirstNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_FIRST_NAME);
            int fillerLastNameIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_LAST_NAME);

            int registrationCenterCodeIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_REGISTRATION_CENTER_CODE);
            int isCertificateIssuedIdIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_CERTIFICATE_ISSUED);
            int registrationDateIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION);
            int imageFilePathIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_IMAGE_FILE_PATH);
            int uploadStatusIndex = cursor.getColumnIndex(Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS);

            cursor.moveToFirst();

            for(int i= 0; i < cursor.getCount(); i++){
                birthRecordsList[i] =
                        new BirthRecordModel(
                        cursor.getInt(recordIdIndex),
                        cursor.getString(formNumberIndex),
                        cursor.getString(childFirstNameIndex),
                        cursor.getString(childSecondNameIndex),
                        cursor.getInt(childSexIdIndex),
                        cursor.getString(childBirthDateIndex),
                        cursor.getInt(childBirthPlaceIdIndex),
                        cursor.getString(childBirthPlaceDistrictCodeIndex),
                        cursor.getInt(childStateAtBirthIdIndex),
                        cursor.getInt(childTypeOfBirthIdIndex),
                        cursor.getString(childResidencePostCodeIndex),

                        cursor.getString(motherFirstNameIndex),
                        cursor.getString(motherSecondNameIndex),
                        cursor.getString(motherLastNameIndex),
                        cursor.getString(motherCountryOfBirthIndex),
                        cursor.getString(motherDistrictCodeIndex),
                        cursor.getInt(motherNumberOfChildrenIndex),
                        cursor.getInt(motherAgeIndex),

                        cursor.getString(fatherFirstNameIndex),
                        cursor.getString(fatherSecondNameIndex),
                        cursor.getString(fatherLastNameIndex),
                        cursor.getString(fatherCountryOfBirthIndex),
                        cursor.getString(fatherDistrictCodeIndex),

                        cursor.getInt(filledByIdIndex),
                        cursor.getString(fillerFirstNameIndex),
                        cursor.getString(fillerLastNameIndex),

                        cursor.getString(registrationCenterCodeIndex),
                        cursor.getInt(isCertificateIssuedIdIndex),
                        cursor.getString(registrationDateIndex),

                        cursor.getString(imageFilePathIndex),
                        cursor.getInt(uploadStatusIndex));
                if(!cursor.isLast()) cursor.moveToNext();
            }
            return birthRecordsList;
        } else return null;
    }

    public static int ALL_RECORDS = 0;
    public static int PENDING_AND_UNCONFIRMED_RECORDS = 1;
    private static final String BIRTH_RECORD_UPLOAD_LIST_SELECTION =
            "(" + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + "= ? OR "
                    + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + "= ?)";
    private static final String[] UPLOAD_LIST_SELECTION_ARG = {
            String.valueOf(BirthRegistrationConstants.UPLOAD_STAUS_PENDING),
            String.valueOf(BirthRegistrationConstants.UPLOAD_STAUS_UNCONFIRMED)
    };
    private static final String[] BIRTH_RECORD_ID_COLUMN_PROJECTION = new String[]{
            Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID};

    public static final String[] BIRTH_RECORD_FULL_PROJECTION = new String[]{
            Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID,
            Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_FIRST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_SECOND_NAME,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_DATE,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE_DISTRICT_CODE,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_STATE_AT_BIRTH,
			Contracts.BirthRecord.COLUMN_NAME_CHILD_TYPE_OF_BIRTH,
            Contracts.BirthRecord.COLUMN_NAME_CHILD_RESIDENCE_POST_CODE,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_FIRST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_SECOND_NAME,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_LAST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_COUNTRY_OF_BIRTH,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_DISTRICT_CODE,
			Contracts.BirthRecord.COLUMN_NAME_MOTHER_NUMBER_OF_CHILDREN,
            Contracts.BirthRecord.COLUMN_NAME_MOTHER_AGE,
            Contracts.BirthRecord.COLUMN_NAME_FATHER_FIRST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_FATHER_SECOND_NAME,
            Contracts.BirthRecord.COLUMN_NAME_FATHER_LAST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_FATHER_COUNTRY_OF_BIRTH,
            Contracts.BirthRecord.COLUMN_NAME_FATHER_DISTRICT_CODE,
            Contracts.BirthRecord.COLUMN_NAME_FILLED_BY,
            Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_FIRST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_LAST_NAME,
            Contracts.BirthRecord.COLUMN_NAME_REGISTRATION_CENTER_CODE,
            Contracts.BirthRecord.COLUMN_NAME_CERTIFICATE_ISSUED,
            Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION,
            Contracts.BirthRecord.COLUMN_NAME_IMAGE_FILE_PATH,
            Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS
    };
}