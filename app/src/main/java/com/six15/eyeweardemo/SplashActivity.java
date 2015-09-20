package com.six15.eyeweardemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private static boolean bSplash = false;
    SharedPreferences prefs;
    static final int SEND_TRACE_REQUEST = 1;
    private ApplicationPreferences sharedPreference;
    Activity context = this;

    private void displayVersionName() {
        String verName = "v";
        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(
                            getApplicationContext().getPackageName(),
                            0
                    );
            verName+= packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView tv = (TextView) findViewById(R.id.ver_name);
        tv.setText(verName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String line;
        String trace = "";
        boolean bErrorlog = false;

        sharedPreference = new ApplicationPreferences();
        displayVersionName();


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(SplashActivity.this.openFileInput("six15.trace")));

            while((line = reader.readLine()) != null) {
                trace += line+"\n";
            }

            bErrorlog = true;

        } catch(FileNotFoundException fnfe) {
            bErrorlog = false;
        } catch(IOException ioe) {

        }

        if(bErrorlog) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            String subject = "Error report";
            String body =
                    "Mail this to gvigelet@duvitech.com: " +
                            "\n" +
                            trace +
                            "\n";

            sendIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"gvigelet@duvitech.com"});
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            sendIntent.setType("message/rfc822");

            SplashActivity.this.startActivityForResult(
                    Intent.createChooser(sendIntent, "Title:"), SEND_TRACE_REQUEST);


        }
        else {
            /****** Create Thread that will sleep for 5 seconds *************/
            if (!bSplash) {
                bSplash = true;
                Thread background = new Thread() {
                    public void run() {

                        Intent i = null;
                        i = new Intent(getBaseContext(), DeviceScanActivity.class);

                        try {
                            // Thread will sleep for 5 seconds
                            for (int x = 0; x <= 100; x++) {
                                sleep(10);
                            }


                            startActivity(i);
                            finish();

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                };

                // start thread
                background.start();

            } else {

                // After 5 seconds redirect to another intent
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SEND_TRACE_REQUEST) {
            SplashActivity.this.deleteFile("six15.trace");
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        /****** Create Thread that will sleep for 5 seconds *************/
        if (!bSplash) {
            bSplash = true;
            Thread background = new Thread() {
                public void run() {
                    Intent i = null;
                    i = new Intent(getBaseContext(), DeviceScanActivity.class);

                    try {
                        // Thread will sleep for 5 seconds
                        for (int x = 0; x <= 100; x++) {
                            sleep(10);
                        }


                        startActivity(i);
                        finish();

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            };

            // start thread
            background.start();

        } else {

            // After 5 seconds redirect to another intent
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }
}
