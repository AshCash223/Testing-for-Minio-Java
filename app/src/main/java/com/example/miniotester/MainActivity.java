package com.example.miniotester;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ning the MinIO connection in a background task
        new MinioTask().execute();
    }
    private class MinioTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Initialising MinIO client
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESS_KEY, SECRET_KEY)
                    .build();
            return null;
        }
    }
}