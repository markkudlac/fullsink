package com.fullsink.mp;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import org.java_websocket.WebSocketImpl;
import static com.fullsink.mp.Const.*;
import fi.iki.elonen.SimpleWebServer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable {
	
	public static WebServer WServ = null;	
	public static SimpleWebServer HttpdServ = null;
	public static WebClient WClient = null;
	public static NsdHelper mNsdHelper = null;
	
	private static int ShuffleLoop = 0;
	WakeLock wakeLock;

	Music track; //currently loaded track

	TextView textout;  // message window
	ScrollView debug;
	ListView playlist;
	ListView serverlist;
	
	ServerAdapter serveradapter;
	PlayAdapter playadapter;
	
	SeekBar seekbar;
	ProgressBar progressbar;
	ProgressDialog progressdialog = null;
	
	ImageView imgPlayPause; //The play button will need to change from 'play' to 'pause'
	ImageView imgVolMute;
	Button btnShuffleLoop;
	
	boolean isTuning; //is user currently jammin out, if so automatically start playing the next track

	private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lexiconda");
        setContentView(R.layout.activity_main);

        gestureDetector = new GestureDetector(this, new FS_GestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        initialize();
    }
    
    
    @Override
    public void onResume(){
    	super.onResume();
    	wakeLock.acquire();

    	if (inClient()) {
    		mNsdHelper.discoverServices();
    	}
    }
	
    
    @Override
	public void onPause(){
		super.onPause();
		wakeLock.release();
		if (inClient()) {
			mNsdHelper.stopDiscovery();
		}
		
		if (isFinishing()){
			clearCurrentTrack();
			finish();
		}
	}
    
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		mNsdHelper.unregisterService();
		stopSockServer();
		stopHttpdServer();
		stopSockClient();
		
	}
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	System.out.println("Got configuration change : Landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	System.out.println("Got configuration change : Portrait");
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
  
	
	public void toPhoto(MenuItem item) {
        Intent intent = new Intent(this,PhotoActivity.class);
        startActivity(intent);
	}
    
	
    @Override
    public void run() {
        int currentPosition= 0;
 
        while (track != null) {
        	
        	try {
                currentPosition = getTrack().getCurrentPosition();
            } catch (Exception ex) {
                System.out.println("Exception in thread run for seek : " + ex);
            } 
        	
            if (track.isPlaying()) {
            	if (inClient()){
            		progressbar.setMax(getTrack().getDuration());
            		progressbar.setProgress(currentPosition);
            	} else {
            		seekbar.setProgress(currentPosition);
            	}
            }
            
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {	//InterruptedException
                return;
            }     
        }
    }
    
    
    private void initialize(){

    	textout = (TextView) findViewById(R.id.textout);
    	debug = (ScrollView) findViewById(R.id.debug);
    	playlist= (ListView) findViewById(R.id.playlist);
    	
    	serverlist = (ListView) View.inflate(this,R.layout.server_adapter, null);
    	((ViewGroup) findViewById(R.id.midfield)).addView(serverlist);
    	
    	serveradapter = new ServerAdapter(this);
    	serverlist.setOnItemClickListener(serveradapter);
    	serverlist.setAdapter(serveradapter);
    	
    	playadapter = new PlayAdapter(this);
    	playlist.setOnItemClickListener(playadapter);
    	playlist.setAdapter(playadapter);
    	
        imgPlayPause = (ImageView) findViewById(R.id.imgPlayPause);
        imgVolMute = (ImageView) findViewById(R.id.imgVolMute);
        btnShuffleLoop = (Button) findViewById(R.id.btnShuffleLoop);
        
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        
        seekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
        	{
        		public void onProgressChanged(SeekBar seekBar, int progress,
                                        boolean fromUser)
        		{
                                     //   textOut("SeekBar value is "+progress);
                        }

                        public void onStartTrackingTouch(SeekBar seekBar)
                        {
                        	textOut("SeekBar start touch "); 
                        	toClients(CMD_PAUSE);
                        	track.pause();
                        }

                        public void onStopTrackingTouch(SeekBar seekBar)
                        {
                        	int xseek;
                        	
                        	xseek = seekBar.getProgress();
                        	textOut("SeekBar end touch progress val: " + xseek); 
                        	track.seekTo(xseek);
                        	toClients(CMD_RESUME + xseek);
                        	track.play();
                        }
});
        
       	isTuning = false;
        MediaMeta.loadMusic(this, playadapter);
    	if (!playadapter.isEmpty()) {
    		playlist.setItemChecked(0, true);
    		loadTrack();
    	}
    	
 //   	debug.setOnTouchListener(gestureListener);
 //   	playlist.setOnTouchListener(gestureListener);
    	
    	 mNsdHelper = new NsdHelper(this, serveradapter);
		 mNsdHelper.initializeNsd();
		 
    	System.out.println("Out Initialize");
    }
    
    
    public void setStreamTrack(Music xtrk) {
    	synchronized(this) {		// May not be needed not sure on Sync
    		track = xtrk;
    	}
    }
    
    
    
    public void clearCurrentTrack() {
  
    	if (track != null){
    		track.dispose();
    		track = null;
    	}
    }
    
    
    
    public Music getTrack() {
    	synchronized(this) {	// May not be needed not sure on Sync
    		return(track);
    	}
    }

  
    private File getMusicDirectory() {
    	
    	File dirpath = null;
    	try {
 //   		System.out.println("External storage state : "+Environment.getExternalStorageState());
    		
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) 
	    			|| Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
	    		dirpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
	
			} else{
				Toast.makeText(getBaseContext(), "SD Card is either mounted elsewhere or is unusable", Toast.LENGTH_LONG).show();
			}
    	} catch(Exception ex) {
    		System.out.println("Exception in loadFromSD : "+ex);
    	}
    	return dirpath;
    }

    
    //Loads the track by calling loadMusic
    private boolean loadTrack(){
    	if (track != null){
    		toClients(CMD_STOP);
    		track.dispose();
    		track = null;
    	}
    	
    	track = loadMusic();
    	return(track != null);
    }
    
	//loads a Music instance using an external resource
    private Music loadMusic(){
 
		Music xmu = null;
	
		if (getCurrentTrackName() != null) {
			if (WServ != null){
				WServ.cueTrack(getMusicDirectory(), getCurrentTrackName());
			}
	
			try{
				FileInputStream fis = new FileInputStream(new File(getMusicDirectory(), getCurrentTrackName()));
				FileDescriptor fileDescriptor = fis.getFD();
				xmu =  new Music(fileDescriptor, (MainActivity)this);
				toClients(CMD_PREP + getCurrentTrackName());	// make sure music play is loaded
			} catch(IOException e){
				e.printStackTrace();
				Toast.makeText(getBaseContext(), "Error Loading " + getCurrentTrackName(), Toast.LENGTH_LONG).show();
			}
		}
		return xmu;
    }
    
    
    public String getCurrentTrackName(){
    	int pos;
    	String track = null;
    	
    	pos = playlist.getCheckedItemPosition();
    	if (pos != ListView.INVALID_POSITION) {
    		track = playadapter.getItem(pos).path;	//Path to song in music dir
    	}
    	
    	return(track);
    }
    
    
	public void onPlayClick(int pos) {
			   	
 		loadTrack();
 		if (!isTuning) {
 			isTuning = true;
 			imgPlayPause.setImageResource(R.drawable.ic_media_pause);
 		}
 		playTrack(false);
 
	}
	
	
    public void playStream(int offset) {
    	textOut("in playStream offset : " + offset);
    	
    	if (track != null){
        	try {
        		getTrack().seekTo(offset);
        		progressbar.setMax(track.getDuration());
        		getTrack().play();
        	   	
        	   	new Thread(this).start();

        	} catch (Exception e) {
                    textOut("Thread exception : "+ e); 
            }   
    	}	
    }

    
 
    /*****************************************  LOOK HERE *******************************/
    
    public void click(View view){
		int id = view.getId();
		switch(id){
		case R.id.btnShare:	
			
			if (((Button) view).getText().equals(
					getResources().getString(R.string.serverbutOff))) {
				
				String ipadd = NetStrat.getWifiApIpAddress();
				int httpdPort = NetStrat.getHttpdPort(this);
				startHttpdServer(httpdPort, ipadd);
				
				int webSockPort = NetStrat.getSocketPort(this);
				
				System.out.println("WebSock Port : " + webSockPort + "  IPADD : " + ipadd);
				startSockServer(webSockPort,ipadd);
				
				((Button) view).setText(getResources().getString(R.string.serverbutOn));
				findViewById(R.id.btnConnect).setEnabled(false);
				NetStrat.logServer(this, ipadd, Prefs.getAcountID(this), webSockPort, httpdPort );
			} else {
				NetStrat.logServer(this,SERVER_OFFLINE);	//Server is turned off
				stopSockServer();
				stopHttpdServer();
			
				((Button) view).setText(getResources().getString(R.string.serverbutOff));
				findViewById(R.id.btnConnect).setEnabled(true);
			}
			return;
			
		case R.id.btnConnect:	
			
			RelativeLayout viewMute;
			LinearLayout parentbuts;
			
			if (((Button) view).getText().equals(
					getResources().getString(R.string.clientbutOff))) {
				
				// Move the mute button
				viewMute = (RelativeLayout) findViewById(R.id.viewMute);
				parentbuts = (LinearLayout) findViewById(R.id.mediabuts);
				parentbuts.removeView(viewMute);
				parentbuts = (LinearLayout) findViewById(R.id.clientbuts);
				viewMute.
				parentbuts.addView(viewMute,0);
				
				((Button) view).setText(getResources().getString(R.string.clientbutOn));
				clearStream();
				findViewById(R.id.btnShare).setEnabled(false);
				findViewById(R.id.seekbar).setVisibility(View.GONE);
				
				findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
				findViewById(R.id.mediabuts).setVisibility(View.GONE);
				findViewById(R.id.clientbuts).setVisibility(View.VISIBLE);
				findViewById(R.id.playlist).setVisibility(View.GONE);
//				findViewById(R.id.debug).setVisibility(View.VISIBLE);
				findViewById(R.id.serverlist).setVisibility(View.VISIBLE);						
				
				mNsdHelper.discoverServices();

				
			} else {
				mNsdHelper.stopDiscovery();
				stopSockClient();
				serverlist.clearChoices();
				serveradapter.clear();
				((Button) view).setText(getResources().getString(R.string.clientbutOff));
				findViewById(R.id.btnShare).setEnabled(true);
				findViewById(R.id.seekbar).setVisibility(View.VISIBLE);
				findViewById(R.id.progressbar).setVisibility(View.GONE);
				findViewById(R.id.mediabuts).setVisibility(View.VISIBLE);
				findViewById(R.id.clientbuts).setVisibility(View.GONE);
				findViewById(R.id.playlist).setVisibility(View.VISIBLE);
				findViewById(R.id.debug).setVisibility(View.GONE);
				findViewById(R.id.serverlist).setVisibility(View.GONE);
				
				isTuning = false;
				imgPlayPause.setImageResource(R.drawable.ic_media_play);
				seekbar.setProgress(0);
				loadTrack();
			}
			return;

			
		case R.id.btnMute:
			if (track != null) {
				if (!track.isMuted()) {
					imgVolMute.setImageResource(R.drawable.ic_audio_vol_mute);
					track.onMuted();
				} else {
					imgVolMute.setImageResource(R.drawable.ic_volume_small);
					track.clearMuted();
				}
			}
			return;
			
		
		case R.id.btnPrevious:
			setTrack(-1);
			loadTrack();
			playTrack(false);
			return;
			
			
		case R.id.btnPlay:
			synchronized(this){
				
				if(isTuning){
					toClients(CMD_PAUSE);
					isTuning = false;
					imgPlayPause.setImageResource(R.drawable.ic_media_play);
					track.pause();
				} else{
					isTuning = true;
					imgPlayPause.setImageResource(R.drawable.ic_media_pause);
					playTrack(true);
				}
			}
			return;
			
			
		case R.id.btnNext:
			butNext();
			return;
			
			
		case R.id.btnShuffleLoop:
			if (ShuffleLoop == 0) {
				ShuffleLoop = 1;
				btnShuffleLoop.setBackgroundResource(R.drawable.buttonblue);
			} else {
				ShuffleLoop = 0;
				btnShuffleLoop.setBackgroundResource(R.drawable.buttonblack);
			}
			return;
			
				
		case R.id.btnclientCopy:
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
			        Toast.makeText(getBaseContext(), "Copy Cancelled", Toast.LENGTH_SHORT).show();
			    }
			});
			
			progressdialog.show();

			WClient.startCopyFile();
			return;
			
		default:
			return;
		}
	}
    
    
    public void clearStream() {
    	
    	clearCurrentTrack();
    	progressbar.setProgress(0);
    }
    
    
    
    private void butNext() {
    	
    	setTrack(1);
		loadTrack();
		playTrack(false);
    }
    
    
    private void setTrack(int direction){
    
    	int pos = 0;
    	
    	// Get current position and if none check, should not happen, got to top
    	pos = playlist.getCheckedItemPosition();
    	if (pos == ListView.INVALID_POSITION) {
        	if (playadapter.isEmpty()) return;
        	else {
        		pos = 0;
        	}
    	} else if (ShuffleLoop == 1 && playadapter.getCount() > 3){
    			int temp = new Random().nextInt(playadapter.getCount());
    			int safety = 0;
    			while (safety < 20){
    				if(temp != pos){
    					pos = temp;
    					break;
    				}
    				temp++;
    				if(temp > playadapter.getCount()-1){
    					temp = 0;
    				}
    				++safety;
    			}
    		}
    	else if (direction == -1){
    		pos--;
			if (pos < 0){
				 pos = playadapter.getCount()-1;
			}
    	} else if(direction == 1){
    		pos++;
			if (pos > playadapter.getCount()-1){
				pos = 0;
			}
    	}  
    	playlist.setItemChecked(pos, true);
    	playlist.smoothScrollToPosition(pos);
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

	    	} catch (Exception e) {
	                textOut("Thread exception : "+ e); 
	            }   
	    	
