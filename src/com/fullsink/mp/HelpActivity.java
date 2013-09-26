package com.fullsink.mp;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class HelpActivity extends Activity {
	
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_help);
	        TextView helpText = (TextView)findViewById(R.id.help_text);
	        helpText.setText(Html.fromHtml(getString(R.string.help_html)));
	    }
	
}
