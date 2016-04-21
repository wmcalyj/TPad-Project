package com.example.hapticebook.newpage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.PageActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.data.book.impl.Book;
import com.example.hapticebook.data.book.impl.PageImpl;
import com.example.hapticebook.newpage.camera.CameraPreview;

public class NewPageActivity extends MainActivity {

	private Camera mCamera;
	private CameraPreview mPreview;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	private Uri fileUri;
	private File imageFile;

	public static final int MEDIA_TYPE_IMAGE = 1;

	private Bitmap imageTaken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_page);
		// setCameraPreview();

		// TODO improve image quality

		// create Intent to take a picture and return control to the calling
		// application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		addSaveListener();
		addCancelListener();

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file
		// to
		// // save the image
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image
		// file
		// // name

		// start the image capture Intent
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".png");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Image captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Image saved to:\n" + data.getData(),
						Toast.LENGTH_LONG).show();
				imageTaken = (Bitmap) data.getExtras().get("data");
				ImageView iv = (ImageView) findViewById(R.id.image_taken);
				iv.setImageBitmap(imageTaken);
				// imageFile = new File(data.getData().getPath());

				saveAction();

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
				cancelAction();
			} else {
				// Image capture failed, advise user
				cancelAction();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////

	public void setCameraPreview() {
		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		// FrameLayout preview = (FrameLayout)
		// findViewById(R.id.camera_preview);
		// preview.addView(mPreview);
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void addSaveListener() {
		ImageView save = (ImageView) findViewById(R.id.new_page_save);
		save.setClickable(true);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				saveAction();

			}
		});
	}

	public void addCancelListener() {
		ImageView cancel = (ImageView) findViewById(R.id.new_page_cancel);
		cancel.setClickable(true);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelAction();
			}
		});
	}

	public void cancelAction() {
		Intent intent = new Intent(NewPageActivity.this, PageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	public void saveAction() {
		if (imageTaken != null) {

			Book book = getMBook();
			PageImpl newPage = book.createNewPage();
			newPage.setmImage(imageTaken);

			book.addPage(newPage);
			book.saveBook();
			if (imageFile != null) {
				imageFile.delete();
			} else {
				Log.w("", "Image File is null");
			}
			Intent intent = new Intent(NewPageActivity.this, PageActivity.class);
			// Bundle b = new Bundle();
			// b.putInt(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
			startActivity(intent);
		}
	}
}
