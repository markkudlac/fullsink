package com.fullsink.mp;

import java.io.File;

import android.support.v4.content.CursorLoader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import static com.fullsink.mp.Const.*;

public class MediaMeta {

	/*
static  void loadMusic(MainActivity mnact, PlayAdapter playadapter ) {
	 
	boolean loop;
	String cutpath;
	
	Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] proj = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
    		MediaStore.Audio.Media.ARTIST};
    String select = MediaStore.Audio.Media.DATA + " LIKE ? ";
    String[] args = { "%" + FILTER_MUSIC + "%" };

    CursorLoader loader = new CursorLoader(mnact, contentUri, proj, select, args, null);
    Cursor cursor = loader.loadInBackground();
    int column_path = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
    int column_title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
    int column_album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
    int column_artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
    
    int offset;
    loop = cursor.moveToFirst();
    while (loop) {
    	cutpath = cursor.getString(column_path);
    	offset = cutpath.indexOf(FILTER_MUSIC);
    	if (offset >= 0) {
    		offset += FILTER_MUSIC.length();
    		cutpath = cutpath.substring(offset);
    		System.out.println("First path : "+cutpath + "  Title : " + cursor.getString(column_title) +"  Album : " +
    				cursor.getString(column_album) + "  art: "+ cursor.getString(column_artist));
    		
    		playadapter.add(null, cursor.getString(column_title), cursor.getString(column_album),
    				cursor.getString(column_artist), cutpath);
    	}
    	loop = cursor.moveToNext();
    }
    cursor.close();
}

*/

static  Cursor getMusicCursor(MainActivity mnact ) {
	
	Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String[] proj = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, 
    		MediaStore.Audio.Media.TITLE,
    		MediaStore.Audio.Media.ALBUM,
    		MediaStore.Audio.Media.ARTIST};
    String select = MediaStore.Audio.Media.DATA + " LIKE ? ";
    String[] args = { "%" + FILTER_MUSIC + "%" };

    CursorLoader loader = new CursorLoader(mnact, contentUri, proj, select, args, null);
    Cursor cursor = loader.loadInBackground();
    
    
//    System.out.println("Out with media cursor");
    return cursor;
}


static void refreshMediaStore(MainActivity mnact, File newmedia) {
	System.out.println("Before broadcast");
	mnact.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newmedia)));
	System.out.println("After broadcast");
}

}


