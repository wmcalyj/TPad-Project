package com.example.hapticebook.edit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;

public class FilterActivity extends MainActivity {

	TPad mTpad;
	final static String TAG = "Filter";

	private Bitmap mImage;
	private Page currentPage;
	private Mat mGray;
	private Mat mRgba;
	private Mat mIntermediateMat;
	private ImageView mView;
	private FrictionMapView mFrictionMapView;
	private static int current = 0;
	private List<HapticFilterEnum> filters;
	private TextView textView;

	private Bitmap filter;
	private File filterFile;
	String filterFilePath;
	private ProgressBar loading;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				mGray = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC1);
				mIntermediateMat = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC4);
				mRgba = new Mat(mImage.getHeight(), mImage.getWidth(), CvType.CV_8UC4);
				setSrcMat();
				addFilters();
				HapticFilterEnum filterEnum = currentPage.getHapticFilter();
				if (filterEnum == null) {
					filter = null;
					mFrictionMapView.setDataBitmap(getEmptyBitmap());
					current = 0;
					mTpad.turnOff();
				} else {
					applyFilter(filterEnum);
					for (int i = 1; i < filters.size(); i++) {
						if (filters.get(i) == currentPage.getHapticFilter()) {
							current = i;
							break;
						}
					}
				}
				textView.setText(String.valueOf(current));
				setView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}

	};

	/**
	 * Add different filters to filter list
	 */
	private void addFilters() {
		if (filters == null) {
			filters = new ArrayList<HapticFilterEnum>();
		}
		filters.add(HapticFilterEnum.NONE); // Pos 0
		filters.add(HapticFilterEnum.ORIGINAL); // Pos 1
		filters.add(HapticFilterEnum.WOODCUT); // Pos 2
		filters.add(HapticFilterEnum.CANNY); // Pos 3
		filters.add(HapticFilterEnum.NOISE);// Pos 4
		filters.add(HapticFilterEnum.WALLPAPER1);
		filters.add(HapticFilterEnum.WALLPAPER2);
		filters.add(HapticFilterEnum.WALLPAPER3);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		current = 0;
		setContentView(R.layout.filter);
		textView = (TextView) findViewById(R.id.filter_id);
		textView.setText(String.valueOf(current));
		// Load new tpad object from TPad Implementation Library
		mTpad = new TPadImpl(this);
		addToolSet();
		getCurrentPageAndImage();
		filterFilePath = currentPage.getImageFilePath() + "_filter.png";
		filterFile = new File(filterFilePath);
		loading = (ProgressBar) findViewById(R.id.filter_loading);
		finishLoading();
		loading.bringToFront();
	}

	private void addToolSet() {
		addLeftListener();
		addRightListener();
		addSaveListener();
		addCancelListener();

	}

	private void addCancelListener() {
		ImageView cancel = (ImageView) findViewById(R.id.filter_cancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.disableAfterClick(v);
				Intent intent = new Intent(FilterActivity.this, EditPageActivity.class);
				if (filter != null) {
					intent.putExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath",
							filterFile.getAbsolutePath());
				} else {
					intent.removeExtra("ChosenFilterFilePath");
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

	private void addSaveListener() {
		ImageView save = (ImageView) findViewById(R.id.filter_save);
		save.setClickable(true);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// loading.setVisibility(View.VISIBLE);
				MainActivity.disableAfterClick(v);
				HapticFilterEnum currentFilter = filters.get(current);
				currentPage.setHapticFilter(currentFilter);
				Intent intent = new Intent(FilterActivity.this, EditPageActivity.class);
				if (filter != null) {
					if (!currentFilter.isWallPaper()) {
						OutputStream outputStream;
						try {
							outputStream = new FileOutputStream(filterFile);
							filter.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
							outputStream.flush();
							outputStream.close();
						} catch (IOException e) {
							Log.d("", "Failed to compress and write to filter file: " + e.getMessage());
						}
						intent.putExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath",
								filterFile.getAbsolutePath());
					} else {
						intent.removeExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath");
						intent.putExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterEnum", currentFilter);
					}
				} else {
					intent.removeExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath");
				}

				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

	private void addLeftListener() {
		ImageView left = (ImageView) findViewById(R.id.filter_prev);
		left.setClickable(true);
		left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loading.setVisibility(View.VISIBLE);
				ImageView left = (ImageView) findViewById(R.id.filter_prev);
				left.setClickable(false);
				loading.setVisibility(View.VISIBLE);
				current--;
				if (current < 0) {
					current = filters.size() - 1;
				}
				setFilterId();
				applyFilter(filters.get(current));
				if (filter != null) {
					mFrictionMapView.setDataBitmap(filter);
					if (Configuration.DEBUG) {
						mView.setImageBitmap(filter);
					}
				} else {
					mFrictionMapView.setDataBitmap(getEmptyBitmap());
					mTpad.turnOff();
				}
				left.setClickable(true);
				loading.setVisibility(View.GONE);
			}
		});
	}

	private void addRightListener() {
		ImageView right = (ImageView) findViewById(R.id.filter_next);
		right.setClickable(true);
		right.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loading.setVisibility(View.VISIBLE);
				ImageView right = (ImageView) findViewById(R.id.filter_next);
				right.setClickable(false);

				current++;
				if (current >= filters.size()) {
					current = 0;
				}

				setFilterId();
				applyFilter(filters.get(current));
				// if (filters.get(current) != null) {
				if (filter != null) {
					// mFrictionMapView.setDataBitmap(filters.get(current));
					mFrictionMapView.setDataBitmap(filter);
					if (Configuration.DEBUG) {
						// mView.setImageBitmap(filters.get(current));
						mView.setImageBitmap(filter);
					}
				} else {
					mFrictionMapView.setDataBitmap(getEmptyBitmap());
					mTpad.turnOff();
				}
				right.setClickable(true);
				loading.setVisibility(View.GONE);
			}
		});
	}

	protected void setFilterId() {
		textView.setText(String.valueOf(current));
	}

	private void setWoodcutFilterBitmap() {
		int tempOpt = 41;
		if (mGray != null && mRgba != null) {
			// First, convert src to grey
			Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
			Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, tempOpt,
					0);
			Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
			Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
			Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
			filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mRgba, filter);

		} else {
			Log.w(TAG, "mSrcMat is null OR mDstMat is null");
			filter = null;
		}
		setSrcMat();
	}

	private void setNoiseFilterBitmap() {
		if (mGray != null && mRgba != null) {
			int tempOpt = 5;
			Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
			Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 15);
			Imgproc.Sobel(mGray, mGray, -1, 1, 1, 3, 1, 0);
			Imgproc.equalizeHist(mGray, mGray);
			for (int i = 0; i < tempOpt; i++) {
				Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 5);
			}
			Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
			filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mRgba, filter);
		} else {
			Log.w(TAG, "mSrcMat is null OR mDstMat is null");
			filter = null;
		}
		setSrcMat();

	}

	private void setCannyFilterBitmap() {
		// input frame has gray scale format
		// Mat kernal = Mat.ones(5, 5, CvType.CV_8UC1);
		// byte[] kbytes = new byte[]{0,0,1,0,0, 0,1,1,1,0, 1,1,1,1,1,
		// 0,1,1,1,0, 0,0,1,0,0};
		Mat kernal = Mat.ones(3, 3, CvType.CV_8UC1);
		byte[] kbytes = new byte[] { 0, 1, 0, 1, 1, 1, 0, 1, 0 };

		kernal.put(0, 0, kbytes);

		// Convert orignal image to gray
		if (mGray != null && mRgba != null) {
			Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY);
			int tempOpt = 3;

			Imgproc.Canny(mGray, mIntermediateMat, 80, 100);
			Imgproc.dilate(mIntermediateMat, mIntermediateMat, kernal);
			Imgproc.GaussianBlur(mIntermediateMat, mIntermediateMat, new Size(tempOpt, tempOpt), 5);
			Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
			filter = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mRgba, filter);
		} else {
			Log.w(TAG, "mSrcMat is null OR mDstMat is null");
			filter = null;
		}
		setSrcMat();

	}

	private void applyFilter(HapticFilterEnum hapticFilterEnum) {
		if (hapticFilterEnum == null) {
			// do nothing
			filter = null;
			return;
		}
		if (filter != null) {
			filter.recycle();
		}
		switch (hapticFilterEnum) {
		case NONE:
			// Default, no filter
			filter = null;
			return;
		case ORIGINAL:
			// Original bitmap
			filter = mImage.copy(Bitmap.Config.ARGB_8888, true);
			return;
		case WOODCUT:
			// Woodcut
			setWoodcutFilterBitmap();
			return;
		case CANNY:
			// Canny, line filter
			setCannyFilterBitmap();
			return;
		case NOISE:
			// Grainy
			setNoiseFilterBitmap();
			return;
		case WALLPAPER1:
			setWallPaperFilterBitmap(1);
			return;
		case WALLPAPER2:
			setWallPaperFilterBitmap(2);
			return;
		case WALLPAPER3:
			setWallPaperFilterBitmap(3);
			return;
		default:
			Log.d(TAG, "cannot recognize the filter, use default - " + hapticFilterEnum);
			filter = this.mImage;

		}
	}

	private void setWallPaperFilterBitmap(int i) {
		int resId;
		switch (i) {
		case 1:
			resId = R.drawable.wallpaper1_j;
			break;
		case 2:
			resId = R.drawable.wallpaper2_j;
			break;
		case 3:
			resId = R.drawable.wallpaper3_j;
			break;
		default:
			Log.w(TAG, "Unknown wallpaper: " + i);
			filter = this.mImage;
			return;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resId, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		filter = BitmapFactory.decodeResource(getResources(), resId, options);
	}

	private void setView() {
		mView = (ImageView) findViewById(R.id.filter_shown_image);
		mView.setImageBitmap(mImage);
		mFrictionMapView = (FrictionMapView) findViewById(R.id.filter_image);
		mFrictionMapView.setTpad(mTpad);

		if (Configuration.DEBUG) {
			if (filter != null) {
				mView.setImageBitmap(filter);
			}
		}
		if (filter != null) {
			mFrictionMapView.setDataBitmap(filter);
		}
	}

	private void getCurrentPageAndImage() {
		this.currentPage = getMBook().getCurrentPage();

		if (currentPage != null && currentPage.getImageBitmap() != null) {
			mImage = currentPage.getImageBitmap();
		} else {
			Log.d(TAG, "currentPage is null or bitmap is null");
		}
	}

	private void setSrcMat() {
		Utils.bitmapToMat(mImage, mRgba);
	}

	@Override
	public void onDestroy() {
		if (mGray != null) {
			mGray.release();
		}
		if (mRgba != null) {
			mRgba.release();
		}
		if (mIntermediateMat != null) {
			mIntermediateMat.release();
		}
		if (filter != null) {
			filter.recycle();
		}
		// if (woodcut != null) {
		// woodcut.recycle();
		// }
		// if (canny != null) {
		// canny.recycle();
		// }
		// if (noise != null) {
		// noise.recycle();
		// }
		mTpad.disconnectTPad();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		if (mGray != null) {
			mGray.release();
		}
		if (mRgba != null) {
			mRgba.release();
		}
		if (mIntermediateMat != null) {
			mIntermediateMat.release();
		}
		if (filter != null) {
			filter.recycle();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to edit page
		finish();
		goToEditPage();
	}

	private void goToEditPage() {
		Intent intent = new Intent(FilterActivity.this, EditPageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}
}
