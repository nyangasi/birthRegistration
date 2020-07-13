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
package tz.co.rita.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import tz.co.rita.utils.PhoneStateChangeListener;

public class PhoneStateChangeListenerService extends Service {
    TelephonyManager tManager;
    public PhoneStateChangeListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tManager.listen(new PhoneStateChangeListener(this),
                PhoneStateChangeListener.LISTEN_SERVICE_STATE);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {

    }

}
