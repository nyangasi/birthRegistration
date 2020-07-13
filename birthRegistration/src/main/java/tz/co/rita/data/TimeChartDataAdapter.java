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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tz.co.rita.birthregistration.R;
import tz.co.rita.constants.BirthRegistrationConstants;

/**
 * Created by mgirmaw on 1/27/2016.
 */
public class TimeChartDataAdapter {

    public static final String TAG = TimeChartDataAdapter.class.getSimpleName();

    private Context mContext;
    private ContentResolver contentResolver;
    // Holds the database object
    private SQLiteDatabase db;


    public TimeChartDataAdapter(Context context) {
        this.mContext = context;
        contentResolver = context.getContentResolver();

    }

    public String[][]getBirthRecordArray(){
        String[][] totalMaleFemaleCombinedArray = new String[4][];

        String[][] totalArray = getBirthRecordArray(TimeChartDataAdapter.RECORDS_COUNT_ALL,
                "", "", TimeChartDataAdapter.ALL_DATA_BOTH_SEX_QUERY, "A");
        String[][] maleArray = getBirthRecordArray(TimeChartDataAdapter.RECORDS_COUNT_ALL,
                "", "", TimeChartDataAdapter.ALL_DATA_MALE_SEX_QUERY, "M");
        String[][] femaleArray = getBirthRecordArray(TimeChartDataAdapter.RECORDS_COUNT_ALL,
                "", "", TimeChartDataAdapter.ALL_DATA_FEMALE_SEX_QUERY, "F");

        if(totalArray==null || totalArray[0].length==0)
            return null;
        totalMaleFemaleCombinedArray[0] = totalArray[0];
        totalMaleFemaleCombinedArray[1] = totalArray[1];

        if(maleArray !=null && maleArray[0].length == totalArray[0].length)
            totalMaleFemaleCombinedArray[2] = maleArray[1];
        else if (maleArray==null){
            String[] tempArray = new String[totalArray[0].length];
            for(int i=0; i<tempArray.length; i++){
                tempArray[i] = "0";
            }
            totalMaleFemaleCombinedArray[2] = tempArray;
        }else {
            String[] tempArray = new String[totalArray[0].length];
            int totalArrayIndex = 0;
            for (int i = 0; i < totalArray[0].length; i++) {
                if (Integer.parseInt(maleArray[totalArrayIndex][0]) == Integer.parseInt(totalArray[0][0]) + i) {
                    tempArray[i] = maleArray[totalArrayIndex][1];
                    totalArrayIndex++;
                } else {
                    tempArray[i] = "0";
                }
            }
            totalMaleFemaleCombinedArray[2] = tempArray;
        }
        if(femaleArray !=null && femaleArray[0].length == totalArray[0].length)
            totalMaleFemaleCombinedArray[3] = femaleArray[1];
        else if (femaleArray==null){
            String[] tempArray = new String[totalArray[0].length];
            for(int i=0; i<tempArray.length; i++){
                tempArray[i] = "0";
            }
            totalMaleFemaleCombinedArray[3] = tempArray;
        }else {
            String[] tempArray = new String[totalArray[0].length];
            int totalArrayIndex = 0;
            for (int i = 0; i < totalArray[0].length; i++) {
                if (Integer.parseInt(femaleArray[totalArrayIndex][0]) == Integer.parseInt(totalArray[0][0]) + i) {
                    tempArray[i] = femaleArray[totalArrayIndex][1];
                    totalArrayIndex++;
                } else {
                    tempArray[i] = "0";
                }
            }
            totalMaleFemaleCombinedArray[3] = tempArray;
        }
        return totalMaleFemaleCombinedArray;
    }



