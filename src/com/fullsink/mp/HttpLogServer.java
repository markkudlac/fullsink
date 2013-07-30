package com.fullsink.mp;


import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import static com.fullsink.mp.Const.*;

public class HttpLogServer extends AsyncTask<String, Void, JSONObject>{

		MainActivity mnact;
		String macaddr;
		
	public HttpLogServer(MainActivity mnact, String macaddr) {
		this.mnact = mnact;
		this.macaddr = macaddr;
	}
	
	
	@Override
	protected JSONObject doInBackground(String... xparam){
		
    	JSONObject json = null;
    	
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        StringBuilder path = new StringBuilder(NetStrat.resolverAddress(mnact)+LOG_SERVER_PATH + macaddr + "/A" + "?");
        StringBuilder params = new StringBuilder();

        try {
        	params.append("ipadd").append('=').append(xparam[0]).append('&');
        	params.append("userhandle").append('=').append(xparam[1]).append('&');
        	params.append("portsock").append('=').append(xparam[2]).append('&');
        	params.append("porthttpd").append('=').append(xparam[3]);

        	HttpGet httpGet = new HttpGet(path.append(params).toString());
        	
            response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                json = new JSONObject(responseString);
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
  	
    	return(json);
	}

	
	@Override
     protected void onPostExecute(JSONObject result) {
    	 
//		System.out.println("****** PostExecuteHttpLogServer *******");
				
    	 if (result != null) {
    		 try {
 
    			 System.out.println("PostExecuteHttpLogServer  rtn : " + result.getBoolean("rtn"));
 
    		 } catch (Exception ex) { System.out.println("Exception caught : " + ex); }
    	 }
     }
}
