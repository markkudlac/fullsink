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
    			servicename = xparam[2];
    			
    		String xurl = HTML_DIR + "/" + SERVERID_JS;
    			
    		URL url = new URL(HTTP_PROT, xparam[0], httpdPort, xurl);	
    		con = (HttpURLConnection) url.openConnection();
    		
    		InputStream xin = (InputStream) con.getInputStream();
    		BufferedReader reader = new BufferedReader(new InputStreamReader(xin));
    		String result, line = reader.readLine();
    		result = line;

    		while((line=reader.readLine())!=null){
    		    result+=line;
    		}
    		json = new JSONObject(result);

 //    		System.out.println("JSON id : " + json.getString("id"));
    	} catch (Exception ex) { System.out.println("Exception caught : " + ex); }

 
    	finally {
    		if (con != null) con.disconnect();	
    	}
    	return(json);
	}
	

     protected void onPostExecute(JSONObject result) {
    	 
    	 if (result != null) {
    		 try {
//    	 System.out.println("Out PostExecute : " + result.getString("id") + "  Address : " +addr); 
//    	 System.out.println("Out PostExecute httpdPort : " + httpdPort + " webSockPort : " +result.getString("port") );
 //   			 mnact.textOut("HttpCom ID Server : " + result.getString("id"));
    	 			 
    			 serveradapter.add(result.getString("id"), addr, httpdPort, result.getInt("port"), servicename,
					 null);
    			 new DownloadImageTask(mnact, addr, httpdPort, serveradapter, null).execute(USERHTML_DIR + "/" + SERVER_PHOTO);
    			 mnact.adapterOut(false,-1);
    		 } catch (Exception ex) { System.out.println("Exception caught : " + ex); }
    	 }
     }
}
