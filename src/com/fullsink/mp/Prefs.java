package com.fullsink.mp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.*;
import android.annotation.TargetApi;
import android.preference.PreferenceActivity;

public class Prefs extends PreferenceActivity {
	private static int prefs = R.xml.settings;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			getClass().getMethod("getFragmentManager");
			AddResourceApi11AndGreater();
		} catch (NoSuchMethodException e) { // Api < 11
			AddResourceApiLessThan11();
		}
	}

	@SuppressWarnings("deprecation")
	protected void AddResourceApiLessThan11() {
		addPreferencesFromResource(prefs);
	}

	@TargetApi(11)
	protected void AddResourceApi11AndGreater() {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PF()).commit();
	}
	
	public static void setVersionNumber(Context context, int count) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		prefs.edit().putInt("com.fullsink.mp.versionnumber", count)
				.commit();
	}

	public static int getVersionNumber(Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getInt("com.fullsink.mp.versionnumber", 0); // Start
																	// count
																	// at
																	// zero
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// This is here to capture orientation change. No need to do
		// anything
	}

	public static Integer getHttpdPort(Context context) {

		String xstr = PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						context.getString(R.string.pref_httpdport),
						context.getString(R.string.default_httpdport));

		return (Integer.parseInt(xstr));
	}

	public static Integer getSocketPort(Context context) {

		String xstr = PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						context.getString(R.string.pref_sockport),
						context.getString(R.string.default_sockport));

		return (Integer.parseInt(xstr));
	}
	
	public static Integer getSortOrder(Context context) {

		String xstr = PreferenceManager
				.getDefaultSharedPreferences(context).getString("sort_order",
						"" + R.id.alphabetical);

		return (Integer.parseInt(xstr));
	}
	
	public static void setSortOrder(Context context, String order) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		prefs.edit().putString("sort_order", order).commit();
	}

	public static boolean getDownload(Context context) {

		boolean allowdown = PreferenceManager.getDefaultSharedPreferences(
				context).getBoolean(
				context.getString(R.string.pref_download), true);

		return (allowdown);
	}

	public static String getServerIPAddress(Context context) {

		String xstr = PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						context.getString(R.string.pref_srvadd), "");

		return (xstr);
	}

	public static boolean getOnAir(Context context) {

		boolean xbool = PreferenceManager.getDefaultSharedPreferences(
				context).getBoolean(context.getString(R.string.pref_onair),
				true);

		return (xbool);
	}

	public static void setName(Context context, String name) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		prefs.edit().putString("com.fullsink.mp.name", name).commit();
	}

	public static String getName(Context context) {
		
		String tmpname;
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		tmpname = prefs.getString("com.fullsink.mp.name", "");
		if (tmpname.length() > 20) {
			tmpname = tmpname.substring(0, 20);
		}
		return tmpname;
	}

	// Number of times app has been loaded. Used for initialization
	public static void setLoadCount(Context context, int count) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		prefs.edit().putInt("com.fullsink.mp.loadcount", count).commit();
	}

	public static int getLoadCount(Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getInt("com.fullsink.mp.loadcount", 0); // Start count
																// at zero
	}

	@TargetApi(11)
	public static class PF extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.settings);
		}

	}
}