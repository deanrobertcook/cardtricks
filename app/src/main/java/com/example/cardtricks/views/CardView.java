package com.example.cardtricks.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.cardtricks.R;

import static java.lang.Math.acos;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

public class CardView extends View {

    private static final String TAG = CardView.class.getName();

    private static final int DEFAULT_INITIAL_ROTATION = 0; //degrees

    private static final long ROTATION_DURATION = 3000;

    private Bitmap frontBitmap;
    private Bitmap backBitmap;

    private int yAxisRot;
    private int zAxisRot;

    private Paint fillPaint;

    private GestureDetector gestureDetector;
    private GestureListener gestureListener;
    private Paint testPaint;

    public CardView(Context context) {
        super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.CardView, 0, 0);

        try {
            yAxisRot = a.getInt(R.styleable.CardView_initial_y_rotation, DEFAULT_INITIAL_ROTATION);
            zAxisRot = a.getInt(R.styleable.CardView_initial_z_rotation, DEFAULT_INITIAL_ROTATION);
        } finally {
            a.recycle();
        }

        fillPaint = new Paint();

        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    public int getYAxisRot() {
        return yAxisRot;
    }

    public void setYAxisRot(int rotation) {
        yAxisRot = rotation % 360;
        invalidate();
    }

    public void startYRotation() {
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "yAxisRot", 0, 360);
        animator.setDuration(ROTATION_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public int getZAxisRot() {
        return zAxisRot;
    }

    public void setZAxisRot(int rotation) {
        zAxisRot = rotation % 360;
    }

    public void setSaturation(float saturation) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(saturation);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(colorMatrix);
        fillPaint.setColorFilter(cf);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap currentBackground = getCurrentBackground(canvas);

        Camera camera = new Camera();
        camera.rotateZ(zAxisRot);
        camera.rotateY(yAxisRot);
        Matrix matrix = new Matrix();
        camera.getMatrix(matrix);


        matrix.preTranslate(-canvas.getWidth() / 2, -canvas.getHeight() / 2);
        matrix.postTranslate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        //Not too sure why putting the matrix directly into here worked...
        //Because I'm not actually moving the canvas here, just the bitmap. So the transformations
        //I was trying to apply to the canvas earlier I can just apply to the bitmap, and then I
        //still have control over what's going on in the background of the canvas.
        canvas.drawBitmap(currentBackground, matrix, fillPaint);

    }

    private Bitmap getCurrentBackground(Canvas canvas) {
        if (backBitmap == null) {
            backBitmap = setBackgroundBitmap(R.drawable.minion2);
        }
        if (frontBitmap == null) {
            frontBitmap = setBackgroundBitmap(R.drawable.minion1);
        }

        Bitmap currentBackground;
        if (yAxisRot > 90 && yAxisRot <= 270) {
            currentBackground = backBitmap;
        } else {
            currentBackground = frontBitmap;
        }
        return currentBackground;
    }

    private Bitmap setBackgroundBitmap(int resourceId) {
        Bitmap bitmap = decodeBitmap(resourceId, getWidth(), getHeight());
        bitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        return bitmap;
    }

    private Bitmap decodeBitmap(int resourceId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = getBitmapOptions(resourceId);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        //allows resizing of the bitmaps
        options.inMutable = true;

        //since the images are not transparent, I can use this config to save space
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //If this is not set, then the images will be scaled up to match the density of the device
        //(scaled 2 or 3 times!). Alternatively, the image can be placed in a drawable-nodpi folder
        options.inScaled = false;

        Bitmap loadedBitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);
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

        return inSampleSize;
    }

    private BitmapFactory.Options getBitmapOptions(int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resourceId, options);
        return options;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event);
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //Resets the lastX/Y positions so that the image doesn't jump around
                //on the next ACTION_DOWN/SCROLL event
                gestureListener.lastX = -1f;
                gestureListener.lastY = -1f;
            }
        }
        return result;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        float lastX = -1f;
        float lastY = -1f;

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float originX = getWidth() / 2f;
            float originY = getHeight() / 2f;

            /**
             * The first MotionEvent is always the MotionEvent associated with the DOWN action that
             * started the scroll. Since the onScroll method gets called a bunch of times for any
             * single scroll event (at some refresh rate) we have to use the second MotionEvent of
             * the last call to onScroll as our starting point in this call to on scroll, or else
             * we end up adding rotation we've already added to the view - many times over
             * (producing an exponentially increasing rotation speed)
             */
            float startX = lastX == -1 ? e1.getX() - originX : lastX;
            float startY = lastY == -1 ? e1.getY() - originY : lastY;
            //All points relative to the center of the view
            float endX = e2.getX() - originX;
            float endY = e2.getY() - originY;

            float dotProduct = startX * endX + startY * endY;

            float magnitudeProduct = (float) (sqrt(startX * startX + startY * startY) *
                    sqrt(endX * endX + endY * endY));

            float angleTravelled = (float) toDegrees(acos(dotProduct / magnitudeProduct));

            //cross product (i and j terms will be 0)
            //this allows us to determine the direction of motion
            float crossProductK = startX * endY - startY * endX;
            angleTravelled = crossProductK > 0 ? -angleTravelled : angleTravelled;

            setZAxisRot(zAxisRot + (int) angleTravelled);
            lastX = endX;
            lastY = endY;
            return true;
        }
    }
}
