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

public class AlbumAdapter extends CursorAdapter implements
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
	private PlayCurAdapter playCurAdapter;
	private String mCurrAlbumId;

	public AlbumAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
		highlight = mnact.getResources().getColor(R.color.highlight);
		mInflater = LayoutInflater.from(mnact);
		artLoader = new AlbumArtLoader();
		mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
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
			CheckableRelativeLayout cl = (CheckableRelativeLayout) view
					.findViewById(R.id.checkableLayout);
			if (currPosition == this.selectedPosition) {
				cl.setBackgroundColor(highlight);
			} else {
				cl.setBackgroundColor(Color.TRANSPARENT);
			}

			TextView field = (TextView) view.findViewById(R.id.album);
			album = cursor.getString(mAlbumIdx);

			artist = cursor.getString(mAlbumIdx);

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
			field.setText(artist);
		} catch (Exception ex) {
			System.out.println("Column cursor : " + ex);
		}
		
		artLoader.init(context);
		int albmIdIndex = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
		Long albumId = cursor.getLong(albmIdIndex);
		ImageView imageView = (ImageView)view.findViewById(R.id.image);
		imageView.setImageResource(R.drawable.albumart_icon);
		imageView.setTag(String.valueOf(albumId));
		artLoader.loadBitmap(albumId.intValue(), Const.ALBUM_PATH + "" + albumId.intValue(), imageView, context);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		final View view = mInflater.inflate(R.layout.play_data, parent, false);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
		mnact.setAlbumSelected(true);
		selectedPosition = pos;
		
		if (android.os.Build.VERSION.SDK_INT > 15) {
			moveHighlight(view);
		}
		this.setCurrAlbumId(Long.valueOf(id).toString());
		playCurAdapter = new PlayCurAdapter(mnact, MediaMeta.getAlbumSongsCursor(mnact, Long.valueOf(id).toString(), mnact.getSongsSortOrder()));
		((ListView)mnact.findViewById(R.id.playlist)).setAdapter(playCurAdapter);
		((ListView)mnact.findViewById(R.id.playlist)).setOnItemClickListener(playCurAdapter);
		mnact.setSongsSubmenu(true);
		mnact.setPlayCurAdapter(playCurAdapter);
		mnact.getPlaylist().setItemChecked(0, true);
		MusicManager mm = mnact.getMusicManager();
		if (!mm.isTuning()) {
			mm.clearCurrentTrack();
			mm.setTrack(mm.getTrack());
		}
		
	}

	public void moveHighlight(View view) {
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

	public String getCurrAlbumId() {
		return this.mCurrAlbumId;
	}
	
	public void setCurrAlbumId(String albumId){
		mCurrAlbumId = albumId;
	}


}
