package com.fullsink.mp;

import static com.fullsink.mp.Const.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;

public class NetStrat {
	
static int httpdPort = 0;

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


static public int getSocketPort(Activity mnact) {
	
	int sock = -1;

	sock = Prefs.getSocketPort(mnact);
	if (sock <= 1024) {
		sock = getNextSocket();
	}
	System.out.println("In getSocketPort port : " + sock);
	return sock;
}


static public int getHttpdPort(Activity mnact) {
	
	int sock = -1;

	sock = Prefs.getHttpdPort(mnact);
	if (sock <= 1024) {
		sock = getNextSocket();
	}
//	System.out.println("In getHttpdPort port : " + sock);
	return sock;
}


static public String getMacAddress(MainActivity mnact ) {
    WifiManager wimanager = (WifiManager) mnact.getSystemService(Context.WIFI_SERVICE);
    String macAddress = wimanager.getConnectionInfo().getMacAddress();

    return macAddress;
}


static public void logServer(MainActivity mnact, String offline) {

 logServer(mnact, offline, Prefs.getName(mnact), 0, 0 );
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
	
 		new HttpLogServer(mnact,mac).execute(ipadd, handle, String.valueOf(portsock), String.valueOf(porthttpd));
    }
}


static public void storeHttpdPort(int port) {
	httpdPort = port;
}
 

static public int getHttpdPort() {
	return httpdPort;
}

}



