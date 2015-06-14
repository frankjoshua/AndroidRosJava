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
        options.inMutable = true;
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
        //Update Lower EyeLid
        final int top = updateValue(mLowerEyelidRectSrc.top, mLowerEyelidRectDest.top, mAnimationPercent);
        mLowerEyelidRect.set(mLowerEyelidRectDest.left, top, mLowerEyelidRectDest.right, mLowerEyelidRectDest.bottom);
        //Redraw main eye
        mEyeCanvas.drawBitmap(mEyeOpen, 0, 0, null);
        //Draw eye lids
        mEyeCanvas.drawRect(mUpperEyelidRect, mEyeLidPaint);
        mEyeCanvas.drawRect(mLowerEyelidRect, mEyeLidPaint);
    }
 
    final int updateValue(final int srcValue, final int dstValue, final float percent){
        return Math.round(srcValue + (dstValue - srcValue) * percent);
    }
    
    @Override
    protected void onDraw(final Canvas canvas) {
        
        canvas.drawBitmap(mEye, null, mEyeRect, null);
        //super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            squint();
            break;
        case MotionEvent.ACTION_MOVE:
            //blink();
            break;
        case MotionEvent.ACTION_UP:
            blink();
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
        }, 2000);
    }
    
    public void close(){
        //Save current position
        saveCurrentEyeLids();
        //Set destination position
        mUpperEyelidRectDest.set(0, 0, mEyeCanvas.getWidth(), mEyeCanvas.getHeight() / 2);
        mLowerEyelidRectDest.set(0, mEyeCanvas.getHeight() / 2, mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        //Create animation
        final ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(1000);
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
     * 
     */
    public void saveCurrentEyeLids() {
        mUpperEyelidRectSrc.set(mUpperEyelidRect);
        mLowerEyelidRectSrc.set(mLowerEyelidRect);
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
        final ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(1000);
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
     * Squint the eye
     */
    public void squint() {
        saveCurrentEyeLids();
        mUpperEyelidRectDest.set(0, 0, mEyeCanvas.getWidth(), ((mEyeCanvas.getHeight() / 4)));
        mLowerEyelidRectDest.set(0, (int) (mEyeCanvas.getHeight() * .9), mEyeCanvas.getWidth(), mEyeCanvas.getHeight());
        final ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(1000);
        animation.addUpdateListener(new AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mAnimationPercent = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animation.start();
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
