package com.waz.deanscool.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.waz.deanscool.R;

import static java.lang.Math.floor;

public class CardView extends View {

    private static final String TAG = CardView.class.getName();

    private static final int DEFAULT_INITIAL_ROTATION = 0; //degrees

    /**
     * How much the height varies as a result of perspective
     */
    private static final double MIN_HEIGHT_AS_RATIO_OF_NORMAL = 0.7;
    private static final long ANIMATION_DURATION = 3000;


    private Bitmap frontBitmap;
    private int frontColor;
    private Bitmap backBitmap;
    private int backColor;
    private int zAxisRot;

    private Paint fillPaint;

    public CardView(Context context) {
        super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.CardView, 0, 0);

        try {
            backColor = a.getColor(R.styleable.CardView_background_back, R.color.white);
            frontColor = a.getColor(R.styleable.CardView_background_front, R.color.black);
            zAxisRot = a.getInt(R.styleable.CardView_initial_rotation, DEFAULT_INITIAL_ROTATION);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        fillPaint = new Paint();

    }

    public int getZAxisRot() {
        return zAxisRot;
    }

    public void setZAxisRot(int rotation) {
        if (rotation > 360) {
            rotation = rotation - 360;
        }
        zAxisRot = rotation;
        invalidate();
    }

    public void startYRotation() {
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "zAxisRot", 0, 360);
        animator.setDuration(ANIMATION_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap currentBackground = getCurrentBackground(canvas);

        Camera camera = new Camera();
        camera.rotateZ(45);
        camera.rotateY(zAxisRot);
        Matrix matrix = new Matrix();
        camera.getMatrix(matrix);

        matrix.preTranslate(-canvas.getWidth() / 2, -canvas.getHeight() / 2);
        matrix.postTranslate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        //Not too sure why putting the matrix directly into here worked...
        canvas.drawBitmap(currentBackground, matrix, fillPaint);

    }

    private Bitmap getCurrentBackground(Canvas canvas) {
        if (backBitmap == null) {
            backBitmap = setBackgroundBitmap(R.drawable.minion2, canvas.getWidth(), canvas
                    .getHeight());
        }
        if (frontBitmap == null) {
            frontBitmap = setBackgroundBitmap(R.drawable.minion1, canvas.getWidth(), canvas
                    .getHeight());
        }

        Bitmap currentBackground;
        if (zAxisRot > 90 && zAxisRot <= 270) {
            currentBackground = backBitmap;
        } else {
            currentBackground = frontBitmap;
        }
        return currentBackground;
    }

    private Bitmap setBackgroundBitmap(int resourceId, int reqWidth, int reqHeight) {
        Bitmap bitmap = decodeBitmap(resourceId, reqWidth, reqHeight);
        bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
        return bitmap;
    }

    private Bitmap decodeBitmap(int resourceId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = getBitmapOptions(resourceId);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        //allows resizing of the bitmaps
        options.inMutable = true;

        //since the images are not transparent.
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //If this is not set, then the images will be scaled up to match the density of the device
        //(scaled 2 or 3 times!). Alternatively, the image can be placed in a drawable-nodpi folder
        options.inScaled = false;

        Bitmap loadedBitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);
        Log.d(TAG, "Bitmap size: " + loadedBitmap.getWidth() + " x " + loadedBitmap.getHeight());
        return loadedBitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (reqWidth < width || reqHeight < height) {
            double widthRatio = (double) width / (double) reqWidth;
            double heightRatio = (double) height / (double) reqHeight;

            double limitingRatio = widthRatio > heightRatio ? heightRatio : widthRatio;
            if (limitingRatio < 1) {
                inSampleSize = 1;
            } else {
                inSampleSize = (int) floor(limitingRatio);
            }
        }
        Log.d(TAG, "Required size: " + reqWidth + " x " + reqHeight);
        Log.d(TAG, "inSampleSize: " + inSampleSize);

        return inSampleSize;
    }

    private BitmapFactory.Options getBitmapOptions(int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resourceId, options);
        Log.d(TAG, "Image size from Options: " + options.outWidth + " x " + options.outHeight);
        Log.d(TAG, "Image mimeType: " + options.outMimeType);
        return options;
    }

}
