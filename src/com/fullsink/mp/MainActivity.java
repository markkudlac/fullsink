package com.fullsink.mp;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.java_websocket.WebSocketImpl;
import static com.fullsink.mp.Const.*;
import fi.iki.elonen.SimpleWebServer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable{
	
	public static WebServer WServ = null;	
	public static SimpleWebServer HttpdServ = null;
	public static WebClient WClient = null;
	
	WakeLock wakeLock;
	private static final String[] EXTENSIONS = { ".mp3", ".mid", ".wav", ".ogg", ".mp4" }; //Playable Extensions
	List<String> trackNames; //Playable Track Titles
	AssetManager assets; //Assets (Compiled with APK)
	File path; //directory where music is loaded from on SD Card

	Music track; //currently loaded track

	TextView textout;  // message window
	SeekBar seekbar;
	ProgressBar progressbar;
	ProgressDialog progressdialog = null;
	
	Button btnPlay; //The play button will need to change from 'play' to 'pause', so we need an instance of it
	Random random; //used for shuffle
	boolean isTuning; //is user currently jammin out, if so automatically start playing the next track
	int currentTrack; //index of current track selected
	int type; //0 for loading from assets, 1 for loading from SD card
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
// was this		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Lexiconda");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lexiconda");
        setContentView(R.layout.activity_main);

        initialize(0);
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	wakeLock.acquire();
    }
	
    @Override
	public void onPause(){
		super.onPause();
		wakeLock.release();
		if(track != null){
			if(track.isPlaying()){
				track.pause();
				isTuning = false;
				btnPlay.setBackgroundResource(R.drawable.play);
			}
			if(isFinishing()){
				track.dispose();
				finish();
			}
		} else{
			if(isFinishing()){
				finish();
			}
		}
	}
    
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		
		stopSockServer();
		stopHttpdServer();
		stopSockClient();
	}
    
	
    @Override
    public void run() {
        int currentPosition= 0;
 
        while (track != null) {
            try {
                Thread.sleep(1000);
                currentPosition= getTrack().getCurrentPosition();
 //       		textOut("In barThread Position : "+ currentPosition);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }     
            
            if (track.isPlaying()) {
            	if (inClient()){
//            		System.out.println("in Client before getDuration");
            		progressbar.setMax(getTrack().getDuration());
            		progressbar.setProgress(currentPosition);
            	} else {
            		seekbar.setProgress(currentPosition);
            	}
            }
        }
    }
    
    
    private void initialize(int type){
 
    	textout = (TextView) findViewById(R.id.textout);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setBackgroundResource(R.drawable.play);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
//        barThread = new Thread(this);
        
        seekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
        	{
        		public void onProgressChanged(SeekBar seekBar, int progress,
                                        boolean fromUser)
        		{
                                        // TODO Auto-generated method stub
                                     //   textOut("SeekBar value is "+progress);
                        }

                        public void onStartTrackingTouch(SeekBar seekBar)
                        {
                        	textOut("SeekBar start touch "); 
                        	toClients(CMD_PAUSE);
                        	track.pause();
                        	// TODO Auto-generated method stub
                        }

                        public void onStopTrackingTouch(SeekBar seekBar)
                        {
                        	int xseek;
                        	
                        	xseek = seekBar.getProgress();
                        	textOut("SeekBar end touch progress val: " + xseek); 
                        	track.seekTo(xseek);
 //                       	toClients(CMD_SEEK + xseek);
                        	toClients(CMD_RESUME + xseek);
                        	track.play();
                                        // TODO Auto-generated method stub
                        }
});
        
        
    	trackNames = new ArrayList<String>();
    	assets = getAssets();
    	currentTrack = 0;
    	isTuning = false;
    	random = new Random();
    	this.type = type;
    	
    	addTracks(getTracks());
    	loadTrack();
    }
    
    
    public void setStreamTrack(Music xtrk) {
    	synchronized(this) {		// May not be needed not sure on Sync
    		track = xtrk;
    	}
    }
    
    
    public Music getTrack() {
    	synchronized(this) {	// May not be needed not sure on Sync
    		return(track);
    	}
    }
    
    
    
    //Generate a String Array that represents all of the files found
    private String[] getTracks(){
    	if(type == 0){
    		try {
    			String[] temp = getAssets().list("");
    			return temp;
    		} catch (IOException e) {
    			e.printStackTrace();
    			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    		}
    	} else if(type == 1){
    		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) 
        			|| Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
        		path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

    			String[] temp = path.list();
    			return temp;
    		} else{
    			Toast.makeText(getBaseContext(), "SD Card is either mounted elsewhere or is unusable", Toast.LENGTH_LONG).show();
    		}
    	}
    	return null;
    }
    
    //Adds the playable files to the trackNames List
    private void addTracks(String[] temp){
    	if(temp != null){
			for(int i = 0; i < temp.length; i++){
				//Only accept files that have one of the extensions in the EXTENSIONS array
				if(trackChecker(temp[i])){
					trackNames.add(temp[i]);
				}
			}
			Toast.makeText(getBaseContext(), "Loaded " + Integer.toString(trackNames.size()) + " Tracks", Toast.LENGTH_SHORT).show();
		}
    }
    
    //Checks to make sure that the track to be loaded has a correct extenson
    private boolean trackChecker(String trackToTest){
    	for(int j = 0; j < EXTENSIONS.length; j++){
			if(trackToTest.contains(EXTENSIONS[j])){
				return true;
			}
		}
    	return false;
    }
    
    //Loads the track by calling loadMusic
    private void loadTrack(){
    	if(track != null){
    		toClients(CMD_STOP);
    		track.dispose();
    		track = null;
    	}
    	
    	if(trackNames.size() > 0){
    		track = loadMusic(type);
    	}
    }
    
	//loads a Music instance using either a built in asset or an external resource
    private Music loadMusic(int type){
 
		Music xmu;
	
		if (WServ != null){
			WServ.cueTrack(getCurrentTrackName());
		}
		
    	switch(type){
    	case 0:
    		

    		try{
    			AssetFileDescriptor assetDescriptor = assets.openFd(getCurrentTrackName());
    			xmu = new Music(assetDescriptor,this);
    			toClients(CMD_PREP + getCurrentTrackName());	// make sure music play is loaded
    			return xmu;
    		} catch(IOException e){
    			e.printStackTrace();
    			Toast.makeText(getBaseContext(), "Error Loading " + getCurrentTrackName(), Toast.LENGTH_LONG).show();
    		}
    		return null;
    		
    	case 1:
    		try{
    			FileInputStream fis = new FileInputStream(new File(path, getCurrentTrackName()));
    			FileDescriptor fileDescriptor = fis.getFD();
    			xmu =  new Music(fileDescriptor,this);
    			toClients(CMD_PREP + getCurrentTrackName());	// make sure music play is loaded
    			return xmu;
    		} catch(IOException e){
    			e.printStackTrace();
    			Toast.makeText(getBaseContext(), "Error Loading " + getCurrentTrackName(), Toast.LENGTH_LONG).show();
    		}
    		return null;
    		
    	default:
    		return null;
    	}
    }
    
    
    public String getCurrentTrackName(){
    	return(trackNames.get(currentTrack));
    }
    
    
    public void playStream(int offset) {
    	textOut("in playStream offset : " + offset);
    	
    	if (track != null){
        	try {
        		getTrack().seekTo(offset);
        		progressbar.setMax(track.getDuration());
        		getTrack().play();
        	   	
        	   	new Thread(this).start();
        	   	/*
        		if (!barThread.isAlive()) {
        			barThread.start();
        		}
        		*/
        	} catch (Exception e) {
                    textOut("Thread exception : "+ e); 
            }   
    	}	
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    
	public void toSettings(MenuItem item) {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
	}
  
    
    /*****************************************  LOOK HERE *******************************/
    
    public void click(View view){
		int id = view.getId();
		switch(id){
		case R.id.btnShare:	
			if (((Button) view).getText().equals(
					getResources().getString(R.string.serverbutOff))) {
				String ipadd = NetStrat.getWifiApIpAddress();
				System.out.println("WebSock Port : " + Prefs.getSocketPort(this) + "  IPADD : " + ipadd);
				
				startHttpdServer(Prefs.getHttpdPort(this), ipadd);
				startSockServer(Prefs.getSocketPort(this),ipadd);
				((Button) view).setText(getResources().getString(R.string.serverbutOn));
				findViewById(R.id.btnConnect).setEnabled(false);
			} else {
				stopSockServer();
				stopHttpdServer();
			
				((Button) view).setText(getResources().getString(R.string.serverbutOff));
				findViewById(R.id.btnConnect).setEnabled(true);
			}
			return;
			
		case R.id.btnConnect:		
			if (((Button) view).getText().equals(
					getResources().getString(R.string.clientbutOff))) {
				startSockClient(Prefs.getSocketPort(this), Prefs.getServerIPAddress(this));
				((Button) view).setText(getResources().getString(R.string.clientbutOn));
				if (track != null) {
					track.dispose();
					track = null;
				}
				findViewById(R.id.btnShare).setEnabled(false);
				findViewById(R.id.seekbar).setVisibility(View.GONE);
				progressbar.setProgress(0);
				findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				findViewById(R.id.mediabuts).setVisibility(View.GONE);
				findViewById(R.id.clientbuts).setVisibility(View.VISIBLE);
			} else {
				stopSockClient();
				((Button) view).setText(getResources().getString(R.string.clientbutOff));
				findViewById(R.id.btnShare).setEnabled(true);
				findViewById(R.id.seekbar).setVisibility(View.VISIBLE);
				findViewById(R.id.progressbar).setVisibility(View.GONE);
				findViewById(R.id.mediabuts).setVisibility(View.VISIBLE);
				findViewById(R.id.clientbuts).setVisibility(View.GONE);
				
				isTuning = false;
				btnPlay.setBackgroundResource(R.drawable.play);
				seekbar.setProgress(0);
				loadTrack();
			}
			return;

			
		case R.id.btnPlay:
			synchronized(this){
//				textOut("Shuffle : " + Prefs.getShuffle(this));
				
				if(isTuning){
					toClients(CMD_PAUSE);
					isTuning = false;
					btnPlay.setBackgroundResource(R.drawable.play);
					track.pause();
				} else{
					isTuning = true;
					btnPlay.setBackgroundResource(R.drawable.pause);
					playTrack(true);
				}
			}
			return;
			
		case R.id.btnPrevious:
			setTrack(0);
			loadTrack();
			playTrack(false);
			return;
			
		case R.id.btnNext:
			butNext();
			return;
			
		case R.id.btnclientMute:
			if (((Button) view).getText().equals(
					getResources().getString(R.string.clientbutMute))) {
				((Button) view).setText(getResources().getString(R.string.clientbutMuted));
				Toast.makeText(getBaseContext(), "Muted", Toast.LENGTH_SHORT).show();
				if (track != null) {
					track.setVolume(0f,0f);
				}
			} else {
				((Button) view).setText(getResources().getString(R.string.clientbutMute));
				
				if (track != null) {
					track.setVolume(1f,1f);
				}
			}
			return;
			
		case R.id.btnclientCopy:
//			Toast.makeText(getBaseContext(), "Copy : "+ WClient.currentTrack, Toast.LENGTH_SHORT).show();
			progressdialog = new ProgressDialog(this);
			progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			
			progressdialog.setMax(100);
			progressdialog.setProgress(0);
			
			progressdialog.setMessage("Copying : " + WClient.currentTrack);
			progressdialog.setCancelable(false);
			
			progressdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	WClient.cancelFileCopy();
			        dialog.dismiss();
			    }
			});
			
			progressdialog.show();

			WClient.startCopyFile();
			return;
			
		default:
			return;
		}
	}
    
    
    
    private void butNext() {
    	
    	setTrack(1);
		loadTrack();
		playTrack(false);
    }
    
    
    private void setTrack(int direction){
    	if(direction == 0){
    		currentTrack--;
			if(currentTrack < 0){
				currentTrack = trackNames.size()-1;
			}
    	} else if(direction == 1){
    		currentTrack++;
			if(currentTrack > trackNames.size()-1){
				currentTrack = 0;
			}
    	}
    	
    	if(Prefs.getShuffle(this)){
			int temp = random.nextInt(trackNames.size());
			while(true){
				if(temp != currentTrack){
					currentTrack = temp;
					break;
				}
				temp++;
				if(temp > trackNames.size()-1){
					temp = 0;
				}
			}
		}
    }
    
    //Plays the Track
    private void playTrack(boolean resume){
    	if(isTuning && track != null){
    				 		
       		if (WServ != null && resume) {
           		toClients(CMD_RESUME + track.getCurrentPosition());
       			android.os.SystemClock.sleep(WServ.netlate);
       		} 
       		
			track.play();
			
	    	seekbar.setMax(track.getDuration());
	    	try {
	    		
	    		new Thread(this).start();
	    		/*
	    		if (!barThread.isAlive()) {
	    			barThread.start();
	    		}
	    		*/
	    	} catch (Exception e) {
	                textOut("Thread exception : "+ e); 
	            }   
	    	
			Toast.makeText(getBaseContext(), "Playing " + getCurrentTrackName().substring(0,
					getCurrentTrackName().length()-4), Toast.LENGTH_SHORT).show();
		}
    }
    
    
    
