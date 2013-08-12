package com.fullsink.mp;


import static com.fullsink.mp.Const.HTML_DIR;
import static com.fullsink.mp.Const.HTTP_PROT;
import static com.fullsink.mp.Const.SERVERID_JS;
import static com.fullsink.mp.Const.SERVER_PHOTO;
import static com.fullsink.mp.Const.USERHTML_DIR;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncTask;

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
	
	@SuppressWarnings("deprecation")
	@Override
	protected JSONObject doInBackground(String... xparam){
		
    	HttpURLConnection con = null;
    	JSONObject json = null;
    	
    	try {
    			addr = xparam[0];
    			httpdPort = Integer.valueOf(xparam[1]);
    			servicename = xparam[2];
    			
    		String xurl = "/" + HTML_DIR + "/" + SERVERID_JS;
    			
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

     		System.out.println("JSON id 3 : " + json.getString("id"));
    	} catch (Exception ex) { 
    		System.out.println("Exception caught 1 : " + ex); 
    	}

    	finally {
    		
    		if (con != null) con.disconnect();	
    		
    	}
    	return(json);
	}
	

	@Override
     protected void onPostExecute(JSONObject result) {
    	 
    	 System.out.println("onPostExecute HttpCom");
    	 
    	 if (result != null) {
    		 try {
 //   	 System.out.println("Out PostExecute : " + result.getString("id") + "  Address : " +addr); 
//    	 System.out.println("Out PostExecute httpdPort : " + httpdPort + " webSockPort : " +result.getString("port") );
//    	 System.out.println("HttpCom ID Server : " + result.getString("id"));
    	 			 
    			 serveradapter.add(result.getString("id"), addr, httpdPort, result.getInt("port"), servicename,
					 null);
    			 new DownloadImageTask(mnact, addr, httpdPort, serveradapter, null).execute("/" + USERHTML_DIR + "/" + SERVER_PHOTO);
    			 mnact.adapterOut(false,-1);
    		 } catch (Exception ex) { System.out.println("Exception caught 2 : " + ex); }
    	 }
    	 return;
     }
	
}
