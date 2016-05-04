package com.example.hapticebook;

import java.io.Serializable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.edit.EditPageActivity;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;
import com.example.hapticebook.newpage.NewPageActivity;

public class PageActivity extends MainActivity {

	private ImageView image;
	private ImageView leftFooter;
	private ImageView rightFooter;
	private Book book = getMBook();
	private Page currentPage;
	private boolean audioOn = false;
	private MediaPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page);

		Bundle b = getIntent().getExtras();
		if (b != null && b.getInt(PAGE_ACTIVITY_KEY) == PAGE_ACTIVITY_NEW_PHOTO) {
			currentPage = book.goToLastPage();

			Log.d("", "Page Activity, go to last page");
		} else {
			currentPage = book.goToFirstPage();
			Log.d("", "Page Activity, go to first page");
		}

		Bitmap bm = currentPage.getBitmapImage();
		image = (ImageView) findViewById(R.id.page_image);
		image.setImageBitmap(bm);

		bringHeaderSetToFront();
		refresh();
		addHeaderButtonListeners();
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
						LogService.WriteToLog(Action.PLAY_AUDIO_PAGE_START);
						mPlayer = currentPage.startPlayingAudio();
						mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								currentPage.stopPlayingAudio(mp);
								((ImageView) v)
										.setImageResource(R.drawable.audio);
								audioOn = false;

							}
						});
					} else {
						((ImageView) v).setImageResource(R.drawable.audio);
						audioOn = false;
						LogService.WriteToLog(Action.PLAY_AUDIO_PAGE_STOP);
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
				currentPage = book.goToPrevPage();
				LogService.WriteToLog(Action.PREVIOUS_PAGE);
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
					currentPage = book.goToNextPage();
					LogService.WriteToLog(Action.NEXT_PAGE);
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
				LogService.WriteToLog(Action.ADD_PAGE);
				Intent intent = new Intent(PageActivity.this,
						NewPageActivity.class);

				startActivity(intent);
			}
		});
	}

	public void addDeletePageListener() {
		ImageView delete = (ImageView) findViewById(R.id.page_delete_button);
		delete.setClickable(true);
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				book.deleteCurrentPage();
				LogService.WriteToLog(Action.DELETE);
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
				LogService.WriteToLog(Action.EDIT);
				Intent intent = new Intent(PageActivity.this,
						EditPageActivity.class);
				intent.putExtra("currentPage", (Serializable) currentPage);
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
			changeLeftFooterToBack();
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
		Bitmap bm = currentPage.getBitmapImage();
		image = (ImageView) findViewById(R.id.page_image);
		image.setImageBitmap(bm);
	}

	private void changeLeftFooterToBack() {
		ImageView back = (ImageView) findViewById(R.id.page_footer_left);
		back.setImageResource(R.drawable.back);
		addBackListener();
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
		int newPageActivityFlag = getIntent().getIntExtra(PAGE_ACTIVITY_KEY,
				PAGE_ACTIVITY_DEFAULT);
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
		super.hideMenu();
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to landing page
		finish();
		goToLandingPage();
	}

}
