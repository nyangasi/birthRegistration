package tz.co.rita.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import tz.co.rita.birthregistration.BirthRegistrationApplication;
import tz.co.rita.birthregistration.DashBoardActivity;
import tz.co.rita.constants.BirthRegistrationConstants;

/**
 * Created by ucc-ian on 13/Jun/2018.
 */
public class DeathRecordModel {


    private int uploadStatus;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    private int recordId;
    private SharedPreferences mSharedPreference;

    private final String formNumber;
    private final String deceaseFirstName;
    private final String deceaseSecondName;
    private final String deceaseLastName;
    private final String deceaseOtherName;
    private final int natID;
    private final String deceaseNatId;
    private final String deceaseTelNo;
    private final int deceaseSex;
    private final String deceaseDOD;
    private final String deceaseBOD;
    private final String deceaseTimeAlive;
    private final String deceaseMotherFirstName;
    private final String deceaseMotherSecondName;
    private final String deceaseMotherLastName;
    private final int motherNatID;
    private final String deceaseMotherNatId;
    private final String deceaseMotherMobileNo;
    private final String deceaseMotherEmail;
    private final String deceaseFatherFirstName;
    private final String deceaseFatherSecondName;
    private final String deceaseFatherLastName;
    private final int fatherNatID;
    private final String deceaseFatherNatId;
    private final String deceaseFatherMobileNo;
    private final String deceaseFatherEmail;
    private final int deacesDeathLocation;
    private final int deacesTypeOfHF;
    private final String hfGereshoNumber;
    private final String deceasedLocation;
    private final String deceaseBeforeLocation;
    private final int deacesMaritalStatus;
    private final int deacesOccupation;
    private final int deacesEducation;
    private final int deceaseBON;
    private final int deacesBirthRegistration;
    private final String deacesBirthRegistrationNo;
    private final String deacesMotherVillage;
    private final String deacesMotherLocation;
    private final int deacesRODVerified;
    private final String deacesReassonOfDeath;
    private final int deacesDeathManner;
    private final int deacesAutospy;
    private final int deacesPregnancyStatus;
    private final int deacesPregnancyAge;
    private final String informatFirstName;
    private final String informatSecondName;
    private final String informatLastName;
    private final int informatNatID;
    private final String informatNatId;
    private final String informatMobileNo;
    private final String informatEmail;
    private final String informatLocation;
    private final int deacesRelationship;
    private final String regAssistantFirstName;
    private final String regAssistantSecName;
    private final String regAssistantLastName;
    private final String facilityName;
    private final String facilityGereshoNo;
    private final String deceasedLocationVillage;
    private final String deceasedBeforeLocationVillage;
    private final String informatLocationVillage;
    private final String informatDate;
    private final String registerDate;


    private static BirthRegistrationApplication
            mAppInstance = BirthRegistrationApplication.getInstance();

    public String getRegisterDate() {
        return registerDate;
    }



    SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");

    public String getInformatDate() {
        return informatDate;
    }


    public String getInformatLocationVillage() {
        return informatLocationVillage;
    }


    public String getDeceasedLocationVillage() {
        return deceasedLocationVillage;
    }

    public String getDeceasedBeforeLocationVillage() {
        return deceasedBeforeLocationVillage;
    }


    public String getFormNumber() {
        return formNumber;
    }

    public String getDeceaseFirstName() {
        return deceaseFirstName;
    }

    public String getDeceaseSecondName() {
        return deceaseSecondName;
    }

    public String getDeceaseLastName() {
        return deceaseLastName;
    }

    public String getDeceaseOtherName() {
        return deceaseOtherName;
    }

    public int getNatID() {
        return natID;
    }

    public String getDeceaseNatId() {
        return deceaseNatId;
    }

    public String getDeceaseTelNo() {
        return deceaseTelNo;
    }

    public int getDeceaseSex() {
        return deceaseSex;
    }

    public String getDeceaseDOD() {
        return deceaseDOD;
    }

    public String getDeceaseBOD() {
        return deceaseBOD;
    }

    public String getDeceaseTimeAlive() {
        return deceaseTimeAlive;
    }

    public String getDeceaseMotherFirstName() {
        return deceaseMotherFirstName;
    }

    public String getDeceaseMotherSecondName() {
        return deceaseMotherSecondName;
    }

    public String getDeceaseMotherLastName() {
        return deceaseMotherLastName;
    }

    public int getMotherNatID() {
        return motherNatID;
    }

    public String getDeceaseMotherNatId() {
        return deceaseMotherNatId;
    }

