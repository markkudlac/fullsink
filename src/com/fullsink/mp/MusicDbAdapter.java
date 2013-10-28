package com.fullsink.mp;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDbAdapter {	
	
	private static final String music_data_database = "music_data";
	private static final int music_data_database_version = 1;
	public static final String tracks_table = "tracks";
	public static final String tracks_table_create_string = "create table "
			+ tracks_table
			+ " (" + Const.DB_TRACK_ID + " integer primary key autoincrement," + Const.DB_TRACK_NAME + " text not null," +  Const.DB_ALBUM_ARTIST + " text," + Const.DB_ALBUM_ID + " text not null);";
	public static final String app_table = "appdata";
	public static final String app_table_create_string = "create table " + app_table + " (numtracks integer);";
	public static final String app_table_init_string = "insert into " + app_table + " values (0);";
	public MusicDbHelper musicDbHelper;
	private Context mContext;
	private SQLiteDatabase musicDatabase;
	
	public MusicDbAdapter(Context context) {
		mContext = context;
	}

	private static class MusicDbHelper extends SQLiteOpenHelper {
		
		public MusicDbHelper(Context context) {
			super(context, music_data_database, null,
					music_data_database_version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO create tables here
			// create: playlist table, tracks table
			try
			{
			db.beginTransaction();			
			db.execSQL(tracks_table_create_string);
			db.execSQL(app_table_create_string);
			db.execSQL(app_table_init_string);
			db.setTransactionSuccessful();
			}
			finally
			{
			db.endTransaction();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO upgrade database to newer version
		}
	}

	public void open() {
		musicDbHelper = new MusicDbHelper(mContext);
		musicDatabase = musicDbHelper.getWritableDatabase();
	}
	
	public void openReadOnly() {
		musicDbHelper = new MusicDbHelper(mContext);
		musicDatabase = musicDbHelper.getReadableDatabase();
	}

	public void close() {
		if(musicDbHelper!=null){
			musicDbHelper.close();
		}
	}


	public Cursor fetchSongs()
	{
		return musicDatabase.query(tracks_table, new String[] {
				Const.DB_TRACK_ID, Const.DB_TRACK_NAME, Const.DB_ALBUM_ARTIST, Const.DB_ALBUM_ID }, null, null, null, null,
				null);
	}
	
	
	public int getNumTracks()
	{
		Cursor appdataCursor = musicDatabase.query(app_table, null, null, null, null, null, null);
		appdataCursor.moveToFirst();
		return appdataCursor.getInt(appdataCursor.getColumnIndex("numtracks"));
	}
	
	public void setNumTracks(int num)
	{
		ContentValues cv = new ContentValues();
		cv.put("numtracks", num);
		try
		{
		musicDatabase.beginTransaction();		
		musicDatabase.insert(app_table, null, cv);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	
	public void deleteAllTrackInfo()
	{
		try
		{
		musicDatabase.beginTransaction();
		musicDatabase.delete(tracks_table, null, null);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }		
	}
	
	public void addTrackInfo(String id, String albumId, String albumArtist, String name)
	{
		/*if(song == null) //TODO: change so that boolean is returned or exception is raised
		{ return; }*/
		ContentValues cv = new ContentValues();
		cv.put(Const.DB_TRACK_ID, id);
		cv.put(Const.DB_ALBUM_ID, albumId);
		cv.put(Const.DB_ALBUM_ARTIST, albumArtist);
		cv.put(Const.DB_TRACK_NAME, name);
		try
		{
		musicDatabase.beginTransaction();		
		musicDatabase.insert(tracks_table, null, cv);
		musicDatabase.setTransactionSuccessful();		
		}
		finally
		{ musicDatabase.endTransaction(); }
	}
	

	
	public String getTrackTitle(int trackId)
	{
		Cursor trackCursor = musicDatabase.query(tracks_table, new String[] {Const.DB_TRACK_NAME}, Const.DB_TRACK_ID + "=" + trackId, null, null, null, null);
		trackCursor.moveToFirst();
		return trackCursor.getString(trackCursor.getColumnIndex(Const.DB_TRACK_NAME));
	}

	public void removeSong(int songId) {
			ContentValues cv = new ContentValues();
			cv.put(Const.DB_TRACK_ID, songId);
			try
			{
				musicDatabase.beginTransaction();		
				musicDatabase.delete(tracks_table, Const.DB_TRACK_ID + "=" + songId,  null);
				musicDatabase.setTransactionSuccessful();		
			}
			finally
			{ musicDatabase.endTransaction(); }
	}
	

}
