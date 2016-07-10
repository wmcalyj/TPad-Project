package com.example.hapticebook.data.book.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

public class PageImpl implements Serializable, Page {

	private static final long serialVersionUID = 7235942974385493408L;

	// instead of using bitmap, only save URI
	@Deprecated
	private PageImage mImage;
	private String imagePath;
	private String filterAppliedImagePath;

	private File mRecordFile;
	private String lastSavedRecordFilePath;

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

	// 0 means no
	private HapticFilterEnum hapticFilter;
	private HapticFilterEnum lastSavedHapticFilter;

	private final UUID ID;

	private static final String root = Configuration.ROOT_PATH + "/hapticEBook/";
	private static final File dir = new File(root, "recordings");

	// Be very careful, this is a static variable which means it's
	// shared across all instance. Also, bitmap CANNOT be written into a file
	// Remember to clean it before saving.
	private static Bitmap imageBmp;

	@SuppressWarnings("unused")
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
		if (imageBmp != null) {
			imageBmp.recycle();
			imageBmp = null;
			System.gc();
		}
		if (nextAvailablePage != null) {
			String fileName;
			fileName = nextAvailablePage.getImageFilePath();
			LogService.WriteToLog(Action.NEXT_PAGE, "Move to page - " + fileName);
		} else {
			LogService.WriteToLog(Action.NEXT_PAGE, "Last page, no more next page");
		}
		if (includeDeleted) {
			return nextPage;
		} else {
			return nextAvailablePage;
		}
	}

	@Override
	public Page getPrevPage(boolean includeDeleted) {
		if (imageBmp != null) {
			imageBmp.recycle();
			imageBmp = null;
			System.gc();
		}
		if (prevAvailablePage != null) {
			String fileName;
			fileName = prevAvailablePage.getImageFilePath();
			LogService.WriteToLog(Action.PREVIOUS_PAGE, "Move to page - " + fileName);
		} else {
			LogService.WriteToLog(Action.PREVIOUS_PAGE, "First page, no more previoius page");
		}
		if (includeDeleted) {
			return prevPage;
		} else {
			return prevAvailablePage;
		}
	}

	@Override
	public void delete() {
		String fileName = this.getImageFilePath();
		LogService.WriteToLog(Action.DELETE, "Page " + fileName + " deleted");
		if (this.prevAvailablePage != null) {
			this.prevAvailablePage.setNextAvailablePage(this.nextAvailablePage);
		}
		if (this.nextAvailablePage != null) {
			this.nextAvailablePage.setPrevAvailablePage(this.prevAvailablePage);
		}
		this.prevAvailablePage = null;
		this.nextAvailablePage = null;
		this.available = false;
		if (imageBmp != null) {
			imageBmp.recycle();
			imageBmp = null;
		}
		System.gc();

	}

	PageImpl(String rootPath) {
		ID = UUID.randomUUID();
		this.prevAvailablePage = null;
		this.prevPage = null;
		this.nextAvailablePage = null;
		this.nextPage = null;
		this.hapticFilter = HapticFilterEnum.NONE;
		if (!dir.exists()) {
			dir.mkdirs();
		}
		imagePath = rootPath;
		LogService.WriteToLog(Action.ADD_PAGE, "New page created for image - " + imagePath);

	}

	@Deprecated
	public PageImage getmImage() {
		return mImage;
	}

	public void setmImage(Bitmap mImage) {
		this.mImage = new PageImage(mImage);
	}

	@Override
	public HapticFilterEnum getHapticFilter() {
		return this.hapticFilter;
	}

	@Override
	public void setHapticFilter(HapticFilterEnum hapticFilter) {
		this.hapticFilter = hapticFilter;
	}

	@Override
	public MediaRecorder startRecording() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		mRecordFile = new File(dir, timeStamp + ".m4a");
		LogService.WriteToLog(Action.RECORD_AUDIO, "Start recording audio in file - " + mRecordFile.getAbsolutePath());
		MediaRecorder mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		Log.d("", "mRecordFile is: " + mRecordFile.getAbsolutePath());

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
		LogService.WriteToLog(Action.STOP_RECORD_AUDIO,
				"Stop recording audio in file - " + mRecordFile.getAbsolutePath());
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.reset();
			mRecorder.release();
			mRecorder = null;
		}
	}

	@Override
	public boolean isAudioAvailable() {
		if (mRecordFile == null || !mRecordFile.exists()) {
			return false;
		}
		return true;
	}

	@Override
	public MediaPlayer startPlayingAudio() {
		if (mRecordFile == null || !mRecordFile.exists()) {
			Log.d("", "mRecordFile is empty, cannot play");
			LogService.WriteToLog(Action.PLAY_AUDIO, "Try to play audio but no audio file found");
			return null;
		}

		MediaPlayer mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mRecordFile.getAbsolutePath());
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e("", "play prepare() failed");
			return null;
		}
		LogService.WriteToLog(Action.PLAY_AUDIO, "Start playing audio file - " + mRecordFile.getAbsolutePath());
		return mPlayer;
	}

	@Override
	public void stopPlayingAudio(MediaPlayer mPlayer) {
		LogService.WriteToLog(Action.STOP_AUDIO, "Stop playing audio file - " + mRecordFile.getAbsolutePath());
		if (mPlayer != null) {
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
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
		if (other == null || !other.getClass().isAssignableFrom(this.getClass())) {
			return false;
		}

		return this.ID.equals(((PageImpl) other).ID);

	}

	@Override
	public int hashCode() {
		return ID.hashCode();
	}

	@Override
	public Bitmap getImageBitmap() {
		if (imageBmp != null) {
			imageBmp.recycle();
			imageBmp = null;
			System.gc();
		}
		if (imageBmp == null || imageBmp.isRecycled()) {
			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
			bmpFactoryOptions.inSampleSize = 2;
			imageBmp = BitmapFactory.decodeFile(imagePath, bmpFactoryOptions);
		}
		return imageBmp;
	}

	@Override
	public void saveAudioFile() {
		if (mRecordFile != null) {
			lastSavedRecordFilePath = mRecordFile.getAbsolutePath();
			LogService.WriteToLog(Action.SAVE_AUDIO, "Audio file saved to " + lastSavedRecordFilePath);
		}
	}

	@Override
	public void cancelAudioFile() {
		if (mRecordFile == null || !mRecordFile.exists()) {
			return;
		}
		LogService.WriteToLog(Action.CANCEL_AUDIO_SAVE, "Cancel to save audio file " + mRecordFile.getAbsolutePath());
		if (lastSavedRecordFilePath == null || lastSavedRecordFilePath.isEmpty()) {
			// No recording ever exists, set record file to null
			mRecordFile = null;
		} else {
			mRecordFile = new File(lastSavedRecordFilePath);
		}
	}

	@Override
	public void saveHapticFilter(String filterFilePath) {
		lastSavedHapticFilter = hapticFilter;
		LogService.WriteToLog(Action.SAVE_FILTER, "Save filter " + lastSavedHapticFilter.toString());
		setFilterImagePath(filterFilePath);

	}

	@Override
	public void cancelHapticFilter() {
		LogService.WriteToLog(Action.CANCEL_FILTER_SAVE, "Cancel to save filter " + hapticFilter.toString());
		if (lastSavedHapticFilter == null) {
			hapticFilter = HapticFilterEnum.NONE;
		} else {
			hapticFilter = lastSavedHapticFilter;
		}
	}

	@Override
	public String getImageFilePath() {
		return imagePath;
	}

	@Override
	public String getFilterImagePath() {
		return filterAppliedImagePath;
	}

	private void setFilterImagePath(String filterImagePath) {
		this.filterAppliedImagePath = filterImagePath;
	}

	@Override
	public boolean isUsingWallPaper() {
		return this.hapticFilter.isWallPaper();
	}

	@Override
	public void deleteNewlyTakenImage() {
		String fileName = this.getImageFilePath();
		File newlyTakenImage = new File(fileName);
		if (newlyTakenImage != null) {
			newlyTakenImage.delete();
			newlyTakenImage = null;
		}

		if (mRecordFile != null) {
			mRecordFile.delete();
			mRecordFile = null;
		}
		if (this.prevAvailablePage != null) {
			this.prevAvailablePage.setNextAvailablePage(this.nextAvailablePage);
		}
		if (this.nextAvailablePage != null) {
			this.nextAvailablePage.setPrevAvailablePage(this.prevAvailablePage);
		}
		this.prevAvailablePage = null;
		this.nextAvailablePage = null;
		this.available = false;
		if (imageBmp != null) {
			imageBmp.recycle();
			imageBmp = null;
		}
		System.gc();
	}
}
