package com.fullsink.mp;

import static com.fullsink.mp.Const.CMD_PREP;
import static com.fullsink.mp.Const.CMD_RESUME;
import static com.fullsink.mp.Const.CMD_STOP;
import static com.fullsink.mp.Const.MODE_PAUSE;
import static com.fullsink.mp.Const.MODE_STOP;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.widget.Toast;

public class MusicManager {

	private MainActivity mnact;
	private Music mTrack;
	private boolean mIsTuning; 	// is user currently jammin out, if so automatically start
								// playing the next track
	

	public MusicManager(MainActivity mnact) {
		this.mnact = mnact;

	}

	// Loads the track by calling loadMusic
	public boolean loadTrack(String prevtrack) {
		if (mTrack != null) {
			mnact.toClients(CMD_STOP);
			mTrack.dispose();
			mTrack = null;

			if (prevtrack != null && mnact.WServ != null) { // Cleanup server cue
				WebServer.DeleteRecursive(new File(mnact.getFilesDir(), prevtrack));
			}
		}

		mTrack = loadMusic();
		return (mTrack != null);
	}

	// loads a Music instance using an external resource
	private Music loadMusic() {

		Music xmu = null;

		if (mnact.getCurrentTrackName() != null) {
			if (mnact.WServ != null) {
				mnact.WServ.cueTrack(mnact.getMusicDirectory(), mnact.getCurrentTrackName());
				mnact.getPlayCurAdapter().setCurrentTrack(mnact.getCurrentTrackName()); // Logg
																		// file
																		// for
																		// removal
																		// when
																		// next
																		// song
																		// up
			}

			try {
				FileInputStream fis = new FileInputStream(new File(
						mnact.getMusicDirectory(), mnact.getCurrentTrackName()));
				FileDescriptor fileDescriptor = fis.getFD();
				xmu = new Music(fileDescriptor, mnact);
				mnact.toClients(CMD_PREP + mnact.getCurrentTrackName()); // make sure music
																// play is
																// loaded

			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(mnact.getBaseContext(),
						"Error Loading " + mnact.getCurrentTrackName(),
						Toast.LENGTH_LONG).show();
			}
		}
		return xmu;
	}

	public void playStream(int offset) {

		if (isTrack()) {
			try {
				mnact.getTrack().seekTo(offset);
				mnact.progressbar.setMax(mTrack.getDuration());
				mnact.getTrack().play();
				mnact.setServerIndicator(MODE_PAUSE);
				mnact.setDownload(mnact.WClient.getDownload());
				new Thread(mnact).start();

			} catch (Exception e) {
				System.out.println("Thread exception : " + e);
			}
		}
	}

	// Plays the Track
	public void playTrack(boolean resume) {
		if (isTuning() && mTrack != null) {

			if (mnact.isSockServerOn() && resume) {
				mnact.toClients(CMD_RESUME + mTrack.getCurrentPosition());
			}

			if (mnact.isSockServerOn())
				mnact.WServ.sendTrackData(null);

			mTrack.play();
		} else if (mTrack == null) {
			mTrack = loadMusic();
		}
		mnact.seekbar.setMax(mTrack.getDuration());
		try {

			new Thread(mnact).start();

		} catch (Exception e) {
			System.out.println("Thread exception : " + e);
		}
	}

	public boolean isTuning() {
		// TODO Auto-generated method stub
		return mIsTuning;
	}

	public void clearStream() {
		clearCurrentTrack();
		mnact.prepClientScreen();
	}

	public void setStreamTrack(Music xtrk) {
		synchronized (this) { // May not be needed not sure on Sync
			mTrack = xtrk;

			if (!isTrack())
				mnact.setServerIndicator(MODE_STOP);
		}
	}

	public void clearCurrentTrack() {

		if (isTrack()) {
			mTrack.dispose();
			mTrack = null;
		}
	}
	
	public boolean isTrack() {
		return mTrack != null;
	}

	public Music getTrack() {
		if(mTrack == null){
			mTrack = loadMusic();
		}
		return mTrack;
	}

	public void setIsTuning(boolean value) {
		mIsTuning = value;
	}

	public void setTrack(Music track) {
		mTrack = track;
		
	}

}
