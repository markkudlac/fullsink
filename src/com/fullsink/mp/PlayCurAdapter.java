package com.fullsink.mp;


import static com.fullsink.mp.Const.FILTER_MUSIC;
import android.provider.MediaStore;
import android.view.ViewGroup.LayoutParams;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PlayCurAdapter extends CursorAdapter implements OnItemClickListener {
	
	MainActivity mnact;
	private final LayoutInflater mInflater;
	private String currentTrack;
	
	
	public PlayCurAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		mInflater=LayoutInflater.from(mnact);
	}
	
	  

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
	
		String albart = "";
		String artist,album;
		try {
		 TextView field=(TextView)view.findViewById(R.id.title);
	        field.setText(cursor.getString(
	        		cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
	        
	        field=(TextView)view.findViewById(R.id.album);
	        album = cursor.getString(
	        		cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
	        
	        artist = cursor.getString(
	        		cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
	        
	        if (artist.indexOf("<unknown>") == -1 && artist.indexOf("Unknown") == -1) {
	        	albart = artist;
	          } else {
	          	albart = "";
	          }
	          
	          if (albart.length() > 0) {
	        	  if (album.length() > 0) {
	        		  albart = album +"    " + albart;
	        	  }
	          } else {
	        	  albart = album;
	          }
	          field.setText(albart);
	          
		} catch(Exception ex) {
			System.out.println("Column cursor : " + ex);
		}
	}



	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
        final View view = mInflater.inflate(R.layout.play_data, parent, false);
		return view;
	}

	
	
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
    	
	   	mnact.onPlayClick(currentTrack);
	   }
    
    
    public String getCurrentTrack(){
    	return currentTrack;
    }
    
    
    public void setCurrentTrack(String curtrk) {
    	currentTrack = curtrk;
    }
    
    
    public String getTrackPath(int pos) {
    	
    	Cursor tcur;
    	String cutpath;
    	
    	tcur = (Cursor)getItem(pos);
    	cutpath = tcur.getString(
        		tcur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

    	int offset = cutpath.indexOf(FILTER_MUSIC);
    	if (offset >= 0) {
    		offset += FILTER_MUSIC.length();
    		cutpath = cutpath.substring(offset);
    	}
    	
    	return(cutpath);
    }
    
    
    
    public void updateImage(String album, Bitmap img){
    	

    }
    
    
    public String[] getTAA(int pos) {
    	String[] xTTA = new String[3];
    	
    	xTTA[0] = xTTA[1] = xTTA[2] = "";
    	
    	Cursor tcur;
    	
    	tcur = (Cursor)getItem(pos);
    	if (tcur != null){
    		xTTA[0] = tcur.getString(
        		tcur.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
    		xTTA[1] = tcur.getString(
            		tcur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
    		xTTA[2] = tcur.getString(
            		tcur.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
    	}
    	return xTTA;
    }

}

