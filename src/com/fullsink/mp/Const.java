package com.fullsink.mp;

import android.provider.BaseColumns;


public interface Const extends BaseColumns {
	
	public static final int LONG_WAIT = 7000;
	public static final int BASE_LATENCY = 20;
	public static final int BASE_BLOCKSIZE = 65536;
	public static final int TRACK_COUNT_LIMIT = 100;
	public static final int FILE_COPY_WAIT = 150;
	public static final int GPS_NULL = 1810000000;
	
	public static final String RESOLVER_ADDRESS = "http://www.fullsink.com";
	public static final String LOG_SERVER_PATH = "/api/";			// Add this to Resolver address
	public static final String SERVER_OFFLINE = "OFF";

	public static final String HTML_DIR = "F_html_S";
	public static final String SERVERID_JS = "serverid.js";
	public static final String MUSIC_DIR = "FullSink";
	public static final String SERVICE_NAME = "FullSink";
	public static final String SERVER_PHOTO = "serverphoto";
	
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
	public static final String CMD_COPY = "COPY:";
	public static final String CANCEL_COPY = "<<!CANCEL_COPY#(?";
	
}