    public String getDeceaseMotherMobileNo() {
        return deceaseMotherMobileNo;
    }

    public String getDeceaseMotherEmail() {
        return deceaseMotherEmail;
    }

    public String getDeceaseFatherFirstName() {
        return deceaseFatherFirstName;
    }

    public String getDeceaseFatherSecondName() {
        return deceaseFatherSecondName;
    }

    public String getDeceaseFatherLastName() {
        return deceaseFatherLastName;
    }

    public int getFatherNatID() {
        return fatherNatID;
    }

    public String getDeceaseFatherNatId() {
        return deceaseFatherNatId;
    }

    public String getDeceaseFatherMobileNo() {
        return deceaseFatherMobileNo;
    }

    public String getDeceaseFatherEmail() {
        return deceaseFatherEmail;
    }

    public int getDeacesDeathLocation() {
        return deacesDeathLocation;
    }

    public int getDeacesTypeOfHF() {
        return deacesTypeOfHF;
    }

    public String getHfGereshoNumber() {
        return hfGereshoNumber;
    }

    public String getDeceasedLocation() {
        return deceasedLocation;
    }

    public String getDeceaseBeforeLocation() {
        return deceaseBeforeLocation;
    }

    public int getDeacesMaritalStatus() {
        return deacesMaritalStatus;
    }

    public int getDeacesOccupation() {
        return deacesOccupation;
    }

    public int getDeacesEducation() {
        return deacesEducation;
    }

    public int getDeceaseBON() {
        return deceaseBON;
    }

    public int getDeacesBirthRegistration() {
        return deacesBirthRegistration;
    }

    public String getDeacesBirthRegistrationNo() {
        return deacesBirthRegistrationNo;
    }

    public String getDeacesMotherVillage() {
        return deacesMotherVillage;
    }

    public String getDeacesMotherLocation() {
        return deacesMotherLocation;
    }

    public int getDeacesRODVerified() {
        return deacesRODVerified;
    }

    public String getDeacesReassonOfDeath() {
        return deacesReassonOfDeath;
    }

    public int getDeacesDeathManner() {
        return deacesDeathManner;
    }

    public int getDeacesAutospy() {
        return deacesAutospy;
    }

    public int getDeacesPregnancyStatus() {
        return deacesPregnancyStatus;
    }

    public int getDeacesPregnancyAge() {
        return deacesPregnancyAge;
    }

    public String getInformatFirstName() {
        return informatFirstName;
    }

    public String getInformatSecondName() {
        return informatSecondName;
    }

    public String getInformatLastName() {
        return informatLastName;
    }

    public int getInformatNatID() {
        return informatNatID;
    }

    public String getInformatNatId() {
        return informatNatId;
    }

    public String getInformatMobileNo() {
        return informatMobileNo;
    }

    public String getInformatEmail() {
        return informatEmail;
    }

    public String getInformatLocation() {
        return informatLocation;
    }

    public int getDeacesRelationship() {
        return deacesRelationship;
    }

    public String getRegAssistantFirstName() {
        return regAssistantFirstName;
    }

    public String getRegAssistantSecName() {
        return regAssistantSecName;
    }

