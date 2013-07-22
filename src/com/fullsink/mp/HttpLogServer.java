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
	
	
	@Override
	protected JSONObject doInBackground(String... xparam){
		
    	JSONObject json = null;
    	
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        
//    	String provider;
    	int lng = GPS_NULL;
    	int lat = GPS_NULL;
    	

        try {
        	HttpPut httpPut = new HttpPut(NetStrat.resolverAddress(mnact)+LOG_SERVER_PATH + macaddr + "/A");
        	
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        		
//            LocationManager locationManager = (LocationManager) mnact.getSystemService(Context.LOCATION_SERVICE);
//            // Define the criteria how to select the locatioin provider -> use
//            // default
            
//            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
//            		locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                Criteria criteria = new Criteria();
//                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//                criteria.setAltitudeRequired(false);
//                criteria.setBearingRequired(false);
//                criteria.setCostAllowed(false);
//                criteria.setPowerRequirement(Criteria.POWER_MEDIUM );
//                provider = locationManager.getBestProvider(criteria, true);
//                Location location = locationManager.getLastKnownLocation(provider);

                // Initialize the location fields
//                if (location != null) {
////                  System.out.println("Provider " + provider + " has been selected.");
//                	
//                	lat = (int) (location.getLatitude() * 10000000);
//                	lng = (int)(location.getLongitude()* 10000000);
//                	
//                    System.out.println("Latitude : " + lat);
//                    System.out.println("Longatude : " + lng);
//                  
//                } else {
//                	System.out.println("Location not available");
//                }
//            } else if (!xparam[2].equals("0")) {
//            	mnact.toastOut("Location services not enabled",Toast.LENGTH_SHORT);
//            	System.out.println("Location services not enabled");
//            }
            
            nameValuePairs.add(new BasicNameValuePair("ipadd", xparam[0]));
        	nameValuePairs.add(new BasicNameValuePair("userhandle", xparam[1]));
            
            nameValuePairs.add(new BasicNameValuePair("portsock", xparam[2]));
            nameValuePairs.add(new BasicNameValuePair("porthttpd", xparam[3]));

            nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(lng)));
            nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(lat))); 

            httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        	
            response = httpclient.execute(httpPut);
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
	
	
//	 protected void onProgressUpdate(Integer... progress) {        
//     }

	
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
