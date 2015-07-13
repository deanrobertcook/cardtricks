package com.example.cardtricks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import com.example.cardtricks.views.CardView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class LoadBitmapTask extends AsyncTask<String, Void, Bitmap> {

    private final Context context;
    private final CardView cardView;
    private final boolean front;
    private Listener listener;

    public LoadBitmapTask(Context context, CardView cardView, boolean front) {
        this.context = context;
        this.cardView = cardView;
        this.front = front;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private static final String TAG = LoadBitmapTask.class.getName();

    @Override
    protected void onPreExecute() {
        if (front && listener != null) {
            listener.onStartExecution();
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String urlString = params[0];
        String fileName = Uri.parse(urlString).getLastPathSegment();

        //First attempt to find the bitmap in the internal storage
        Bitmap bitmap = fetchBitmapFromFile(fileName);
        if (bitmap == null) {
            //otherwise attempt to download it.
            downloadBitmapToFile(urlString, fileName);
            bitmap = fetchBitmapFromFile(fileName);
        }
        return bitmap;
    }

    private Bitmap fetchBitmapFromFile(String fileName) {
        fileName = context.getFilesDir() + "/" + fileName;

        BitmapFactory.Options options = getBitmapOptions(fileName);
        Bitmap loadedBitmap = BitmapFactory.decodeFile(fileName, options);
        return loadedBitmap;
    }

    private BitmapFactory.Options getBitmapOptions(String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
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

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > screenWidth || height > screenHeight) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) > screenWidth
                    && (halfHeight / inSampleSize) > screenHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void downloadBitmapToFile(String urlString, String fileName) {
        try {
            URL url = new URL(urlString);
            InputStream inputStream = url.openStream();
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
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
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
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

        if (listener != null) {
            listener.onFinishExecution();
        }
    }

    public interface Listener {
        void onStartExecution();

        void onFinishExecution();
    }
}
