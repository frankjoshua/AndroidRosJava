package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

/**
 * Created by josh on 1/27/2016.
 */
public class CartoonEyeView extends EyeView {
    public CartoonEyeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getEyelidColor() {
        return Color.parseColor("#7A5B78");
    }

    @Override
    protected int getIrisColor() {
        return Color.parseColor("#A8D4A9");
    }

    protected LinearGradient getEyeBallGradient(final int w, final int h){
        return new LinearGradient(0, 0, w, h, Color.parseColor("#CdB6B6"), Color.parseColor("#ffffff"), Shader.TileMode.CLAMP);
    }

    protected LinearGradient getEyeOuterGradient(final int w, final int h){
        return new LinearGradient(0, 0, w, h, Color.parseColor("#9BC7A0"), Color.parseColor("#9BC7AC"), Shader.TileMode.CLAMP);
    }
}
