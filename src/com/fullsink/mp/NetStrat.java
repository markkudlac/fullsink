package com.fullsink.mp;

import static com.fullsink.mp.Const.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

public class NetStrat {
	
	/*  Old function for ip address 
	 * 
	public String getServerIPAddress() {
		
		try {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		
		String strIP = String.format("%d.%d.%d.%d", 		// This is bad as IPv4 only, change later
				(ipAddress & 0xff), 
				(ipAddress >> 8 & 0xff), 
				(ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));

		System.out.println("Server IP : "+strIP);
		
		return(strIP);
		} catch ( Exception ex ) {
	    	   System.out.println( "WebServer ipAddress not found" + ex);
	       }
		return null;

	}
	*/


static	public String getWifiApIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
	                .hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            if (intf.getName().contains("wlan")) {
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
	                        .hasMoreElements();) {
	                    InetAddress inetAddress = enumIpAddr.nextElement();
	                    if (!inetAddress.isLoopbackAddress()
	                            && (inetAddress.getAddress().length == 4)) {
	                    	System.out.println("AP address : " + inetAddress.getHostAddress());
	                        return inetAddress.getHostAddress();
	                    }
	                }
	            }
	        }
	    } catch (SocketException ex) {
	    	System.out.println("AP exception : " + ex);
	    }
	    return null;
	}
	

static public int getNextSocket() {
    // Initialize a server socket on the next available port.
	
	ServerSocket  mSocket;
	int sock = -1;
	
	try {
	    mSocket = new ServerSocket(0);
	    sock = mSocket.getLocalPort();
	    mSocket.close();
	} catch (Exception ex) {
		System.out.println("Socket exception : " + ex);
	}
	return sock;
}


static public int getSocketPort(MainActivity mnact) {
	
	int sock = -1;

	sock = Prefs.getSocketPort(mnact);
	if (sock <= 1024) {
		sock = getNextSocket();
	}
	System.out.println("In getSocketPort port : " + sock);
	return sock;
}


static public int getHttpdPort(MainActivity mnact) {
	
	int sock = -1;

	sock = Prefs.getHttpdPort(mnact);
	if (sock <= 1024) {
		sock = getNextSocket();
	}
	System.out.println("In getHttpdPort port : " + sock);
	return sock;
}


static public String getMacAddress(MainActivity mnact ) {
    WifiManager wimanager = (WifiManager) mnact.getSystemService(Context.WIFI_SERVICE);
    String macAddress = wimanager.getConnectionInfo().getMacAddress();

    return macAddress;
}


static public void logServer(MainActivity mnact, String offline) {

 logServer(mnact, offline, Prefs.getAcountID(mnact), 0, 0 );
}


static public String resolverAddress(MainActivity mnact) {
	
	String resolver = Prefs.getServerIPAddress(mnact);
	
	if (resolver.length() <= 18) resolver = RESOLVER_ADDRESS;
	
	return resolver;
}

static public void logServer(MainActivity mnact, String ipadd, String handle, int portsock, int porthttpd ) {
	
	String mac = getMacAddress( mnact );
	
    if (mac == null) {
        System.out.println("No MAC address");
    } else {
    	String provider;
    	int lng = GPS_NULL;
    	int lat = GPS_NULL;
    	
        LocationManager locationManager = (LocationManager) mnact.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
//          System.out.println("Provider " + provider + " has been selected.");
        	
        	
        	lat = (int) (location.getLatitude() * 10000000);
        	lng = (int)(location.getLongitude()* 10000000);
        	
            System.out.println("Latitude : " + lat);
            System.out.println("Longatude : " + lng);
          
        } else {
        	System.out.println("Location not available");
        }
    	
 		new HttpLogServer(mnact,mac).execute(ipadd, handle, String.valueOf(portsock), String.valueOf(porthttpd),
			String.valueOf(lng), String.valueOf(lat), Prefs.getImageHash(mnact));
    }
}
}
