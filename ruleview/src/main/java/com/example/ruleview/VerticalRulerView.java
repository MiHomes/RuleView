package com.example.ruleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * Created by pc on 2018/1/24.
 */

public class VerticalRulerView extends View{
    private Context mContext;
    private Paint centerLinePaint;
    private Paint grayLinePaint;
    private Paint txtPaint;
    private int space = 30;
    private int startValue = 100;
    private int endValue = 250;
    private int width;
    private int height;
    private float mLastY;
    private int touchSlop;
    private Scroller mScroller;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int maxScrollX = 1000; // 最大允许滑出范围
    private int currentOffset; // 当前偏移
    private VelocityTracker mVelocityTracker;
    private boolean isFastScroll;
    private AllRulerCallback mListener;
    private int number;
    private int BASELINE_OFFSET;
    private int interval;
    private int textOffset;

    public VerticalRulerView(Context context) {
        this(context, null);
    }

    public VerticalRulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        centerLinePaint = new Paint();
        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setColor(getResources().getColor(R.color.yellow));
        centerLinePaint.setStrokeWidth(5);

        grayLinePaint = new Paint();
        grayLinePaint.setAntiAlias(true);
        grayLinePaint.setColor(getResources().getColor(R.color.age_text));
        grayLinePaint.setStrokeWidth(5);

        txtPaint = new Paint();
        txtPaint.setAntiAlias(true);
        txtPaint.setColor(getResources().getColor(R.color.age_text));
        txtPaint.setTextSize(50);

        // 新增部分 start
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mContext);
        touchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(mContext);
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        // 新增部分 end
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = (int) (mContext.getResources().getDisplayMetrics().density * 200 + 0.5);
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        setMeasuredDimension(width, height);

        BASELINE_OFFSET = height / 2;
        int x = (number - startValue) * space  - BASELINE_OFFSET + BASELINE_OFFSET % space;
        if (x % space != 0) {
            x -= x % space;
        }
        scrollTo(0,x);
        computeAndCallback(x);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = startValue ; i < endValue  + 1; i++) {
            int lineHeight = 30;
            if (i % interval == 0) {
                lineHeight = 70;
                 int y = (i - startValue ) * space;
                if (y > 0 || y < height) {
                    canvas.drawText(String.valueOf(i ),0, y +
                            textOffset, txtPaint);
                }
            }else if (i % 5 == 0){
                lineHeight = 50;
            }

            int startY = (i - startValue) * space;
            if (startY > 0 || startY < height) {
                //从控件宽度-linHeight画到控件宽度
                canvas.drawLine(getMeasuredWidth()-lineHeight, startY,getMeasuredWidth()-2,startY, grayLinePaint);
            }
        }
        int startX = BASELINE_OFFSET + getScrollY() - BASELINE_OFFSET % space;
        centerLinePaint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        path.moveTo(getMeasuredWidth(), startX - 10);
        path.lineTo(getMeasuredWidth(), startX + 10);
        path.lineTo(getMeasuredWidth()-15,startX );
        path.close();
        canvas.drawPath(path, centerLinePaint);
        // 空心
        centerLinePaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getY();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isFastScroll = false;
                float moveY = event.getY();
                currentOffset = (int) (moveY - mLastY);
                scrollTo(0, getScrollY() - currentOffset);
                computeAndCallback(getScrollY());
                mLastY = moveY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) mVelocityTracker.getYVelocity();
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    isFastScroll = true;
                    flingY(-initialVelocity);
                } else {
                    int y = getScrollY();
                    if (y % space != 0) {
                        y -= y % space;
                    }
                    if (y < -BASELINE_OFFSET) {
                        y = -BASELINE_OFFSET + BASELINE_OFFSET % space;
                    } else if (y > (endValue - startValue) * space  - BASELINE_OFFSET) {
                        y = (endValue - startValue) * space  - BASELINE_OFFSET + BASELINE_OFFSET % space;
                    }
                    scrollTo(0, y);
                    computeAndCallback(y);
                }
                releaseVelocityTracker();
                break;
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        return true;
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrY();
            scrollTo(0,x);
            computeAndCallback(x);
            postInvalidate();
        } else {
            if (isFastScroll) {
                int x = mScroller.getCurrY() + BASELINE_OFFSET % space;
                if (x % space != 0) {
                    x -= x % space;
                }
                scrollTo(0,x);
                computeAndCallback(x);
                postInvalidate();
            }
        }
    }



    /**
     * 释放 速度追踪器
     */
    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    /**
     * 初始化 速度追踪器
     */
    private void obtainVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /**
     * 惯性滑动
     *
     *
     */
    public void flingY(int velocityY) {
        mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, -BASELINE_OFFSET, (endValue - startValue) * space  - BASELINE_OFFSET);
        awakenScrollBars(mScroller.getDuration());
        invalidate();
    }


    /**
     * 计算并回调位置信息
     *
     *
     */
    private void computeAndCallback(int scrollX) {
        if (mListener != null) {
            int finalX = BASELINE_OFFSET + scrollX;
            if (finalX % space != 0) {
                finalX -= finalX % space;
            }
            mListener.onRulerSelected((endValue - startValue), startValue  + finalX / space);
        }
    }
    public void setRuleListener(AllRulerCallback mListener) {
        this.mListener = mListener;
    }

    /**
     * 设置number的值
     *
     *
     */
    public void setNumber(int number) {
        this.number = number;
    }
    /**
     * 设置刻度尺的最小值
     *
     *
     */
    public void setMin(int min){
        this.startValue = min;
    }

    /**
     * 设置刻度尺的最大值
     *
     *
     */
    public void setMax(int max){
        this.endValue = max;
    }

    /**
     * 设置刻度尺的数字显示间距值
     *
     *
     */
    public void setInterval(int interval){
        this.interval = interval;
    }

    public void setTextOffset(int textOffset){
        this.textOffset = textOffset;
    }
}
