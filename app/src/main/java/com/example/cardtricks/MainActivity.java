package com.example.cardtricks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.example.cardtricks.views.CardView;


public class MainActivity extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener, LoadBitmapTask.Listener {

    private static final String TAG = MainActivity.class.getName();

    private final static String foregroundPicURL = "https://dl.dropboxusercontent.com/u/1638040/minion1.jpg";
    private final static String backgroundPicURL = "https://dl.dropboxusercontent.com/u/1638040/minion2.jpg";

    private CardView cardView;
    private SeekBar saturationBar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardView = (CardView) findViewById(R.id.card__my_card);
        cardView.startYRotation();

        saturationBar = (SeekBar) findViewById(R.id.seek_bar__saturation);
        saturationBar.setOnSeekBarChangeListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        LoadBitmapTask frontTask = new LoadBitmapTask(this, cardView, true);
        frontTask.setListener(this);
        frontTask.execute(foregroundPicURL);

        LoadBitmapTask backTask = new LoadBitmapTask(this, cardView, false);
        backTask.setListener(this);
        backTask.execute(backgroundPicURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up rotationButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        progressBar.setVisibility(View.GONE);
    }
}
