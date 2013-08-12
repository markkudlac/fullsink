package com.fullsink.mp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static com.fullsink.mp.Const.*;

import android.net.Uri;
import android.os.Environment;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class WebClient extends WebSocketClient {
	
	private MainActivity mnact;
	private String copyTrack;
	private String currentTrack;
	private String ipAddress;
	private int httpdport;
	private DownloadFile fileTask;
 	private boolean downloadEnabled;
	
	public WebClient( int websocketport, String ipAddress, int httpdport, MainActivity mnact ) throws URISyntaxException {
	
        super(
        		new URI("ws://" + ipAddress + ":" + websocketport)
        	);
        
        this.mnact = mnact;
        this.ipAddress = ipAddress;
        this.httpdport = httpdport;
        copyTrack = null;
        currentTrack = null;
        fileTask = null;
        downloadEnabled = true;
}
	
	@Override
    public void onMessage(String message ) {

		System.out.println("Cl Mess: " + message);
		
		if (message.startsWith(CMD_SEEK)) {	
			mnact.getTrack().seekTo(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PREP)) {	
			streamTrack(WebServer.getArg(message));
		} else if (message.startsWith(CMD_PLAY)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_RESUME)) {	
			mnact.playStream(Integer.parseInt(WebServer.getArg(message)));
		} else if (message.startsWith(CMD_PAUSE)) {	
			mnact.setServerIndicator(MODE_PLAY);
			mnact.getTrack().pause();
		} else if (message.startsWith(CMD_STOP)) {	
			mnact.getTrack().dispose();
			mnact.setStreamTrack(null);
		} else if (message.startsWith(CMD_PLAYING)) {
			setSongData(WebServer.getArg(message));
		} else if (message.startsWith(CMD_FILE)){
			rcvTrack(message.substring(5));
        } else if (message.startsWith(CMD_DOWNEN)) {	
        	manageDisableDownload(WebServer.getArg(message));
        }
    }
	
    @Override
    public void onOpen( ServerHandshake handshake ) {
    	System.out.println( "You are connected to WebServer: " + getURI() );
   	
    	send(CMD_CONNECT + Prefs.getName(mnact));
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
    
    
    public void manageDisableDownload(String dwncode){
    	
    	boolean tdown;
    	tdown = dwncode.equals("T");
    	
    	if (downloadEnabled != tdown) {
    		
    		String state;
    		
    		if (!tdown)  state = "disabled";
    		else state = "enabled";
    		
    		mnact.toastOut("Server "+state+" download",Toast.LENGTH_LONG);
    		downloadEnabled = tdown;
        	mnact.setDownload(downloadEnabled);
    	}
    }
    
    
    public boolean getDownload() {
    	return downloadEnabled;
    }
 
    
    public void startCopyFile(){
    	copyTrack = currentTrack;
    	send(CMD_COPY + copyTrack);
    }
    
    public String getCurrentTrack(){
    	return currentTrack;
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
    	
    	for (int i=0; i<sdat.length; i++){
    		sdat[i] = mapColonSub(sdat[i]);
    	}
    	
    	int pos = mnact.serverlist.getCheckedItemPosition();
 
		if (pos != ListView.INVALID_POSITION ) {
			mnact.serveradapter.updateSongData(pos, sdat[0]);
		}
    }
    
    
    private String mapColonSub(String str) {
    	
    	return(str.replaceAll(COLON_SUB,":"));
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
    
    
    // This is not being used now as it needs the nsd service leave in though
    public static void autoSelect(final MainActivity mnact, final int cnt) {
    	System.out.println("In autoSel");
    	
    	new Thread(new Runnable() {

            public void run() {
        		try {
	            	System.out.println("Before sleep in autoSel");
	            	
	            	Thread.sleep(3000);
	            	System.out.println("After sleep in autoSel");
	            	if (mnact.serverlist.getCount() > 0) {
	            		mnact.runOnUiThread(new Runnable() {
	            	        public void run() {
	            	        	String xtime = "time";
	            	        	if (cnt > 1) xtime += "s";
	            	        	if (cnt > 0) {
	            	        	Toast.makeText(mnact.getBaseContext(), "Client will auto connect "+cnt+
	            	        			" more "+xtime, Toast.LENGTH_LONG).show();
	            	        	}
	            	        	mnact.callLocal();
	            	        	mnact.serverlist.setItemChecked(0, true);
	            	        	mnact.serveradapter.serverSelected(0);	//This is bad but performClick didn't work
	            	        											// as onClickItem works though
	            	        }
	            		});
	            		System.out.println("Items in list");
	            	} else {
	            		System.out.println("NO Items in list");
	            	}
           
        		} catch(Exception ex) {
        			System.out.println("Select thread exception : "+ex);
        		}
            }
        }).start();
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

