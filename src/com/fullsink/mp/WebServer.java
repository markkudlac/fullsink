package com.fullsink.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.widget.Toast;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.fullsink.mp.Const.*;
/**
 * A simple WebSocketServer implementation.
 */
public class WebServer extends WebSocketServer {

	MainActivity mnact = null;
	int netlate = BASE_LATENCY;
	String copyfile;
	Thread fileThread = null;
	
        public WebServer( int port, String ipadd, MainActivity xmnact ) throws UnknownHostException {
        	
                super( new InetSocketAddress(
                InetAddress.getByName(ipadd), port ) );
                
                mnact = xmnact;
        }

        public WebServer( InetSocketAddress address ) {
                super( address );
        }

        @Override
        public void onOpen( WebSocket conn, ClientHandshake handshake ) {
 //               this.sendToAll( "CMD:MESS : new connection: " + handshake.getResourceDescriptor() );
        	conn.send(CMD_PING + System.currentTimeMillis());
                System.out.println("WebSocketServer client connected : " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }

        @Override
        public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        	System.out.println("onClose remote flag : " + remote);
                System.out.println("WebSocketServer client closed : " + conn);
        }

        @Override
        public void onMessage( WebSocket conn, String message ) {
        	// Receive messages from controller here
          System.out.println( "onMessage server : " + message );
            mnact.textOut( "onMessage server : " + message );
            
            if (message.startsWith(CMD_PING)) {	
            	conn.send(CMD_PONG + getArg(message));
            } else if (message.startsWith(CMD_INIT)) {	
            	conn.send(CMD_PREP + mnact.getCurrentTrackName());
            } else if (message.startsWith(CMD_READY)) {	
            	
            		if (mnact.track.isPlaying()) {
            			conn.send(CMD_PLAY + (mnact.track.getCurrentPosition()));
            		} else {
            			conn.send(CMD_SEEK + mnact.track.getCurrentPosition());
            		}
            		
        	} else if (message.startsWith(CMD_PONG)) {	
    			netlate = calcLatency(Long.parseLong(getArg(message)));
    		}  else if (message.startsWith(CMD_COPY)) {
    			filesizeCopyTrack(getArg(message), conn);
    		} else if (message.startsWith(CMD_CONNECT)) {
        		mnact.toastOut(getArg(message) + " " + mnact.getResources().getString(R.string.tunedin),Toast.LENGTH_LONG);
    		} else if (message.startsWith(CMD_ZIPPREP)) {
    			zipTrack(getArg(message), conn);
    		}
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
        
        
        public void deleteCues(){
        	
        	String[] xdump = mnact.getFilesDir().list();
    		for (int i=0; i< xdump.length; i++){
//    			System.out.println("Dump of cued bits : "+xdump[i]+"  : "+i);
    			if (!xdump[i].equals(HTML_DIR) && !xdump[i].equals(USERHTML_DIR)) {
    				DeleteRecursive(new File(mnact.getFilesDir(),xdump[i]));
    			}
       		}
        }
        
    
        
 static   void DeleteRecursive(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    DeleteRecursive(child);

            fileOrDirectory.delete();
        }
        
        
        public void cueTrack(File musicdir, String mfile){
 
        	try {
        		byte [] xbuf = new byte[BASE_BLOCKSIZE];     		
        		      		
           		//Parent directories need to be generated first
        		
        		int xind = mfile.lastIndexOf("/");   		
        		if (xind > 0){
        			String tmpdir = mfile.substring(0, xind);
        			new File(mnact.getFilesDir(),tmpdir).mkdirs();
        		}
        		
        		File trkFile = new File(mnact.getFilesDir(), mfile);
        		trkFile.createNewFile();
        		
           	    FileInputStream in = new FileInputStream(new File(musicdir, mfile));
 //       		System.out.println("Create input");        	    
        	    OutputStream out = new FileOutputStream(trkFile);
        	    
        	    // Transfer bytes from in to out
        	    int len;
        	    while ((len = in.read(xbuf)) > 0) {
        	        out.write(xbuf, 0, len);
        	    }
        	    in.close();
        	    out.close();
        	    
        	} catch (IOException e) {
        		System.out.println( "File I/O error " + e);
        	}
        }
      
        
       public void zipTrack(String mfile, WebSocket client){
 
        	try {
        		byte [] xbuf = new byte[BASE_BLOCKSIZE];     		
        		      		
           		// Take file path passed in and create zip file for it for download for web
        		// all directories will already exist
        		
        		String zipfl = mfile;		// find final file name to make zip file flat
        		int xind = zipfl.lastIndexOf("/");   		
        		if (xind > 0){
        			zipfl = zipfl.substring(xind + 1);
        		}
        		
        		File trkFile = new File(mnact.getFilesDir(), mfile);
   
        		System.out.println("Zip file path is : " + mnact.getFilesDir() + mfile + ".zip");
        		File zipFile = new File(mnact.getFilesDir(), mfile + ".zip");
        		
        		if (!zipFile.exists()) {
        			zipFile.createNewFile();
	           	    FileInputStream in = new FileInputStream(trkFile);   	    
		    	    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile)); 
		    	    zout.putNextEntry(new ZipEntry(zipfl)); // Make zipfile flat so no directories
		    	    
		    	    // Transfer bytes from in to out
		    	    int len;
		    	    while ((len = in.read(xbuf)) > 0) {
		    	        zout.write(xbuf, 0, len);
		    	    }
		    	    in.close();
		    	    zout.close();
        		}
        		client.send(CMD_ZIPREADY);
        	} catch (IOException e) {
        		System.out.println( "File I/O error " + e);
        	}
        }
        
        
        public static String getArg(String xstr){
        	
        	return(xstr.substring(xstr.indexOf(':')+1));
        }
        
        
       public static int calcLatency(long starttm){
        	
    	   int lag;
    	   
    	   lag = (int) (System.currentTimeMillis() - starttm);
    	   lag = lag / 2;
    	   
    	   System.out.println("Lag is : "+lag);
    	   	if (lag < BASE_LATENCY) lag = BASE_LATENCY;
    	   	
        	return(lag);
        }
       
       
       public void filesizeCopyTrack(String copyfile, WebSocket client) {
    	   
    	   // Determine file size
	        	File trkFile;
	        
	        	try {
		        	trkFile = new File(mnact.getFilesDir(),copyfile);
		        	client.send(CMD_FILE+ ((trkFile.length() / BASE_BLOCKSIZE)+1)); // Send length of file first
	        	} catch(Exception ex){
	        		client.send(CMD_FILE+ "-1");	// Error with file
	        		System.out.println("File transfer exception : "+ ex);
	        	}
    	   
       }
       

 
        public void initHTML(int webServerPort) {
        	
       	// Put this back later after testing complete
//       	if (! new File(mnact.getFilesDir(),HTML_DIR).isDirectory()) {
        	 
        	try {
        		
        		byte [] xbuf = new byte[BASE_BLOCKSIZE];  
        		File htmldest,htmlpar,userdir;
        		      		
           		//Parent directories need to be generated first
        		String[] afiles = mnact.getAssets().list(HTML_DIR);
        		userdir = new File(mnact.getFilesDir(),USERHTML_DIR);
        		htmlpar = new File(mnact.getFilesDir(),HTML_DIR);
        		
      //*******  This delete is here for testing now 
       // 		if (htmlpar.exists())	DeleteRecursive(htmlpar); 
      //*************
        		if (!userdir.exists()){
        			userdir.mkdirs();	//Create the user directory if it doesn't exit
        		}
        		
        		htmlpar.mkdirs();
        		generateServerId(htmlpar,webServerPort);
        		
        		// Copy contents of assets over to files
        		for (int i=0; i<afiles.length; i++){
        		//	System.out.println("Transfer : "+afiles[i]); 
        			
        			if (afiles[i].equals(FLSKINDEX)) {
        				htmldest = new File(mnact.getFilesDir(), "index.html");
        			} else {
        				htmldest = new File(htmlpar, afiles[i]);
        			}
            		
            		htmldest.createNewFile();
        		
            		InputStream in = mnact.getAssets().open(HTML_DIR+"/"+afiles[i]);    
            		OutputStream out = new FileOutputStream(htmldest);
        	    
        	    // Transfer bytes from in to out
            		int len;
            		while ((len = in.read(xbuf)) > 0) {
            			out.write(xbuf, 0, len);
            		}
            		in.close();
            		out.close();
        		}
        	    
        	} catch (IOException e) {
        		System.out.println( "File I/O error " + e);
        	}

 //      	}
        }
        
        
        private void generateServerId(File htmldir, int webSocketPort) {
        	
        	/*
        	 * 
        Server id JSON looks like 
        
        {
product:"FullSink",
id:"The server name from Prefs",
port: "12345",     //websocket port
}            
        	 */
        	
        	File serverid;
        	String builder;

        	builder = "{ \"service\":\""+ SERVICE_NAME+ "\", \"id\":\"" + Prefs.getAcountID(mnact) + "\", \"port\":\""+
    				webSocketPort;
        	
        	try {
	        	serverid = new File(htmldir,SERVERID_JS );
	    		serverid.createNewFile();
	    		
	    		OutputStream out = new FileOutputStream(serverid);
	    		out.write(builder.getBytes(), 0, builder.length());
	    		
	    		builder="\"}";
	    		out.write(builder.getBytes(), 0, builder.length());
	    		out.close();
        	} catch (IOException e) {
        		System.out.println( "File I/O error " + e);
        	}
        }
}

   
		/*	Various File writing routines
		 * 
       	int i = 0;
       	
       	
       	       		if (trkFile.exists()) {
        			mnact.textOut("File exists - remove");
        			trkFile.delete();
        		}
       	
       	
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
		
		int bcnt;

		AssetFileDescriptor afd = mnact.getAssets().openFd("Track2.mp3");
	    FileInputStream reader = afd.createInputStream();
	    		
     	    mnact.textOut("File off: " + afd.getStartOffset() + "  Len: "+afd.getLength());

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
	    */

