package com.six15.eyeweardemo;

import android.app.Activity;
import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by George on 9/19/2015.
 */
public class Six15ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Activity app = null;

    public Six15ExceptionHandler(Activity app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StackTraceElement[] arr = throwable.getStackTrace();
        String report = throwable.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++)
        {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = throwable.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++)
            {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        try {
            FileOutputStream trace = app.openFileOutput(
                    "six15.trace", Context.MODE_PRIVATE);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
// ...
        }

        defaultUEH.uncaughtException(thread, throwable);
    }
}
