package com.fullsink.mp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class ServerAdapter extends ArrayAdapter<String> implements OnItemClickListener {
	
	MainActivity mnact;
	static ArrayList<String> items = new ArrayList<String>();
	
	public ServerAdapter(MainActivity mnact) {
		super(mnact,R.layout.server_adapter, items);
		this.mnact = mnact;
		
	}
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mnact.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
        }

        String item = items.get(position);
        if (item!= null) {
            // My layout has only one TextView
        	CheckedTextView  itemView = (CheckedTextView) view.findViewById(android.R.id.text1);
            if (itemView != null) {
                // do whatever you want with your string and long
 //               itemView.setText(String.format("%s %d", item.reason, item.long_val));
                itemView.setText(item);
                itemView.setChecked(false);
            }
         }

        return view;
    }
    
    public void add(String xstr){
    	items.add(xstr);
    	
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
	   	System.out.println("Got item click: "+ (String)adapter.getItemAtPosition(position)+ "pos : "+position);
	   	mnact.startSockClient(Prefs.getSocketPort(mnact), (String)adapter.getItemAtPosition(position));
	   }

}