//			Toast.makeText(getBaseContext(), "Playing " + getCurrentTrackName().substring(0,
//					getCurrentTrackName().length()-4), Toast.LENGTH_SHORT).show();
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
    
    System.out.println( "In WebSockServer");
   try {
	stopSockServer();
    WServ = new WebServer( port, ipadd, MainActivity.this );
    WServ.deleteCues();
    WServ.cueTrack(getMusicDirectory(), getCurrentTrackName());		//Copy for stream
    WServ.start();       

    System.out.println( "WebSockServ started on port: " + WServ.getPort() );
    textOut("WebSockServ started");
    textOut("Address : " + WServ.getAddress());
    WServ.initHTML(WServ.getPort());
    		
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

public void startSockClient(int webSockPort, String ipadd, int httpdPort){
	WebSocketImpl.DEBUG = false;		//This was true originally
	try {
		stopSockClient();
		 WClient = new WebClient(webSockPort, ipadd, httpdPort,  MainActivity.this);
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


public void startHttpdServer(int httpdPort, String ipadd) {
    
   try {
	stopHttpdServer();
    HttpdServ = new SimpleWebServer( ipadd, httpdPort, getFilesDir() ); 
    HttpdServ.start();  
    mNsdHelper.registerService(httpdPort);
    textOut("HttpdServ started");
    textOut("Address : " + ipadd + "  Port : "+httpdPort);
   } catch ( Exception ex ) {
	   System.out.println( "HttpdServer error : " + ex);
   }
  }


public void stopHttpdServer() {
	
	try {
		if (HttpdServ != null) {
			HttpdServ.stop();
			HttpdServ = null;
		}
		mNsdHelper.unregisterService();
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

// This is not good and should be reviewed
public void toastOut(final String xmess, final int length){

	runOnUiThread(new Runnable() {
        public void run() {
        	Toast.makeText(getBaseContext(), xmess, length).show();
        
        }
    });
}



public void adapterOut(final boolean remove, final int item){

	runOnUiThread(new Runnable() {
        public void run() {
        	
        	if (remove ) {
        		serveradapter.setNotifyOnChange(true);	//turn auto upadte back on
        		if (item >= 0 ) serverlist.setItemChecked(item,false);	// This is dumb and should not be required
        	}
        	
        	serveradapter.notifyDataSetChanged();
        	
        	//There may be a hole here as not sure if notify is completed here. Seems to work
        	System.out.println( "Checked count : " + serverlist.getCheckedItemCount());
        	// Stop stream if the check (current connection) is lost count zero
			if (remove && serverlist.getCheckedItemCount() == 0) {
			 clearStream();
			}
        }
    });
}



public void fileProgressControl(final int xprog){

	runOnUiThread(new Runnable() {
        public void run() {
        	if (xprog == DOWNLOADERR) {
        		progressdialog.dismiss();
        		Toast.makeText(getBaseContext(), "Copy error. Re-try", Toast.LENGTH_LONG).show();
        	} else if (xprog == 0) {
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


}