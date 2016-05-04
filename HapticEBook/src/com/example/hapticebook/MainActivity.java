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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.impl.BookImpl;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

public class MainActivity extends Activity {

	public static final String FILE_NAME = "book.srl";
	public static final String PAGE_ACTIVITY_KEY = "PAGE_ACTIVITY_KEY";
	public static final int PAGE_ACTIVITY_NEW_PHOTO = 1;
	public static final int PAGE_ACTIVITY_DEFAULT = -1;
	// To get access to mBook, use getMBook
	private static Book mBook = null;

	public Book getMBook() {
		if (mBook == null) {
			mBook = loadBook();
		}
		return mBook; // The book may still be null
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.landing);
		if (mBook == null) {
			mBook = loadBook();
		}
	}

	public void hideMenu() {

		View decorView = getWindow().getDecorView();
		// Hide the status bar.

		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
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

		try {
			input = new ObjectInputStream(new FileInputStream(new File(
					new File(getFilesDir(), "") + File.separator + filename)));
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
		} finally {
			if (mBook == null) {
				mBook = new BookImpl();
				File dir = getFilesDir();
				if (dir != null) {
					mBook.setFilePath(dir);
					mBook.saveBook(dir);
				} else {
					Log.d("", "File dir is null");
				}
			} else {
				File dir = getFilesDir();
				if (dir != null) {
					mBook.setFilePath(dir);
					mBook.saveBook();
				} else {
					Log.d("", "File dir is null");
				}
			}
		}
		Toast.makeText(this,
				"Book load from:\n" + mBook.getFilePath().getAbsolutePath(),
				Toast.LENGTH_LONG).show();
		return mBook;
	}

	public boolean saveBook() {

		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(
					getFilesDir(), "") + File.separator + FILE_NAME));
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogService.WriteToLog(Action.CLOSE);
		LogService.StopWriting();
	}
}
