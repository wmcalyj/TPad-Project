package com.example.findspecialspot;

import java.util.Random;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	// Define 'FrictionMapView' class that will link to the .xml file
	FrictionMapView fricView;

	// Instantiate a new TPad object
	TPad mTpad;

	ImageView pic;
	Canvas canvas;
	Bitmap bmp;
	Bitmap workingBmp;

	int[] pics = { R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
			R.drawable.pic4 };
	int maxPic = 2;
	int currentPic = 0;

	Button nextPicButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get TPad reference from the TPad Implementation Library
		mTpad = new TPadImpl(this);

		bmp = BitmapFactory.decodeResource(getResources(), pics[currentPic]);
		workingBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
		canvas = new Canvas(workingBmp);
		pic = (ImageView) findViewById(R.id.pic1);
		pic.setImageBitmap(workingBmp);
		Drawable d = getResources().getDrawable(pics[currentPic]);
		final int h = d.getIntrinsicHeight();
		final int w = d.getIntrinsicWidth();
		int[] pairs = getRandPairs(w, h);
		setOnTouchFunction(pic, h, w, pairs);

		addListenerOnButton();

		// Link the first 'View' called basicView to the view with the id=view1
		// pic = (ImageView) findViewById(R.id.view);
	}

	public void addListenerOnButton() {

		pic = (ImageView) findViewById(R.id.pic1);

		nextPicButton = (Button) findViewById(R.id.nextPic);
		nextPicButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				bmp.recycle();
				workingBmp.recycle();

				currentPic++;
				if (currentPic >= maxPic) {
					currentPic = 0;
				}

				bmp = BitmapFactory.decodeResource(getResources(),
						pics[currentPic]);
				workingBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
				canvas = new Canvas(workingBmp);
				pic.setImageBitmap(workingBmp);
				Drawable d = getResources().getDrawable(pics[currentPic]);
				final int h = d.getIntrinsicHeight();
				final int w = d.getIntrinsicWidth();
				int[] pairs = getRandPairs(w, h);
				setOnTouchFunction(pic, h, w, pairs);
			}

		});

	}

	public int[] getRandPairs(float w, float h) {
		int[] pairs = new int[4];
		Random rand = new Random();
		int x1 = rand.nextInt(200), y1 = rand.nextInt((int) h - 500 + 1), x2 = x1 + 500, y2 = y1 + 500;
		pairs[0] = x1;
		pairs[1] = y1;
		pairs[2] = x2;
		pairs[3] = y2;
		return pairs;
	}

	public void drawRectangle(int x1, int y1, int x2, int y2) {
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(10);
		canvas.drawRect(x1, y1, x2, y2, paint);
	}

	public void setOnTouchFunction(ImageView pic, final int h, final int w,
			final int[] pairs) {
		pic.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				float x = event.getX();
				float y = event.getY();

				int x1 = pairs[0], y1 = pairs[1], x2 = pairs[2], y2 = pairs[3];

				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					// If the initial touch was on the left half of the
					// view, turn off the TPad, else turn it on to 100%
					if (x > x1 && y > y1 && x < x2 && y < y2) {
						mTpad.sendFriction(1f);
						drawRectangle(x1, y1, x2, y2);
					} else {
						mTpad.turnOff();
					}
					break;

				case MotionEvent.ACTION_MOVE:
					if (x > x1 && y > y1 && x < x2 && y < y2) {
						mTpad.sendFriction(1f);
						drawRectangle(x1, y1, x2, y2);
					} else {
						mTpad.turnOff();
					}
					break;

				case MotionEvent.ACTION_UP:
					// If the user lifts up their finger from the
					// screen, turn the TPad off (0%)
					mTpad.turnOff();
					break;
				}
				return true;
			}
		});
	}

	@Override
	protected void onDestroy() {
		mTpad.disconnectTPad();
		super.onDestroy();
	}
}
