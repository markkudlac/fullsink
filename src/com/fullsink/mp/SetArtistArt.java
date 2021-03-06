package com.fullsink.mp;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

public class SetArtistArt implements Callback{
	String artist;
	int numAlbums;
	MainActivity mnact;
	ImageView view;
	Cursor albumCursor;
	Context context;

	public SetArtistArt(String artist, MainActivity mnact, View view, Context context){
		this.artist = artist;
		this.mnact = mnact;
		this.view = (ImageView)view;
		this.context = context;
		//cursor containing album ids of this artist
		this.albumCursor = MediaMeta.getArtistAlbumsCursor(mnact, artist);
		this.numAlbums = albumCursor.getCount();
	 	
	}
	
	public void getNextArtistArt(){
		//initiate cursor
		albumCursor.moveToFirst();
		int columnIdIndex = albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
		try {
		Long albumId = albumCursor.getLong(columnIdIndex);
		view.setTag(String.valueOf(albumId));
		new LoadImageTask().execute(String.valueOf(albumId), Const.ALBUM_PATH + "" + albumId.intValue(), view, context, this);
		//move to the next album id
		albumCursor.moveToNext();
    	numAlbums--;
		} catch(CursorIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		
	}
	

	@Override
	public boolean handleMessage(Message msg) {
            if (msg.what == Const.MSG_CONTINUE && numAlbums > 0) {
            	getNextArtistArt();
            	return true;
            }
            return true;
	}
}
