package com.example.hapticebook.data.book.impl;

import java.io.IOException;
import java.io.Serializable;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

public class PageRecorder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8303136626441083072L;
	private MediaRecorder mRecorder = null;
	private String mFileName = null;
	private MediaPlayer mPlayer = null;

	public boolean startRecording(String file) {
		this.mFileName = file;
		return startRecording();
	}

	public boolean startRecording() {

		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e("", "record prepare() failed");
			return false;
		}

		mRecorder.start();
		return true;

	}

	public void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	public boolean startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.d("", "play prepare() failed");
			return false;
		}
		return true;
	}

	public void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}
}
