package com.kunalukey.mediastore;

import androidx.annotation.NonNull;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import java.util.Map;
import java.util.HashMap;
import com.kunalukey.mediastore.NativeDeleteMediaSpec;

import androidx.activity.result.IntentSenderRequest;
import java.util.ArrayList;
import java.util.Collections;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import static android.app.Activity.RESULT_CANCELED;

public class DeleteMediaModule extends NativeDeleteMediaSpec {

    private static final String ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED = "ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED";
    private static final String ERROR_URIS_NOT_FOUND = "ERROR_URIS_NOT_FOUND";
    private static final String ERROR_USER_REJECTED = "ERROR_USER_REJECTED";
    private static final String ERROR_URIS_PARAMETER_NULL = "ERROR_URIS_PARAMETER_NULL";
    private static final String ERROR_URIS_PARAMETER_INVALID = "ERROR_URIS_PARAMETER_INVALID";
    private static final String ERROR_MODULE_NOT_INITIALIZED = "ERROR_MODULE_NOT_INITIALIZED";
    private static final String ERROR_UNEXPECTED = "ERROR_UNEXPECTED";


    public static String NAME = "ReactNativeMediaStore";

    DeleteMediaModule(ReactApplicationContext context) {
        super(context);
    }

    public static void init(ReactActivity activity){
        ActivityResultLauncherWrapper.init(activity);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private String[] checkAndParseParams(ReadableArray urisString) throws IllegalArgumentException, NullPointerException{

        if(urisString == null){
            throw new NullPointerException();
        }

        String[] uris = new String[urisString.size()];

        for (int i=0; i<urisString.size(); i++){
            if(urisString.getType(i) != ReadableType.String){
                throw new IllegalArgumentException("Element " + Integer.toString(i) + " is not a string.");
            }
            uris[i] = urisString.getString(i);
        }
        return uris;
    }

    private boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void deletePhotos(ReadableArray urisString, Promise promise){

        if(!checkPermissions()){
            promise.reject(ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED, "Error: WRITE_EXTERNAL_STORAGE permission is needed to delete media");
            return;
        }

        if(urisString.size() == 0){
            promise.resolve(null);
            return;
        }

        String[] urisParsed;

        try{
            urisParsed = checkAndParseParams(urisString);
        }catch(IllegalArgumentException e){
            promise.reject(ERROR_URIS_PARAMETER_INVALID, "Error: uris parameter should be an array of valid uri strings");
            return;
        }catch(NullPointerException e){
            promise.reject(ERROR_URIS_PARAMETER_NULL, "Error: uris parameter is null");
            return;
        }

        ContentResolver resolver = getReactApplicationContext().getContentResolver();
        // Set up the projection (we only need the ID)
        String[] projection = { MediaStore.Images.Media._ID };

        // Match on the file path
        String innerWhere = "?";
        for(int i=1; i<urisParsed.length; i++){
            innerWhere += ", ?";
        }

        String selection = MediaStore.Images.Media.DATA + " IN (" + innerWhere + ")";
        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        
        String[] selectionArgs = new String[urisParsed.length];

        for(int i=0; i<urisParsed.length; i++){
            Uri uri = Uri.parse(urisParsed[i]);
            selectionArgs[i] = uri.getPath();
        }

        Cursor cursor = resolver.query(queryUri, projection, selection, selectionArgs, null);

        ArrayList<Uri> arrayList = new ArrayList();
        while(cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            arrayList.add(deleteUri);
        }

        if(arrayList.size() != urisParsed.length){
            promise.reject(ERROR_URIS_NOT_FOUND, "Error: some uris were not found on device");
            return;
        }

        Collections.addAll(arrayList);
        IntentSender intentSender = MediaStore.createDeleteRequest(resolver, arrayList).getIntentSender();
        IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(intentSender)
                .setFillInIntent(null)
                .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
                .build();

        if(!ActivityResultLauncherWrapper.isInitialized()){
            promise.reject(ERROR_MODULE_NOT_INITIALIZED, "Error: the module was not initialized in the MainActivity.java file, please follow the installation steps described in the module page (https://gitlab.com/issambenelgada/react-native-delete-media#readme).");
            return;
        }

        ActivityResultLauncherWrapper.setOnResult((boolean success, int error_code) -> {
            if(!success){
                if(error_code == RESULT_CANCELED){
                    promise.reject(ERROR_USER_REJECTED, "The user rejected the deletion of the media");
                }else{
                    promise.reject(ERROR_UNEXPECTED, "OnActivityResult for deleting media activity return error code : " + Integer.toString(error_code));
                }
            }else{
                promise.resolve(null);
            }
        });
        ActivityResultLauncherWrapper.getActivityResultLauncher().launch(senderRequest);
    }

    // Delete Videos
    @Override
public void deleteVideos(ReadableArray urisString, Promise promise) {
    if (!checkPermissions()) {
        promise.reject(ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED, "Error: WRITE_EXTERNAL_STORAGE permission is needed to delete media");
        return;
    }

    if (urisString.size() == 0) {
        promise.resolve(null);
        return;
    }

    String[] urisParsed;
    try {
        urisParsed = checkAndParseParams(urisString);
    } catch (IllegalArgumentException e) {
        promise.reject(ERROR_URIS_PARAMETER_INVALID, "Error: uris parameter should be an array of valid uri strings");
        return;
    } catch (NullPointerException e) {
        promise.reject(ERROR_URIS_PARAMETER_NULL, "Error: uris parameter is null");
        return;
    }

    ContentResolver resolver = getReactApplicationContext().getContentResolver();
    String[] projection = { MediaStore.Video.Media._ID };
    String innerWhere = "?";
    for (int i = 1; i < urisParsed.length; i++) {
        innerWhere += ", ?";
    }

    String selection = MediaStore.Video.Media.DATA + " IN (" + innerWhere + ")";
    Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    String[] selectionArgs = new String[urisParsed.length];
    for (int i = 0; i < urisParsed.length; i++) {
        Uri uri = Uri.parse(urisParsed[i]);
        selectionArgs[i] = uri.getPath();
    }

    Cursor cursor = resolver.query(queryUri, projection, selection, selectionArgs, null);
    ArrayList<Uri> arrayList = new ArrayList<>();
    while (cursor != null && cursor.moveToNext()) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        arrayList.add(deleteUri);
    }
    if (cursor != null) cursor.close();

    if (arrayList.size() != urisParsed.length) {
        promise.reject(ERROR_URIS_NOT_FOUND, "Error: some uris were not found on device");
        return;
    }

    try {
        IntentSender intentSender = MediaStore.createDeleteRequest(resolver, arrayList).getIntentSender();
        IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(intentSender)
            .setFillInIntent(null)
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build();

        if (!ActivityResultLauncherWrapper.isInitialized()) {
            promise.reject(ERROR_MODULE_NOT_INITIALIZED, "Error: the module was not initialized in MainActivity.java, follow the module page instructions.");
            return;
        }

        ActivityResultLauncherWrapper.setOnResult((boolean success, int error_code) -> {
            if (!success) {
                if (error_code == RESULT_CANCELED) {
                    promise.reject(ERROR_USER_REJECTED, "The user rejected the deletion of the media");
                } else {
                    promise.reject(ERROR_UNEXPECTED, "OnActivityResult returned error code: " + error_code);
                }
            } else {
                promise.resolve(null);
            }
        });

        ActivityResultLauncherWrapper.getActivityResultLauncher().launch(senderRequest);
    } catch (Exception e) {
        promise.reject(ERROR_UNEXPECTED, "Unexpected error occurred during media deletion: " + e.getMessage());
    }
}

// Rename Video
@Override
public void renameVideo(String filePath, String newFileName, Promise promise) {
    // Check for WRITE_EXTERNAL_STORAGE permission
    if (!checkPermissions()) {
        promise.reject(ERROR_WRITE_EXTERNAL_STORAGE_PERMISSION_NEEDED, "Error: WRITE_EXTERNAL_STORAGE permission is needed to rename media");
        return;
    }

    // Validate input parameters
    if (filePath == null || filePath.isEmpty()) {
        promise.reject(ERROR_INVALID_PARAMETERS, "Error: file path is null or empty");
        return;
    }

    if (newFileName == null || newFileName.isEmpty()) {
        promise.reject(ERROR_INVALID_PARAMETERS, "Error: new file name is invalid");
        return;
    }

    // Get the ContentResolver
    ContentResolver resolver = getReactApplicationContext().getContentResolver();
    
    // Prepare projection and selection for querying the file by path
    String[] projection = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME };
    String selection = MediaStore.Video.Media.DATA + " = ?";
    String[] selectionArgs = new String[]{ filePath };

