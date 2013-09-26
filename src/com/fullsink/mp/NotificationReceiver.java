package com.fullsink.mp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver{
	private MainActivity mMainAct;
	
	public NotificationReceiver(MainActivity mnact){
		mMainAct = mnact;
	}
	
	public NotificationReceiver(){
		super();
	}
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("BroadcastReceiver", "onReceive");
        String action = intent.getAction();
        if (action.equals(MusicService.PLAY)) {
        	Log.d("BroadcastReceiver", "PLAY");
        	mMainAct.playPause();
        } else if (action.equals(MusicService.NEXT)) {
        	Log.d("BroadcastReceiver", "NEXT");
        	mMainAct.nextTrack();
        } else if (action.equals(MusicService.PREVIOUS)) {
        	mMainAct.previousTrack();
        }
    }

}
