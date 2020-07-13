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

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tz.co.rita.birthregistration.BirthRegistrationApplication;
import tz.co.rita.constants.BirthRegistrationConstants;

public class DataValidator {

    private static final int MIN_FORM_DEATH_NUMBER_LENGTH =10 ;
    private static final int MAX_FORM_DEATH_NUMBER_LENGTH = 10;
    private static final int MIN_HOURS_LENGTH = 1;
    private static final int MAX_HOURS_LENGTH = 24;
    public static int MIN_FORM_NUMBER_LENGTH = 10;
    public static int MAX_FORM_NUMBER_LENGTH = 10;
    public static int MIN_FIRST_NAME_LENGTH = 1;
    public static int MAX_FIRST_NAME_LENGTH = 20;
    public static int MIN_SECOND_NAME_LENGTH = 0;
    public static int MAX_SECOND_NAME_LENGTH = 20;
    public static int MIN_LAST_NAME_LENGTH = 1;
    public static int MAX_LAST_NAME_LENGTH = 20;
    public static int COUNTRY_CODE_LENGTH = 3;
    public static int DISTRICT_CODE_LENGTH = 3;
    public static int MIN_POST_CODE_LENGTH = 5;
    public static int MAX_POST_CODE_LENGTH = 5;
    public static int DATE_LENGTH = 8;
    public static int MIN_MOTHER_AGE = 10;
    public static int MAX_NUMBER_OF_CHILDREN = 99;
    public static int MIN_REGISTRATION_CODE_LENGTH = 8;
    public static int MAX_REGISTRATION_CODE_LENGTH = 8;
    public static int MIN_MOBILE_NO = 10;
    private static BirthRegistrationApplication
            mAppInstance = BirthRegistrationApplication.getInstance();

    public static boolean isNumber(String inputString) {
        return !inputString.matches(".*\\D.*");
    }

    public static boolean isName(String inputString) {
        return !inputString.matches(".*[\\d].*");
    }

    public static boolean isValidLength(String inputString, int minLength,
                                        int maxLength) {
        return inputString.length() >= minLength
                && inputString.length() <= maxLength;
    }

    public static boolean isValidSex(int inputId) {
        if (inputId == 0)
            return false;
        else return true;
    }


    public static boolean isValidSpinner(int inputId) {
        if (inputId == 0)
            return false;
        else return true;
    }

    public static boolean isValidDate(String passedDate)
    {
        if (!isValidPastDate(passedDate))
            return false;

        return true;
    }


