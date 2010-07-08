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

import android.net.Uri;
import android.os.Build;

public class Constants {
	final private static String contentProvider;
	static {
		if (Integer.parseInt(Build.VERSION.SDK) <= 7) {
			contentProvider = "calendar";
		} else {
			contentProvider = "com.android.calendar";
		}
	}
	final public static Uri CALENDARS_URI = Uri.parse(String.format("content://%s/calendars",contentProvider));
	final public static Uri EVENTS_URI = Uri.parse(String.format("content://%s/events",contentProvider));
	final public static String PREFS_OLDID = "prefs_oldid";
}
