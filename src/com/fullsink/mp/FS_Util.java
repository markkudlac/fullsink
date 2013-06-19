package com.fullsink.mp;


public class FS_Util {
	
	public static int scaleDipPx(MainActivity mnact, Integer dip) {
		
		float scale = mnact.getResources().getDisplayMetrics().density; // scale px with this
		
		return(Float.valueOf((dip.floatValue() * scale)).intValue());
	}
	
}