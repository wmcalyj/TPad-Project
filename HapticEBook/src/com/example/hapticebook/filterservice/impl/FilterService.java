package com.example.hapticebook.filterservice.impl;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import com.example.hapticebook.R;
import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

public class FilterService {
	private static final String TAG = "DefaultFilterService";
	private static List<HapticFilterEnum> filters;

	/**
	 * 
	 * @param resources
	 *            activity resources
	 * @param currentPage
	 *            current page
	 * @param sampleSize
	 *            sampleSize to lower the resolution
	 * @return This method will return a bitmap in given samplesize based on the
	 *         given filter path If it is a wallpaper, it will also return the
	 *         bitmap based on the wall paper To generate new filter based on
	 *         the image, please use generateTPadFilter
	 */
	public static Bitmap getTPadFilter(Resources resources, Page currentPage, Point size) {
		// TODO Auto-generated method stub
		HapticFilterEnum filterEnum = currentPage.getHapticFilter();
		// No filter
		if (filterEnum == null || filterEnum.equals(HapticFilterEnum.NONE)) {
			return null;
		}
		if (filterEnum.isWallPaper()) {
			return getTPadWallpaperFilter(resources, filterEnum, size.x, size.y);
		} else {
			String chosenFilterFilePath = currentPage.getFilterImagePath();
			return getTpadImageFilter(resources, chosenFilterFilePath, size.x, size.y);
		}
	}

	public static Bitmap generteTPadFilter(Resources resources, HapticFilterEnum filterEnum, Bitmap original, Mat mGray,
			Mat mRgba, Mat mIntermediateMat, Point size) {
		if (filterEnum == null || filterEnum.equals(HapticFilterEnum.NONE)) {
			return null;
		}
		if (filterEnum.isWallPaper()) {
			return getTPadWallpaperFilter(resources, filterEnum, size.x, size.y);
		} else {
			return generateTPadFilterImageBased(filterEnum, original, mGray, mRgba, mIntermediateMat);
		}
	}

	private static Bitmap generateTPadFilterImageBased(HapticFilterEnum filterEnum, Bitmap original, Mat mGray,
			Mat mRgba, Mat mIntermediateMat) {
		Bitmap filterImage = null;
		switch (filterEnum) {
		case CANNY:
			filterImage = FilterGenerator.getCannyFilterBitmap(original, mGray, mRgba, mIntermediateMat);
			break;
		// case NOISE:
		// filterImage = FilterGenerator.getNoiseFilterBitmap(original, mGray,
		// mRgba, mIntermediateMat);
		// break;
		case ORIGINAL:
			filterImage = original.copy(Config.ARGB_8888, true);
			break;
		case WOODCUT:
			filterImage = FilterGenerator.generateWoodcutFilterBitmap(original, mGray, mRgba, mIntermediateMat);
			break;
		default:
			Log.w(TAG, "uncaught filter type: " + filterEnum.toString());
			break;
		}
		return filterImage;

	}

	private static Bitmap getTpadImageFilter(Resources resources, String chosenFilterFilePath, int reqWidth,
			int reqHeight) {
		if (chosenFilterFilePath != null && chosenFilterFilePath.length() > 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(chosenFilterFilePath, options);
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap filterBmp = BitmapFactory.decodeFile(chosenFilterFilePath, options);
			if (filterBmp == null) {
				return null;
			} else {
				return filterBmp;
			}
		} else {
			return null;
		}
	}

	private static Bitmap getTPadWallpaperFilter(Resources resources, HapticFilterEnum filterEnum, int reqWidth,
			int reqHeight) {
		int resId;
		switch (filterEnum) {
		case WALLPAPER1:
			resId = R.drawable.wallpaper1_j;
			break;
		case WALLPAPER2:
			resId = R.drawable.wallpaper2_j;
			break;
		case WALLPAPER3:
			resId = R.drawable.wallpaper3_j;
			break;
		case WALLPAPER4:
			resId = R.drawable.wallpaper4;
			break;
		default:
			Log.w("", "Unknown wallpaper: " + filterEnum.toString());
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(resources, resId, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if (Configuration.USE_GIVEN_INSAMPLESIZE) {
			return Configuration.INSAMPLESIZE;
		}
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/**
	 * 
	 * @return a list of existing filters
	 */
	public static List<HapticFilterEnum> getAllFilterTypes() {
		if (filters == null) {
			filters = new ArrayList<HapticFilterEnum>(8);
			filters.add(HapticFilterEnum.NONE.getFilterIndex(), HapticFilterEnum.NONE); // 0
			filters.add(HapticFilterEnum.ORIGINAL.getFilterIndex(), HapticFilterEnum.ORIGINAL); // 1
			filters.add(HapticFilterEnum.WOODCUT.getFilterIndex(), HapticFilterEnum.WOODCUT);// 2
			filters.add(HapticFilterEnum.CANNY.getFilterIndex(), HapticFilterEnum.CANNY); // 3
			// filters.add(HapticFilterEnum.NOISE.getFilterIndex(),
			// HapticFilterEnum.NOISE);// 4
			filters.add(HapticFilterEnum.WALLPAPER1.getFilterIndex(), HapticFilterEnum.WALLPAPER1);
			filters.add(HapticFilterEnum.WALLPAPER2.getFilterIndex(), HapticFilterEnum.WALLPAPER2);
			filters.add(HapticFilterEnum.WALLPAPER3.getFilterIndex(), HapticFilterEnum.WALLPAPER3);
			filters.add(HapticFilterEnum.WALLPAPER4.getFilterIndex(), HapticFilterEnum.WALLPAPER4);
		}

		return filters;
	}
}
