package com.fullsink.mp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.*;
//import android.view.*;
//import android.content.Context;

public class Prefs extends PreferenceFragment {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
	}

	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
// This is here to capture orientation change. No need to do anything
    }
	
	
	public static Integer getHttpdPort(Context context) {
				
		String xstr = PreferenceManager.
		getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_httpdport),
				context.getString(R.string.default_httpdport));
		
		return(Integer.parseInt(xstr));
	}
	

	public static Integer getSocketPort(Context context) {
		
		String xstr = PreferenceManager.
		getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_sockport),
				context.getString(R.string.default_sockport));
		
		return(Integer.parseInt(xstr));
	}

	
	
	public static String getAcountID(Context context) {
			
		String xstr = PreferenceManager.
		getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_acntid),"");
		
		return(xstr);
	}
	
	
	public static String getServerIPAddress(Context context) {
		
		String xstr = PreferenceManager.
		getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_srvadd),"");
		
		return(xstr);
	}
	
	
public static boolean getShuffle(Context context) {
		
		boolean xbool = PreferenceManager.
		getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_shuffle),false);
		
		return(xbool);
	}


/*
public static void setImageHash(Context context, String hash) {
	
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	
	prefs.edit().putString("com.fullsink.mp.imagehash", hash).commit();
}


public static String getImageHash(Context context) {
	
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	
	return prefs.getString("com.fullsink.mp.imagehash","");
}
*/
}