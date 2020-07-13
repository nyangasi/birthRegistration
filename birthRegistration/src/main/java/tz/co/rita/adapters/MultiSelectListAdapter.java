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
package tz.co.rita.adapters;

import java.util.HashMap;
import java.util.Set;

import tz.co.rita.birthregistration.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Class that extends SimpleCursorAdaptor to provide functions for a user slectable list supporting
 * multiple selections.
 *
 * @author Molalgne Girmaw
 * @version 1.0.0
 * @since June, 2015
 *
 *
 */
@SuppressLint("UseSparseArrays")
public class MultiSelectListAdapter extends SimpleCursorAdapter {

	public interface MultiSelectListAdapterCallBacks {
		public void onBirthRecordItemClick(String recordId);
		public void onListItemSelectionChanged(int selectionCount, int itemCount, boolean allItemsSelected);
	}

	private MultiSelectListAdapterCallBacks mCallBack;
	private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

	public MultiSelectListAdapter(Activity activity, Context context,
			int layout, Cursor cursor, String[] fromColumns, int[] toViews,
			int flags) {
		super(context, layout, cursor, fromColumns, toViews, flags);
		try {
			mCallBack = (MultiSelectListAdapterCallBacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnBirthRecordItemClickListener");
		}

	}
	
	private boolean areAllItemsSelected(){
		return this.getCount() == this.getSelectionCount();
	}
	
	private void updateSelectAllCheckBox(){				
			mCallBack.onListItemSelectionChanged(
					getSelectionCount(),
					getCount(), 
					areAllItemsSelected());
	}
	
	public int getSelectionCount(){
		return mSelection.size();
	}

	public void setNewSelection(int position) {
		mSelection.put(position, true);		
		updateSelectAllCheckBox();
	}

	private boolean isPositionChecked(int position) {
		Boolean result = mSelection.get(position);
		return result == null ? false : result;
	}

	public Set<Integer> getCurrentCheckedPosition() {
		return mSelection.keySet();
	}
	
	public void selectAll(){
		for(int i=0; i<this.getCount(); i++){			
			setNewSelection(i);
		}
		updateSelectAllCheckBox();
		
	}

	
	public void removeSelection(int position) {
		mSelection.remove(position);
		updateSelectAllCheckBox();
	}

	public void clearSelection() {
		mSelection = new HashMap<Integer, Boolean>();
		updateSelectAllCheckBox();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		boolean shouldSetTag = convertView == null;
		final View v = super.getView(position, convertView, parent);
		if (shouldSetTag) {
			holder = new ViewHolder();
			holder.contentContainer = (LinearLayout) v.findViewById(R.id.itemContentContainer);
			holder.checkbox = (CheckBox) v.findViewById(R.id.itemCheckBox); 
			holder.idview = (TextView) v.findViewById(R.id.textViewRecordIdHolder);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		holder.id = position;
		holder.checkbox.setId(position);
		holder.checkbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View checkBoxView) {
				CheckBox cb = (CheckBox) checkBoxView;
				int id = cb.getId();
				if (isPositionChecked(id)) {
					cb.setChecked(false);
					removeSelection(id);
				} else {
					cb.setChecked(true);
					setNewSelection(id);
				}

			}
		});
		holder.contentContainer.setOnClickListener(new OnClickListener() {
			public void onClick(View containerView) {
				String recordId = ((TextView)containerView.findViewById(R.id.textViewRecordIdHolder))
						.getText().toString();
				mCallBack.onBirthRecordItemClick(recordId);
			}
		});
		return v;
	}
	
	public Integer getBirthRecordId(View containerView){
		return Integer.parseInt(((TextView)containerView.findViewById(R.id.textViewRecordIdHolder))
				.getText().toString());
	}
}

class ViewHolder {
	TextView idview;
	LinearLayout contentContainer;
	CheckBox checkbox;
	int id;
}