package com.fullsink.mp;


import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
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
	
	protected JSONObject doInBackground(String... xparam){
		
    	JSONObject json = null;
    	
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        
        try {
        	HttpPut httpPut = new HttpPut(NetStrat.resolverAddress(mnact)+LOG_SERVER_PATH + macaddr + "/A");
        	
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        	if (xparam.length > 0) {
	        	nameValuePairs.add(new BasicNameValuePair("ipadd", xparam[0]));
	        	nameValuePairs.add(new BasicNameValuePair("userhandle", xparam[1]));
	            
	            nameValuePairs.add(new BasicNameValuePair("portsock", xparam[2]));
	            nameValuePairs.add(new BasicNameValuePair("porthttpd", xparam[3]));
	            
	            nameValuePairs.add(new BasicNameValuePair("longitude", xparam[4]));
	            nameValuePairs.add(new BasicNameValuePair("latitude", xparam[5]));
//	            nameValuePairs.add(new BasicNameValuePair("imagehash", xparam[6]));
        	} 
    /*    	else {
           // Update image only
	            byte[] xb = PhotoActivity.getPhotoByte(mnact);
	            System.out.println("Size of photo : "+xb.length);
	            
	            String img = new String(xb);
	            System.out.println("Size of photo 2 added to httpLogServer: "+img.length());
	            nameValuePairs.add(new BasicNameValuePair("userimage", img));

        	}
        	*/
            httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        	
            response = httpclient.execute(httpPut);
            StatusLine statusLine = response.getStatusLine();
            
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                json = new JSONObject(responseString);
                /*
                if (xparam.length == 0) {
                	System.out.println("Assigh photo to false");
                	json.put("photo", false);
                }
                */
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
	
//	 protected void onProgressUpdate(Integer... progress) {        
//     }

     protected void onPostExecute(JSONObject result) {
    	 
    	 if (result != null) {
    		 try {
 
    			 System.out.println("PostExecuteHttpLogServer  rtn : " + result.getBoolean("rtn"));
  /*  			 
    			 if (result.getBoolean("photo")){
    				 // Update the photo
    				 new HttpLogServer(mnact,macaddr).execute();
    			 }
    */
    		 } catch (Exception ex) { System.out.println("Exception caught : " + ex); }
    	 }
     }
}
