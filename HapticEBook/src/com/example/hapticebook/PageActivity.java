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

import com.example.hapticebook.data.book.Book;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.edit.EditPageActivity;
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

		Page p;
		Bundle b = getIntent().getExtras();
		if (b != null && b.getInt(PAGE_ACTIVITY_KEY) == PAGE_ACTIVITY_NEW_PHOTO) {
			p = book.goToLastPage();
		} else {
			p = book.goToFirstPage();
		}

		currentPage = p;

		Bitmap bm = p.getImage().getImage();
		image = (ImageView) findViewById(R.id.page_image);
		image.setImageBitmap(bm);

		bringHeaderSetToFront();
		setPlayAudio();
		checkFooter();
		addAllButtonListeners();
	}

	private void bringHeaderSetToFront() {
		RelativeLayout root = (RelativeLayout) findViewById(R.id.page_page);
		FrameLayout header = (FrameLayout) findViewById(R.id.page_header_set);
		root.bringChildToFront(header);
	}

	private void setPlayAudio() {
		boolean audioAvailable = currentPage.isAudioAvailable();
		if (audioAvailable) {
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
								((ImageView) v)
										.setImageResource(R.drawable.audio);
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

	protected void addAllButtonListeners() {
		addNewPageListener();
		addNextListener();
		addPrevListener();
		addDeletePageListener();
		addEditPageListener();
	}

	private void addPrevListener() {
		ImageView prev = (ImageView) findViewById(R.id.page_footer_left);

		if (prev.getVisibility() == View.VISIBLE) {
			prev.setClickable(true);
			prev.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Page p = book.goToPrevPage();
					checkFooter();
					Bitmap bm = p.getBitmapImage();
					image = (ImageView) findViewById(R.id.page_image);
					image.setImageBitmap(bm);
					currentPage = p;
					setPlayAudio();
					addPrevListener();
					addNextListener();

				}

			});
		}
	}

	private void addNextListener() {
		ImageView next = (ImageView) findViewById(R.id.page_footer_right);
		if (next.getVisibility() == View.VISIBLE) {
			next.setClickable(true);
			next.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Page p = book.goToNextPage();
					checkFooter();
					Bitmap bm = p.getBitmapImage();
					image = (ImageView) findViewById(R.id.page_image);
					image.setImageBitmap(bm);
					currentPage = p;
					setPlayAudio();
					addPrevListener();
					addNextListener();
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
				Page p = book.getCurrentPage();
				checkFooter();
				if (p != null) {
					Bitmap bm = p.getBitmapImage();
					image = (ImageView) findViewById(R.id.page_image);
					image.setImageBitmap(bm);
					currentPage = p;
					setPlayAudio();
				}
				addPrevListener();
				addNextListener();
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
				Intent intent = new Intent(PageActivity.this,
						EditPageActivity.class);
				intent.putExtra("currentPage", (Serializable) currentPage);
				startActivity(intent);
			}
		});

	}

	private void checkFooter() {
		leftFooter = (ImageView) findViewById(R.id.page_footer_left);
		rightFooter = (ImageView) findViewById(R.id.page_footer_right);

		if (book.isCurrentPageFirstPage()) {
			leftFooter.setVisibility(View.INVISIBLE);
		} else {
			leftFooter.setVisibility(View.VISIBLE);
		}
		if (book.isCurrentPageLastPage()) {
			rightFooter.setVisibility(View.INVISIBLE);
		} else {
			rightFooter.setVisibility(View.VISIBLE);
		}
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
	protected void onNewIntent(Intent intent) {
		if (intent != null)
			setIntent(intent);
		setPlayAudio();
	}

	@Override
	protected void onResume() {
		super.onResume();
		int newPageActivityFlag = getIntent().getIntExtra(PAGE_ACTIVITY_KEY,
				PAGE_ACTIVITY_DEFAULT);
		switch (newPageActivityFlag) {

		case PAGE_ACTIVITY_NEW_PHOTO:
			Page p = book.goToLastPage();
			Bitmap bm = p.getBitmapImage();
			image = (ImageView) findViewById(R.id.page_image);
			image.setImageBitmap(bm);
			currentPage = p;
			setPlayAudio();
			checkFooter();
			addAllButtonListeners();
			break;
		case PAGE_ACTIVITY_DEFAULT:
		default:
			break;

		}
	}
}
