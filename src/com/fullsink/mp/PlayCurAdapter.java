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
import android.widget.TextView;

public class PlayCurAdapter extends CursorAdapter implements
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
	private String SONG_PATH = "content://media/external/audio/media/";

	public PlayCurAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		highlight = mnact.getResources().getColor(R.color.highlight);
		mInflater = LayoutInflater.from(mnact);
		artLoader = new AlbumArtLoader();
		mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
        mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
        mTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
	}

	public void updateSelectedPosition(int currSelected) {
		selectedPosition = currSelected;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		String albart = "";
		String artist, album;
		try {
			int currPosition = cursor.getPosition();
			TextView field = (TextView) view.findViewById(R.id.title);
			field.setText(cursor.getString(mTitle));
			CheckableRelativeLayout cl = (CheckableRelativeLayout) view
					.findViewById(R.id.checkableLayout);
			if (currPosition == this.selectedPosition) {
				cl.setBackgroundColor(highlight);
			} else {
				cl.setBackgroundColor(Color.TRANSPARENT);
			}

			field = (TextView) view.findViewById(R.id.album);
			album = cursor.getString(mAlbumIdx);

			artist = cursor.getString(mArtistIdx);

			if (artist.indexOf("<unknown>") == -1
					&& artist.indexOf("Unknown") == -1) {
				albart = artist;
			} else {
				albart = "";
			}

			if (albart.length() > 0) {
				if (album.length() > 0) {
					albart = album + "    " + albart;
				}
			} else {
				albart = album;
			}
			field.setText(albart);
		} catch (Exception ex) {
			System.out.println("Column cursor : " + ex);
		}
		
		artLoader.init(context);
		int albmIdIndex = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
		int albumId = ((Long)cursor.getLong(albmIdIndex)).intValue();
		ImageView imageView = (ImageView)view.findViewById(R.id.image);
		imageView.setTag(String.valueOf(albumId));
		imageView.setImageResource(R.drawable.albumart_icon);
		artLoader.loadBitmap(albumId, SONG_PATH + albumId + "/albumart", imageView, context);
		

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = mInflater.inflate(R.layout.play_data, parent, false);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
		selectedPosition = pos;
		mnact.onPlayClick(currentTrack);
		// in api > 15 bindview is not called,as a result need to update
		// background
		// for each row in view
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

	public String[] getTAA(int pos) {
		String[] xTTA = new String[3];

		xTTA[0] = xTTA[1] = xTTA[2] = "";

		Cursor tcur;

		tcur = (Cursor) getItem(pos);
		if (tcur != null) {
			xTTA[0] = tcur.getString(tcur
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			xTTA[1] = tcur.getString(tcur
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
			xTTA[2] = tcur.getString(tcur
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
		}
		return xTTA;
	}

}