public void playNextTrack() {
	
	if (!inClient()) {
		textOut("In playNextTrack should not be client");
		butNext();
	}
}



public void startSockServer(int port, String ipadd) {
    WebSocketImpl.DEBUG = false;		//This was true originally
    
   try {
	 
    WServ = new WebServer( port, ipadd, MainActivity.this );
    WServ.cueTrack(getCurrentTrackName());		//Copy for stream
    WServ.start();       

    System.out.println( "WebSockServ started on port: " + WServ.getPort() );
    textOut("WebSockServ started");
    textOut("Address : " + WServ.getAddress());
   } catch ( Exception ex ) {
	   System.out.println( "WebSockServer host not found error" + ex);
   }
  }

public void stopSockServer() {
	
	try {
		if (WServ != null) {
			WServ.stop();
			WServ = null;
		}
	} catch(Exception ex ) {
	   System.out.println( "WebSockServer stop error" + ex);
   }
}

public void startSockClient(int port, String ipadd){
	WebSocketImpl.DEBUG = false;		//This was true originally
	try {

		 WClient = new WebClient(port, ipadd, MainActivity.this);
		 WClient.connect();
		 
	} catch ( Exception ex ) {
		   System.out.println( "WebClient error : " + ex);
	   }
	}


public void toClients(String mess){
	
	if (WServ != null){
		WServ.sendToAll(mess);
	}
}


