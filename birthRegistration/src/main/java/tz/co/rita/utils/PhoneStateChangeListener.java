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
package tz.co.rita.utils;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;

import tz.co.rita.data.BirthRecordAdapter;

/**
 * Created by mgirmaw on 12/21/2015.
 */
public class PhoneStateChangeListener extends PhoneStateListener {

    private Context mContext;
    private BirthRecordAdapter mBirthRecordAdapter;

    public PhoneStateChangeListener(Context context) {
        mContext = context;

        mBirthRecordAdapter = new BirthRecordAdapter(mContext);
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        switch (serviceState.getState()) {
            case ServiceState.STATE_IN_SERVICE:
                mBirthRecordAdapter.uploadRecords(BirthRecordAdapter.PENDING_AND_UNCONFIRMED_RECORDS);
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                break;
            case ServiceState.STATE_POWER_OFF:
                break;
        }
    }
}
