package com.fullsink.mp;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.java_websocket.util.Base64.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerAdapter extends ArrayAdapter<ServerData> implements OnItemClickListener {
	
	MainActivity mnact;
	static ArrayList<ServerData> items = new ArrayList<ServerData>();
	
	public ServerAdapter(MainActivity mnact) {
		super(mnact,R.layout.server_adapter, items);
		this.mnact = mnact;
	}
	
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mnact.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.server_data, null);
 //           view = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
        }

        ServerData rec = items.get(position);
        
        if (rec != null) {
        	String item = rec.id + " : " + rec.ipAddr;
        	
            // My layout has only one TextView
        	CheckedTextView  itemView = (CheckedTextView) view.findViewById(R.id.text1);
            if (itemView != null) {
                itemView.setText(item);
                itemView.setChecked(false);
            }
            
            ImageView imgv = (ImageView) view.findViewById(R.id.image1);
            imgv.setImageBitmap(rec.img);
         }
        return view;
    }
    
    
    public void add(String id, String ipAddr, int httpdPort, int webSockPort, Bitmap img){
 
    	items.add(new ServerData(id, ipAddr, httpdPort, webSockPort, img));
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
    	
//    	System.out.println("Should be checked : " +mnact.serverlist.getCheckedItemPosition());
    	
    	ServerData xdata = ((ServerData)(adapter.getItemAtPosition(position)));
    	 ((CheckedTextView)(v.findViewById(R.id.text1))).setChecked(true);
	   	System.out.println("Got item click: "+ xdata.ipAddr + "pos : "+position);
	   	
	   	mnact.clearCurrentTrack();
	   	
	   	mnact.startSockClient(xdata.webSockPort, xdata.ipAddr, xdata.httpdPort) ;
	   }
}



final class ServerData {
	
	public String id;
	public String ipAddr;
	public int httpdPort;
	public int webSockPort;
	public Bitmap img;
	
	
	public ServerData(String id, String ipAddr, int httpdPort, int webSockPort, Bitmap img) {
		this.id = id;
		this.ipAddr = ipAddr;
		this.httpdPort = httpdPort;
		this.webSockPort = webSockPort;
		this.img = img;
	}
}