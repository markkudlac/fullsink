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
	private ArtistContentAdapter artistContentAdapter;
	private String ALBUM_PATH = "content://media/external/audio/albumart/";
	private int mNumAlbum;

	public ArtistAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		highlight = mnact.getResources().getColor(R.color.highlight);
		mInflater = LayoutInflater.from(mnact);
		artLoader = new AlbumArtLoader();
        mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
        mNumAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
	}

	public void updateSelectedPosition(int currSelected) {
		selectedPosition = currSelected;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		String artist, numAlbums;

		int currPosition = cursor.getPosition();
		CheckableRelativeLayout cl = (CheckableRelativeLayout) view
				.findViewById(R.id.checkableLayout);
		if (currPosition == this.selectedPosition) {
			cl.setBackgroundColor(highlight);
		} else {
			cl.setBackgroundColor(Color.TRANSPARENT);
		}
		TextView artistField = (TextView) view.findViewById(R.id.title);
		artistField.setText(cursor.getString(mArtistIdx));
		
		TextView albumField = (TextView) view.findViewById(R.id.album);
		numAlbums = cursor.getString(mNumAlbum);
		albumField.setText(numAlbums + ' ' + "albums");
		ImageView imageView = (ImageView)view.findViewById(R.id.image);
		imageView.setImageResource(R.drawable.albumart_icon);
		SetArtistArt saa = new SetArtistArt(cursor.getString(mArtistIdx),  Integer.parseInt(numAlbums), mnact, imageView, context);
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
		
		if (android.os.Build.VERSION.SDK_INT > 15) {
			// # or rows currently displayed
			int childCount = ((ViewGroup) view.getParent()).getChildCount();
			View v;
			CheckableRelativeLayout currLayout;
			for (int i = 0; i < childCount; i++) {
				v = ((ViewGroup) view.getParent()).getChildAt(i);
				if (v != null) {
					currLayout = (CheckableRelativeLayout) v
							.findViewById(R.id.checkableLayout);
					currLayout.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			CheckableRelativeLayout cl = (CheckableRelativeLayout) view
					.findViewById(R.id.checkableLayout);
			cl.setBackgroundColor(mnact.getResources().getColor(
					R.color.highlight));
		}
		
		artistContentAdapter = new ArtistContentAdapter(mnact, MediaMeta.getArtistSongsCursor(mnact, Long.valueOf(id).toString()));
		((ListView)mnact.findViewById(R.id.playlist)).setAdapter(artistContentAdapter);
		((ListView)mnact.findViewById(R.id.playlist)).setOnItemClickListener(artistContentAdapter);
		mnact.setSongsSubmenu(true);
		
	}

	public String getCurrentTrack() {
		return currentTrack;
	}

	public void setCurrentTrack(String curtrk) {
		currentTrack = curtrk;
	}

	public String getTrackPath(int pos) {

		Cursor tcur;
		String cutpath;

		tcur = (Cursor) getItem(pos);
		cutpath = tcur.getString(tcur
				.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

		int offset = cutpath.indexOf(FILTER_MUSIC);
		if (offset >= 0) {
			offset += FILTER_MUSIC.length();
			cutpath = cutpath.substring(offset);
		}

		return (cutpath);
	}


}