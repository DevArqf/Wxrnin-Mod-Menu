package com.android.support.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.os.SystemClock;
import android.widget.TextView;

import org.lsposed.lsparanoid.Obfuscate;

/** A TextView whose text is painted with a slowly sweeping blue gradient. */
@Obfuscate
public class ShimmerText extends TextView {

    private static final long CYCLE_MS = 3600;

    private final Matrix matrix = new Matrix();
    private LinearGradient shader;
    private float span;

    public ShimmerText(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        span = Math.max(1f, getPaint().measureText(String.valueOf(getText())) * 2f);
        shader = new LinearGradient(0, 0, span, 0,
                new int[]{Theme.ACCENT_LIGHT, Theme.TEXT, Theme.ACCENT, Theme.ACCENT_DEEP, Theme.ACCENT_LIGHT},
                null, Shader.TileMode.REPEAT);
        getPaint().setShader(shader);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        // rebuild the gradient for the new text width
        if (getWidth() > 0) onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shader != null) {
            matrix.setTranslate((SystemClock.uptimeMillis() % CYCLE_MS) / (float) CYCLE_MS * span, 0);
            shader.setLocalMatrix(matrix);
        }
        super.onDraw(canvas);
        if (isShown()) postInvalidateOnAnimation();
    }
}
