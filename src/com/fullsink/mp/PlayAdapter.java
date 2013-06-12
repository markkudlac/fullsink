package com.fullsink.mp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
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
        CheckedTextView  chktextView;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mnact.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.play_data, null);
        }

  //      ((CheckableLinearLayout) view).setChecked(false);
        
        PlayData rec = items.get(position);
        
        if (rec != null) {
//        	String item = rec.id + " : " + rec.ipAddr;
        	
            // My layout has only one TextView
        	chktextView = (CheckedTextView) view.findViewById(R.id.title);
            if (chktextView != null) {
            	chktextView.setText(rec.title);
            }
           
            ImageView imgv = (ImageView) view.findViewById(R.id.image);
            imgv.setImageBitmap(rec.img);
            TextView textv = (TextView) view.findViewById(R.id.album);
            textv.setText(rec.album);
            textv = (TextView) view.findViewById(R.id.artist);
            textv.setText(rec.artist);
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

    	PlayData xdata = ((PlayData)(adapter.getItemAtPosition(position)));
    	
	   	System.out.println("Got item click: "+ xdata.path + "pos : "+position);

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

