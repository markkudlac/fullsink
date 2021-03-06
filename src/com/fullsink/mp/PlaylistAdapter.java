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

public class PlaylistAdapter extends CursorAdapter implements
		OnItemClickListener {

	MainActivity mnact;
	private final LayoutInflater mInflater;
	private String currentTrack;
	private AlbumArtLoader artLoader;
	private int mAlbumIdx;
	private int mArtistIdx;
	private int mTitle;
	private final int mHighlight;
	private String [] mxTTA;
	private String SONG_PATH = "content://media/external/audio/media/";
	private String mTrackName;
	private int mArtist;
	private String mCurrTrackName;
	private int mDbPath;
	private int mTitleRaw;

	public PlaylistAdapter(MainActivity mnact, Cursor cursor) {
		super((Context) mnact, cursor, false);
		this.mnact = mnact;
		currentTrack = null;
	    mHighlight = mnact.getResources().getColor(R.color.highlight);
		mInflater = LayoutInflater.from(mnact);
		artLoader = new AlbumArtLoader();
        mTitle = cursor.getColumnIndex(Const.DB_TRACK_NAME);
        mArtist = cursor.getColumnIndex(Const.DB_ALBUM_ARTIST);
        mDbPath = cursor.getColumnIndex(Const.DB_TRACK_PATH);
        mTitleRaw = cursor.getColumnIndex(Const.DB_TRACK_RAW);
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
			ListView playlist = mnact.getPlaylist();
			if (currPosition == mnact.getMusicManager().getCurrentSongPosition()) {
				cl.setBackgroundColor(mHighlight);
				playlist.setItemChecked(currPosition, true);
			} else {
				cl.setBackgroundColor(Color.TRANSPARENT);
			}

			field = (TextView) view.findViewById(R.id.album);
			artist = cursor.getString(mArtist);

			
			field.setText(artist);
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
		mnact.getMusicManager().setCurrentSongPosition(pos);
		TextView field = (TextView) view.findViewById(R.id.title);
		setCurrentTrack((String)field.getText());
		mnact.onPlayClick(currentTrack);
		// in api > 15 bindview is not called,as a result need to update
		// background
		// for each row in view
		if (android.os.Build.VERSION.SDK_INT > 15) {
			moveHighlight(view);
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
							if(currLayout != null) {
								currLayout.setBackgroundColor(Color.TRANSPARENT);
							}
						}
					}
					CheckableRelativeLayout cl = (CheckableRelativeLayout) view
							.findViewById(R.id.checkableLayout);
					cl.setBackgroundColor(mnact.getResources().getColor(
							R.color.highlight));
		
	}

	public String getCurrentTrack() {
		return mCurrTrackName;
	}

	public void setCurrentTrack(String curtrk) {
		mCurrTrackName = curtrk;
	}
	
	public String getTrackName(int pos) {

		Cursor tcur;
		String title = null;
		tcur = (Cursor) getItem(pos);
		if(!tcur.isNull(mTitleRaw)){
			title = tcur.getString(mTitleRaw);
		}

		return (title);
	}
	
	private void setTAA(String [] xTTA){
		mxTTA = xTTA;
	}
	public String[] getTAA(int pos) {
		String[] xTTA = new String[3];

		xTTA[0] = xTTA[1] = xTTA[2] = "";

		Cursor tcur;

		tcur = (Cursor) getItem(pos);
		if (tcur != null) {
			xTTA[0] = tcur.getString(mTitle);
			xTTA[2] = tcur.getString(mArtist);
		}
		return xTTA;
	}

	public String getTrackDir(int pos) {
		Cursor tcur;
		String cutpath;

		tcur = (Cursor) getItem(pos);
		cutpath = tcur.getString(mDbPath);

		int offset = cutpath.lastIndexOf("/");
		if (offset >= 0) {
			cutpath = cutpath.substring(0, offset + 1);
		}

		return (cutpath);
	}

}
