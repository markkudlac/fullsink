package com.fullsink.mp;

import java.util.ArrayList;

import android.view.ViewGroup.LayoutParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PlayAdapter extends ArrayAdapter<PlayData> implements OnItemClickListener {
	
	MainActivity mnact;
	int currentChecked = -1;
	boolean testflg = false;
	
	static ArrayList<PlayData> items = new ArrayList<PlayData>();
	
	public PlayAdapter(MainActivity mnact) {
		super(mnact,R.layout.play_data, items);
		this.mnact = mnact;
	}
	
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        TextView textv;
        String albart;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mnact.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.play_data, null);
        }
        
        PlayData rec = items.get(position);
        
        if (rec != null) {
        	
        	//Need to reset the wrap for display or it grows to max album name
        	RelativeLayout.LayoutParams layoutp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
        			LayoutParams.WRAP_CONTENT);		
        	
        	textv = (TextView) view.findViewById(R.id.title);
            textv.setText(rec.title);
           
            if (rec.img != null) {
            	ImageView imgv = (ImageView) view.findViewById(R.id.image);
            	imgv.setImageBitmap(rec.img);
            }
            
          if (rec.artist.indexOf("<unknown>") == -1 && rec.artist.indexOf("Unknown") == -1) {
        	albart = rec.artist;
          } else {
          	albart = "";
          }
          
          if (albart.length() > 0) {
        	  if (rec.album.length() > 0) {
        		  albart = rec.album +"    " + albart;
        	  }
          } else {
        	  albart = rec.album;
          }
            textv = (TextView) view.findViewById(R.id.album);
            textv.setText(albart);
            textv.setLayoutParams(layoutp);

         }
        return view;
    }
    
    
    public void updateImage(String album, Bitmap img){
    	
    	PlayData rec;

    	for (int i=0; i< getCount(); i++){
    		rec = (PlayData) getItem(i);
    		if (album.equals(rec.album)) {
    			rec.img = img;
    			notifyDataSetChanged();
    		//	System.out.println("In updateImage found and updated");
    			return;
    		}
    	}
    }
    
    
    public void add(Bitmap img, String title, String album, String artist, String path ){
 
    	items.add(new PlayData( img, title, album, artist, path));
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

 //   	PlayData xdata = ((PlayData)(adapter.getItemAtPosition(position)));
    	
//	   	System.out.println("Got item click: "+ xdata.path + "pos : "+position);
	   	mnact.onPlayClick(position);

	   }
    
    
    
    public boolean inServerList(String path) {
    	
    	for (int i=0; i < getCount(); i++){
    		if ( ((PlayData) getItem(i)).path.equals(path) ) {
    			System.out.println("Duplicate Address in PlayAdapter");
    			return true;
    		}
    	}
    	return false;
    }
    
    
    public String[] getTAA(int pos) {
    	String[] xTTA = new String[3];
    	
    	PlayData xdata = (PlayData) getItem(pos);
    	if (xdata != null) {
	    	xTTA[0] = xdata.title;
	    	xTTA[1] = xdata.album;
	    	xTTA[2] = xdata.artist;
    	}
    	return xTTA;
    }
}



final class PlayData {
	public Bitmap img;
	public String title;
	public String album;
	public String artist;
	public String path;

	
	
	public PlayData(Bitmap img, String title, String album, String artist, String path) {
		
		this.img = img;
		this.title = title;
		this.album = album;
		this.artist = artist;
		this.path = path;
	}
}

