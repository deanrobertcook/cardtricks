package com.waz.deanscool.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.waz.deanscool.R;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class CardView extends View {

    private static final String TAG = CardView.class.getName();

    private static final int DEFAULT_INITIAL_ROTATION = 0; //degrees

    /**
     * How much the height varies as a result of perspective
     */
    private static final double MIN_HEIGHT_AS_RATIO_OF_NORMAL = 0.7;
    private static final long ANIMATION_DURATION = 500;

    private int backColor;
    private int frontColor;
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

    public void rotateBy(int degrees) {
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "zAxisRot", zAxisRot, zAxisRot +
                degrees).setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int color = getVisibleColor();
        fillPaint.setColor(color);

        float[] leadingEdge = getLeadingEdgeCoords(canvas);
        Log.d(TAG, "leading Edge: " + leadingEdge[0] + ", " + leadingEdge[1] + ", " + leadingEdge[2] + ", " + leadingEdge[3]);
        float[] trailingEdge = getTrailingEdgeCoords(canvas);

//        Bitmap frontBitmap = decodeBitmap(R.drawable.minion1, canvas.getWidth(), canvas
//                .getHeight());
//        Log.d(TAG, "Bitmap size: " + frontBitmap.getWidth() + " x " + frontBitmap.getHeight());
//        Canvas bitmapCanvas = new Canvas(frontBitmap);

        Path path = new Path();
        path.moveTo(leadingEdge[0], leadingEdge[1]);
        path.lineTo(leadingEdge[2], leadingEdge[3]);
        path.lineTo(trailingEdge[2], trailingEdge[3]);
        path.lineTo(trailingEdge[0], trailingEdge[1]);
        path.close();

        canvas.drawPath(path, fillPaint);

//        Matrix matrix = new Matrix();
//
//        matrix.postScale();
//
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(frontBitmap, canvas.getWidth(), canvas.getHeight(), true);
//
//        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
//
//        canvas.drawBitmap(rotatedBitmap, 0, 0, fillPaint);


    }

    private int getVisibleColor() {
        if (zAxisRot > 90 && zAxisRot <= 270) {
            return backColor;
        } else {
            return frontColor;
        }
    }

    /**
     * Let the leading edge be the right hand edge of the card when at 0 deg rotation.
     *
     * @param canvas
     * @return the coordinates of the line representing the leading edge
     * [topx, topy, bottomx, bottomy]
     */
    private float[] getLeadingEdgeCoords(Canvas canvas) {
        int verticalAxis = canvas.getWidth() / 2;
        int cardWidth = getPerceivedCardWidth(canvas.getWidth());

        int horizAxis = canvas.getHeight() / 2;
        int edgeHeight = getLeadingEdgeHeight(canvas.getHeight());

        if (zAxisRot > 90 && zAxisRot <= 270) {
            return new float[] {
                verticalAxis + cardWidth / 2, horizAxis + edgeHeight / 2,
                verticalAxis + cardWidth / 2, horizAxis - edgeHeight / 2
            };
        } else {
            return new float[] {
                verticalAxis - cardWidth / 2, horizAxis + edgeHeight / 2,
                verticalAxis - cardWidth / 2, horizAxis - edgeHeight / 2
            };
        }
    }

    /**
     * Let the trailing edge be the left hand edge of the card when at 0 deg rotation.
     *
     * @param canvas
     * @return the coordinates of the line representing the leading edge
     * [topx, topy, bottomx, bottomy]
     */
    private float[] getTrailingEdgeCoords(Canvas canvas) {
        int verticalAxis = canvas.getWidth() / 2;
        int cardWidth = getPerceivedCardWidth(canvas.getWidth());

        int horizAxis = canvas.getHeight() / 2;
        int edgeHeight = getTrailingEdgeHeight(canvas.getHeight());

        if (zAxisRot > 90 && zAxisRot <= 270) {
            return new float[] {
                    verticalAxis - cardWidth / 2, horizAxis + edgeHeight / 2,
                    verticalAxis - cardWidth / 2, horizAxis - edgeHeight / 2
            };
        } else {
            return new float[] {
                    verticalAxis + cardWidth / 2, horizAxis + edgeHeight / 2,
                    verticalAxis + cardWidth / 2, horizAxis - edgeHeight / 2
            };
        }
    }

    private int getPerceivedCardWidth(int originalCanvasWidth) {
        double canvasWidthRatio = cos(toRadians(zAxisRot));
        return abs((int) (originalCanvasWidth * canvasWidthRatio));
    }

    /**
     * The perceived height of the left edge of the card, where the left edge
     * is assumed to be the left edge at 0 deg rotation.
     *
     * @return
     */
    private int getLeadingEdgeHeight(int canvasHeight) {
        double heightDiff = (1.00 - MIN_HEIGHT_AS_RATIO_OF_NORMAL) * canvasHeight;
        Log.d(TAG, "Height diff: " + heightDiff);
        return canvasHeight + (int) (sin(toRadians(zAxisRot)) * heightDiff);
    }

    private int getTrailingEdgeHeight(int canvasHeight) {
        double heightDiff = (1.00 - MIN_HEIGHT_AS_RATIO_OF_NORMAL) * canvasHeight;
        return canvasHeight - (int) (sin(toRadians(zAxisRot)) * heightDiff);
    }

    private Bitmap decodeBitmap(int resourceId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = getBitmapOptions(resourceId);
        options.inSampleSize = 8; //calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        return BitmapFactory.decodeResource(getResources(), resourceId, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
            Log.d(TAG, "Required size: " + reqWidth + " x " + reqHeight);
            Log.d(TAG, "inSampleSize: " + inSampleSize);
        }

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
