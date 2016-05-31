package com.example.hapticebook.filterservice.impl;

import com.example.hapticebook.R;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.filterservice.FilterService;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DefaultFilterServiceImpl implements FilterService {
	private static final String TAG = "DefaultFilterService";

	@Override
	public Bitmap generateTPadFilter(Resources resources, Page currentPage) {
		// TODO Auto-generated method stub
		return generateTPadFilter(resources, currentPage, 2);
	}

	@Override
	public Bitmap generateTPadFilter(Resources resources, Page currentPage, int sampleSize) {
		// TODO Auto-generated method stub
		HapticFilterEnum filterEnum = currentPage.getHapticFilter();
		// No filter
		if (filterEnum == null || filterEnum.equals(HapticFilterEnum.NONE)) {
			return null;
		}
		if (filterEnum.isWallPaper()) {
			return generateTPadWallpaperFilter(resources, filterEnum, sampleSize);
		} else {
			String chosenFilterFilePath = currentPage.getFilterImagePath();
			return generateTpadImageFilter(resources, chosenFilterFilePath, sampleSize);
		}
	}

	private Bitmap generateTpadImageFilter(Resources resources, String chosenFilterFilePath, int sampleSize) {
		if (chosenFilterFilePath != null && chosenFilterFilePath.length() > 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(chosenFilterFilePath, options);
			// Calculate inSampleSize
			options.inSampleSize = sampleSize;
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

	private Bitmap generateTPadWallpaperFilter(Resources resources, HapticFilterEnum filterEnum, int sampleSize) {
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
		default:
			Log.w("", "Unknown wallpaper: " + filterEnum.toString());
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);
		// Calculate inSampleSize
		options.inSampleSize = sampleSize;
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(resources, resId, options);
	}

}
