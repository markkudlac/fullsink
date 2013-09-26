package com.fullsink.mp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;


public class ServerAdapter extends ArrayAdapter<ServerData> implements OnItemClickListener {
	
	MainActivity mnact;
	int currentChecked = -1;
	boolean testflg = false;
	private int selectedPosition;
	
	static ArrayList<ServerData> items = new ArrayList<ServerData>();
	
	public ServerAdapter(MainActivity mnact) {
		super(mnact,R.layout.server_adapter, items);
		this.mnact = mnact;
	}
	
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        CheckedTextView  chktextView;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mnact.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.server_data, null);
        }
        
        CheckableLinearLayout cl = (CheckableLinearLayout)view.findViewById(R.id.serverDataLayout);	
        if(position == this.selectedPosition ){
        	cl.setBackgroundColor((mnact.getResources().getColor(R.color.highlight)));
        } else {
        	cl.setBackgroundColor(Color.TRANSPARENT);
        }
        
        ServerData rec = items.get(position);
        
        if (rec != null) {
        	String item = rec.id;
        	
			if (rec.title != null && rec.title.length() > 0) {
				item = item + " - " + rec.title;
			}
        			
            // My layout has only one TextView
        	chktextView = (CheckedTextView) view.findViewById(R.id.text1);
            if (chktextView != null) {
            	chktextView.setText(item);
            }
           
            ImageView imgv = (ImageView) view.findViewById(R.id.image1);
            imgv.setImageBitmap(rec.img); 
         }
        return view;
    }
    
    public void updateSelectedPosition(int newSelectedPosition){
    	selectedPosition = newSelectedPosition;
    }
    
    public void updateImage(String ipAddr, Bitmap img){
    	
    	ServerData rec;

    	for (int i=0; i< getCount(); i++){
    		rec = (ServerData)getItem(i);
    		if (ipAddr.equals(rec.ipAddr)) {
    			rec.img = img;
    			notifyDataSetChanged();
    		//	System.out.println("In updateImage found and updated");
    			return;
    		}
    	}
    }
    
    
    public void updateSongData(int pos, String title) {
    	
    	ServerData xdata = (ServerData) getItem(pos);
    	if (xdata != null) {
	    	xdata.title = title;
	    	mnact.adapterOut(false, pos);
    	}
    	return;
    }

    
public String[] getSongData(int pos) {
    	
	String[] xval = {"","",""};
	
    	ServerData xdata = (ServerData) getItem(pos);
    	if (xdata != null) {
	    	xval[0] = xdata.title;
    	}
    	return(xval);
    }

    
    public void clearSongData() {
    	
    	for (int i=0; i < getCount(); i++) {
    		updateSongData(i, "");
    	}
    }
    
    
    public synchronized void add(String id, String ipAddr, int httpdPort, int webSockPort, String servicename, Bitmap img){
    	items.add(new ServerData(id, ipAddr, httpdPort, webSockPort, servicename, img));	
    /*	
    try {
    	System.out.println( "In Adapter add SockClient");
    	WebClient webclient = new WebClient(webSockPort, ipAddr, httpdPort,  mnact);
   		 webclient.connect();
    	items.add(new ServerData(id, ipAddr, httpdPort, webSockPort, servicename, img, webclient));	
    } catch ( Exception ex ) {
		   System.out.println( "WebClient add in ServerAdapter error : " + ex);
	   }
	   */
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

    	if (mnact.isSockServerOn()) {
			mnact.turnServerOff(mnact);
		} 
    	

 //   	System.out.println("After turnserverOff");
    	this.updateSelectedPosition(position);

    	
    	ServerData xdata = ((ServerData)(adapter.getItemAtPosition(position)));
    	
	   	System.out.println("Got item click: "+ xdata.ipAddr + "pos : "+position);
    	clearSongData();
    	
	   	mnact.getMusicManager().clearCurrentTrack();
	   	mnact.startSockClient(xdata.webSockPort, xdata.ipAddr, xdata.httpdPort) ;
	   }
    
    
    public void serverSelected( int pos){
    	
    	ServerData xdata = ((ServerData)(items.get(pos)));
    	
//	   	System.out.println("Got Selected: "+ xdata.ipAddr + "pos : "+pos);
    	
	   	mnact.getMusicManager().clearCurrentTrack();
	   	mnact.startSockClient(xdata.webSockPort, xdata.ipAddr, xdata.httpdPort) ;
    	
    }
    
    public synchronized boolean inServerList(String addr) {
    	
    	for (int i=0; i < getCount(); i++){
    		if ( ((ServerData) getItem(i)).ipAddr.equals(addr) ) {
    			System.out.println("Duplicate Address in ServerAdapter");
    			((ServerData) getItem(i)).alive = true;
    			return true;
    		}
    	}
    	return false;
    }
    
     
    public synchronized int removeFromServerList(String servicename) {
    	
    
    	for (int i=0; i < getCount(); i++){
    		System.out.println("removefromService list name : " + ((ServerData) getItem(i)).servicename +
    				"  nsd name : " + servicename);
    		if (  ((ServerData) getItem(i)).servicename.equals(servicename) ) {  
    			
    			setNotifyOnChange(false);   // This delays redraw so that it is on main thread
    			remove(getItem(i));
    			
    			System.out.println("Removed Address from ServerAdapter");
    			return i;
    		}
    	}
    	return -1;
    }
    
    
    public void clearAlive() {
    	
    	for (int i=0; i < getCount(); i++){
    		((ServerData) getItem(i)).alive = false;
    	}
    }
 
}



final class ServerData {
	
	public String id;
	public String ipAddr;
	public int httpdPort;
	public int webSockPort;
	public String servicename;
	public Bitmap img;
	public String title;
	public boolean alive;

	
	public ServerData(String id, String ipAddr, int httpdPort, int webSockPort, String servicename, 
			Bitmap img) {
		this.id = id;
		this.ipAddr = ipAddr;
		this.httpdPort = httpdPort;
		this.webSockPort = webSockPort;
		this.servicename = servicename;
		this.img = img;
		this.title = "";
		this.alive = true;
	}
}



