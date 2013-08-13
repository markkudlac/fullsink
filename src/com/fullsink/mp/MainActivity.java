package com.fullsink.mp;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import org.java_websocket.WebSocketImpl;
import static com.fullsink.mp.Const.*;
import fi.iki.elonen.SimpleWebServer;

import android.annotation.TargetApi;
import android.annotation.SuppressLint;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;

import android.os.Build;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable {
	
	public static WebServer WServ = null;	
	public static SimpleWebServer HttpdServ = null;
	public static WebClient WClient = null;
//	public static NsdHelper mNsdHelper = null;
	public static DiscoverHttpd mDiscoverHttpd = null;
	
	private static int ShuffleLoop = MODE_NORMAL;
	WakeLock wakeLock;

	protected Music track; //currently loaded track

	ListView playlist;
	ListView serverlist;
	
	ServerAdapter serveradapter;
	PlayCurAdapter playcuradapter;
	
	SeekBar seekbar;
	ProgressBar progressbar;
	ProgressDialog progressdialog = null;
	
	boolean isTuning; //is user currently jammin out, if so automatically start playing the next track
	private boolean serverIndicator;

	//private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetStrat.setSsid(this);
        //load action bar for OS 2.3 or greater
        if(android.os.Build.VERSION.SDK_INT>=11) {
        	 ActionBar ab = getActionBar();
        	 ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
             ab.setCustomView(R.layout.actionbar);
//             ImageView photoActionBarView = (ImageView) findViewById(R.id.photoActionBar);
             
             Bitmap bm = PhotoActivity.getPhotoBitmap(this);
             if (bm != null){
            	 ((ImageView) findViewById(R.id.photoActionBar)).setImageBitmap(bm);
             }

             ImageButton logoButton = (ImageButton) findViewById(R.id.logo_record);
             final String ssid = NetStrat.getSsid();

             logoButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                	 Toast.makeText(getApplicationContext(), ssid, Toast.LENGTH_LONG).show();
                 }
             });
        }
        
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		// Not sure if this is needed.
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Fullsink");
        setContentView(R.layout.activity_main);

        /*
        gestureDetector = new GestureDetector(this, new FS_GestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        */
        initialize();
    }
    
    
    @Override
    public void onResume(){
    	super.onResume();
    	wakeLock.acquire();
    	System.out.println("In RESUME");
    	if (isSockServerOn()) WServ.sendTrackData(null);	// Will update changes in settings
//    	mNsdHelper.discoverServices(); Keep out was problems

    	if(android.os.Build.VERSION.SDK_INT>=11) {
	    	ImageView photoActionBarView = (ImageView) findViewById(R.id.photoActionBar);
	    	Bitmap bitmap = PhotoActivity.getPhotoBitmap(this);
	    	if (bitmap != null)	photoActionBarView.setImageBitmap(bitmap);
    	}
    }
	
    
    @Override
	public void onPause(){
		super.onPause();
		
		System.out.println("In PAUSE");
		
		wakeLock.release();
//		mNsdHelper.stopDiscovery();		Keep out was problems
		
		if (isFinishing()){
			clearCurrentTrack();
			finish();
		}
	}
    
    
    @Override
	public void onDestroy() {
    	super.onDestroy();
    	
    	if (mDiscoverHttpd != null ) {
    		mDiscoverHttpd.constantPoll(-1);
    	}
    	
		System.out.println("In DESTROY");
		NetStrat.logServer(this, SERVER_OFFLINE);

		stopSockServer(true);
		stopHttpdServer();
		stopSockClient();
		System.out.println("Destroy OUT");
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case R.id.action_settings:
				toSettings(item);
				return true;
			case R.id.action_photo:
				toPhoto(item);
				return true;
			case R.id.action_ipaddress:
				toIPAddress(item);
				return true;
      	default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
	public void toSettings(MenuItem item) {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
	}
  
	
	public void toPhoto(MenuItem item) {
        Intent intent = new Intent(this,PhotoActivity.class);
        startActivity(intent);
	}
   
	
	public void toIPAddress(MenuItem item) {
		
		if (!isSockServerOn()) turnServerOn(this);
		
        Intent intent = new Intent(this,IPAddressActivity.class);
        startActivity(intent);
	}
    
	
    @Override
    public void run() {
        int currentPosition= 0;
 
        while (isTrack()) {
        	
        	try {
                currentPosition = getTrack().getCurrentPosition();
            } catch (Exception ex) {
                System.out.println("Exception in thread run for seek : " + ex);
            } 
        	
        	try {
	        	if (track.isPlaying()) {
	            	if (inClient()){
	            		progressbar.setMax(getTrack().getDuration());
	            		progressbar.setProgress(currentPosition);
	            	} else {
	            		seekbar.setProgress(currentPosition);
	            	}
	            }
        	} catch (Exception ex){
        		Log.e("Fullsink", "Error in track.isPlaying()", ex);
        		StringBuffer result = new StringBuffer();
                StackTraceElement[] trace = ex.getStackTrace();
                ex.printStackTrace();
                for (int i=0;i<trace.length;i++) {
                    result.append(trace[i].toString()).append('\n');
                }
        		Log.e("Stack Trace", result.toString());
        	 
        	}
            
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {	//InterruptedException
                return;
            }     
        }
    }
    
    
    private void initialize(){

    	WebSocketImpl.DEBUG = false;		//This was true originally
    	
    	playlist= (ListView) findViewById(R.id.playlist);
    	
    	serverlist = (ListView) View.inflate(this,R.layout.server_adapter, null);
    	((ViewGroup) findViewById(R.id.midfield)).addView(serverlist);
    	
    	serveradapter = new ServerAdapter(this);
    	serverlist.setOnItemClickListener(serveradapter);
    	serverlist.setAdapter(serveradapter);
    	
       	playcuradapter = new PlayCurAdapter(this, MediaMeta.getMusicCursor(this));
    	playlist.setOnItemClickListener(playcuradapter);
    	playlist.setAdapter(playcuradapter);
    	
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
                        	toClients(CMD_PAUSE);
                        	track.pause();
                        }

                        public void onStopTrackingTouch(SeekBar seekBar)
                        {
                        	int xseek;
                        	
                        	xseek = seekBar.getProgress();
                        	track.seekTo(xseek);
                        	toClients(CMD_RESUME + xseek);
                        	track.play();
                        }
});
        
       	isTuning = false;

    	if (!playcuradapter.isEmpty()) {
    		playlist.setItemChecked(0, true);
    		playcuradapter.updateSelectedPosition(0);
    		loadTrack(null);
    	}
    	
 //   	serverlist.setOnTouchListener(gestureListener);
 //   	playlist.setOnTouchListener(gestureListener);
    	
 //   	 mNsdHelper = new NsdHelper(this, serveradapter);
