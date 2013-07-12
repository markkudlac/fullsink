package com.fullsink.mp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;


public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActionBar().setCustomView(R.layout.actionbar);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Prefs())
                .commit();
    }
	
}