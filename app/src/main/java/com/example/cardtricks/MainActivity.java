package com.example.cardtricks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.cardtricks.data.LoadBitmapTask;
import com.example.cardtricks.views.CardView;


public class MainActivity extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener, LoadBitmapTask.Listener {

    private static final String TAG = MainActivity.class.getName();

    private CardView cardView;
    private SeekBar saturationBar;
    private ProgressBar progressBar;

    private int imagesLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        String customFrontUrl = getString(R.string.front_image_url);
        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType() != null) {
            String customUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
            customFrontUrl = customUrl;
        }

        setContentView(R.layout.activity_main);

        cardView = (CardView) findViewById(R.id.card__my_card);
        cardView.startYRotation();

        saturationBar = (SeekBar) findViewById(R.id.seek_bar__saturation);
        saturationBar.setOnSeekBarChangeListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        LoadBitmapTask frontTask = new LoadBitmapTask(this, cardView, true);
        frontTask.setListener(this);
        frontTask.execute(customFrontUrl);

        LoadBitmapTask backTask = new LoadBitmapTask(this, cardView, false);
        backTask.setListener(this);
        backTask.execute(getString(R.string.back_image_url));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_browse) {
            sendBrowsingIntent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendBrowsingIntent() {
        Toast.makeText(this, "Try browsing for an image and then share it back to this app!",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/images"));
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float saturation = (float) progress / 100f;
        cardView.setSaturation(saturation);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartExecution() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishExecution() {
        imagesLoaded++;
        if (imagesLoaded == 2) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
