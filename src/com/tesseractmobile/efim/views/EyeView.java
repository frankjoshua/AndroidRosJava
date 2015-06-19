package com.tesseractmobile.efim.views;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tesseractmobile.efim.R;

public class EyeView extends View {
    
    private final Handler mHandler = new Handler();
    private final Bitmap mEyeOpen;
    private final Rect mEyeRect = new Rect();
    private final Rect mUpperEyelidRect = new Rect();
    private final Rect mLowerEyelidRect = new Rect();
    private final Rect mUpperEyelidRectDest = new Rect();
    private final Rect mLowerEyelidRectDest = new Rect();
    private final Rect mUpperEyelidRectSrc = new Rect();
    private final Rect mLowerEyelidRectSrc = new Rect();
    private int mUpperEyeRotation = 0;
    private int mLowerEyeRotation = 0;
    private int mUpperEyeRotationDest = 0;
    private int mLowerEyeRotationDest = 0;
    private int mUpperEyeRotationSrc = 0;
    private int mLowerEyeRotationSrc = 0;
    private final Canvas mEyeCanvas;
    private final Paint mEyeLidPaint;
    private float mAnimationPercent;
    private final Bitmap mEye;
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mEyeRect.set(0, 0, w, h);
        //Start with the eyes closed
        mUpperEyelidRect.set(0, 0, mEyeCanvas.getWidth(), mEyeCanvas.getHeight() / 2);
        mLowerEyelidRect.set(0, mEyeCanvas.getHeight() / 2, mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        open();
    }

    public EyeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final BitmapFactory.Options options = new Options();
        //options.inMutable = true;
        mEyeOpen = BitmapFactory.decodeResource(getResources(), R.drawable.eye_open, options);
        mEye = BitmapFactory.decodeResource(getResources(), R.drawable.eye_open, options);
        mEyeCanvas = new Canvas(mEye);
        //mEyeCanvas.drawColor(Color.RED);
        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.BLACK);
        mEyeLidPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        
    }

    /**
     * Redraws the eye with the current animation settings
     */
    private void updateEye() {
        //Update Top Eyelid
        final int bottom = updateValue(mUpperEyelidRectSrc.bottom, mUpperEyelidRectDest.bottom, mAnimationPercent);
        mUpperEyelidRect.set(mUpperEyelidRectDest.left, mUpperEyelidRectDest.top, mUpperEyelidRectDest.right, bottom);
        mUpperEyeRotation = updateValue(mUpperEyeRotationSrc, mUpperEyeRotationDest, mAnimationPercent);
        //Update Lower EyeLid
        final int top = updateValue(mLowerEyelidRectSrc.top, mLowerEyelidRectDest.top, mAnimationPercent);
        mLowerEyelidRect.set(mLowerEyelidRectDest.left, top, mLowerEyelidRectDest.right, mLowerEyelidRectDest.bottom);
        mLowerEyeRotation = updateValue(mLowerEyeRotationSrc, mLowerEyeRotationDest, mAnimationPercent);
        //Redraw main eye
        mEyeCanvas.drawBitmap(mEyeOpen, 0, 0, null);
        //Draw eye lids
        final int xCenter = mEyeCanvas.getWidth() / 2;
        final int yCenter = mEyeCanvas.getHeight() / 2;
        mEyeCanvas.rotate(mUpperEyeRotation, xCenter, yCenter);
        mEyeCanvas.drawRect(mUpperEyelidRect, mEyeLidPaint);
        mEyeCanvas.rotate(mLowerEyeRotation - mUpperEyeRotation, xCenter, yCenter);
        mEyeCanvas.drawRect(mLowerEyelidRect, mEyeLidPaint);
        mEyeCanvas.rotate(-mLowerEyeRotation, xCenter, yCenter);
    }
 
    /**
     * Transform a value between a source and destination
     * @param srcValue
     * @param dstValue
     * @param percent
     * @return
     */
    final int updateValue(final int srcValue, final int dstValue, final float percent){
        return Math.round(srcValue + (dstValue - srcValue) * percent);
    }
    
    @Override
    protected void onDraw(final Canvas canvas) {
        //Draw the eye
        canvas.drawBitmap(mEye, null, mEyeRect, null);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            //squint();
            break;
        case MotionEvent.ACTION_MOVE:
            //blink();
            break;
        case MotionEvent.ACTION_UP:
            //blink();
            break;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Blink the eye
     * Must be called from the UI Thread
     */
    public void blink(){
        close();
        postDelayed(new Runnable() {
            
            @Override
            public void run() {
                open();
            }
        }, 250);
    }
    
    public void close(){
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        final int center = (int) (mEyeCanvas.getHeight() * .6);
        mUpperEyelidRectDest.set(0, 0, mEyeCanvas.getWidth(), center);
        mLowerEyelidRectDest.set(0, center, mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        //Create animation
        startAnimation(250);
    }

    /**
     * 
     */
    public void startAnimation(final int duration) {
        final ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(duration);
        animation.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mAnimationPercent = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animation.start();
    }

    /**
     * Saves the current positions to use in transforms
     */
    public void saveCurrentEyeLids() {
        mUpperEyelidRectSrc.set(mUpperEyelidRect);
        mLowerEyelidRectSrc.set(mLowerEyelidRect);
        mUpperEyeRotationSrc = mUpperEyeRotation;
        mLowerEyeRotationSrc = mLowerEyeRotation;
    }
    
    /**
     * Open the eye
     */
    public void open() {
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        mUpperEyelidRectDest.set(0, 0, mEyeCanvas.getWidth(), mEyeCanvas.getHeight() / 10);
        mLowerEyelidRectDest.set(0, (int) (mEyeCanvas.getHeight() * .9), mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        mUpperEyeRotationDest = 0;
        mLowerEyeRotationDest = 0;
        //Create an animation
        startAnimation(250);
    }
    
    /**
     * Squint the eye
     */
    public void squint() {
        saveCurrentEyeLids();
        mUpperEyelidRectDest.set(0, 0, mEyeCanvas.getWidth(), ((mEyeCanvas.getHeight() / 4)));
        mLowerEyelidRectDest.set(0, (int) Math.round(mEyeCanvas.getHeight() * .9), mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        mUpperEyeRotationDest = 0;
        mLowerEyeRotationDest = 0;
        startAnimation(1000);
    }

    public void squintRight() {
        saveCurrentEyeLids();
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 4;
        mLowerEyelidRectDest.top = (int) Math.round(mEyeCanvas.getHeight() * .8);
        mUpperEyeRotationDest = 30;
        mLowerEyeRotationDest = 0;
        startAnimation(500);
    }

    public void squintLeft() {
        saveCurrentEyeLids();
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 4;
        mLowerEyelidRectDest.top = (int) Math.round(mEyeCanvas.getHeight() * .8);
        mUpperEyeRotationDest = -30;
        mLowerEyeRotationDest = 0;
        startAnimation(500);
    }
    
    @Override
    public void invalidate() {
        updateEye();
        super.invalidate();
    }

    /**
     * @param eyePosition
     */
    protected void postDelayed(final int eyePosition, final int delay) {
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                setBackgroundResource(eyePosition);
            }
        }, delay);
    }

    

}
