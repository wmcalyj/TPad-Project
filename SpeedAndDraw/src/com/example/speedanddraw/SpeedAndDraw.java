package com.example.speedanddraw;

import nxr.tpad.lib.TPad;
import nxr.tpad.lib.TPadImpl;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SpeedAndDraw extends Activity {
	DrawingView dv;
	private Paint mPaint;
	private VelocityTracker mVelocityTracker = null;
	GridView gridView;
	final static int dvId = View.generateViewId();

	static Integer[] colors = { Color.BLACK, Color.BLUE, Color.CYAN,
			Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA,
			Color.RED, Color.YELLOW };

	// Instantiate a new TPad object
	TPad mTpad;

	Button clear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Get TPad reference from the TPad Implementation Library
		mTpad = new TPadImpl(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_and_draw);
		RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent);

		gridView = (GridView) findViewById(R.id.colorGrid);
		setGridView();

		dv = new DrawingView(this);

		RelativeLayout.LayoutParams dvLayout = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		dvLayout.addRule(RelativeLayout.BELOW, R.id.colorGrid);
		dvLayout.addRule(RelativeLayout.ABOVE, R.id.clear);
		dv.setLayoutParams(dvLayout);
		dv.setId(dvId);
		parent.addView(dv);
		clear = (Button) findViewById(R.id.clear);

		addListenerOnButton();

		setContentView(parent);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);
	}

	private void setGridView() {
		gridView.setAdapter(new ImageAdapter(this));

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mPaint.setColor(colors[position]);
			}
		});
	}

	public void addListenerOnButton() {
		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (dv != null) {
					dv.clear();
				}
			}
		});

	}

	public class DrawingView extends View {

		public int width;
		public int height;
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;
		Context context;
		private Paint circlePaint;
		private Path circlePath;

		public DrawingView(Context c) {
			super(c);
			context = c;
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			circlePaint = new Paint();
			circlePath = new Path();
			circlePaint.setAntiAlias(true);
			circlePaint.setColor(Color.BLUE);
			circlePaint.setStyle(Paint.Style.STROKE);
			circlePaint.setStrokeJoin(Paint.Join.MITER);
			circlePaint.setStrokeWidth(4f);
		}

		public DrawingView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public DrawingView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);

			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			canvas.drawPath(mPath, mPaint);
			canvas.drawPath(circlePath, circlePaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;

				circlePath.reset();
				circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			circlePath.reset();
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			int index = event.getActionIndex();
			int action = event.getActionMasked();
			int pointerId = event.getPointerId(index);

			switch (action) {
			case MotionEvent.ACTION_DOWN:
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
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				mVelocityTracker.addMovement(event);
				// When you want to determine the velocity, call
				// computeCurrentVelocity(). Then call getXVelocity()
				// and getYVelocity() to retrieve the velocity for each pointer
				// ID.
				mVelocityTracker.computeCurrentVelocity(1000);
				// Log velocity of pixels per second
				// Best practice to use VelocityTrackerCompat where possible.
				float vx = VelocityTrackerCompat.getXVelocity(mVelocityTracker,
						pointerId);
				float vy = VelocityTrackerCompat.getYVelocity(mVelocityTracker,
						pointerId);
				float frictionLevel = getFrictionLevel(vx, vy);
				mTpad.sendFriction(frictionLevel);
				// Log.d("", "X velocity: " + vx);
				// Log.d("", "Y velocity: " + vy);
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				// Return a VelocityTracker object back to be re-used by others.
				mVelocityTracker.recycle();
				mVelocityTracker = null;
				mTpad.turnOff();
				break;
			case MotionEvent.ACTION_CANCEL:
				mVelocityTracker.recycle();
				mVelocityTracker = null;
				mTpad.turnOff();
				break;
			}
			return true;
		}

		public void clear() {
			if (mCanvas != null) {
				mCanvas.drawColor(0, Mode.CLEAR);
			}
			// if (mPath != null) {
			// mPath.reset();
			// }
			// if (mBitmapPaint != null) {
			// mBitmapPaint.reset();
			// }
			// if (circlePath != null) {
			// circlePath.reset();
			// }
			invalidate();
		}
	}

	public float getSpeed(float vx, float vy) {
		return (float) Math.sqrt(vx * vx + vy * vy);
	}

	public float getFrictionLevel(float vx, float vy) {
		float speed = getSpeed(vx, vy);
		Log.d("", "Speed: " + speed);
		float frictionLevel = 0.3f;
		for (int i = 1; i <= 2; i++) {
			if (speed - 3000 <= 0) {
				Log.d("", "Friction Level: " + frictionLevel);
				return frictionLevel;
			} else {
				speed -= 3000;
				frictionLevel += 0.3f;
			}
		}
		Log.d("", "Friction Level: " + frictionLevel);
		return 1f;
	}

	public class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return colors.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return colors[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
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
			imageView.setBackgroundColor(colors[position]);
			return imageView;
		}

	}
}
