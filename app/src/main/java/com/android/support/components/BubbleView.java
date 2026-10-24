package com.android.support.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * Floating button: the logo in a dark blue disc, wrapped by a slowly spinning gradient ring
 * and a soft breathing glow. Also used as the small avatar in the panel header.
 * A GIF logo plays in both.
 */
@Obfuscate
public class BubbleView extends View {

    private static final long SPIN_MS = 4000, BREATHE_MS = 2400;

    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix matrix = new Matrix();
    private final Matrix spin = new Matrix();
    private final String text;
    private final Logo logo;
    private final int sizeDp;
    private final boolean animated;
    private SweepGradient sweep;
    private long shownVersion = -1;

    /** Floating bubble. logo may be null, then the text is drawn. */
    public BubbleView(Context context, Typeface typeface, String text, Logo logo) {
        this(context, typeface, text, logo, Theme.BUBBLE_SIZE, true);
    }

    /** sizeDp: diameter, animated: spinning ring and glow. */
    public BubbleView(Context context, Typeface typeface, String text, Logo logo, int sizeDp, boolean animated) {
        super(context);
        this.text = text;
        this.logo = logo;
        this.sizeDp = sizeDp;
        this.animated = animated;
        background.setColor(Theme.CARD_BG | 0xFF000000);
        ring.setStyle(Paint.Style.STROKE);
        label.setColor(Theme.TEXT);
        label.setTypeface(Typeface.create(typeface, Typeface.BOLD_ITALIC));
        label.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = dp(sizeDp);
        setMeasuredDimension(size, size);
    }

    private float ringWidth() {
        return Math.max(dp(1.5f), getWidth() * 0.045f);
    }

    /** Space between the edge and the disc, used by the glow. */
    private float glowSpace() {
        return animated ? getWidth() * 0.08f : 0f;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float c = w / 2f;
        ring.setStrokeWidth(ringWidth());
        sweep = new SweepGradient(c, c,
                new int[]{Theme.ACCENT_LIGHT, Theme.ACCENT, Theme.ACCENT_DEEP, Theme.ACCENT_LIGHT & 0x40FFFFFF, Theme.ACCENT_LIGHT},
                new float[]{0f, 0.3f, 0.6f, 0.85f, 1f});
        ring.setShader(sweep);
        glow.setShader(new RadialGradient(c, c, c, new int[]{Theme.ACCENT, Theme.ACCENT, Theme.ACCENT & 0x00FFFFFF},
                new float[]{0f, 0.72f, 1f}, Shader.TileMode.CLAMP));

        if (logo == null) return;
        // center-crop the logo into the inner disc
        float disc = (c - glowSpace() - ringWidth() * 1.6f) * 2f;
        float scale = Math.max(disc / logo.getWidth(), disc / logo.getHeight());
        matrix.setScale(scale, scale);
        matrix.postTranslate(c - logo.getWidth() * scale / 2f, c - logo.getHeight() * scale / 2f);
        shownVersion = -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float s = getWidth(), c = s / 2f;
        long now = SystemClock.uptimeMillis();
        float outer = c - glowSpace();

        if (animated) {
            float breathe = (float) (0.5 + 0.5 * Math.sin(now % BREATHE_MS / (double) BREATHE_MS * Math.PI * 2));
            glow.setAlpha((int) (40 + 60 * breathe));
            canvas.drawCircle(c, c, c, glow);
            spin.setRotate(now % SPIN_MS / (float) SPIN_MS * 360f, c, c);
            sweep.setLocalMatrix(spin);
        }

        canvas.drawCircle(c, c, outer, background);
        float disc = outer - ringWidth() * 1.6f;
        if (logo != null) {
            logo.update();
            if (shownVersion != logo.getVersion() || image.getShader() == null) {
                shownVersion = logo.getVersion();
                Shader shader = logo.newShader();
                shader.setLocalMatrix(matrix);
                image.setShader(shader);
            }
            canvas.drawCircle(c, c, disc, image);
        } else {
            label.setTextSize(s * 0.36f);
            float baseline = c - (label.descent() + label.ascent()) / 2f;
            canvas.drawText(text, c, baseline, label);
        }
        canvas.drawCircle(c, c, outer - ringWidth() / 2f, ring);

        boolean moving = animated || (logo != null && logo.isAnimated());
        if (moving && isShown()) postInvalidateOnAnimation();
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
