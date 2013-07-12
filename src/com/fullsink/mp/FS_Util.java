package com.fullsink.mp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;


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
	

}