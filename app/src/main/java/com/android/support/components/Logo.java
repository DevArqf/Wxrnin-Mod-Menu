package com.android.support.components;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.lsposed.lsparanoid.Obfuscate;

/**
 * The menu logo, from the base64 returned by Setup.cpp's Icon().
 * PNG and JPG are still images, a GIF plays in a loop (bubble, header and toast all animate).
 */
@Obfuscate
public class Logo {

    private static final String TAG = "Wxrnin";

    private final Bitmap bitmap;
    private final Movie movie;     // null for still images
    private final Canvas canvas;
    private int lastTime = -1;
    private long version;          // bumped every time the frame changes

    private Logo(Bitmap bitmap, Movie movie) {
        this.bitmap = bitmap;
        this.movie = movie;
        this.canvas = movie != null ? new Canvas(bitmap) : null;
        update();
    }

    /** Returns null when the string is empty or not a valid image. */
    @SuppressWarnings("deprecation")
    public static Logo decode(String base64) {
        if (TextUtils.isEmpty(base64)) return null;
        int comma = base64.indexOf(',');
        if (base64.startsWith("data:") && comma >= 0) base64 = base64.substring(comma + 1);
        byte[] bytes;
        try {
            bytes = Base64.decode(base64, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Logo is not valid base64, using text instead", e);
            return null;
        }

        if (isGif(bytes)) {
            Movie movie = Movie.decodeByteArray(bytes, 0, bytes.length);
            if (movie != null && movie.width() > 0 && movie.height() > 0) {
                if (movie.duration() > 0) {
                    return new Logo(Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888), movie);
                }
                // a single frame GIF, fall through to the still image decoder
            }
        }
        Bitmap still = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (still == null) {
            Log.e(TAG, "Logo is not a valid image, using text instead");
            return null;
        }
        return new Logo(still, null);
    }

    private static boolean isGif(byte[] b) {
        return b.length > 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
    }

    public boolean isAnimated() {
        return movie != null;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    /** The current frame. For a GIF the same bitmap is redrawn by update(). */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /** Advances a GIF to the current time. Returns true when the frame changed. */
    @SuppressWarnings("deprecation")
    public boolean update() {
        if (movie == null) return false;
        int time = (int) (SystemClock.uptimeMillis() % movie.duration());
        if (time == lastTime) return false;
        lastTime = time;
        movie.setTime(time);
        bitmap.eraseColor(0);
        movie.draw(canvas, 0, 0);
        version++;
        return true;
    }

    /** Counter that changes with every new frame, so views know when to refresh their shader. */
    public long getVersion() {
        return version;
    }

    /** A fresh shader for the current frame (hardware canvases cache the old pixels otherwise). */
    public BitmapShader newShader() {
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }
}
