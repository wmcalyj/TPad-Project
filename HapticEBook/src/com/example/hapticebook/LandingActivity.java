package com.example.hapticebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.example.hapticebook.newpage.NewPageActivity;

public class LandingActivity extends MainActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing);
		addFooterListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem student = menu.findItem(R.id.student_mode);
		MenuItem teacher = menu.findItem(R.id.teacher_mode);

		if (super.getMBook().isStudentMode()) {
			student.setChecked(true);
		}
		if (super.getMBook().isTeacherMode()) {
			teacher.setChecked(true);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem student = menu.findItem(R.id.student_mode);
		MenuItem teacher = menu.findItem(R.id.teacher_mode);

		if (super.getMBook().isStudentMode()) {
			student.setChecked(true);
		}
		if (super.getMBook().isTeacherMode()) {
			teacher.setChecked(true);
		}
		return true;
	}

	public void addFooterListener() {
		ImageView footer = (ImageView) findViewById(R.id.landing_footer);
		footer.setClickable(true);
		footer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("", "footer clicked");
				// If there are available pages, go to the first page
				// Otherwise, go to new page
				Intent intent;
				if (isBookEmpty()) {
					intent = new Intent(LandingActivity.this,
							NewPageActivity.class);
					Log.d("", "Book is empty");
				} else {
					intent = new Intent(LandingActivity.this,
							PageActivity.class);
				}
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
}
