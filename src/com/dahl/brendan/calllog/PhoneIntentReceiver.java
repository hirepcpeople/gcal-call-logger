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

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

public class PhoneIntentReceiver extends BroadcastReceiver {
	final static private String LOGTAG = PhoneIntentReceiver.class.getSimpleName();
	class Testing extends TimerTask {
		final private Context context;
		public Testing(Context context) {
			this.context = context;
		}
		public void run() {
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			long oldId = preferences.getLong(Constants.PREFS_OLDID, -1);
//			Log.d(LOGTAG,"started");
			final Cursor cursor = context.getContentResolver().query(
					CallLog.Calls.CONTENT_URI,
					(new String[] { CallLog.Calls._ID, CallLog.Calls.DURATION,
							CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
							CallLog.Calls.NUMBER, CallLog.Calls.DATE }), null,
					null, CallLog.Calls.DEFAULT_SORT_ORDER);
//			Log.d(LOGTAG,"query");
			if (cursor.getCount() > 0) {
//				Log.d(LOGTAG,"count available");
				cursor.moveToFirst();
//				Log.d(LOGTAG,String.format("oldId: %s, newId: %s", oldId, cursor.getLong(0)));
				if (oldId != -1 && oldId != cursor.getLong(0)) {
//					Log.d(LOGTAG, String.format("oldId: %s", oldId));
					String type = null;
					String from = cursor.getString(3);
					if (TextUtils.isEmpty(from)) {
						from = PhoneNumberUtils.formatNumber(cursor.getString(4));
					} else {
						from += ": (" + PhoneNumberUtils.formatNumber(cursor.getString(4)) + ")";
					}
					int string = R.string.PREFS_INCOMING;
					long calendarId = Long.valueOf(preferences.getString(context.getString(R.string.PREFS_CALENDAR_ID), "-1"));
					if (calendarId != -1) {
						switch (cursor.getInt(2)) {
						case CallLog.Calls.INCOMING_TYPE:
							type = "Incoming";
							string = R.string.PREFS_INCOMING;
							break;
						case CallLog.Calls.MISSED_TYPE:
							type = "Missed";
							string = R.string.PREFS_MISSED;
							break;
						case CallLog.Calls.OUTGOING_TYPE:
							type = "Outgoing";
							string = R.string.PREFS_OUTGOING;
							break;
						default:
							type = String.valueOf(cursor.getInt(2));
							Log.e(LOGTAG, String.format("Unknown type: %s", type));
							break;
						}
					}
					boolean enabled = preferences.getBoolean(context.getString(string), true);
//					Log.i(LOGTAG, String.format("enabled: %s", String.valueOf(enabled)));
					if (enabled) {
						ContentValues initialValues = new ContentValues();
						initialValues.put("title", String.format("%s Call with %s", type, from));
						initialValues.put("calendar_id", calendarId);
						initialValues.put("dtstart",cursor.getLong(5));
						initialValues.put("dtend",cursor.getLong(5)+(cursor.getLong(1)*1000));
//						Log.v(LOGTAG, initialValues.getAsString("title"));
//						Log.v(LOGTAG, initialValues.getAsString("calendar_id"));
//						Log.v(LOGTAG, initialValues.getAsString("dtstart"));
//						Log.v(LOGTAG, initialValues.getAsString("dtend"));
//						Log.v(LOGTAG, DateUtils.formatDate(new Date(initialValues.getAsLong("dtstart"))));
//						Log.v(LOGTAG, DateUtils.formatDate(new Date(initialValues.getAsLong("dtend"))));
						context.getContentResolver().insert(Constants.EVENTS_URI, initialValues);
					}
				}
			}
			boolean committed = preferences.edit().putLong(Constants.PREFS_OLDID, cursor.getLong(0)).commit();
			Log.v(LOGTAG, String.format("commited: %s", String.valueOf(committed)));
		}
	}

	@Deprecated
	public static void debugIntent(Intent intent) {
		Log.d(LOGTAG, String.format("action: %s", intent.getAction()));
		Log.d(LOGTAG, String.format("data: %s", intent.getDataString()));
		for (String key : intent.getExtras().keySet()) {
			Log.d(LOGTAG, String.format("Key: %s, Val: %s", key, intent.getExtras().get(key)));
		}
		if (intent.getCategories() != null) {
			for (String cat : intent.getCategories()) {
				Log.d(LOGTAG, String.format("cat: %s", cat));
			}
		}
	}
	
	public void onReceive(Context context, Intent intent) {
//		debugIntent(intent);
		if (!"IDLE".equals(intent.getExtras().getString("state"))) {
			return;
		}
		Timer timer = new Timer();
		timer.schedule(new Testing(context), 1000*10);
	}
}
