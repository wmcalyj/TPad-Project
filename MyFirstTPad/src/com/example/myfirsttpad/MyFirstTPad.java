package com.example.myfirsttpad;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MyFirstTPad extends Activity {
	// Custom Haptic Rendering view defined in TPadLib
	FrictionMapView fricView;

	// TPad object defined in TPadLib
	TPad mTpad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the content of the screen to the .xml file that is in the layout
		// folder
		setContentView(R.layout.activity_my_first_tpad);

		// Load new tpad object from TPad Implementation Library
		mTpad = new TPadImpl(this);

		// Link friction view to .xml file
		fricView = (FrictionMapView) findViewById(R.id.view1);

		// Link local tpad object to the FrictionMapView
		fricView.setTpad(mTpad);

		// Load in the image stored in the drawables folder
		Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.filter);

		// Set the friction data bitmap to the test image
		fricView.setDataBitmap(defaultBitmap);
		defaultBitmap.recycle();

		fricView.setDisplayShowing(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_first_tpad, menu);
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
}
