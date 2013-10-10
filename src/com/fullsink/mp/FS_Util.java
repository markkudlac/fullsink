package com.fullsink.mp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.provider.Settings;
import android.widget.Toast;


public class FS_Util {
	
	public static int scaleDipPx(MainActivity mnact, Integer dip) {
		
		float scale = mnact.getResources().getDisplayMetrics().density; // scale px with this
		
		return(Float.valueOf((dip.floatValue() * scale)).intValue());
	}
	
	
	public static int getVersionNumber(Activity act) {
		
		int versionCode = -1;
		try {
	PackageInfo packageInfo = act.getPackageManager()
		    .getPackageInfo(act.getPackageName(), 0);
		versionCode = packageInfo.versionCode;
		} catch(Exception ex) {
			System.out.println("Version Num : "+ex);
		}
		return versionCode;
	}
	
	
	public static boolean changedVersionNumber(Activity act) {
		
		int vnum = getVersionNumber( act);
		
		if (vnum != Prefs.getVersionNumber(act))  {
			Prefs.setVersionNumber(act, vnum);
			return true;
		} else {
			return false;
		}
	}
	
	public static void showConnectionWarning(Activity act){
		final Context ctx = act;
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(ctx.getResources().getString(R.string.warning))
		.setMessage(ctx.getResources().getString(R.string.warning_connection_msg));
		 builder.setPositiveButton(ctx.getResources().getString(R.string.settings), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) 
		        {
		        	ctx.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		        }
		    });

		    builder.setNegativeButton(R.string.cancel, null);
		AlertDialog alert = builder.create();
		alert.show();
	}
	

}