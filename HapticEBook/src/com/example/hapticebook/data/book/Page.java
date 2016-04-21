package com.example.hapticebook.data.book;

import android.graphics.Bitmap;
import android.media.MediaPlayer;

import com.example.hapticebook.data.book.impl.PageImage;

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
	 * @param image
	 *            the image attached to the current page
	 */
	public void setImage(Bitmap image);

	public void setImage(PageImage pageImage);

	public PageImage getImage();

	public Bitmap getBitmapImage();

	public boolean isAudioAvailable();

	public MediaPlayer startPlayingAudio();

	public void stopPlayingAudio(MediaPlayer mediaPlayer);

}
