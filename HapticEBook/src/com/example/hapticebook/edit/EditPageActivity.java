package com.example.hapticebook.edit;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.PageActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.config.Configuration;
import com.example.hapticebook.data.book.Page;
import com.example.hapticebook.filterservice.impl.FilterService;
import com.example.hapticebook.log.Action;
import com.example.hapticebook.log.LogService;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import nxr.tpad.lib.views.FrictionMapView;

public class EditPageActivity extends MainActivity {

	Page currentPage;
	ImageView image, save, cancel, recording, play, edit;
	FrictionMapView tpadView;
	private MediaRecorder mRecorder;
	private MediaPlayer player;
	boolean recordOn = false;
	boolean playOn = false;
	String TAG = "EditPage";

	Bitmap filterBmp;
	Bitmap mImage;

	boolean isNewlyTakenImage = false;
	boolean callFromFilterForNewlyTakenImage = false;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		setIsNewlyTakenImage();
		setCalledFromFilterForNewImage();
		setImageAndCurrentPage();
		if ((isNewlyTakenImage && isNewlyTakenImage(currentPage)) || callFromFilterForNewlyTakenImage) {
			// set feel button to invisible
			edit = (ImageView) findViewById(R.id.edit_tool_feel);
			edit.setVisibility(View.INVISIBLE);
		} else {
			if (Configuration.HAPTICDISABLED) {
				if (edit == null) {
					edit = (ImageView) findViewById(R.id.edit_tool_feel);
				}
				edit.setVisibility(View.GONE);
			} else {
				if (edit == null) {
					edit = (ImageView) findViewById(R.id.edit_tool_feel);
				}
				edit.setVisibility(View.VISIBLE);
			}
		}
		context = this;

