package com.example.hapticebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.crash.CustomExceptionHandler;
import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.impl.BookImpl;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;

public class MainActivity extends Activity {

	public static final String FILE_NAME = "book.srl";
	public static final String PAGE_ACTIVITY_KEY = "PAGE_ACTIVITY_KEY";
	public static final int PAGE_ACTIVITY_NEW_PHOTO = 1;
	public static final int PAGE_ACTIVITY_DEFAULT = -1;
	// To get access to mBook, use getMBook
	private static Book mBook = null;
	protected TPad mTpad;
	private static Bitmap empty = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	private static Point screenSize;

	public Book getMBook() {
		if (mBook == null) {
			mBook = loadBook();
		}
		return mBook; // The book may still be null
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hideMenu();
		if (!(CustomExceptionHandler.class.isAssignableFrom(Thread.getDefaultUncaughtExceptionHandler().getClass()))) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		}
		mTpad = new TPadImpl(this);
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.landing);
		if (mBook == null) {
			mBook = loadBook();
		}
	}

	protected Point getScreenSize(Bitmap original) {
		if (original == null) {
			if (screenSize == null) {
				Display display = getWindowManager().getDefaultDisplay();
				screenSize = new Point();
				display.getSize(screenSize);
			}
			return screenSize;
		}
		if (screenSize == null || original.getWidth() != screenSize.x || original.getHeight() != screenSize.y) {
			screenSize = new Point();
			screenSize.x = original.getWidth();
			screenSize.y = original.getHeight();
		}
		return screenSize;
	}

	protected Bitmap getEmptyBitmap() {
		if (empty == null || empty.isRecycled()) {
			empty = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		}
		return empty;
	}

	public void hideMenu() {

		View decorView = getWindow().getDecorView();
		// Hide the status bar.

		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide
																								// nav
																								// bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		// int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		// decorView.setSystemUiVisibility(uiOptions);
		// // Remember that you should never show the action bar if the
		// // status bar is hidden, so hide that too if necessary.
		// ActionBar actionBar = getActionBar();
		// actionBar.hide();
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
		if (id == R.id.teacher_mode) {
			mBook.readAsTeacher();
			item.setChecked(true);
		} else {
			mBook.readAsStudent();
			item.setChecked(true);
		}

		return super.onOptionsItemSelected(item);
	}

	private Book loadBook() {
		ObjectInputStream input;
		String filename = FILE_NAME;
		String root = Configuration.ROOT_PATH;
		File dir = new File(root, "hapticEBook/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File book = new File(dir, filename);
		if (!book.exists()) {
			// First time open this app
			mBook = new BookImpl();
			mBook.setFilePath(dir);
			mBook.saveBook();
		} else {
			try {
				input = new ObjectInputStream(new FileInputStream(new File(dir, filename)));
				mBook = (Book) input.readObject();
				if (mBook.isEmpty()) {
					Log.d("", "Book is empty");
				}

				input.close();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Toast.makeText(this, "Book load from:\n" + mBook.getFilePath().getAbsolutePath(), Toast.LENGTH_LONG).show();
		return mBook;
	}

	public boolean saveBook() {

		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(
					new FileOutputStream(new File(getFilesDir(), "") + File.separator + FILE_NAME));
			out.writeObject(mBook);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean isBookEmpty() {
		if (mBook == null) {
			mBook = loadBook();
		}
		return mBook.isEmpty();
	}

	protected int calculateInSampleSize(BitmapFactory.Options options) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int reqHeight = size.y;
		int reqWidth = size.x;

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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getClass().isAssignableFrom(MainActivity.class)) {
			Log.i("QUIT", "Quit app");
			LogService.WriteToLog(Action.CLOSE, "Quit App");
		} else {
			Log.i("QUIT", "Calling from " + getClass().getName());
		}
	}

	@Override
	public void finishAffinity() {
		LogService.WriteToLog(Action.CLOSE, "Exit app");
		super.finishAffinity();
	}

	protected static void disableAfterClick(View v) {
		// We need this function for all save/cancel buttons because we don't
		// want the children to click on save or cancel twice to trigger use
		// recycled bitmap exception
		v.setClickable(false);
	}

	protected void finishLoading() {
		ProgressBar l1 = (ProgressBar) findViewById(R.id.filter_loading);
		ProgressBar l2 = (ProgressBar) findViewById(R.id.landing_loading);
		ProgressBar l3 = (ProgressBar) findViewById(R.id.page_loading);
		if (l1 != null) {
			l1.setVisibility(View.GONE);
		}
		if (l2 != null) {
			l2.setVisibility(View.GONE);
		}
		if (l3 != null) {
			l3.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		hideMenu();
		super.onResume();
	}
}
