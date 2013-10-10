package com.fullsink.mp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

public class LoadImageTask extends AsyncTask {
	private ImageView mImageView;
	private SetArtistArt setArtistArt;
	private Context context;
	private String tag;

	@Override
	protected void onPostExecute(Object result) {
		//get # of albums and check if we went through all of them, 
		//update the count as we go through them
		String currImageViewTag;
		if (result != null) {
			currImageViewTag = mImageView.getTag().toString();
			if (currImageViewTag.equals(tag)) {
	                //The path is not same. This means that this
	                //image view is handled by some other async task. 
	                //We don't do anything and return. 
	        	mImageView.setImageBitmap((Bitmap) result);
	        }
		} else if (setArtistArt != null){
			Message msg = new Message();
			msg.what = 	Const.MSG_CONTINUE;
				setArtistArt.handleMessage(msg);
		}
	}

	@Override
	protected Object doInBackground(Object... args) {
		final String imageKey = (String) args[0];
		this.tag = imageKey;
		final String path = (String) args[1];
		this.mImageView = (ImageView) args[2]; 
		this.context = (Context)args[3];
		if(args.length > 4){
			this.setArtistArt = (SetArtistArt) args[4];
		}
		Bitmap bitmap = AlbumArtLoader.getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			return bitmap;
		} else {
			// Process image to save to cache
			Uri albumArtUri = Uri.parse(path);
			try {
				ContentResolver cr = context.getContentResolver();

				   bitmap = BitmapFactory.decodeResource(
							context.getResources(),
							R.drawable.albumart_icon);

					bitmap = MediaStore.Images.Media.getBitmap(cr, albumArtUri);
					bitmap = Bitmap
							.createScaledBitmap(bitmap, 60, 60, true);
					// Add final bitmap to caches
					AlbumArtLoader.addBitmapToMemoryCache(imageKey, bitmap);
					return bitmap;

			} catch (FileNotFoundException exception) {

			} catch (IOException e) {

			} catch (Exception ex) {
	
			}
		}
		return bitmap;
	}
}