    // Query the MediaStore to get the video
    try (Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null)) {
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
            Uri videoContentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

            // Prepare the content values to rename the video
            ContentValues values = new ContentValues();
            String fileExtension = filePath.substring(filePath.lastIndexOf("."));
            values.put(MediaStore.Video.Media.DISPLAY_NAME, newFileName + fileExtension);

            ArrayList<Uri> uriList = new ArrayList<>();
            uriList.add(videoContentUri);

            try {
                // Prepare IntentSender to request permission to rename the file (for Android 10+)
                IntentSender intentSender = MediaStore.createWriteRequest(resolver, uriList).getIntentSender();
                IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(intentSender)
                    .setFillInIntent(null)
                    .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
                    .build();

                // Check if ActivityResultLauncherWrapper is initialized
                if (!ActivityResultLauncherWrapper.isInitialized()) {
                    promise.reject(ERROR_MODULE_NOT_INITIALIZED, "Error: the module was not initialized in MainActivity.java, follow the module page instructions.");
                    return;
                }

                // Set callback for result from the IntentSender
                ActivityResultLauncherWrapper.setOnResult((boolean success, int error_code) -> {
                    if (!success) {
                        if (error_code == RESULT_CANCELED) {
                            promise.reject(ERROR_USER_REJECTED, "The user rejected the rename operation");
                        } else {
                            promise.reject(ERROR_UNEXPECTED, "OnActivityResult returned error code: " + error_code);
                        }
                    } else {
                        try {
                            // Try to update the video with the new name
                            int updatedRows = resolver.update(videoContentUri, values, null, null);
                            if (updatedRows > 0) {
                                promise.resolve("Video renamed successfully");
                            } else {
                                promise.reject(ERROR_RENAME_FAILED, "Error: Failed to rename the video");
                            }
                        } catch (Exception e) {
                            promise.reject(ERROR_UNEXPECTED, "Unexpected error occurred during renaming: " + e.getMessage());
                        }
                    }
                });

                // Launch the intent to request permission
                ActivityResultLauncherWrapper.getActivityResultLauncher().launch(senderRequest);

            } catch (Exception e) {
                promise.reject(ERROR_UNEXPECTED, "Unexpected error occurred while preparing rename request: " + e.getMessage());
            }
        } else {
            promise.reject(ERROR_URIS_NOT_FOUND, "Error: Video file not found in MediaStore");
        }
    } catch (Exception e) {
        promise.reject(ERROR_UNEXPECTED, "Unexpected error occurred during renaming: " + e.getMessage());
    }
}


}