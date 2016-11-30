package com.TestApp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.TestApp.R;
import com.TestApp.constants.AppConstant;
import com.TestApp.functions.JAP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class DownloadActivity extends AppCompatActivity {
    String urlReceived, fileReceived;
    Context context;
    TextView percentage, downCount;
    File myFilesDir;
    ProgressBar seekBar;
    long fileOnServer, total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        urlReceived = "url-to-the-file";
        fileReceived = "name-of-the-file";
        context = this;
        percentage = (TextView) findViewById(R.id.txtPercentage);
        percentage.setText("");
        percentage.setVisibility(View.GONE);
        downCount = (TextView) findViewById(R.id.countDwon);
        downCount.setText("");
        downCount.setVisibility(View.GONE);
        myFilesDir = new File(AppConstant.newFilesDir);
        if (!myFilesDir.exists()) {
            myFilesDir.mkdirs();
        }
        seekBar = (ProgressBar) findViewById(R.id.seekBar1);
        if (fileReceived.equals("first.mp4")) {
            seekBar.setMax((int) AppConstant.FIRST_FILE_SIZE);
        }
        seekBar.setProgress(0);
        new DownloadFileFromURLFirst().execute(urlReceived, fileReceived);
    }

    class DownloadFileFromURLFirst extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urlReceived) {
            int count;
            try {
                URL url = new URL(urlReceived[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                fileOnServer = conection.getContentLength();
                Log.d(AppConstant.TAG, "" + fileOnServer);
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                FileOutputStream output = new FileOutputStream(new File(myFilesDir + "/" + fileReceived));
                byte data[] = new byte[1024];
                total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100) / fileOnServer));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", "" + e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            percentage.setVisibility(View.VISIBLE);
            downCount.setVisibility(View.VISIBLE);
            if (fileReceived.equals("first.mp4")) {
                seekBar.setProgress((int) (total));
                String percent = ((100 * total) / (int) AppConstant.FIRST_FILE_SIZE) + "%";
                percentage.setText(percent);
                String dcount = (total / (1024 * 1024)) + "/ " + (AppConstant.FIRST_FILE_SIZE / (1024 * 1024)) + "Mb";
                downCount.setText(dcount);
            }

        }

        @Override
        protected void onPostExecute(String file_url) {
            seekBar.setProgress(0);
            switch (fileReceived) {
                case "first.mp4":
                    Intent mDownloadFinish = new Intent(context, VideoViewActivity.class);
                    mDownloadFinish.putExtra("First", "first.mp4");
                    startActivity(mDownloadFinish);
                    break;
                default:
                    break;
            }
            finish();
            overridePendingTransition(R.anim.curlin, R.anim.curlout);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            JAP.S(percentage, "Please wait for file to finish download!");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
