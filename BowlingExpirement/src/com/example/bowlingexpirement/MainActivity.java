package com.example.bowlingexpirement;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import nxr.tpad.lib.views.FrictionMapView;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MainActivity extends Activity {

	static Integer[] surfaces = { R.id.texture1, R.id.texture2, R.id.texture3 };

	// Custom Haptic Rendering view defined in TPadLib
	FrictionMapView fricView;
	ImageView background;

	// TPad object defined in TPadLib
	static TPad mTpad;

	static float ratio = 1.0f;

	ImageView ball;

	private VelocityTracker mVelocityTracker = null;

	private int yDelta, yOffset;
	private static float lineY;

	private int originalX;
	private int originalY;
	private boolean crossLine = false;
	private boolean rolled = false;

	private int currentImageResource;

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
			ImageView imageView, float vy) {
		Log.d("", "vy passed: " + vy);
		double newSpeed = Math.pow(Math.abs(vy), 0.5) * 20;
		Log.d("", "new speed is: " + newSpeed);
		TranslateAnimation translateAmination = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_PARENT, (-0.95f * ratio));
		translateAmination.setRepeatCount(0);
		translateAmination.setInterpolator(new DecelerateInterpolator());
		translateAmination.setDuration((long) (1000000 / (newSpeed / ratio)));
		return translateAmination;
	}

	public static Animation createBowlingMovement(ImageView imageView, float vy) {
		RotateAnimation rolling = createImageRotationAnimation(imageView);
		TranslateAnimation translate = createTranslateAnimation(imageView, vy);
		AnimationSet animations = new AnimationSet(false);
		animations.addAnimation(rolling);
		animations.addAnimation(translate);
		animations.setAnimationListener(new AnimationListener() {
			// This should be the correct place to set onAnimationEnd action
			// Namely, update the ball position, however, a work around is to
			// set the flag globally and only reset them when reset is clicked
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO
				mTpad.turnOff();

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// Nothing
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Nothing
			}
		});
		animations.setFillAfter(true);

		return animations;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTpad = new TPadImpl(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		currentImageResource = R.drawable.glass_rain;

		// Get all surfaces
		ImageView[] textures = new ImageView[surfaces.length];
		for (int i = 0; i < surfaces.length; i++) {
			textures[i] = (ImageView) findViewById(surfaces[i]);
		}
		addTextureListener(textures);

		View startLine = (View) findViewById(R.id.startLine);
		RelativeLayout.LayoutParams lp = (LayoutParams) startLine
				.getLayoutParams();
		lineY = lp.bottomMargin;

		ball = (ImageView) findViewById(R.id.ball);
		LayoutParams bp = (LayoutParams) ball.getLayoutParams();
		originalX = bp.leftMargin;
		originalY = bp.bottomMargin;
		addBollDragingListerner(ball);

		background = (ImageView) findViewById(R.id.surface);

		Button start = (Button) findViewById(R.id.start);
		Button stop = (Button) findViewById(R.id.reset);
		addStartListener(start);
		addStopListener(stop);
	}

	public void addBollDragingListerner(ImageView ball) {

		ball.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int index = event.getActionIndex();
				int action = event.getActionMasked();
				int pointerId = event.getPointerId(index);
				final int y = (int) event.getRawY();

				switch (action) {
				case MotionEvent.ACTION_DOWN: {
					mTpad.sendFriction(1.4f - ratio);
					RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v
							.getLayoutParams();
					yDelta = y - lParams.topMargin;
					yOffset = lParams.bottomMargin;

					if (mVelocityTracker == null) {
						// Retrieve a new VelocityTracker object to watch the
						// velocity of a motion.
						mVelocityTracker = VelocityTracker.obtain();
					} else {
						// Reset the velocity tracker back to its initial state.
						mVelocityTracker.clear();
					}
					// Add a user's movement to the tracker.
					mVelocityTracker.addMovement(event);
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					mTpad.sendFriction(1.4f - ratio);
					mVelocityTracker.addMovement(event);
					mVelocityTracker.computeCurrentVelocity(1000);
					float vy = VelocityTrackerCompat.getYVelocity(
							mVelocityTracker, pointerId);

					if (!crossLine) {
						// When you want to determine the velocity, call
						// computeCurrentVelocity(). Then call getXVelocity()
						// and getYVelocity() to retrieve the velocity for each
						// pointer
						// ID.
						RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v
								.getLayoutParams();
						layoutParams.bottomMargin = yDelta - y + yOffset;
						v.setLayoutParams(layoutParams);

						Log.d("", "vy - " + vy);

						if (layoutParams.bottomMargin + v.getHeight() >= lineY) {
							crossLine = true;
							Log.d("", "Cross line");
						}
					}
					if (crossLine && !rolled) {

						Animation bowling = createBowlingMovement(
								(ImageView) v, vy);
						v.startAnimation(bowling);
						rolled = true;

					}

					break;

				}

				case MotionEvent.ACTION_UP: {
					// Return a VelocityTracker object back to be re-used by
					// others.
					mVelocityTracker.recycle();
					mVelocityTracker = null;
					mTpad.turnOff();
					v.invalidate();
				}
					break;
				case MotionEvent.ACTION_CANCEL: {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
					v.invalidate();
					mTpad.turnOff();
				}
					break;

				}
				return true;
			}
		});
	}

	public void addTextureListener(ImageView[] textures) {
		for (int i = 0; i < textures.length; i++) {
			textures[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {
					case R.id.texture1:
						background.setImageResource(R.drawable.glass_rain);
						ratio = 1.0f;
						currentImageResource = R.drawable.glass_rain;
						stopAction(null);

						break;
					case R.id.texture2:
						background
								.setImageResource(R.drawable.basketball_cover);
						currentImageResource = R.drawable.basketball_cover;
						ratio = 0.6f;
						stopAction(null);
						break;
					case R.id.texture3:
						background.setImageResource(R.drawable.rug);
						currentImageResource = R.drawable.rug;
						ratio = 0.3f;
						stopAction(null);
						break;
					default:
						break;
					}
				}
			});
		}
	}

	public void addStartListener(Button start) {
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ball.clearAnimation();
				Animation bowling = createBowlingMovement(ball, 2000);
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
				stopAction(arg0);
			}
		});

	}

	public void stopAction(View arg0) {
		mTpad.turnOff();
		ball.clearAnimation();
		background = (ImageView) findViewById(R.id.surface);
		background.setImageResource(currentImageResource);
		LayoutParams bp = (LayoutParams) ball.getLayoutParams();
		bp.leftMargin = originalX;
		bp.bottomMargin = originalY;
		ball.setLayoutParams(bp);
		crossLine = false;
		rolled = false;
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

	// Don't use on drag, not good
	private void onDrag(ImageView ball) {

		ball.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipData.Item item = new ClipData.Item((CharSequence) v
						.getTag());
				String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };

				ClipData dragData = new ClipData(v.getTag().toString(),
						mimeTypes, item);
				View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);

				v.startDrag(dragData, myShadow, v, 0);
				return true;
			}
		});
		ball.setOnDragListener(new View.OnDragListener() {

			@Override
			public boolean onDrag(View v, DragEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
				case DragEvent.ACTION_DRAG_STARTED:
					Log.d("", "action drag started");
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_ENTERED:
					Log.d("", "action drag entered");
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_ENDED:
					Log.d("", "action drag ended");
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_EXITED:
					Log.d("", "action drag exited");
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_LOCATION:
					Log.d("", "action drag location");
					// Do nothing
					break;
				case DragEvent.ACTION_DROP:
					Log.d("", "action drop");
					// Do nothing
					break;
				// case DragEvent.
				}
				return true;
			}
		});
	}
}
