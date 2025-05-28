package com.canvamedium.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom uncaught exception handler to gracefully handle app crashes.
 * This class captures crash details and saves them to a log file.
 */
public class AppExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "AppExceptionHandler";
    private static final String CRASH_LOG_FOLDER = "crash_logs";
    
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    /**
     * Constructor for the AppExceptionHandler.
     *
     * @param context The application context
     */
    public AppExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Install the exception handler as the default uncaught exception handler.
     *
     * @param context The application context
     */
    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler(context));
        Log.i(TAG, "AppExceptionHandler installed");
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        try {
            // Save the crash information
            saveCrashReport(ex);
            
            // Log the crash
            Log.e(TAG, "Uncaught exception", ex);
            
            // You could optionally start a recovery activity here
            // Intent intent = new Intent(context, CrashRecoveryActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // context.startActivity(intent);
            
            // If you have a crash reporting service, you could send the report here
            // sendCrashReport(ex);
            
            // Let the default handler do its thing
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            } else {
                // If no default handler, manually kill the process
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        } catch (Exception e) {
            // If we encounter a problem while handling the exception
            Log.e(TAG, "Error handling uncaught exception", e);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        }
    }

    /**
     * Save the crash report to a file.
     *
     * @param throwable The throwable that caused the crash
     */
    private void saveCrashReport(Throwable throwable) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String filename = "crash_" + timestamp + ".log";
            
            // Ensure directory exists
            File directory = new File(context.getExternalFilesDir(null), CRASH_LOG_FOLDER);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "Failed to create crash log directory");
                    return;
                }
            }
            
            File file = new File(directory, filename);
            
            // Write crash info to file
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(fos);
            
            // App info
            writer.println("App Version: " + getAppVersion());
            writer.println("OS Version: Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
            writer.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            writer.println();
            
            // Exception info
            writer.println("Exception: " + throwable.getClass().getName());
            writer.println("Message: " + throwable.getMessage());
            writer.println();
            
            // Stack trace
            writer.println("Stack Trace:");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            writer.println(sw.toString());
            
            writer.close();
            fos.close();
            
            Log.i(TAG, "Crash report saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving crash report", e);
        }
    }

    /**
     * Get the app version.
     *
     * @return The app version string
     */
    private String getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName + " (" + packageInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting app version", e);
            return "Unknown";
        }
    }
} 