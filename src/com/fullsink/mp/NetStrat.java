package com.fullsink.mp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

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

}
