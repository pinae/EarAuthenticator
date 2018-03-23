package de.ct.earauthenticator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 * TODO: document your custom view class.
 */
public class EarTouchArea extends View {
    private LinkedHashMap<Integer, Tuple> mTouchPoints;
    private Paint mBackgroundPaint, mTouchPointPaint, mLinePaint;
    private Paint mValuePaint, mGreenPaint, mRedPaint;
    private boolean earPossible;
    private Rect wholeCanvasRect;
    private Tuple top_point = new Tuple(0, 0);
    private Tuple bottom_point = new Tuple(0, 0);
    private float maxDistance;
    private float minDw = 0;
    private float minDwDh = 0;
    private float maxDw = 0;
    private float maxDwDh = 0;
    private Tuple minDwStart = new Tuple(0, 0);
    private Tuple maxDwStart = new Tuple(0, 0);
    private boolean trainMode = false;
    private LinkedList<EarDataset> mTrainingData = new LinkedList<>();
    OnTrainFinishedEventListener mTrainFinishedListener;

    public EarTouchArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EarTouchArea(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EarTouchArea, defStyle, 0);
        try {
            earPossible = a.getBoolean(R.styleable.EarTouchArea_earPossible, true);
        } finally {
            a.recycle();
        }

        // Initialize Touch Points
        mTouchPoints = new LinkedHashMap<>();
        mTouchPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTouchPointPaint.setStyle(Paint.Style.FILL);
        mTouchPointPaint.setColor(0xffbadaff);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(4);
        mLinePaint.setTextSize(48);
        mLinePaint.setColor(0xffffffff);
        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setTextSize(48);
        mValuePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        mValuePaint.setColor(0xffffffff);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(0xff000000);
        wholeCanvasRect = new Rect(0, 0, getWidth(), getHeight());
    }

    public interface OnTrainFinishedEventListener {
        void onEvent();
    }

    public void setTrainFinishedEventListener(OnTrainFinishedEventListener eventListener) {
        mTrainFinishedListener = eventListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(wholeCanvasRect, mBackgroundPaint);
        // Draw touch points
        for (Map.Entry<Integer, Tuple> entry : mTouchPoints.entrySet()) {
            //int key = entry.getKey();
            Tuple p = entry.getValue();
            canvas.drawCircle(p.x, p.y,
                    (float) 40.0, mTouchPointPaint);
        }
        // draw measurement lines and labels
        canvas.drawLine(bottom_point.x, bottom_point.y, top_point.x, top_point.y, mLinePaint);
        canvas.drawLine(minDwStart.x, minDwStart.y,
                minDwStart.x - minDw * (top_point.y - bottom_point.y),
                minDwStart.y + minDw * (top_point.x - bottom_point.x), mLinePaint);
        canvas.drawLine(maxDwStart.x, maxDwStart.y,
                maxDwStart.x - maxDw * (top_point.y - bottom_point.y),
                maxDwStart.y + maxDw * (top_point.x - bottom_point.x), mLinePaint);
        if (maxDwDh < 0.5 || maxDw < 0.01) {
            canvas.drawText(String.format(Locale.GERMANY, "%.0f", maxDistance / 100),
                    (float) (bottom_point.x + 0.7 * (top_point.x - bottom_point.x) -
                            30 * (top_point.y - bottom_point.y) / maxDistance),
                    (float) (bottom_point.y + 0.7 * (top_point.y - bottom_point.y) +
                            30 * (top_point.x - bottom_point.x) / maxDistance), mLinePaint);
        } else {
            canvas.drawText(String.format(Locale.GERMANY, "%.0f", maxDistance / 100),
                    (float) (bottom_point.x + 0.3 * (top_point.x - bottom_point.x) -
                            30 * (top_point.y - bottom_point.y) / maxDistance),
                    (float) (bottom_point.y + 0.3 * (top_point.y - bottom_point.y) +
                            30 * (top_point.x - bottom_point.x) / maxDistance), mLinePaint);
        }
        if (minDw < -0.01) {
            canvas.drawText(String.format(Locale.GERMANY, "%.0f", minDw * -10),
                    (float) (minDwStart.x - 0.5 * minDw * (top_point.y - bottom_point.y) +
                            15 * (top_point.x - bottom_point.x) / maxDistance),
                    (float) (minDwStart.y + 0.5 * minDw * (top_point.x - bottom_point.x) +
                            15 * (top_point.y - bottom_point.y) / maxDistance),
                    mLinePaint);
        }
        if (maxDw > 0.01) {
            canvas.drawText(String.format(Locale.GERMANY, "%.0f", maxDw * 10),
                    (float) (maxDwStart.x - 0.5 * maxDw * (top_point.y - bottom_point.y) +
                            15 * (top_point.x - bottom_point.x) / maxDistance),
                    (float) (maxDwStart.y + 0.5 * maxDw * (top_point.x - bottom_point.x) +
                            15 * (top_point.y - bottom_point.y) / maxDistance),
                    mLinePaint);
        }
        float alpha = (float) (Math.atan(-1*minDw/minDwDh) / 6.283 * 360);
        if (maxDwDh > 0.01) {
            alpha = (float) (Math.atan(maxDw/maxDwDh) / 6.283 * 360);
        }
        canvas.drawText("α: " + String.format(Locale.GERMANY, "%.1f", alpha),
                canvas.getWidth()/2,
                canvas.getHeight()-140, mValuePaint);
        canvas.drawText("β: " + String.format(Locale.GERMANY, "%.1f",
                0104.2018 * Math.sin(maxDistance / 9 *
                        Math.max(maxDw/maxDwDh, -1*minDw/minDwDh))),
                canvas.getWidth()/2,
                canvas.getHeight()-60, mValuePaint);

        /*int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);
        setMeasuredDimension(w, h);
        wholeCanvasRect.set(0, 0, w, h);
    }

    private EarDataset getEarMeasures() {
        // calculate max distance
        maxDistance = 0;
        if (mTouchPoints.size() < 2) {
            return new EarDataset(0, 0, 0, 0,0);
        }
        top_point = mTouchPoints.get(0);
        bottom_point = mTouchPoints.get(mTouchPoints.size()-1);
        for (Map.Entry<Integer, Tuple> entry1 : mTouchPoints.entrySet()) {
            int key = entry1.getKey();
            Tuple p1 = entry1.getValue();
            for (Map.Entry<Integer, Tuple> entry2 : mTouchPoints.entrySet()) {
                if (entry2.getKey() != key) {
                    Tuple p2 = entry2.getValue();
                    float entry_distance = p1.getDistance(p2);
                    if (entry_distance >= maxDistance) {
                        maxDistance = entry_distance;
                        if (p1.y <= p2.y) {
                            top_point = p1;
                            bottom_point = p2;
                        } else {
                            top_point = p2;
                            bottom_point = p1;
                        }
                    }
                }
            }
        }
        // calculate rightmost and leftmost point
        minDw = 0;
        minDwDh = 0;
        maxDw = 0;
        maxDwDh = 0;
        for (Map.Entry<Integer, Tuple> entry1 : mTouchPoints.entrySet()) {
            //int key = entry1.getKey();
            Tuple p = entry1.getValue();
            Tuple h = new Tuple(top_point.x - bottom_point.x, top_point.y - bottom_point.y);
            float d_h = (-1*bottom_point.x*h.x+h.y*(p.y-bottom_point.y)+p.x*h.x)/(h.x*h.x+h.y*h.y);
            float d_w = (h.y*(bottom_point.x-p.x)-bottom_point.y*h.x+p.y*h.x)/(h.x*h.x+h.y*h.y);
            if (d_w <= minDw) {
                minDw = d_w;
                minDwDh = d_h;
            }
            if (d_w >= maxDw) {
                maxDw = d_w;
                maxDwDh = d_h;
            }
        }
        minDwStart.x = bottom_point.x + minDwDh * (top_point.x - bottom_point.x);
        minDwStart.y = bottom_point.y + minDwDh * (top_point.y - bottom_point.y);
        maxDwStart.x = bottom_point.x + maxDwDh * (top_point.x - bottom_point.x);
        maxDwStart.y = bottom_point.y + maxDwDh * (top_point.y - bottom_point.y);
        return new EarDataset(maxDistance, minDw, minDwDh, maxDw, maxDwDh);
    }

    private void updatePointerPositions(MotionEvent event) {
        for(int i = 0; i < event.getPointerCount(); i++) {
            int pointerID = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);
            mTouchPoints.put(pointerID, new Tuple(x, y));
        }
        EarDataset newEar = getEarMeasures();
        //Log.d("Ear measures", newEar.toString());
        // Add new training dataset
        if (trainMode && (newEar.minDw < -0.05 || newEar.maxDw > 0.05) &&
                mTouchPoints.size() >= 4) {
            mTrainingData.add(newEar);
            trainMode = false;
            mTrainFinishedListener.onEvent();
        }
        // Check if this is a correct ear
        if (!trainMode) {
            float minErr = 10000;
            for (EarDataset correctEar : mTrainingData) {
                float err = correctEar.squaredNormalizedError(newEar);
                if (err <= minErr) {
                    minErr = err;
                }
            }
            //Log.d("Min ear err", Float.toString(minErr));
            if (minErr < 0.01) {
                Log.d("Ear Recognized! Error: ", Float.toString(minErr));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mTouchPoints.clear();
                updatePointerPositions(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_POINTER_UP:
                updatePointerPositions(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //mTouchPoints.clear();
                break;
        }
        postInvalidate();
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setEarPossible(boolean isPossible) {
        earPossible = isPossible;
        invalidate();
        requestLayout();
    }

    public boolean getEarPossible() {
        return earPossible;
    }

    public void startTrainMode() {
        trainMode = true;
    }
}
