package com.fullsink.mp;

import static com.fullsink.mp.Const.*;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class IPAddressActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipaddress);

		((TextView) findViewById(R.id.ipaddress)).setText(NetStrat
				.getWifiApIpAddress() + ":" + NetStrat.getHttpdPort());
		
        String ssid = ((WifiManager) this.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")){
            ssid = ssid.substring(1, ssid.length()-1);
        }
        ((TextView) findViewById(R.id.networkIPAdressView)).setText(ssid);
        
        //show action bar for OS 2.3 or greater
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			getActionBar().setCustomView(R.layout.actionbar);
	        //show user picture in the action bar
	        Bitmap bm = PhotoActivity.getPhotoBitmap(this);
            if (bm != null){
           	 ((ImageView) findViewById(R.id.photoActionBar)).setImageBitmap(bm);
            }
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			System.out.println("Got configuration change : Landscape");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			System.out.println("Got configuration change : Portrait");
		}
	}

}