/*
private class FileServer implements Runnable {
	
	   MainActivity mnact;
	   String fileCopy;
	   WebSocket client;
	   
	   	FileServer(MainActivity xact, String xfile, WebSocket client) {
	   		
	   		mnact = xact;
	   		fileCopy = xfile;
	   		this.client = client;
	   	}
	   
	   @Override
	   public void run() {
		   copyTrackToClient(fileCopy);    
	   }
	   
	      public void copyTrackToClient(String xtrk) {
	    	   
	    	   	byte [] xbuf = new byte[BASE_BLOCKSIZE];

	           	mnact.textOut("In copyTrackToClient : "+mnact.getFilesDir());  
	           	
	        	File trkFile;
	        	FileInputStream reader = null;
	        	
	        	try {
		        	trkFile = new File(mnact.getFilesDir(),xtrk);
		        	reader = new FileInputStream(trkFile);
	        	
		        	int bcnt;
		        	
		 		    bcnt = reader.read(xbuf);
		 		    //was mnact.toClients
		        	client.send(CMD_FILE+ ((trkFile.length() / BASE_BLOCKSIZE)+1)); // Send length of file first
		 		    
		        	 while (bcnt > 0) {
		 		    	
		        		 client.send(CMD_FILE+Base64.encodeToString(xbuf,Base64.DEFAULT));
		 		    	Thread.sleep(FILE_COPY_WAIT);

		 		    	bcnt = reader.read(xbuf);

		 		    }
		        	reader.close();
		        	client.send(CMD_FILE);  //End of file
	        	} catch(IOException ex){
	        		System.out.println("File exception : "+ ex);
	        	} catch (InterruptedException e) {

	        		if (reader != null) {
	        			System.out.println("Interupt caught");
	        			try {
	        				reader.close();
	        			} catch (Exception ex) {
	      	               return;
	      	           }     
	        		}
	               return;
	        	} catch (Exception e) {
	               return;
	           }     
	       }
}
*/ 

