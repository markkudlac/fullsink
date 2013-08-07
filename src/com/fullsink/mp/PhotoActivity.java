package com.fullsink.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.support.v4.content.CursorLoader;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts; 
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import static com.fullsink.mp.Const.*;

public class PhotoActivity extends Activity {
	
	private static final int CONTACT_PICKER_RESULT = 1001;
	ImageView photoimageview;
	private EditText editname;
	
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_photo);
        
        photoimageview = (ImageView) findViewById(R.id.photoimage);
        
        Bitmap bm = getPhotoBitmap(this);
        if (bm != null)	photoimageview.setImageBitmap(bm);
        
        if(android.os.Build.VERSION.SDK_INT>=11) {
	        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
	        getActionBar().setCustomView(R.layout.actionbar);
	        ImageView photoActionBarView = (ImageView) findViewById(R.id.photoActionBar);
	        if (bm != null)	photoActionBarView.setImageBitmap(bm);
        }
        
        
        addKeyListener(this);
        
//        
    }
	
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	System.out.println("Got configuration change : Landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	System.out.println("Got configuration change : Portrait");
        }
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
	                    	ImageView imgAB = (ImageView) findViewById(R.id.photoActionBar);
	                    	img.setImageBitmap(bm);
	                    	imgAB.setImageBitmap(bm);
                        
                    		storePhoto(this, photoblob);
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
    
    
    // Contact photo is stored on FS_html
    static public void storePhoto(Activity pact, byte[] photoblob) {
    	
       	try {
	    		File photodest;
	      		
	    		photodest = new File(pact.getFilesDir(),USERHTML_DIR);
	    		photodest.mkdirs();
	    			
        		photodest = new File(photodest, SERVER_PHOTO);
        		photodest.createNewFile();
    		
    	    FileOutputStream writer = new FileOutputStream(photodest,false);
    	    writer.write(photoblob);
    	    writer.close();
    	    
    	} catch (IOException e) {
    		System.out.println( "Phot File write error " + e);
    	}
    }
    
 
    static public Bitmap getPhotoBitmap(Context context) {
    	
    	Bitmap bm = null;
    	byte[] xbyte;
    	
    	
		xbyte = getPhotoByte(context);
		if (xbyte != null && xbyte.length > 0) {
	    	bm = BitmapFactory.decodeByteArray(xbyte, 0, xbyte.length);
		}
        return(bm);
    }
    
    
    static public byte[] getPhotoByte(Context context) {
 
		byte [] xbuf = new byte[BASE_BLOCKSIZE];
		int flsz;
      	try {
	    		File photofl;
	    		photofl = new File(context.getFilesDir(),USERHTML_DIR + "/"+SERVER_PHOTO);
	    		if (photofl.exists()){
		    	    FileInputStream reader = new FileInputStream(photofl);
		    	    flsz = reader.read(xbuf);
		    	    System.out.println("Read photo file bytes : "+flsz);
		    	    reader.close();
		    	    
		    	    byte [] destbuf = new byte[flsz];
		    	    System.arraycopy(xbuf, 0, destbuf, 0, flsz);
		    	    return (destbuf);
	    		} else {
	    			System.out.println("Photo not found");
	    		}
    	} catch (IOException e) {
    		System.out.println( "Photo File write error " + e);
    	}
        return(null);
    }
    
    
    private void clearPhoto() {
    	
    	new File(getFilesDir(),USERHTML_DIR + "/"+SERVER_PHOTO).delete();
    	photoimageview.setImageResource(R.drawable.ic_menu_invite);    //This needs to be changed
    }
    
    
    
    public void addKeyListener(final Context context) {
    	 
    	editname = (EditText) findViewById(R.id.nameField);
    	editname.setText(Prefs.getName(context));
    	System.out.println("Addkeykistener text : "+Prefs.getName(context));
    	// add a keylistener to keep track user input
    	editname.addTextChangedListener(new TextWatcher() {
    		 
    	    @Override
    	    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    	 
    	    }
    	 
    	    @Override
    	    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    	 
    	    }
    	 
    	    @Override
    	    public void afterTextChanged(Editable editable) {
    	       //here, after we introduced something in the EditText we get the string from it
    	       String name = editname.getText().toString();

    	       	name = name.trim();
    	       
    	       	if (name.matches(".*[<>\"\'].*")) {
    	       		Toast.makeText(getBaseContext(), "Invalid character <,> or quotes", Toast.LENGTH_SHORT).show();
    	  //     		System.out.println("The invalid string from EditText");
    	       	} else {
    	 //      		System.out.println("The string from EditText is: "+name);
    	       		Prefs.setName(context, name);  // Save in prefs
    	       	}
    	        
    	    }
    	});
    }
    
    
    static public boolean setNamePhoto(Activity pact){
    	
    	boolean nameupdated = false;
    	final String[] projection = new String[] {
    			ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,  // the name of the contact
    			ContactsContract.Contacts.Photo.PHOTO_ID,       // the data table for the image
    			ContactsContract.Contacts._ID
    			};

    	if (Prefs.getName(pact).length() > 0) {
    		System.out.println("Name is already set");
    		return nameupdated;
    	}
    	
     CursorLoader loader = new CursorLoader(pact,
    			                Contacts.CONTENT_URI,
    			                projection,
    			                null, null, ContactsContract.Contacts._ID +" ASC");
     Cursor cursor = loader.loadInBackground();
     
    	 if (cursor.moveToFirst()) {
    		final String name = cursor.getString(
    		    cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));
    		System.out.println("Got contact name : "+ name);
    		
    		String xname = name.split("@",2)[0];	//Split out @ if it is email returned
    		Prefs.setName(pact, xname);		//Save the name
    		nameupdated = true;
    		String photoid = cursor.getString(cursor.getColumnIndex(
    				ContactsContract.Contacts.Photo.PHOTO_ID));
            if (photoid != null) {
            	System.out.println( "Got photo ID");
            	setPhoto(pact, photoid);
            	
            } else {
            	System.out.println("Photo ID is null");		// Scan for a photo that matches this primary either email or name
            	setEmailPhoto(pact, name);
            }
    	 } else {
    		System.out.println("Got contact FAIL");
    	}
    	
    	cursor.close();
    	return(nameupdated);
    }
    
    
   static public void setEmailPhoto(Activity pact,String emailname){
    	
    	final String[] projection = new String[] {
    			ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,		// This is here for testing
    			Email.ADDRESS,       // the data table for the image
    			ContactsContract.Contacts.Photo.PHOTO_ID
    			};

     CursorLoader loader = new CursorLoader(pact,		// Match name or email
    			                Email.CONTENT_URI,
    			                projection,
    			                Email.ADDRESS + "=? OR "+ContactsContract.Contacts.DISPLAY_NAME_PRIMARY+"=?", 
    			                new String[]{ emailname, emailname }, null);
     Cursor cursor = loader.loadInBackground();
     
   boolean loop = cursor.moveToFirst();
   int i = 0;	//safety
    	 while (loop && i < 500) {
    		 ++i;
    		
    		final String name = cursor.getString(
    		    cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));
    		System.out.println("Got email contact name : "+ name);
    		
    		String photoid = cursor.getString(cursor.getColumnIndex(
    				ContactsContract.Contacts.Photo.PHOTO_ID));
            if (photoid != null) {
            	System.out.println( "Got Email photo ID");
            	setPhoto(pact, photoid);
            	break;
            } else {
            	System.out.println("Email Photo ID is null");
            }
            loop = cursor.moveToNext();
    	 }
    	
    	cursor.close();
    }
    
    
    static public void setPhoto(Activity pact, String photoid) {
    	
      	final String[] projection = new String[] {
    			ContactsContract.Contacts.Photo.PHOTO, 
    			};

     CursorLoader loader = new CursorLoader(pact,
    		 					ContactsContract.Data.CONTENT_URI,
    			                projection,
    			                ContactsContract.Data._ID + "=?", new String[]{photoid}, null);
     Cursor cursor = loader.loadInBackground();
     
    	if (cursor.moveToFirst()) {
    		
    		byte[] photoblob = cursor.getBlob(cursor.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO));
            if (photoblob != null) {
            	System.out.println( "Got photo Blob");
        		storePhoto(pact, photoblob);
            } else {
            	System.out.println( "Photo Blob is null");
            }
    	} else {
    		System.out.println("Got contact Photo FAIL");
    	}
    	
    	cursor.close();
    }
}




    /*  
    static private Bitmap getPhotoBitmap() {
    	
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
    		System.out.println( "Photo File write error " + e);
    	}
        return(bm);
    }
 */
    
    
    
    /* 
     * 
     * 
     Keep this for reference for awhile
     
    public String computeHash(byte[] input) {
    	
    	try {
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	        digest.reset();
	
	        byte[] byteData = digest.digest(input);
	        StringBuffer sb = new StringBuffer();
	
	        for (int i = 0; i < byteData.length; i++){
	          sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        return sb.toString();
        
        } catch (Exception ex) {
        	System.out.println("Has : " + ex);
        }
        return null;
    }
    */


