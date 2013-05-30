package com.fullsink.mp;


import java.net.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONObject;

import static com.fullsink.mp.Const.*;

public class HttpCom extends AsyncTask<String, Void, JSONObject>{

		MainActivity mnact;
		String addr;
		int httpdPort;
		ServerAdapter serveradapter;
		String servicename;
		
	public HttpCom(MainActivity mnact, ServerAdapter serveradapter) {
		this.mnact = mnact;
		this.serveradapter = serveradapter;
	}
	
	protected JSONObject doInBackground(String... xparam){
		
    	HttpURLConnection con = null;
    	JSONObject json = null;
    	
    	try {
    			addr = xparam[0];
    			httpdPort = Integer.valueOf(xparam[1]);
    			servicename = xparam[3];
    			
    		String xurl = "http://"+ xparam[0]  + ":" + xparam[1] + "/" + HTML_DIR + "/" + xparam[2];
    			
    		URL url = new URL(xurl);
    		
    		con = (HttpURLConnection) url.openConnection();
    		
    		InputStream xin = (InputStream) con.getInputStream();
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(xin));
    		String result, line = reader.readLine();
    		result = line;

    		while((line=reader.readLine())!=null){
    		    result+=line;
    		}
 
 //   		System.out.println("Get serverid.js from server HTTP 3");
    		json = new JSONObject(result);

     		System.out.println("JSON id : " + json.getString("id"));
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
    			 byte[] xbuf;
//    	 System.out.println("Out PostExecute : " + result.getString("id") + "  Address : " +addr); 
//    	 System.out.println("Out PostExecute httpdPort : " + httpdPort + " webSockPort : " +result.getString("port") );
    			 mnact.textOut("HttpCom ID Server : " + result.getString("id"));
    	 
    			 xbuf = Base64.decode(result.getString("img"),Base64.DEFAULT);
    			 
    			 serveradapter.add(result.getString("id"), addr, httpdPort, result.getInt("port"), servicename,
    					 BitmapFactory.decodeByteArray(xbuf, 0, xbuf.length));

    			 mnact.adapterOut(false,-1);
    		 } catch (Exception ex) { System.out.println("Exception caught : " + ex); }
    	 }
     }
}
