package com.fullsink.mp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import static com.fullsink.mp.Const.*;

//mnact,rec.ipAddr,rec,httpdPort,imgv

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

	MainActivity mnact;
    String addr;
    int httpdport;
    ServerAdapter serveradapter;
	ImageView bmImage;

    public DownloadImageTask(MainActivity mnact, String addr, int httpdport, 
    		ServerAdapter serveradapter, ImageView bmImage) {
        
    	this.mnact = mnact;
    	this.addr = addr;
    	this.httpdport = httpdport;
    	this.serveradapter = serveradapter;
    	this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String imgUrl = urls[0];
        Bitmap mIcon11 = null;
        HttpURLConnection con = null;
        
        try {
        	
        	URL url = new URL(HTTP_PROT,addr,httpdport,imgUrl);	
    		con = (HttpURLConnection) url.openConnection();
    		InputStream xin = (InputStream) con.getInputStream();
            mIcon11 = BitmapFactory.decodeStream(xin);
        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
 
    	if (bmImage != null) {
    		bmImage.setImageBitmap(result);
    	} else {
    		serveradapter.updateImage(addr, result);
    	}
    }
}