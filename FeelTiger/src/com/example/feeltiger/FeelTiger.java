package com.example.feeltiger;

import com.example.mysecondtpad.R;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class FeelTiger extends Activity {
	// Define 'FrictionMapView' class that will link to the .xml file
	FrictionMapView fricView;

	// Instantiate a new TPad object
	TPad mTpad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_second_tpad);

		// Link FrictionMapView to the .xml file
		fricView = (FrictionMapView) findViewById(R.id.view);

		mTpad = new TPadImpl(this);

		// Set the TPad of the FrictionMapView to the current TPad
		fricView.setTpad(mTpad);

		// Load an image from resources
		Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.tiger);

		// Set the friction data bitmap to the test image
		fricView.setDataBitmap(defaultBitmap);
	}
	
	@Override
	protected void onDestroy() {
		mTpad.disconnectTPad();
		super.onDestroy();
	}
}
