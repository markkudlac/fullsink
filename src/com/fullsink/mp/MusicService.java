package com.fullsink.mp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MusicService extends Service {
	//public MainActivity mnact;
	
	private final IBinder mBinder = new LocalBinder();
	//private Music track;
    private static final int NOTIFY_ID=1337;
    public static final String PLAY = "com.fullsink.mp.play";
	public static final String NEXT = "com.fullsink.mp.next";
	public static final String PREVIOUS = "com.fullsink.mp.previous";
	//private NotificationReceiver notifReceiver = new NotificationReceiver();
	
//	
//    public void setStreamTrack(Music xtrk) {
//    	synchronized(this) {		// May not be needed not sure on Sync
//    		track = xtrk;
//    		
//    		if (!mnact.isTrack())	mnact.setServerIndicator(MODE_STOP);
//    	}
//    }
    
  //Loads the track by calling loadMusic
//    private boolean loadTrack(String prevtrack){
//    	if (track != null){
//    		mnact.toClients(CMD_STOP);
//    		track.dispose();
//    		track = null;
//    		
//    		if (prevtrack != null && MainActivity.WServ != null){		//Cleanup server cue
//    			WebServer.DeleteRecursive(new File(getFilesDir(), prevtrack));
//    		}
//    	}
//    	
//    	track = loadMusic();
//    	return(track != null);
//    }
    
//  //loads a Music instance using an external resource
//    private Music loadMusic(){
// 
//		Music xmu = null;
//	
//		if (mnact.getCurrentTrackName() != null) {
//			if (MainActivity.WServ != null){
//				MainActivity.WServ.cueTrack(mnact.getMusicDirectory(), mnact.getCurrentTrackName());
//				mnact.playcuradapter.setCurrentTrack(mnact.getCurrentTrackName());  // Logg file for removal when next song up
//			}
//	
//			try{
//				FileInputStream fis = new FileInputStream(new File(getMusicDirectory(), getCurrentTrackName()));
//				FileDescriptor fileDescriptor = fis.getFD();
//				xmu =  new Music(fileDescriptor, mnact);
//				mnact.toClients(CMD_PREP + mnact.getCurrentTrackName());	// make sure music play is loaded
//				
//			} catch(IOException e){
//				e.printStackTrace();
//				Toast.makeText(getBaseContext(), "Error Loading " + mnact.getCurrentTrackName(), Toast.LENGTH_LONG).show();
//			}
//		}
//		return xmu;
//    }
    

//	@Override
//	public void onCreate() {
//	        IntentFilter commandFilter = new IntentFilter();
//	        commandFilter.addAction(PLAY);
//	        commandFilter.addAction(NEXT);
//	        //commandFilter.addAction(Const.CMD_PREVIOUS);
//	        registerReceiver(intentReceiver, commandFilter);
//	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	
	@Override
	//this is for Android 2.0 or earlier 
	public void onStart(Intent intent, int startId) {
		Toast.makeText(this,"Service start", Toast.LENGTH_LONG).show();
        String currTrack = intent.getExtras().getString("currTrack");
       initNotification(currTrack);
	}

	@Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
	        Toast.makeText(this,"Service start", Toast.LENGTH_LONG).show();
	        String currTrack = intent.getExtras().getString("currTrack");
	      // initNotification(currTrack);


	        return START_STICKY;
	    }
	
 public void initNotification(String currTrack){
	 Intent notifIntent = new Intent(this, MainActivity.class);
     notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
		
		Intent intentPlay = new Intent(this,NotificationReceiver.class);
		intentPlay.setAction(PLAY);
		PendingIntent pIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
		
		PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0, new Intent(NEXT), 0);
		
		PendingIntent pIntentPrevious = PendingIntent.getBroadcast(this, 0, new Intent(PREVIOUS), 0);
		
		NotificationManager mgr=
		        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		    NotificationCompat.Builder normal = new NotificationCompat.Builder(this);
		    normal.setTicker("Fullsink")
		    .setAutoCancel(true)
		    .setPriority(Notification.PRIORITY_HIGH)
			.setContentText(currTrack)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(pIntent)
			.addAction(R.drawable.ic_media_previous, this.getString(R.string.previous), pIntentPrevious)
			.addAction(R.drawable.ic_media_play, this.getString(R.string.play), pIntentPlay)
			.addAction(R.drawable.ic_media_next, this.getString(R.string.next), pIntentNext);
		    NotificationCompat.InboxStyle big=
		        new NotificationCompat.InboxStyle(normal);
		    Notification notific = big.setSummaryText("Fullsink app player").build();
		    notific.flags |= Notification.FLAG_ONGOING_EVENT;
		    mgr.notify(NOTIFY_ID, notific);
 }

public class LocalBinder extends Binder {
    MusicService getService() {
        return MusicService.this;
    }
}


}
