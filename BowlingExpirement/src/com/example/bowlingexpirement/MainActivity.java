package com.example.bowlingexpirement;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {

	static Integer[] surfaces = { R.drawable.basketball_cover,
			R.drawable.glass_rain };

	// Custom Haptic Rendering view defined in TPadLib
	FrictionMapView fricView;

	// TPad object defined in TPadLib
	TPad mTpad;

	static float ratio = 1.0f;

	ImageView ball;

	// Create the bowling animation
	public static RotateAnimation createImageRotationAnimation(
			ImageView imageView) {
		// RotateAnimation animation = new RotateAnimation(0f, 360f,
		// imageView.getWidth()/2, imageView.getHeight()/2);
		RotateAnimation animation = new RotateAnimation(0f, 720f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animation.setRepeatCount(0);
		animation.setInterpolator(new DecelerateInterpolator());
		// The smaller the number is, the faster the ball rotates
		animation.setDuration((long) (1200 / ratio));
		animation.setFillAfter(true);
		return animation;
	}

	public static TranslateAnimation createTranslateAnimation(
			ImageView imageView) {
		TranslateAnimation translateAmination = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, (-0.95f * ratio));
		translateAmination.setRepeatCount(0);
		translateAmination.setInterpolator(new DecelerateInterpolator());
		translateAmination.setDuration((long) (2000 / ratio));
		translateAmination.setFillAfter(true);
		return translateAmination;
	}

	public static Animation createBowlingMovement(ImageView imageView) {
		RotateAnimation rolling = createImageRotationAnimation(imageView);
		TranslateAnimation translate = createTranslateAnimation(imageView);
		AnimationSet animations = new AnimationSet(false);
		animations.addAnimation(rolling);
		animations.addAnimation(translate);
		animations.setFillAfter(true);

		return animations;
	}

	// Create switching background
	private GridView setGridView(GridView gridView) {

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				ImageView surface = (ImageView) findViewById(R.id.surface);
				surface.setImageResource(surfaces[position]);
				ball.clearAnimation();
				if (surfaces[position] == R.drawable.basketball_cover) {
					ratio = 0.6f;
				} else if (surfaces[position] == R.drawable.glass_rain) {
					ratio = 1.0f;
				}
			}
		});
		return gridView;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTpad = new TPadImpl(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		GridView surfaces = (GridView) findViewById(R.id.surfaceGrid);
		surfaces.setAdapter(new ImageAdapter(this));
		setGridView(surfaces);
		ball = (ImageView) findViewById(R.id.ball);

		Button start = (Button) findViewById(R.id.start);
		Button stop = (Button) findViewById(R.id.reset);
		addStartListener(start);
		addStopListener(stop);
	}

	public void addStartListener(Button start) {
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ball.clearAnimation();
				Animation bowling = createBowlingMovement(ball);
				ball.startAnimation(bowling);
				long time = bowling.getDuration();
				mTpad.sendFriction(1.4f - ratio);
				Handler handler = new Handler();
				Runnable r = new Runnable() {
					@Override
					public void run() {
						mTpad.turnOff();
					}
				};
				handler.postDelayed(r, 2000 + time);
			}
		});

	}

	public void addStopListener(Button stop) {
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ball.clearAnimation();
				mTpad.turnOff();
			}
		});

	}

	public class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return surfaces.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return surfaces[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				// if it's not recycled, initialize some attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(surfaces[position]);

			return imageView;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		mTpad.disconnectTPad();
		super.onDestroy();
	}
}