package com.example.miniotester;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MinioHelloWorld";
    private static final String ENDPOINT = "https://z4-n1.dv.exaba.io:9000";
    private static final String ACCESS_KEY = "JC5QAFBT2OUHJRJJ";
    private static final String SECRET_KEY = "IeQWblMFbQ3RjAjEBMCIOqwSSbYgs2PdMicxwGjR";
    private static final String BUCKET_NAME = "test";

    private TextView tv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tv1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new MinioTask().execute();
    }

    private class MinioTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder buckets = new StringBuilder();

            try {
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(ENDPOINT)
                        .credentials(ACCESS_KEY, SECRET_KEY)
                        .build();

                minioClient.listBuckets().forEach(bucket -> {
                    Log.d(TAG, "Bucket: " + bucket.name());
                    buckets.append(bucket.name()).append("\n");
                });

                uploadImage(minioClient, "th.jpeg");

            } catch (MinioException e) {
                Log.e(TAG, "MinIO Exception: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception: " + e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "No Such Algorithm: " + e.getMessage(), e);
            } catch (InvalidKeyException e) {
                Log.e(TAG, "Invalid Key: " + e.getMessage(), e);
            }
            return buckets.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tv1.setText("Buckets: " + "\n" + result);
        }

        // this method uploads an image to the bucket. It is passed the cliient and a file name.
        private void uploadImage(MinioClient minioClient, String objectName) {
            try {
                // creating an input stream that opens the file
                @SuppressLint("ResourceType") InputStream inputStream = getResources().openRawResource(R.drawable.th);
                //creating a temporary file
                File tempFile = File.createTempFile("temp_image", ".jpeg", getCacheDir());
                FileOutputStream out = new FileOutputStream(tempFile);
                //creating a buffer that sends 1024 bytes across.
                byte[] buffer = new byte[1024];
                int length;
                //while there are still bytes to send
                while ((length = inputStream.read(buffer)) > 0) {
                    //send the bytes
                    out.write(buffer, 0, length);
                }
                //close the FOS and the IS
                out.close();
                inputStream.close();

                //code to put the object into the bucket
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .stream(new FileInputStream(tempFile), tempFile.length(), -1)
                        .contentType("image/jpeg")
                        .build());

                //documenting to the log that the image was sent
                Log.d(TAG, "Image uploaded successfully: " + objectName);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Image uploaded: " + objectName, Toast.LENGTH_SHORT).show());
                tempFile.delete();

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
            }
        }
    }
}
