package com.fullsink.mp;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


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
                this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
 //               this.sendToAll( "new connection: " );
                System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
        }

        @Override
        public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
                this.sendToAll( conn + " closed" );
                System.out.println( conn + " closed" );
        }

        @Override
        public void onMessage( WebSocket conn, String message ) {
        	// Receive messages from controller here
          System.out.println( "onMessage server : " + conn + ": " + message );
            mnact.textOut( "onMessage server : " + ": " + message );
        	if (message.contains("TRACK")) {	
        		mnact.sendTrack();
    		} else {		
        		this.sendToAll( message );
    		}

          this.sendToAll( message );
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
}