    public String[][] getBirthRecordArray(int queryType, String selectedYear, String selectedMonth, String queryString,
                                          String sex) {
        Log.i(TAG, "Query Type = " + queryType + " SelectedYear = " + selectedYear);
        try {
            db = (new BirthRegistrationProvider.BirthRegistrationDbHelper(mContext,
                    Contracts.DB_NAME, null, Contracts.DB_VERSION)).getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            Cursor cursor = null;
            String[][] birthRecordArray = null;
            int xValuesIndex = -1;
            int yValuesIndex = -1;

            switch (queryType) {
                case RECORDS_COUNT_ALL:
                    cursor = db.rawQuery(queryString,
                            null
                    );
                    xValuesIndex = cursor.getColumnIndex("year");
                    yValuesIndex = cursor.getColumnIndex("count");
                    break;
                case RECORDS_COUNT_SINGLE_YEAR:

                    if (selectedYear == null)
                        return null;
                    String sexFilter = "";
                    if (sex.equals("F")){
                        sexFilter = " AND " + Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX + " = 1";
                    }else if (sex.equals("M")){
                        sexFilter = " AND " + Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX + " = 2";
                    }
                    cursor = db.rawQuery(
                            "SELECT substr(" + Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION + ", 3, 2) AS month, "
                                    + "COUNT(" + Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + ") AS count "
                                    + "FROM " + Contracts.BirthRecord.TABLE_NAME + " "
                                    + "WHERE substr(" + Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION + ", 5, 4) LIKE "
                                    + selectedYear + sexFilter + " AND "
                                    + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + " =  "
                                    + BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED + " "
                                    + "GROUP BY month "
                                    + "ORDER BY month ASC ",
                            null
                    );
                    xValuesIndex = cursor.getColumnIndex("month");
                    yValuesIndex = cursor.getColumnIndex("count");
                default:
                    break;
            }


            if (cursor != null && cursor.getCount() > 0) {
                birthRecordArray = new String[2][cursor.getCount()];
                cursor.moveToFirst();
                birthRecordArray[0][0] = cursor.getString(xValuesIndex);
                birthRecordArray[1][0] = cursor.getString(yValuesIndex);
                int count = 0;
                while (!cursor.isLast()) {
                    count++;
                    cursor.moveToNext();
                    birthRecordArray[0][count] = cursor.getString(xValuesIndex);
                    birthRecordArray[1][count] = cursor.getString(yValuesIndex);
                }
            }
            if (cursor != null)
                cursor.close();
            return getAdjustedBirthRecordArray(queryType, birthRecordArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
        }
    }

    private String[][] getAdjustedBirthRecordArray(int queryType, String[][] birthRecordArray) {

        if (queryType == TimeChartDataAdapter.RECORDS_COUNT_ALL) {
        /* Check if there are any missing entries in the array. E.g.
            birthRecordArray[0] = { 2003, 2004, 2006};  In this case no registrations were made
            during 2005, thus the entry must be added at position 2.
             */



            if(birthRecordArray==null || birthRecordArray[0].length==0)
                return null;

            int firstValue = Integer.parseInt(birthRecordArray[0][0]);
            int lastValue = Integer.parseInt(birthRecordArray[0][birthRecordArray[0].length - 1]);
            int adjustedLength = lastValue - firstValue + 1;
            if (adjustedLength == birthRecordArray[0].length) {//No Missing Values
                return birthRecordArray;
            } else {
                int originalArrayIndex = 0;
                String[][] adjustedBirthRecordArray = new String[2][adjustedLength];
                for (int i = 0; i < adjustedBirthRecordArray[0].length; i++) {
                    if (Integer.parseInt(birthRecordArray[0][originalArrayIndex]) == firstValue + i) {
                        adjustedBirthRecordArray[0][i] = birthRecordArray[0][originalArrayIndex];
                        adjustedBirthRecordArray[1][i] = birthRecordArray[1][originalArrayIndex];
                        Log.i(TAG, "originalArrayIndex = " + originalArrayIndex + " originalArrayValue = "
                                + birthRecordArray[0][originalArrayIndex]);
                        originalArrayIndex++;
                    } else {
                        adjustedBirthRecordArray[0][i] =
                                String.valueOf(firstValue + i);
                        adjustedBirthRecordArray[1][i] = "0";
                    }
                    Log.i(TAG,
                            " i = " + i + " AdjustedBirthRecordArrayValue = " + adjustedBirthRecordArray[0][i]);
                }
                return adjustedBirthRecordArray;
            }
        } else if (queryType == TimeChartDataAdapter.RECORDS_COUNT_SINGLE_YEAR) {
            String[][] adjustedBirthRecordArray = new String[2][12];
            String[] mXValsArray = mContext.getResources().getStringArray(R.array.month_array);

            for (int i = 0; i < adjustedBirthRecordArray[0].length; i++) {
                adjustedBirthRecordArray[0][i] = mXValsArray[i];
                adjustedBirthRecordArray[1][i] = "0";
            }
            if (birthRecordArray != null || birthRecordArray[0].length > 0) {
                for (int i = 0; i < birthRecordArray[0].length; i++) {
                    if (birthRecordArray[0][i].contentEquals("01")) {
                        adjustedBirthRecordArray[1][0] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("02")) {
                        adjustedBirthRecordArray[1][1] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("03")) {
                        adjustedBirthRecordArray[1][2] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("04")) {
                        adjustedBirthRecordArray[1][3] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("05")) {
                        adjustedBirthRecordArray[1][4] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("06")) {
                        adjustedBirthRecordArray[1][5] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("07")) {
                        adjustedBirthRecordArray[1][6] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("08")) {
                        adjustedBirthRecordArray[1][7] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("09")) {
                        adjustedBirthRecordArray[1][8] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("10")) {
                        adjustedBirthRecordArray[1][9] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("11")) {
                        adjustedBirthRecordArray[1][10] = birthRecordArray[1][i];
                    } else if (birthRecordArray[0][i].contentEquals("12")) {
                        adjustedBirthRecordArray[1][11] = birthRecordArray[1][i];
                    }
                }

            }
            return adjustedBirthRecordArray;
        }
        return null;
    }

    private boolean isMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static final String YEAR_STRING_REGEX = "\\d{4}";
    private static final String MONTH_STRING_REGEX = "\\d{6}";
    public static final int RECORDS_COUNT_ALL = 0;
    public static final int RECORDS_COUNT_ALL_FEMALE = 1;
    public static final int RECORDS_COUNT_ALL_MALE = 2;
    public static final int RECORDS_COUNT_SINGLE_YEAR = 3;
    public static final int RECORDS_COUNT_SINGLE_MONTH = 4;

    public static final String ALL_DATA_BOTH_SEX_QUERY =
            "SELECT substr(" + Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION + ", 5, 4) AS year, "
                    + "COUNT(" + Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + ") AS count "
                    + "FROM " + Contracts.BirthRecord.TABLE_NAME + " "
                    + "WHERE " + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + " =  "
                    + BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED + " "
                    + "GROUP BY year "
                    + "ORDER BY year ASC ";

    public static final String ALL_DATA_MALE_SEX_QUERY =
            "SELECT substr(" + Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION + ", 5, 4) AS year, "
                    + Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX + " AS sex, "
                    + "COUNT(" + Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + ") AS count "
                    + "FROM " + Contracts.BirthRecord.TABLE_NAME + " "
                    + "WHERE sex = 2 AND "
                    + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + " =  "
                    + BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED + " "
                    + "GROUP BY year "
                    + "ORDER BY year ASC ";
    public static final String ALL_DATA_FEMALE_SEX_QUERY =
            "SELECT substr(" + Contracts.BirthRecord.COLUMN_NAME_DATE_OF_REGISTRATION + ", 5, 4) AS year, "
                    + Contracts.BirthRecord.COLUMN_NAME_CHILD_SEX + " AS sex, "
                    + "COUNT(" + Contracts.BirthRecord.COLUMN_NAME_BIRTH_RECORD_ID + ") AS count "
                    + "FROM " + Contracts.BirthRecord.TABLE_NAME + " "
                    + "WHERE sex = 1 AND "
                    + Contracts.BirthRecord.COLUMN_NAME_UPLOAD_STATUS + " =  "
                    + BirthRegistrationConstants.UPLOAD_STAUS_CONFIRMED + " "
                    + "GROUP BY year "
                    + "ORDER BY year ASC ";


}
