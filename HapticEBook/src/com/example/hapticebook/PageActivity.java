package com.example.hapticebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.edit.EditPageActivity;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
	private static final String TAG = "PageActivity";

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	private Uri fileUri;

	public static final int MEDIA_TYPE_IMAGE = 1;

	private Bitmap imageTaken;
	Bitmap filterBmp;

	private FrictionMapView tpadView;
	// private ProgressBar loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		image = (ImageView) findViewById(R.id.page_image);
		tpadView = (FrictionMapView) findViewById(R.id.page_feel_image);
		tpadView.setTpad(mTpad);
		if (this.isBookEmpty()) {
			// If book is empty, go to camera
			// create Intent to take a picture and return control to the
			// calling application
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			// create a file to save the image
			fileUri = getOutputMediaFileUri();
			// tell camera to save iamge to the given fileUri
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			// start the image capture Intent
			// Result is in onActivityResult function
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		} else {
			// Else, go to the first page or last page,depending on the flag
			Bundle b = getIntent().getExtras();
			if (b != null && b.getInt(PAGE_ACTIVITY_KEY) == PAGE_ACTIVITY_NEW_PHOTO) {
				currentPage = book.goToLastPage();
				Log.d("", "Page Activity, go to last page");
			} else {
				currentPage = book.goToFirstPage();
				Log.d("", "Page Activity, go to first page");
			}

			// loading = (ProgressBar) findViewById(R.id.page_loading);
			// Uri iamgeURI = currentPage.getImageUri();
			Bitmap imageBmp = currentPage.getImageBitmap();
			image.setImageBitmap(imageBmp);

		}
		// Set all buttons, etc
		refresh();
		bringHeaderSetToFront();
		addHeaderButtonListeners();
		finishLoading();
	}

	private void setFrictionMapView() {
		if (filterBmp != null) {
			filterBmp.recycle();
		}
		if (currentPage.getHapticFilter() == null || currentPage.getHapticFilter().equals(HapticFilterEnum.NONE)) {
			mTpad.turnOff();
			tpadView.setDataBitmap(super.getEmptyBitmap());
			return;
		} else {
			if (currentPage.isUsingWallPaper()) {
				filterBmp = setWallPaperFilterBitmap(currentPage.getHapticFilter());

			} else {
				String chosenFilterFilePath = currentPage.getFilterImagePath();
				if (chosenFilterFilePath != null) {
					filterBmp = BitmapFactory.decodeFile(chosenFilterFilePath);
				}
			}
			if (filterBmp == null) {
				return;
			}
			tpadView.setDataBitmap(filterBmp);
			if (Configuration.DEBUG) {
				image.setImageBitmap(filterBmp);
			}
		}
	}

	private Bitmap setWallPaperFilterBitmap(HapticFilterEnum filter) {
		int resId;
		switch (filter) {
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
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resId, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(getResources(), resId, options);
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
					// TODO Auto-generated method stub
					if (!audioOn) {
						((ImageView) v).setImageResource(R.drawable.audio_blue);
						audioOn = true;
						mPlayer = currentPage.startPlayingAudio();
						mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								currentPage.stopPlayingAudio(mp);
								((ImageView) v).setImageResource(R.drawable.audio);
								audioOn = false;

							}
						});
					} else {
						((ImageView) v).setImageResource(R.drawable.audio);
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
		prev.setImageResource(R.drawable.corner_left);

		prev.setClickable(true);
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
				// create Intent to take a picture and return control to the
				// calling application
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// Intent intent = new Intent(PageActivity.this,
				// NewPageActivity.class);
				// startActivity(intent);

				fileUri = getOutputMediaFileUri(); // create a
													// file
				// to save the image
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the
																	// image
				// file
				// // name

				// start the image capture Intent
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

			}
		});
	}

	/** Create a file Uri for saving an image or video */
	private Uri getOutputMediaFileUri() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		return Uri.fromFile(new File(root + "pictures/", timeStamp + ".jpg"));
	}

	public void addDeletePageListener() {
		ImageView delete = (ImageView) findViewById(R.id.page_delete_button);
		delete.setClickable(true);
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				book.deleteCurrentPage();
				currentPage = book.getCurrentPage();
				if (currentPage == null) {
					// Book is now empty
					goToLandingPage();
				}
				refresh();
			}

		});

	}

	public void addEditPageListener() {
		ImageView edit = (ImageView) findViewById(R.id.page_edit_button);
		edit.setClickable(true);
		edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Page currentPage = book.getCurrentPage();
				LogService.WriteToLog(Action.EDIT, "Edit page - " + currentPage.getImageFilePath());
				Intent intent = new Intent(PageActivity.this, EditPageActivity.class);
				// intent.putExtra("currentPage", (Serializable) currentPage);
				startActivity(intent);
			}
		});

	}

	private void goToLandingPage() {
		Intent intent = new Intent(PageActivity.this, LandingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	/**
	 * This method should be called when the footer needs to be refreshed. Also,
	 * when a mode is switched, this method should be called to decide if the
	 * "Deleted" TextView should be shown
	 */
	private void refresh() {
		super.hideMenu();
		if (this.currentPage == null) {
			return;
		}
		// Set the visibility of TextView (deleted)
		if (!this.currentPage.isAvailable()) {
			TextView imageDeleted = (TextView) findViewById(R.id.deleted);
			imageDeleted.setVisibility(View.VISIBLE);
		} else {
			TextView imageDeleted = (TextView) findViewById(R.id.deleted);
			imageDeleted.setVisibility(View.INVISIBLE);
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
		Bitmap imageBmp = currentPage.getImageBitmap();
		image = (ImageView) findViewById(R.id.page_image);
		setFrictionMapView();
		image.setImageBitmap(imageBmp);
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
		super.hideMenu();
		super.onResume();
		int newPageActivityFlag = getIntent().getIntExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_DEFAULT);
		switch (newPageActivityFlag) {

		case PAGE_ACTIVITY_NEW_PHOTO:
			currentPage = book.goToLastPage();
			refresh();
			addHeaderButtonListeners();
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
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				try {
					Bitmap captureBmp = Media.getBitmap(getContentResolver(), fileUri);
					OutputStream outputStream = null;
					File imageFile = new File(fileUri.getPath());
					try {
						outputStream = new FileOutputStream(imageFile);
						captureBmp.compress(Bitmap.CompressFormat.JPEG, this.book.getCompressionRate(), outputStream);
						// picture.recycle();
						outputStream.flush();
						outputStream.close();
					} catch (IOException e) {
						Log.d("", "Failed to compress and write to file: " + e.getMessage());
					}
					imageTaken = captureBmp;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Save new page
				saveAction();
				// Move to last page
				currentPage = book.goToLastPage();
				// ImageView iv = (ImageView) findViewById(R.id.page_image);
				// iv.setImageBitmap(imageTaken);
				refresh();
				getIntent().putExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture (back is pressed)
				// Go to the previous page that the user was at
				refresh();

			} else {
				// Image capture failed, advise user
				// Go to the previous page that the user was at
				refresh();
			}
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
	public void onPause() {
		super.onPause();
		if (filterBmp != null) {
			filterBmp.recycle();
		}
	}

	@Override
	public void onDestroy() {
		if (filterBmp != null) {
			filterBmp.recycle();
		}
		super.onDestroy();
	}

}
