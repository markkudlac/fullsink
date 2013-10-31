package com.fullsink.mp;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class AlbumArtLoader {

	private Context context;
	public static final HashMap<String, Bitmap> mMemoryCache = new HashMap<String, Bitmap>();
	Bitmap bitmap;

	public void init(Context context) {
		this.context = context;
	}

	public void loadBitmap(int resId, String path, ImageView imageView, Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			new LoadImageTask().execute(String.valueOf(resId), path, imageView, context);
		}
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