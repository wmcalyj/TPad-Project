package com.example.gear;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	TPad mTpad;
	View gearView;
	GearsDrawable gd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTpad = new TPadImpl(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button start = (Button) findViewById(R.id.start);
		Button stop = (Button) findViewById(R.id.stop);
		addStartListener(start);
		addStopListener(stop);
		gearView = findViewById(R.id.gears);
		gd = new GearsDrawable(this);
		gearView.setBackground(gd);
	}

	private void addStopListener(Button stop) {
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mTpad.turnOff();
				gd.stop();
			}
		});
	}

	private void addStartListener(Button start) {

		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mTpad.sendFriction(0.5f);
				gd.start();
			}
		});

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
	public void onDestroy() {
		mTpad.disconnectTPad();
		super.onDestroy();
	}
}