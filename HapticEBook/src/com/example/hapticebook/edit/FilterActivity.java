package com.example.hapticebook.edit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.PageActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.filterservice.impl.FilterService;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
	private List<HapticFilterEnum> filters;
	private TextView textView;

	private Bitmap filter;
	private File filterFile;
	String filterFilePath;
	private int current;
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
				filters = FilterService.getAllFilterTypes();
				HapticFilterEnum filterEnum = currentPage.getHapticFilter();
				if (filterEnum == null) {
					filter = null;
					mFrictionMapView.setDataBitmap(getEmptyBitmap());
					mView.setImageBitmap(mImage);
					current = 0;
					mTpad.turnOff();
				} else {
					applyFilter(filterEnum);
					current = filterEnum.getFilterIndex();
				}
				textView.setText(getLabelString(current));
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

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	protected String getLabelString(int filterNumber) {
		if (filterNumber == 0) {
			return "No Texture";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Texture ").append(filterNumber);
			return sb.toString();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		current = 0;
		setContentView(R.layout.filter);
		textView = (TextView) findViewById(R.id.filter_id);
		textView.setText(getLabelString(current));
		// Load new tpad object from TPad Implementation Library
		mTpad = new TPadImpl(this);
		addToolSet();
		getCurrentPageAndImage();
		filterFilePath = currentPage.getImageFilePath() + "_filter.png";
		filterFile = new File(filterFilePath);
		loading = (ProgressBar) findViewById(R.id.filter_loading);
		finishLoading();
		loading.bringToFront();
		if (isNewlyTakenImage(currentPage)) {
			Toast.makeText(this, "Which texture goes with this photo?", Toast.LENGTH_LONG).show();
		}
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
				Intent intent;
				if (isNewlyTakenImage(currentPage)) {
					getMBook().cancelSavingNewImage();
					intent = new Intent(FilterActivity.this, PageActivity.class);
					intent.putExtra(PAGE_ACTIVITY_KEY, CANCEL_SAVING_NEW_PHOTO_FROM_EDIT);
					LogService.WriteToLog(Action.CANCEL_EDIT, "Cancel saving newly taken image, go back to pages");
				} else {
					if (Configuration.RECORDINGENABLED) {
						intent = new Intent(FilterActivity.this, EditPageActivity.class);
					} else {
						intent = new Intent(FilterActivity.this, PageActivity.class);
					}
					if (filter != null) {
						intent.putExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath",
								filterFile.getAbsolutePath());
					} else {
						intent.removeExtra("ChosenFilterFilePath");
					}
				}

				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				cleanup();
				startActivity(intent);
			}
		});
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (intent != null) {
			setIntent(intent);
		}
	}

	private void cleanup() {
		if (filter != null) {
			filter.recycle();
			filter = null;
		}
		if (mImage != null) {
			mImage.recycle();
			mImage = null;
		}
		if (mGray != null) {
			mGray.release();
			mGray = null;
		}
		if (mRgba != null) {
			mRgba.release();
			mRgba = null;
		}
		if (mIntermediateMat != null) {
			mIntermediateMat.release();
			mIntermediateMat = null;
		}
		mLoaderCallback = null;
		filters = null;
		System.gc();
		finish();
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
				currentPage.saveHapticFilter(filterFilePath);
				Intent intent;
				if (Configuration.RECORDINGENABLED) {
					intent = new Intent(FilterActivity.this, EditPageActivity.class);
				} else {
					intent = new Intent(FilterActivity.this, PageActivity.class);
				}
				if (isNewlyTakenImage(currentPage)) {
					getMBook().addNewPage(currentPage);
					// Newly Taken Image, go back to browse page if recording
					// disabled
					// If recording is enabled, go to edit page
					intent.putExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
					intent.putExtra(FILTER_ACTIVITY_NEW_IMAGE_SAVED, true);
					LogService.WriteToLog(Action.SAVE_EDIT, "Save newly taken image " + currentPage.getImageFilePath());
				}
				getMBook().saveBook();
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
				cleanup();
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
					mView.setImageBitmap(mImage);
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
					// Reset image to original one
					mView.setImageBitmap(mImage);
					mFrictionMapView.setDataBitmap(getEmptyBitmap());
					mTpad.turnOff();
				}
				right.setClickable(true);
				loading.setVisibility(View.GONE);
			}
		});
	}

	protected void setFilterId() {
		textView.setText(getLabelString(current));
	}

	private void applyFilter(HapticFilterEnum hapticFilterEnum) {
		Point size = getScreenSize(mImage);
		if (filter != null) {
			filter.recycle();
			filter = null;
			System.gc();
		}
		filter = FilterService.generteTPadFilter(getResources(), hapticFilterEnum, mImage, mGray, mRgba,
				mIntermediateMat, size);
		resizeFilter(size);
	}

	private void resizeFilter(Point size) {
		if (filter == null) {
			return;
		}
		Bitmap tmp = filter;
		filter = Bitmap.createScaledBitmap(tmp, size.x, size.y, true);
		if (filter != tmp && tmp != mImage) {
			tmp.recycle();
		}
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
		cleanup();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to edit page
		finish();
		goToEditPage();
	}

	private void goToEditPage() {
		Intent intent = new Intent(FilterActivity.this, EditPageActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}
}
