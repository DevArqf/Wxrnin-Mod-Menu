package com.android.support.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * Toast with an animated rainbow text and border, drawn in its own overlay window.
 * Without the overlay permission it falls back to a system toast with rainbow colored letters.
 * Native code calls it through Menu::showToast.
 */
@Obfuscate
public final class RainbowToast {

    private static final String TAG = "Wxrnin";
    private static final long SHORT_MS = 2000, LONG_MS = 3500;

    private static final Handler main = new Handler(Looper.getMainLooper());
    private static Logo icon;
    private static ToastView current;
    private static WindowManager currentManager;

    private RainbowToast() {
    }

    /** Image drawn on the left of the toast, the menu sets it to the bubble icon. */
    public static void setIcon(Logo logo) {
        icon = logo;
    }

    /** length: 0 short, 1 long (same as Toast.LENGTH_SHORT / LENGTH_LONG). Safe to call from any thread. */
    public static void show(Context context, String text, int length) {
        if (context == null || text == null) return;
        String plain = Ui.html(text).toString().trim();
        long duration = length == Toast.LENGTH_LONG ? LONG_MS : SHORT_MS;
        main.post(() -> {
            if (canDrawOverlays(context)) {
                try {
                    showOverlay(context, plain, duration);
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "Rainbow toast overlay failed, using a system toast", e);
                }
            }
            Toast.makeText(context, rainbowLetters(plain), length == Toast.LENGTH_LONG ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        });
    }

    private static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    @SuppressWarnings("deprecation")
    private static void showOverlay(Context context, String text, long duration) {
        dismissCurrent();

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = dp(context, 72);

        ToastView view = new ToastView(context, text, icon);
        manager.addView(view, params);
        current = view;
        currentManager = manager;

        view.setAlpha(0f);
        view.setTranslationY(dp(context, 18));
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        view.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setDuration(260)
                .setInterpolator(new DecelerateInterpolator(1.6f)).start();

        view.postDelayed(() -> view.animate().alpha(0f).translationY(dp(context, 10)).setDuration(220)
                .withEndAction(() -> remove(manager, view)).start(), duration);
    }

    private static void dismissCurrent() {
        if (current != null) {
            current.animate().cancel();
            remove(currentManager, current);
        }
    }

    private static void remove(WindowManager manager, View view) {
        if (view == current) {
            current = null;
            currentManager = null;
        }
        try {
            if (view.getParent() != null) manager.removeViewImmediate(view);
        } catch (Exception ignored) {
        }
    }

    private static int rainbow(float hue) {
        return Color.HSVToColor(new float[]{((hue % 360f) + 360f) % 360f, 0.62f, 1f});
    }

    private static CharSequence rainbowLetters(String text) {
        SpannableString span = new SpannableString(text);
        int letters = Math.max(1, text.length());
        for (int i = 0; i < text.length(); i++) {
            span.setSpan(new ForegroundColorSpan(rainbow(i * 300f / letters)), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        span.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private static int dp(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    /** The pill: navy background, glowing rainbow border, rainbow text and an optional round icon. */
    private static class ToastView extends View {
        private static final long CYCLE_MS = 2200; // one full trip of the rainbow

        private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint imageBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Matrix matrix = new Matrix();
        private final Matrix shaderMatrix = new Matrix();
        private final RectF rect = new RectF();
        private final Logo icon;
        private long shownVersion = -1;
        private final String fullText;
        private String text;

        private final int padH, padV, iconSize, gap;
        private LinearGradient textShader, borderShader;
        private float gradientWidth;

        ToastView(Context context, String text, Logo icon) {
            super(context);
            this.fullText = text;
            this.text = text;
            this.icon = icon;
            padH = dp(context, 18);
            padV = dp(context, 11);
            iconSize = icon != null ? dp(context, 24) : 0;
            gap = icon != null ? dp(context, 10) : 0;

            background.setColor(Theme.PANEL_BG);
            imageBg.setColor(Theme.CARD_BG | 0xFF000000); // same navy as the bubble, a GIF logo is drawn on it
            glow.setStyle(Paint.Style.STROKE);
            glow.setStrokeWidth(dp(context, 4));
            glow.setAlpha(70);
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(dp(context, 1.6f));
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.5f, context.getResources().getDisplayMetrics()));
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            textPaint.setLetterSpacing(0.03f);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int maxWidth = getResources().getDisplayMetrics().widthPixels - dp(getContext(), 32);
            int maxText = Math.max(0, maxWidth - padH * 2 - iconSize - gap);
            text = TextUtils.ellipsize(fullText, new android.text.TextPaint(textPaint), maxText, TextUtils.TruncateAt.END).toString();
            int textWidth = (int) Math.ceil(textPaint.measureText(text));
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            int content = Math.max(iconSize, (int) Math.ceil(fm.descent - fm.ascent));
            setMeasuredDimension(padH * 2 + iconSize + gap + textWidth, padV * 2 + content);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int[] colors = new int[8];
            for (int i = 0; i < colors.length; i++) colors[i] = rainbow(i * 360f / (colors.length - 1));
            gradientWidth = Math.max(w, dp(getContext(), 160));
            textShader = new LinearGradient(0, 0, gradientWidth, 0, colors, null, Shader.TileMode.REPEAT);
            borderShader = new LinearGradient(0, 0, gradientWidth, 0, colors, null, Shader.TileMode.REPEAT);
            textPaint.setShader(textShader);
            border.setShader(borderShader);
            glow.setShader(borderShader);

            if (icon != null) {
                float scale = Math.max(iconSize / (float) icon.getWidth(), iconSize / (float) icon.getHeight());
                matrix.setScale(scale, scale);
                matrix.postTranslate(padH + (iconSize - icon.getWidth() * scale) / 2f, (h - iconSize) / 2f + (iconSize - icon.getHeight() * scale) / 2f);
                shownVersion = -1;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight(), r = h / 2f;
            float shift = (SystemClock.uptimeMillis() % CYCLE_MS) / (float) CYCLE_MS * gradientWidth;

            shaderMatrix.setTranslate(shift, 0);
            textShader.setLocalMatrix(shaderMatrix);
            shaderMatrix.setTranslate(-shift, 0);
            borderShader.setLocalMatrix(shaderMatrix);

            float inset = glow.getStrokeWidth() / 2f;
            rect.set(inset, inset, w - inset, h - inset);
            canvas.drawRoundRect(rect, r, r, background);
            canvas.drawRoundRect(rect, r - inset, r - inset, glow);
            canvas.drawRoundRect(rect, r - inset, r - inset, border);

            if (icon != null) {
                icon.update();
                if (shownVersion != icon.getVersion() || image.getShader() == null) {
                    shownVersion = icon.getVersion();
                    Shader shader = icon.newShader();
                    shader.setLocalMatrix(matrix);
                    image.setShader(shader);
                }
                float cx = padH + iconSize / 2f, cy = h / 2f;
                canvas.drawCircle(cx, cy, iconSize / 2f, imageBg);
                canvas.drawCircle(cx, cy, iconSize / 2f, image);
            }

            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = h / 2f - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(text, padH + iconSize + gap, baseline, textPaint);

            if (isAttachedToWindow()) postInvalidateOnAnimation();
        }
    }
}