    public static boolean isValidEditText(String inputString, int min, int max) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isName(inputString)
                && isValidLength(inputString, min,
                max);
    }




    public static boolean isValidRegistrantBirthDate(String birthDate, String registrationDate) {
        if (!isValidPastDate(birthDate) || !isValidPastDate(registrationDate))
            return false;

        Calendar birthCal = new GregorianCalendar();
        int day = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4)) - 1;
        int year = Integer.parseInt(birthDate.substring(4, 8));
        birthCal.set(year, month, day);

        Calendar regCal = new GregorianCalendar();
        day = Integer.parseInt(registrationDate.substring(0, 2));
        month = Integer.parseInt(registrationDate.substring(2, 4)) - 1;
        year = Integer.parseInt(registrationDate.substring(4, 8));
        regCal.set(year, month, day);

        return (regCal.getTimeInMillis() - birthCal.getTimeInMillis())
                <= (long) mAppInstance.getMaxAllowedRegistrantAge() * BirthRegistrationConstants.MILLIS_IN_A_YEAR;
    }

    public static boolean isValidPastDate(String date) {
        if (date.equals(null))
            return false;
        if (date.equals(""))
            return false;
        if (date.length() != DataValidator.DATE_LENGTH)
            return false;
        Calendar cal = new GregorianCalendar();
        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(2, 4)) - 1;
        int year = Integer.parseInt(date.substring(4, 8));
        cal.set(year, month, day);
        Calendar now = new GregorianCalendar();
        now.setTime(new Date());
        return (now.getTimeInMillis() - cal.getTimeInMillis()) >= 0; //Valid Past Date
    }

    public static boolean isValidDistrictCode(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isNumber(inputString)
                && inputString.length() == DISTRICT_CODE_LENGTH;
    }

    public static boolean isValidPostCode(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isNumber(inputString)
                && isValidLength(inputString, MIN_POST_CODE_LENGTH,
                MAX_POST_CODE_LENGTH);
    }


    public static boolean isValidHours(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isNumber(inputString)
                && isValidLength(inputString, MIN_HOURS_LENGTH,
                MAX_HOURS_LENGTH);
    }

    public static boolean isValidFirstName(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isName(inputString)
                && isValidLength(inputString, MIN_FIRST_NAME_LENGTH,
                MAX_FIRST_NAME_LENGTH);
    }

    public static boolean isValidSecondName(String inputString) {
        if (inputString.equals(null))
            return true;
        if (inputString.equals(""))
            return true;
        return isName(inputString)
                && isValidLength(inputString, MIN_SECOND_NAME_LENGTH,
                MAX_SECOND_NAME_LENGTH);
    }

    public static boolean isValidLastName(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isName(inputString)
                && isValidLength(inputString, MIN_LAST_NAME_LENGTH,
                MAX_LAST_NAME_LENGTH);
    }


    public static boolean isValidMobileNumber(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isName(inputString)
                && isValidLength(inputString, MIN_MOBILE_NO,
                1000);
    }


    public static boolean isValidNumberOfChildren(int numberOfChildren) {
        return numberOfChildren <= MAX_NUMBER_OF_CHILDREN;
    }

    public static boolean isValidMotherAge(int age) {

        return age >= MIN_MOTHER_AGE;
    }

    public static boolean isValidRegistrationCenterCode(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return inputString.matches("\\d\\d\\d\\d\\d\\d\\d[[L][D][H]]")
                && isValidLength(inputString, MIN_REGISTRATION_CODE_LENGTH,
                MAX_REGISTRATION_CODE_LENGTH); //0000000aa
    }


    public static boolean isValidFormNumber(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        if (!isNumber(inputString))
            return false;
        long startingFormNumber =
                mAppInstance.getStartingFormNumber();
        long endingFormNumber =
                mAppInstance.getEndingFormNumber();
        if (Long.valueOf(inputString) < startingFormNumber || Long.valueOf(inputString) > endingFormNumber)
            return false;
        return isValidLength(inputString, MIN_FORM_NUMBER_LENGTH,
                MAX_FORM_NUMBER_LENGTH);
    }


    public static boolean isbirthRegistartionFormNumber(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        if (!isNumber(inputString))
            return false;
        return isValidLength(inputString, MIN_FORM_NUMBER_LENGTH,
                MAX_FORM_NUMBER_LENGTH);
    }

    public static boolean isValidDeathFormNumber(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        if (!isNumber(inputString))
            return false;
        long startingFormNumber =
                mAppInstance.getStartingDeathFormNumber();
        long endingFormNumber =
                mAppInstance.getEndingDeathFormNumber();
        Log.i("startingFormNumber", startingFormNumber + "");
        Log.i("endingFormNumber", endingFormNumber + "");
        Log.i("inputString", inputString + "");
        if (Long.valueOf(inputString) < startingFormNumber || Long.valueOf(inputString) > endingFormNumber)
            return false;
        Log.i("inputString", inputString + "");
       return isValidLength(inputString, MIN_FORM_DEATH_NUMBER_LENGTH,
                MAX_FORM_DEATH_NUMBER_LENGTH);

      //  return true;
    }


    public static  boolean checkIFDeathGereshoNoRangeHasBeenSet()
    {
        long startingFormNumber =
                mAppInstance.getStartingDeathFormNumber();

        long endingFormNumber =
                mAppInstance.getEndingDeathFormNumber();

        Log.i("startingFormNumber", startingFormNumber + "");
        Log.i("endingFormNumber", endingFormNumber + "");

        if(startingFormNumber == 0 && endingFormNumber==0)
        {
            return false;
        }

        return true;
    }


    public static  boolean checkIfGereshoLakituoHasBeenSet()
    {
        String gereshoLakituo =
                mAppInstance.getRegistrationCenterCode();

        Log.i("gereshoLakituo", gereshoLakituo + "");


        if(gereshoLakituo.length() < 1 )
        {
            return false;
        }

        return true;
    }

    public static boolean isValidGereshoLaKituo( ) {
        String inputString= mAppInstance.getRegistrationCenterCode();
        Log.i("gereshoLakituo", inputString + "");
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return inputString.matches("\\d\\d\\d\\d\\d\\d\\d[[L][D][H]]")
                && isValidLength(inputString, MIN_REGISTRATION_CODE_LENGTH,
                MAX_REGISTRATION_CODE_LENGTH); //0000000aa
    }

    public static boolean isValidCountryCode(String inputString) {
        if (inputString.equals(null))
            return false;
        if (inputString.equals(""))
            return false;
        return isValidLength(inputString, COUNTRY_CODE_LENGTH,
                COUNTRY_CODE_LENGTH);
    }

}
