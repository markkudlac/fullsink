package com.fullsink.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
//import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts; 
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static com.fullsink.mp.Const.*;

public class PhotoActivity extends Activity {
	
	private static final int CONTACT_PICKER_RESULT = 1001;
	ImageView photoimageview;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_photo);
        
        photoimageview = (ImageView) findViewById(R.id.photoimage);
        
        Bitmap bm = getPhotoBitmap();
        
        if (bm != null)	photoimageview.setImageBitmap(bm);
        
    }
	
    
    public void doLaunchContactPicker(View view) {  
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
                Contacts.CONTENT_URI);  
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);  
    }
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case CONTACT_PICKER_RESULT:
                Cursor cursor = null;

                try {
                    Uri result = data.getData();
                    System.out.println( "Got a contact result: "
                            + result.toString());
                    // get the contact id from the Uri
                    String id = result.getLastPathSegment();
                    // query for everything photo
                    
                    Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.valueOf( id));
                    Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
                    cursor = getContentResolver().query(photoUri,
                            new String[] {Contacts.Photo.PHOTO}, null, null, null);
                    
                    if (cursor == null) {
                    	System.out.println( "Photo cursor null");
                        return;
                    }
                    // let's just get the first photo
                    if (cursor.moveToFirst()) {
                    	
                    	byte[] photoblob = cursor.getBlob(0);
                        if (photoblob != null) {
                        	System.out.println( "Got photo Blob : ");
	                    	Bitmap bm = BitmapFactory.decodeByteArray(photoblob,0,photoblob.length);
	                    	ImageView img = (ImageView) findViewById(R.id.photoimage);
	                    	img.setImageBitmap(bm);
                        
                    		storePhoto(photoblob);
                        } else {
                        	System.out.println( "No photo Blob");
                        }
                    } else {
                    	System.out.println( "No results");
                    }
                } catch (Exception e) {
                	System.out.println( "Failed to get email data : " + e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                break;
            }
        } else {
        	System.out.println("Warning: activity result not ok");
        }
    }
    

    public void click(View view){
		int id = view.getId();
		switch(id){
		case R.id.photopick:	
			doLaunchContactPicker( view);
			return;
			
		case R.id.photoclear:	
				clearPhoto();
			return;
			
		default:
			return;
		}
    }
    
    
    // Contact photo is store as base64
    private void storePhoto(byte[] photoblob) {
    	
       	try {
	    		File photodest;
	      		
	    		photodest = new File(getFilesDir(),HTML_DIR);
	    		photodest.mkdirs();
	    			
        		photodest = new File(photodest, SERVER_PHOTO);
        		photodest.createNewFile();
    		
    	    FileOutputStream writer = new FileOutputStream(photodest,false);
    	    byte[] xbuf = Base64.encode(photoblob,Base64.DEFAULT);

    	    writer.write(xbuf);
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println( "Phot File write error " + e);
    	}
    }
    
    
    private Bitmap getPhotoBitmap() {
    	
    	Bitmap bm = null;
    	
      	try {
	    		File photofl;
	    		byte [] xbuf = new byte[BASE_BLOCKSIZE];
	    		
	    		photofl = new File(getFilesDir(),HTML_DIR + "/"+SERVER_PHOTO);
	    		if (photofl.exists()){
		    	    FileInputStream reader = new FileInputStream(photofl);
		    	    if (reader.read(xbuf) > 0) {
		    	    	xbuf = Base64.decode(xbuf,Base64.DEFAULT);
		    	    	bm = BitmapFactory.decodeByteArray(xbuf, 0, xbuf.length);
		    	    }
	    	    reader.close();
	    		}
    	} catch (IOException e) {
    		System.out.println( "Phot File write error " + e);
    	}
        return(bm);
    }
 
    
    private void clearPhoto() {
    	
    	new File(getFilesDir(),HTML_DIR + "/"+SERVER_PHOTO).delete();
    	photoimageview.setImageResource(R.drawable.looping);    //This needs to be changed
    }
}


