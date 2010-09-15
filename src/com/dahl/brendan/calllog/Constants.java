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

import java.util.Map;

import com.google.api.client.xml.XmlNamespaceDictionary;

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
	final public static Uri CALENDARS_URI = Uri.parse(String.format("content://%s/calendars", contentProvider));
	final public static Uri EVENTS_URI = Uri.parse(String.format("content://%s/events", contentProvider));
	final public static String PREFS_OLDID = "prefs_oldid";
	final public static String PREFS_ACCOUNT_NAME = "prefs_account_name";

	final public static String AUTH_TOKEN_TYPE = "lh2";
	final public static int RESULT_AUTHENTICATE = 0;
	final public static String ALLOWED_ACCOUNT_TYPE = "com.google";
	public static final XmlNamespaceDictionary NAMESPACE_DICTIONARY = new XmlNamespaceDictionary();
	static {
		Map<String, String> map = NAMESPACE_DICTIONARY.namespaceAliasToUriMap;
		map.put("", "http://www.w3.org/2005/Atom");
		map.put("atom", "http://www.w3.org/2005/Atom");
		map.put("exif", "http://schemas.google.com/photos/exif/2007");
		map.put("gd", "http://schemas.google.com/g/2005");
		map.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		map.put("georss", "http://www.georss.org/georss");
		map.put("gml", "http://www.opengis.net/gml");
		map.put("gphoto", "http://schemas.google.com/photos/2007");
		map.put("media", "http://search.yahoo.com/mrss/");
		map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
		map.put("xml", "http://www.w3.org/XML/1998/namespace");
	}

}
