package com.fullsink.mp;


import java.net.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.AsyncTask;
import org.json.JSONObject;

import static com.fullsink.mp.Const.*;

public class HttpCom extends AsyncTask<String, Void, JSONObject>{

		MainActivity mnact;
		String addr;
		ServerAdapter serveradapter;
		
	public HttpCom(MainActivity mnact, ServerAdapter serveradapter) {
		this.mnact = mnact;
		this.serveradapter = serveradapter;
	}
	
	protected JSONObject doInBackground(String... xparam){
		
    	HttpURLConnection con = null;
    	JSONObject json = null;
    	
    	try {
    			addr = xparam[0] ;
    		String xurl = "http://"+ xparam[0]  + ":" + xparam[1] + "/" + HTMLDIR + "/" + xparam[2];
    		System.out.println("In logUser url : " + xurl);
    		
    		URL url = new URL(xurl);
    		
    		con = (HttpURLConnection) url.openConnection();
    		
    		InputStream xin = (InputStream) con.getInputStream();
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(xin));
    		String result, line = reader.readLine();
    		result = line;
    		while((line=reader.readLine())!=null){
    		    result+=line;
    		}
 //   		System.out.println("from server : " + result);
    		json = new JSONObject(result);

//      		System.out.println("JSON id : " + json.getString("id"));
    	} catch (Exception ex) { System.out.println("Exception caught : " + ex); }

 
    	finally {
    		if (con != null) con.disconnect();	
    	}
  	
    	return(json);
	}
	
//	 protected void onProgressUpdate(Integer... progress) {        
//     }

     protected void onPostExecute(JSONObject result) {
    	 
    	 if (result != null) {
    		 try {
 //   	 System.out.println("Out PostExecute : " + result.getString("id")); 	
    	 mnact.textOut("HttpCom ID Server : " + result.getString("id"));
	    	serveradapter.add(addr);
//	    	serveradapter.add(result.getString("id")+":" + addr);
	    	mnact.adapterOut();
    		 } catch (Exception ex) { System.out.println("Exception caught : " + ex); }
    	 }
     }
}
