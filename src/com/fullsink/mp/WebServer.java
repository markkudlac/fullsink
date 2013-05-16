package com.fullsink.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import static com.fullsink.mp.Const.*;

import android.content.res.AssetFileDescriptor;
import android.util.Base64;


/**
 * A simple WebSocketServer implementation.
 */
public class WebServer extends WebSocketServer {

	MainActivity mnact = null;
	int netlate = BASE_LATENCY;
	String copyfile;
	
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
        	mnact.toClients(CMD_PING + System.currentTimeMillis());
                System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " : CONNECTED" );
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
            
            if (message.startsWith(CMD_PING)) {	
            	mnact.toClients(CMD_PONG + getArg(message));
            } else if (message.startsWith(CMD_INIT)) {	
            	mnact.toClients(CMD_PREP + mnact.getCurrentTrackName());
            } else if (message.startsWith(CMD_READY)) {	
            	
            		if (mnact.track.isPlaying()) {
            			mnact.toClients(CMD_PLAY + (mnact.track.getCurrentPosition()));
            		} else {
            			mnact.toClients(CMD_SEEK + mnact.track.getCurrentPosition());
            		}
            		
        	} else if (message.startsWith(CMD_PONG)) {	
    			netlate = calcLatency(Long.parseLong(getArg(message)));
    		}  else if (message.startsWith(CMD_COPY)) {
    			controlCopyTrack(getArg(message));
    			
    		} else if (message.startsWith(CMD_CONNECT)) {
        		mnact.textOut(getArg(message) + " has connected");
    		} else {		
//        		this.sendToAll( message );
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
        
        public void cueTrack(String mfile){
        	
        	mnact.textOut("In cueTrack : " + mfile);
 
        	try {
        		
        		byte [] xbuf = new byte[65536];
        	    		
        		File trkFile = new File(mnact.getFilesDir(),mfile);
        		trkFile.createNewFile();
     		
        		AssetFileDescriptor afd = mnact.getAssets().openFd(mfile);
        	    FileInputStream in = afd.createInputStream();
        	    
        	    OutputStream out = new FileOutputStream(trkFile);

        	    // Transfer bytes from in to out

        	    int len;
        	    while ((len = in.read(xbuf)) > 0) {
        	        out.write(xbuf, 0, len);
        	    }
        	    in.close();
        	    out.close();
        	    
        	    mnact.textOut("File sz : "+trkFile.length());
        	    
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
       
       
       public void controlCopyTrack(String xfile) {
    	   
    	   if (xfile.contentEquals(CANCEL_COPY)) {
    		   mnact.textOut("Got cancel command");
    	   } else {
    		   copyfile = xfile;
    		   new Thread(new FileServer(mnact,xfile)).start();
 //   		   copyTrackToClient(xfile);
    	   }
       }
       
/*
       public void copyTrackToClient(String xtrk) {
    	   
    	   	byte [] xbuf = new byte[BASE_BLOCKSIZE];

           	mnact.textOut("In copyTrackToClient : "+mnact.getFilesDir());  
           	
        	File trkFile;
        	FileInputStream reader;
        	
        	try {
	        	trkFile = new File(mnact.getFilesDir(),xtrk);
	        	reader = new FileInputStream(trkFile);
        	
	        	int bcnt, i;
	 		    i = 0;
	        	 
	 		    bcnt = reader.read(xbuf);
	        	mnact.toClients(CMD_FILE+ ((trkFile.length() / BASE_BLOCKSIZE)+1)); // Send length of file first
	 		    
	        	 while (bcnt > 0) {
	 		    	
	 		    	mnact.toClients(CMD_FILE+Base64.encodeToString(xbuf,Base64.DEFAULT));
	 		    	android.os.SystemClock.sleep(100);
//	 		    	mnact.textOut("Srv sent : " + i + " Sz : "+ bcnt); 
	 		    	bcnt = reader.read(xbuf);
	 		    	++i;
	 		    }
	        	reader.close();
	        	mnact.toClients(CMD_FILE);  //End of file
        	} catch(Exception ex){
        		System.out.println("File exception : "+ ex);
        	}
       }
   */    
       
       
       private class FileServer implements Runnable {
       	
    	   MainActivity mnact;
    	   String fileCopy;
    	   
    	   	FileServer(MainActivity xact, String xfile) {
    	   		
    	   		mnact = xact;
    	   		fileCopy = xfile;
    	   	}
    	   
    	   @Override
    	   public void run() {

    	           try {
    	               copyTrackToClient(fileCopy);
    	           } catch (InterruptedException e) {
    	               return;
    	           } catch (Exception e) {
    	               return;
    	           }     
    	   }
    	   
    	      public void copyTrackToClient(String xtrk) throws InterruptedException {
    	    	   
    	    	   	byte [] xbuf = new byte[BASE_BLOCKSIZE];

    	           	mnact.textOut("In copyTrackToClient : "+mnact.getFilesDir());  
    	           	
    	        	File trkFile;
    	        	FileInputStream reader;
    	        	
    	        	try {
    		        	trkFile = new File(mnact.getFilesDir(),xtrk);
    		        	reader = new FileInputStream(trkFile);
    	        	
    		        	int bcnt, i;
    		 		    i = 0;
    		        	 
    		 		    bcnt = reader.read(xbuf);
    		        	mnact.toClients(CMD_FILE+ ((trkFile.length() / BASE_BLOCKSIZE)+1)); // Send length of file first
    		 		    
    		        	 while (bcnt > 0) {
    		 		    	
    		 		    	mnact.toClients(CMD_FILE+Base64.encodeToString(xbuf,Base64.DEFAULT));
    		 		    	Thread.sleep(100);
//    		 		    	android.os.SystemClock.sleep(100);
//    		 		    	mnact.textOut("Srv sent : " + i + " Sz : "+ bcnt); 
    		 		    	bcnt = reader.read(xbuf);
    		 		    	++i;
    		 		    }
    		        	reader.close();
    		        	mnact.toClients(CMD_FILE);  //End of file
    	        	} catch(Exception ex){
    	        		System.out.println("File exception : "+ ex);
    	        	}
    	       }

       }
       
}

/* 	

*/     
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


