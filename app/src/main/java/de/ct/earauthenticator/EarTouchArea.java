package de.ct.earauthenticator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO: document your custom view class.
 */
public class EarTouchArea extends View {
    private LinkedHashMap<Integer, Tuple> mTouchPoints;
    private Paint mBackgroundPaint, mTouchPointPaint;
    private boolean earPossible;
    private Rect wholeCanvasRect;

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
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(0xff000000);
        wholeCanvasRect = new Rect(0, 0, getWidth(), getHeight());
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
        // Align and draw ear graphic


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

    private void getEarMeasures() {
        // calculate max distance
        double maxDistance = 0;
        Tuple top_point = mTouchPoints.get(0);
        Tuple bottom_point = mTouchPoints.get(mTouchPoints.size()-1);
        for (Map.Entry<Integer, Tuple> entry1 : mTouchPoints.entrySet()) {
            int key = entry1.getKey();
            Tuple p1 = entry1.getValue();
            for (Map.Entry<Integer, Tuple> entry2 : mTouchPoints.entrySet()) {
                if (entry2.getKey() != key) {
                    Tuple p2 = entry2.getValue();
                    double entry_distance = p1.getDistance(p2);
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
        double minDw = 0;
        double minDwDh = 0;
        double maxDw = 0;
        double maxDwDh = 0;
        for (Map.Entry<Integer, Tuple> entry1 : mTouchPoints.entrySet()) {
            int key = entry1.getKey();
            Tuple p = entry1.getValue();
            Tuple h = new Tuple(top_point.x - bottom_point.x, top_point.y - bottom_point.y);
            double d_h = (p.y - bottom_point.y - h.x * (p.x - bottom_point.x) / (-1 * h.y)) /
                    (h.y + h.x * h.x / (-1 * h.y));
            double d_w = (p.x - bottom_point.x - d_h * h.x) / (-1 * h.y);
            if (d_w <= minDw) {
                minDw = d_w;
                minDwDh = d_h;
            }
            if (d_w >= maxDw) {
                maxDw = d_w;
                maxDwDh = d_h;
            }
        }
        Log.d("Ear measures", "h: " + Double.toString(maxDistance) +
                " , min d_w: " + Double.toString(minDw) +
                " , min d_w_d_h: " + Double.toString(minDwDh) +
                " , max d_w: " + Double.toString(maxDw) +
                " , max d_w_d_h: " + Double.toString(maxDwDh));
    }

    private void updatePointerPositions(MotionEvent event) {
        for(int i = 0; i < event.getPointerCount(); i++) {
            int pointerID = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);
            mTouchPoints.put(pointerID, new Tuple(x, y));
        }
        for (Map.Entry<Integer, Tuple> entry : mTouchPoints.entrySet()) {
            int key = entry.getKey();
            Tuple p = entry.getValue();
            Log.d("TouchPoint", Integer.toString(key) + ": (" + Float.toString(p.x) +
                    ", " + Float.toString(p.y) + ")");
        }
        getEarMeasures();
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
}
