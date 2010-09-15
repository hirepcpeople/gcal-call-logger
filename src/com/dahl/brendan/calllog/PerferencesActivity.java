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

import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.admob.android.ads.AdManager;
import com.dahl.brendan.calllog.util.Logger;
import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.xml.atom.AtomParser;

public class PerferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	final static private String LOGTAG = PerferencesActivity.class.getSimpleName();
	final static private int DIALOG_ACCOUNTS = 0;
	private static HttpTransport transport;

	static class SendData {
		String fileName;
		Uri uri;
		String contentType;
		long contentLength;

		SendData(Intent intent, ContentResolver contentResolver) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = this.uri = (Uri) extras
						.getParcelable(Intent.EXTRA_STREAM);
				String scheme = uri.getScheme();
				if (scheme.equals("content")) {
					Cursor cursor = contentResolver.query(uri, null, null,
							null, null);
					cursor.moveToFirst();
					this.fileName = cursor.getString(cursor
							.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME));
					this.contentType = intent.getType();
					this.contentLength = cursor.getLong(cursor
							.getColumnIndexOrThrow(Images.Media.SIZE));
				}
			}
		}
	}

	static SendData sendData;
	private String authToken;
	private String postLink;
	private CharSequence PREFS_CALENDAR_ID;

	public PerferencesActivity() {
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		transport = GoogleTransport.create();
		GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName("google-picasaandroidsample-1.0");
		headers.gdataVersion = "2";
		AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Constants.NAMESPACE_DICTIONARY;
		transport.addParser(parser);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_ACCOUNTS: {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Select a Google account");
	        final AccountManager manager = AccountManager.get(this);
	        final Account[] accounts = manager.getAccountsByType(Constants.ALLOWED_ACCOUNT_TYPE);
	        final int size = accounts.length;
	        String[] names = new String[size];
	        for (int i = 0; i < size; i++) {
	          names[i] = accounts[i].name;
	        }
	        builder.setItems(names, new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int which) {
	            gotAccount(manager, accounts[which]);
	          }
	        });
	        dialog = builder.create();
			break;
		}
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	private void calendarSetup(boolean log) {
		
	}

	private void calendarUpdate() {
		ListPreference p = (ListPreference)this.findPreference(PREFS_CALENDAR_ID);
		CharSequence calendar = p.getEntry();
		if (calendar == null) {
			p.setTitle(R.string.CALENDAR);
		} else {
			p.setTitle(calendar);
		}
	}
	private void gotAccount(AccountManager manager, Account account) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Constants.PREFS_ACCOUNT_NAME, account.name);
		editor.commit();
		try {
			Bundle bundle = manager.getAuthToken(account, Constants.AUTH_TOKEN_TYPE, true, null, null).getResult();
			if (bundle.containsKey(AccountManager.KEY_INTENT)) {
				Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
				int flags = intent.getFlags();
				flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
				intent.setFlags(flags);
				startActivityForResult(intent, Constants.RESULT_AUTHENTICATE);
			} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
				authenticatedClientLogin(bundle.getString(AccountManager.KEY_AUTHTOKEN));
			}
		} catch (Exception e) {
//			handleException(e);
			return;
		}
	}

	private void authenticatedClientLogin(String authToken) {
		this.authToken = authToken;
		((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(authToken);
		authenticated();
	}

	private void gotAccount(boolean tokenExpired) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String accountName = settings.getString(Constants.PREFS_ACCOUNT_NAME, null);
		if (accountName != null) {
			AccountManager manager = AccountManager.get(this);
			Account[] accounts = manager.getAccountsByType(Constants.ALLOWED_ACCOUNT_TYPE);
			int size = accounts.length;
			for (int i = 0; i < size; i++) {
				Account account = accounts[i];
				if (accountName.equals(account.name)) {
					if (tokenExpired) {
						manager.invalidateAuthToken(Constants.ALLOWED_ACCOUNT_TYPE, this.authToken);
					}
					gotAccount(manager, account);
					return;
				}
			}
		}
		showDialog(DIALOG_ACCOUNTS);
	}

	private void authenticated() {
		if (sendData != null) {
			try {
				if (sendData.fileName != null) {
					boolean success = false;
					try {
						HttpRequest request = transport.buildPostRequest();
						request.url = PicasaUrl.relativeToRoot("feed/api/user/default/albumid/default");
						((GoogleHeaders) request.headers).setSlugFromFileName(sendData.fileName);
						InputStreamContent content = new InputStreamContent();
						content.inputStream = getContentResolver().openInputStream(sendData.uri);
						content.type = sendData.contentType;
						content.length = sendData.contentLength;
						request.content = content;
						request.execute().ignore();
						success = true;
					} catch (IOException e) {
//						handleException(e);
					}
					setListAdapter(new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_1,
							new String[] { success ? "OK" : "ERROR" }));
				}
			} finally {
				sendData = null;
			}
		} else {
			executeRefreshAlbums();
		}
	}

	private void executeRefreshAlbums() {
		String[] albumNames;
		List<AlbumEntry> albums = this.albums;
		albums.clear();
		try {
			PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");
			// page through results
			while (true) {
				UserFeed userFeed = UserFeed.executeGet(transport, url);
				this.postLink = userFeed.getPostLink();
				if (userFeed.albums != null) {
					albums.addAll(userFeed.albums);
				}
				String nextLink = userFeed.getNextLink();
				if (nextLink == null) {
					break;
				}
			}
			int numAlbums = albums.size();
			albumNames = new String[numAlbums];
			for (int i = 0; i < numAlbums; i++) {
				albumNames[i] = albums.get(i).title;
			}
		} catch (IOException e) {
//			handleException(e);
			albumNames = new String[] { e.getMessage() };
			albums.clear();
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, albumNames));
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
		calendarSetup(false);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		Log.v(LOGTAG,"added preferences listener");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constants.RESULT_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				gotAccount(false);
			} else {
				showDialog(DIALOG_ACCOUNTS);
			}
			break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PREFS_CALENDAR_ID.equals(key)) {
			calendarUpdate();
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
			calendarSetup(true);
			Logger.collectAndSendLog(this, LOGTAG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}