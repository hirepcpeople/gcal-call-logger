package com.dahl.brendan.calllog.util;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.dahl.brendan.calllog.R;

public class Logger {
    public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$
    
    public static void collectAndSendLog(final Context context, final String LOGTAG){
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(ACTION_SEND_LOG);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        final boolean isInstalled = list.size() > 0;
        
        if (!isInstalled) {
            new AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Install the free and open source Log Collector application to collect the device log and send it to the developer.")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + LOG_COLLECTOR_PACKAGE_NAME));
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(marketIntent); 
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        } else {
            final String email = "dahl.brendan@brendandahl.com";
            new AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Run Log Collector application.\nIt will collect the device log and send it to "+email+".\nYou will have an opportunity to review and modify the data being sent.")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
        			String info = "";
					try {
						PackageInfo pi = packageManager.getPackageInfo(context.getPackageName(), 0);
	        			info += "\nVersion: "+pi.versionName;
					} catch (NameNotFoundException e) {
						Log.e(LOGTAG, e.getMessage());
					}
        			info += "\nModel: "+android.os.Build.MODEL;
        			info += "\nOS: "+android.os.Build.VERSION.RELEASE;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_SEND_INTENT_ACTION, Intent.ACTION_SENDTO);
                    intent.putExtra(EXTRA_DATA, Uri.parse("mailto:" + email));
                    intent.putExtra(EXTRA_ADDITIONAL_INFO, "Additonal info: "+info+"\n");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Application failure report");
                    
                    intent.putExtra(EXTRA_FORMAT, "time");
                    
                    //The log can be filtered to contain data relevant only to your app
                    String[] filterSpecs = new String[3];
                    filterSpecs[0] = "AndroidRuntime:E";
                    filterSpecs[1] = LOGTAG;
                    filterSpecs[2] = "*:S";
                    intent.putExtra(EXTRA_FILTER_SPECS, filterSpecs);
                    
                    context.startActivity(intent);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
    }
}
