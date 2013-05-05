package com.fullsink.mp;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;


import java.net.URI;
import java.net.URISyntaxException;


public class WebClient extends WebSocketClient {
	
	MainActivity mnact = null;
	
	public WebClient( int port, String ipadd, MainActivity xmnact ) throws URISyntaxException {
	
        super(
        		new URI("ws://192.168.1.105:8080")
        	);
        
        mnact = xmnact;
}
	
	@Override
    public void onMessage( String message ) {
		System.out.println("Client onMess: " + message );
		
		mnact.textOut("Client onMess: " + message);

    }

    @Override
    public void onOpen( ServerHandshake handshake ) {
    	System.out.println( "You are connected to WebServer: " + getURI() );
    	send("CMD:TRACK");
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
    	System.out.println( "Disconnected from: " + getURI() + "; Code: " + code + " " + reason );
    }

    @Override
    public void onError( Exception ex ) {
    	System.out.println( "Exception occured:\n" + ex );
    }
}