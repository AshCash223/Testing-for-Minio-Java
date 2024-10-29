package com.example.miniotester;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MinioHelloWorld";
    private static final String ENDPOINT = "https://z4-n1.dv.exaba.io:9000";
    private static final String ACCESS_KEY = "JC5QAFBT2OUHJRJJ";
    private static final String SECRET_KEY = "IeQWblMFbQ3RjAjEBMCIOqwSSbYgs2PdMicxwGjR";

    private TextView tv1;  // Declare TextView variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize tv1 after setting the content view
        tv1 = findViewById(R.id.tv1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Running the MinIO connection in a background task
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

                // List buckets and log their names
                minioClient.listBuckets().forEach(bucket -> {
                    Log.d(TAG, "Bucket: " + bucket.name());
                    buckets.append(bucket.name()).append("\n"); // Append each bucket name with a newline
                });

            } catch (MinioException e) {
                Log.e(TAG, "MinIO Exception: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception: " + e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "No Such Algorithm: " + e.getMessage(), e);
            } catch (InvalidKeyException e) {
                Log.e(TAG, "Invalid Key: " + e.getMessage(), e);
            }
            return buckets.toString(); // Return the concatenated bucket names
        }

        @Override
        protected void onPostExecute(String result) { // Override onPostExecute
            super.onPostExecute(result);
            tv1.setText(result); // Update the TextView with bucket names
        }
    }
}
