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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.BirthRecordModel;
import tz.co.rita.data.Contracts;
import tz.co.rita.data.DataValidator;
import tz.co.rita.dataexchange.DataUploader;
import tz.co.rita.uiaids.DatePickerFragment;
import tz.co.rita.uiaids.EnumeratedFields;

/**
 * This is the Activity class that represents the user interface for creating/editing
 * birth registration records.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 */

public class BirthRecordActivity extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private int mRecordViewMode;
    private int mRecordId;
    private Cursor mCountryCursor;
    private String mImageFilePath = null;

    private BirthRecordModel mBirthRecordModel;

    private static final String DEFAULT_COUNTRY_CODE = "TZA";

    private static final String[] COUNTRY_PROJECTION_FOR_SPINNER = new String[]{
            Contracts.Country.COLUMN_NAME_COUNTRY_ID,
            Contracts.Country.COLUMN_NAME_COUNTRY_CODE,
            Contracts.Country.COLUMN_NAME_COUNTRY_NAME};
    private static final String[] COUNTRY_FROM_COLUMNS = {
            Contracts.Country.COLUMN_NAME_COUNTRY_NAME
    };
    private static final int[] COUNTRY_TO_VIEWS = {android.R.id.text1};

    SimpleCursorAdapter mCountrySpinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birth_record);

        Intent intent = getIntent();
        mRecordViewMode = intent.getIntExtra(DashBoardActivity.RECORD_VIEW_MODE,
                DashBoardActivity.RECORD_VIEW_MODE_NEW);
        if (mRecordViewMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT)
            mRecordId = Integer.parseInt(intent.getStringExtra(Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID));
        mBirthRecordModel = new BirthRecordModel(mRecordId, getContentResolver());
        mCountryCursor = getContentResolver().query(
                Contracts.Country.CONTENT_URI,
                COUNTRY_PROJECTION_FOR_SPINNER,
                null,
                null,
                Contracts.Country.COLUMN_NAME_COUNTRY_NAME);
        mCountrySpinnerAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, mCountryCursor, COUNTRY_FROM_COLUMNS,
                COUNTRY_TO_VIEWS, 0);
        mCountrySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        initializeViews();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (BirthRegistrationApplication.app_invalid) {
            setContentView(R.layout.app_disabled);
            ((TextView) findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_invalid_message));
            return;
        }
        if (BirthRegistrationApplication.app_locked) {
            setContentView(R.layout.app_disabled);
            ((TextView) findViewById(R.id.textViewAppInvalid)).setText(getString(R.string.app_locked_message));
            return;
        }
        ImageView iv = (ImageView) findViewById(R.id.formPicture);
        if (mImageFilePath != null) {
            iv.setImageURI(Uri.parse(mImageFilePath));
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }


    }

    private void initializeViews() {


        findViewById(R.id.editTextDateOfRegistration).setOnFocusChangeListener(
                datePickerFocusChangeListener);
        findViewById(R.id.editTextBirthDate).setOnFocusChangeListener(
                datePickerFocusChangeListener);
        ((Spinner) findViewById(R.id.spinnerPlaceOfBirth))
                .setOnItemSelectedListener(spinnerItemSelectedListener);
        ((Spinner) findViewById(R.id.spinnerFilledBy))
                .setOnItemSelectedListener(spinnerItemSelectedListener);

        Spinner fatherCountrySpinner = (Spinner) findViewById(R.id.spinnerFatherCountryOfBirth);
        fatherCountrySpinner.setAdapter(mCountrySpinnerAdapter);
        Spinner motherCountrySpinner = (Spinner) findViewById(R.id.spinnerMotherCountryOfBirth);
        motherCountrySpinner.setAdapter(mCountrySpinnerAdapter);

        switch (mRecordViewMode) {
            case DashBoardActivity.RECORD_VIEW_MODE_NEW:
                int countrySpinnerPosition = this.getCountrySpinnerPosition(DEFAULT_COUNTRY_CODE);
                motherCountrySpinner.setSelection(countrySpinnerPosition);
                fatherCountrySpinner.setSelection(countrySpinnerPosition);
                ((EditText) findViewById(R.id.editTextRegistrationCenterCode))
                        .setText(BirthRegistrationApplication.getInstance().getRegistrationCenterCode());
                break;
            case DashBoardActivity.RECORD_VIEW_MODE_EDIT:
                ((EditText) findViewById(R.id.editTextFormNumber)).setText(this.mBirthRecordModel.getFormNumber());
                ((EditText) findViewById(R.id.editTextFormNumber)).setEnabled(false);
                ((EditText) findViewById(R.id.editTextBabyFirstName)).setText(this.mBirthRecordModel.getChildFirstName());
                ((EditText) findViewById(R.id.editTextBabySecondName)).setText(this.mBirthRecordModel.getChildSecondName());
                ((Spinner) findViewById(R.id.spinnerBabySex)).setSelection(this.mBirthRecordModel.getChildSexId());
                ((EditText) findViewById(R.id.editTextBirthDate))
                        .setText(this.mBirthRecordModel.getChildBirthDate());
                ((Spinner) findViewById(R.id.spinnerPlaceOfBirth))
                        .setSelection(this.mBirthRecordModel.getChildBirthPlaceId());
                ((EditText) findViewById(R.id.editTextBabyPlaceOfBirthDistrictCode))
                        .setText(this.mBirthRecordModel.getChildBirthPlaceDistrictCode());
                ((Spinner) findViewById(R.id.spinnerStateAtBirth))
                        .setSelection(this.mBirthRecordModel.getChildStateAtBirthId());
                ((Spinner) findViewById(R.id.spinnerTypeOfBirth))
                        .setSelection(this.mBirthRecordModel.getChildTypeOfBirthId());
                ((EditText) findViewById(R.id.editTextBabyResidencePostCode))
                        .setText(this.mBirthRecordModel.getChildResidencePostCode());

                ((EditText) findViewById(R.id.editTextMotherFirstName))
                        .setText(this.mBirthRecordModel.getMotherFirstName());
                ((EditText) findViewById(R.id.editTextMotherSecondName))
                        .setText(this.mBirthRecordModel.getMotherSecondName());
                ((EditText) findViewById(R.id.editTextMotherLastName))
                        .setText(this.mBirthRecordModel.getMotherLastName());
                countrySpinnerPosition = this.getCountrySpinnerPosition(this.mBirthRecordModel.getMotherCountryOfBirth());
                motherCountrySpinner.setSelection(countrySpinnerPosition);
                ((EditText) findViewById(R.id.editTextMotherDistrictCode))
                        .setText(this.mBirthRecordModel.getMotherDistrictCode());
                ((EditText) findViewById(R.id.editTextMotherNumberOfChildren))
                        .setText(String.valueOf(this.mBirthRecordModel.getMotherNumberOfChildren()));
                ((EditText) findViewById(R.id.editTextMotherAge))
                        .setText(String.valueOf(this.mBirthRecordModel.getMotherAge()));

                ((EditText) findViewById(R.id.editTextFatherFirstName))
                        .setText(this.mBirthRecordModel.getFatherFirstName());
                ((EditText) findViewById(R.id.editTextFatherSecondName))
                        .setText(this.mBirthRecordModel.getFatherSecondName());
                ((EditText) findViewById(R.id.editTextFatherLastName))
                        .setText(this.mBirthRecordModel.getFatherLastName());
                countrySpinnerPosition = this.getCountrySpinnerPosition(this.mBirthRecordModel.getFatherCountryOfBirth());
                fatherCountrySpinner.setSelection(countrySpinnerPosition);
                ((EditText) findViewById(R.id.editTextFatherDistrictCode))
                        .setText(this.mBirthRecordModel.getFatherDistrictCode());


                ((Spinner) findViewById(R.id.spinnerFilledBy)).setSelection(this.mBirthRecordModel.getFilledById());
                ((EditText) findViewById(R.id.editTextFilledByFirstName))
                        .setText(this.mBirthRecordModel.getFillerFirstName());
                ((EditText) findViewById(R.id.editTextFilledByLastName))
                        .setText(this.mBirthRecordModel.getFillerLastName());

                ((EditText) findViewById(R.id.editTextRegistrationCenterCode))
                        .setText(this.mBirthRecordModel.getRegistrationCenterCode());

                ((Spinner) findViewById(R.id.spinnerCertificateIssued))
                        .setSelection(this.mBirthRecordModel.getIsCertificateIssuedId());
                ((EditText) findViewById(R.id.editTextDateOfRegistration))
                        .setText(this.mBirthRecordModel.getRegistrationDate());

                break;
        }
    }


    private int getCountrySpinnerPosition(String countryCode) {

        int count = 0;
        mCountryCursor.moveToFirst();
        int countryCodeIndex = mCountryCursor.getColumnIndex(Contracts.Country.COLUMN_NAME_COUNTRY_CODE);
        while (mCountryCursor.moveToNext()) {
            count++;
            if (mCountryCursor.getString(countryCodeIndex).equals(countryCode)) {
                return count;
            }
        }
        return 0;

    }

    private OnFocusChangeListener datePickerFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                showDatePickerDialog(v);
            }

        }
    };


    private AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {

            if (parent.getId() == R.id.spinnerPlaceOfBirth) {
                findViewById(R.id.editTextBabyPlaceOfBirthDistrictCode)
                        .setEnabled(true);

            } else if (parent.getId() == R.id.spinnerFilledBy) {

                if (pos == EnumeratedFields.filledBy.Y.getId()
                        || pos == EnumeratedFields.filledBy.T.getId()) {
                    findViewById(R.id.editTextFilledByFirstName).setEnabled(
                            true);
                    findViewById(R.id.editTextFilledByLastName)
                            .setEnabled(true);
                } else {
                    ((EditText) findViewById(R.id.editTextFilledByFirstName))
                            .setText("");
                    findViewById(R.id.editTextFilledByFirstName).setEnabled(
                            false);
                    ((EditText) findViewById(R.id.editTextFilledByLastName))
                            .setText("");
                    findViewById(R.id.editTextFilledByLastName).setEnabled(
                            false);
                }

            }
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    };

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();

        Bundle args = new Bundle();
        args.putInt("viewId", v.getId());
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void setEditTextValue(int id, String value) {
        ((EditText) findViewById(id)).setText(value);
    }

    public void onSaveButtonClick(View v) {
        int birthRecordid = 0;
        if (mBirthRecordModel != null)
            birthRecordid = this.mBirthRecordModel.getRecordId();

        mBirthRecordModel = createBirthRecord();

        if (mBirthRecordModel == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.input_error_message),
                    Toast.LENGTH_LONG).show();
        } else {
            if (this.mRecordViewMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT)
                this.mBirthRecordModel.setRecordId(birthRecordid);
            mBirthRecordModel.saveBirthRecord(getContentResolver(),
                    this.mRecordViewMode);
            finish();
        }

    }

    public void onUploadButtonClick(View v) {
        int birthRecordid = 0;
        if (mBirthRecordModel != null)
            birthRecordid = this.mBirthRecordModel.getRecordId();

        mBirthRecordModel = createBirthRecord();

        if (mBirthRecordModel == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.input_error_message),
                    Toast.LENGTH_LONG).show();
        } else {
            if (this.mRecordViewMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT) {
                this.mBirthRecordModel.setRecordId(birthRecordid);
            }
            mBirthRecordModel.saveBirthRecord(getContentResolver(),
                    this.mRecordViewMode);
            DataUploader dataUploader = new DataUploader((Context) this);
            dataUploader.Upload(mBirthRecordModel);
            finish();
        }
    }

    public void onCancelButtonClick(View v) {
        finish();
    }

    private BirthRecordModel createBirthRecord() {
        boolean isValidInput = true;
        // Get the Form Data
        String formNumber = ((EditText) findViewById(R.id.editTextFormNumber))
                .getText().toString();
        String childFirstName = ((EditText) findViewById(R.id.editTextBabyFirstName))
                .getText().toString();
        String childSecondName = ((EditText) findViewById(R.id.editTextBabySecondName))
                .getText().toString();
        int childSexId = ((Spinner) findViewById(R.id.spinnerBabySex))
                .getSelectedItemPosition();
        String childBirthDate = ((EditText) findViewById(R.id.editTextBirthDate))
                .getText().toString();
        int childBirthPlaceId = ((Spinner) findViewById(R.id.spinnerPlaceOfBirth))
                .getSelectedItemPosition();
        String childBirthPlaceDistrictCode = ((EditText) findViewById(R.id.editTextBabyPlaceOfBirthDistrictCode))
                .getText().toString();
        int childStateAtBirthId = ((Spinner) findViewById(R.id.spinnerStateAtBirth))
                .getSelectedItemPosition();
        int childTypeOfBirthId = ((Spinner) findViewById(R.id.spinnerTypeOfBirth))
                .getSelectedItemPosition();
        String childResidencePostCode = ((EditText) findViewById(R.id.editTextBabyResidencePostCode))
                .getText().toString();

        String motherFirstName = ((EditText) findViewById(R.id.editTextMotherFirstName))
                .getText().toString();
        String motherSecondName = ((EditText) findViewById(R.id.editTextMotherSecondName))
                .getText().toString();
        String motherLastName = ((EditText) findViewById(R.id.editTextMotherLastName))
                .getText().toString();


        Cursor mcc = (Cursor) ((Spinner) findViewById(R.id.spinnerMotherCountryOfBirth)).getSelectedItem();
        String motherCountryOfBirth =
                mcc.getString(mcc.getColumnIndexOrThrow(Contracts.Country.COLUMN_NAME_COUNTRY_CODE));

        String motherDistrictCode = ((EditText) findViewById(R.id.editTextMotherDistrictCode))
                .getText().toString();

        String motherNumberOfChildrenString = ((EditText) findViewById(R.id.editTextMotherNumberOfChildren))
                .getText().toString();
        String motherAgeString = ((EditText) findViewById(R.id.editTextMotherAge))
                .getText().toString();

        int motherNumberOfChildren = 0;
        int motherAge = 0;
        try {
            motherNumberOfChildren = Integer.parseInt(motherNumberOfChildrenString);
            motherAge = Integer.parseInt(motherAgeString);
        } catch (Exception e) {

        }

        String fatherFirstName = ((EditText) findViewById(R.id.editTextFatherFirstName))
                .getText().toString();
        String fatherSecondName = ((EditText) findViewById(R.id.editTextFatherSecondName))
                .getText().toString();
        String fatherLastName = ((EditText) findViewById(R.id.editTextFatherLastName))
                .getText().toString();

        Cursor fcc = (Cursor) ((Spinner) findViewById(R.id.spinnerFatherCountryOfBirth)).getSelectedItem();
        String fatherCountryOfBirth = fcc.getString(fcc.getColumnIndexOrThrow(Contracts.Country.COLUMN_NAME_COUNTRY_CODE));

        String fatherDistrictCode = ((EditText) findViewById(R.id.editTextFatherDistrictCode))
                .getText().toString();

        int filledById = ((Spinner) findViewById(R.id.spinnerFilledBy))
                .getSelectedItemPosition();
        String fillerFirstName = ((EditText) findViewById(R.id.editTextFilledByFirstName))
                .getText().toString();
        String fillerLastName = ((EditText) findViewById(R.id.editTextFilledByLastName))
                .getText().toString();

        String registrationCenterCode = ((EditText) findViewById(R.id.editTextRegistrationCenterCode))
                .getText().toString();

        int isCertificateIssuedId = ((Spinner) findViewById(R.id.spinnerCertificateIssued))
                .getSelectedItemPosition();
        String registrationDate = ((EditText) findViewById(R.id.editTextDateOfRegistration))
                .getText().toString();
        String imageFilePath = mImageFilePath;
        int uploadStatus = BirthRegistrationConstants.UPLOAD_STAUS_PENDING;


        // ==========================
        // Form Number Validation
        // ==========================
        if (!DataValidator.isValidFormNumber(formNumber)) {
            ((TextView) findViewById(R.id.TextViewFormNumber))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewFormNumber))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Child Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(childFirstName)) {
            ((TextView) findViewById(R.id.TextViewBabyFirstName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewBabyFirstName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSecondName(childSecondName)) {
            ((TextView) findViewById(R.id.TextViewBabySecondName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewBabySecondName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidSex(childSexId)) {
            ((TextView) findViewById(R.id.TextViewBabySex))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        }

        if (!DataValidator.isValidRegistrantBirthDate(childBirthDate, registrationDate)) {
            ((TextView) findViewById(R.id.TextViewBirthDate))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewBirthDate))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (childBirthPlaceId == EnumeratedFields.birthPlace.H.getId()) {
            if (!DataValidator.isValidDistrictCode(childBirthPlaceDistrictCode)) {
                ((TextView) findViewById(R.id.TextViewBabyPlaceOfBirthDistrictCode))
                        .setTextAppearance(this, R.style.ErrorTextViewStyle);
                isValidInput = false;
            } else {
                ((TextView) findViewById(R.id.TextViewBabyPlaceOfBirthDistrictCode))
                        .setTextAppearance(
                                this,
                                android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
            }
        }
        if (!DataValidator.isValidPostCode(childResidencePostCode)) {
            ((TextView) findViewById(R.id.TextViewBabyResidencePostCode))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewBabyResidencePostCode))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Mother Data Validation
        // ==========================
        if (!DataValidator.isValidFirstName(motherFirstName)) {
            ((TextView) findViewById(R.id.TextViewMotherFirstName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherFirstName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSecondName(motherSecondName)) {
            ((TextView) findViewById(R.id.TextViewMotherSecondName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherSecondName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidLastName(motherLastName)) {
            ((TextView) findViewById(R.id.TextViewMotherLastName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherLastName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidDistrictCode(motherDistrictCode)) {
            ((TextView) findViewById(R.id.TextViewMotherDistrictCode))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherDistrictCode))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidNumberOfChildren(motherNumberOfChildren)) {
            ((TextView) findViewById(R.id.TextViewMotherNumberOfChildren))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherNumberOfChildren))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidMotherAge(motherAge)) {
            ((TextView) findViewById(R.id.TextViewMotherAge))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewMotherAge))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Father Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(fatherFirstName)) {
            ((TextView) findViewById(R.id.TextViewFatherFirstName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewFatherFirstName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSecondName(fatherSecondName)) {
            ((TextView) findViewById(R.id.TextViewFatherSecondName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewFatherSecondName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidLastName(fatherLastName)) {
            ((TextView) findViewById(R.id.TextViewFatherLastName))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewFatherLastName))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidDistrictCode(fatherDistrictCode)) {
            ((TextView) findViewById(R.id.TextViewFatherDistrictCode))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewFatherDistrictCode))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Solemn Declaration Validation
        // ==========================
        if (filledById == EnumeratedFields.filledBy.Y.getId()
                || filledById == EnumeratedFields.filledBy.T.getId()) {
            if (!DataValidator.isValidFirstName(fillerFirstName)) {
                ((TextView) findViewById(R.id.TextViewFilledByFirstName))
                        .setTextAppearance(this, R.style.ErrorTextViewStyle);
                isValidInput = false;
            } else {
                ((TextView) findViewById(R.id.TextViewFilledByFirstName))
                        .setTextAppearance(
                                this,
                                android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
            }
            if (!DataValidator.isValidFirstName(fillerLastName)) {
                ((TextView) findViewById(R.id.TextViewFilledByLastName))
                        .setTextAppearance(this, R.style.ErrorTextViewStyle);
                isValidInput = false;
            } else {
                ((TextView) findViewById(R.id.TextViewFilledByLastName))
                        .setTextAppearance(
                                this,
                                android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
            }
        }

        // ==========================
        // Notice of Registrar Validation
        // ==========================

        if (!DataValidator
                .isValidRegistrationCenterCode(registrationCenterCode)) {
            ((TextView) findViewById(R.id.TextViewRegistrationCenterCode))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewRegistrationCenterCode))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidPastDate(registrationDate)) {
            ((TextView) findViewById(R.id.TextViewDateOfRegistration))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
        } else {
            ((TextView) findViewById(R.id.TextViewDateOfRegistration))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (isValidInput) {

            mBirthRecordModel = new BirthRecordModel(
                    // Form Header
                    formNumber,
                    // Child Data
                    childFirstName, childSecondName, childSexId, childBirthDate,
                    childBirthPlaceId, childBirthPlaceDistrictCode, childStateAtBirthId,
                    childTypeOfBirthId, childResidencePostCode,
                    // Mother Data
                    motherFirstName, motherSecondName, motherLastName,
                    motherCountryOfBirth, motherDistrictCode, motherNumberOfChildren, motherAge,
                    // Father Data
                    fatherFirstName, fatherSecondName, fatherLastName,
                    fatherCountryOfBirth, fatherDistrictCode,
                    // Solemn Disclosure
                    filledById, fillerFirstName, fillerLastName,
                    //Registration Center
                    registrationCenterCode, isCertificateIssuedId, registrationDate,
                    imageFilePath, uploadStatus

            );
            return mBirthRecordModel;
        }
        return null;
    }

    public void onTakePictureButtonClick(View v) {
        Intent intent = new Intent(this, CameraActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mImageFilePath = data.getStringExtra(CameraActivity.FORM_IMAGE_PATH);

                ImageView iv = (ImageView) findViewById(R.id.formPicture);
                iv.setImageURI(Uri.parse(mImageFilePath));
                iv.setVisibility(View.VISIBLE);
            }
        }
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
