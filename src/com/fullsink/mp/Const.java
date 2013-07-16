package com.fullsink.mp;

import android.provider.BaseColumns;


public interface Const extends BaseColumns {
	
	public static final int BASE_LATENCY = 20;
	public static final int BASE_BLOCKSIZE = 65536;
	public static final int FILE_COPY_WAIT = 110;
	public static final int GPS_NULL = 1810000000;
	public static final int DOWNLOADERR = -1810000000;		// This is for file download fail
	
	public static final String RESOLVER_ADDRESS = "http://www.fullsink.com";
	public static final String LOG_SERVER_PATH = "/api/";			// Add this to Resolver address
	public static final String SERVER_OFFLINE = "OFF";
	public static final int INSTALL_AUTO = 3;
	public static final String COLON_SUB = "~%~";
	
	public static final int POLL_SECONDS = 12;
	public static final int POLL_SLEEP_CLEAN = 4;
	public static final int POLL_SLEEP_SOCKET = 900;
	
//	public static final String[] EXTENSIONS = {".mp3", ".mid", ".wav", ".ogg", ".m4a"}; //Playable Extensions
	public static final String HTTP_PROT = "http";
	public static final String HTML_DIR = "FlSkHtml";
	public static final String USERHTML_DIR = "UserHtml";
//	public static final String FLSKINDEX = "FlSkindex.html";
	public static final String SERVERID_JS = "serverid.json";
	public static final String MUSIC_DIR = "FullSink";
	public static final String SERVICE_NAME = "FullSink";
	public static final String SERVER_PHOTO = "serverphoto.jpg";
	public static final String FILTER_MUSIC = "/Music/";
	
	public static final String CMD_PREP = "PREP:";
	public static final String CMD_READY = "READY";
	public static final String CMD_PLAY = "PLAY:";
	public static final String CMD_PAUSE = "PAUSE";
	public static final String CMD_RESUME = "RESUME:";
	public static final String CMD_SEEK = "SEEK:";
	public static final String CMD_STOP = "STOP";
	public static final String CMD_INIT = "INIT";
	public static final String CMD_PING = "PING:";
	public static final String CMD_PONG = "PONG:";
	public static final String CMD_CONNECT = "CONNECT:";
	public static final String CMD_FILE = "FILE:";
	public static final String CMD_DOWNEN = "DOWNEN:";
	public static final String CMD_COPY = "COPY:";
	public static final String CMD_ZIPPREP = "ZIPPREP:";
	public static final String CMD_ZIPREADY = "ZIPREADY";
	public static final String CMD_PLAYING = "PLAYING:";
	public static final String CMD_WHATPLAY = "WHATPLAY";
	public static final String CMD_REMOTE = "REMOTE:";	// T,F,S=seize,IP Addres resonse to seize
	
	
	public static final int MODE_PAUSE = 1;
	public static final int MODE_PLAY = 2;
	public static final int MODE_STOP = 3;
	public static final int MODE_NORMAL = 0;
	public static final int MODE_SHUFFLE = 1;
	public static final int MODE_LOOP = 2;
}


