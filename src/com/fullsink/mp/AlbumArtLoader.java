package com.fullsink.mp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.fullsink.mp.DiskLruCache.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class AlbumArtLoader {

	private Context context;
	public static final HashMap<String, Bitmap> mMemoryCache = new HashMap<String, Bitmap>();
	Bitmap bitmap;

	public void init(Context context) {
		this.context = context;
	}

	/*public void loadSongBitmap(int resId, ImageView imageView) {
		final String imageKey = String.valueOf(resId);
		 bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			// Process image to save to cache
			Uri albumArtUri = Uri.parse("content://media/external/audio/media/"
					+ imageKey + "/albumart");
			try {
				ContentResolver cr = context.getContentResolver();
				bitmap = MediaStore.Images.Media.getBitmap(cr, albumArtUri);
				if (bitmap != null) {
					bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
					// Add final bitmap to caches
					addBitmapToMemoryCache(imageKey, bitmap);
					imageView.setImageBitmap(bitmap);
				} else {
					imageView.setImageResource(R.drawable.albumart_icon);
				}

			} catch (FileNotFoundException exception) {
				exception.printStackTrace();

			} catch (IOException e) {

				Log.e("AlbumArtLoader", "IOEXception in bitmap");
				e.printStackTrace();
			} catch (Exception ex) {
				Log.e("AlbumArtLoader", "other error in bitmap");
				ex.printStackTrace();
			}
		}
	}
*/
	public void loadBitmap(int resId, String path, ImageView imageView, Context context) {
		new LoadImageTask().execute(String.valueOf(resId), path, imageView, context);
	}

	public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
			if (getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
	}

	public static Bitmap getBitmapFromMemCache(String key) {
			return mMemoryCache.get(key);
	}
	
}