package com.fullsink.mp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


import static com.fullsink.mp.Const.*;

import android.os.Looper;
import android.os.SystemClock;


public class DiscoverHttpd {

    MainActivity mnact;
    ServerAdapter serveradapter;
    String ipadd;
    int port;
    
    volatile int timerSecs;
    Thread currentThread;
    
    public DiscoverHttpd(MainActivity context, ServerAdapter serveradapter, String ipadd, int port) {
        mnact = context;
        this.serveradapter = serveradapter;
        timerSecs = POLL_SECONDS;	// wait x seconds between poll runs
        this.ipadd = ipadd;
        this.port = port;
        
        pollHttpd();
    }
    
    
    
 //   public void pollHttpd(final String ipadd, final int port, final int timer) {
    public void pollHttpd() {	
     	currentThread = new Thread() {
            public void run() {
            	
            	int i = 0;	// Just for debug
            	int lower, upper,cnt;
            	
            	int LOWER_lim = 0;
            	int UPPER_lim = 255;
            	
            	int ipend =  Integer.valueOf(ipadd.substring(ipadd.lastIndexOf(".")+1));
		    	String ipbase =  ipadd.substring(0,ipadd.lastIndexOf(".")+1);
		    	
            	while (timerSecs > 0) {
            		
            	try {	
            		
            		serveradapter.clearAlive();
            		
			    	System.out.println("In pollHttpd : "+i + "  TimerSecs : "+timerSecs);
			    	
			    	lower = ipend - 1;
			    	upper = ipend + 1;
			    	cnt = 1;
			    	
			    	while ((lower > LOWER_lim || upper < UPPER_lim) && !isInterrupted()) {
			    		
			    		if (cnt % 25 == 0 ) {
			    			Thread.sleep(POLL_SLEEP_SOCKET);
			    		}
			    		
			    		if (lower > LOWER_lim) {
			    			pollAddress(ipbase+lower,port);
			    			--lower;
			    			++cnt;
			    		}
			    		
			    		if (upper < UPPER_lim) {
			    			pollAddress(ipbase+upper,port);
			    			++upper;
			    			++cnt;
			    		}
			    	}
			    	
			    	++i;	// Just for debug
			    	
			    	if (timerSecs > 0) {
			    		Thread.sleep(POLL_SLEEP_CLEAN * 1000);
			    		clearDeadServers();
			    		Thread.sleep(1000 * timerSecs);
			    	} else {
			    		System.out.println("Exiting from pollHttpd 1");
			    		return;
			    	}
			    	
            	
            	} catch (InterruptedException ex) {
             		System.out.println("Got interupt exception in run");
             		
             	}
            	}
            	System.out.println("Exiting from pollHttpd 2");
            }   	
     	};
     	
     	currentThread.start();
    }
    
    
    private void pollAddress(final String ipadd, final int port) {
    	
    	new Thread(new Runnable() {
            public void run() {
            	
	 try {
            
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ipadd, port), POLL_SLEEP_SOCKET - 100);
            System.out.println("******* Socket open : " +ipadd);
            socket.close();
            if (! serveradapter.inServerList(ipadd)) {
            	System.out.println("ServerSearch connect Address : " + ipadd + " Port : "+port);
				new HttpCom(mnact,serveradapter).execute(ipadd,String.valueOf(port), ipadd);
			}
        } catch (IOException e) {
//        	System.out.println("Socket closed : " +ipadd);
        }
            }   	
        }).start(); 
    }
    
    

    public void constantPoll(int timer) {	
    	if (currentThread.isAlive()) {
    		timerSecs = timer;
    		System.out.println("Thread is alive");
    		currentThread.interrupt();
    	}
    }
    
    
    public void clearDeadServers() {
    	
    	for (int i=0; i < serveradapter.getCount(); i++){
    		if ( !((ServerData) serveradapter.getItem(i)).alive ) {
    			System.out.println("This server in Adapter is dead : "+ ((ServerData) serveradapter.getItem(i)).servicename);
    			int clearitem = serveradapter.removeFromServerList(((ServerData) serveradapter.getItem(i)).servicename);
    			
				mnact.adapterOut(true, clearitem);
    		}
    	}
    }
}
  

/*
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
	   		this.servicename = servicename;
//	   		this.servicename = servicename.replaceAll("k..032", "k ");  Old for nsd
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
	   		this.servicename = servicename;
//	   		this.servicename = servicename.replaceAll("k..032", "k ");
	   	}
	   
	   
	   @Override
	   public void run() {
		    
				int clearitem = serveradapter.removeFromServerList(servicename);
			
				mnact.adapterOut(true, clearitem);
	    	    return;
	   }
}
*/