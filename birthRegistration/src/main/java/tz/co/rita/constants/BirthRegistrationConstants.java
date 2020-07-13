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
package tz.co.rita.constants;

/**
 * Constants used by the application.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 */
public class BirthRegistrationConstants {
    public static String SMS_ADDRESS = "15036";
    //public static String SMS_ADDRESS = "+255712350385";
    //public static String UPLOAD_URL =  "http://192.168.43.40/url-receive.php";
    public static String UPLOAD_URL = "http://41.188.155.187:8080/scripts/url-receive1.php";

    public static final String PREFERENCE_NAME = "global_preference";
    public static final String PASSWORD_RESET_ENABLED = "password_reset_enabled";
    public static final String APP_LOCKED = "app_locked";
    public static final String APP_INVALID = "app_invalid";

    public static final String PASSWORD_RESET_REQUEST = "rqstpassrst";

    public static final String MBIRH_V2_PREFIX = "mbenc";
    public static final String MBIRTH = "mbirth";
    public static final String MDEATH = "mdeath";
    public static final String APP = "appl";
    public static final String APP_LOCK = "lock";
    public static final String APP_UN_LOCK = "ulck";
    public static final String APP_WIPE = "wipe";
    public static final String INITIAL_RUN = "initial_run";

    public static final String USER = "user";

    public static final String USER_VERIFY_ACCEPTED = "accd";
    public static final String USER_VERIFY_VERIFIED = "vrfd";
    public static final String USER_PASSWORD_RESET = "rst";

    public static final String USER_ACCT_DISABLE = "dsbl";
    public static final String USER_ACCT_ENABLE = "enbl";

    public static final String SEPARATOR_SYMBOL = ";";

    public static final String FORM_RECEIVE_PREFIX = "Asante fomu namba ";
    public static final String FORM_RECEIVE_SUFFIX = "imashasajiliwa";

    public static final String UPLOAD_COUNT = "upload_count";
    public static final String MAX_ALLOWED_REGISTRANT_AGE = "max_allowed_registrant_age";

    public static final int DEFAULT_MAX_ALLOWED_REGISTRANT_AGE = 1000;

    public static final int UPLOAD_STAUS_PENDING = 0;
    public static final int UPLOAD_STAUS_UNCONFIRMED = 1;
    public static final int UPLOAD_STAUS_CONFIRMED = 2;

    public static final int MAX_CONCURRENT_UPLOAD = 15;

    public static final long MILLIS_IN_A_YEAR =
            1000 // MilLiseconds in a second
            * 60 // Seconds in a minute
            * 60 // minutes in an hour
            * 24 // hours in a day
            * 366; // days in a year

}
