package com.fullsink.mp;

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
	
	
	public FS_GestureDetector(MainActivity xmnact){
		
		vc = ViewConfiguration.get(xmnact);
		swipeMinDistance = vc.getScaledTouchSlop();
		swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		mnact = xmnact;
	}
	
	
	@Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > 100)
                return false;
            
            // right to left swipe
            if(e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
            	Toast.makeText(mnact, "Left Swipe", Toast.LENGTH_SHORT).show(); 
				((Button)mnact.findViewById(R.id.btnAlbums)).setClickable(true);
				mnact.setActiveMenu(R.id.btnAlbums);
            	if(mnact.albumAdapter == null) {
            		mnact.albumAdapter = new AlbumAdapter(mnact, MediaMeta.getAlbumCursor(mnact));
				}
				mnact.playlist.setAdapter(mnact.albumAdapter);
				mnact.playlist.setOnItemClickListener(mnact.albumAdapter);

            }  else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
            	Toast.makeText(mnact, "Right Swipe", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }
}