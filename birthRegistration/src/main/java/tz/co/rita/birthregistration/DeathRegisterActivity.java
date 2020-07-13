package tz.co.rita.birthregistration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tz.co.rita.constants.BirthRegistrationConstants;
import tz.co.rita.data.BirthRecordModel;
import tz.co.rita.data.Contracts;
import tz.co.rita.data.DataValidator;
import tz.co.rita.data.DeathRecordModel;
import tz.co.rita.dataexchange.DataUploader;
import tz.co.rita.uiaids.DatePickerFragment;
import tz.co.rita.uiaids.EnumeratedFields;

import android.support.v4.widget.SimpleCursorAdapter;

/**
 * Created by ucc-ian on 04/Jun/2018.
 */
public class DeathRegisterActivity extends Activity {


    private DeathRecordModel mDeathRecordModel;
    private int mRecordViewMode;
    SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
    SimpleCursorAdapter mCountrySpinnerAdapter;
    private static final String DEFAULT_COUNTRY_CODE = "TZA";
    private Cursor mCountryCursor;

    private static final String[] COUNTRY_PROJECTION_FOR_SPINNER = new String[]{
            Contracts.Country.COLUMN_NAME_COUNTRY_ID,
            Contracts.Country.COLUMN_NAME_COUNTRY_CODE,
            Contracts.Country.COLUMN_NAME_COUNTRY_NAME};
    private static final String[] COUNTRY_FROM_COLUMNS = {
            Contracts.Country.COLUMN_NAME_COUNTRY_NAME
    };
    private static final int[] COUNTRY_TO_VIEWS = {android.R.id.text1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_death_register);
        Intent intent = getIntent();
        mRecordViewMode = intent.getIntExtra(DashBoardActivity.RECORD_VIEW_MODE,
                DashBoardActivity.RECORD_VIEW_MODE_NEW);
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

       if(!DataValidator.checkIFDeathGereshoNoRangeHasBeenSet())
       {
           Toast.makeText(this,
                   getResources().getString(R.string.geresho_error_message),
                   Toast.LENGTH_LONG).show();
           Intent settingIntent= new Intent(DeathRegisterActivity.this, SettingsActivity.class);
           startActivity(settingIntent);
           finish();
       }


