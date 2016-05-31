package com.example.hapticebook.data.book;

import com.example.hapticebook.data.HapticFilterEnum;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

// This is the interface for Page object
public interface Page {
	/**
	 * 
	 * @param includeDeleted
	 *            Indicator of whether or not to show deleted page
	 * @return next Page object, depending on what includeDeleted is, the return
	 *         value can be a deleted page or a undeleted page
	 */
	public Page getNextPage(boolean includeDeleted);

	/**
	 * 
	 * @param includeDeleted
	 *            Indicator of whether or not to show deleted page
	 * @return previous Page object, depending on what includeDeleted is, the
	 *         return value can be a deleted page or a undeleted page
	 */
	public Page getPrevPage(boolean includeDeleted);

	/**
	 * delete current page and maintain status of previous page and next page
	 * accordingly
	 */
	public void delete();

	/**
	 * 
	 * @param prevPage
	 *            set previous page
	 */
	public void setPrevPage(Page prevPage);

	/**
	 * 
	 * @param prevAvailablePage
	 *            set previous existing page
	 */

	public void setPrevAvailablePage(Page prevAvailablePage);

	/**
	 * 
	 * @param nextPage
	 *            set next page (including deleted), used when adding a new page
	 */
	public void setNextPage(Page nextPage);

	/**
	 * 
	 * @param nextAvailablePage
	 *            set next existing page
	 */
	public void setNextAvailablePage(Page nextAvailablePage);

	/**
	 * 
	 * @return true if there is an audio attached to the current page,
	 *         otherwise, return false
	 */
	public boolean isAudioAvailable();

	/**
	 * 
	 * @return <code>MediaPlayer</code> and start playing audio. NULL will be
	 *         returned if there is no audio to play
	 */
	public MediaPlayer startPlayingAudio();

	/**
	 * 
	 * @param mediaPlayer
	 *            returned from <code>startPlayingAudio()</code> and stop
	 *            playing audio
	 */
	public void stopPlayingAudio(MediaPlayer mediaPlayer);

	/**
	 * 
	 * @return true if the current page is available (not deleted), otherwise,
	 *         return false
	 */
	boolean isAvailable();

	MediaRecorder startRecording();

	void stopRecording(MediaRecorder mRecorder);

	public Bitmap getImageBitmap();

	HapticFilterEnum getHapticFilter();

	void setHapticFilter(HapticFilterEnum hapticFilter);

	void saveAudioFile();

	void cancelAudioFile();

	@Override
	boolean equals(Object object);

	@Override
	int hashCode();

	void saveHapticFilter(String filterFilePath);

	void cancelHapticFilter();

	public String getImageFilePath();

	String getFilterImagePath();

	public boolean isUsingWallPaper();

}
