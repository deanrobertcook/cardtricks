package com.example.cardtricks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.cardtricks.views.CardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Math.floor;

public class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {

    private final Context context;
    private final CardView cardView;
    private final boolean front;

    public LoadBitmapTask(Context context, CardView cardView, boolean front) {
        this.context = context;
        this.cardView = cardView;
        this.front = front;
    }

    private static final String TAG = LoadBitmapTask.class.getName();

    @Override
    protected Bitmap doInBackground(String... params) {
        String urlString = params[0];
//        String fileName = Uri.parse(urlString).getLastPathSegment();
//
//        Bitmap bitmap = fetchBitmapFromFile(fileName);
//        if (bitmap == null) {
//            downloadBitmapToFile(urlString, fileName);
//            bitmap = fetchBitmapFromFile(fileName);
//        }
//
//        Log.d(TAG, "Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
//        return bitmap;
        try {
            URL url = new URL(urlString);

            InputStream inputStream = url.openStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            Log.d(TAG, "Options size: " + options.outWidth + "x" + options.outHeight + ", " +
                    "MimeType: " + options.outMimeType);

            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options);
            inputStream.close();

            inputStream = url.openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            Log.d(TAG, "Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            inputStream.close();
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap fetchBitmapFromFile(String fileName) {
        BitmapFactory.Options options = getBitmapOptions(fileName);
        Bitmap loadedBitmap = BitmapFactory.decodeFile(fileName, options);
        return loadedBitmap;
    }

    private BitmapFactory.Options getBitmapOptions(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Log.d(TAG, "Options size: " + options.outWidth + "x" + options.outHeight + ", MimeType: "
                + options.outMimeType);
        BitmapFactory.decodeFile(fileName, options);

        //allows resizing of the bitmaps
        options.inMutable = true;
        //since the images are not transparent, I can use this config to save space
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //If this is not set, then the images will be scaled up to match the density of the device
        //(scaled 2 or 3 times!). Alternatively, the image can be placed in a drawable-nodpi folder
        options.inScaled = false;
        options.inSampleSize = calculateInSampleSize(options);

        options.inJustDecodeBounds = false;

        return options;
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (screenWidth < width || screenWidth < height) {
            double widthRatio = (double) width / (double) screenWidth;
            double heightRatio = (double) height / (double) screenHeight;

            double limitingRatio = widthRatio > heightRatio ? heightRatio : widthRatio;
            if (limitingRatio < 1) {
                inSampleSize = 1;
            } else {
                inSampleSize = (int) floor(limitingRatio);
            }
        }

        Log.d(TAG, "Screen size: " + screenWidth + "x" + screenHeight);
        Log.d(TAG, "Sample Size: " + inSampleSize);

        return inSampleSize;
    }

    private void downloadBitmapToFile(String urlString, String fileName) {
        HttpURLConnection connection;
        File file = new File(context.getCacheDir(), fileName);
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = new FileOutputStream(file);
            writeBytesToFile(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBytesToFile(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        os.flush();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (front) {
            cardView.setFrontBitmap(bitmap);
        } else {
            cardView.setBackBitmap(bitmap);
        }
    }
}
