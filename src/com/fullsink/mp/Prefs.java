package com.fullsink.mp;

import android.content.Context;
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
}