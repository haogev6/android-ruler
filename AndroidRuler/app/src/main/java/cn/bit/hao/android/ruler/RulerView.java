package cn.bit.hao.android.ruler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import cn.bit.hao.android.R;

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

    private float longScaleLength = DEFAULT_LONG_SCALE_LENGTH_MM;
    private float mediumScaleLength = DEFAULT_MEDIUM_SCALE_LENGTH_MM;
    private float shortScaleLength = DEFAULT_SHORT_SCALE_LENGTH_MM;

    private static final int DEFAULT_TEXT_SIZE_MM = 3;
    private float mTextSize = DEFAULT_TEXT_SIZE_MM;
    private float mUnitSize = DEFAULT_TEXT_SIZE_MM;

    private int mTextColor = Color.BLACK;

    private boolean mShowCentimeter = true;
    private boolean mShowInch = true;
    private boolean mOrientationVertical = true;

    private float oneMillimeter = 1;
    private float oneInch = 1;

    private TextPaint mTextPaint;
    private TextPaint mUnitPaint;
    private Paint mLinePaint;

    private float mTextHeight;

    private float zeroShift = 0;

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

    public boolean isOrientationVertical() {
        return mOrientationVertical;
    }

    public void setOrientationVertical(boolean vertical) {
        mOrientationVertical = vertical;
        invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        oneMillimeter = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, displayMetrics);
        oneInch = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1, displayMetrics);

        longScaleLength = oneMillimeter * DEFAULT_LONG_SCALE_LENGTH_MM;
        mediumScaleLength = oneMillimeter * DEFAULT_MEDIUM_SCALE_LENGTH_MM;
        shortScaleLength = oneMillimeter * DEFAULT_SHORT_SCALE_LENGTH_MM;

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RulerView, defStyle, 0);

        mShowCentimeter = a.getBoolean(R.styleable.RulerView_centimeter, mShowCentimeter);
        mShowInch = a.getBoolean(R.styleable.RulerView_inch, mShowInch);
        mOrientationVertical = a.getBoolean(R.styleable.RulerView_vertical, mOrientationVertical);

        mTextSize = oneMillimeter * DEFAULT_TEXT_SIZE_MM;
        mUnitSize = mTextSize / 1.25f;

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mTextColor);

        mUnitPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mUnitPaint.setTextAlign(Paint.Align.LEFT);
        mUnitPaint.setColor(mTextColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics));
        mLinePaint.setColor(mTextColor);
        mLinePaint.setStyle(Paint.Style.STROKE);

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

        int contentLength = contentHeight;
        int saveCount = 0;
        if (mOrientationVertical) {
            saveCount = canvas.save();
            canvas.rotate(90, getWidth() / 2, getWidth() / 2);
        } else {
            contentLength = contentWidth;
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

        if (mOrientationVertical) {
            canvas.restoreToCount(saveCount);
        }
    }

}
