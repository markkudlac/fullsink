package com.fullsink.mp;

import static com.fullsink.mp.Const.CMD_PAUSE;
import static com.fullsink.mp.Const.CMD_REMOTE;
import static com.fullsink.mp.Const.CMD_RESUME;
import static com.fullsink.mp.Const.DOWNLOADERR;
import static com.fullsink.mp.Const.INSTALL_AUTO;
import static com.fullsink.mp.Const.MODE_LOOP;
import static com.fullsink.mp.Const.MODE_NORMAL;
import static com.fullsink.mp.Const.MODE_PAUSE;
import static com.fullsink.mp.Const.MODE_PLAY;
import static com.fullsink.mp.Const.MODE_SHUFFLE;
import static com.fullsink.mp.Const.MODE_STOP;
import static com.fullsink.mp.Const.MUSIC_DIR;
import static com.fullsink.mp.Const.SERVER_OFFLINE;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocketImpl;

import android.annotation.SuppressLint;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import fi.iki.elonen.SimpleWebServer;

public class MainActivity extends Activity implements Runnable,
		OnGestureListener, OnItemLongClickListener {

	public static WebServer WServ = null;
	public static SimpleWebServer HttpdServ = null;
	public static WebClient WClient = null;
	public static DiscoverHttpd mDiscoverHttpd = null;

	private static int ShuffleLoop = MODE_NORMAL;
	public static MainActivity getMainObject() {
		return mnact;
	}

	WakeLock wakeLock;
	ListView playlist;

	ListView serverlist;
	ServerAdapter serveradapter;
	PlayCurAdapter mPlaycuradapter;
	AlbumAdapter albumAdapter;

	ArtistAdapter artistAdapter;
	SeekBar seekbar;
	ProgressBar progressbar;
	ProgressDialog progressdialog = null;
	private boolean serverIndicator;
	private boolean albumSelected;
	private boolean artistSelected;
	public final static int DELETE_ITEM = 0;
	public final static int ADD_TO_PLAYLIST = 1;
	private boolean PLAYLIST_TAB;
	private long[] deleteList;
	static MainActivity mnact;
	private GestureDetector gestureDetector;

	private View.OnTouchListener gestureListener;
	private NotificationReceiver mIntentReceiver;
	private NotificationManager mgr;
	private static final int NOTIFY_ID = 1337;
	public static final String PLAY = "com.fullsink.mp.play";
	public static final String NEXT = "com.fullsink.mp.next";
	public static final String PREVIOUS = "com.fullsink.mp.previous";
	private String mSortOrderString;
	private TabsManager mTabsManager;
	private MusicManager mMusicManager;
	private boolean mSongsSubmenu;
	public String mDeletetitle;
	private int mSortOrderInt;
	private MusicDbAdapter mDbadapter;
	private PlaylistAdapter mPlaylistAdapter;
	private Builder mNotifyBuilder;
	private Notification mNotify;

	public void adapterOut(final boolean remove, final int item) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (remove) {
					serveradapter.setNotifyOnChange(true); // turn auto upadte
															// back on
					if (item >= 0) { // it is in the list else just leave
						serverlist.setItemChecked(item, false); // This is dumb
																// and should
																// not be
																// required
						serveradapter.updateSelectedPosition(item);
					} else {
						return;
					}
				}

				serveradapter.notifyDataSetChanged();

				// There may be a hole here as not sure if notify is completed
				// here. Seems to work
				int serverCount = serverlist.getCheckedItemPosition();
				System.out.println("Checked count : " + serverCount);
				// Stop stream if the check (current connection) is lost count
				// zero
				if (remove && serverCount == ListView.INVALID_POSITION) {
					mMusicManager.clearStream();
				}
			}
		});
	}

	private void butNext(int offset) {

		mMusicManager.loadTrack(setTrack(offset));
		mMusicManager.playTrack(false);
		// notify change
	}

	public void callLocal() {
		serverIndicator = false;
		RelativeLayout viewMute;
		LinearLayout parentbuts;
		String wifiIPaddress = NetStrat.getWifiApIpAddress();
		if (wifiIPaddress != null) {
			// Move the mute button
			mDiscoverHttpd = new DiscoverHttpd(this, serveradapter,
					wifiIPaddress, NetStrat.getHttpdPort(this));
			// mDiscoverHttpd.constantPoll(20); // Increase poll frequency when
			// in
			// client

			viewMute = (RelativeLayout) findViewById(R.id.viewMute);
			parentbuts = (LinearLayout) findViewById(R.id.mediabuts);
			parentbuts.removeView(viewMute);
			parentbuts = (LinearLayout) findViewById(R.id.clientbuts);
			LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 3.0f);
			layoutp.setMargins(FS_Util.scaleDipPx(this, 8), 0,
					FS_Util.scaleDipPx(this, 8), 0);
			viewMute.setLayoutParams(layoutp);
			parentbuts.addView(viewMute, 0);

			mTabsManager.clearActiveMenu();
			((ImageView) findViewById(R.id.imgReceiver))
					.setImageResource(R.drawable.fs_receive_blue);
			((Button) findViewById(R.id.btnReceiver)).setClickable(false);
			Button serverButton = ((Button) findViewById(R.id.btnServer));
			serverButton.setClickable(false);
			serverButton.setBackgroundResource(R.drawable.buttongrey);
			((ImageView) findViewById(R.id.imgServer))
					.setImageResource(R.drawable.ic_media_route_off_holo_dark);

			prepClientScreen();
			findViewById(R.id.seekbar).setVisibility(View.GONE);

			findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
			findViewById(R.id.mediabuts).setVisibility(View.GONE);
			findViewById(R.id.clientbuts).setVisibility(View.VISIBLE);
			findViewById(R.id.playlist).setVisibility(View.GONE);
			findViewById(R.id.serverlist).setVisibility(View.VISIBLE);

		} else {
			FS_Util.showConnectionWarning(mnact);
		}
	}

	/***************************************** LOOK HERE *******************************/

	public void click(View view) {
		int id = view.getId();
		Button serverButton = ((Button) findViewById(R.id.btnServer));

		switch (id) {
		case R.id.btnServer:
			if (!isSockServerOn()) {
				turnServerOn(this);
				((ImageView) findViewById(R.id.imgServer))
						.setImageResource(R.drawable.ic_media_route_on_holo_blue);
				serverIndicator = true;
			} else {
				turnServerOff(this);
				((ImageView) findViewById(R.id.imgServer))
						.setImageResource(R.drawable.ic_media_route_off_holo_dark);
				serverIndicator = false;
			}
			return;

		case R.id.btnRemote:
			toClients(CMD_REMOTE + "S"); // Make me the current station on
											// client
			return;

		case R.id.btnSongs:
		case R.id.btnAlbums:
		case R.id.btnArtists: {
			PLAYLIST_TAB = false;
			RelativeLayout viewMute;
			LinearLayout parentbuts;

			if (!isSockServerOn() || !serverIndicator) {
				turnServerOn(this);
				((ImageView) findViewById(R.id.imgServer))
						.setImageResource(R.drawable.ic_media_route_on_holo_blue);
				serverButton.setBackgroundResource(R.drawable.buttonblack);
				serverButton.setClickable(true);
				serverIndicator = true;
			}

			// Put mute button back
			viewMute = (RelativeLayout) findViewById(R.id.viewMute);
			parentbuts = (LinearLayout) findViewById(R.id.clientbuts);

			parentbuts.removeView(viewMute);

			LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 1.0f);
			layoutp.setMargins(FS_Util.scaleDipPx(this, 2), 0,
					FS_Util.scaleDipPx(this, 8), 0);
			viewMute.setLayoutParams(layoutp);
			viewMute.setLayoutParams(layoutp);

			findViewById(R.id.seekbar).setVisibility(View.VISIBLE);
			findViewById(R.id.progressbar).setVisibility(View.GONE);
			findViewById(R.id.mediabuts).setVisibility(View.VISIBLE);
			findViewById(R.id.clientbuts).setVisibility(View.GONE);

			findViewById(R.id.playlist).setVisibility(View.VISIBLE);
			findViewById(R.id.serverlist).setVisibility(View.GONE);

			if (DownloadFile.fileWasDownloaded()) { // If a song was dowloaded,
													// reload playlist
				// System.out.println("File was dowloaded, reload adapter");
				DownloadFile.clearWasDownloaded();
				loadPlayAdapter();
				((ImageView) findViewById(R.id.imgPlayPause))
						.setImageResource(R.drawable.ic_media_play);
				seekbar.setProgress(0);
			} else if (inClient()) { // a stream was started so restart track
				// System.out.println("Coming from receiver, track cued");
				mMusicManager.setIsTuning(false);
				((ImageView) findViewById(R.id.imgPlayPause))
						.setImageResource(R.drawable.ic_media_play);
				seekbar.setProgress(0);
				mMusicManager.loadTrack(null);
			}

			stopSockClient();
			serverlist.clearChoices(); // Need both of these statements
			serveradapter.clear();
			parentbuts = (LinearLayout) findViewById(R.id.mediabuts);
			if (parentbuts.indexOfChild(viewMute) < 0) {
				parentbuts.addView(viewMute, 0);
			}
			if (mDiscoverHttpd != null) {
				mDiscoverHttpd.constantPoll(-1);
			}

			if (id == R.id.btnSongs) {
				mTabsManager.setActiveMenu(R.id.btnSongs);
				Button songsButton = ((Button) findViewById(R.id.btnSongs));
				songsButton.setOnClickListener(new OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View view) {
						customSelectedMenuOnclick(view);

					}
				});
				mPlaycuradapter = new PlayCurAdapter(this,
						MediaMeta.getMusicCursor(this,
								this.getSortOrderString()));
				playlist.setAdapter(mPlaycuradapter);
				playlist.setOnItemClickListener(mPlaycuradapter);
				playlist.setItemChecked(0, true);
			} else if (id == R.id.btnAlbums) {
				mTabsManager.setActiveMenu(R.id.btnAlbums);
				Button albumButton = ((Button) findViewById(R.id.btnAlbums));
				albumButton.setClickable(true);
				albumButton.setOnClickListener(new OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View view) {
						customSelectedMenuOnclick(view);
					}
				});
				if (albumAdapter == null) {
					albumAdapter = new AlbumAdapter(this,
							MediaMeta.getAlbumCursor(this,
									MediaStore.Audio.Albums.DEFAULT_SORT_ORDER));
				}
				playlist.setAdapter(albumAdapter);
				playlist.setOnItemClickListener(albumAdapter);
			} else {
				mTabsManager.setActiveMenu(R.id.btnArtists);
				Button artistButton = ((Button) findViewById(R.id.btnArtists));
				artistButton.setClickable(true);
				artistButton.setOnClickListener(new OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View view) {
						customSelectedMenuOnclick(view);
					}
				});
				if (artistAdapter == null) {
					artistAdapter = new ArtistAdapter(this,
							MediaMeta.getArtistCursor(this, "artist_key"));
				}
				playlist.setAdapter(artistAdapter);
				playlist.setOnItemClickListener(artistAdapter);
			}
		}
			return;

		case R.id.btnReceiver: {
			mTabsManager.setActiveMenu(R.id.btnReceiver);
			callLocal();
		}
			return;

		case R.id.btnMute:
			if (!Music.isMuted()) {
				((ImageView) findViewById(R.id.imgVolMute))
						.setImageResource(R.drawable.ic_audio_vol_mute);
				if (mMusicManager.getTrack() != null) {
					mMusicManager.getTrack().onMuted();
				} else {
					Music.setMuted(true);
				}
			} else {
				((ImageView) findViewById(R.id.imgVolMute))
						.setImageResource(R.drawable.ic_volume_small);
				if (mMusicManager.getTrack() != null) {
					mMusicManager.getTrack().clearMuted();
				} else {
					Music.setMuted(false);
				}
			}
			return;

		case R.id.btnPrevious:
			previousTrack();
			return;

		case R.id.btnPlay:
			synchronized (this) {
				playPause();
			}
			return;

		case R.id.btnNext:
			nextTrack();
			return;

		case R.id.btnShuffleLoop:
			ImageView imgShuffleLoop = (ImageView) findViewById(R.id.imgShuffleLoop);

			if (ShuffleLoop == MODE_NORMAL) {
				ShuffleLoop = MODE_SHUFFLE;
				imgShuffleLoop.setImageResource(R.drawable.fs_shuffle_blue);
			} else if (ShuffleLoop == MODE_SHUFFLE) {
				imgShuffleLoop.setImageResource(R.drawable.ic_menu_loop);
				ShuffleLoop = MODE_LOOP;
			} else {
				ShuffleLoop = MODE_NORMAL;
				imgShuffleLoop.setImageResource(R.drawable.fs_shuffle_white);
			}
			return;

		case R.id.btnclientCopy:

			if (mMusicManager.isTrack()) {
				((Button) view).setClickable(false); // Click only once
				progressdialog = new ProgressDialog(this);
				progressdialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

				progressdialog.setMax(100);
				progressdialog.setProgress(0);

				progressdialog.setMessage(getResources().getString(
						R.string.download)
						+ " : " + WClient.getSongData()[0]);
				progressdialog.setCancelable(false);

				progressdialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								WClient.cancelFileCopy();
								dialog.dismiss();
								downloadClickable();
								Toast.makeText(getBaseContext(),
										R.string.downcanc, Toast.LENGTH_SHORT)
										.show();
							}
						});

				progressdialog.show();
				WClient.startCopyFile();
				File filePath;
				filePath = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
				String currTrack = WClient.getCurrentTrack();
				if (currTrack != null) {
					int slashIndex = currTrack.lastIndexOf("/");
					if (slashIndex >= 0) {
						currTrack.substring(slashIndex - 1);
					}
				}
				String newSongPath = filePath.toString() + "/" + MUSIC_DIR
						+ "/" + currTrack;
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(new File(newSongPath))));
			}
			return;

		default:
			return;
		}
	}

	@SuppressLint("NewApi")
	private void customSelectedMenuOnclick(View view) {
		final CharSequence[] items = { getString(R.string.order_alphabetical),
				getString(R.string.order_newest),
				getString(R.string.order_oldest) };
		/*
		 * if (android.os.Build.VERSION.SDK_INT >= 13) { popup = new
		 * PopupMenu(mnact, view); popuplistener = new OnMenuItemClickListener()
		 * {
		 * 
		 * @Override public boolean onMenuItemClick(MenuItem item) {
		 * Toast.makeText(getApplicationContext(), item.toString(),
		 * Toast.LENGTH_SHORT).show(); orderItems(item.toString()); View view =
		 * item.getActionView(); return true; }
		 * 
		 * };
		 * 
		 * popup.setOnMenuItemClickListener(popuplistener); MenuInflater
		 * inflater = popup.getMenuInflater(); Menu popupMenu = popup.getMenu();
		 * inflater.inflate(R.menu.ontouch_menu, popup.getMenu()); popup.show();
		 * } else {
		 */
		final AlertDialog builder = new AlertDialog.Builder(this).create();
		builder.setTitle("Order:");
		LayoutInflater adbInflater = this.getLayoutInflater();
		final View checkboxLayout = adbInflater.inflate(R.layout.order_menu,
				null);
		builder.setView(checkboxLayout);

		RadioGroup rg = (RadioGroup) checkboxLayout
				.findViewById(R.id.order_group);

		rg.check(this.getSortOrderInt());

		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				group.check(checkedId);
				mnact.setSortOrderInt(checkedId);
				RadioButton checkedOption = (RadioButton) checkboxLayout
						.findViewById(checkedId);
				String selectedRadioValue = (String) checkedOption.getText();
				mnact.orderItems(selectedRadioValue);
				builder.dismiss();
			}
		});
		builder.show();
		// }
	}

	private AlertDialog Delete() {
		AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
				// set message, title, and icon
				.setTitle("Delete")
				.setMessage(
						getResources().getString(
								R.string.delete_confirm_button_text)
								+ " " + this.mDeletetitle + "?")
				// .setIcon(R.drawable.delete)

				.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								String curTrack = mPlaycuradapter
										.getCurrentTrack();
								if (curTrack.equals(mDeletetitle)) {
									playPause();
									progressbar.setProgress(0);
									nextTrack();
								}
								MusicUtils.deleteTracks(MainActivity.this,
										deleteList);
								mPlaycuradapter.changeCursor(MediaMeta
										.getMusicCursor(
												mnact,
												MediaStore.Audio.Media.DEFAULT_SORT_ORDER));
								dialog.dismiss();
							}

						})

				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();

							}
						}).create();
		return myQuittingDialogBox;

	}

	public void downloadClickable() {
		((Button) findViewById(R.id.btnclientCopy)).setClickable(true);
	}

	public void fileProgressControl(final int xprog) {

		runOnUiThread(new Runnable() {
			public void run() {
				if (xprog == DOWNLOADERR) {
					downloadClickable(); // Can download again
					progressdialog.dismiss();
					Toast.makeText(getBaseContext(), "Copy error. Re-try",
							Toast.LENGTH_LONG).show();

				} else if (xprog == 0) {
					downloadClickable();
					progressdialog.dismiss();
					Toast.makeText(getBaseContext(), "Copy Complete",
							Toast.LENGTH_SHORT).show();
				} else if (xprog < 0) {
					progressdialog.setMax(-xprog);
				} else {
					progressdialog.setProgress(xprog);
				}
			}
		});
	}

	public String getCurrentTrackName() {
		int pos;
		String track = null;
		
		pos = playlist.getCheckedItemPosition();
		if (pos != ListView.INVALID_POSITION) {
			if(!PLAYLIST_TAB){
				track = mPlaycuradapter.getTrackPath(pos); 
			} else if(PLAYLIST_TAB){
				track = this.mPlaylistAdapter.getTrackName(pos);
			}
		}

		return (track);
	}

	public File getMusicDirectory() {

		File dirpath = null;
		String dir = null;

		int pos = playlist.getCheckedItemPosition();
		if (pos != ListView.INVALID_POSITION) {
			if(!PLAYLIST_TAB){
				dir = mPlaycuradapter.getTrackDir(pos); // Path to song in music
													// dir
			} else if(PLAYLIST_TAB){
				dir = this.mPlaylistAdapter.getTrackDir(pos);
			}
		}

		return new File(dir);
	}

	public MusicManager getMusicManager() {
		return mMusicManager;
	}

	public PlayCurAdapter getPlayCurAdapter() {
		// TODO Auto-generated method stub
		return mPlaycuradapter;
	}

	public ListView getPlaylist() {
		return playlist;
	}

	public int getSortOrderInt() {
		return mSortOrderInt;
	}

	public String getSortOrderString() {
		return mSortOrderString;
	}

	public TabsManager getTabsManager() {
		return mTabsManager;
	}

	public Music getTrack() {
		synchronized (this) { // May not be needed not sure on Sync
			return (mMusicManager.getTrack());
		}
	}

	public boolean inClient() {
		return (WClient != null);
	}

	private void incrementLoadCount() {

		int cnt = Prefs.getLoadCount(this) + 1;
		System.out.println("Increment loadcount : " + cnt);
		Prefs.setLoadCount(this, cnt);
	}

	@SuppressLint("NewApi")
	private void initialize() {
		this.setSortOrderInt(Prefs.getSortOrder(this));
		mnact = this;
		WebSocketImpl.DEBUG = false;
		mIntentReceiver = new NotificationReceiver(mnact);
		mTabsManager = new TabsManager(this);
		gestureDetector = new GestureDetector(this,
				new FS_GestureDetector(this));
		gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		mDbadapter = new MusicDbAdapter(mnact);

		IntentFilter commandFilter = new IntentFilter();

		commandFilter.addAction(PLAY);
		commandFilter.addAction(NEXT);
		commandFilter.addAction(PREVIOUS);
		registerReceiver(mIntentReceiver, commandFilter);

		playlist = (ListView) findViewById(R.id.playlist);
		// playlist.setOnCreateContextMenuListener(this);
		playlist.setOnItemLongClickListener(this);
		playlist.setOnTouchListener(gestureListener);
		serverlist = (ListView) View.inflate(this, R.layout.server_adapter,
				null);
		((ViewGroup) findViewById(R.id.midfield)).addView(serverlist);

		serveradapter = new ServerAdapter(this);
		serverlist.setOnItemClickListener(serveradapter);
		serverlist.setAdapter(serveradapter);
		serverlist.setOnTouchListener(gestureListener);
		mMusicManager = new MusicManager(this);
		loadPlayAdapter();

		seekbar = (SeekBar) findViewById(R.id.seekbar);
		progressbar = (ProgressBar) findViewById(R.id.progressbar);

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// textOut("SeekBar value is "+progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				toClients(CMD_PAUSE);
				mMusicManager.getTrack().pause();
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				int xseek;

				xseek = seekBar.getProgress();
				mMusicManager.getTrack().seekTo(xseek);
				toClients(CMD_RESUME + xseek);
				mMusicManager.getTrack().play();
			}
		});

		mTabsManager.setActiveMenu(R.id.btnSongs);
		Button songsButton = ((Button) findViewById(R.id.btnSongs));
		songsButton.setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onClick(View view) {
				customSelectedMenuOnclick(view);

			}
		});
		WebServer.versionChangeHTML(this); // This must be set before call to
											// turnServerON

		if (Prefs.getOnAir(this))
			turnServerOn(this);

		// mNsdHelper.discoverServices();

		incrementLoadCount();
		int cnt = Prefs.getLoadCount(this);
		if (cnt <= INSTALL_AUTO) {
			// System.out.println("Before setName for first installs");
			PhotoActivity.setNamePhoto(this);
			// WebClient.autoSelect(this, INSTALL_AUTO - cnt); Leave this in
			// used with nsd
		}

		Music.setMuted(false);
		// System.out.println("Out Initialize");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 1.0f;
			Button tbtn = (Button) findViewById(R.id.btnSongs);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnAlbums);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnArtists);
			tbtn.setLayoutParams(params);
			System.out.println("Got configuration change : Landscape");
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 0.3f;
			Button tbtn = (Button) findViewById(R.id.btnSongs);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnAlbums);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnArtists);
			tbtn.setLayoutParams(params);
			System.out.println("Got configuration change : Portrait");
		}
	}

	public void initNotification() {

		Intent notifIntent = new Intent(this, MainActivity.class);
		notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, notifIntent,
				0);

		PendingIntent pIntentPlay = PendingIntent.getBroadcast(this, 0,
				new Intent(PLAY), 0);

		PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0,
				new Intent(NEXT), 0);

		PendingIntent pIntentPrevious = PendingIntent.getBroadcast(this, 0,
				new Intent(PREVIOUS), 0);

		mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotifyBuilder = new NotificationCompat.Builder(this);
		mNotifyBuilder
				.setPriority(Notification.PRIORITY_HIGH)
				.setContentTitle("Fullsink")
				.setContentText(getCurrentTrackName())
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent)
				.addAction(R.drawable.ic_media_previous,
						this.getString(R.string.previous), pIntentPrevious);
		if (mMusicManager.isTuning()) {
			mNotifyBuilder.addAction(R.drawable.ic_media_play,
					this.getString(R.string.play), pIntentPlay);
		} else {
			mNotifyBuilder.addAction(R.drawable.ic_media_pause,
					this.getString(R.string.play), pIntentPlay);
		}
		mNotifyBuilder.addAction(R.drawable.ic_media_next,
				this.getString(R.string.next), pIntentNext);

		mNotify = mNotifyBuilder.build();
		mNotify.flags |= Notification.FLAG_ONGOING_EVENT
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP;
		mgr.notify(NOTIFY_ID, mNotify);
	}

	public boolean isSockServerOn() {
		return (WServ != null);
	}

	public void loadPlayAdapter() {
		// MediaStore.Audio.Media.DEFAULT_SORT_ORDER
		int sortOrder = Prefs.getSortOrder(mnact);
		String cursorOrder = null;
		switch (sortOrder) {
		case R.id.alphabetical:
			cursorOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
			break;
		case R.id.oldest:
			cursorOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
			break;
		case R.id.newest:
			cursorOrder = MediaStore.Audio.Media.DATE_MODIFIED;
			break;
		case R.id.playlist:
			cursorOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
			break;

		}
		mPlaycuradapter = new PlayCurAdapter(this, MediaMeta.getMusicCursor(
				this, cursorOrder));
		playlist.setOnItemClickListener(mPlaycuradapter);
		playlist.setAdapter(mPlaycuradapter);

		mMusicManager.setIsTuning(false);

		if (!mPlaycuradapter.isEmpty()) {
			playlist.setItemChecked(0, true);
			mMusicManager.setCurrentSongPosition(0);
			mMusicManager.loadTrack(null);
		}

	}

	public void manageRemote(final String arg) {

		runOnUiThread(new Runnable() {
			public void run() {

				if (arg.equals("T")) {
					findViewById(R.id.viewRemote).setVisibility(View.VISIBLE);
				} else if (arg.equals("F")) {
					findViewById(R.id.viewRemote).setVisibility(View.GONE);
					((ImageView) findViewById(R.id.imgRemote))
							.setImageResource(R.drawable.fs_remote_white_dot);
				} else if (NetStrat.getWifiApIpAddress().indexOf(arg) >= 0) {
					((ImageView) findViewById(R.id.imgRemote))
							.setImageResource(R.drawable.fs_remote_blue_dot);
				} else {
					((ImageView) findViewById(R.id.imgRemote))
							.setImageResource(R.drawable.fs_remote_white_dot);
				}
			}

		});
	}

	public void nextTrack() {
		mMusicManager.loadTrack(setTrack(1));
		mMusicManager.playTrack(false);
		mNotifyBuilder.setContentText(getCurrentTrackName());
		mgr.notify(NOTIFY_ID, mNotifyBuilder.build());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 1.0f;
			Button tbtn = (Button) findViewById(R.id.btnSongs);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnAlbums);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnArtists);
			tbtn.setLayoutParams(params);
			System.out.println("Got configuration change : Landscape");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 0.3f;
			Button tbtn = (Button) findViewById(R.id.btnSongs);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnAlbums);
			tbtn.setLayoutParams(params);
			tbtn = (Button) findViewById(R.id.btnArtists);
			tbtn.setLayoutParams(params);
			System.out.println("Got configuration change : Portrait");
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NetStrat.setSsid(this);
		// load action bar for OS 2.3 or greater
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar ab = getActionBar();
			ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			ab.setCustomView(R.layout.actionbar);
			// ImageView photoActionBarView = (ImageView)
			// findViewById(R.id.photoActionBar);

			Bitmap bm = PhotoActivity.getPhotoBitmap(this);
			if (bm != null) {
				((ImageView) findViewById(R.id.photoActionBar))
						.setImageBitmap(bm);
			}

			ImageButton logoButton = (ImageButton) findViewById(R.id.logo_record);
			final String ssid = NetStrat.getSsid();
			logoButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(), ssid,
							Toast.LENGTH_LONG).show();
				}
			});
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Not sure if this is needed.
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"Fullsink");
		setContentView(R.layout.activity_main);

		initialize();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfoIn) {
		if (mTabsManager.getActiveTab() == (R.id.btnSongs) || mSongsSubmenu) {
			menu.add(0, DELETE_ITEM, 0, R.string.delete);
			// menu.add(0, ADD_TO_PLAYLIST, 1, R.string.add_playlist);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mDiscoverHttpd != null) {
			mDiscoverHttpd.constantPoll(-1);
		}

		System.out.println("In DESTROY");
		NetStrat.logServer(this, SERVER_OFFLINE);

		stopSockServer(true);
		stopHttpdServer();
		stopSockClient();
		unregisterReceiver(mIntentReceiver);
		System.out.println("Destroy OUT");
		mDbadapter.close();
		mgr.cancel(NOTIFY_ID);
		Prefs.setSortOrder(mnact, "" + getSortOrderInt());
	}

	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, final long id) {
		final View thisview = view;
		final int pos = position;

		// int previousSelected = playlist.getCheckedItemPosition();
		// mPreviousView = (CheckableRelativeLayout)
		// playlist.getChildAt(previousSelected);
		//
		// getMusicManager().setCurrentSongPosition(position);
		// playlist.setItemChecked(position, true);
		TextView field = (TextView) view.findViewById(R.id.title);
		final String selectedTitle = (String) field.getText();
		// mPlaycuradapter.setCurrentTrack(deleteTitle);
		// mPlaycuradapter.moveHighlight(view);

		Timer longpressTimer = new Timer();
		FS_GestureDetector.moving = false;
		longpressTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (((mTabsManager.getActiveTab() == (R.id.btnSongs)) || mSongsSubmenu)
						&& !FS_GestureDetector.moving) {
					mDeletetitle = selectedTitle;
					ImageView imageView = (ImageView) thisview
							.findViewById(R.id.image);
					final int songId = Integer.valueOf(imageView.getTag()
							.toString());
					deleteList = new long[1];
					deleteList[0] = (int) songId;
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							mnact);
					// builder.setOnKeyListener(new
					// DialogInterface.OnKeyListener() {
					// @Override
					// public boolean onKey (DialogInterface dialog, int
					// keyCode, KeyEvent event) {
					// if (keyCode == KeyEvent.KEYCODE_BACK) {
					// mPlaycuradapter.moveHighlight(mPreviousView);
					// return true;
					// }
					// return false;
					// }
					// });
					builder.setTitle(selectedTitle);
					String items[];
					if (PLAYLIST_TAB) {
						items = new String[1];
						items[0] = getString(R.string.remove_from_playlist);
					} else {
						items = new String[2];
						items[0] = getString(R.string.delete);
						items[1] = getString(R.string.add_playlist);
					}
					builder.setItems(items,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int item) {
									if (PLAYLIST_TAB) {
										mDbadapter.removeSong(songId);
										mPlaylistAdapter
												.changeCursor(mDbadapter
														.fetchSongs());
									} else {
										switch (item) {
										case DELETE_ITEM:
											AlertDialog deleteDialog = Delete();
											deleteDialog.show();
										case ADD_TO_PLAYLIST:
											TextView fieldTitle = (TextView) thisview
													.findViewById(R.id.title);
											String title = (String) fieldTitle
													.getText();
											String artist = (String) ((TextView) thisview
													.findViewById(R.id.album))
													.getText();
											String albumId = (String) (thisview
													.findViewById(R.id.image))
													.getTag();
											// PlaylistManager.addToPlaylist(title,
											// artist, albumId, mnact);
											String dir = mPlaycuradapter.getTrackDir(pos);
											String titleRaw = mPlaycuradapter.getTrackPath(pos); 
											mDbadapter.open();
											mDbadapter.addTrackInfo(
													Long.toString(songId),
													albumId, artist, title, dir, titleRaw);
										}
									}
								}
							});
					mnact.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog alert = builder.create();
							alert.show();
						}
					});

				} else if (mnact.PLAYLIST_TAB) {

				}
			}
		}, 1000); // 1 second delay

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) { // Back key pressed
			if (albumSelected) {
				setAlbumSelected(false);
				playlist.setAdapter(albumAdapter);
				playlist.setOnItemClickListener(albumAdapter);
				setSongsSubmenu(false);
				return true;
			} else if (artistSelected) {
				setArtistSelected(false);
				playlist.setAdapter(artistAdapter);
				playlist.setOnItemClickListener(artistAdapter);
				setSongsSubmenu(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String value = extras.getString(Const.CMD_PLAY);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			toSettings(item);
			return true;
		case R.id.action_photo:
			toPhoto(item);
			return true;
		case R.id.action_help:
			toHelp(item);
			return true;
		case R.id.action_ipaddress:
			toIPAddress(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// private void notifyChange(String what) {
	//
	// Intent i = new Intent(what);
	// i.putExtra("id", Long.valueOf(getAudioId()));
	// i.putExtra("artist", getArtistName());
	// i.putExtra("album",getAlbumName());
	// i.putExtra("track", getTrackName());
	// sendBroadcast(i);
	//
	// if (what.equals(QUEUE_CHANGED)) {
	// saveQueue(true);
	// } else {
	// saveQueue(false);
	// }
	//
	// // Share this notification directly with our widgets
	// mAppWidgetProvider.notifyChange(this, what);
	// }

	@Override
	public void onPause() {
		super.onPause();

		System.out.println("In PAUSE");

		wakeLock.release();
		// mNsdHelper.stopDiscovery(); Keep out was problems

		if (isFinishing()) {
			mMusicManager.clearCurrentTrack();
			finish();
		}
	}

	public void onPlayClick(String prevFile) {
		this.initNotification();
		mMusicManager.loadTrack(prevFile);
		if (!mMusicManager.isTuning()) {
			mMusicManager.setIsTuning(true);
			((ImageView) findViewById(R.id.imgPlayPause))
					.setImageResource(R.drawable.ic_media_pause);
		}
		mMusicManager.playTrack(false);

	}

	@Override
	public void onResume() {
		super.onResume();
		wakeLock.acquire();
		System.out.println("In RESUME");
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(PLAY);
		commandFilter.addAction(NEXT);
		commandFilter.addAction(PREVIOUS);
		registerReceiver(mIntentReceiver, commandFilter);
		if (isSockServerOn())
			WServ.sendTrackData(null); // Will update changes in settings
		// mNsdHelper.discoverServices(); Keep out was problems
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ImageView photoActionBarView = (ImageView) findViewById(R.id.photoActionBar);
			Bitmap bitmap = PhotoActivity.getPhotoBitmap(this);
			if (bitmap != null)
				photoActionBarView.setImageBitmap(bitmap);
		}
	}

	private void orderItems(String order) {
		int selectedTab = mTabsManager.getActiveTab();
		if (order.equals(getString(R.string.order_alphabetical))) {
			playlist.setAdapter(mPlaycuradapter);
			PLAYLIST_TAB = false;
			switch (mTabsManager.ACTIVE_TAB) {
			case R.id.btnSongs: {
				mPlaycuradapter.changeCursor(MediaMeta.getMusicCursor(mnact,
						MediaStore.Audio.Media.DEFAULT_SORT_ORDER));
				setSortOrderString(MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				return;
			}
			case R.id.btnAlbums: {
				albumAdapter.changeCursor(MediaMeta.getAlbumCursor(this,
						MediaStore.Audio.Albums.DEFAULT_SORT_ORDER));
				return;
			}
			case R.id.btnArtists: {
				artistAdapter.changeCursor(MediaMeta.getArtistCursor(this,
						MediaStore.Audio.Media.DEFAULT_SORT_ORDER));
				return;
			}
			}
		} else if (order.equals(getString(R.string.order_newest))) {
			playlist.setAdapter(mPlaycuradapter);
			PLAYLIST_TAB = false;
			switch (mTabsManager.ACTIVE_TAB) {
			case R.id.btnSongs: {
				mPlaycuradapter.changeCursor(MediaMeta.getMusicCursor(mnact,
						MediaStore.Audio.Media.DATE_MODIFIED));
				setSortOrderString(MediaStore.Audio.Media.DATE_MODIFIED);
				return;
			}
			case R.id.btnAlbums: {
				albumAdapter.changeCursor(MediaMeta.getAlbumCursor(this,
						MediaStore.Audio.Albums.FIRST_YEAR + " ASC"));
				return;
			}
			case R.id.btnArtists: {
				artistAdapter.changeCursor(MediaMeta.getArtistCursor(this,
						MediaStore.Audio.Media.DATE_MODIFIED));
				return;
			}
			}
		} else if (order.equals(getString(R.string.order_oldest))) {
			playlist.setAdapter(mPlaycuradapter);
			PLAYLIST_TAB = false;
			switch (mTabsManager.ACTIVE_TAB) {
			case R.id.btnSongs: {
				mPlaycuradapter.changeCursor(MediaMeta.getMusicCursor(mnact,
						MediaStore.Audio.Media.DATE_MODIFIED + " DESC"));
				setSortOrderString(MediaStore.Audio.Media.DATE_MODIFIED
						+ " DESC");
				return;
			}
			case R.id.btnAlbums: {
				albumAdapter.changeCursor(MediaMeta.getAlbumCursor(this,
						MediaStore.Audio.Albums.FIRST_YEAR + " DESC"));
				return;
			}
			case R.id.btnArtists: {
				artistAdapter.changeCursor(MediaMeta.getArtistCursor(this,
						MediaStore.Audio.Media.DATE_MODIFIED + " DESC"));
				return;
			}
			}

		} else if (order.equals(getString(R.string.playlist))) {
			PLAYLIST_TAB = true;
			mDbadapter.open();
			mPlaylistAdapter = new PlaylistAdapter(this,
					mDbadapter.fetchSongs());
			playlist.setAdapter(mPlaylistAdapter);
			playlist.setOnItemClickListener(mPlaylistAdapter);
		}
	}

	public void playNextTrack() {

		if (!inClient()) {
			if (ShuffleLoop == MODE_LOOP) {
				butNext(0);
			} else {
				butNext(1);
			}
		}
	}

	public void playPause() {
		initNotification();
		if (mMusicManager.isTuning()) {
			toClients(CMD_PAUSE);
			mMusicManager.setIsTuning(false);
			((ImageView) findViewById(R.id.imgPlayPause))
					.setImageResource(R.drawable.ic_media_play);
			mMusicManager.getTrack().pause();
		} else {
			mMusicManager.setIsTuning(true);
			((ImageView) findViewById(R.id.imgPlayPause))
					.setImageResource(R.drawable.ic_media_pause);
			mMusicManager.playTrack(true);

		}

	}

	public void prepClientScreen() {
		progressbar.setProgress(0);
		setDownload(false);
		setServerIndicator(MODE_STOP);
	}

	public void previousTrack() {
		mMusicManager.loadTrack(setTrack(-1));
		mMusicManager.playTrack(false);
		mNotifyBuilder.setContentText(getCurrentTrackName());
		mgr.notify(NOTIFY_ID, mNotifyBuilder.build());
	}

	@Override
	public void run() {
		int currentPosition = 0;

		while (mMusicManager.isTrack()) {

			try {
				currentPosition = getTrack().getCurrentPosition();
			} catch (Exception ex) {
				System.out.println("Exception in thread run for seek : " + ex);
			}

			try {
				boolean playing = mMusicManager.getTrack().isPlaying();
				if (playing) {
					if (inClient()) {
						progressbar.setMax(getTrack().getDuration());
						progressbar.setProgress(currentPosition);
					} else {
						seekbar.setProgress(currentPosition);
					}
				}
			} catch (Exception ex) {
				Log.e("Fullsink", "Error in track.isPlaying()", ex);
				StringBuffer result = new StringBuffer();
				StackTraceElement[] trace = ex.getStackTrace();
				ex.printStackTrace();
				for (int i = 0; i < trace.length; i++) {
					result.append(trace[i].toString()).append('\n');
				}
				Log.e("Stack Trace", result.toString());

			}

			try {
				Thread.sleep(1000);
			} catch (Exception ex) { // InterruptedException
				return;
			}
		}
	}

	public void setAlbumSelected(boolean selected) {
		albumSelected = selected;
	}

	public void setArtistSelected(boolean selected) {
		artistSelected = selected;
	}

	public void setDownload(final boolean enable) {

		runOnUiThread(new Runnable() {
			public void run() {
				Button copybut = (Button) findViewById(R.id.btnclientCopy);

				if (enable) {
					copybut.setBackgroundResource(R.drawable.buttonblack);
					copybut.setClickable(true);
				} else {
					copybut.setBackgroundResource(R.drawable.buttongrey);
					copybut.setClickable(false);
				}
			}
		});
	}

	public void setPlayCurAdapter(PlayCurAdapter playCurAdapter) {
		mPlaycuradapter = playCurAdapter;

	}

	public void setServerIndicator(final int mode) {

		runOnUiThread(new Runnable() {
			public void run() {
				ImageView imgbut = (ImageView) findViewById(R.id.imgServerIndicator);

				if (mode == MODE_PAUSE) {
					imgbut.setImageResource(R.drawable.ic_media_pause);
				} else if (mode == MODE_PLAY) {
					imgbut.setImageResource(R.drawable.ic_media_play);
				} else {
					imgbut.setImageResource(R.drawable.ic_media_stop);
				}
			}
		});
	}

	public void setSongsSubmenu(boolean active) {
		Button tbtn = (Button) findViewById(R.id.btnSongs);
		mSongsSubmenu = active;
		if (active) {
			tbtn.setTypeface(Typeface.DEFAULT_BOLD);
			tbtn.setClickable(false);
		} else {
			tbtn.setTypeface(Typeface.DEFAULT);
			tbtn.setClickable(true);
		}
	}

	public void setSortOrderInt(int sortOrder) {
		mSortOrderInt = sortOrder;
	}

	public void setSortOrderString(String sortOrder) {
		mSortOrderString = sortOrder;
	}

	private String setTrack(int direction) {

		int pos = 0;
		String prevtrack = null;

		// Get current position and if none check, should not happen, got to top
		pos = playlist.getCheckedItemPosition();
		if (pos == ListView.INVALID_POSITION) {
			if (mPlaycuradapter.isEmpty()) {
				pos = -1;
			} else {
				pos = 0;
			}
			return (prevtrack);
		}

		prevtrack = getCurrentTrackName();

		if (ShuffleLoop == MODE_SHUFFLE && mPlaycuradapter.getCount() > 3) {
			int temp = new Random().nextInt(mPlaycuradapter.getCount());
			int safety = 0;
			while (safety < 20) {
				if (temp != pos) {
					pos = temp;
					break;
				}
				temp++;
				if (temp > mPlaycuradapter.getCount() - 1) {
					temp = 0;
				}
				++safety;
			}
		} else if (direction == -1) {
			pos--;
			if (pos < 0) {
				pos = mPlaycuradapter.getCount() - 1;
			}
		} else if (direction == 1) {
			pos++;
			if(!PLAYLIST_TAB){
				if (pos > mPlaycuradapter.getCount() - 1) {
					pos = 0;
				}
			} else if(PLAYLIST_TAB){
				if (pos > mPlaylistAdapter.getCount() - 1) {
					pos = 0;
				}
			}
		}
		playlist.setItemChecked(pos, true);
		if (!mMusicManager.isTuning()) {
			mMusicManager.setIsTuning(true);
			((ImageView) findViewById(R.id.imgPlayPause))
					.setImageResource(R.drawable.ic_media_pause);
		}
		mMusicManager.setCurrentSongPosition(pos);// used to be 0 instead of pos
		// might need to move highlight here as well
		playlist.smoothScrollToPosition(pos);
		return (prevtrack);
	}

	public void startHttpdServer(int httpdPort, String ipadd) {

		try {
			stopHttpdServer();
			HttpdServ = new SimpleWebServer(ipadd, httpdPort, getFilesDir());
			HttpdServ.start();
			// mNsdHelper.registerService(httpdPort);
			System.out.println("HttpdServ started Add : " + ipadd + "  Port : "
					+ httpdPort);
			NetStrat.storeHttpdPort(httpdPort);
		} catch (Exception ex) {
			System.out.println("HttpdServer error  : " + ex);
		}
	}

	public void startSockClient(int webSockPort, String ipadd, int httpdPort) {

		try {
			System.out.println("In startSockClient");
			stopSockClient();
			WClient = new WebClient(webSockPort, ipadd, httpdPort,
					MainActivity.this);
			WClient.connect();

		} catch (Exception ex) {
			System.out.println("WebClient error : " + ex);
		}
	}

	public void startSockServer(int port, String ipadd) {
		WebSocketImpl.DEBUG = false; // This was true originally
		boolean serverinit = false;

		// System.out.println( "In WebSockServer : "+ipadd);
		try {
			// stopSockServer();
			// This was changed to allow websocket server to remain running for
			// 2.3 bug

			if (!isSockServerOn()) {
				System.out.println("In startSockServer");
				WServ = new WebServer(port, ipadd, MainActivity.this);
				serverinit = true;

			}

			WServ.deleteCues();
			WServ.cueTrack(getMusicDirectory(), getCurrentTrackName()); // Copy
																		// for
																		// stream

			if (serverinit) {
				WServ.start();
			}

			System.out.println("WebSockServ started on port: "
					+ WServ.getPort());
			System.out.println("WebSockServ start Add : " + WServ.getAddress());
			WServ.generateServerId(WServ.getPort());

		} catch (Exception ex) {
			System.out.println("WebSockServer host not found error" + ex);
		}
	}

	public void stopHttpdServer() {

		try {
			if (HttpdServ != null) {
				// mNsdHelper.unregisterService();
				HttpdServ.stop();
				HttpdServ = null;
				NetStrat.storeHttpdPort(0);
			}

		} catch (Exception ex) {
			System.out.println("HttpdServer stop : " + ex);
		}
	}

	public void stopSockClient() {

		try {
			if (WClient != null) {
				WClient.close();
				WClient = null;
			}
		} catch (Exception ex) {
			System.out.println("WebSockClient stop error" + ex);
		}
	}

	public void stopSockServer(boolean closeflg) {

		try {
			if (isSockServerOn()) {
				// System.out.println( "WebSockServer there are clients : " +
				// WServ.isClient());
				// There seems to be a bug when there are no connections the
				// stop hangs in 2.3
				// To get around this just let the socketserver run if no
				// connections as no one will be able
				// to connect as the Httpd server is the contact point for a
				// live server

				if (android.os.Build.VERSION.SDK_INT > 10 || closeflg
						|| WServ.isClient()) {
					// System.out.println("WebSockServ stopping");
					WServ.stop();
					WServ = null;
				}

			}
		} catch (Exception ex) {
			System.out.println("WebSockServer stop error" + ex);
		}
	}

	// This is not good and should be reviewed
	public void toastOut(final String xmess, final int length) {

		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getBaseContext(), xmess, length).show();
			}
		});
	}

	public void toClients(String mess) {

		if (isSockServerOn()) {
			WServ.sendToAll(mess);
		}
	}

	public void toHelp(MenuItem item) {
		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

	public void toIPAddress(MenuItem item) {

		if (!isSockServerOn())
			turnServerOn(this);

		Intent intent = new Intent(this, IPAddressActivity.class);
		startActivity(intent);
	}

	public void toPhoto(MenuItem item) {
		Intent intent = new Intent(this, PhotoActivity.class);
		startActivity(intent);
	}

	public void toSettings(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void turnServerOff(final MainActivity mnact) {

		stopHttpdServer();
		stopSockServer(false);

		NetStrat.logServer(mnact, SERVER_OFFLINE); // Server is turned off

	}

	public void turnServerOn(final MainActivity mnact) {

		final String ipadd = NetStrat.getWifiApIpAddress();
		if (ipadd != null) {
			final int httpdPort = NetStrat.getHttpdPort(mnact);
			final int webSockPort = NetStrat.getSocketPort(mnact);

			NetStrat.logServer(mnact, ipadd, Prefs.getName(mnact), webSockPort,
					httpdPort);

			new Thread(new Runnable() {
				public void run() {
					try {

						startHttpdServer(httpdPort, ipadd);

						System.out.println("WebSock Port : " + webSockPort
								+ "  IPADD : " + ipadd);
						startSockServer(webSockPort, ipadd);

					} catch (Exception ex) {
						System.out.println("Select thread exception : " + ex);
					}

					runOnUiThread(new Runnable() {
						public void run() {
							((ImageView) findViewById(R.id.imgServer))
									.setImageResource(R.drawable.ic_media_route_on_holo_blue);
						}
					});
				}
			}).start();
		} else {
			FS_Util.showConnectionWarning(mnact);
		}
	}

}