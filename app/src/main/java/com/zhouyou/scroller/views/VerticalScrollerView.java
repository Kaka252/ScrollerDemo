package com.zhouyou.scroller.views;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 作者：ZhouYou
 * 日期：2016/8/15.
 */
public class VerticalScrollerView extends ViewGroup {

    private static final String TAG = VerticalScrollerView.class.getSimpleName();

    // 子控件的数量
    private Scroller scroller;
    private int touchSlop;
    private int topBorder;
    private int bottomBorder;
    private int maxVelocity;
    private int minVelocity;

    private int mPointerId;

    public VerticalScrollerView(Context context) {
        this(context, null);
    }

    public VerticalScrollerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                childView.layout(0, i * childView.getMeasuredHeight(), childView.getMeasuredWidth(), (i + 1) * childView.getMeasuredHeight());
            }
        }
        topBorder = getChildAt(0).getTop();
        bottomBorder = getChildAt(childCount - 1).getBottom();
    }


    // 开始时的Y轴
    private float lastY;
    private float lastInterceptY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        float interceptX = ev.getX();
        float interceptY = ev.getY();
        acquireVelocityTracker(ev);
        final VelocityTracker verTracker = mVelocityTracker;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPointerId = ev.getPointerId(0);
                isIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                verTracker.computeCurrentVelocity(1000, maxVelocity);
                final float velocityY = verTracker.getYVelocity(mPointerId);
                float deltaY = interceptY - lastInterceptY;
                isIntercept = Math.abs(deltaY) > touchSlop || Math.abs(velocityY) > minVelocity;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                releaseVelocityTracker();
                isIntercept = false;
                break;
            default:
                break;
        }
        lastY = interceptY;
        lastInterceptY = interceptY;
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float touchY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float deltaY = touchY - lastY;
                float scrollByStart = deltaY;
                Log.d(TAG, "deltaY = " + deltaY + " | scrollY = " + getScrollY());
                if (getScrollY() - deltaY < topBorder) {
                    scrollByStart = deltaY / 3;
                } else if (getScrollY() + getHeight() > bottomBorder) {
                    scrollByStart = deltaY / 3;
                }
                scrollBy(0, (int) -scrollByStart);
                break;
            case MotionEvent.ACTION_UP:
                int index = (getScrollY() + getHeight() / 2) / getHeight();
                if (index > getChildCount() - 1) {
                    index = getChildCount() - 1;
                } else if (index < 0) {
                    index = 0;
                }
                int dy = index * getHeight() - getScrollY();
                scroller.startScroll(0, getScrollY(), 0, dy, 250);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        lastY = touchY;
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(0, scroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * 向VelocityTracker添加MotionEvent
     *
     * @param event
     */
    private VelocityTracker mVelocityTracker;

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 释放VelocityTracker
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
}
