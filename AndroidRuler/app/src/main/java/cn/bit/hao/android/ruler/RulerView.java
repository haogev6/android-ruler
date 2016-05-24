package cn.bit.hao.android.ruler;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * @author wuhao at 2016/5/20
 */
public class RulerView extends View {
	private static final String TAG = RulerView.class.getSimpleName();

	private static final String CENTIMETER_UNIT_LABEL = "cm";
	private static final String INCH_UNIT_LABEL = "in";

	private static final float DEFAULT_LONG_SCALE_LENGTH_MM = 5;
	private static final float DEFAULT_MEDIUM_SCALE_LENGTH_MM = 3.5f;
	private static final float DEFAULT_SHORT_SCALE_LENGTH_MM = 2.5f;
	private static final int DEFAULT_TEXT_SIZE_MM = 3;
	private static final String ORIENTATION = "ORIENTATION";
	private float longScaleLength = DEFAULT_LONG_SCALE_LENGTH_MM;
	private float mediumScaleLength = DEFAULT_MEDIUM_SCALE_LENGTH_MM;
	private float shortScaleLength = DEFAULT_SHORT_SCALE_LENGTH_MM;
	private float mTextSize = DEFAULT_TEXT_SIZE_MM;
	private float mUnitSize = DEFAULT_TEXT_SIZE_MM;
	private int mTextColor = Color.BLACK;
	private boolean mShowCentimeter = true;
	private boolean mShowInch = true;
	private float oneMillimeter = 1;
	private float oneInch = 1;
	private TextPaint mTextPaint;
	private TextPaint mUnitPaint;
	private Paint mLinePaint;
	private float mTextHeight;
	private float zeroShift = 0;
	private int mRotateDegree = 0;

	public RulerView(Context context) {
		super(context);
		init(null, 0);
	}

	public RulerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public RulerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	public int getRotateDegree() {
		return mRotateDegree;
	}

	public void setRotateDegree(int rotateDegree) {
		if (this.mRotateDegree == rotateDegree % 360) {
			return;
		}
		this.mRotateDegree = rotateDegree % 360;
		initUnitLength();
		invalidate();
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i(TAG, newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait");
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		this.mRotateDegree = savedState.rotateDegree;
		invalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.rotateDegree = this.mRotateDegree;
		return savedState;
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.RulerView, defStyle, 0);

		mShowCentimeter = a.getBoolean(R.styleable.RulerView_centimeter, mShowCentimeter);
		mShowInch = a.getBoolean(R.styleable.RulerView_inch, mShowInch);
		mRotateDegree = a.getInt(R.styleable.RulerView_rotate_degree, mRotateDegree) % 360;

		a.recycle();

		// Set up a default TextPaint object
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setColor(mTextColor);

		mUnitPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mUnitPaint.setTextAlign(Paint.Align.LEFT);
		mUnitPaint.setColor(mTextColor);

		final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics));
		mLinePaint.setColor(mTextColor);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setStrokeCap(Paint.Cap.BUTT);

		initUnitLength();
	}

	private void initUnitLength() {
		final Configuration configuration = getResources().getConfiguration();
		final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			oneInch = mRotateDegree % 180 == 0 ? displayMetrics.ydpi
					: TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1, displayMetrics);
		} else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			oneInch = mRotateDegree % 180 == 0 ? TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1, displayMetrics)
					: displayMetrics.ydpi;
		}
		oneMillimeter = oneInch * (1.0f / 25.4f);

		longScaleLength = oneMillimeter * DEFAULT_LONG_SCALE_LENGTH_MM;
		mediumScaleLength = oneMillimeter * DEFAULT_MEDIUM_SCALE_LENGTH_MM;
		shortScaleLength = oneMillimeter * DEFAULT_SHORT_SCALE_LENGTH_MM;

		mTextSize = oneMillimeter * DEFAULT_TEXT_SIZE_MM;
		mUnitSize = mTextSize / 1.25f;

		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint.setTextSize(mTextSize);
		mUnitPaint.setTextSize(mUnitSize);

		final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		zeroShift = mTextPaint.measureText("0");

		mTextHeight = -mTextPaint.getFontMetrics().top;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawColor(Color.WHITE);

		// allocations per draw cycle.
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;

		int contentLength = mRotateDegree % 180 == 90 ? contentHeight : contentWidth;
		int saveCount = canvas.save();
		canvas.rotate(mRotateDegree, (float) getWidth() / 2, (float) getHeight() / 2);
		if (mRotateDegree % 180 == 90) {
			canvas.translate((float) (contentWidth - contentHeight) / 2, (float) (contentHeight - contentWidth) / 2);
		}

		int scale = 0;
		float scaleShiftLength = scale;
		while (scaleShiftLength < contentLength) {
			float scaleLength = shortScaleLength;
			if (scale % 10 == 0) {
				scaleLength = longScaleLength;
				canvas.drawText(String.valueOf(scale / 10), paddingLeft + zeroShift + scaleShiftLength, scaleLength + mTextHeight, mTextPaint);
				if (scale == 0) {
					canvas.drawText(CENTIMETER_UNIT_LABEL, paddingLeft + zeroShift * 1.6f + scaleShiftLength, scaleLength + mTextHeight, mUnitPaint);
				}
			} else if (scale % 5 == 0) {
				scaleLength = mediumScaleLength;
			}

			canvas.drawLine(paddingLeft + zeroShift + scaleShiftLength, 0, paddingLeft + zeroShift + scaleShiftLength, scaleLength, mLinePaint);

			++scale;
			scaleShiftLength = oneMillimeter * scale;
		}

		canvas.restoreToCount(saveCount);
	}

	public static class SavedState extends BaseSavedState {
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
		public int rotateDegree;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			rotateDegree = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(rotateDegree);
		}

		@Override
		public String toString() {
			return "RulerView.SavedState{mRotateDegree" + rotateDegree + "}";
		}
	}
}
