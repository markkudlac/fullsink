package com.fullsink.mp;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;


public class MediaMeta {
	
static String scanTitle(String fileName) {
	
	System.out.println("MediaMeta input path : " + fileName);
	MediaMetadataRetriever titleMMR = new MediaMetadataRetriever();
	
    titleMMR.setDataSource(fileName);
 
    String title;

    if(titleMMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) == null)
        title = fileName;
    else
        title = titleMMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    
    System.out.println("MediaMeta album title : " + title);
    return title;
}

}


