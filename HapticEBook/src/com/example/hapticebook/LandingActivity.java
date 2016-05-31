package com.example.hapticebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class LandingActivity extends MainActivity {

	private ProgressBar loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.hideMenu();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.landing);
		addFooterListener();
		addExitListener();
		loading = (ProgressBar) findViewById(R.id.landing_loading);
	}

	private void addExitListener() {
		Button exit = (Button) findViewById(R.id.landing_exit);
		exit.setClickable(true);
		exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishAffinity();
			}
		});

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
				loading.setVisibility(View.VISIBLE);
				Intent intent;
				intent = new Intent(LandingActivity.this, PageActivity.class);
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

	@Override
	public void onBackPressed() {
		finishAffinity();
	}

	@Override
	public void onResume() {
		super.onResume();
		super.hideMenu();
	}
}