//		 mNsdHelper.initializeNsd();
    	
		 setActiveMenu(R.id.btnSongs);
		 
		 WebServer.versionChangeHTML(this);	// This must be set before call to turnServerON
		 
		 if (Prefs.getOnAir(this)) turnServerOn(this);
		 
//		 mNsdHelper.discoverServices();
		 
		 incrementLoadCount();
		 int cnt = Prefs.getLoadCount(this);
		 if (cnt <= INSTALL_AUTO) {
//			 System.out.println("Before setName for first installs");
			 PhotoActivity.setNamePhoto(this);
	//		 WebClient.autoSelect(this, INSTALL_AUTO - cnt);	Leave this in used with nsd
		 }
		 
		 Music.setMuted(false);
    	System.out.println("Out Initialize");
    }
    
    
   
    public void setStreamTrack(Music xtrk) {
    	synchronized(this) {		// May not be needed not sure on Sync
    		track = xtrk;
    		
    		if (!isTrack())	setServerIndicator(MODE_STOP);
    	}
    }
    
    
    public void clearCurrentTrack() {
  
    	if (isTrack()){
    		track.dispose();
    		track = null;
    	}
    }
    
    
    public void setDownload(final boolean enable){
 
    	runOnUiThread(new Runnable() {
            public void run() {
		    	Button copybut = (Button) findViewById(R.id.btnclientCopy);
		    	
		    	if (enable) {
			    	copybut.setBackgroundResource(R.drawable.buttonblack);
			    	copybut.setClickable(true);
		    	} else {
		    		copybut.setBackgroundResource(R.drawable.buttongrey);
			    	copybut.setClickable(false);
		    	}
            }
        });
    }
    
    
    public void setServerIndicator(final int mode){
    	 
    	runOnUiThread(new Runnable() {
            public void run() {
		    	ImageView imgbut = (ImageView) findViewById(R.id.imgServerIndicator);
		    	
		    	if (mode == MODE_PAUSE) {
			    	imgbut.setImageResource(R.drawable.ic_media_pause);
		    	} else if (mode == MODE_PLAY) {
		    		imgbut.setImageResource(R.drawable.ic_media_play);
		    	} else {
		    		imgbut.setImageResource(R.drawable.ic_media_stop);
		    	}
            }
        });
    }
    
    
    public boolean isTrack() {
    	return track != null;
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
				Toast.makeText(getBaseContext(), "SD Card is unreachable", Toast.LENGTH_LONG).show();
			}
    	} catch(Exception ex) {
    		System.out.println("Exception in loadFromSD : "+ex);
    	}
    	return dirpath;
    }

    
    //Loads the track by calling loadMusic
    private boolean loadTrack(String prevtrack){
    	if (track != null){
    		toClients(CMD_STOP);
    		track.dispose();
    		track = null;
    		
    		if (prevtrack != null && WServ != null){		//Cleanup server cue
    			WebServer.DeleteRecursive(new File(getFilesDir(), prevtrack));
    		}
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
				playcuradapter.setCurrentTrack(getCurrentTrackName());  // Logg file for removal when next song up
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
    		track = playcuradapter.getTrackPath(pos);	//Path to song in music dir
    	}
    	
    	return(track);
    }
    
    
	public void onPlayClick(String prevFile) {
			   	
 		loadTrack(prevFile);
 		if (!isTuning) {
 			isTuning = true;
 			((ImageView) findViewById(R.id.imgPlayPause)).setImageResource(R.drawable.ic_media_pause);
 		}
 		playTrack(false);
 
	}
	
	
    public void playStream(int offset) {
    	
    	if (isTrack()){
        	try {
        		getTrack().seekTo(offset);
        		progressbar.setMax(track.getDuration());
        		getTrack().play();
        	   	setServerIndicator(MODE_PAUSE);
        	   	setDownload(WClient.getDownload());
        	   	new Thread(this).start();

        	} catch (Exception e) {
                    System.out.println("Thread exception : "+ e); 
            }   
    	}	
    }
    
    
    
    public void clearStream() {
    	clearCurrentTrack();
    	prepClientScreen();
    }
    
    
    public void prepClientScreen() {
       	progressbar.setProgress(0);
    	setDownload(false);
    	setServerIndicator(MODE_STOP);
    }
    
    
    public void turnServerOn(final MainActivity mnact) {
    	
    	final String ipadd = NetStrat.getWifiApIpAddress();
		final int httpdPort = NetStrat.getHttpdPort(mnact);
		final int webSockPort = NetStrat.getSocketPort(mnact);
		
    	NetStrat.logServer(mnact, ipadd, Prefs.getName(mnact), webSockPort, httpdPort );
    	
    	new Thread(new Runnable() {
            public void run() {
        		try {
		
		startHttpdServer(httpdPort, ipadd);
		
		System.out.println("WebSock Port : " + webSockPort + "  IPADD : " + ipadd);
		startSockServer(webSockPort,ipadd);

        		} catch(Exception ex) {
        			System.out.println("Select thread exception : "+ex);
        		}      
            
            runOnUiThread(new Runnable() {
                public void run() {
                	((ImageView) findViewById(R.id.imgServer)).setImageResource(R.drawable.ic_media_route_on_holo_blue);
                }
            });
            }     	
       }).start();
    }
 
    
	public void turnServerOff(final MainActivity mnact) {
	
		stopHttpdServer();
		stopSockServer(false);
		
		NetStrat.logServer(mnact,SERVER_OFFLINE);	//Server is turned off
		
	} 	
	 
    
    public void callLocal() {
		RelativeLayout viewMute;
		LinearLayout parentbuts;
	
		// Move the mute button
		mDiscoverHttpd = new DiscoverHttpd(this,serveradapter,
   			 NetStrat.getWifiApIpAddress(), NetStrat.getHttpdPort(this));
	//	mDiscoverHttpd.constantPoll(20);	// Increase poll frequency when in client
		
		viewMute = (RelativeLayout) findViewById(R.id.viewMute);
		parentbuts = (LinearLayout) findViewById(R.id.mediabuts);
		parentbuts.removeView(viewMute);
		parentbuts = (LinearLayout) findViewById(R.id.clientbuts);
		LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(0,
				LayoutParams.WRAP_CONTENT,3.0f);
		layoutp.setMargins(FS_Util.scaleDipPx(this, 8), 0, FS_Util.scaleDipPx(this, 8), 0);
		viewMute.setLayoutParams(layoutp);
		parentbuts.addView(viewMute,0);
		
		clearActiveMenu();
		((ImageView) findViewById(R.id.imgReceiver)).setImageResource(R.drawable.fs_receive_blue);
		((Button) findViewById(R.id.btnReceiver)).setClickable(false);
		Button serverButton = ((Button) findViewById(R.id.btnServer));
		serverButton.setClickable(false);
		serverButton.setBackgroundResource(R.drawable.buttongrey);
		((ImageView) findViewById(R.id.imgServer)).setImageResource(R.drawable.ic_media_route_off_holo_dark);
		
		prepClientScreen();
		findViewById(R.id.seekbar).setVisibility(View.GONE);
		
		findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
		findViewById(R.id.mediabuts).setVisibility(View.GONE);
		findViewById(R.id.clientbuts).setVisibility(View.VISIBLE);
		findViewById(R.id.playlist).setVisibility(View.GONE);
		findViewById(R.id.serverlist).setVisibility(View.VISIBLE);	
    }
    
    
    /*****************************************  LOOK HERE *******************************/
    
    public void click(View view){
		int id = view.getId();
		Button serverButton = ((Button) findViewById(R.id.btnServer));
		
		switch(id){
		case R.id.btnServer:	
			if (!isSockServerOn()) {
				turnServerOn(this);
				((ImageView) findViewById(R.id.imgServer)).setImageResource(R.drawable.ic_media_route_on_holo_blue);
				serverIndicator = true;
			} else {
				turnServerOff(this);
				((ImageView) findViewById(R.id.imgServer)).setImageResource(R.drawable.ic_media_route_off_holo_dark);
				serverIndicator = false;
			}
			return;
			
		case R.id.btnRemote:
			toClients(CMD_REMOTE+"S");		// Make me the current station on client
			return;
			
		case R.id.btnSongs:
			{
				RelativeLayout viewMute;
				LinearLayout parentbuts;
			
				if (!isSockServerOn() || !serverIndicator) {
					turnServerOn(this);
					((ImageView) findViewById(R.id.imgServer)).setImageResource(R.drawable.ic_media_route_on_holo_blue);
					serverButton.setBackgroundResource(R.drawable.buttonblack);
					serverButton.setClickable(true);
					serverIndicator = true;
				}
				
				// Put mute button back
				viewMute = (RelativeLayout) findViewById(R.id.viewMute);
				parentbuts = (LinearLayout) findViewById(R.id.clientbuts);
				
				parentbuts.removeView(viewMute);
				
				LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(0,
						LayoutParams.WRAP_CONTENT,1.0f);
				layoutp.setMargins(FS_Util.scaleDipPx(this, 2), 0, FS_Util.scaleDipPx(this, 8), 0);
				viewMute.setLayoutParams(layoutp);
				viewMute.setLayoutParams(layoutp);
				parentbuts = (LinearLayout) findViewById(R.id.mediabuts);
				parentbuts.addView(viewMute,0);
				
				setActiveMenu(R.id.btnSongs);
				findViewById(R.id.seekbar).setVisibility(View.VISIBLE);
				findViewById(R.id.progressbar).setVisibility(View.GONE);
				findViewById(R.id.mediabuts).setVisibility(View.VISIBLE);
				findViewById(R.id.clientbuts).setVisibility(View.GONE);
				
				/*
		       	playcuradapter = new PlayCurAdapter(this, MediaMeta.getMusicCursor(this));
		    	playlist.setOnItemClickListener(playcuradapter);
		    	playlist.setAdapter(playcuradapter);
				*/
				
				findViewById(R.id.playlist).setVisibility(View.VISIBLE);
				findViewById(R.id.serverlist).setVisibility(View.GONE);
				
				if (inClient()){	// a stream was started so restart track
					isTuning = false;
					((ImageView) findViewById(R.id.imgPlayPause)).setImageResource(R.drawable.ic_media_play);
					seekbar.setProgress(0);
					loadTrack(null);
				}
				
				stopSockClient();
				/*
				serverlist.clearChoices();	// Need both of these statements
				serveradapter.clear();
				*/
				mDiscoverHttpd.constantPoll(-1);
			}
			return;

			
		case R.id.btnReceiver:	
			{
				serverIndicator = false;
				callLocal();
			}
			return;
			
				
		case R.id.btnMute:
			if (!Music.isMuted()) {
				((ImageView) findViewById(R.id.imgVolMute)).setImageResource(R.drawable.ic_audio_vol_mute);
				if (track != null) {
					track.onMuted();
				} else {
					Music.setMuted(true);
				}
			} else {
				((ImageView) findViewById(R.id.imgVolMute)).setImageResource(R.drawable.ic_volume_small);
				if (track != null) {
					track.clearMuted();
				} else {
					Music.setMuted(false);
				}
			}
			return;
			
		
		case R.id.btnPrevious:
			loadTrack(setTrack(-1));
			playTrack(false);
			return;
			
			
		case R.id.btnPlay:
			synchronized(this){
				
				if(isTuning){
					toClients(CMD_PAUSE);
					isTuning = false;
					((ImageView) findViewById(R.id.imgPlayPause)).setImageResource(R.drawable.ic_media_play);
					track.pause();
				} else{
					isTuning = true;
					((ImageView) findViewById(R.id.imgPlayPause)).setImageResource(R.drawable.ic_media_pause);
					playTrack(true);
				}
			}
			return;
			
			
		case R.id.btnNext:
			butNext(1);
			return;
			
			
		case R.id.btnShuffleLoop:
	        ImageView imgShuffleLoop = (ImageView) findViewById(R.id.imgShuffleLoop);
	        
			if (ShuffleLoop == MODE_NORMAL) {
				ShuffleLoop = MODE_SHUFFLE;
				imgShuffleLoop.setImageResource(R.drawable.fs_shuffle_blue);
			} else if (ShuffleLoop == MODE_SHUFFLE) {
				imgShuffleLoop.setImageResource(R.drawable.ic_menu_loop);
				ShuffleLoop = MODE_LOOP;
			} else {
				ShuffleLoop = MODE_NORMAL;
				imgShuffleLoop.setImageResource(R.drawable.fs_shuffle_white);
			}
			return;
			
				
		case R.id.btnclientCopy:
			
			if (isTrack()) {
				((Button) view).setClickable(false); 	//Click only once
				progressdialog = new ProgressDialog(this);
				progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				
				progressdialog.setMax(100);
				progressdialog.setProgress(0);
				
				progressdialog.setMessage(getResources().getString(R.string.download) +
						" : " + WClient.getSongData()[0]);
				progressdialog.setCancelable(false);
				
				progressdialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	WClient.cancelFileCopy();
				        dialog.dismiss();
				        downloadClickable();
				        Toast.makeText(getBaseContext(), R.string.downcanc, Toast.LENGTH_SHORT).show();
				    }
				});
				
				progressdialog.show();
				WClient.startCopyFile();
				File filePath;
				filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
				String currTrack = WClient.getCurrentTrack();
				if(currTrack != null){
					int slashIndex = currTrack.lastIndexOf("/");
					if(slashIndex >= 0){
						currTrack.substring(slashIndex-1);
					}
				}
				String newSongPath = filePath.toString() + "/" + MUSIC_DIR + "/" + currTrack;
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(newSongPath))));
			}
		return;
			
		default:
			return;
		}
	}
    
    
    public void downloadClickable() {
    	 ((Button) findViewById(R.id.btnclientCopy)).setClickable(true);
    }

    
    private void butNext(int offset) {
    	
		loadTrack(setTrack(offset));
		playTrack(false);
    }
    
    
    private String setTrack(int direction){
    
    	int pos = 0;
    	String prevtrack = null;
    	
    	// Get current position and if none check, should not happen, got to top
    	pos = playlist.getCheckedItemPosition();
    	if (pos == ListView.INVALID_POSITION) {
        	if (playcuradapter.isEmpty()){
        		pos = -1;
        	}
        	else {
        		pos = 0;
        	}
        	return(prevtrack);
    	}
    	
    	prevtrack = getCurrentTrackName();
    	
    	if (ShuffleLoop == MODE_SHUFFLE && playcuradapter.getCount() > 3){
			int temp = new Random().nextInt(playcuradapter.getCount());
			int safety = 0;
			while (safety < 20){
				if(temp != pos){
					pos = temp;
					break;
				}
				temp++;
				if(temp > playcuradapter.getCount()-1){
					temp = 0;
				}
				++safety;
			}
		} else if (direction == -1){
    		pos--;
			if (pos < 0){
				 pos = playcuradapter.getCount()-1;
			}
    	} else if(direction == 1){
    		pos++;
			if (pos > playcuradapter.getCount()-1){
				pos = 0;
			}
    	}  
    	playlist.setItemChecked(pos, true);
    	playcuradapter.updateSelectedPosition(pos);
    	playlist.smoothScrollToPosition(pos);
    	return(prevtrack);
    }
     
    
    //Plays the Track
    private void playTrack(boolean resume){
    	if(isTuning && track != null){
    				 		
       		if (isSockServerOn() && resume) {
           		toClients(CMD_RESUME + track.getCurrentPosition());
       		} 
       		
       		if (isSockServerOn()) WServ.sendTrackData(null);
      
			track.play();
			
	    	seekbar.setMax(track.getDuration());
	    	try {
	    		
	    		new Thread(this).start();

	    	} catch (Exception e) {
	                System.out.println("Thread exception : "+ e); 
	            }   
		}
    }
    
    
    
    private void clearActiveMenu() {
       	Button tbtn;
       	
    	ViewGroup menu = (ViewGroup)findViewById(R.id.topMenu);
    	for (int i=2; i<menu.getChildCount()-1; i++) {
    		tbtn = (Button) menu.getChildAt(i);
    		tbtn.setPaintFlags(0);
    		tbtn.setTypeface(Typeface.DEFAULT);
    		tbtn.setClickable(true);
    	}
    	
    	((Button) findViewById(R.id.btnReceiver)).setClickable(true);
    	((ImageView) findViewById(R.id.imgReceiver)).setImageResource(R.drawable.fs_receive_white);
    }
    
    
    
    private void setActiveMenu(int select) {
    	
    	Button tbtn;
    	
    	clearActiveMenu();
    	tbtn = (Button) findViewById(select);
    	tbtn.setTypeface(Typeface.DEFAULT_BOLD);
    	tbtn.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
    	tbtn.setClickable(false);
    }
    
    
    public void playNextTrack() {
    	
	
	if (!inClient()) {
		if (ShuffleLoop == MODE_LOOP) {
			butNext(0);
		} else {
			butNext(1);
		}
	}
}



