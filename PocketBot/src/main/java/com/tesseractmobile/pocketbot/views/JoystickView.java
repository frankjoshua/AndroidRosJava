package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by josh on 10/25/2015.
 */
public class JoystickView extends View {

    //Main circle
    private TouchPad mainPad;
    //Center Circle
    private TouchCircle touchCircle;
    //Toggle Circle
    private TouchCircle toggleCircle;

    /** true if the user is touching */
    private boolean mHasFocus;
    /** if true joystick will hold position */
    private boolean mSticky = false;

    private boolean mChangingState = false;

    private JoystickListener mJoystickListener;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainPad = new TouchPad(Color.parseColor("#bcb9e5"), 150);
        touchCircle = new TouchCircle(Color.parseColor("#e59100"), 200);
        toggleCircle = new TouchCircle(Color.BLUE, 100);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int size = Math.min(w, h) / 10;
        mainPad.set(w / 2, h / 2);
        touchCircle.set(w / 2, h / 2);

        toggleCircle.set(size, h - size);

        toggleCircle.setSize(size);
        mainPad.setSize(Math.min(w, h) / 2);

        touchCircle.setSize(Math.round(size * 1.5f));

        int[] circleColors = new int[]{
                Color.parseColor("#bcb9e5"),
                Color.parseColor("#dddbe5"),
                Color.parseColor("#b3a2c1")
        };
        final Shader circleShader = new LinearGradient(0, 0, w, h, circleColors, null, Shader.TileMode.CLAMP);
        mainPad.setShader(circleShader);

        int[] touchColors = new int[]{
                Color.parseColor("#e59100"),
                Color.parseColor("#f2d7a8"),
                Color.parseColor("#e09900")
        };
        final Shader touchShader = new LinearGradient(0, 0, w, h, touchColors, null, Shader.TileMode.CLAMP);
        touchCircle.setShader(touchShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mainPad.draw(canvas);
        toggleCircle.draw(canvas);
        touchCircle.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && toggleCircle.isTouched(Math.round(event.getX()), Math.round(event.getY()))){
            mSticky = !mSticky;
            toggleCircle.toggle(mSticky);
            mChangingState = true;
        } else if(mChangingState == false && (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)){
            touchCircle.set(Math.round(event.getX()), Math.round(event.getY()));
            update();
            setHasFocus(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            if(!mSticky) {
                touchCircle.set(mainPad.point.x, mainPad.point.y);
                update();
                setHasFocus(false);
            }
            mChangingState = false;
        }
        invalidate();
        return true;
    }

    private void update() {
        float cx = touchCircle.point.x / (float) getWidth();
        float cy = touchCircle.point.y / (float) getHeight();

        float x = cx * 2 - 1;
        float y = -(cy * 2 - 1);

        //Constrain values between -1 and 1
        x = Math.max(-1.0f, Math.min(1.0f, x));
        y = Math.max(-1.0f, Math.min(1.0f, y));

        mJoystickListener.onPositionChange(this, x, y, mHasFocus ? 1.0f : 0.0f, false, false);
    }

    public void setJoystickListener(final JoystickListener joystickListener){
        mJoystickListener = joystickListener;
    }

    private void setHasFocus(boolean hasFocus) {
        if(this.mHasFocus != hasFocus){
            this.mHasFocus = hasFocus;
            mJoystickListener.onFocusChange(this, hasFocus);
        }
    }

    private static class TouchCircle {
        public Paint paint = new Paint();
        public Point point = new Point();
        public int circleSize;

        public TouchCircle(final int color, final int alpha){
            paint.setColor(color);
            paint.setAlpha(alpha);
        }

        public void set(int x, int y) {
            point.set(x, y);
        }

        public void setSize(int size) {
            circleSize = size;
        }

        public void setShader(Shader shader) {
            paint.setShader(shader);
        }

        public void draw(Canvas canvas) {
            canvas.drawCircle(point.x, point.y, circleSize, paint);
        }

        public boolean isTouched(int x, int y) {
            final double distance = Math.sqrt(Math.pow((x - point.x), 2) + Math.pow((y - point.y), 2));
            return distance <= circleSize;
        }

        public void toggle(final boolean sticky){
            int alpha = paint.getAlpha();
            if(sticky){
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.BLUE);
            }
            paint.setAlpha(alpha);
        }
    }

    public interface JoystickListener {
        /**
         * Called with the position of the joystick is updated
         * @param x -1.0 is far left, 1.0 is far right, 0.0 is center
         * @param y -1.0 is down, 1.0 is up, 0.0 is center
         */
        void onPositionChange(final JoystickView joystickView, float x, float y, float z, boolean a, boolean b);

        /**
         * True when user is touching false when they let go
         * @param hasFocus
         */
        void onFocusChange(final JoystickView joystickView, final boolean hasFocus);
    }

    private class TouchPad {
        public Paint paint = new Paint();
        public RectF rect = new RectF();
        public Point point = new Point();

        public TouchPad(final int color, final int alpha) {
            paint.setColor(color);
            paint.setAlpha(alpha);
        }

        public void set(final int x, final int y) {
            point.set(x, y);
        }

        public void setSize(final int size) {
            rect.set(point.x - size, point.y - size, point.x + size, point.y + size);
        }

        public void setShader(final Shader circleShader) {
            paint.setShader(circleShader);
        }

        public void draw(final Canvas canvas) {
            canvas.drawRoundRect(rect, rect.width() / 10, rect.width() / 10, paint);
        }
    }
}
