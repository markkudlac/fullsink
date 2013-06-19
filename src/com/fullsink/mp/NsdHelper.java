package com.fullsink.mp;

import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;

import static com.fullsink.mp.Const.*;

import android.os.SystemClock;
import android.widget.ListView;

public class NsdHelper {

    MainActivity mnact;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    
    public String mServiceName = SERVICE_NAME;		
    ServerAdapter serveradapter;
    boolean fsregistered = false;
    
    public NsdHelper(MainActivity context, ServerAdapter serveradapter) {
        mnact = context;
        this.serveradapter = serveradapter;
        
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
            	System.out.println("Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
            	System.out.println( "Service discovery success " + service);
            	System.out.println( "Service discovery mServicename " + mServiceName);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                	System.out.println( "Unknown Service Type: " + service.getServiceType());
 //               } else if (service.getServiceName().equals(mServiceName)) {
  //              	System.out.println( "Same machine no resolver call: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)){
                	System.out.println( "Call the resolver for contains name  : " + service.getServiceName());
                    mNsdManager.resolveService(service, mResolveListener);
                }
                else {
                	mNsdManager.resolveService(service, mResolveListener); 
                }
            }

            
            @Override
            public void onServiceLost(NsdServiceInfo service) {
            	
            System.out.println("Service Lost - good : "+ service.getServiceName())	;
            
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                	System.out.println( "Unknown Service Type: " + service.getServiceType());
 //               } else if (service.getServiceName().contains(mServiceName)){
                } else if (service.getServiceName().contains(SERVICE_NAME)){	
                	System.out.println("*** Call to remove from serverlist  *****");
                    new Thread(new ServerRemove(mnact, serveradapter,service.getServiceName())).start();
                } else {
                	System.out.println("*** Lost but no Call to remove from serverlist : "+mServiceName);
                }
            }
            
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
            	System.out.println( "Discovery stopped: " + serviceType);        
            }

            
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                System.out.print( "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                System.out.println( "Discovery failed: Error code : " + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                System.out.println( "Resolve failed error: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
            	System.out.println( "Resolve Succeeded  " + serviceInfo);
/*
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    System.out.println( "Same IP/name : " + serviceInfo.getServiceName());
                    return;
                }
 */               
                String addr = serviceInfo.getHost().getHostAddress();
 /*               
                mnact.textOut( "Service resolved");
                mnact.textOut( "Service get host : " + addr);
                mnact.textOut( "Service get port : " + serviceInfo.getPort());
                
                System.out.println( "Service resolved");
                System.out.println( "Service get host : " + addr);
                System.out.println( "Service get port : " + serviceInfo.getPort());
 */               
                if (!addr.equals(NetStrat.getWifiApIpAddress())) {
                	new Thread(new ServerSearch(mnact, serveradapter, addr,
                		serviceInfo.getPort(),serviceInfo.getServiceName())).start();
                } else {
        			System.out.println("Finding my own ip address for servce. not added : "+ addr);
        		}
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                System.out.println( "Service registered : " + mServiceName);
                fsregistered = true;
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            	System.out.println( "Service register FAILED : " + arg0.getServiceName());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            	System.out.println( "Service unregistered : " + arg0.getServiceName());
            	fsregistered = false;
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	System.out.println( "Service unregistered error : " + errorCode);
            }
            
        };
    }

    public void registerService(int port) {
    	
//    	if (!fsregistered) {
    	
//	    	mServiceName = SERVICE_NAME;
	        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
	        serviceInfo.setPort(port);
//	        serviceInfo.setServiceName(mServiceName);
	        serviceInfo.setServiceName(SERVICE_NAME);
	        serviceInfo.setServiceType(SERVICE_TYPE);
        
        	mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
 //       }
    }

    
    public void unregisterService() {
    	 
 //   	if (fsregistered) {
    		mNsdManager.unregisterService(mRegistrationListener);
 //   	}
    	
    }
 
    
    public void discoverServices() {
 //   	mServiceName = SERVICE_NAME.substring(0, SERVICE_NAME.length()-1);
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
 //   	mServiceName = null;
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
 
}

class ServerSearch implements Runnable {
   	
	   MainActivity mnact;
	   ServerAdapter serveradapter;
	   String addr;
	   int port;
	   String servicename;
	   
	   ServerSearch(MainActivity xact, ServerAdapter serveradapter, String addr, int port, String servicename) {
	   		
	   		mnact = xact;
	   		this.serveradapter = serveradapter;
	   		this.addr = addr;
	   		this.port = port;
	   		this.servicename = servicename.replaceAll("k..032", "k ");
	   	}
	   
	   
	   @Override
	   public void run() {
		    
				System.out.println("ServerSearch connect Address : " + addr + " Port : "+port);

				if (! serveradapter.inServerList(addr)) {
					new HttpCom(mnact,serveradapter).execute(addr,String.valueOf(port), servicename);
				}
	    	    return;
	   }
}


class ServerRemove implements Runnable {
   	
	   MainActivity mnact;
	   ServerAdapter serveradapter;
	   String servicename;
	   
	   ServerRemove(MainActivity mnact, ServerAdapter serveradapter, String servicename) {
	   		
	   		this.mnact = mnact;
	   		this.serveradapter = serveradapter;
	   		this.servicename = servicename.replaceAll("k..032", "k ");
	   	}
	   
	   
	   @Override
	   public void run() {
		    
				int clearitem = serveradapter.removeFromServerList(servicename);
			
				mnact.adapterOut(true, clearitem);
	    	    return;
	   }
}