public void startSockServer(int port, String ipadd) {
    WebSocketImpl.DEBUG = false;		//This was true originally
    boolean serverinit = false;
    
 //   System.out.println( "In WebSockServer : "+ipadd);
   try {
//	stopSockServer();
// This was changed to allow websocket server to remain running for 2.3 bug
	   
	if (!isSockServerOn()) {
		System.out.println( "In startSockServer");
		WServ = new WebServer( port, ipadd, MainActivity.this );
		serverinit = true;
		
	}
	
    WServ.deleteCues();
    WServ.cueTrack(getMusicDirectory(), getCurrentTrackName());		//Copy for stream
     
    if (serverinit) {
    	WServ.start(); 
    }

    System.out.println( "WebSockServ started on port: " + WServ.getPort() );
    System.out.println("WebSockServ start Add : " + WServ.getAddress());
    WServ.generateServerId(WServ.getPort());
    		
   } catch ( Exception ex ) {
	   System.out.println( "WebSockServer host not found error" + ex);
   }
  }



public void stopSockServer(boolean closeflg) {
	
	try {
		if (isSockServerOn()) {
//			System.out.println( "WebSockServer there are clients : " + WServ.isClient());
	// There seems to be a bug when there are no connections the stop hangs in 2.3
	// To get around this just let the socketserver run if no connections as no one will be able
	// to connect as the Httpd server is the contact point for a live server
			
			if (android.os.Build.VERSION.SDK_INT > 10 || closeflg || WServ.isClient()) {
//				System.out.println("WebSockServ stopping");
				WServ.stop();
				WServ = null;
			}
			
		}
	} catch(Exception ex ) {
	   System.out.println( "WebSockServer stop error" + ex);
   }
}


