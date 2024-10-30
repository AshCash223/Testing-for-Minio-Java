package com.example.miniotester;

import android.Manifest;  // Ensure to import Manifest
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MinioHelloWorld";
    private static final String ENDPOINT = "https://z4-n1.dv.exaba.io:9000";
    private static final String ACCESS_KEY = "JC5QAFBT2OUHJRJJ";
    private static final String SECRET_KEY = "IeQWblMFbQ3RjAjEBMCIOqwSSbYgs2PdMicxwGjR";
    private static String BUCKET_NAME = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        syncButton = findViewById(R.id.syncButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        syncButton.setOnClickListener(view -> {
            openFilePicker();
        });

        BUCKET_NAME = getIntent().getStringExtra("radioText");

        // Check if the media permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

            // Permissions not granted, request them
            Log.w(TAG, "Permissions not granted");
            askForPermission();
        } else {
            // Permissions are already granted
            Log.w(TAG, "Permissions granted");
        }
    }

    // Method to request media permissions from the user
    public void askForPermission() {
        ActivityResultLauncher<String[]> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean imagePermissionGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                    Boolean videoPermissionGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_VIDEO, false);

                    // If both permissions are granted
                    if (imagePermissionGranted != null && imagePermissionGranted
                            && videoPermissionGranted != null && videoPermissionGranted) {
                        Log.w(TAG, "Permissions are now granted");
                        Toast.makeText(MainActivity.this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permissions were not granted
                        Log.w(TAG, "Permissions were not granted");
                        Toast.makeText(MainActivity.this, "Permissions are required to upload images", Toast.LENGTH_LONG).show();
                    }
                });

        activityResultLauncher.launch(new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
        });
    }

    public void onClickSettings(View view) {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        startActivity(intent);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && (data.getClipData() != null || data.getData() != null)) {


            if(data.getClipData() != null)
            {
                ClipData filess = data.getClipData();
                for(int x = 0; x < filess.getItemCount(); x++)
                {
                    Uri imageUri = filess.getItemAt(x).getUri();
                    String objectName = getFileName(imageUri);
                    new MinioUploadTask(imageUri, objectName).execute();
                }
            }
            else
            {
                Uri imageUri = data.getData();
                String objectName = getFileName(imageUri);
                new MinioUploadTask(imageUri, objectName).execute();
            }
        }
    }

    private String getFileName(Uri uri) {
        String file = "File";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    file = cursor.getString(nameIndex);
                }
            }
        }
        return file;
    }

    public void onclickDisplay(View view) {
        String Url = getIntent().getStringExtra("drawableURL");
        if (Url == null) {
            Log.e(TAG, "URL is null");
        }
        Intent swap = new Intent(MainActivity.this, QrActivity.class);
        swap.putExtra("drawableURL", Url);
        startActivity(swap);
    }

    private class MinioUploadTask extends AsyncTask<Void, Void, Boolean> {

        private final Uri imageUri;
        private final String objectName;

        public MinioUploadTask(Uri imageUri, String objectName) {
            this.imageUri = imageUri;
            this.objectName = objectName;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                if (inputStream == null) return false;

                MinioClient minioClient = MinioClient.builder()
                        .endpoint(ENDPOINT)
                        .credentials(ACCESS_KEY, SECRET_KEY)
                        .build();

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .stream(inputStream, inputStream.available(), -1)
                                .contentType("image/jpeg") // Adjust content type if necessary
                                .build()
                );

                Log.d(TAG, "Image uploaded successfully: " + objectName);
                return true;

            } catch (MinioException e) {
                Log.e(TAG, "MinIO Exception: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