		addAllButtonListerners();
		if (!Configuration.HAPTICDISABLED) {
			tpadView = (FrictionMapView) findViewById(R.id.edit_feel_image);
			tpadView.setTpad(mTpad);
			applyFilter();
		} else {
			tpadView = (FrictionMapView) findViewById(R.id.edit_feel_image);
			((FrameLayout) tpadView.getParent()).removeView(tpadView);
		}
		if (Configuration.RECORDINGENABLED) {
			recording = (ImageView) findViewById(R.id.edit_tool_record);
			if (recording != null) {
				recording.setVisibility(View.VISIBLE);
			}
			play = (ImageView) findViewById(R.id.edit_tool_play);
			if (play != null) {
				play.setVisibility(View.VISIBLE);
			}
		} else {
			recording = (ImageView) findViewById(R.id.edit_tool_record);
			if (recording != null) {
				recording.setVisibility(View.GONE);
			}
			play = (ImageView) findViewById(R.id.edit_tool_play);
			if (play != null) {
				play.setVisibility(View.GONE);
			}
		}
		finishLoading();
		if (isNewlyTakenImage || callFromFilterForNewlyTakenImage) {
			Toast.makeText(this, "Record a message that describes this photo", Toast.LENGTH_LONG).show();
		}
	}

	private void applyFilter() {
		if (edit == null) {
			edit = (ImageView) findViewById(R.id.edit_tool_feel);
		}
		if (filterBmp != null) {
			filterBmp.recycle();
			filterBmp = null;
			System.gc();
		}
		filterBmp = FilterService.getTPadFilter(getResources(), currentPage, getScreenSize(mImage));
		if (filterBmp == null) {
			edit.setImageResource(R.drawable.texture);
			tpadView.setDataBitmap(getEmptyBitmap());
			mTpad.turnOff();
			return;
		} else {
			tpadView.setDataBitmap(filterBmp);
			if (Configuration.DEBUG) {
				image.setImageBitmap(filterBmp);
			}
			edit.setImageResource(R.drawable.touch_active);
		}
	}

	private void addAllButtonListerners() {
		if (Configuration.RECORDINGENABLED) {
			addRecordButtonListener();
			addPlayButtonListener();
		}
		addSaveButtonListener();
		addCancelButtonListener();
		if (!Configuration.HAPTICDISABLED) {
			addAnnotateButtonListener();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		checkImage();
	}

	private void setImageAndCurrentPage() {
		if (callFromFilterForNewlyTakenImage) {
			currentPage = getMBook().goToLastPage();
		} else {
			currentPage = getMBook().getCurrentPage();
		}
		if (currentPage != null) {
			mImage = currentPage.getImageBitmap();
			if (image == null) {
				image = (ImageView) findViewById(R.id.edit_image);
			}
			image.setImageBitmap(mImage);
		} else {
			Log.d("", "currentPage is null");
		}
	}

	protected void addRecordButtonListener() {

		recording = (ImageView) findViewById(R.id.edit_tool_record);

		recording.setClickable(true);
		recording.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disablePlayingAudio();
				if (recordOn) {
					// It's currently on, on click should close
					((ImageView) v).setImageResource(R.drawable.record);
					currentPage.stopRecording(mRecorder);
					recordOn = false;
				} else {
					// It's current off, on click should start recording
					mRecorder = currentPage.startRecording();
					if (mRecorder != null) {
						((ImageView) v).setImageResource(R.drawable.record_active);
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

		play = (ImageView) findViewById(R.id.edit_tool_play);

		play.setClickable(true);
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				disableRecording();
				if (playOn) {
					// Audio is on, turn off
					currentPage.stopPlayingAudio(player);
					((ImageView) v).setImageResource(R.drawable.play);
					playOn = false;
				} else {
					// Audio is off, turn on
					player = currentPage.startPlayingAudio();
					if (player == null) {
						// No recording file
						Toast.makeText(context, "Please record an audio before playing it", Toast.LENGTH_LONG).show();

					} else {
						((ImageView) v).setImageResource(R.drawable.play_active);
						playOn = true;
						player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								currentPage.stopPlayingAudio(mp);
								((ImageView) v).setImageResource(R.drawable.play);
								playOn = false;
							}
						});
					}
				}
			}
		});
	}

	protected void addCancelButtonListener() {
		if (cancel == null) {
			cancel = (ImageView) findViewById(R.id.edit_tool_cancel);
		}

		cancel.setClickable(true);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableRecording();
				disablePlayingAudio();
				MainActivity.disableAfterClick(v);
				currentPage.cancelAudioFile();
				currentPage.cancelHapticFilter();
				Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
				if (isNewlyTakenImage(currentPage)) {
					getMBook().cancelSavingNewImage();
					intent.putExtra(PAGE_ACTIVITY_KEY, CANCEL_SAVING_NEW_PHOTO_FROM_EDIT);
					LogService.WriteToLog(Action.CANCEL_EDIT, "Cancel saving newly taken image, go back to pages");
				} else {
					if (!Configuration.HAPTICDISABLED) {
						getMBook().saveBook();
						LogService.WriteToLog(Action.CANCEL_EDIT,
								"Cancel editing page " + currentPage.getImageFilePath() + ", go back to pages");
					}
				}

				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				cleanup();
				startActivity(intent);
			}
		});
	}

	protected void addSaveButtonListener() {
		if (!isNewlyTakenImage) {
			save = (ImageView) findViewById(R.id.edit_tool_save);
		} else {
			((ImageView) findViewById(R.id.edit_tool_save)).setVisibility(View.INVISIBLE);
			save = (ImageView) findViewById(R.id.edit_tool_save_corner);
			save.setVisibility(View.VISIBLE);
		}
		save.setClickable(true);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableRecording();
				disablePlayingAudio();
				MainActivity.disableAfterClick(v);
				Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				if (isNewlyTakenImage(currentPage) || callFromFilterForNewlyTakenImage) {
					if (!callFromFilterForNewlyTakenImage) {
						getMBook().addNewPage(currentPage);
						LogService.WriteToLog(Action.SAVE_EDIT,
								"Save newly taken image " + currentPage.getImageFilePath());
					}
					intent.putExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
				} else {
					intent.removeExtra(PAGE_ACTIVITY_KEY);
					LogService.WriteToLog(Action.SAVE_EDIT, "Save edited file " + currentPage.getImageFilePath());
				}
				currentPage.saveAudioFile();
				getMBook().saveBook();
				cleanup();
				startActivity(intent);
			}
		});
	}

	private void addAnnotateButtonListener() {
		if (edit == null) {
			edit = (ImageView) findViewById(R.id.edit_tool_feel);
		}
		edit.setClickable(true);
		edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableRecording();
				disablePlayingAudio();
				Intent intent = new Intent(EditPageActivity.this, FilterActivity.class);
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
		setIsNewlyTakenImage();
		setCalledFromFilterForNewImage();
		checkImage();
		if (!Configuration.HAPTICDISABLED) {
			applyFilter();
		}
	}

	private void setIsNewlyTakenImage() {
		if (getIntent().getIntExtra(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_DEFAULT) == PAGE_ACTIVITY_NEW_PHOTO) {
			isNewlyTakenImage = true;
		} else {
			isNewlyTakenImage = false;
		}
	}

	private void setCalledFromFilterForNewImage() {
		if (getIntent().getBooleanExtra(FILTER_ACTIVITY_NEW_IMAGE_SAVED, false)) {
			this.callFromFilterForNewlyTakenImage = true;
		} else {
			this.callFromFilterForNewlyTakenImage = false;
		}
	}

	@Override
	public void onBackPressed() {
		// Back is pressed, go to page activity page
		cleanup();
		finish();
		goToPageActivity();
	}

	private void goToPageActivity() {
		Intent intent = new Intent(EditPageActivity.this, PageActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		if (recordOn && mRecorder != null) {
			currentPage.stopRecording(mRecorder);
		}
		if (playOn && player != null) {
			currentPage.stopPlayingAudio(player);
		}
		cleanup();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		if (recordOn && mRecorder != null) {
			currentPage.stopRecording(mRecorder);
		}
		if (playOn && player != null) {
			currentPage.stopPlayingAudio(player);
		}
		super.onPause();
	}

	private void disableRecording() {
		if (recordOn && mRecorder != null) {
			currentPage.stopRecording(mRecorder);
			ImageView recording = (ImageView) findViewById(R.id.edit_tool_record);
			recording.setImageResource(R.drawable.record);
			recordOn = false;
		}
	}

	private void disablePlayingAudio() {
		if (playOn && player != null) {
			currentPage.stopPlayingAudio(player);
			ImageView playing = (ImageView) findViewById(R.id.edit_tool_play);
			playing.setImageResource(R.drawable.play);
			playOn = false;

		}
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
		finish();
		System.gc();
	}

	private void checkImage() {
		if (mImage == null || mImage.isRecycled()) {
			mImage = currentPage.getImageBitmap();
			image.setImageBitmap(mImage);
		}
	}
}
