package com.android.support.components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * Panel background: a dark blue gradient with two soft "aurora" lights in opposite corners,
 * a faint dot grid and a sky to indigo gradient border.
 */
@Obfuscate
public class PanelBackground extends Drawable {

    private final float radius, strokeWidth, dotSpacing, dotRadius;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowA = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowB = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dots = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path();
    private final RectF rect = new RectF();

    public PanelBackground(float radiusPx, float strokePx, float dotSpacingPx) {
        radius = radiusPx;
        strokeWidth = strokePx;
        dotSpacing = dotSpacingPx;
        dotRadius = Math.max(1f, strokePx * 0.8f);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(strokePx);
        dots.setColor(0x0F8DB4FF);
    }

    @Override
    protected void onBoundsChange(Rect b) {
        float w = b.width(), h = b.height();
        fill.setShader(new LinearGradient(b.left, b.top, b.left, b.bottom, Theme.PANEL_BG, Theme.PANEL_BG_END, Shader.TileMode.CLAMP));
        glowA.setShader(new RadialGradient(b.right - w * 0.1f, b.top, Math.max(1f, w * 0.55f),
                Theme.PANEL_GLOW, Theme.PANEL_GLOW & 0x00FFFFFF, Shader.TileMode.CLAMP));
        glowB.setShader(new RadialGradient(b.left, b.bottom, Math.max(1f, w * 0.45f),
                (Theme.ACCENT_DEEP & 0x00FFFFFF) | 0x2E000000, Theme.ACCENT_DEEP & 0x00FFFFFF, Shader.TileMode.CLAMP));
        stroke.setShader(new LinearGradient(b.left, b.top, b.right, b.bottom,
                new int[]{Theme.ACCENT_LIGHT, Theme.PANEL_STROKE & 0x33FFFFFF, Theme.ACCENT_DEEP},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));

        float half = strokeWidth / 2f;
        rect.set(b.left + half, b.top + half, b.right - half, b.bottom - half);
        clip.reset();
        clip.addRoundRect(rect, radius, radius, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        canvas.drawPath(clip, fill);

        int save = canvas.save();
        canvas.clipPath(clip);
        canvas.drawRect(b, glowA);
        canvas.drawRect(b, glowB);
        for (float y = b.top + dotSpacing; y < b.bottom; y += dotSpacing) {
            for (float x = b.left + dotSpacing; x < b.right; x += dotSpacing) {
                canvas.drawCircle(x, y, dotRadius, dots);
            }
        }
        canvas.restoreToCount(save);

        canvas.drawRoundRect(rect, radius, radius, stroke);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fill.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
