package com.fullsink.mp;

import java.io.FileDescriptor;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

import static com.fullsink.mp.Const.*;


public class Music extends MediaPlayer implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener {

	MainActivity mnact = null;
	static boolean mute = false;
	
	public Music(AssetFileDescriptor assetDescriptor, MainActivity xmnact){
		
		try{
			initMusic(xmnact);
			setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			prepare();
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load " + ex);
		}
	}
	
	
	public Music(FileDescriptor fileDescriptor, MainActivity xmnact){
		
		try{
			initMusic(xmnact);
			setDataSource(fileDescriptor);
			prepare();
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load, uh oh!");
		}
	}
	
	public Music(String path, MainActivity xmnact){
		
		try{
			initMusic(xmnact);
			setAudioStreamType(AudioManager.STREAM_MUSIC);
			setOnPreparedListener(this);
		
			setDataSource(path);
			prepareAsync();

		} catch(Exception ex){
			System.out.println("Couldn't load : "+ex);
		}
	}
	
	
	private void initMusic(MainActivity xmnact) {
		mnact = xmnact;
		setOnCompletionListener(this);
		setOnSeekCompleteListener(this);
	}
	
	
	@Override
public void onPrepared(MediaPlayer mp) {
	
		mnact.textOut( "IN onPrepare send READY");
		MainActivity.WClient.send(CMD_READY);
    }
    
	
	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {

		mnact.textOut( "IN onCompletion");
		mnact.playNextTrack();
	}

		   
	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {
		
		mnact.textOut( "Seek complete listener");

	}
		
	
	
	public void play() {
//		System.out.println("In play");
		if (isPlaying()){
			return;
		}
		
		try{
			resetMuted();
			start();
		} catch(IllegalStateException ex){
			ex.printStackTrace();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
	public void switchTracks(){
		seekTo(0);
		pause();
	}
	

	public void dispose() {
		if(isPlaying()){
			stop();
		}
		
		release();
	}
	
	
	public static boolean isMuted() {
		return mute;
	}
	
	
	public static void setMuted(boolean flg) {
		mute = flg;
	}
	
	
	public void onMuted() {
		setMuted(true);
		setVolume(0f,0f);
	}
	
	
	public void resetMuted(){
		
		if (isMuted()) onMuted();
	}
	
	public void clearMuted() {
		setMuted(false);
		setVolume(1f,1f);
	}
	
}