package com.example.cardtricks.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

public class CardView extends View {

    private static final String TAG = CardView.class.getName();

    /**
     * If the starting rotation is not set as an attribute - for both Z and Y axis rotation
     */
    private static final int DEFAULT_INITIAL_ROTATION = 0;

    /**
     * Time for one full rotation of the card about the Y axis in milliseconds (also, mHz)
     */
    private static final long ROTATION_DURATION = 3000;

    /**
     * Default colors to set the card's backgrounds to while the bitmap is still loading.
     */
    private static final int DEFAULT_FRONT_BACKGROUND_COLOR = R.color.blue;
    private static final int DEFAULT_BACK_BACKGROUND_COLOR = R.color.red;

    private int frontBackgroundColor;
    private int backBackgroundColor;

    private Bitmap frontBitmap;
    private Bitmap backBitmap;

    private int yAxisRot;
    private int zAxisRot;

    private Paint fillPaint;

    private GestureDetector gestureDetector;
    private GestureListener gestureListener;

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
            frontBackgroundColor = a.getColor(R.styleable.CardView_color_background_front, getResources().getColor(DEFAULT_FRONT_BACKGROUND_COLOR));
            backBackgroundColor = a.getColor(R.styleable.CardView_color_background_back, getResources().getColor(DEFAULT_BACK_BACKGROUND_COLOR));
            yAxisRot = a.getInt(R.styleable.CardView_initial_y_rotation, DEFAULT_INITIAL_ROTATION);
            zAxisRot = a.getInt(R.styleable.CardView_initial_z_rotation, DEFAULT_INITIAL_ROTATION);
        } finally {
            a.recycle();
        }

        fillPaint = new Paint();

        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    public void setFrontBitmap(Bitmap bitmap) {
        this.frontBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), false);
    }

    public void setBackBitmap(Bitmap bitmap) {
        this.backBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), false);;
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

        Bitmap currentBackground = getCurrentBackground();

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

    private Bitmap getCurrentBackground() {
        if (backBitmap == null) {
            backBitmap = getSolidColorBitmap(backBackgroundColor);
//            backBitmap = setBackgroundBitmap(R.drawable.minion2);
        }
        if (frontBitmap == null) {
            frontBitmap = getSolidColorBitmap(frontBackgroundColor);
//            frontBitmap = setBackgroundBitmap(R.drawable.minion1);
        }

        Bitmap currentBackground;
        if (yAxisRot > 90 && yAxisRot <= 270) {
            currentBackground = backBitmap;
        } else {
            currentBackground = frontBitmap;
        }
        return currentBackground;
    }

    private Bitmap getSolidColorBitmap(int color) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        bitmap.eraseColor(color);
        return bitmap;
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
