package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

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

	// Is current page available (deleted or not deleted)
	// Default is available (not deleted)
	private boolean available = true;

	private final UUID ID;

	private PageImpl() {
		// Do nothing but set UUID, do not call Page
		ID = UUID.randomUUID();
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
		if (this.prevAvailablePage != null) {
			this.prevAvailablePage.setNextAvailablePage(this.nextAvailablePage);
		}
		if (this.nextAvailablePage != null) {
			this.nextAvailablePage.setPrevAvailablePage(this.prevAvailablePage);
		}
		this.prevAvailablePage = null;
		this.nextAvailablePage = null;
		this.available = false;
	}

	PageImpl(String rootPath) {
		ID = UUID.randomUUID();
		this.mRecordFile = new File(rootPath + "/" + System.currentTimeMillis());
		this.prevAvailablePage = null;
		this.prevPage = null;
		this.nextAvailablePage = null;
		this.nextPage = null;
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

	@Override
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

	@Override
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
		if (mRecordFile == null || !mRecordFile.exists()) {
			return false;
		}
		return true;
	}

	@Override
	public MediaPlayer startPlayingAudio() {
		if (mRecordFile == null || !mRecordFile.exists()) {
			return null;
		}

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

	@Override
	public void stopPlayingAudio(MediaPlayer mPlayer) {
		mPlayer.reset();
		mPlayer.release();
		mPlayer = null;
	}

	@Override
	public boolean isAvailable() {
		if (available == false) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null
				|| !other.getClass().isAssignableFrom(this.getClass())) {
			return false;
		}

		return this.ID.equals(((PageImpl) other).ID);

	}

	@Override
	public int hashCode() {
		return ID.hashCode();
	}
}
