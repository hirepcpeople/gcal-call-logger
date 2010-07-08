//    This file is part of GCal Call Logger.
//
//    Open WordSearch is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Open WordSearch is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Open WordSearch.  If not, see <http://www.gnu.org/licenses/>.
//
//	  Copyright 2010 Brendan Dahl <dahl.brendan@brendandahl.com>
//	  	http://www.brendandahl.com

package com.dahl.brendan.calllog;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class PerferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	final static private String LOGTAG = PerferencesActivity.class.getSimpleName();
	private ContentProviderClient client;
	private CharSequence PREFS_CALENDAR_ID;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		client.release();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });
		View v = this.getLayoutInflater().inflate(R.layout.main, null, false);
		v.findViewById(R.id.list).setId(android.R.id.list);
		this.setContentView(v);
		PREFS_CALENDAR_ID = this.getString(R.string.PREFS_CALENDAR_ID);
//		Log.d(LOGTAG,"init");
		client = this.getContentResolver().acquireContentProviderClient(Constants.CALENDARS_URI);
		addPreferencesFromResource(R.xml.preferences);
		setupCalendars();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
//		Log.d(LOGTAG,"added preferences listener");
	}

	private void setupCalendars() {
		List<CharSequence> labels = null;
		List<CharSequence> values = null;
//		Log.d(LOGTAG,"setup_"+Constants.CALENDARS_URI.toString());
		try {
			final Cursor cursor = client.query(Constants.CALENDARS_URI, (new String[] {
					"_id", "displayName"}), "selected = 1 and access_level >= 500", null, null);
			labels = new ArrayList<CharSequence>(cursor.getCount());
			values = new ArrayList<CharSequence>(cursor.getCount());
			cursor.moveToFirst();
			while (!cursor.isLast()) {
				labels.add(cursor.getString(1));
				values.add(String.valueOf(cursor.getLong(0)));
				cursor.moveToNext();
			}
		} catch (RemoteException e) {
			Log.v(LOGTAG, "RemoteException", e);
		}
//		Log.d(LOGTAG,"calendars list");
		if (labels != null && values != null && labels.size() == values.size()) {
			ListPreference p = (ListPreference)this.findPreference(PREFS_CALENDAR_ID);
			p.setEntries(labels.toArray(new CharSequence[0]));
			p.setEntryValues(values.toArray(new CharSequence[0]));
			updateCalendar();
		}
//		Log.d(LOGTAG,"updated calendar");
	}
	private void updateCalendar() {
		ListPreference p = (ListPreference)this.findPreference(PREFS_CALENDAR_ID);
		CharSequence calendar = p.getEntry();
		if (calendar == null) {
			p.setTitle(R.string.CALENDAR);
		} else {
			p.setTitle(calendar);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PREFS_CALENDAR_ID.equals(key)) {
			updateCalendar();
		}
	}
}