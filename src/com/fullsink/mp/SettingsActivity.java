package com.fullsink.mp;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity {
	private static int prefs = R.xml.settings;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(android.os.Build.VERSION.SDK_INT>=11) {
	        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
	        getActionBar().setCustomView(R.layout.actionbar);
	        Bitmap bm = PhotoActivity.getPhotoBitmap(this);
            if (bm != null){
           	 ((ImageView) findViewById(R.id.photoActionBar)).setImageBitmap(bm);
            }
            ImageButton logoButton = (ImageButton) findViewById(R.id.logo_record);
            logoButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
               	 Toast.makeText(getApplicationContext(), NetStrat.getSsid(), Toast.LENGTH_LONG).show();
                }
            });
        }
        
    try {
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Prefs.PF())
                .commit();
    } catch (NoSuchMethodError e) { // Api < 11
		AddResourceApiLessThan11();
	}
    }
    
	@SuppressWarnings("deprecation")
	protected void AddResourceApiLessThan11() {
		addPreferencesFromResource(prefs);
	}
	
}