public void stopSockClient() {
	
	try {
		if (WClient != null) {
			WClient.close();
			WClient = null;
		}
	} catch(Exception ex ) {
		   System.out.println( "WebSockClient stop error" + ex);
	   }
	}


public boolean inClient() {
	return(WClient != null);
}


public void startHttpdServer(int port, String ipadd) {
    
   try {
    
    HttpdServ = new SimpleWebServer( ipadd, port, getFilesDir() ); 
    HttpdServ.start();       
    textOut("HttpdServ started");
    textOut("Address : " + ipadd + "  Port : "+port);
   } catch ( Exception ex ) {
	   System.out.println( "HttpdServer error : " + ex);
   }
  }


public void stopHttpdServer() {
	
	try {
		if (WServ != null) {
			WServ.stop();
			WServ = null;
		}
	} catch(Exception ex ) {
	   System.out.println( "HttpdServer stop : " + ex);
   }
}

	
public void textOut(final String xmess){

	runOnUiThread(new Runnable() {
        public void run() {
        	textout.append(xmess + "\n");
        }
    });
}


public void fileProgressControl(final int xprog){

	runOnUiThread(new Runnable() {
        public void run() {
        	if (xprog == 0) {
        		progressdialog.dismiss();
        		Toast.makeText(getBaseContext(), "Copy Complete", Toast.LENGTH_SHORT).show();
        	} else if (xprog < 0) {
        		progressdialog.setMax(-xprog);
        	} else {
        		progressdialog.setProgress(xprog);
        	}
        }
    });
}
/*
 * 
 * This is here for reference only no us System

public void playDelay(final int xwait){

	runOnUiThread(new Runnable() {
        public void run() {
        	try {
        	Thread.sleep(xwait);
//        	System.out.println("After sleep : " + xwait);
        	} catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }     
        }
    });
}
*/

}