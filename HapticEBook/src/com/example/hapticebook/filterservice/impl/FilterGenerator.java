package com.example.hapticebook.filterservice.impl;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.example.hapticebook.config.Configuration;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * This class is used to generate all image based filters. All methods should
 * have mGray, mRgba and mIntermediateMat although maybe not all of them will be
 * used. Also, a pre-check should be called before anything is done.
 * 
 * @author mengchaowang
 *
 */
public class FilterGenerator {
	private static final String TAG = "FilterGenerato";

	static Bitmap generateWoodcutFilterBitmap(Bitmap mImage, Mat mGray, Mat mRgba, Mat mIntermediateMat) {
		if (!isEverythinOK(mImage, mGray, mRgba, mIntermediateMat)) {
			return null;
		}
		int tempOpt = Configuration.FilterValue.WOODCUT;

		// First, convert src to grey
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, tempOpt, 0);
		Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
		Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
		Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
		Bitmap filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mRgba, filter);
		return filter;

	}

	static Bitmap getCannyFilterBitmap(Bitmap mImage, Mat mGray, Mat mRgba, Mat mIntermediateMat) {
		if (!isEverythinOK(mImage, mGray, mRgba, mIntermediateMat)) {
			return null;
		}
		// input frame has gray scale format
		// Mat kernal = Mat.ones(5, 5, CvType.CV_8UC1);
		// byte[] kbytes = new byte[]{0,0,1,0,0, 0,1,1,1,0, 1,1,1,1,1,
		// 0,1,1,1,0, 0,0,1,0,0};
		Mat kernal = Mat.ones(3, 3, CvType.CV_8UC1);
		byte[] kbytes = new byte[] { 0, 1, 0, 1, 1, 1, 0, 1, 0 };

		kernal.put(0, 0, kbytes);

		// Convert orignal image to gray
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
		int tempOpt = Configuration.FilterValue.CANNY;

		Imgproc.Canny(mGray, mIntermediateMat, 80, 100);
		Imgproc.dilate(mIntermediateMat, mIntermediateMat, kernal);
		Imgproc.GaussianBlur(mIntermediateMat, mIntermediateMat, new Size(tempOpt, tempOpt), 5);
		Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
		Bitmap filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mRgba, filter);
		return filter;
	}

	@SuppressWarnings("unused")
	@Deprecated
	// We no longer want to use noise filter
	private static Bitmap getNoiseFilterBitmap(Bitmap mImage, Mat mGray, Mat mRgba, Mat mIntermediateMat) {
		if (!isEverythinOK(mImage, mGray, mRgba, mIntermediateMat)) {
			return null;
		}
		int tempOpt = Configuration.FilterValue.NOISE;
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
		Imgproc.Sobel(mGray, mGray, -1, 1, 1, 3, 1, 0);
		Imgproc.equalizeHist(mGray, mGray);
		for (int i = 0; i < tempOpt; i++) {
			Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 5);
		}
		Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
		Bitmap filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mRgba, filter);
		return filter;
	}

	private static boolean isEverythinOK(Bitmap mImage, Mat mGray, Mat mRgba, Mat mIntermediateMat) {
		if (mGray == null || mRgba == null || mIntermediateMat == null) {
			Log.w(TAG, "mGray or mRgba or mIntermediateMat is not ready yet");
			return false;
		}
		if (mImage != null && !mImage.isRecycled()) {
			Utils.bitmapToMat(mImage, mRgba);
			return true;
		} else {
			return false;
		}
	}
}
