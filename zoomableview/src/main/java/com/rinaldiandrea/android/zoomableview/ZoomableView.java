package com.rinaldiandrea.android.zoomableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Calendar;

/**
 * @author arinaldi
 */
public class ZoomableView
        extends RelativeLayout
        implements ScaleGestureDetector.OnScaleGestureListener {

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;

    private ViewGroup mZoomContent;

    // region Construction

    public ZoomableView(Context context) {
        this(context, null, 0);
    }

    public ZoomableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    // endregion

    // region View implementation

    @Override
    public void addView(View child) {
        if (getChildCount() == 0) {
            super.addView(child);

        } else {
            mZoomContent.addView(child);
        }
    }

    // endregion

    // region Private methods

    private void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ZoomableView, 0, 0);
        try {
            int layoutId = ta.getResourceId(R.styleable.ZoomableView_layout, -1);
            if (layoutId > -1) {
                mZoomContent = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, this, false);
                mZoomContent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mZoomContent.setBackgroundColor(Color.BLUE);
                addView(mZoomContent);

                final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
                this.setOnTouchListener(new OnTouchListener() {

                    private long mLastActionDown;

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:

                                mLastActionDown = Calendar.getInstance().getTimeInMillis();

                                if (scale > MIN_ZOOM) {
                                    mode = Mode.DRAG;
                                    for (int i = 0; i < getChildCount(); i++) {
                                        Tag tag = (Tag) getChildAt(i).getTag();
                                        tag.startX = motionEvent.getX() - tag.prevDx;
                                        tag.startY = motionEvent.getY() - tag.prevDy;
                                    }
                                }
                                break;

                            case MotionEvent.ACTION_MOVE:
                                if (mode == Mode.DRAG) {
                                    for (int i = 0; i < getChildCount(); i++) {
                                        Tag tag = (Tag) getChildAt(i).getTag();
                                        tag.dx = motionEvent.getX() - tag.startX;
                                        tag.dy = motionEvent.getY() - tag.startY;
                                    }
                                }
                                break;

                            case MotionEvent.ACTION_POINTER_DOWN:
                                mode = Mode.ZOOM;
                                break;

                            case MotionEvent.ACTION_POINTER_UP:
                                mode = Mode.NONE;
                                break;

                            case MotionEvent.ACTION_UP:

                                if (Calendar.getInstance().getTimeInMillis() - mLastActionDown < 100L) {
                                    performClick();
                                }

                                mode = Mode.NONE;
                                for (int i = 0; i < getChildCount(); i++) {
                                    Tag tag = (Tag) getChildAt(i).getTag();
                                    tag.prevDx = tag.dx;
                                    tag.prevDy = tag.dy;
                                }
                                break;
                        }

                        scaleDetector.onTouchEvent(motionEvent);

                        if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
                            getParent().requestDisallowInterceptTouchEvent(true);

                            Tag tag;
                            View child;
                            for (int i = 0; i < getChildCount(); i++) {
                                child = getChildAt(i);
                                tag = (Tag) child.getTag();
                                if (tag == null) {
                                    tag = new Tag();
                                    child.setTag(tag);
                                }

                                float maxDx = (child.getWidth() - (child.getWidth() / scale)) / 2 * scale;
                                float maxDy = (child.getHeight() - (child.getHeight() / scale)) / 2 * scale;
                                tag.dx = Math.min(Math.max(tag.dx, -maxDx), maxDx);
                                tag.dy = Math.min(Math.max(tag.dy, -maxDy), maxDy);

                                applyScaleAndTranslation(child, tag);
                            }
                        }

                        return true;
                    }
                });
            }

        } finally {
            ta.recycle();
        }
    }

    private void applyScaleAndTranslation(View child, Tag tag) {
        child.setScaleX(scale);
        child.setScaleY(scale);
        child.setTranslationX(tag.dx);
        child.setTranslationY(tag.dy);
    }

    private static class Tag {
        float dx;
        float dy;
        float prevDx;
        float prevDy;
        float startX;
        float startY;

        Tag() {
            super();

            dx = 0f;
            dy = 0f;
            prevDx = 0f;
            prevDy = 0f;
            startX = 0f;
            startY = 0f;
        }
    }

    // endregion

    // region ScaleGestureDetector.OnScaleGestureListener implementation

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleDetector) {
        float scaleFactor = scaleDetector.getScaleFactor();

        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;

        } else {
            lastScaleFactor = 0;
        }

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleDetector) {
        // INF: Empty
    }

    // endregion
}
