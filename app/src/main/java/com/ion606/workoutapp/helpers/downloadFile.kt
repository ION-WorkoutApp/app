package com.ion606.workoutapp.helpers

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

private const val TAG = "downloadFile"


fun saveJsonContentToDownloads(context: Context, jsonContent: String, fileName: String) {
    Log.d(TAG, "Saving json content to downloads folder");

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // using mediastore api for android 10 and above
        val resolver = context.contentResolver;
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        };
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(jsonContent.toByteArray());
            };
        };
    } else {
        // for older devices, write directly to the external storage downloads folder
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        val file = File(downloadsDir, fileName);
        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonContent.toByteArray());
        };
    }
}
