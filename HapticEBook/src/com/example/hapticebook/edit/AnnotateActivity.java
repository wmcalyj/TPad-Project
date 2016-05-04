package com.example.hapticebook.edit;

import java.util.ArrayList;
import java.util.List;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.example.hapticebook.R;

public class AnnotateActivity extends Activity implements CvCameraViewListener {

	TPad mTpad;
	List<FrictionMapView> fricViews;
	List<Integer> annotateId;
	List<Bitmap> bitmaps;
	final static String TAG = "Annotate";

	private CameraBridgeViewBase mOpenCvCameraView;
	private Bitmap mImage;
	private Mat mMat;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.annotate);
		// Load new tpad object from TPad Implementation Library
		mTpad = new TPadImpl(this);
		prepareData();
		setAllImages();
		mMat = new Mat();
		Utils.bitmapToMat(mImage, mMat);
	}

	private void prepareData() {
		// TODO This function needs to be optimized
		fricViews = new ArrayList<FrictionMapView>(10);
		annotateId = new ArrayList<Integer>(10);
		annotateId.add(-1);
		annotateId.add(1, R.id.color1);
		annotateId.add(2, R.id.color2);
		annotateId.add(3, R.id.color3);
		annotateId.add(4, R.id.color4);
		annotateId.add(5, R.id.color5);
		annotateId.add(6, R.id.color6);
		annotateId.add(7, R.id.color7);
		annotateId.add(8, R.id.color8);
		annotateId.add(9, R.id.color9);
		bitmaps = new ArrayList<Bitmap>(10);
		bitmaps.add(null); // Pos 0
		bitmaps.add(null); // Pos 1
		bitmaps.add(null); // Pos 2
		bitmaps.add(3, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_3));

		bitmaps.add(4, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_4));

		bitmaps.add(5, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_5));

		bitmaps.add(6, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_6));

		bitmaps.add(7, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_7));

		bitmaps.add(8, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_8));

		bitmaps.add(9, BitmapFactory.decodeResource(getResources(),
				R.drawable.layer_9));

		for (int i = 0; i < 10; i++) {
			fricViews.add(null);
		}

	}

	private void setAllImages() {

		for (int i = 9; i >= 3; i--) {
			// Link friction view to .xml file
			fricViews.set(i, (FrictionMapView) findViewById(annotateId.get(i)));

			// Link local tpad object to the FrictionMapView
			fricViews.get(i).setTpad(mTpad);

			// Load in the image stored in the drawables folder

			// Set the friction data bitmap to the test image
			Bitmap tmp = bitmaps.get(i);
			fricViews.get(i).setDataBitmap(tmp);
			tmp.recycle();
			fricViews.get(i).setDisplayShowing(true);
		}
		// TODO temp solution to solve NullPointerException
		for (int i = 1; i < 3; i++) {
			fricViews.set(i, (FrictionMapView) findViewById(annotateId.get(i)));
			fricViews.get(i).setTpad(mTpad);
		}

	}

	@Override
	public Mat onCameraFrame(Mat arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCameraViewStarted(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

}
