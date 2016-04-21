package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.hapticebook.data.book.Page;

public class PageImpl implements Serializable, Page {

	private static final long serialVersionUID = 7235942974385493408L;

	private PageImage mImage;
	private File mRecordFile;
	private HapticLayer mHaptic; // HapticLayer empty for now

	// Previous page including deleted page
	private Page prevPage;
	// Previous existing (not deleted) page
	private Page prevAvailablePage;
	// Next page including deleted page
	private Page nextPage;
	// Next existing (not deleted) page
	private Page nextAvailablePage;

	private PageImpl() {
		// Do nothing, do not call Page
	}

	@Override
	public void setPrevPage(Page prevPage) {
		this.prevPage = prevPage;
	}

	@Override
	public void setPrevAvailablePage(Page prevAvailablePage) {
		this.prevAvailablePage = prevAvailablePage;
	}

	@Override
	public void setNextPage(Page nextPage) {
		this.nextPage = nextPage;
	}

	@Override
	public void setNextAvailablePage(Page nextAvailablePage) {
		this.nextAvailablePage = nextAvailablePage;
	}

	@Override
	public Page getNextPage(boolean includeDeleted) {
		if (includeDeleted) {
			return nextPage;
		} else {
			return nextAvailablePage;
		}
	}

	@Override
	public Page getPrevPage(boolean includeDeleted) {
		if (includeDeleted) {
			return prevPage;
		} else {
			return prevAvailablePage;
		}
	}

	@Override
	public void delete() {
		this.prevAvailablePage.setNextAvailablePage(this.nextAvailablePage);
		this.nextAvailablePage.setPrevAvailablePage(this.prevAvailablePage);
	}

	private PageImpl(String rootPath) {
		this.mRecordFile = new File(rootPath + System.currentTimeMillis());
	}

	public File getRecordFile() {
		return mRecordFile;
	}

	public PageImage getmImage() {
		return mImage;
	}

	public void setmImage(Bitmap mImage) {
		this.mImage = new PageImage(mImage);
	}

	public HapticLayer getmHaptic() {
		return mHaptic;
	}

	public void setmHaptic(HapticLayer mHaptic) {
		this.mHaptic = mHaptic;
	}

	public MediaPlayer startPlayingRecording() {
		MediaPlayer mPlayer = new MediaPlayer();
		try {
			if (mRecordFile != null) {
				mPlayer.setDataSource(mRecordFile.getAbsolutePath());
				mPlayer.prepare();
				mPlayer.start();
			} else {
				Log.d("", "mRecordFile is empty, cannot play");
				return null;
			}
		} catch (IOException e) {
			Log.e("", "play prepare() failed");
			return null;
		}
		return mPlayer;
	}

	public void stopPlayingRecording(MediaPlayer mPlayer) {
		mPlayer.reset();
		mPlayer.release();
		mPlayer = null;
	}

	public MediaRecorder startRecording() {

		Log.d("", "mRecordFile is: " + mRecordFile);

		MediaRecorder mRecorder = new MediaRecorder();

		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("", "record prepare() failed");
			return null;
		}
		mRecorder.start();
		return mRecorder;
	}

	public void stopRecording(MediaRecorder mRecorder) {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
		}
	}

	@Override
	public void setImage(Bitmap mImage) {
		this.mImage = new PageImage(mImage);
	}

	@Override
	public void setImage(PageImage pageImage) {
		this.mImage = pageImage;

	}

	@Override
	public PageImage getImage() {
		return this.mImage;
	}

	@Override
	public Bitmap getBitmapImage() {
		if (mImage != null) {
			return this.mImage.getImage();
		}
		return null;
	}

	@Override
	public boolean isAudioAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MediaPlayer startPlayingAudio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopPlayingAudio(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub

	}

}
