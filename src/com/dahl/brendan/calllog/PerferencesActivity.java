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

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.admob.android.ads.AdManager;
import com.dahl.brendan.calllog.util.Logger;

public class PerferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	final static private String LOGTAG = PerferencesActivity.class.getSimpleName();
	private CharSequence PREFS_CALENDAR_ID;

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });
		View v = this.getLayoutInflater().inflate(R.layout.main, null, false);
		v.findViewById(R.id.list).setId(android.R.id.list);
		this.setContentView(v);
		PREFS_CALENDAR_ID = this.getString(R.string.PREFS_CALENDAR_ID);
		Log.v(LOGTAG,"init");
		addPreferencesFromResource(R.xml.preferences);
		setupCalendars(false);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		Log.v(LOGTAG,"added preferences listener");
	}

	private void setupCalendars(boolean log) {
		List<CharSequence> labels = null;
		List<CharSequence> values = null;
		if (log) {
			Log.v(LOGTAG,"setup_"+Constants.CALENDARS_URI.toString());
			final Cursor cursor2 = getContentResolver().query(Constants.CALENDARS_URI, (new String[] {"_id", "displayName"}), null, null, null);
			Log.v(LOGTAG, "all count:"+cursor2.getCount());
		}
		final Cursor cursor = getContentResolver().query(Constants.CALENDARS_URI, (new String[] {"_id", "displayName"}), "selected = 1 and access_level >= 500", null, null);
		if (log) {
			Log.v(LOGTAG, "usable count:"+cursor.getCount());
		}
		labels = new ArrayList<CharSequence>(cursor.getCount());
		values = new ArrayList<CharSequence>(cursor.getCount());
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			if (log) {
				Log.v(LOGTAG,"label:"+cursor.getString(1));
				Log.v(LOGTAG,"value:"+String.valueOf(cursor.getLong(0)));
			}
			labels.add(cursor.getString(1));
			values.add(String.valueOf(cursor.getLong(0)));
			cursor.moveToNext();
		}
		if (log) {
			Log.v(LOGTAG,"calendars list");
		}
		if (labels != null && values != null && labels.size() == values.size()) {
			ListPreference p = (ListPreference)this.findPreference(PREFS_CALENDAR_ID);
			p.setEntries(labels.toArray(new CharSequence[0]));
			p.setEntryValues(values.toArray(new CharSequence[0]));
			updateCalendar();
		}
		if (log) {
			Log.v(LOGTAG,"updated calendar");
		}
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.options_preferences, menu);
		menu.findItem(R.id.menu_log).setIcon(android.R.drawable.ic_menu_help);
		menu.findItem(R.id.menu_quit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_quit:
			this.finish();
			return true;
		case R.id.menu_log:
			setupCalendars(true);
			Logger.collectAndSendLog(this, LOGTAG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}