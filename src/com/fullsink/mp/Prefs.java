package com.fullsink.mp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.*;


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
	
	
	public static boolean getDownload(Context context) {
			
		boolean allowdown = PreferenceManager.
		getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_download),true);
		
		return(allowdown);
	}
	
	
	public static String getServerIPAddress(Context context) {
		
		String xstr = PreferenceManager.
		getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_srvadd),"");
		
		return(xstr);
	}
	
	
public static boolean getOnAir(Context context) {
		
		boolean xbool = PreferenceManager.
		getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_onair),true);
		
		return(xbool);
	}

public static void setName(Context context, String name) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	prefs.edit().putString("com.fullsink.mp.name", name).commit();
}


public static String getName(Context context) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	return prefs.getString("com.fullsink.mp.name","");
}


// Number of times app has been loaded. Used for initialization
public static void setLoadCount(Context context, int count) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	prefs.edit().putInt("com.fullsink.mp.loadcount", count).commit();
}


public static int getLoadCount(Context context) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	return prefs.getInt("com.fullsink.mp.loadcount", 0);	//Start count at zero
}



public static void setVersionNumber(Context context, int count) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	prefs.edit().putInt("com.fullsink.mp.versionnumber", count).commit();
}


public static int getVersionNumber(Context context) {

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	return prefs.getInt("com.fullsink.mp.versionnumber", 0);	//Start count at zero
}

}