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
package tz.co.rita.uiaids;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

import tz.co.rita.birthregistration.BirthRecordActivity;
import tz.co.rita.birthregistration.DeathRegisterActivity;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		String monthString = Integer.toString(month +1);
		if (monthString.length() == 1)
			monthString = "0" + monthString;
		String dayString = Integer.toString(day);
		if (dayString.length() == 1)
			dayString = "0" + dayString;
		String textVal = dayString + monthString + Integer.toString(year);
		int viewId = this.getArguments().getInt("viewId");
		int requestOrigin = this.getArguments().getInt("requestOrigin");
		if(requestOrigin==1)
		{
			((DeathRegisterActivity) getActivity()).setEditTextValue(viewId, textVal);
		}else {
			((BirthRecordActivity) getActivity()).setEditTextValue(viewId, textVal);
		}

	}
}