        if(!DataValidator.checkIfGereshoLakituoHasBeenSet())
        {
            Toast.makeText(this,
                    getResources().getString(R.string.geresho_la_kituo_error_message),
                    Toast.LENGTH_LONG).show();
            Intent settingIntent= new Intent(DeathRegisterActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
            finish();
        }



        if(!DataValidator.isValidGereshoLaKituo())
        {
            Toast.makeText(this,
                    getResources().getString(R.string.geresho_la_kituo_wrong_format_error_message),
                    Toast.LENGTH_LONG).show();
            Intent settingIntent= new Intent(DeathRegisterActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
            finish();
        }
           initView();


    }


    public void onUploadButtonClick(View v) {
        int birthRecordid = 0;
        //  if (mDeathRecordModel != null)
        //     birthRecordid = this.mDeathRecordModel.getRecordId();

        mDeathRecordModel = createDeathRecord();

        if (mDeathRecordModel == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.input_error_message),
                    Toast.LENGTH_LONG).show();
        } else {
            if (this.mRecordViewMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT) {
                // this.mDeathRecordModel.setRecordId(birthRecordid);
            }
            mDeathRecordModel.saveDeathRecord(getContentResolver(),
                    this.mRecordViewMode);
            DataUploader dataUploader = new DataUploader((Context) this);
            dataUploader.UploadDeath(mDeathRecordModel);
            finish();
        }
    }


    private void initView() {
        EditText formNumber = ((EditText) findViewById(R.id.editTextFormNumber));
        EditText deceaseFirstName = ((EditText) findViewById(R.id.deceaseFirstName));
        EditText deceaseSecondName = ((EditText) findViewById(R.id.deceaseSecondName));
        EditText deceaseLastName = ((EditText) findViewById(R.id.deceaseLastName));
        EditText deceaseOtherName = ((EditText) findViewById(R.id.deceaseOtherName));
        final Spinner natID = ((Spinner) findViewById(R.id.natID));
        final LinearLayout deceaseNatId = ((LinearLayout) findViewById(R.id.deceaseNatLayout));
        natID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    deceaseNatId.setVisibility(View.GONE);
                } else {
                    deceaseNatId.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deceaseNatId.setVisibility(View.GONE);
            }
        });
        EditText deceaseTelNo = ((EditText) findViewById(R.id.deceaseTelNo));
        final EditText deceaseDOD = ((EditText) findViewById(R.id.deceaseDOD));
        final EditText deceaseDOB = ((EditText) findViewById(R.id.deceaseBOD));
        deceaseDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.i("deceaseDOBß", "has focus");
                if (hasFocus) {
                    Log.i("deceaseDOBß", "has focus");
                } else {
                    String DOD = deceaseDOD.getText().toString();
                    String DOB = deceaseDOB.getText().toString();

                    if (DOD.equalsIgnoreCase(DOB)) {
                        findViewById(R.id.deceaseTimeAliveLayout).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.deceaseTimeAliveLayout).setVisibility(View.GONE);
                    }

                    try {
                        int deceasedAge = getDiffYears(df.parse(DOB), df.parse(DOD));
                        Log.i("Years", deceasedAge + "");
                        if (deceasedAge < 1) {
                             findViewById(R.id.motherLayout).setVisibility(View.VISIBLE);
                        } else {
                            ((LinearLayout) findViewById(R.id.motherLayout)).setVisibility(View.GONE);
                        }

                        if (deceasedAge < 18) {
                            deceaseNatId.setVisibility(View.GONE);
                            findViewById(R.id.natIDLayout).setVisibility(View.GONE);
                        } else {
                            deceaseNatId.setVisibility(View.VISIBLE);
                            findViewById(R.id.natIDLayout).setVisibility(View.VISIBLE);
                        }

                        if(deceasedAge < 15)
                        {
                            findViewById(R.id.maritalStatusSection).setVisibility(View.GONE);
                        }else
                        {
                            findViewById(R.id.maritalStatusSection).setVisibility(View.VISIBLE);
                        }


                        if(deceasedAge < 2)
                        {
                            findViewById(R.id.deacesEducationSection).setVisibility(View.GONE);
                        }else
                        {
                            findViewById(R.id.deacesEducationSection).setVisibility(View.VISIBLE);
                        }


                        if(deceasedAge < 13)
                        {
                            findViewById(R.id.deacesOccupationSection).setVisibility(View.GONE);


                        }else
                        {
                            findViewById(R.id.deacesOccupationSection).setVisibility(View.VISIBLE);

                        }


                        if(deceasedAge < 12)
                        {
                            findViewById(R.id.deacesPregnancyStatusLayout).setVisibility(View.GONE);
                            findViewById(R.id.deacesPregnancyAgeLayout).setVisibility(View.GONE);

                        }else
                        {
                            findViewById(R.id.deacesPregnancyStatusLayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.deacesPregnancyAgeLayout).setVisibility(View.VISIBLE);
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        EditText deceaseTimeAlive = ((EditText) findViewById(R.id.deceaseTimeAlive));
        EditText deceaseMotherFirstName = ((EditText) findViewById(R.id.deceaseMotherFirstName));
        EditText deceaseMotherSecondName = ((EditText) findViewById(R.id.deceaseMotherSecondName));
        EditText deceaseMotherLastName = ((EditText) findViewById(R.id.deceaseMotherLastName));
        Spinner motherNatID = ((Spinner) findViewById(R.id.motherNatID));
        final LinearLayout deceaseMotherNatId = ((LinearLayout) findViewById(R.id.deceaseMotherNatIdLayout));
        motherNatID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    deceaseMotherNatId.setVisibility(View.GONE);
                } else {
                    deceaseMotherNatId.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deceaseMotherNatId.setVisibility(View.GONE);
            }
        });
        EditText deceaseMotherMobileNo = ((EditText) findViewById(R.id.deceaseMotherMobileNo));
        EditText deceaseMotherEmail = ((EditText) findViewById(R.id.deceaseMotherEmail));
        EditText deceaseFatherFirstName = ((EditText) findViewById(R.id.deceaseFatherFirstName));
        EditText deceaseFatherSecondName = ((EditText) findViewById(R.id.deceaseFatherSecondName));
        EditText deceaseFatherLastName = ((EditText) findViewById(R.id.deceaseFatherLastName));
        Spinner fatherNatID = ((Spinner) findViewById(R.id.fatherNatID));
        final LinearLayout deceaseFatherNatId = ((LinearLayout) findViewById(R.id.deceaseFatherNatIdLayout));
        fatherNatID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    deceaseFatherNatId.setVisibility(View.GONE);
                } else {
                    deceaseFatherNatId.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deceaseFatherNatId.setVisibility(View.GONE);
            }
        });
        EditText deceaseFatherMobileNo = ((EditText) findViewById(R.id.deceaseFatherMobileNo));
        EditText deceaseFatherEmail = ((EditText) findViewById(R.id.deceaseFatherEmail));
        Spinner deacesDeathLocation = ((Spinner) findViewById(R.id.deacesDeathLocation));
        final LinearLayout deacesTypeOfHF = ((LinearLayout) findViewById(R.id.deacesTypeOfHFLayout));
        final LinearLayout hfGereshoNumber = ((LinearLayout) findViewById(R.id.hfGereshoNumberLayout));
        deacesDeathLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {
                    deacesTypeOfHF.setVisibility(View.VISIBLE);
                    hfGereshoNumber.setVisibility(View.GONE);
                } else {
                    deacesTypeOfHF.setVisibility(View.GONE);
                    hfGereshoNumber.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deacesTypeOfHF.setVisibility(View.GONE);
                hfGereshoNumber.setVisibility(View.GONE);
            }
        });

        EditText deceasedLocation = ((EditText) findViewById(R.id.deceasedLocation));
        EditText deceaseBeforeLocation = ((EditText) findViewById(R.id.deceaseBeforeLocation));
        Spinner deacesMaritalStatus = ((Spinner) findViewById(R.id.deacesMaritalStatus));
        Spinner deacesOccupation = ((Spinner) findViewById(R.id.deacesOccupation));
        Spinner deacesEducation = ((Spinner) findViewById(R.id.deacesEducation));
        Spinner deceaseBON = ((Spinner) findViewById(R.id.deceaseBON));
        deceaseBON.setAdapter(mCountrySpinnerAdapter);
        final int countrySpinnerPosition = this.getCountrySpinnerPosition(DEFAULT_COUNTRY_CODE);
        deceaseBON.setSelection(countrySpinnerPosition);
        Spinner deacesBirthRegistration = ((Spinner) findViewById(R.id.deacesBirthRegistration));
        final LinearLayout deacesBirthRegistrationNo = ((LinearLayout) findViewById(R.id.deacesBirthRegistrationNoLayout));
        deacesBirthRegistration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 2 || i == 0 || i == 3) {
                    deacesBirthRegistrationNo.setVisibility(View.GONE);
                } else {
                    deacesBirthRegistrationNo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deacesBirthRegistrationNo.setVisibility(View.GONE);
            }
        });

        deceaseBON.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == countrySpinnerPosition) {

                    findViewById(R.id.deacesBirthRegistrationLayout).setVisibility(View.VISIBLE);

                } else {
                    findViewById(R.id.deacesBirthRegistrationLayout).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        EditText deacesMotherVillage = ((EditText) findViewById(R.id.deacesMotherVillage));
        EditText deacesMotherLocation = ((EditText) findViewById(R.id.deacesMotherLocation));
        Spinner deacesRODVerified = ((Spinner) findViewById(R.id.deacesRODVerified));
        final LinearLayout deacesReassonOfDeath = ((LinearLayout) findViewById(R.id.deacesReassonOfDeathLayout));
        deacesRODVerified.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {
                    deacesReassonOfDeath.setVisibility(View.VISIBLE);
                    findViewById(R.id.deacesDeathMannerTextSection).setVisibility(View.GONE);
                    findViewById(R.id.deacesAutospySection).setVisibility(View.GONE);


                } else {
                    deacesReassonOfDeath.setVisibility(View.GONE);
                    findViewById(R.id.deacesDeathMannerTextSection).setVisibility(View.VISIBLE);
                    findViewById(R.id.deacesAutospySection).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deacesReassonOfDeath.setVisibility(View.GONE);
            }
        });


        Spinner deacesDeathManner = ((Spinner) findViewById(R.id.deacesDeathManner));
        Spinner deacesAutospy = ((Spinner) findViewById(R.id.deacesAutospy));
        Spinner deceaseSex = ((Spinner) findViewById(R.id.deceaseSex));
        final LinearLayout deacesPregnancyStatusLayout = ((LinearLayout) findViewById(R.id.deacesPregnancyStatusLayout));
        final LinearLayout deacesPregnancyAge = ((LinearLayout) findViewById(R.id.deacesPregnancyAgeLayout));
        deceaseSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 2) {
                    deacesPregnancyStatusLayout.setVisibility(View.VISIBLE);
                    deacesPregnancyAge.setVisibility(View.VISIBLE);
                } else {
                    deacesPregnancyStatusLayout.setVisibility(View.GONE);
                    deacesPregnancyAge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                deacesPregnancyStatusLayout.setVisibility(View.GONE);
                deacesPregnancyAge.setVisibility(View.GONE);
            }
        });



        Spinner deacesPregnancyStatus = ((Spinner) findViewById(R.id.deacesPregnancyStatus));
        deacesPregnancyStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 1) {

                    deacesPregnancyAge.setVisibility(View.VISIBLE);
                } else {

                    deacesPregnancyAge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                deacesPregnancyAge.setVisibility(View.GONE);
            }
        });

        final EditText informatFirstName = ((EditText) findViewById(R.id.informatFirstName));
        final EditText informatSecondName = ((EditText) findViewById(R.id.informatSecondName));
        final EditText informatLastName = ((EditText) findViewById(R.id.informatLastName));
        final Spinner informatNatID = ((Spinner) findViewById(R.id.informatNatID));
        final LinearLayout informatNatId = ((LinearLayout) findViewById(R.id.informatNatIdLayout));
        informatNatID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    informatNatId.setVisibility(View.GONE);
                } else {
                    informatNatId.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                informatNatId.setVisibility(View.GONE);
            }
        });


        final EditText informatMobileNo = ((EditText) findViewById(R.id.informatMobileNo));
        final EditText informatEmail = ((EditText) findViewById(R.id.informatEmail));
        final EditText informatLocation = ((EditText) findViewById(R.id.informatLocation));
        final Spinner deacesRelationship = ((Spinner) findViewById(R.id.deacesRelationship));
        deacesRelationship.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 2 || i == 3) {
                    findViewById(R.id.informatLayout).setVisibility(View.GONE);
                }else if(i == 0 )
                {
                    findViewById(R.id.informatLayout).setVisibility(View.GONE);
                }
                else {
                    findViewById(R.id.informatLayout).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                findViewById(R.id.informatLayout).setVisibility(View.GONE);
            }
        });
        EditText regAssistantFirstName = ((EditText) findViewById(R.id.regAssistantFirstName));
        EditText regAssistantSecName = ((EditText) findViewById(R.id.regAssistantSecName));
        EditText regAssistantLastName = ((EditText) findViewById(R.id.regAssistantLastName));
        EditText facilityName = ((EditText) findViewById(R.id.facilityName));
        EditText facilityGereshoNo = ((EditText) findViewById(R.id.facilityGereshoNo));


        //

    }


    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    private DeathRecordModel createDeathRecord() {
        boolean isValidInput = true;
        // Get the Form Data
        String formNumber = ((EditText) findViewById(R.id.editTextFormNumber))
                .getText().toString();
        String deceaseFirstName = ((EditText) findViewById(R.id.deceaseFirstName))
                .getText().toString();
        String deceaseSecondName = ((EditText) findViewById(R.id.deceaseSecondName))
                .getText().toString();
        String deceaseLastName = ((EditText) findViewById(R.id.deceaseLastName))
                .getText().toString();
        String deceaseOtherName = ((EditText) findViewById(R.id.deceaseOtherName))
                .getText().toString();
        int natID = ((Spinner) findViewById(R.id.natID))
                .getSelectedItemPosition();
        String deceaseNatId = ((EditText) findViewById(R.id.deceaseNatId))
                .getText().toString();
        String deceaseTelNo = ((EditText) findViewById(R.id.deceaseTelNo))
                .getText().toString();
        int deceaseSex = ((Spinner) findViewById(R.id.deceaseSex))
                .getSelectedItemPosition();
        String deceaseDOD = ((EditText) findViewById(R.id.deceaseDOD))
                .getText().toString();
        String deceaseBOD = ((EditText) findViewById(R.id.deceaseBOD))
                .getText().toString();
        String deceaseTimeAlive = ((EditText) findViewById(R.id.deceaseTimeAlive))
                .getText().toString();
        String deceaseMotherFirstName = ((EditText) findViewById(R.id.deceaseMotherFirstName))
                .getText().toString();
        String deceaseMotherSecondName = ((EditText) findViewById(R.id.deceaseMotherSecondName))
                .getText().toString();
        String deceaseMotherLastName = ((EditText) findViewById(R.id.deceaseMotherLastName))
                .getText().toString();
        int motherNatID = ((Spinner) findViewById(R.id.motherNatID))
                .getSelectedItemPosition();
        String deceaseMotherNatId = ((EditText) findViewById(R.id.deceaseMotherNatId))
                .getText().toString();
        String deceaseMotherMobileNo = ((EditText) findViewById(R.id.deceaseMotherMobileNo))
                .getText().toString();
        String deceaseMotherEmail = ((EditText) findViewById(R.id.deceaseMotherEmail))
                .getText().toString();
        String deceaseFatherFirstName = ((EditText) findViewById(R.id.deceaseFatherFirstName))
                .getText().toString();
        String deceaseFatherSecondName = ((EditText) findViewById(R.id.deceaseFatherSecondName))
                .getText().toString();
        String deceaseFatherLastName = ((EditText) findViewById(R.id.deceaseFatherLastName))
                .getText().toString();
        int fatherNatID = ((Spinner) findViewById(R.id.fatherNatID))
                .getSelectedItemPosition();
        String deceaseFatherNatId = ((EditText) findViewById(R.id.deceaseFatherNatId))
                .getText().toString();
        String deceaseFatherMobileNo = ((EditText) findViewById(R.id.deceaseFatherMobileNo))
                .getText().toString();
        String deceaseFatherEmail = ((EditText) findViewById(R.id.deceaseFatherEmail))
                .getText().toString();
        int deacesDeathLocation = ((Spinner) findViewById(R.id.deacesDeathLocation))
                .getSelectedItemPosition();
        int deacesTypeOfHF = ((Spinner) findViewById(R.id.deacesTypeOfHF))
                .getSelectedItemPosition();
        String hfGereshoNumber = ((EditText) findViewById(R.id.hfGereshoNumber))
                .getText().toString();
        String deceasedLocationVillage = ((EditText) findViewById(R.id.deceasedLocationVillage))
                .getText().toString();
        String deceasedLocation = ((EditText) findViewById(R.id.deceasedLocation))
                .getText().toString();
        String deceasedBeforeLocationVillage = ((EditText) findViewById(R.id.deceasedBeforeLocationVillage))
                .getText().toString();
        String deceaseBeforeLocation = ((EditText) findViewById(R.id.deceaseBeforeLocation))
                .getText().toString();
        int deacesMaritalStatus = ((Spinner) findViewById(R.id.deacesMaritalStatus))
                .getSelectedItemPosition();
        int deacesOccupation = ((Spinner) findViewById(R.id.deacesOccupation))
                .getSelectedItemPosition();
        int deacesEducation = ((Spinner) findViewById(R.id.deacesEducation))
                .getSelectedItemPosition();
        int deceaseBON = ((Spinner) findViewById(R.id.deceaseBON))
                .getSelectedItemPosition();
        int deacesBirthRegistration = ((Spinner) findViewById(R.id.deacesBirthRegistration))
                .getSelectedItemPosition();
        String deacesBirthRegistrationNo = ((EditText) findViewById(R.id.deacesBirthRegistrationNo))
                .getText().toString();
        String deacesMotherVillage = ((EditText) findViewById(R.id.deacesMotherVillage))
                .getText().toString();
        String deacesMotherLocation = ((EditText) findViewById(R.id.deacesMotherLocation))
                .getText().toString();
        int deacesRODVerified = ((Spinner) findViewById(R.id.deacesRODVerified))
                .getSelectedItemPosition();
        String deacesReassonOfDeath = ((EditText) findViewById(R.id.deacesReassonOfDeath))
                .getText().toString();
        int deacesDeathManner = ((Spinner) findViewById(R.id.deacesDeathManner))
                .getSelectedItemPosition();
        int deacesAutospy = ((Spinner) findViewById(R.id.deacesAutospy))
                .getSelectedItemPosition();
        int deacesPregnancyStatus = ((Spinner) findViewById(R.id.deacesPregnancyStatus))
                .getSelectedItemPosition();
        int deacesPregnancyAge = ((Spinner) findViewById(R.id.deacesPregnancyAge))
                .getSelectedItemPosition();
        String informatFirstName = ((EditText) findViewById(R.id.informatFirstName))
                .getText().toString();
        String informatSecondName = ((EditText) findViewById(R.id.informatSecondName))
                .getText().toString();
        String informatLastName = ((EditText) findViewById(R.id.informatLastName))
                .getText().toString();
        int informatNatID = ((Spinner) findViewById(R.id.informatNatID))
                .getSelectedItemPosition();
        String informatNatId = ((EditText) findViewById(R.id.informatNatId))
                .getText().toString();
        String informatMobileNo = ((EditText) findViewById(R.id.informatMobileNo))
                .getText().toString();
        String informatEmail = ((EditText) findViewById(R.id.informatEmail))
                .getText().toString();
        String informatLocationVillage = ((EditText) findViewById(R.id.informatLocationVillage))
                .getText().toString();
        String informatLocation = ((EditText) findViewById(R.id.informatLocation))
                .getText().toString();
        String informatDate = ((EditText) findViewById(R.id.informatDate))
                .getText().toString();
        int deacesRelationship = ((Spinner) findViewById(R.id.deacesRelationship))
                .getSelectedItemPosition();
        String regAssistantFirstName = ((EditText) findViewById(R.id.regAssistantFirstName))
                .getText().toString();
        String regAssistantSecName = ((EditText) findViewById(R.id.regAssistantSecName))
                .getText().toString();
        String regAssistantLastName = ((EditText) findViewById(R.id.regAssistantLastName))
                .getText().toString();
        String facilityName = ((EditText) findViewById(R.id.facilityName))
                .getText().toString();
        String facilityGereshoNo = ((EditText) findViewById(R.id.facilityGereshoNo))
                .getText().toString();
       String  registerDate= ((EditText) findViewById(R.id.registerDate))
                .getText().toString();

        // ==========================
        // Form Number Validation
        // ==========================
        if (!DataValidator.isValidDeathFormNumber(formNumber)) {
            ((TextView) findViewById(R.id.TextViewFormNumber))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "TextViewForm");
        } else {
            ((TextView) findViewById(R.id.TextViewFormNumber))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Deceased Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(deceaseFirstName)) {
            ((TextView) findViewById(R.id.deceaseFirstNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseFirstNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseFirstNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidLastName(deceaseLastName)) {
            ((TextView) findViewById(R.id.deceaseLastNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseLastNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseLastNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSex(deceaseSex)) {
            ((TextView) findViewById(R.id.deceaseSexText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseSexText");
        } else {
            ((TextView) findViewById(R.id.deceaseSexText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidDate(deceaseDOD)) {
            ((TextView) findViewById(R.id.deceaseDODText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseDODText");
        } else {
            ((TextView) findViewById(R.id.deceaseDODText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidDate(deceaseBOD)) {
            ((TextView) findViewById(R.id.deceaseBODText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseBODText");
        } else {
            ((TextView) findViewById(R.id.deceaseBODText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }


        if (!DataValidator.isValidDate(registerDate)) {
            ((TextView) findViewById(R.id.registerDateText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "registerDate");
        } else {
            ((TextView) findViewById(R.id.registerDateText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidHours(deceaseTimeAlive) && isVisible(R.id.deceaseTimeAliveLayout)) {
            ((TextView) findViewById(R.id.deceaseTimeAliveText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseTimeAliveText");
        } else {
            ((TextView) findViewById(R.id.deceaseTimeAliveText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Deceased Mother Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(deceaseMotherFirstName)) {
            ((TextView) findViewById(R.id.deceaseMotherFirstNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseMotherFirstNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseMotherFirstNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidLastName(deceaseMotherLastName)) {
            ((TextView) findViewById(R.id.deceaseMotherLastNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseMotherLastNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseMotherLastNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Deceased Father Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(deceaseFatherFirstName)) {
            ((TextView) findViewById(R.id.deceaseFatherFirstNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseFatherFirstNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseFatherFirstNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidLastName(deceaseFatherLastName)) {
            ((TextView) findViewById(R.id.deceaseFatherLastNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseFatherLastNameText");
        } else {
            ((TextView) findViewById(R.id.deceaseFatherLastNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Death Information Data Validation
        // ==========================


        if (!DataValidator.isValidSpinner(deacesDeathLocation)) {
            ((TextView) findViewById(R.id.deacesDeathLocationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesDeathLocationText");
        } else {
            ((TextView) findViewById(R.id.deacesDeathLocationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesTypeOfHF) && findViewById(R.id.deacesTypeOfHFLayout).getVisibility() == View.VISIBLE) {
            ((TextView) findViewById(R.id.deacesTypeOfHFText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesTypeOfHFText");
        } else {
            ((TextView) findViewById(R.id.deacesTypeOfHFText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidEditText(hfGereshoNumber, 1, 1000) && isVisible(R.id.hfGereshoNumberLayout)) {
            ((TextView) findViewById(R.id.hfGereshoNumberText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "hfGereshoNumberText");
        } else {
            ((TextView) findViewById(R.id.hfGereshoNumberText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        if (!DataValidator.isValidPostCode(deceasedLocation)) {
            ((TextView) findViewById(R.id.deceasedLocationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceasedLocationText");
        } else {
            ((TextView) findViewById(R.id.deceasedLocationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidPostCode(deceaseBeforeLocation)) {
            ((TextView) findViewById(R.id.deceaseBeforeLocationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseBeforeLocationText");
        } else {
            ((TextView) findViewById(R.id.deceaseBeforeLocationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Deceased Personel Data Validation
        // ==========================


        if (!DataValidator.isValidSpinner(deacesMaritalStatus) &&  isVisible(R.id.maritalStatusSection)) {
            ((TextView) findViewById(R.id.deacesMaritalStatusText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesMaritalStatusText");
        } else {
            ((TextView) findViewById(R.id.deacesMaritalStatusText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesOccupation)&&  isVisible(R.id.deacesOccupationSection)) {
            ((TextView) findViewById(R.id.deacesOccupationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesOccupationText");
        } else {
            ((TextView) findViewById(R.id.deacesOccupationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesEducation) &&  isVisible(R.id.deacesEducationSection)) {
            ((TextView) findViewById(R.id.deacesEducationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesEducationText");
        } else {
            ((TextView) findViewById(R.id.deacesEducationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }


        if (!DataValidator.isValidSpinner(deceaseBON)) {
            ((TextView) findViewById(R.id.deceaseBONText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deceaseBONText");
        } else {
            ((TextView) findViewById(R.id.deceaseBONText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }


        if (!DataValidator.isValidSpinner(deacesBirthRegistration)) {
            ((TextView) findViewById(R.id.deacesBirthRegistrationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesBirthRegistrationText");
        } else {
            ((TextView) findViewById(R.id.deacesBirthRegistrationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isbirthRegistartionFormNumber(deacesBirthRegistrationNo) && isVisible(R.id.deacesBirthRegistrationNoLayout)) {
            ((TextView) findViewById(R.id.deacesBirthRegistrationNoText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesBirthRegistrationNoText");
        } else {
            ((TextView) findViewById(R.id.deacesBirthRegistrationNoText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        // ==========================
        // Deceased Mother Location Data Validation
        // ==========================


        if (!DataValidator.isValidEditText(deacesMotherVillage, 1, 1000) && isVisible(R.id.motherLayout)) {
            ((TextView) findViewById(R.id.deacesMotherVillageText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesMotherVillageText");
        } else {
            ((TextView) findViewById(R.id.deacesMotherVillageText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidPostCode(deacesMotherLocation) && isVisible(R.id.motherLayout)) {
            ((TextView) findViewById(R.id.deacesMotherLocationText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesMotherLocationText");
        } else {
            ((TextView) findViewById(R.id.deacesMotherLocationText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesRODVerified)) {
            ((TextView) findViewById(R.id.deacesRODVerifiedText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesRODVerifiedText");
        } else {
            ((TextView) findViewById(R.id.deacesRODVerifiedText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidEditText(deacesReassonOfDeath, 1, 1000) && isVisible(R.id.deacesReassonOfDeathLayout)) {
            ((TextView) findViewById(R.id.deacesReassonOfDeathText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesReassonOfDeathText");
        } else {
            ((TextView) findViewById(R.id.deacesReassonOfDeathText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesDeathManner) &&  isVisible(R.id.deacesDeathMannerTextSection)) {
            ((TextView) findViewById(R.id.deacesDeathMannerText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesDeathMannerText");
        } else {
            ((TextView) findViewById(R.id.deacesDeathMannerText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesAutospy) &&  isVisible(R.id.deacesAutospySection)) {
            ((TextView) findViewById(R.id.deacesAutospyText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesAutospyText");
        } else {
            ((TextView) findViewById(R.id.deacesAutospyText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesPregnancyStatus) && isVisible(R.id.deacesPregnancyStatusLayout)) {
            ((TextView) findViewById(R.id.deacesPregnancyStatusText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesPregnancyStatusText");
        } else {
            ((TextView) findViewById(R.id.deacesPregnancyStatusText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesPregnancyAge) && isVisible(R.id.deacesPregnancyAgeLayout)) {
            ((TextView) findViewById(R.id.deacesPregnancyAgeText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesPregnancyAgeText");
        } else {
            ((TextView) findViewById(R.id.deacesPregnancyAgeText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }
        // ==========================
        // Informant Data Validation
        // ==========================

        if (!DataValidator.isValidFirstName(informatFirstName) && isVisible(R.id.informatLayout)) {
            ((TextView) findViewById(R.id.informatFirstNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "informatFirstNameText");
        } else {
            ((TextView) findViewById(R.id.informatFirstNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidLastName(informatLastName) && isVisible(R.id.informatLayout)) {
            ((TextView) findViewById(R.id.informatLastNameText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "informatLastNameText");
        } else {
            ((TextView) findViewById(R.id.informatLastNameText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (!DataValidator.isValidSpinner(deacesRelationship)) {
            ((TextView) findViewById(R.id.deacesRelationshipText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "deacesRelationshipText");
        } else {
            ((TextView) findViewById(R.id.deacesRelationshipText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        Log.i("validationStatus", isValidInput + "");


        //RegistrationDateValidation
        if (!DataValidator.isValidRegistrantBirthDate(deceaseBOD,registerDate)) {
            ((TextView) findViewById(R.id.registerDateText))
                    .setTextAppearance(this, R.style.ErrorTextViewStyle);
            isValidInput = false;
            Log.i("fail", "registerDateText");
        } else {
            ((TextView) findViewById(R.id.registerDateText))
                    .setTextAppearance(
                            this,
                            android.R.style.TextAppearance_DeviceDefault_Widget_TextView);
        }

        if (isValidInput) {

            mDeathRecordModel = new DeathRecordModel(
                    formNumber,
                    deceaseFirstName, deceaseSecondName, deceaseLastName, deceaseOtherName,
                    natID, deceaseNatId, deceaseTelNo,
                    deceaseSex, deceaseDOD,
                    deceaseBOD, deceaseTimeAlive, deceaseMotherFirstName,
                    deceaseMotherSecondName, deceaseMotherLastName, motherNatID, deceaseMotherNatId,
                    deceaseMotherMobileNo, deceaseMotherEmail, deceaseFatherFirstName,
                    deceaseFatherSecondName, deceaseFatherLastName,
                    fatherNatID, deceaseFatherNatId, deceaseFatherMobileNo,
                    deceaseFatherEmail, deacesDeathLocation, deacesTypeOfHF,
                    hfGereshoNumber, deceasedLocationVillage, deceasedLocation, deceasedBeforeLocationVillage, deceaseBeforeLocation,
                    deacesMaritalStatus, deacesOccupation, deacesEducation, deceaseBON,
                    deacesBirthRegistration, deacesBirthRegistrationNo, deacesMotherVillage,
                    deacesMotherLocation, deacesRODVerified,
                    deacesReassonOfDeath, deacesDeathManner, deacesAutospy,
                    deacesPregnancyStatus, deacesPregnancyAge, informatFirstName,
                    informatSecondName, informatLastName, informatNatID,
                    informatNatId, informatMobileNo, informatEmail, informatLocationVillage, informatLocation, informatDate,
                    deacesRelationship, regAssistantFirstName, regAssistantSecName,
                    regAssistantLastName, facilityName,
                    facilityGereshoNo, registerDate

            );
            return mDeathRecordModel;
        }
        return null;
    }

    public boolean isVisible(int id) {
        if (findViewById(id).getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();

        Bundle args = new Bundle();
        args.putInt("viewId", v.getId());
        args.putInt("requestOrigin", 1);
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void setEditTextValue(int id, String value) {
        ((EditText) findViewById(id)).setText(value);
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
}
