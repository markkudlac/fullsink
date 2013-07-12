package com.fullsink.mp;

import static com.fullsink.mp.Const.*;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;;

public class IPAddressActivity extends Activity {
	
	   @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_ipaddress);
	        
	        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
	        getActionBar().setCustomView(R.layout.actionbar);
	        
	        ((TextView) findViewById(R.id.ipaddress)).setText(NetStrat.getWifiApIpAddress() +
	        		":"+ NetStrat.getHttpdPort());
	    }
		
	    
	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);

	        // Checks the orientation of the screen
	        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        	System.out.println("Got configuration change : Landscape");
	        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        	System.out.println("Got configuration change : Portrait");
	        }
	    }
	 
}
