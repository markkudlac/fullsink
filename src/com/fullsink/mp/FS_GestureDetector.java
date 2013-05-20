package com.fullsink.mp;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;


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
            if (Math.abs(e1.getY() - e2.getY()) > 200)
                return false;
            // right to left swipe
            if(e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
 //               System.out.println("Left Swipe");
               	mnact.findViewById(R.id.debug).setVisibility(View.GONE);
            	mnact.findViewById(R.id.playlist).setVisibility(View.VISIBLE);
            }  else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
 //           	System.out.println("Right Swipe");
            	mnact.findViewById(R.id.debug).setVisibility(View.VISIBLE);
            	mnact.findViewById(R.id.playlist).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }
}