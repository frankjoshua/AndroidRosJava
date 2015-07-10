package com.tesseractmobile.efim.views;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tesseractmobile.efim.R;

public class EyeView extends View {
    
    private final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(final Message msg) {
            animation.start();
        }
        
    };
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
    private final Paint mCenterPaint;
    private final Paint mInnerRingPaint;
    private final Paint mIrisPaint;
    private final Paint mPupilPaint;
    private float mCenterEyeX = 1.0f;
    private float mCenterEyeXSrc;
    private float mCenterEyeXDest;
    private float mCenterEyeYDest;
    private float mCenterEyeYSrc;
    private float mCenterEyeY = 1.0f;
    private float mPupilSize = .26f;
    private float mPupilSizeDst;
    private float mPupilSizeSrc;
    private final ValueAnimator animation;
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mEyeRect.set(0, 0, w, h);
        //Start with the eyes closed
        final int halfHeight = mEyeCanvas.getHeight() / 2;
        mUpperEyelidRect.set(0, -halfHeight, mEyeCanvas.getWidth(), halfHeight);
        mLowerEyelidRect.set(0, halfHeight, mEyeCanvas.getWidth(), mEyeCanvas.getHeight() + halfHeight);
        mUpperEyelidRectDest.set(mUpperEyelidRect);
        mLowerEyelidRectDest.set(mLowerEyelidRect);
        //Setup gradients
        final Shader shader = new LinearGradient(0, 0, w, h, Color.argb(255, 80, 80, 80), Color.argb(255, 0, 0, 0), Shader.TileMode.CLAMP);
        mPupilPaint.setShader(shader);
        //Open the eyes
        open();
        //Look straight ahead
        look(1.0f,1.0f);
    }

    public EyeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final BitmapFactory.Options options = new Options();
        options.inMutable = true;
        mEye = BitmapFactory.decodeResource(getResources(), R.drawable.eye_open, options);
        mEyeCanvas = new Canvas(mEye);

        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.BLACK);
        mEyeLidPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        
        mCenterPaint = new Paint();
        mCenterPaint.setColor(Color.argb(255, 181, 181, 181));// White 45 degree
        
        mInnerRingPaint = new Paint();
        mInnerRingPaint.setColor(Color.BLACK); //Color.argb(255, 72, 72, 72) 45 degree
        mInnerRingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        
        mIrisPaint = new Paint();
        mIrisPaint.setColor(Color.argb(255, 137, 223, 255));
        mIrisPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
  
        mPupilPaint = new Paint();
        mPupilPaint.setColor(Color.argb(255, 80, 80, 80)); //Color.argb(255, 0, 0, 0) -45 degree
        mPupilPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        
        animation = ValueAnimator.ofFloat(0f, 1f);
        animation.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mAnimationPercent = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
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
        //Update pupil
        mCenterEyeX = updateValue(mCenterEyeXSrc, mCenterEyeXDest, mAnimationPercent);
        mCenterEyeY = updateValue(mCenterEyeYSrc, mCenterEyeYDest, mAnimationPercent);
        mPupilSize = updateValue(mPupilSizeSrc, mPupilSizeDst, mAnimationPercent);
        //Redraw main eye
        //mEyeCanvas.drawColor(Color.RED);
        final int cx = mEyeCanvas.getWidth() / 2;
        final int cy = mEyeCanvas.getHeight() / 2;
        mEyeCanvas.drawCircle(cx, cy, mEyeCanvas.getWidth() * 0.5f, mCenterPaint);
        mEyeCanvas.drawCircle(cx * mCenterEyeX, cy * mCenterEyeY, mEyeCanvas.getWidth() * 0.44f, mInnerRingPaint);
        mEyeCanvas.drawCircle(cx * mCenterEyeX, cy * mCenterEyeY, mEyeCanvas.getWidth() * 0.35f, mIrisPaint);
        mEyeCanvas.drawCircle(cx * mCenterEyeX, cy * mCenterEyeY, mEyeCanvas.getWidth() * mPupilSize, mPupilPaint);
        //Draw eye lids
        final int xCenter = cy;
        final int yCenter = cx;
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
    final float updateValue(final float srcValue, final float dstValue, final float percent){
        return srcValue + (dstValue - srcValue) * percent;
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
        animation.setDuration(duration);
        mHandler.sendEmptyMessage(0);
    }

    /**
     * Saves the current positions to use in transforms
     */
    public void saveCurrentEyeLids() {
        mUpperEyelidRectSrc.set(mUpperEyelidRect);
        mLowerEyelidRectSrc.set(mLowerEyelidRect);
        mUpperEyeRotationSrc = mUpperEyeRotation;
        mLowerEyeRotationSrc = mLowerEyeRotation;
        mCenterEyeXSrc = mCenterEyeX;
        mCenterEyeYSrc = mCenterEyeY;
        mPupilSizeSrc = mPupilSize;
    }
    
    /**
     * Open the eye
     */
    public void open() {
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 10;
        mLowerEyelidRectDest.top = (int) Math.round(mEyeCanvas.getHeight() * .9);
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
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 4;
        mLowerEyelidRectDest.top = (int) Math.round(mEyeCanvas.getHeight() * .9);
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
        mPupilSizeDst = 0.1f;
        startAnimation(500);
    }

    public void squintLeft() {
        saveCurrentEyeLids();
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 4;
        mLowerEyelidRectDest.top = (int) Math.round(mEyeCanvas.getHeight() * .8);
        mUpperEyeRotationDest = -30;
        mLowerEyeRotationDest = 0;
        mPupilSizeDst = 0.1f;
        startAnimation(500);
    }
    
    public void wideOpenLeft() {
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 10;
        mLowerEyelidRectDest.top = (int) (mEyeCanvas.getHeight() * .9);
        mUpperEyeRotationDest = -10;
        mLowerEyeRotationDest = 10;
        mPupilSizeDst = 0.2f;
        //Create an animation
        startAnimation(250);
    }
    
    public void wideOpenRight() {
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        mUpperEyelidRectDest.bottom = mEyeCanvas.getHeight() / 10;
        mLowerEyelidRectDest.top = (int) (mEyeCanvas.getHeight() * .9);
        mUpperEyeRotationDest = 10;
        mLowerEyeRotationDest = -10;
        mPupilSizeDst = 0.2f;
        //Create an animation
        startAnimation(250);
    }
    
    public void look(final float x, final float y){
        final float xDest = Math.max(Math.min(x, 1.5f), 0.5f);
        final float yDest = Math.max(Math.min(y, 1.5f), 0.5f);
        //Don't update for small changes
        if(Math.abs(yDest - mCenterEyeYDest) > .05f || Math.abs(xDest - mCenterEyeXDest) > .05f){
            saveCurrentEyeLids();
            mCenterEyeXDest = xDest;
            mCenterEyeYDest = yDest;
            startAnimation(50);
        }
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
