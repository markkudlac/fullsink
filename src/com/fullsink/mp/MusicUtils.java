package com.fullsink.mp;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


public class MusicUtils {
    public static void deleteTracks(Context context, long [] list) {
        
        String [] cols = new String [] { MediaStore.Audio.Media._ID, 
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID };
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            where.append(list[i]);
            if (i < list.length - 1) {
                where.append(",");
            }
        }
        where.append(")");
        Cursor c = MediaMeta.deleteItemsCursor(context, list);

        if (c != null) {

            c.moveToFirst();
			while (! c.isAfterLast()) {
			    // remove from album art cache
			    long artIndex = c.getLong(2);
			    synchronized(AlbumArtLoader.mMemoryCache) {
			    	AlbumArtLoader.mMemoryCache.remove(artIndex);
			    }
			    c.moveToNext();
			}

            // step 2: remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);

            // step 3: remove files from card
            c.moveToFirst();
            while (! c.isAfterLast()) {
                String name = c.getString(1);
                File f = new File(name);
                try {  // File.delete can throw a security exception
                    if (!f.delete()) {
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }
        
        Toast.makeText(context, list.length + " tracks have been deleted", Toast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number of things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    }
}
