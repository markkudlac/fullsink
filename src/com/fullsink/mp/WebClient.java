package com.fullsink.mp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static com.fullsink.mp.Const.*;

import android.net.Uri;
import android.os.Environment;
import android.widget.ListView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class WebClient extends WebSocketClient {
	
	MainActivity mnact = null;
	String copyTrack = null;
	String currentTrack = null;
	int netlate = BASE_LATENCY;
	String ipAddress;
	int httpdport;
	DownloadFile fileTask = null;
	
	public WebClient( int websocketport, String ipAddress, int httpdport, MainActivity mnact ) throws URISyntaxException {
	
        super(
        		new URI("ws://" + ipAddress + ":" + websocketport)
        	);
        
        this.mnact = mnact;
        this.ipAddress = ipAddress;
        this.httpdport = httpdport;
}
	
	@Override
    public void onMessage( String message ) {

		mnact.textOut("Cl Mess: " + message);
		
		if (message.startsWith(CMD_SEEK)) {	
			mnact.getTrack().seekTo(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PREP)) {	
			streamTrack(WebServer.getArg(message));
		} else if (message.startsWith(CMD_PLAY)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)) + netlate);
		} else if (message.startsWith(CMD_RESUME)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PAUSE)) {	
			mnact.setServerIndicator(MODE_PLAY);
			mnact.getTrack().pause();
		} else if (message.startsWith(CMD_STOP)) {	
			mnact.getTrack().dispose();
			mnact.setStreamTrack(null);
		} else if (message.startsWith(CMD_PONG)) {	
			netlate = WebServer.calcLatency(Long.parseLong(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PLAYING)) {
			setSongData(WebServer.getArg(message));
		} else if (message.startsWith(CMD_FILE)){
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
    	send(CMD_COPY + copyTrack);
    }
    
    
    public void rcvTrack(String blk) {
    	
    	int blktot = Integer.valueOf(blk);
    	
    	if (blktot < 0) {
			mnact.fileProgressControl(DOWNLOADERR);
    	} else {
			mnact.fileProgressControl(- blktot); // Set total progress
	    	fileTask = new DownloadFile(mnact, ipAddress, httpdport, blktot);
	    	fileTask.execute(copyTrack);
    	}
    }

    
    static public File targetCopyFile(String copyfile) {
    	File wrfile;
		String fileonly;
		
    		wrfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    		wrfile = new File(wrfile,MUSIC_DIR);
    		if (!wrfile.exists()) {		
    			wrfile.mkdir();
    		}
    		
    		fileonly = copyfile;
    		int xind = fileonly.lastIndexOf("/");   		
    		if (xind >= 0){
    			 fileonly = fileonly.substring(xind+1);
    		}
    		return (new File(wrfile,fileonly));
    }
    
    
    public void cancelFileCopy() {
    	
    	if (fileTask != null) {
    		fileTask.cancel(true);
    		fileTask = null;
    		try {
        		targetCopyFile(copyTrack).delete();
        		copyTrack = null;
        	} catch (Exception e) {
        		System.out.println( "Copy File delete error " + e);
        	}
    	}
    }
    
    
    public void streamTrack(String strmfile) {
    	
    	System.out.println("Ready to stream addr : " + ipAddress + "  Port : "+ httpdport);
    	currentTrack = strmfile;
    	String url = "http://" + ipAddress + ":" +
				httpdport + "/"+Uri.encode(strmfile);
    	System.out.println( "Stream URL : " +url);
    	mnact.setStreamTrack(new Music(url, mnact));

    }

    
    public void setSongData(String songdat) {
        	
    	// songdata has format title:album:artist
    	
    	String[] sdat = songdat.split(":",3);
    	int pos = mnact.serverlist.getCheckedItemPosition();
 
		if (pos != ListView.INVALID_POSITION ) {
			mnact.serveradapter.updateSongData(pos, sdat[0]);
		}
    }
    
    
    public String[] getSongData() {
    	
    	// songdata has format title:album:artist
    	String[] rtn = {"","",""};
    	int pos = mnact.serverlist.getCheckedItemPosition();
 
		if (pos != ListView.INVALID_POSITION ) {
			rtn = mnact.serveradapter.getSongData(pos);
		} 
		
		return(rtn);
		
    }
    
    
}


/*
class ServerSearch implements Runnable {
   	
	   MainActivity mnact;
	   ServerAdapter serveradapter;
	   
	   ServerSearch(MainActivity xact, ServerAdapter serveradapter) {
	   		
	   		mnact = xact;
	   		this.serveradapter = serveradapter;
	   	}
	   
	   @Override
	   public void run() {
		    Socket sock;
		    String addr;
	    
		    // This is mainly here for testing
		    addr = Prefs.getServerIPAddress(mnact);
		    
		    if (addr.length() > 8) {
				System.out.println("Preference connect Address : " + addr );
	    	    new HttpCom(mnact,serveradapter).execute(addr,Prefs.getHttpdPort(mnact).toString(),SERVERID_JS);
	    	    return;
			}
		    
		    addr = NetStrat.getWifiApIpAddress();
		    String baseaddr = addr.substring(0, addr.lastIndexOf('.') + 1);
//		    	sock = new Socket();
		    	
			    for (int i=190; i<=214; i++)
			    {
			    	sock = null;
			    	sock = new Socket();
			    	addr = baseaddr.concat(String.format("%d", i));
 			    	System.out.println("Address : " + addr +"  :  "+Prefs.getHttpdPort(mnact) );
			    	try {	    	       
			    	       sock.connect(new InetSocketAddress( addr, Prefs.getHttpdPort(mnact)), 150);
//			    	       sock.connect(new InetSocketAddress( addr, Prefs.getSocketPort(mnact)), 500);
			    	       sock.close();
			    	       
//			    	       mnact.WClient = new WebClient(Prefs.getSocketPort(mnact), addr, mnact);
//			    	       mnact.WClient.connect();
		 			    	System.out.println("Server found Address : " + addr );
			    	       new HttpCom(mnact,serveradapter).execute(addr,Prefs.getHttpdPort(mnact).toString(),SERVERID_JS);

			    	     }
			    	catch (Exception ex) {
//			    	       System.out.println("Websocket NOT found on IP:" + addr + "  : "+ex);
			    	    }
			    }
			    
			    System.out.println("Websocket search complete : " + addr);
	   }
}

*/

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
