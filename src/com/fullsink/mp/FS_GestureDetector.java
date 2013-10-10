package com.fullsink.mp;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Point;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Button;
import android.widget.Toast;


class FS_GestureDetector extends SimpleOnGestureListener {
    
	ViewConfiguration vc;
	final int swipeMinDistance;
	final int swipeThresholdVelocity;
	MainActivity mnact;
    private final int SONGS = R.id.btnSongs;
    private final int ALBUMS = R.id.btnAlbums;
    private final int ARTISTS = R.id.btnArtists;
    private final int RECEIVER = R.id.btnReceiver;
    private TabsManager mTabsManager;
     static boolean moving = false;
	
	
	public FS_GestureDetector(MainActivity xmnact){
		
		vc = ViewConfiguration.get(xmnact);
		swipeMinDistance = vc.getScaledTouchSlop();
		swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		mnact = xmnact;
		mTabsManager= mnact.getTabsManager();
	}
	
	
	@Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		moving = true;
		try {
            if (Math.abs(e1.getY() - e2.getY()) > 100){
                return false;
            }
            
            // right to left swipe
            if(e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
            	Log.d("FS_GestureDetector", "Left Swipe"); 
            	switch(mTabsManager.getActiveTab()){
            	case ARTISTS:
            		mTabsManager.setActiveMenu(R.id.btnArtists);
                	if(mnact.artistAdapter == null) {
                		mnact.artistAdapter = new ArtistAdapter(mnact, MediaMeta.getArtistCursor(mnact, mnact.getSongsSortOrder()));
    				}
    				mnact.playlist.setAdapter(mnact.artistAdapter);
    				mnact.playlist.setOnItemClickListener(mnact.artistAdapter);
    				return false;
            	case ALBUMS:
            		mTabsManager.setActiveMenu(R.id.btnArtists);
                	if(mnact.artistAdapter == null) {
                		mnact.artistAdapter = new ArtistAdapter(mnact, MediaMeta.getArtistCursor(mnact, mnact.getSongsSortOrder()));
    				}
    				mnact.playlist.setAdapter(mnact.artistAdapter);
    				mnact.playlist.setOnItemClickListener(mnact.artistAdapter);
    				return false;
            	case SONGS:
            		mTabsManager.setActiveMenu(R.id.btnAlbums);
                	if(mnact.albumAdapter == null) {
                		mnact.albumAdapter = new AlbumAdapter(mnact, MediaMeta.getAlbumCursor(mnact, mnact.getSongsSortOrder()));
    				}
    				mnact.playlist.setAdapter(mnact.albumAdapter);
    				mnact.playlist.setOnItemClickListener(mnact.albumAdapter);
    				return false;
            	}

            }  else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
            	Log.d("FS_GestureDetector", "Right Swipe");
            	switch(mTabsManager.getActiveTab()){
            	case ARTISTS:
            		mTabsManager.setActiveMenu(R.id.btnAlbums);
                	if(mnact.albumAdapter == null) {
                		mnact.albumAdapter = new AlbumAdapter(mnact, MediaMeta.getAlbumCursor(mnact, MediaStore.Audio.Media.DATE_ADDED));
    				}
    				mnact.playlist.setAdapter(mnact.albumAdapter);
    				mnact.playlist.setOnItemClickListener(mnact.albumAdapter);
    				return false;
            	case ALBUMS:
            		mTabsManager.setActiveMenu(R.id.btnSongs);
                	if(mnact.getPlayCurAdapter() == null) {
                		mnact.setPlayCurAdapter(new PlayCurAdapter(mnact, MediaMeta.getMusicCursor(mnact, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)));
    				}
                	PlayCurAdapter playcuradaptor = mnact.getPlayCurAdapter();
                	playcuradaptor.changeCursor(MediaMeta.getMusicCursor(mnact, mnact.getSongsSortOrder()));
    				mnact.playlist.setAdapter(playcuradaptor);
    				mnact.playlist.setOnItemClickListener(playcuradaptor);
    				return false;
            	case SONGS:
            		mTabsManager.setActiveMenu(R.id.btnArtists);
                	if(mnact.artistAdapter == null) {
                		mnact.artistAdapter = new ArtistAdapter(mnact, MediaMeta.getArtistCursor(mnact, mnact.getSongsSortOrder()));
    				}
    				mnact.playlist.setAdapter(mnact.artistAdapter);
    				mnact.playlist.setOnItemClickListener(mnact.artistAdapter);
    				return false;
            	}


            }
        } catch (Exception e) {
            // nothing
        }
        return true;
    }
	
}