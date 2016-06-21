package com.example.hapticebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.edit.EditPageActivity;
import com.example.hapticebook.filterservice.impl.FilterService;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import nxr.tpad.lib.views.FrictionMapView;

public class PageActivity extends MainActivity {

	private static final String root = Configuration.ROOT_PATH + "/hapticEBook/";
	private static final File dir = new File(root);

	private ImageView image;
	private ImageView leftFooter;
	private ImageView rightFooter;
	private Book book = getMBook();
	private Page currentPage;
	private boolean audioOn = false;
	private MediaPlayer mPlayer;
	@SuppressWarnings("unused")
	private static final String TAG = "PageActivity";

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	private Uri fileUri;
	private static AlertDialog alert;

	public static final int MEDIA_TYPE_IMAGE = 1;

	private Bitmap imageTaken;
	Bitmap filterBmp;
	Bitmap mImage;

	private FrictionMapView tpadView;
	// private ProgressBar loading;

	private long captureTime;
	OrientationEventListener mOrientationListener;
	int rotation = 0, count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page);
		mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				Log.v("Orientation", "Orientation changed to " + orientation);
				if (rotation != -1) {
					rotation += orientation;
					count++;
				}
			}
		};
		if (!dir.exists()) {
			dir.mkdirs();
		}
		image = (ImageView) findViewById(R.id.page_image);
		if (!Configuration.HAPTICDISABLED) {
			tpadView = (FrictionMapView) findViewById(R.id.page_feel_image);
			tpadView.setTpad(mTpad);
		} else {
			tpadView = (FrictionMapView) findViewById(R.id.page_feel_image);
			((RelativeLayout) tpadView.getParent()).removeView(tpadView);
		}
		if (this.isBookEmpty()) {
			showInstructionForEmptyBook();
			playInstructionForEmptyBookAudio();
			startCameraActivity();
		} else {
			Bundle b = getIntent().getExtras();
			if (b != null && b.getInt(PAGE_ACTIVITY_KEY) == PAGE_ACTIVITY_NEW_PHOTO) {
				currentPage = book.goToLastPage();
				Log.d("", "Page Activity, go to last page");
			} else {
				if (b.getBoolean(Configuration.IntentExtraValue.LandingEntry)) {
					currentPage = book.goToFirstPage();
					Log.d("", "Page Activity, go to first page");
				} else {
					currentPage = book.getCurrentPage();
					if (currentPage == null) {
						currentPage = book.goToFirstPage();
						Log.d("", "Page Activity, go to first page");
					}
				}
			}
			// currentPage = book.getCurrentPage();

			// loading = (ProgressBar) findViewById(R.id.page_loading);
			// Uri iamgeURI = currentPage.getImageUri();
			mImage = currentPage.getImageBitmap();
			image.setImageBitmap(mImage);

		}
		// Set all buttons, etc
		refresh();
		bringHeaderSetToFront();
		addHeaderButtonListeners();
		finishLoading();
		createAlertDialog();
	}

	private void playInstructionForEmptyBookAudio() {
		// TODO Auto-generated method stub
	}

	private void showInstructionForEmptyBook() {
		if (showEmptyInstruction) {
			Toast toast = Toast.makeText(this, Configuration.EMPTYBOOKINSTRUCTION, Toast.LENGTH_LONG);
			ViewGroup group = (ViewGroup) toast.getView();
			TextView messageTextView = (TextView) group.getChildAt(0);
			messageTextView.setTextSize(25);
			toast.show();
			showEmptyInstruction = false;
		}
	}

	private void setFrictionMapView() {
		if (filterBmp != null) {
			filterBmp.recycle();
		}
		filterBmp = FilterService.getTPadFilter(getResources(), currentPage, getScreenSize(this.imageTaken));
		if (filterBmp == null) {
			mTpad.turnOff();
			tpadView.setDataBitmap(super.getEmptyBitmap());
			return;
		} else {
			tpadView.setDataBitmap(filterBmp);
			if (Configuration.DEBUG) {
				image.setImageBitmap(filterBmp);
			}
		}
	}

	private void bringHeaderSetToFront() {
		RelativeLayout root = (RelativeLayout) findViewById(R.id.page_page);
		FrameLayout header = (FrameLayout) findViewById(R.id.page_header_set);
		root.bringChildToFront(header);
	}

	private void setPlayAudio() {

		if (currentPage != null && currentPage.isAudioAvailable()) {
			Log.d("", "Record file exists");
			RelativeLayout root = (RelativeLayout) findViewById(R.id.page_page);
			ImageView audio = (ImageView) findViewById(R.id.page_play_button);
			audio.setVisibility(View.VISIBLE);
			root.bringChildToFront(audio);

			audio.setClickable(true);
			audio.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					if (!audioOn) {
						((ImageView) v).setImageResource(R.drawable.play_bottom);
						audioOn = true;
						mPlayer = currentPage.startPlayingAudio();
						mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								currentPage.stopPlayingAudio(mp);
								// Have no icon to switch
								// ((ImageView)
								// v).setImageResource(R.drawable.audio);
								audioOn = false;

							}
						});
					} else {
						// Have no icon to switch
						// ((ImageView) v).setImageResource(R.drawable.audio);
						audioOn = false;
						currentPage.stopPlayingAudio(mPlayer);
					}

				}
			});
		} else {
			Log.d("", "Record file doesn't exist");
			ImageView audio = (ImageView) findViewById(R.id.page_play_button);
			audio.setVisibility(View.INVISIBLE);
			audio.setClickable(false);
		}

	}

	protected void addHeaderButtonListeners() {
		addNewPageListener();
		addDeletePageListener();
		addEditPageListener();
	}

	private void addPrevListener() {
		ImageView prev = (ImageView) findViewById(R.id.page_footer_left);
		prev.setImageResource(R.drawable.flip_left);

		prev.setClickable(true);
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disablePlaying();
				// loading.setVisibility(View.VISIBLE);
				currentPage = book.goToPrevPage();
				refresh();
			}

		});

	}

	private void addNextListener() {
		ImageView next = (ImageView) findViewById(R.id.page_footer_right);
		if (next.getVisibility() == View.VISIBLE) {
			next.setClickable(true);
			next.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					disablePlaying();
					// loading.setVisibility(View.VISIBLE);
					currentPage = book.goToNextPage();
					refresh();
				}

			});
		}
	}

	private void addNewPageListener() {
		ImageView newPage = (ImageView) findViewById(R.id.page_new_button);
		newPage.setClickable(true);
		newPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disablePlaying();
				startCameraActivity();

			}
		});
	}

	protected void startCameraActivity() {
		// create Intent to take a picture and return control to the
		// calling application
		if (mOrientationListener.canDetectOrientation() == true) {
			Log.v("Orientation", "Can detect orientation");
			mOrientationListener.enable();
		}
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri(); // create a file to save the image
		// set the image file name
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// start the image capture Intent
		captureTime = System.currentTimeMillis();
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File subFolder = new File(root + "pictures/");
		if (!subFolder.exists()) {
			subFolder.mkdirs();
		}
		return Uri.fromFile(new File(subFolder, timeStamp + ".jpg"));
	}

	public void addDeletePageListener() {
		ImageView delete = (ImageView) findViewById(R.id.page_delete_button);
		delete.setClickable(true);
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disablePlaying();
				displayConfirmationDialog();
				// book.deleteCurrentPage();
				// currentPage = book.getCurrentPage();
				// if (currentPage == null) {
				// // Book is now empty
				// goToLandingPage();
				// }
				// refresh();
			}

		});

	}

	private void createAlertDialog() {
		alert = new AlertDialog.Builder(this).setTitle("Confirmation")
				.setMessage("Are you sure you want to delete this page?").setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						book.deleteCurrentPage();
						currentPage = book.getCurrentPage();
						if (currentPage == null) {
							// Book is now empty
							goToLandingPage();
						}
						refresh();
					}
				}).setNegativeButton("Cancel", null).create();
	}

	protected void displayConfirmationDialog() {
		if (alert == null) {
			createAlertDialog();
		}
		alert.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

		// Show the dialog!
		alert.show();

		// Set the dialog to immersive
		alert.getWindow().getDecorView()
				.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		// Clear the not focusable flag from the window
		alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

	}

	public void addEditPageListener() {
		ImageView edit = (ImageView) findViewById(R.id.page_edit_button);
		edit.setClickable(true);
		edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disablePlaying();
				Page currentPage = book.getCurrentPage();
				LogService.WriteToLog(Action.EDIT, "Edit page - " + currentPage.getImageFilePath());
				Intent intent = new Intent(PageActivity.this, EditPageActivity.class);
				cleanup();
				startActivity(intent);
			}
		});

	}

	private void goToLandingPage() {
		Intent intent = new Intent(PageActivity.this, LandingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		cleanup();
		startActivity(intent);
	}

	/**
	 * This method should be called when the footer needs to be refreshed. Also,
	 * when a mode is switched, this method should be called to decide if the
	 * "Deleted" TextView should be shown
	 */
	private void refresh() {
		cleanup();
		super.hideMenu();
		if (this.currentPage == null) {
			return;
		}

		// Set the left footer and right footer (go to previous/next page)
		leftFooter = (ImageView) findViewById(R.id.page_footer_left);
		rightFooter = (ImageView) findViewById(R.id.page_footer_right);

		if (book.isCurrentPageFirstPage()) {
			// changeLeftFooterToBack();
			addBackListener();
			Log.d("", "Current page is first page");
		} else {
			leftFooter.setVisibility(View.VISIBLE);
			addPrevListener();
		}
		if (book.isCurrentPageLastPage()) {
			rightFooter.setVisibility(View.INVISIBLE);
			Log.d("", "Current page is last page");
		} else {
			rightFooter.setVisibility(View.VISIBLE);
			addNextListener();
		}

		// Set audio icon
		setPlayAudio();

		// Set new image source
		// Uri imageUri = currentPage.getImageUri();
		mImage = currentPage.getImageBitmap();
		image = (ImageView) findViewById(R.id.page_image);
		if (!Configuration.HAPTICDISABLED) {
			setFrictionMapView();
		}
		image.setImageBitmap(mImage);
	}

	private void addBackListener() {
		ImageView back = (ImageView) findViewById(R.id.page_footer_left);
		back.setClickable(true);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToLandingPage();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem student = menu.findItem(R.id.student_mode);
		MenuItem teacher = menu.findItem(R.id.teacher_mode);

		if (book.isStudentMode()) {
			student.setChecked(true);
		}
		if (book.isTeacherMode()) {
			teacher.setChecked(true);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem student = menu.findItem(R.id.student_mode);
		MenuItem teacher = menu.findItem(R.id.teacher_mode);

		if (book.isStudentMode()) {
			student.setChecked(true);
		}
		if (book.isTeacherMode()) {
			teacher.setChecked(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		boolean selected = super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.student_mode) {
			if (!currentPage.isAvailable()) {
				// Switching back to student mode and the current page is
				// unavailable, jump to the first available page
				this.currentPage = book.goToFirstPage();
				// If there is no first available page, the book is empty, go to
				// landing page
				if (this.currentPage == null) {
					goToLandingPage();
					return selected;
				}
			}
		}
		refresh();
		return selected;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d("", "New Intent");
		if (intent != null)
			setIntent(intent);
		setPlayAudio();
		refresh();
	}

	@Override
	protected void onResume() {
		super.onResume();
		int newPageActivityFlag = getIntent().getIntExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_DEFAULT);
		switch (newPageActivityFlag) {
		case PAGE_ACTIVITY_NEW_PHOTO:
			currentPage = book.goToLastPage();
			refresh();
			addHeaderButtonListeners();
			break;
		case CANCEL_SAVING_NEW_PHOTO_FROM_EDIT:
			if (currentPage == null) {
				goToLandingPage();
			}
			break;
		case PAGE_ACTIVITY_DEFAULT:
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to landing page
		finish();
		goToLandingPage();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mOrientationListener != null) {
			mOrientationListener.disable();
		}
		String imageFilePath = null;
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				try {
					Bitmap captureBmp = Media.getBitmap(getContentResolver(), fileUri);
					captureBmp = rotateImageIfNeeded(captureBmp);
					OutputStream outputStream = null;
					File imageFile = new File(fileUri.getPath());
					imageFilePath = imageFile.getAbsolutePath();
					book.createNewPage(imageFilePath);
					try {
						outputStream = new FileOutputStream(imageFile);
						captureBmp.compress(Bitmap.CompressFormat.JPEG, this.book.getCompressionRate(), outputStream);
						// picture.recycle();
						outputStream.flush();
						outputStream.close();
					} catch (IOException e) {
						Log.d("", "Failed to compress and write to file: " + e.getMessage());
					}
					// captureBmp = rotateImageIfNeeded(imageFilePath,
					// captureBmp);
					imageTaken = captureBmp;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// If this is a new page, we don't save them now, save them
				// later according to the new requirement
				// saveAction();
				// Move to last page
				// currentPage = book.goToLastPage();
				// ImageView iv = (ImageView) findViewById(R.id.page_image);
				// iv.setImageBitmap(imageTaken);
				// refresh();
				// getIntent().putExtra(PAGE_ACTIVITY_KEY,
				// PAGE_ACTIVITY_NEW_PHOTO);

				// New page, go to edit page
				Intent intent = new Intent(PageActivity.this, EditPageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				cleanup();
				finish();
				startActivity(intent);

			} else if (resultCode == RESULT_CANCELED) {
				if (getMBook().isEmpty()) {
					goToLandingPage();
				} else {
					// User cancelled the image capture (back is pressed)
					// Go to the previous page that the user was at
					refresh();
				}

			} else {
				if (getMBook().isEmpty()) {
					goToLandingPage();
				} else {
					// Image capture failed, advise user
					// Go to the previous page that the user was at
					refresh();
				}
			}
		}
	}

	private Bitmap rotateImageIfNeeded(String imgFilePath, Bitmap captureBmp) {
		// This is a simple way to check if the image is landscape
		// In the future after upgrade to Android 5.0, we may want to actually
		// use exif
		// Exif info is wrong for KitKat due to known bug from Android
		// http://stackoverflow.com/questions/8450539/images-taken-with-action-image-capture-always-returns-1-for-exifinterface-tag-or/8864367#8864367
		int rotation = -1;
		long fileSize = new File(imgFilePath).length();

		Cursor mediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE },
				MediaStore.MediaColumns.DATE_ADDED + ">=?", new String[] { String.valueOf(captureTime / 1000 - 1) },
				MediaStore.MediaColumns.DATE_ADDED + " desc");

		if (mediaCursor != null && captureTime != 0 && mediaCursor.getCount() != 0) {
			while (mediaCursor.moveToNext()) {
				long size = mediaCursor.getLong(1);
				// Extra check to make sure that we are getting the orientation
				// from the proper file
				if (size == fileSize) {
					rotation = mediaCursor.getInt(0);
					break;
				}
			}
		}
		if (rotation == -1) {
			rotation = getExifOrientationAttribute(imgFilePath);
		}
		if (rotation == 0) {
			// Check again
			return rotateImageIfNeeded(captureBmp);
		}
		Matrix m = new Matrix();
		if (rotation != 0) {
			if (rotation > 0 && rotation <= 90) {
				m.postRotate(90);
			} else if (rotation > 90 && rotation <= 180) {
				m.postRotate(180);
			} else if (rotation > 180 && rotation <= 270) {
				m.postRotate(270);
			}
		}
		Bitmap rotatedBmp = Bitmap.createBitmap(captureBmp, 0, 0, captureBmp.getWidth(), captureBmp.getHeight(), m,
				true);
		if (!rotatedBmp.equals(captureBmp)) {
			captureBmp.recycle();
			captureBmp = null;
			System.gc();
		}

		// Write rotated bmp to file
		File imageFile = new File(imgFilePath);
		try {
			OutputStream outputStream = new FileOutputStream(imageFile);
			captureBmp.compress(Bitmap.CompressFormat.JPEG, this.book.getCompressionRate(), outputStream);
			// picture.recycle();
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			Log.d("", "Failed to compress and write to file: " + e.getMessage());
			return captureBmp;
		}
		return rotatedBmp;

	}

	private int getExifOrientationAttribute(String imgFilePath) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(imgFilePath);
			return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ExifInterface.ORIENTATION_UNDEFINED;
	}

	private Bitmap rotateImageIfNeeded(Bitmap bmp) {
		// This is a simple way to check if the image is landscape
		// In the future after upgrade to Android 5.0, we may want to actually
		// use exif. Exif info is wrong for KitKat due to known bug from Android
		// http://stackoverflow.com/questions/8450539/images-taken-with-action-image-capture-always-returns-1-for-exifinterface-tag-or/8864367#8864367

		if (bmp != null && bmp.getWidth() > bmp.getHeight()) {
			Matrix matrix = new Matrix();
			double rotate = rotation / count;
			if (rotate > 0 && rotate <= 180) {
				matrix.postRotate(270);
			}

			if (rotate > 180 && rotate <= 360) {
				matrix.postRotate(90);
			}
			Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
			if (!rotatedBmp.equals(bmp)) {
				bmp.recycle();
				bmp = null;
				System.gc();
			}
			rotation = 0;
			count = 0;
			return rotatedBmp;
		} else {
			return bmp;
		}

	}

	public void saveAction() {
		if (imageTaken != null) {
			Book book = getMBook();
			Page newPage = book.createNewPage(fileUri);
			book.addNewPage(newPage);
			book.saveBook();
		}
	}

	@Override
	public void onDestroy() {
		cleanup();
		super.onDestroy();
	}

	private void cleanup() {
		if (filterBmp != null) {
			filterBmp.recycle();
			filterBmp = null;
		}
		if (mImage != null) {
			mImage.recycle();
			mImage = null;
		}
		System.gc();
	}

	private void disablePlaying() {
		if (audioOn && mPlayer != null) {
			currentPage.stopPlayingAudio(mPlayer);
			audioOn = false;
		}
	}

}
