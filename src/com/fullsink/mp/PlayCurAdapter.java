package com.fullsink.mp;


import static com.fullsink.mp.Const.FILTER_MUSIC;
import android.provider.MediaStore;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class PlayCurAdapter extends CursorAdapter implements OnItemClickListener {
	
	MainActivity mnact;
	private final LayoutInflater mInflater;
	private String currentTrack;
	private int selectedPosition = 0;
	private int previousSelected = 0;
	
	
	public PlayCurAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		mInflater=LayoutInflater.from(mnact);
	}
	
	public void updateSelectedPosition(int currSelected){
		selectedPosition = currSelected;
	}  

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
	
		String albart = "";
		String artist,album;
		try {
		int currPosition = cursor.getPosition();
		 TextView field=(TextView)view.findViewById(R.id.title);
	        field.setText(cursor.getString(
	        		cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
	        CheckableRelativeLayout cl = (CheckableRelativeLayout)view.findViewById(R.id.checkableLayout);	
	        if(currPosition == this.selectedPosition ){
	        	cl.setBackgroundColor(mnact.getResources().getColor(R.color.highlight));
	        } else {
	        	cl.setBackgroundColor(Color.TRANSPARENT);
	        }
	        
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
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
    	previousSelected = selectedPosition;
    	View previousView = ((ViewGroup)view.getParent()).getChildAt(previousSelected);
    	CheckableRelativeLayout prevLayout = (CheckableRelativeLayout)previousView.findViewById(R.id.checkableLayout);
    	prevLayout.setBackgroundColor(Color.TRANSPARENT);
    	selectedPosition = pos;
	   	mnact.onPlayClick(currentTrack);
	   	CheckableRelativeLayout cl = (CheckableRelativeLayout)view.findViewById(R.id.checkableLayout);	
        cl.setBackgroundColor(mnact.getResources().getColor(R.color.highlight));
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

