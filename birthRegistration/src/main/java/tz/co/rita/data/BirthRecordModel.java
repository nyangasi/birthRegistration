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


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import tz.co.rita.birthregistration.CameraActivity;
import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.birthregistration.DashBoardActivity;
import tz.co.rita.uiaids.EnumeratedFields;
import tz.co.rita.uiaids.EnumeratedFields.birthPlace;
import tz.co.rita.uiaids.EnumeratedFields.certificateIssued;
import tz.co.rita.uiaids.EnumeratedFields.filledBy;
import tz.co.rita.uiaids.EnumeratedFields.sex;
import tz.co.rita.uiaids.EnumeratedFields.stateAtBirth;
import tz.co.rita.uiaids.EnumeratedFields.typeOfBirth;
import tz.co.rita.utils.UtilsNetworking;

public class BirthRecordModel {

    public static final int SAVE_MODE_INSERT = 0;
    public static final int SAVE_MODE_UPDATE = 1;
    // These are the Contacts rows that we will retrieve
    private static final String[] ALL_COLUMNS_PROJECTION = new String[]{
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

    // Match form-number Selection
    static final String BIRTH_RECORD_ID_SELECTION = "(" + Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + "= ?)";

    private SharedPreferences mSharedPreference;

    private int recordId;
    private String formNumber;
    private String childFirstName;
    private String childSecondName;
    private int childSexId;
    private String childBirthDate;
    private int childBirthPlaceId;
    private String childBirthPlaceDistrictCode;
    private int childStateAtBirthId;
    private int childTypeOfBirthId;
    private String childResidencePostCode;

    private String motherFirstName;
    private String motherSecondName;
    private String motherLastName;
    private String motherCountryOfBirth;
    private String motherDistrictCode;
    private int motherNumberOfChildren;
    private int motherAge;

    private String fatherFirstName;
    private String fatherSecondName;
    private String fatherLastName;
    private String fatherCountryOfBirth;
    private String fatherDistrictCode;

    private int filledById;
    private String fillerFirstName;
    private String fillerLastName;

    private String registrationCenterCode;
    private int isCertificateIssuedId;
    private String registrationDate;


    private String imageFilePath;
    private int uploadStatus;


    public BirthRecordModel(int birthRecordId, ContentResolver contentResolver) {
        String[] selectionArg = {String.valueOf(birthRecordId)};
        Cursor cursor = contentResolver.query(
                Contracts.BirthRecord.CONTENT_URI,
                ALL_COLUMNS_PROJECTION,
                BIRTH_RECORD_ID_SELECTION,
                selectionArg,
                null);

        if (cursor.getCount() == 1) {
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
            this.recordId = cursor.getInt(recordIdIndex);
            this.formNumber = cursor.getString(formNumberIndex);

            this.childFirstName = cursor.getString(childFirstNameIndex);
            this.childSecondName = cursor.getString(childSecondNameIndex);
            this.childSexId = cursor.getInt(childSexIdIndex);
            this.childBirthDate = cursor.getString(childBirthDateIndex);
            this.childBirthPlaceId = cursor.getInt(childBirthPlaceIdIndex);
            this.childBirthPlaceDistrictCode = cursor.getString(childBirthPlaceDistrictCodeIndex);
            this.childStateAtBirthId = cursor.getInt(childStateAtBirthIdIndex);
            this.childTypeOfBirthId = cursor.getInt(childTypeOfBirthIdIndex);
            this.childResidencePostCode = cursor.getString(childResidencePostCodeIndex);

            this.motherFirstName = cursor.getString(motherFirstNameIndex);
            this.motherSecondName = cursor.getString(motherSecondNameIndex);
            this.motherLastName = cursor.getString(motherLastNameIndex);
            this.motherCountryOfBirth = cursor.getString(motherCountryOfBirthIndex);
            this.motherDistrictCode = cursor.getString(motherDistrictCodeIndex);
            this.motherNumberOfChildren = cursor.getInt(motherNumberOfChildrenIndex);
            this.motherAge = cursor.getInt(motherAgeIndex);

            this.fatherFirstName = cursor.getString(fatherFirstNameIndex);
            this.fatherSecondName = cursor.getString(fatherSecondNameIndex);
            this.fatherLastName = cursor.getString(fatherLastNameIndex);
            this.fatherCountryOfBirth = cursor.getString(fatherCountryOfBirthIndex);
            this.fatherDistrictCode = cursor.getString(fatherDistrictCodeIndex);

            this.filledById = cursor.getInt(filledByIdIndex);
            this.fillerFirstName = cursor.getString(fillerFirstNameIndex);
            this.fillerLastName = cursor.getString(fillerLastNameIndex);


            this.registrationCenterCode = cursor.getString(registrationCenterCodeIndex);
            this.isCertificateIssuedId = cursor.getInt(isCertificateIssuedIdIndex);
            this.registrationDate = cursor.getString(registrationDateIndex);

            this.imageFilePath = cursor.getString(imageFilePathIndex);
            this.uploadStatus = cursor.getInt(uploadStatusIndex);
        }
    }

    // Constructor with new Data
    public BirthRecordModel(
            // Form Header
            String formNumber,
            // Child Data
            String childFirstName, String childSecondName, int childSexId, String childBirthDate,
            int childBirthPlaceId, String childBirthPlaceDistrictCode, int childStateAtBirthId,
            int childTypeOfBirthId, String childResidencePostCode,
            // Mother Data
            String motherFirstName, String motherSecondName, String motherLastName,
            String motherCountryOfBirth, String motherDistrictCode, int motherNumberOfChildren, int motherAge,
            // Father Data
            String fatherFirstName, String fatherSecondName, String fatherLastName,
            String fatherCountryOfBirth, String fatherDistrictCode,
            // Solemn Disclosure
            int filledById, String fillerFirstName, String fillerLastName,
            //Registration Center
            String registrationCenterCode, int isCertificateIssuedId, String registrationDate,
            String imageFilePath, int uploadStatus
    ) {



        this.formNumber = formNumber;

        this.childFirstName = childFirstName;
        this.childSecondName = childSecondName;
        this.childSexId = childSexId;
        this.childBirthDate = childBirthDate;
        this.childBirthPlaceId = childBirthPlaceId;
        this.childBirthPlaceDistrictCode = childBirthPlaceDistrictCode;
        this.childStateAtBirthId = childStateAtBirthId;
        this.childTypeOfBirthId = childTypeOfBirthId;
        this.childResidencePostCode = childResidencePostCode;

        this.motherFirstName = motherFirstName;
        this.motherSecondName = motherSecondName;
        this.motherLastName = motherLastName;
        this.motherCountryOfBirth = motherCountryOfBirth;
        this.motherDistrictCode = motherDistrictCode;
        this.motherNumberOfChildren = motherNumberOfChildren;
        this.motherAge = motherAge;

        this.fatherFirstName = fatherFirstName;
        this.fatherSecondName = fatherSecondName;
        this.fatherLastName = fatherLastName;
        this.fatherCountryOfBirth = fatherCountryOfBirth;
        this.fatherDistrictCode = fatherDistrictCode;

        this.filledById = filledById;
        this.fillerFirstName = fillerFirstName;
        this.fillerLastName = fillerLastName;

        this.registrationCenterCode = registrationCenterCode;
        this.isCertificateIssuedId = isCertificateIssuedId;
        this.registrationDate = registrationDate;

        this.imageFilePath = imageFilePath;
        this.uploadStatus = uploadStatus;

    }

    public BirthRecordModel(
            int recordId,
            // Form Header
            String formNumber,
            // Child Data
            String childFirstName, String childSecondName, int childSexId, String childBirthDate,
            int childBirthPlaceId, String childBirthPlaceDistrictCode, int childStateAtBirthId,
            int childTypeOfBirthId, String childResidencePostCode,
            // Mother Data
            String motherFirstName, String motherSecondName, String motherLastName,
            String motherCountryOfBirth, String motherDistrictCode, int motherNumberOfChildren, int motherAge,
            // Father Data
            String fatherFirstName, String fatherSecondName, String fatherLastName,
            String fatherCountryOfBirth, String fatherDistrictCode,
            // Solemn Disclosure
            int filledById, String fillerFirstName, String fillerLastName,
            //Registration Center
            String registrationCenterCode, int isCertificateIssuedId, String registrationDate,
            String imageFilePath, int uploadStatus
    ) {
        this.recordId =  recordId;
        this.formNumber = formNumber;

        this.childFirstName = childFirstName;
        this.childSecondName = childSecondName;
        this.childSexId = childSexId;
        this.childBirthDate = childBirthDate;
        this.childBirthPlaceId = childBirthPlaceId;
        this.childBirthPlaceDistrictCode = childBirthPlaceDistrictCode;
        this.childStateAtBirthId = childStateAtBirthId;
        this.childTypeOfBirthId = childTypeOfBirthId;
        this.childResidencePostCode = childResidencePostCode;

        this.motherFirstName = motherFirstName;
        this.motherSecondName = motherSecondName;
        this.motherLastName = motherLastName;
        this.motherCountryOfBirth = motherCountryOfBirth;
        this.motherDistrictCode = motherDistrictCode;
        this.motherNumberOfChildren = motherNumberOfChildren;
        this.motherAge = motherAge;

        this.fatherFirstName = fatherFirstName;
        this.fatherSecondName = fatherSecondName;
        this.fatherLastName = fatherLastName;
        this.fatherCountryOfBirth = fatherCountryOfBirth;
        this.fatherDistrictCode = fatherDistrictCode;

        this.filledById = filledById;
        this.fillerFirstName = fillerFirstName;
        this.fillerLastName = fillerLastName;

        this.registrationCenterCode = registrationCenterCode;
        this.isCertificateIssuedId = isCertificateIssuedId;
        this.registrationDate = registrationDate;

        this.imageFilePath = imageFilePath;
        this.uploadStatus = uploadStatus;

    }

    public Uri updateUploadStatusToDb(ContentResolver contentResolver){
        ContentValues values = new ContentValues();
        values.put(Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS, getUploadStatus());
        String[] idSelector = {String.valueOf(this.getRecordId())};
        contentResolver.update(Contracts.BirthRecord.CONTENT_URI, values,
                Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + "=?",
                idSelector);
        return Uri.parse(Contracts.BirthRecord.CONTENT_URI + "/"
                + this.getRecordId());
    }

    public static void updateUploadStatusToDb(ContentResolver contentResolver, String formNumber, int uploadStatus) {
        String[] formNumberSelector = {formNumber};
        ContentValues values = new ContentValues();
        values.put(Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS, uploadStatus);

        contentResolver.update(Contracts.BirthRecord.CONTENT_URI, values,
                Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER + "=?",
                formNumberSelector);
    }

    public Uri saveBirthRecord(ContentResolver contentResolver, int saveMode) {
        ContentValues values = new ContentValues();
        values.put(Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER, getFormNumber());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_FIRST_NAME, getChildFirstName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_SECOND_NAME, getChildSecondName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX, getChildSexId());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_DATE, getChildBirthDate());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE, getChildBirthPlaceId());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_BIRTH_PLACE_DISTRICT_CODE, getChildBirthPlaceDistrictCode());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_STATE_AT_BIRTH, getChildStateAtBirthId());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_TYPE_OF_BIRTH, getChildTypeOfBirthId());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CHILD_RESIDENCE_POST_CODE, getChildResidencePostCode());

        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_FIRST_NAME, getMotherFirstName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_SECOND_NAME, getMotherSecondName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_LAST_NAME, getMotherLastName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_COUNTRY_OF_BIRTH, getMotherCountryOfBirth());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_DISTRICT_CODE, getMotherDistrictCode());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_NUMBER_OF_CHILDREN, getMotherNumberOfChildren());
        values.put(Contracts.BirthRecord.COLUMN_NAME_MOTHER_AGE, getMotherAge());

        values.put(Contracts.BirthRecord.COLUMN_NAME_FATHER_FIRST_NAME, getFatherFirstName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FATHER_SECOND_NAME, getFatherSecondName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FATHER_LAST_NAME, getFatherLastName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FATHER_COUNTRY_OF_BIRTH, getFatherCountryOfBirth());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FATHER_DISTRICT_CODE, getFatherDistrictCode());

        values.put(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY, getFilledById());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_FIRST_NAME, getFillerFirstName());
        values.put(Contracts.BirthRecord.COLUMN_NAME_FILLED_BY_LAST_NAME, getFillerLastName());

        values.put(Contracts.BirthRecord.COLUMN_NAME_REGISTRATION_CENTER_CODE, getRegistrationCenterCode());
        values.put(Contracts.BirthRecord.COLUMN_NAME_CERTIFICATE_ISSUED, getIsCertificateIssuedId());
        values.put(Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION, getRegistrationDate());

        values.put(Contracts.BirthRecord.COLUMN_NAME_IMAGE_FILE_PATH, getImageFilePath());
        values.put(Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS, getUploadStatus());
        Log.i("BR Model", " " + getUploadStatus());

        if (saveMode == DashBoardActivity.RECORD_VIEW_MODE_NEW) {
            Uri uri = contentResolver.insert(Contracts.BirthRecord.CONTENT_URI,
                    values);
            this.recordId = Integer.parseInt(uri.getLastPathSegment());
            return uri;
        } else if (saveMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT) {
            String[] idSelector = {String.valueOf(this.getRecordId())};
            contentResolver.update(Contracts.BirthRecord.CONTENT_URI, values,
                    Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + "=?",
                    idSelector);
            return Uri.parse(Contracts.BirthRecord.CONTENT_URI + "/"
                    + this.getRecordId());
        } else
            return null;
    }

    private filledBy getfilledBy() {
        filledBy filler;
        if (getFilledById() == 0) {
            filler = filledBy.M;
        } else if (getFilledById() == 1) {
            filler = filledBy.B;
        } else if (getFilledById() == 2) {
            filler = filledBy.Y;
        } else {
            filler = filledBy.T;
        }
        return filler;
    }

    private certificateIssued getIsCertificateIssued() {
        certificateIssued isCertificateIssued;
        if (getIsCertificateIssuedId() == 0) {
            isCertificateIssued = certificateIssued.Y;
        } else {
            isCertificateIssued = certificateIssued.N;
        }
        return isCertificateIssued;
    }

    private sex getSex() {

        sex childSex;
        if (getChildSexId() == 1) {
            childSex = sex.M;
        } else {
            childSex = sex.F;
        }
        return childSex;
    }

    private birthPlace getChildBirthPlace() {
        birthPlace childBirthPlace;
        if (getChildBirthPlaceId() == 0) {
            childBirthPlace = birthPlace.H;
        } else if (getChildBirthPlaceId() == 1) {
            childBirthPlace = birthPlace.M;
        } else {
            childBirthPlace = birthPlace.N;
        }
        return childBirthPlace;
    }

    private stateAtBirth getChildStateAtBirth() {
        stateAtBirth childStateAtBirth;
        if (getChildStateAtBirthId() == 0) {
            childStateAtBirth = stateAtBirth.M;
        } else {
            childStateAtBirth = stateAtBirth.H;
        }
        return childStateAtBirth;
    }

    private typeOfBirth getChildTypeOfBirth() {
        typeOfBirth childTypeOfBirth;
        if (getChildTypeOfBirthId() == 0) {
            childTypeOfBirth = typeOfBirth.S;
        } else if (getChildTypeOfBirthId() == 1) {
            childTypeOfBirth = typeOfBirth.T;
        } else {
            childTypeOfBirth = typeOfBirth.M;
        }
        return childTypeOfBirth;
    }

    public void upload(Context context, ContentResolver contentResolver) {

        if (UtilsNetworking.isNetworkAvailable((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))) {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation gc = (GsmCellLocation) telephonyManager.getCellLocation();
            int cellId = gc.getLac();
            mSharedPreference = context.getSharedPreferences(
                    BirthRegistrationConstants.PREFERENCE_NAME,
                    Context.MODE_PRIVATE);
            int currentUploadCount = this.mSharedPreference.getInt(BirthRegistrationConstants.UPLOAD_COUNT, 0);

            String SEP = ";";
            String mmform = BirthRegistrationConstants.MBIRTH + ":" + Integer.toHexString(currentUploadCount) + SEP + Integer.toHexString(cellId) + SEP + getFormNumber() + SEP
                    + getChildFirstName() + SEP + getChildSecondName() + SEP + getSex().name()
                    + SEP + getChildBirthDate() + SEP + getChildBirthPlace().name() + SEP
                    + getChildBirthPlaceDistrictCode() + SEP + getChildStateAtBirth().name()
                    + SEP + getChildResidencePostCode()
                    + SEP + getMotherFirstName() + SEP + getMotherSecondName() + SEP
                    + getMotherLastName() + SEP + getMotherCountryOfBirth() + SEP + getChildTypeOfBirth().name() + SEP + getMotherDistrictCode() + SEP + getMotherAge() + SEP + getFatherFirstName()
                    + SEP + getFatherSecondName() + SEP + getFatherLastName() + SEP + getFatherCountryOfBirth() + SEP
                    + getFatherDistrictCode() + SEP + getfilledBy().name()
                    + SEP + getFillerFirstName()
                    + SEP + getFillerLastName() + SEP + getRegistrationCenterCode() + SEP
                    + getIsCertificateIssued().name() + SEP + getRegistrationDate() + SEP + getMotherNumberOfChildren() + SEP;

            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(mmform);
            sms.sendMultipartTextMessage(BirthRegistrationConstants.SMS_ADDRESS, null, parts, null, null);
        }

    }

    public String getRecordUploadString(Context context, boolean urlEncode) {
        String mmform = null;
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation gc = (GsmCellLocation) telephonyManager.getCellLocation();
            int cellId = gc.getLac();

            mSharedPreference = context.getSharedPreferences(
                    BirthRegistrationConstants.PREFERENCE_NAME,
                    Context.MODE_PRIVATE);
            int currentUploadCount = this.mSharedPreference.getInt(BirthRegistrationConstants.UPLOAD_COUNT, 0);

            String SEP = ";";
            mmform = BirthRegistrationConstants.MBIRTH + ":"
                    + Integer.toHexString(currentUploadCount) + SEP
                    + Integer.toHexString(cellId) + SEP + getFormNumber() + SEP
                    + getChildFirstName() + SEP + getChildSecondName() + SEP + getSex().name()
                    + SEP + getChildBirthDate() + SEP + getChildBirthPlace().name() + SEP
                    + getChildBirthPlaceDistrictCode() + SEP + getChildStateAtBirth().name()
                    + SEP + getChildResidencePostCode()
                    + SEP + getMotherFirstName() + SEP + getMotherSecondName() + SEP
                    + getMotherLastName() + SEP + getMotherCountryOfBirth() + SEP + getChildTypeOfBirth().name() + SEP + getMotherDistrictCode() + SEP + getMotherAge() + SEP + getFatherFirstName()
                    + SEP + getFatherSecondName() + SEP + getFatherLastName() + SEP + getFatherCountryOfBirth() + SEP
                    + getFatherDistrictCode() + SEP + getfilledBy().name()
                    + SEP + getFillerFirstName()
                    + SEP + getFillerLastName() + SEP + getRegistrationCenterCode() + SEP
                    + getIsCertificateIssued().name() + SEP + getRegistrationDate() + SEP + getMotherNumberOfChildren() + SEP
            //        + getImageBase64(urlEncode)
            ;

        return mmform;
    }

    public void deleteBirthRecord(ContentResolver contentResolver) {
        String[] idSelector = {Integer.toString(this.getRecordId())};
        contentResolver.delete(Contracts.BirthRecord.CONTENT_URI,
                Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + "=?",
                idSelector);

    }

    public static void deleteBirthRecord(ContentResolver contentResolver, String formNumber) {
        String[] formNumberSelector = {formNumber};
        contentResolver.delete(Contracts.BirthRecord.CONTENT_URI,
                Contracts.BirthRecord.COLUMN_NAME_FORM_NUMBER + "=?",
                formNumberSelector);

    }

    public void setRecordId(int value){
        recordId = value;
    }
    public int getRecordId() {
        return recordId;
    }

    public String getFormNumber() {
        return formNumber;
    }

    public String getChildFirstName() {
        return childFirstName;
    }

    public String getChildSecondName() {
        return childSecondName;
    }

    public int getChildSexId() {
        return childSexId;
    }

    public String getChildBirthDate() {
        return childBirthDate;
    }

    public int getChildBirthPlaceId() {
        return childBirthPlaceId;
    }

    public String getChildBirthPlaceDistrictCode() {
        return childBirthPlaceDistrictCode;
    }

    public int getChildStateAtBirthId() {
        return childStateAtBirthId;
    }

    public int getChildTypeOfBirthId() {
        return childTypeOfBirthId;
    }

    public String getChildResidencePostCode() {
        return childResidencePostCode;
    }

    public String getMotherFirstName() {
        return motherFirstName;
    }

    public String getMotherSecondName() {
        return motherSecondName;
    }

    public String getMotherLastName() {
        return motherLastName;
    }

    public String getMotherCountryOfBirth() {
        return motherCountryOfBirth;
    }

    public String getMotherDistrictCode() {
        return motherDistrictCode;
    }

    public int getMotherNumberOfChildren() {
        return motherNumberOfChildren;
    }

    public int getMotherAge() {
        return motherAge;
    }

    public String getFatherFirstName() {
        return fatherFirstName;
    }

    public String getFatherSecondName() {
        return fatherSecondName;
    }

    public String getFatherLastName() {
        return fatherLastName;
    }

    public String getFatherCountryOfBirth() {
        return fatherCountryOfBirth;
    }

    public String getFatherDistrictCode() {
        return fatherDistrictCode;
    }

    public int getFilledById() {
        return filledById;
    }

    public String getFillerFirstName() {
        return fillerFirstName;
    }

    public String getFillerLastName() {
        return fillerLastName;
    }

    public String getRegistrationCenterCode() {
        return registrationCenterCode;
    }

    public int getIsCertificateIssuedId() {
        return isCertificateIssuedId;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public int getUploadStatus(){ return uploadStatus; }

    public String getImageFilePath(){ return imageFilePath; }

    public String getImageBase64(boolean urlEncode){
        Bitmap bm = BitmapFactory.decodeFile(imageFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(
                CameraActivity.IMAGE_MIME_TYPE,
                CameraActivity.IMAGE_BASE64_ENCODING_QUALITY,
                baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        try {
            return urlEncode
                    ? URLEncoder.encode(Base64.encodeToString(b, Base64.DEFAULT), "UTF8")
                    : Base64.encodeToString(b, Base64.DEFAULT);
        } catch(Exception e){
            return Base64.encodeToString(b, Base64.DEFAULT);
        }
    }

    public void setUploadStatus(int uploadStatus){ this.uploadStatus = uploadStatus;}
}