    public String getRegAssistantLastName() {
        return regAssistantLastName;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getFacilityGereshoNo() {
        return facilityGereshoNo;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }


    // Constructor with new Data
    public DeathRecordModel(
            String formNumber,
            String deceaseFirstName, String deceaseSecondName, String deceaseLastName, String deceaseOtherName,
            int natID, String deceaseNatId, String deceaseTelNo,
            int deceaseSex, String deceaseDOD,
            String deceaseBOD, String deceaseTimeAlive, String deceaseMotherFirstName,
            String deceaseMotherSecondName, String deceaseMotherLastName, int motherNatID, String deceaseMotherNatId,
            String deceaseMotherMobileNo, String deceaseMotherEmail, String deceaseFatherFirstName,
            String deceaseFatherSecondName, String deceaseFatherLastName,
            int fatherNatID, String deceaseFatherNatId, String deceaseFatherMobileNo,
            String deceaseFatherEmail, int deacesDeathLocation, int deacesTypeOfHF,
            String hfGereshoNumber, String deceasedLocationVillage, String deceasedLocation, String deceasedBeforeLocationVillage, String deceaseBeforeLocation,
            int deacesMaritalStatus, int deacesOccupation, int deacesEducation, int deceaseBON,
            int deacesBirthRegistration, String deacesBirthRegistrationNo, String deacesMotherVillage,
            String deacesMotherLocation, int deacesRODVerified,
            String deacesReassonOfDeath, int deacesDeathManner, int deacesAutospy,
            int deacesPregnancyStatus, int deacesPregnancyAge, String informatFirstName,
            String informatSecondName, String informatLastName, int informatNatID,
            String informatNatId, String informatMobileNo, String informatEmail, String informatLocationVillage, String informatLocation,
            String informatDate, int deacesRelationship, String regAssistantFirstName, String regAssistantSecName,
            String regAssistantLastName, String facilityName,
            String facilityGereshoNo,
            String registerDate

    ) {


        this.formNumber = formNumber;

        this.deceasedLocationVillage = deceasedLocationVillage;
        this.deceasedBeforeLocationVillage = deceasedBeforeLocationVillage;
        this.deceaseFirstName = deceaseFirstName;
        this.deceaseSecondName = deceaseSecondName;
        this.deceaseLastName = deceaseLastName;
        this.deceaseOtherName = deceaseOtherName;
        this.natID = natID;
        this.deceaseNatId = deceaseNatId;
        this.deceaseTelNo = deceaseTelNo;
        this.deceaseSex = deceaseSex;
        this.deceaseDOD = deceaseDOD;
        this.deceaseBOD = deceaseBOD;
        this.deceaseTimeAlive = deceaseTimeAlive;
        this.deceaseMotherFirstName = deceaseMotherFirstName;
        this.deceaseMotherSecondName = deceaseMotherSecondName;
        this.deceaseMotherLastName = deceaseMotherLastName;
        this.motherNatID = motherNatID;
        this.deceaseMotherNatId = deceaseMotherNatId;
        this.deceaseMotherMobileNo = deceaseMotherMobileNo;
        this.deceaseMotherEmail = deceaseMotherEmail;
        this.deceaseFatherFirstName = deceaseFatherFirstName;
        this.deceaseFatherSecondName = deceaseFatherSecondName;
        this.deceaseFatherLastName = deceaseFatherLastName;
        this.fatherNatID = fatherNatID;
        this.deceaseFatherNatId = deceaseFatherNatId;
        this.deceaseFatherMobileNo = deceaseFatherMobileNo;
        this.deceaseFatherEmail = deceaseFatherEmail;
        this.deacesDeathLocation = deacesDeathLocation;
        this.deacesTypeOfHF = deacesTypeOfHF;
        this.hfGereshoNumber = hfGereshoNumber;
        this.deceasedLocation = deceasedLocation;
        this.deceaseBeforeLocation = deceasedLocation;
        this.deacesMaritalStatus = deacesMaritalStatus;
        this.deacesOccupation = deacesOccupation;
        this.deacesEducation = deacesEducation;
        this.deceaseBON = deceaseBON;
        this.deacesBirthRegistration = deacesBirthRegistration;
        this.deacesBirthRegistrationNo = deacesBirthRegistrationNo;
        this.deacesMotherVillage = deacesMotherVillage;
        this.deacesMotherLocation = deacesMotherLocation;
        this.deacesRODVerified = deacesRODVerified;
        this.deacesReassonOfDeath = deacesReassonOfDeath;
        this.deacesDeathManner = deacesDeathManner;
        this.deacesAutospy = deacesAutospy;
        this.deacesPregnancyStatus = deacesPregnancyStatus;
        this.deacesPregnancyAge = deacesPregnancyAge;
        this.informatFirstName = informatFirstName;
        this.informatSecondName = informatSecondName;
        this.informatLastName = informatLastName;
        this.informatNatID = informatNatID;
        this.informatNatId = informatNatId;
        this.informatMobileNo = informatMobileNo;
        this.informatEmail = informatEmail;
        this.informatLocation = informatLocation;
        this.deacesRelationship = deacesRelationship;
        this.regAssistantFirstName = regAssistantFirstName;
        this.regAssistantSecName = regAssistantSecName;
        this.regAssistantLastName = regAssistantLastName;
        this.facilityName = facilityName;
        this.facilityGereshoNo = facilityGereshoNo;
        this.informatLocationVillage = informatLocationVillage;
        this.informatDate = informatDate;
        this.registerDate=registerDate;


    }


    public Uri saveDeathRecord(ContentResolver contentResolver, int saveMode) {
        ContentValues values = new ContentValues();
        values.put(Contracts.DeathRecord.COLUMN_NAME_FORM_NUMBER, getFormNumber());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FIRST_NAME, getDeceaseFirstName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_SECOND_NAME, getDeceaseSecondName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_LAST_NAME, getDeceaseLastName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_OTHER_NAME, getDeceaseOtherName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_NAT_ID_TYPE, getNatID());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_NAT_ID, getDeceaseNatId());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOB_NO, getDeceaseTelNo());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_SEX, getDeceaseSex());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_BIRTH_DATE, getDeceaseBOD());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_DEATH_DATE, getDeceaseDOD());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_HOURS_SURVIVED, getDeceaseTimeAlive());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_FIRST_NAME, getDeceaseMotherFirstName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_SECOND_NAME, getDeceaseMotherSecondName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_LAST_NAME, getDeceaseMotherLastName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_NAT_ID_TYPE, getMotherNatID());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_NAT_ID, getDeceaseMotherNatId());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_MOB_NO, getDeceaseMotherMobileNo());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_EMAIL, getDeceaseMotherEmail());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_FIRST_NAME, getDeceaseFatherFirstName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_SECOND_NAME, getDeceaseFatherSecondName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_LAST_NAME, getDeceaseFatherLastName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_NAT_ID_TYPE, getFatherNatID());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_NAT_ID, getDeceaseFatherNatId());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_MOB_NO, getDeceaseFatherMobileNo());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_FATHER_EMAIL, getDeceaseFatherEmail());
        values.put(Contracts.DeathRecord.COLUMN_NAME_PLACE_OF_DEATH, getDeacesDeathLocation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_TYPE_OF_HF, getDeacesTypeOfHF());
        values.put(Contracts.DeathRecord.COLUMN_NAME_HF_GERESHO_NO, getHfGereshoNumber());
        values.put(Contracts.DeathRecord.COLUMN_NAME_ADT_DECEASE_VILLAGE, getDeceasedLocationVillage());
        values.put(Contracts.DeathRecord.COLUMN_NAME_ADT_DECEASE_POSTCODE, getDeceasedLocation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_USUAL_DECEASE_VILLAGE, getDeceasedBeforeLocationVillage());
        values.put(Contracts.DeathRecord.COLUMN_NAME_USUAL_DECEASE_POSTCODE, getDeceaseBeforeLocation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MARITAL_STATUS, getDeacesMaritalStatus());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_OCCUPATION, getDeacesOccupation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_EDUCATION, getDeacesEducation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_COUNRTY_OF_BIRTH, getDeceaseBON());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_BIRTH_REGISTERED, getDeacesBirthRegistration());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_BIRTH_REGISTERED_NO, getDeacesBirthRegistrationNo());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_VILLAGE, getDeacesMotherVillage());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_MOTHER_POSTCODE, getDeacesMotherLocation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DEATH_CAUSE_CERTIFIED, getDeacesRODVerified());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DEATH_CAUSE, getDeacesReassonOfDeath());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DEATH_MANNER, getDeacesDeathManner());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DEATH_AUTOPSY, getDeacesAutospy());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_PREGNANCY, getDeacesPregnancyStatus());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_PREGNANCY_AGE, getDeacesPregnancyAge());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_FIRST_NAME, getInformatFirstName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_SECOND_NAME, getInformatSecondName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_LAST_NAME, getInformatLastName());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_NAT_ID_TYPE, getInformatNatID());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_NAT_ID, getInformatNatId());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_MOB_NO, getInformatMobileNo());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_EMAIL, getInformatEmail());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_VILLAGE, getInformatLocationVillage());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_POSTCODE, getInformatLocation());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_RELATIONSHIP, getDeacesRelationship());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DECEASE_INFORMANT_DATE, getInformatDate());
        //   values.put(Contracts.DeathRecord.COLUMN_NAME_FILLED_BY, getF());
        //   values.put(Contracts.DeathRecord.COLUMN_NAME_FILLED_BY_FIRST_NAME, getFillerFirstName());
