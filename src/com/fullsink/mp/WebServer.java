package com.fullsink.mp;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.content.res.AssetFileDescriptor;
import android.util.Base64;


/**
 * A simple WebSocketServer implementation.
 */
public class WebServer extends WebSocketServer {

	MainActivity mnact = null;
	
        public WebServer( int port, String ipadd, MainActivity xmnact ) throws UnknownHostException {
        	
                super( new InetSocketAddress(
 //               InetAddress.getByName("192.168.1.106"), port ) );
                InetAddress.getByName(ipadd), port ) );
                
                mnact = xmnact;
        }

        public WebServer( InetSocketAddress address ) {
                super( address );
        }

        @Override
        public void onOpen( WebSocket conn, ClientHandshake handshake ) {
 //               this.sendToAll( "CMD:MESS : new connection: " + handshake.getResourceDescriptor() );
 //               this.sendToAll( "new connection: " );
                System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
        }

        @Override
        public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
 //               this.sendToAll("CMD:MESS closed " + conn);
                System.out.println("closed" + conn);
        }

        @Override
        public void onMessage( WebSocket conn, String message ) {
        	// Receive messages from controller here
          System.out.println( "onMessage server : " + conn + ": " + message );
            mnact.textOut( "onMessage server : " + message );
        	if (message.startsWith("CMD:TRACK")) {	
        		sendTrack();
//        		this.sendToAll("CMD:PLAY");
    		} else {		
//        		this.sendToAll( message );
    		}

//          this.sendToAll( message );
        }

        @Override
        public void onError( WebSocket conn, Exception ex ) {
                ex.printStackTrace();
                if( conn != null ) {
                        // some errors like port binding failed may not be assignable to a specific websocket
                }
        }

        /**
         * Sends <var>text</var> to all currently connected WebSocket clients.
         *
         * @param text
         *            The String to send across the network.
         * @throws InterruptedException
         *             When socket related I/O errors occur.
         */
        public void sendToAll( String text ) {
                Collection<WebSocket> con = connections();
                synchronized ( con ) {
                        for( WebSocket c : con ) {
                                c.send( text );
                        }
                }
        }
        
        public void sendTrack(){
        	
        	mnact.textOut("In sendTrack");
        	int i = 0;
        	try {
        		/*
        	    BufferedReader reader = new BufferedReader(
        	        new InputStreamReader(getAssets().open("Track1.mp3")), 32000);

        	    // do reading, usually loop until end of file reading  
        	    String mLine = reader.readLine();
        	    while (mLine != null && i < 5) {
        	    	
        	    	WServ.sendToAll(mLine);
        	    	textOut("Srv sent : " + i + " Sz : "+ mLine.length()); 
        	       mLine = reader.readLine(); 
        	       ++i;
        	    }
        */
        		
        		byte [] xbuf = new byte[65536];
        		int bcnt;

        		AssetFileDescriptor afd = mnact.getAssets().openFd("Track2.mp3");
        	    FileInputStream reader = afd.createInputStream();
        	    		
  //      	    mnact.textOut("File off: " + afd.getStartOffset() + "  Len: "+afd.getLength());

        		    bcnt = reader.read(xbuf);
        		    
        		    while (bcnt > 0 && i < 98) {
        		    	
        		    	mnact.toClients("DATA:"+Base64.encodeToString(xbuf,Base64.DEFAULT));
        		    	mnact.textOut("Srv sent : " + i + " Sz : "+ bcnt); 
        		    	bcnt = reader.read(xbuf); 
        		    	if (i == 5) {
        		    		mnact.textOut("Send CMD:PLAY");
        		    		mnact.toClients("CMD:PLAY");
        		    	}
        		       ++i;
        		    }
        	    reader.close();
        	} catch (IOException e) {
        		System.out.println( "File I/O error " + e);
        	}

        }

}

