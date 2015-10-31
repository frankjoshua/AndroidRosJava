package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by josh on 10/25/2015.
 */
public class JoystickView extends View {

    private Paint mCirclePaint;
    private Paint mTouchPaint;
    private Point mCenterPoint;
    private Point mTouchPoint;
    private int mSizeCircle;
    private int mSizeTouch;

    /** true if the user is touching */
    private boolean mHasFocus;

    private JoystickListener mJoystickListener;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.parseColor("#bcb9e5"));
        mTouchPaint = new Paint();
        mTouchPaint.setColor(Color.parseColor("#e59100"));

        mCenterPoint = new Point();
        mTouchPoint = new Point();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterPoint.set(w / 2, h / 2);
        mTouchPoint.set(w / 2, h / 2);
        mSizeCircle = Math.min(w, h) / 2;
        mSizeTouch = Math.min(w, h) / 10;

        int[] circleColors = new int[]{
                Color.parseColor("#bcb9e5"),
                Color.parseColor("#dddbe5"),
                Color.parseColor("#b3a2c1")
        };
        final Shader circleShader = new LinearGradient(0, 0, w, h, circleColors, null, Shader.TileMode.CLAMP);
        mCirclePaint.setShader(circleShader);

        int[] touchColors = new int[]{
                Color.parseColor("#e59100"),
                Color.parseColor("#f2d7a8"),
                Color.parseColor("#e09900")
        };
        final Shader touchShader = new LinearGradient(0, 0, w, h, touchColors, null, Shader.TileMode.CLAMP);
        mTouchPaint.setShader(touchShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mSizeCircle, mCirclePaint);
        canvas.drawCircle(mTouchPoint.x, mTouchPoint.y, mSizeTouch, mTouchPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
            mTouchPoint.set(Math.round(event.getX()), Math.round(event.getY()));
            update();
            setHasFocus(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            mTouchPoint.set(mCenterPoint.x, mCenterPoint.y);
            update();
            setHasFocus(false);
        }
        invalidate();
        return true;
    }

    private void update() {
        float cx = mTouchPoint.x / (float) getWidth();
        float cy = mTouchPoint.y / (float) getHeight();

        float x = cx * 2 - 1;
        float y = -(cy * 2 - 1);

        mJoystickListener.onPositionChange(x, y, 0);
    }

    public void setJoystickListener(final JoystickListener joystickListener){
        mJoystickListener = joystickListener;
    }

    private void setHasFocus(boolean hasFocus) {
        if(this.mHasFocus != hasFocus){
            this.mHasFocus = hasFocus;
            mJoystickListener.onFocusChange(hasFocus);
        }
    }

    public interface JoystickListener {
        /**
         * Called with the position of the joystick is updated
         * @param x -1.0 is far left, 1.0 is far right, 0.0 is center
         * @param y -1.0 is down, 1.0 is up, 0.0 is center
         */
        void onPositionChange(float x, float y, float z);

        /**
         * True when user is touching false when they let go
         * @param hasFocus
         */
        void onFocusChange(final boolean hasFocus);
    }
}