public boolean isSockServerOn() {
	return(WServ != null);
}


public void startSockClient(int webSockPort, String ipadd, int httpdPort){
	
	try {
		System.out.println( "In startSockClient");
		stopSockClient();
		 WClient = new WebClient(webSockPort, ipadd, httpdPort,  MainActivity.this);
		 WClient.connect();
		 
	} catch ( Exception ex ) {
		   System.out.println( "WebClient error : " + ex);
	   }
	
	}


public void toClients(String mess){
	
	if (isSockServerOn()){
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
 //   mNsdHelper.registerService(httpdPort);
    System.out.println("HttpdServ started Add : " + ipadd + "  Port : "+httpdPort);
    NetStrat.storeHttpdPort(httpdPort);
   } catch ( Exception ex ) {
	   System.out.println( "HttpdServer error  : " + ex);
   }
  }


public void stopHttpdServer() {
	
	try {
		if (HttpdServ != null) {
//			mNsdHelper.unregisterService();
			HttpdServ.stop();
			HttpdServ = null;
			NetStrat.storeHttpdPort(0);

		}
		
	} catch(Exception ex ) {
	   System.out.println( "HttpdServer stop : " + ex);
   }
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
		@Override
        public void run() {
        	
        	if (remove ) {
        		serveradapter.setNotifyOnChange(true);	//turn auto upadte back on
        		if (item >= 0 ) {		// it is in the list else just leave
        			serverlist.setItemChecked(item,false);	// This is dumb and should not be required
        			serveradapter.updateSelectedPosition(item);
        		} else {
        			return;
        		}
        	}
        	
        	serveradapter.notifyDataSetChanged();
        	
        	//There may be a hole here as not sure if notify is completed here. Seems to work

        	int serverCount = serverlist.getCheckedItemPosition();
        	System.out.println( "Checked count : " + serverCount);
        	// Stop stream if the check (current connection) is lost count zero
			if (remove && serverCount == ListView.INVALID_POSITION) {
			 clearStream();
			}
        }
    });
}



