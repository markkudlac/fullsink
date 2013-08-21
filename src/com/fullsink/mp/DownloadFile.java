package com.fullsink.mp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.os.AsyncTask;

import static com.fullsink.mp.Const.*;


public class DownloadFile extends AsyncTask<String, Integer, File> {

	MainActivity mnact;
    String addr;
    int httpdport;
    int filesize = 0;
    Integer blkcount = 0;
    static boolean download = false;

    public DownloadFile(MainActivity mnact, String addr, int httpdport, int filesize) {
        
    	this.mnact = mnact;
    	this.addr = addr;
    	this.httpdport = httpdport;
    	this.filesize = filesize - 3;
    }

    
    public static boolean fileWasDownloaded() {
    	return download;
    }
    
    
    public static void clearWasDownloaded() {
    	download = false;
    }
    
    
    protected File doInBackground(String... urls) {
        String fileUrl = urls[0];
        HttpURLConnection con = null;
        File downfl = null;
        byte [] xbuf = new byte[BASE_BLOCKSIZE];
        		
   		String destfl = fileUrl;		// find final file name to make zip file flat
		int xind = destfl.lastIndexOf("/");   		
		if (xind > 0){
			destfl = destfl.substring(xind + 1);
		}
		
        try {

        	downfl = WebClient.targetCopyFile(fileUrl);
 //       	System.out.println("Dest file path is : " + downfl.getAbsolutePath() );
        	
        	URL url = new URL(HTTP_PROT,addr,httpdport,"/"+Uri.encode(fileUrl));	
    		con = (HttpURLConnection) url.openConnection();

       		InputStream httpin = (InputStream) con.getInputStream();
    	    FileOutputStream downflout = new FileOutputStream(downfl); 
    	    
    	    // Transfer bytes from in to out
 //   	    System.out.println("Start transfer");
    	    Integer fbytes = 0;
    	    int len;
    	    while ((len = httpin.read(xbuf)) > 0) {
    	        downflout.write(xbuf, 0, len);
    	        fbytes += len;
    	        publishProgress(fbytes);
    	    }
    	    httpin.close();
    	    downflout.close();
 //   	    System.out.println("Done transfer");
        } catch (Exception e) {
            System.out.println("Error download" + e.getMessage());
            e.printStackTrace();
        }
        return(downfl);
    }
    
    
    protected void onProgressUpdate(Integer... progress) {    
    	
    	if (blkcount != progress[0] / BASE_BLOCKSIZE){
    		blkcount = progress[0] / BASE_BLOCKSIZE;
    		mnact.fileProgressControl(blkcount);
    		
    		if (blkcount >= filesize) {
    			mnact.fileProgressControl(0);
    		}
    	}
    	
 //   	System.out.println("Progress : "+progress[0] / BASE_BLOCKSIZE);
      }
    	
    
    protected void onPostExecute(File result) {
    	
    	download = true;
    	MediaMeta.refreshMediaStore(mnact, result);
    }
}