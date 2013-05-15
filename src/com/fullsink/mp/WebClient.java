package com.fullsink.mp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import static com.fullsink.mp.Const.*;

import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.util.Base64;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebClient extends WebSocketClient {
	
	MainActivity mnact = null;
	String copyTrack = null;
	String currentTrack = null;
	boolean fileAppend = false;
	int netlate = BASE_LATENCY;
	
	public WebClient( int port, String ipadd, MainActivity xmnact ) throws URISyntaxException {
	
        super(
//        		new URI("ws://192.168.1.105:8080")
        		new URI("ws://" + ipadd + ":" + port)
        	);
        
        mnact = xmnact;
}
	
	@Override
    public void onMessage( String message ) {

			// Don't dump out FILE message as it is huge
		if (! message.startsWith(CMD_FILE)){
//			System.out.println("Client onMess: " + message );
			mnact.textOut("Cl Mess: " + message);
		}
		
		if (message.startsWith(CMD_SEEK)) {	
			mnact.getTrack().seekTo(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PREP)) {	
			streamTrack(WebServer.getArg(message));
		} else if (message.startsWith(CMD_PLAY)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)) + netlate);
		} else if (message.startsWith(CMD_RESUME)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PAUSE)) {	
			mnact.getTrack().pause();
		} else if (message.startsWith(CMD_STOP)) {	
			mnact.getTrack().dispose();
		} else if (message.startsWith(CMD_PONG)) {	
			netlate = WebServer.calcLatency(Long.parseLong(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_FILE)){
			mnact.textOut("CL Mess size : " + message.length());
			rcvTrack(message.substring(5));
		} else if (message.startsWith(CMD_PING)) {	
        	send(CMD_PONG + WebServer.getArg(message));
        }
    }

    @Override
    public void onOpen( ServerHandshake handshake ) {
    	System.out.println( "You are connected to WebServer: " + getURI() );
    	
    	send(CMD_CONNECT + Prefs.getAcountID(mnact));
    	send(CMD_PING + System.currentTimeMillis());
    	send(CMD_INIT);
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
    	System.out.println( "Disconnected from: " + getURI() + "; Code: " + code + " " + reason );
    }

    @Override
    public void onError( Exception ex ) {
    	System.out.println( "Exception occured:\n" + ex );
    }
    

    public void startCopyFile(){
    	
    	copyTrack = currentTrack;
    	fileAppend = false;
    	send(CMD_COPY + copyTrack);
    }
    
    
    public void rcvTrack(String blk) {
    	
    	byte [] xbuf;
    	File wrfile = null;
    	
    	try {
    		wrfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    		wrfile = new File(wrfile,"FullSink");
    		if (!wrfile.exists()) {
    			
    			wrfile.mkdir();
    		}
    		
    		wrfile = new File(wrfile,copyTrack);
    		
    	    FileOutputStream writer = new FileOutputStream(wrfile,fileAppend);
    	    fileAppend = true;
    	    xbuf = Base64.decode(blk,Base64.DEFAULT);

    	    writer.write(xbuf);
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println( "File write error " + e);
    	}
    }

    
    
    public void streamTrack(String strmfile) {
    		
    	currentTrack = strmfile;
    	mnact.setStreamTrack(new Music("http://" + Prefs.getServerIPAddress(mnact) + ":" +
    					Prefs.getHttpdPort(mnact) + "/"+strmfile, mnact));

    }


}

/*
 Various file reading routines
 */

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


******************
*
*
//    	FileInputStream fis = null;
    	try {
//    		fis = new FileInputStream(trkFile);
//    		clMusic = new Music(fis.getFD());
    		if (clMusic == null) {
    			clMusic = new Music("http://" + Prefs.getServerIPAddress(mnact) + ":" + Prefs.getHttpdPort(mnact) + "/trkfile.mp3");
    		} else {
    			clMusic.play();
    		}
//    		clMusic = new Music(getAssets().openFd("Track3.mp3"));
//    		clMusic.play();
    	} catch (Exception e) {
    		System.out.println( "I/O outTrack " + e);
    	}
    	
    	
    	************************
    	*
    	*    	
    	*    try {
    		
    	    RandomAccessFile writer = new RandomAccessFile(trkFile,"rw");
    	    xbuf = Base64.decode(blk,Base64.DEFAULT);
    	    mnact.textOut("CL write sze : " + xbuf.length + " skipFile : " + skipFile);
    	    writer.seek(skipFile);
    	    writer.write(xbuf);
    	    skipFile = skipFile + xbuf.length;
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println( "File write error " + e);
    	}
    }

*/


/*
public void setFile(int xsize) {
	
	byte [] xbuf = new byte[65536];

   	mnact.textOut("In setFile : "+mnact.getFilesDir());  
   	
	trkFile = new File(mnact.getFilesDir(),trackFile);
	try {
		
	if (trkFile.exists()) {
		mnact.textOut("File exists - remove");
		trkFile.delete();
	}
	
	trkFile.createNewFile();
	skipFile = 0;

	
	AssetFileDescriptor afd = mnact.getAssets().openFd(TRKFILE);
    FileInputStream in = afd.createInputStream();
//    InputStream in = new FileInputStream();
    
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
*/  
