package com.example.hapticebook.edit;

import java.io.Serializable;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.PageActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.HapticFilterEnum;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;

public class EditPageActivity extends MainActivity {

	Page currentPage;
	ImageView image;
	FrictionMapView tpadView;
	private MediaRecorder mRecorder;
	private MediaPlayer player;
	boolean recordOn = false;
	boolean playOn = false;

	String chosenFilterFilePath;

	Bitmap filterBmp;

	TPad mTpad;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		context = this;

		setImage();

		mTpad = new TPadImpl(this);
		tpadView = (FrictionMapView) findViewById(R.id.edit_feel_image);
		tpadView.setTpad(mTpad);
		addAllButtonListerners();
		applyFilter();

	}

	private void applyFilter() {
		chosenFilterFilePath = currentPage.getFilterImagePath();
		if (chosenFilterFilePath != null) {
			filterBmp = BitmapFactory.decodeFile(chosenFilterFilePath);
			if (filterBmp == null) {
				return;
			}
			tpadView.setDataBitmap(filterBmp);
			if (Configuration.DEBUG) {
				image.setImageBitmap(filterBmp);
			}
			ImageView feel = (ImageView) findViewById(R.id.edit_tool_feel);
			feel.setImageResource(R.drawable.touch_orange);
		} else {
			ImageView feel = (ImageView) findViewById(R.id.edit_tool_feel);
			feel.setImageResource(R.drawable.touch);
		}

	}

	private void addAllButtonListerners() {
		addRecordButtonListener();
		addPlayButtonListener();
		addSaveButtonListener();
		addCancelButtonListener();
		addAnnotateButtonListener();
		addFeelButtonListener();

	}

	private void addFeelButtonListener() {
		// TODO Auto-generated method stub
		HapticFilterEnum currentFilter = currentPage.getHapticFilter();
		if (currentFilter != null && !HapticFilterEnum.NONE.equals(currentFilter)) {

		}

	}

	@Override
	public void onResume() {
		super.hideMenu();
		super.onResume();
	}

	private void setImage() {

		// this.currentPage = (Page)
		// getIntent().getSerializableExtra("currentPage");
		this.currentPage = getMBook().getCurrentPage();
		if (currentPage != null) {
			image = (ImageView) findViewById(R.id.edit_image);
			image.setImageBitmap(currentPage.getImageBitmap());
			// image.setImageURI(currentPage.getImageUri());

			// LinearLayout toolSet = (LinearLayout)
			// findViewById(R.id.tool_set);
			// RelativeLayout editPage = (RelativeLayout)
			// findViewById(R.id.edit_page);
			// editPage.bringChildToFront(toolSet);
			// ImageView cancel = (ImageView)
			// findViewById(R.id.edit_tool_cancel);
			// cancel.bringToFront();
			// ((View) cancel.getParent()).requestLayout();
			// ((View) cancel).invalidate();
		} else {
			Log.d("", "currentPage is null");
		}
	}

	protected void addRecordButtonListener() {

		ImageView recording = (ImageView) findViewById(R.id.edit_tool_record);

		recording.setClickable(true);
		recording.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (recordOn) {
					// It's currently on, on click should close
					((ImageView) v).setImageResource(R.drawable.record);
					currentPage.stopRecording(mRecorder);
					recordOn = false;
				} else {
					// It's current off, on click should start recording
					mRecorder = currentPage.startRecording();
					if (mRecorder != null) {
						((ImageView) v).setImageResource(R.drawable.record_red);
						recordOn = true;
					} else {
						Toast.makeText(context, "Failed to start recorder, please try again later", Toast.LENGTH_LONG)
								.show();
					}

				}

			}
		});
	}

	protected void addPlayButtonListener() {

		ImageView play = (ImageView) findViewById(R.id.edit_tool_play);

		play.setClickable(true);
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				if (playOn) {
					// Audio is on, turn off
					currentPage.stopPlayingAudio(player);
					((ImageView) v).setImageResource(R.drawable.audio);
					playOn = false;
				} else {
					// Audio is off, turn on
					player = currentPage.startPlayingAudio();
					if (player == null) {
						// No recording file
						Toast.makeText(context, "Please record an audio before playing it", Toast.LENGTH_LONG).show();

					} else {
						((ImageView) v).setImageResource(R.drawable.audio_blue);
						playOn = true;
						player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								currentPage.stopPlayingAudio(mp);
								((ImageView) v).setImageResource(R.drawable.audio);
								playOn = false;

							}
						});
					}
				}
			}
		});
	}

	protected void addCancelButtonListener() {

		ImageView cancel = (ImageView) findViewById(R.id.edit_tool_cancel);

		cancel.setClickable(true);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentPage.cancelAudioFile();
				currentPage.cancelHapticFilter();
				getMBook().saveBook();
				LogService.WriteToLog(Action.CANCEL_EDIT,
						"Cancel editing page " + currentPage.getImageFilePath() + ", go back to pages");
				Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

	protected void addSaveButtonListener() {

		ImageView save = (ImageView) findViewById(R.id.edit_tool_save);

		save.setClickable(true);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LogService.WriteToLog(Action.SAVE_EDIT, "Save edited file " + currentPage.getImageFilePath());
				currentPage.saveAudioFile();
				currentPage.saveHapticFilter(chosenFilterFilePath);
				getMBook().saveBook();
				Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
				// Bundle b = new Bundle();
				// b.putInt(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

	private void addAnnotateButtonListener() {
		ImageView edit = (ImageView) findViewById(R.id.edit_tool_paint);

		edit.setClickable(true);
		edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditPageActivity.this, FilterActivity.class);
				intent.putExtra("currentPage", (Serializable) currentPage);
				// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (intent != null)
			setIntent(intent);
		chosenFilterFilePath = intent
				.getStringExtra("com.example.hapticebook.edit.FilterActivity.ChosenFilterFilePath");
		if (chosenFilterFilePath != null) {
			tpadView.setDataBitmap(BitmapFactory.decodeFile(chosenFilterFilePath));
			if (Configuration.DEBUG) {
				image.setImageBitmap(BitmapFactory.decodeFile(chosenFilterFilePath));
			}
			ImageView feel = (ImageView) findViewById(R.id.edit_tool_feel);
			feel.setImageResource(R.drawable.touch_orange);
		} else {
			ImageView feel = (ImageView) findViewById(R.id.edit_tool_feel);
			feel.setImageResource(R.drawable.touch);
			mTpad.turnOff();
			image.setImageBitmap(currentPage.getImageBitmap());
		}
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to page activity page
		finish();
		goToPageActivity();
	}

	private void goToPageActivity() {
		Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		if (filterBmp != null) {
			filterBmp.recycle();
		}
		mTpad.disconnectTPad();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		if (filterBmp != null) {
			filterBmp.recycle();
		}
		super.onPause();
	}
}
