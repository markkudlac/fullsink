package com.fullsink.mp;

import java.io.FileDescriptor;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class Music implements OnCompletionListener, OnPreparedListener, OnSeekCompleteListener{
	MediaPlayer mediaPlayer = null;
	boolean isPrepared = false;
	MainActivity mnact = null;
	
	public Music(AssetFileDescriptor assetDescriptor, MainActivity xmnact){
		mediaPlayer = new MediaPlayer();
		mnact = xmnact;
		
		try{
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load music, uh oh!");
		}
	}
	
	public Music(FileDescriptor fileDescriptor, MainActivity xmnact){
		mediaPlayer = new MediaPlayer();
		mnact = xmnact;
		
		try{
			mediaPlayer.setDataSource(fileDescriptor);
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load music, uh oh!");
		}
	}
	
	public Music(String path, MainActivity xmnact){
		
		mediaPlayer = new MediaPlayer();
		mnact = xmnact;
		
		try{
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);

			mediaPlayer.setDataSource(path);
			mediaPlayer.prepareAsync();

		} catch(Exception ex){
			System.out.println("Couldn't load : "+ex);
		}
	}
	
	
 
public void onPrepared(MediaPlayer mp) {
	
	synchronized(this){
		mnact.textOut( "IN onPrepare");
		isPrepared = true;
	}	
    play();
    }
    
    
	public void onCompletion(MediaPlayer mediaPlayer) {
		synchronized(this){
			isPrepared = false;
		}
	}

		   
    
	public void onSeekComplete(MediaPlayer mediaPlayer) {

		mnact.textOut( "Seek complete listener");
	}
		
	
	
	public void play() {
		System.out.println("In play");
		if(mediaPlayer.isPlaying()){
			return;
		}
		try{
			synchronized(this){
				if(!isPrepared){
					mediaPlayer.prepare();
				}
				mediaPlayer.start();
			}
		} catch(IllegalStateException ex){
			ex.printStackTrace();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void stop() {
		mediaPlayer.stop();
		synchronized(this){
			isPrepared = false;
		}
	}
	
	public void switchTracks(){
		mediaPlayer.seekTo(0);
		mediaPlayer.pause();
	}
	
	public void pause() {
		mediaPlayer.pause();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
	
	
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}
	
	
	public int getDuration() {
		return mediaPlayer.getDuration();
	}
	
	
	public void seekTo(int pos) {
		mediaPlayer.seekTo(pos);
	}

	
	public boolean isLooping() {
		return mediaPlayer.isLooping();
	}
	
	public void setLooping(boolean isLooping) {
		mediaPlayer.setLooping(isLooping);
	}

	public void setVolume(float volumeLeft, float volumeRight) {
		mediaPlayer.setVolume(volumeLeft, volumeRight);
	}

	public void dispose() {
		if(mediaPlayer != null && mediaPlayer.isPlaying()){
			stop();
		}
		mediaPlayer.release();
		mediaPlayer = null;
	}
}