package com.fullsink.mp;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
        String path = new String(NetStrat.resolverAddress(mnact)+LOG_SERVER_PATH + macaddr + "/A" + "?");
        StringBuilder params = new StringBuilder();

        try {
        	params.append("ipadd").append('=').append(xparam[0]).append('&');
        	params.append("userhandle").append('=').append(xparam[1]).append('&');
        	params.append("portsock").append('=').append(xparam[2]).append('&');
        	params.append("porthttpd").append('=').append(xparam[3]);
        	
//            String ssid = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
//            if (ssid.startsWith("\"") && ssid.endsWith("\"")){
//                ssid = ssid.substring(1, ssid.length()-1);
//            }
        	//params.append("netname").append('=').append(ssid);
        	
        	String pathString = path + (params).toString();
            final String encodedURL = URLEncoder.encode(pathString, "UTF-8");
        	
        	//TESTING
        	System.out.println("The ip and port: " + encodedURL);

        	HttpGet httpGet = new HttpGet(encodedURL);
        	
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
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
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
