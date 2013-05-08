package com.fullsink.mp;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.content.res.AssetFileDescriptor;
import android.util.Base64;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;


public class WebClient extends WebSocketClient {
	
	MainActivity mnact = null;
	String trackFile = "trackfile";
	File trkFile = null;
	Music clMusic = null;
	int skipFile = 0;
	
	public WebClient( int port, String ipadd, MainActivity xmnact ) throws URISyntaxException {
	
        super(
        		new URI("ws://192.168.1.105:8080")
        	);
        
        mnact = xmnact;
}
	
	@Override
    public void onMessage( String message ) {
//		System.out.println("Client onMess: " + message );
		
//		mnact.textOut("Cl Mess: " + message);
		if (message.startsWith("CMD:PLAY")) {	
    		outTrack();
		} else if (message.startsWith("DATA:")){
			mnact.textOut("CL Mess size : " + message.length());
			rcvTrack(message.substring(5));
		}
    }

    @Override
    public void onOpen( ServerHandshake handshake ) {
    	System.out.println( "You are connected to WebServer: " + getURI() );
    	
    	setFile(100);
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
    
    
    public void setFile(int xsize) {
    	mnact.textOut("In setFile : "+mnact.getFilesDir());    	

    	byte [] xbuf = new byte[65536];
		int i;
		
    	trkFile = new File(mnact.getFilesDir(),trackFile);
    	try {
    		
    	if (trkFile.exists()) {
    		mnact.textOut("File exists - remove");
    		trkFile.delete();
    	}
    	
    	trkFile.createNewFile();
    	skipFile = 0;
 /*   	
    	for (i=0; i<65536; i++){
    		xbuf[i] = '0';
    	}
    			
    	    FileOutputStream writer = new FileOutputStream(trkFile,true);
        	for (i=0; i<xsize; i++){
        		writer.write(xbuf);
        		writer.flush();
        	}
    	    writer.close();
    	    
    	    */
    	
    	AssetFileDescriptor afd = mnact.getAssets().openFd("Track3.mp3");
	    FileInputStream in = afd.createInputStream();
//        InputStream in = new FileInputStream();
        
        OutputStream out = new FileOutputStream(trkFile);

        // Transfer bytes from in to out

        int len;
        while ((len = in.read(xbuf)) > 0) {
            out.write(xbuf, 0, len);
        }
        in.close();
        out.close();
    	
    	
    	    mnact.textOut("Blank file sz : "+trkFile.length());
    	} catch (IOException e) {
    		System.out.println( "File write error " + e);
    	}

    }
    
    
    public void rcvTrack(String blk) {
    	
    	mnact.textOut("In rcvTrack");
    	byte [] xbuf;
    	
    	try {
    		
    	    RandomAccessFile writer = new RandomAccessFile(trkFile,"rw");
    	    xbuf = Base64.decode(blk,Base64.DEFAULT);
    	    mnact.textOut("CL write sze : " + xbuf.length + " skipFile : " + skipFile);
    	    writer.seek(skipFile);
    	    writer.write(xbuf);
    	    skipFile = skipFile + xbuf.length;
//    	    writer.flush();
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println( "File write error " + e);
    	}
    }


    public void outTrack() {
    	
    	mnact.textOut("In outTrack size : " + trkFile.length() );
    	
    	FileInputStream fis = null;
		try {
			fis = new FileInputStream(trkFile);
			clMusic = new Music(fis.getFD());
			clMusic.play();
		} catch (IOException e) {
    		System.out.println( "I/O outTrack " + e);
    	}
		
    	/*
    	try {
    	    BufferedReader reader = new BufferedReader(
    	        new FileReader(trkfile));

    	    // do reading, usually loop until end of file reading  
    	    String mLine = reader.readLine();
    	    while (mLine != null) {
    	    	mnact.textOut(mLine ); 
    	       mLine = reader.readLine(); 
    	    }

    	    reader.close();
    	} catch (IOException e) {
    		System.out.println( "File I/O error " + e);
    	}
    	*/
    }
   
}