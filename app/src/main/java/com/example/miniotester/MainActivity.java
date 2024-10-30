package com.example.miniotester;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.util.ArrayList;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

public class MainActivity extends AppCompatActivity {

    //variable to hold a tag to search for in logcat
    private static final String TAG = "MinioHelloWorld";
    //setting variable to the end point to exaba server
    private static final String ENDPOINT = "https://z4-n1.dv.exaba.io:9000";
    //variable to hold to public access key
    private static final String ACCESS_KEY = "JC5QAFBT2OUHJRJJ";
    //variable to hold to private access key
    private static final String SECRET_KEY = "IeQWblMFbQ3RjAjEBMCIOqwSSbYgs2PdMicxwGjR";
    //variable to the name of the bucket selected
    private static String BUCKET_NAME = "";
    //variable for image selection process
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
    }
    public void onClickSettings(View view) {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        //starting the activity with the intent
        startActivity(intent);
    }
    //method that runs the file manager
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    //This method checks if a file has been selected in the file manager, if an image has been selected it then calls the image upload method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getClipData() != null) {

            ClipData filess = data.getClipData();
            for(int x = 0; x< filess.getItemCount(); x++)
            {
                Uri imageUri = filess.getItemAt(x).getUri();
                String objectName = getFileName(imageUri);
                new MinioUploadTask(imageUri, objectName).execute();
            }
        }
    }
    //this method gets the file name that was uploaded
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
        // Get the URL from the intent
        String Url = getIntent().getStringExtra("drawableURL");
        if (Url == null) {
            Log.e(TAG, "URL is null");
        }
        Intent swap = new Intent(MainActivity.this, QrActivity.class);
        swap.putExtra("drawableURL", Url);
        startActivity(swap);
    }


    //This class uploads the chosen file to the bucket
    private class MinioUploadTask extends AsyncTask<Void, Void, Boolean> {

        //declaring variables to hold important values
        private final Uri imageUri;
        private final String objectName;

        //this constructor initialises the image and the object names
        public MinioUploadTask(Uri imageUri, String objectName) {
            this.imageUri = imageUri;
            this.objectName = objectName;
        }

        //This method puts the file into the bucket
        @Override
        protected Boolean doInBackground(Void... voids) {
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {{
                if (inputStream == null) return false;

                //buiding the minIO client
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(ENDPOINT)
                        .credentials(ACCESS_KEY, SECRET_KEY)
                        .build();
                    // uploading the image
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(BUCKET_NAME)
                                    .object(objectName)
                                    .stream(inputStream, inputStream.available(), -1)
                                    .contentType("image/jpeg") // Adjust content type if necessary
                                    .build()
                    );
                }


                Log.d(TAG, "Image uploaded successfully: " + objectName);
                return true;

            } catch (MinioException e) {
                Log.e(TAG, "MinIO Exception: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
            }
            return false;
        }

        //this method displays a toast to the user to tell them whether the upload was successful or not
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
