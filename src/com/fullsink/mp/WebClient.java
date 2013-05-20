package com.fullsink.mp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static com.fullsink.mp.Const.*;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class WebClient extends WebSocketClient {
	
	MainActivity mnact = null;
	String copyTrack = null;
	String currentTrack = null;
	int fileCount = 0;
	int netlate = BASE_LATENCY;
	
	public WebClient( int port, String ipadd, MainActivity xmnact ) throws URISyntaxException {
	
        super(
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
			mnact.setStreamTrack(null);
		} else if (message.startsWith(CMD_PONG)) {	
			netlate = WebServer.calcLatency(Long.parseLong(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_FILE)){
//			mnact.textOut("CL Mess size : " + message.length());
			rcvTrack(message.substring(5));
		} else if (message.startsWith(CMD_PING)) {	
        	send(CMD_PONG + WebServer.getArg(message));
        } else if (message.startsWith(CMD_NAME)) {	
        	mnact.textOut("Found server : " + WebServer.getArg(message));
        }
    }

    @Override
    public void onOpen( ServerHandshake handshake ) {
    	System.out.println( "You are connected to WebServer: " + getURI() );
 /*   	
    	send(CMD_CONNECT + Prefs.getAcountID(mnact));
    	send(CMD_PING + System.currentTimeMillis());
    	send(CMD_INIT);
    	*/
    	
	       send(CMD_NAME);
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
    	System.out.println( "Disconnected from: " + getURI() + "; Code: " + code + " " + reason );
    }

    @Override
    public void onError( Exception ex ) {
    	System.out.println( "Exception occured:\n" + ex );
    }
    
    
    public void scanForServers() {
    	
    	new Thread(new ServerSearch(mnact)).start();
    }
    
 
    
    public void startCopyFile(){
    	
    	copyTrack = currentTrack;
    	fileCount = 0;
    	send(CMD_COPY + copyTrack);
    }
    
    
    public void rcvTrack(String blk) {
    	
    	byte [] xbuf;
    	File wrfile = null;
    	
    	if (fileCount < 0) {
    		return;
    	} else if (fileCount == 0 && blk.length() < 9) {
 //   		mnact.textOut("File start : "+ blk);
    		mnact.fileProgressControl(-(Integer.valueOf(blk)));
    		return;
    	} else if (blk.isEmpty()) {
    		mnact.fileProgressControl(0);
    		fileCount = 0;
    		return;
    	}
    	
    	try {

    		wrfile = targetCopyFile();
    		
    	    FileOutputStream writer = new FileOutputStream(wrfile,fileCount > 0);
    	    ++fileCount;
    	    mnact.fileProgressControl(fileCount);
    	    xbuf = Base64.decode(blk,Base64.DEFAULT);

    	    writer.write(xbuf);
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println( "File write error " + e);
    	}
    }

    
    public File targetCopyFile() {
    	File wrfile;
		String fileonly;
		
    		wrfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    		wrfile = new File(wrfile,"FullSink");
    		if (!wrfile.exists()) {		
    			wrfile.mkdir();
    		}
    		
    		fileonly = copyTrack;
    		int xind = fileonly.lastIndexOf("/");   		
    		if (xind >= 0){
    			 fileonly = fileonly.substring(xind+1);
    		}
    		return (new File(wrfile,fileonly));
    }
    
    
    public void cancelFileCopy() {

    	send(CMD_COPY + CANCEL_COPY);
    	android.os.SystemClock.sleep(1000);
    	
    	if (fileCount > 0) {
    		
    		fileCount = -1;
    		try {
        		targetCopyFile().delete();
        	} catch (Exception e) {
        		System.out.println( "Copy File delete error " + e);
        	}
    	}
    }
    
    
    public void streamTrack(String strmfile) {
    		
    	currentTrack = strmfile;
    	String url = "http://" + Prefs.getServerIPAddress(mnact) + ":" +
				Prefs.getHttpdPort(mnact) + "/"+Uri.encode(strmfile);
    	System.out.println( "Stream URL : " +url);
    	mnact.setStreamTrack(new Music(url, mnact));

    }

}


class ServerSearch implements Runnable {
   	
	   MainActivity mnact;
  
	   ServerSearch(MainActivity xact) {
	   		
	   		mnact = xact;
	   	}
	   
	   @Override
	   public void run() {
		    Socket sock;
		    String addr = NetStrat.getWifiApIpAddress();

		    
		    String baseaddr = addr.substring(0, addr.lastIndexOf('.') + 1);
		    	sock = new Socket();
		    	
			    for (int i=1; i<=254; i++)
			    {
			    	addr = baseaddr.concat(String.format("%d", i));
//			    	System.out.println("Address : " + addr +"  :  "+Prefs.getSocketPort(mnact) );
			    	try {	    	       
			    	       sock.connect(new InetSocketAddress( addr, Prefs.getSocketPort(mnact)), 500);
			    	       System.out.println("Websocket found on IP:" + addr );
			    	       sock.close();
			    	       
			    	       mnact.WClient = new WebClient(Prefs.getSocketPort(mnact), addr, mnact);
			    	       mnact.WClient.connect();

			    	     }
			    	catch (Exception ex) {
//			    	       System.out.println("Websocket NOT found on IP:" + addr );
			    	    }
			    }
			    
			    System.out.println("Websocket search complete : " + addr);
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
