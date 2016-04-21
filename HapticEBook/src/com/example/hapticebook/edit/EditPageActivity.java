package com.example.hapticebook.edit;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.hapticebook.MainActivity;
import com.example.hapticebook.PageActivity;
import com.example.hapticebook.R;
import com.example.hapticebook.data.book.impl.PageImpl;

public class EditPageActivity extends MainActivity {

	PageImpl currentPage;
	ImageView image;
	private MediaRecorder mRecorder;
	private MediaPlayer player;
	boolean recordOn = false;
	boolean playOn = false;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		context = this;

		setImage();

		addAllButtonListerners();

	}

	private void addAllButtonListerners() {
		// TODO Auto-generated method stub
		addRecordButtonListener();
		addPlayButtonListener();
		addSaveButtonListener();
		addCancelButtonListener();

	}

	private void setImage() {

		LinearLayout toolSet = (LinearLayout) findViewById(R.id.tool_set);
		RelativeLayout editPage = (RelativeLayout) findViewById(R.id.edit_page);
		editPage.bringChildToFront(toolSet);
		this.currentPage = (PageImpl) getIntent().getSerializableExtra(
				"currentPage");

		if (currentPage != null) {
			image = (ImageView) findViewById(R.id.edit_image);
			image.setImageBitmap(currentPage.getmImage().getImage());
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
						Toast.makeText(
								context,
								"Failed to start recorder, please try again later",
								Toast.LENGTH_LONG).show();
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
					currentPage.stopPlayingRecording(player);
					((ImageView) v).setImageResource(R.drawable.audio);
					playOn = false;
				} else {
					// Audio is off, turn on

					((ImageView) v).setImageResource(R.drawable.audio_blue);
					playOn = true;
					player = currentPage.startPlayingRecording();
					player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							currentPage.stopPlayingRecording(mp);
							((ImageView) v).setImageResource(R.drawable.audio);
							playOn = false;

						}
					});
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
				currentPage.getRecordFile().delete();
				Intent intent = new Intent(EditPageActivity.this,
						PageActivity.class);
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
				Intent intent = new Intent(EditPageActivity.this,
						PageActivity.class);
				// Bundle b = new Bundle();
				// b.putInt(PAGE_ACTIVITY_KEY, PAGE_ACTIVITY_NEW_PHOTO);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
	}

}
