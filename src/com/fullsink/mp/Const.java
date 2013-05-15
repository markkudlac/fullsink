package com.fullsink.mp;

import android.provider.BaseColumns;


public interface Const extends BaseColumns {
	
	public static final int LONG_WAIT = 7000;
	public static final int BASE_LATENCY = 20;
//	public static final String LOOKUP_SERVER = "Dttp://192.168.1.102:3000/api/";   //Start with D to make default
//	public static final String LOOKUP_SERVER_DEFAULT = "http://www.rtrol.com/api/";
//	public static final String LOOKUP_PICK = "http://192.168.1.102:3000/pick/";
//	public static final String LOOKUP_PICK = "http://www.rtrol.com/pick/";
	
//	public static final String TRKFILE = "trkfile.mp3";
	
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
	
}