public void fileProgressControl(final int xprog){

	runOnUiThread(new Runnable() {
        public void run() {
        	if (xprog == DOWNLOADERR) {
           		downloadClickable();			// Can download again
        		progressdialog.dismiss();
        		Toast.makeText(getBaseContext(), "Copy error. Re-try", Toast.LENGTH_LONG).show();
        		
        	} else if (xprog == 0) {
        		downloadClickable();
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


private void incrementLoadCount() {
	
	int cnt = Prefs.getLoadCount(this) + 1;
	System.out.println("Increment loadcount : "+cnt);
	Prefs.setLoadCount(this, cnt); 
}


public void manageRemote(final String arg){
			
	runOnUiThread(new Runnable() {
        public void run() {
       
			if (arg.equals("T")) {
				findViewById(R.id.viewRemote).setVisibility(View.VISIBLE);
			} else if (arg.equals("F")) {
				findViewById(R.id.viewRemote).setVisibility(View.GONE);
				((ImageView) findViewById(R.id.imgRemote)).setImageResource(R.drawable.fs_remote_white_dot);
			} else if (NetStrat.getWifiApIpAddress().indexOf(arg) >= 0){
				((ImageView) findViewById(R.id.imgRemote)).setImageResource(R.drawable.fs_remote_blue_dot);
			} else {
				((ImageView) findViewById(R.id.imgRemote)).setImageResource(R.drawable.fs_remote_white_dot);
			}
        }
        
    });
}


}