//        values.put(Contracts.DeathRecord.COLUMN_NAME_REGISTRATION_CENTER_CODE, getFacilityGereshoNo());
        //values.put(Contracts.DeathRecord.COLUMN_NAME_CERTIFICATE_ISSUED, getIsCertificateIssuedId());
        values.put(Contracts.DeathRecord.COLUMN_NAME_DATE_OF_REGISTRATION, df.format(new Date()));
        //values.put(Contracts.DeathRecord.COLUMN_NAME_IMAGE_FILE_PATH, getImageFilePath());
        //values.put(Contracts.DeathRecord.COLUMN_NAME_UPLOAD_STATUS, getUploadStatus());
        //  Log.i("BR Model", " " + getUploadStatus());

        if (saveMode == DashBoardActivity.RECORD_VIEW_MODE_NEW) {
            Uri uri = contentResolver.insert(Contracts.DeathRecord.CONTENT_URI,
                    values);
            this.recordId = Integer.parseInt(uri.getLastPathSegment());
            Log.i("Data Saved ",this.recordId + "");
            return uri;
        } else if (saveMode == DashBoardActivity.RECORD_VIEW_MODE_EDIT) {
           /* String[] idSelector = {String.valueOf(this.getRecordId())};
            contentResolver.update(Contracts.DeathRecord.CONTENT_URI, values,
                    Contracts.DeathRecord.COLUMN_NAME_BIRTH_RECORD_ID + "=?",
                    idSelector);
            return Uri.parse(Contracts.BirthRecord.CONTENT_URI + "/"
                    + this.getRecordId());
                    */
            return null;
        } else
            return null;
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
        mmform = BirthRegistrationConstants.MDEATH + ":" //1
                + Integer.toHexString(currentUploadCount) + SEP //2
                + Integer.toHexString(cellId) + SEP //3
                + getFormNumber() + SEP //4
                + getDeceaseFirstName() + SEP //5
                + getDeceaseSecondName() + SEP //6
                + getDeceaseLastName() + SEP //7
                + getDeceaseOtherName() + SEP //8
                + getNatID() + SEP //9
                + getDeceaseNatId() + SEP //10
                + getDeceaseTelNo() + SEP //11
                + getDeceaseSex() + SEP //12
                + getDeceaseDOD() + SEP //13
                + getDeceaseBOD() + SEP //14
                + getDeceaseTimeAlive() + SEP //15
                + getDeceaseMotherFirstName() + SEP //16
                + getDeceaseMotherSecondName() + SEP //17
                + getDeceaseMotherLastName() + SEP //18
                + getMotherNatID() + SEP //19
                + getDeceaseMotherNatId() + SEP //20
                + getDeceaseMotherMobileNo() + SEP //21
                + getDeceaseMotherEmail() + SEP //22
                + getDeceaseFatherFirstName() + SEP //23
                + getDeceaseFatherSecondName() + SEP //24
                + getDeceaseFatherLastName() + SEP //25
                + getFatherNatID() + SEP //26
                + getDeceaseFatherNatId() + SEP //27
                + getDeceaseFatherMobileNo() + SEP //28
                + getDeceaseFatherEmail() + SEP //29
                + getDeacesDeathLocation() + SEP //30
                + getDeacesTypeOfHF() + SEP //31
                + mAppInstance.getRegistrationCenterCode() + SEP //32
                + getDeceasedLocationVillage() + SEP //33
                + getDeceasedLocation() + SEP //34
                + getDeceasedBeforeLocationVillage() + SEP //35
                + getDeceaseBeforeLocation() + SEP //36
                + getDeacesMaritalStatus() + SEP //37
                + getDeacesOccupation() + SEP //38
                + getDeacesEducation() + SEP //39
                + getDeceaseBON() + SEP //40
                + getDeacesBirthRegistration() + SEP //41
                + getDeacesBirthRegistrationNo() + SEP //42
                + getDeacesMotherVillage() + SEP //43
                + getDeacesMotherLocation() + SEP //44
                + getDeacesRODVerified() + SEP //45
                + getDeacesReassonOfDeath() + SEP //46
                + getDeacesDeathManner() + SEP //47
                + getDeacesAutospy() + SEP //48
                + getDeacesPregnancyStatus() + SEP //49
                + getDeacesPregnancyAge() + SEP //50
                + getInformatFirstName() + SEP //51
                + getInformatSecondName() + SEP //52
                + getInformatLastName() + SEP //53
                + getInformatNatID() + SEP //54
                + getInformatNatId() + SEP //55
                + getInformatMobileNo() + SEP //56
                + getInformatEmail() + SEP //57
                + getDeacesRelationship() + SEP //58
                + getInformatLocationVillage() + SEP //59
                + getInformatLocation() + SEP //60
                + getInformatDate() + SEP //61
                + getRegAssistantFirstName() + SEP //62
                + getRegAssistantSecName() + SEP //63
                + getRegAssistantLastName() + SEP //64
                + getRegisterDate() + SEP; //65

        return mmform;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Uri updateUploadStatusToDb(ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(Contracts.DeathRecord.COLUMN_NAME_UPLOAD_STATUS, getUploadStatus());
        String[] idSelector = {String.valueOf(this.getRecordId())};
        contentResolver.update(Contracts.DeathRecord.CONTENT_URI, values,
                Contracts.DeathRecord.COLUMN_NAME_DEATH_RECORD_ID + "=?",
                idSelector);
        return Uri.parse(Contracts.DeathRecord.CONTENT_URI + "/"
                + this.getRecordId());
    }

}
