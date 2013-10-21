package com.fullsink.mp;

import static com.fullsink.mp.Const.FILTER_MUSIC;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.fullsink.mp.Const.FILTER_MUSIC;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistAdapter extends CursorAdapter implements
		OnItemClickListener {

	MainActivity mnact;
	private final LayoutInflater mInflater;
	private String currentTrack;
	private int selectedPosition = 0;
	private AlbumArtLoader artLoader;
	private int mAlbumIdx;
	private int mArtistIdx;
	private int mTitle;
	private final int highlight;
	private PlayCurAdapter artistContentAdapter;
	private String ALBUM_PATH = "content://media/external/audio/albumart/";
	private int mNumAlbum;
	private String mArtistId;

	public ArtistAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		highlight = mnact.getResources().getColor(R.color.highlight);
		mInflater = LayoutInflater.from(mnact);
		artLoader = new AlbumArtLoader();
        mArtistIdx = cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ARTIST);
        //mNumAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
	}

	public void updateSelectedPosition(int currSelected) {
		selectedPosition = currSelected;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		String artist, numAlbums;

		int currPosition = cursor.getPosition();
		TextView artistField = (TextView) view.findViewById(R.id.title);
		artistField.setText(cursor.getString(mArtistIdx));
		
		
		TextView albumField = (TextView) view.findViewById(R.id.album);
//		numAlbums = cursor.getString(mNumAlbum);
//		albumField.setText(numAlbums + ' ' + "albums");
		ImageView imageView = (ImageView)view.findViewById(R.id.image);
		imageView.setImageResource(R.drawable.albumart_icon);
		SetArtistArt saa = new SetArtistArt(cursor.getString(mArtistIdx), mnact, imageView, context);
		saa.getNextArtistArt();

			


	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		final View view = mInflater.inflate(R.layout.play_data, parent, false);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
		mnact.setArtistSelected(true);
		selectedPosition = pos;
		
		Cursor tcur = (Cursor) getItem(pos);
		String artistIdtoTest = tcur.getString(tcur
				.getColumnIndexOrThrow( MediaStore.Audio.Media.ARTIST_ID));
		
		setCurrArtistId(Long.valueOf(artistIdtoTest).toString());
		artistContentAdapter = new PlayCurAdapter(mnact, MediaMeta.getArtistSongsCursor(mnact, Long.valueOf(artistIdtoTest).toString(), mnact.getSortOrderString()));
		((ListView)mnact.findViewById(R.id.playlist)).setAdapter(artistContentAdapter);
		((ListView)mnact.findViewById(R.id.playlist)).setOnItemClickListener(artistContentAdapter);
		mnact.setSongsSubmenu(true);
		mnact.setPlayCurAdapter(artistContentAdapter);
		mnact.getPlaylist().setItemChecked(0, true);
		MusicManager mm = mnact.getMusicManager();
		if (!mm.isTuning()) {
			mm.clearCurrentTrack();
			mm.setTrack(mm.getTrack());
		}
	}
	

	public String getCurrentTrack() {
		return currentTrack;
	}

	public void setCurrentTrack(String curtrk) {
		currentTrack = curtrk;
	}

	public void setCurrArtistId(String artistId){
		mArtistId = artistId;
	}
	
	public String getCurrAlbumId() {
		return mArtistId;
	}




}
