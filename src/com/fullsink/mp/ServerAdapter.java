package com.fullsink.mp;

import static com.fullsink.mp.Const.SERVERID_JS;

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


public class ServerAdapter extends ArrayAdapter<ServerData> implements OnItemClickListener {
	
	MainActivity mnact;
	int currentChecked = -1;
	boolean testflg = false;
	
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

  //      ((CheckableLinearLayout) view).setChecked(false);
        
        ServerData rec = items.get(position);
        
        if (rec != null) {
        	String item = rec.id + " : " + rec.ipAddr;
        	
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
    
    
    public void add(String id, String ipAddr, int httpdPort, int webSockPort, String servicename, Bitmap img){
 
    	items.add(new ServerData(id, ipAddr, httpdPort, webSockPort, servicename, img));
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

    	ServerData xdata = ((ServerData)(adapter.getItemAtPosition(position)));
    	
//	   	System.out.println("Got item click: "+ xdata.ipAddr + "pos : "+position);
	   	mnact.clearCurrentTrack();
	   	mnact.startSockClient(xdata.webSockPort, xdata.ipAddr, xdata.httpdPort) ;
	   }
    
    
    
    public boolean inServerList(String addr) {
    	
    	for (int i=0; i < getCount(); i++){
    		if ( ((ServerData) getItem(i)).ipAddr.equals(addr) ) {
    			System.out.println("Duplicate Address in ServerAdapter");
    			return true;
    		}
    	}
    	return false;
    }
    
     
    public int removeFromServerList(String servicename) {
    	
    
    	for (int i=0; i < getCount(); i++){
    		if ( ((ServerData) getItem(i)).servicename.equals(servicename) ) {  
    			
    			setNotifyOnChange(false);   // This delays redraw so that it is on main thread
    			remove(getItem(i));
    			
    			System.out.println("Removed Address from ServerAdapter");
    			return i;
    		}
    	}
    	return -1;
    }
}



final class ServerData {
	
	public String id;
	public String ipAddr;
	public int httpdPort;
	public int webSockPort;
	public String servicename;
	public Bitmap img;
	
	
	public ServerData(String id, String ipAddr, int httpdPort, int webSockPort, String servicename, Bitmap img) {
		this.id = id;
		this.ipAddr = ipAddr;
		this.httpdPort = httpdPort;
		this.webSockPort = webSockPort;
		this.servicename = servicename;
		this.img = img;
	}
}



